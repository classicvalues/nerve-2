package io.nuls.crosschain.nuls.servive.impl;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.BroadCtxHashMessage;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.base.model.bo.txdata.VerifierChangeData;
import io.nuls.crosschain.base.model.bo.txdata.VerifierInitData;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.constant.ParamConstant;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.model.po.SendCtxHashPO;
import io.nuls.crosschain.nuls.model.po.VerifierChangeSendFailPO;
import io.nuls.crosschain.nuls.rpc.call.ConsensusCall;
import io.nuls.crosschain.nuls.rpc.call.NetWorkCall;
import io.nuls.crosschain.nuls.servive.BlockService;
import io.nuls.crosschain.nuls.srorage.*;
import io.nuls.crosschain.nuls.utils.MessageUtil;
import io.nuls.crosschain.nuls.utils.TxUtil;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;

import java.util.*;

import static io.nuls.crosschain.nuls.constant.NulsCrossChainErrorCode.*;
import static io.nuls.crosschain.nuls.constant.ParamConstant.CHAIN_ID;
import static io.nuls.crosschain.nuls.constant.ParamConstant.NEW_BLOCK_HEIGHT;

/**
 * 提供给区块模块调用的接口实现类
 * @author tag
 * @date 2019/4/25
 */
@Component
public class BlockServiceImpl implements BlockService {
    @Autowired
    private ChainManager chainManager;

    @Autowired
    private SendHeightService sendHeightService;

    @Autowired
    private SendedHeightService sendedHeightService;

    @Autowired
    private NulsCrossChainConfig config;

    @Autowired
    private ConvertCtxService convertCtxService;

    @Autowired
    private CtxStatusService ctxStatusService;

    @Autowired
    private VerifierChangeBroadFailedService verifierChangeBroadFailedService;

    @Override
    @SuppressWarnings("unchecked")
    public Result newBlockHeight(Map<String, Object> params) {
        Result result = paramValid(params);
        if(result.isFailed()){
            return result;
        }
        int chainId = (int) params.get(CHAIN_ID);
        Chain chain = chainManager.getChainMap().get(chainId);
        long height = Long.valueOf(params.get(NEW_BLOCK_HEIGHT).toString());
        chain.getLogger().info("收到区块高度更新信息，最新区块高度为：{}", height);
        //查询是否有待广播的跨链交易
        Map<Long , SendCtxHashPO> sendHeightMap = sendHeightService.getList(chainId);
        if(sendHeightMap != null && sendHeightMap.size() >0){
            Set<Long> sortSet = new TreeSet<>(sendHeightMap.keySet());
            Map<Integer,Byte> crossStatusMap = new HashMap<>(NulsCrossChainConstant.INIT_CAPACITY_16);
            for (long cacheHeight:sortSet) {
                if(height >= cacheHeight){
                    chain.getLogger().debug("广播区块高度为{}的跨链交易给其他链",cacheHeight );
                    SendCtxHashPO po = sendHeightMap.get(cacheHeight);
                    List<NulsHash> broadSuccessCtxHash = new ArrayList<>();
                    List<NulsHash> broadFailCtxHash = new ArrayList<>();
                    for (NulsHash ctxHash:po.getHashList()) {
                        if(broadCtxHash(chain, ctxHash, cacheHeight, crossStatusMap)){
                            broadSuccessCtxHash.add(ctxHash);
                        }else{
                            broadFailCtxHash.add(ctxHash);
                        }
                    }
                    if(broadSuccessCtxHash.size() > 0){
                        SendCtxHashPO sendedPo = sendedHeightService.get(cacheHeight,chainId);
                        if(sendedPo != null){
                            sendedPo.getHashList().addAll(broadSuccessCtxHash);
                        }else{
                            sendedPo = new SendCtxHashPO(broadSuccessCtxHash);
                        }
                        if(!sendedHeightService.save(cacheHeight, sendedPo, chainId)){
                            continue;
                        }
                    }
                    if(broadFailCtxHash.size() > 0){
                        po.setHashList(broadFailCtxHash);
                        sendHeightService.save(cacheHeight, po, chainId);
                        chain.getLogger().error("区块高度为{}的跨链交易广播失败",cacheHeight);
                    }else{
                        sendHeightService.delete(cacheHeight, chainId);
                        chain.getLogger().info("区块高度为{}的跨链交易广播成功",cacheHeight);
                    }
                }else{
                    break;
                }
            }
        }
        chain.getLogger().debug("区块高度更新消息处理完成,Height:{}\n\n",height);
        return Result.getSuccess(SUCCESS);
    }

    private Result paramValid(Map<String, Object> params){
        if (params.get(CHAIN_ID) == null || params.get(NEW_BLOCK_HEIGHT) == null || params.get(ParamConstant.PARAM_BLOCK_HEADER) == null) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        int chainId = (int) params.get(CHAIN_ID);
        if (chainId <= 0) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(CHAIN_NOT_EXIST);
        }
        try {
            BlockHeader blockHeader = new BlockHeader();
            String headerHex = (String) params.get(ParamConstant.PARAM_BLOCK_HEADER);
            blockHeader.parse(RPCUtil.decode(headerHex), 0);
            if(!chainManager.isCrossNetUseAble()){
                chainManager.getChainHeaderMap().put(chainId, blockHeader);
                return Result.getSuccess(SUCCESS);
            }
            if(config.isMainNet() && chainManager.getRegisteredCrossChainList().size() <= 1){
                chainManager.getChainHeaderMap().put(chainId, blockHeader);
                return Result.getSuccess(SUCCESS);
            }
            /*
            检测是否有轮次变化，如果有轮次变化，查询共识模块共识节点是否有变化，如果有变化则创建验证人变更交易
            */
            Map<String,List<String>> agentChangeMap;
            BlockHeader localHeader = chainManager.getChainHeaderMap().get(chainId);
            if(localHeader != null){
                BlockExtendsData blockExtendsData = blockHeader.getExtendsData();
                BlockExtendsData localExtendsData = localHeader.getExtendsData();
                if(blockExtendsData.getRoundIndex() == localExtendsData.getRoundIndex()){
                    chainManager.getChainHeaderMap().put(chainId, blockHeader);
                    return Result.getSuccess(SUCCESS);
                }
                agentChangeMap = ConsensusCall.getAgentChangeInfo(chain, localHeader.getExtend(), blockHeader.getExtend());
            }else{
                agentChangeMap = ConsensusCall.getAgentChangeInfo(chain, null, blockHeader.getExtend());
            }
            if(agentChangeMap != null){
                List<String> registerAgentList = agentChangeMap.get(ParamConstant.PARAM_REGISTER_AGENT_LIST);
                List<String> cancelAgentList = agentChangeMap.get(ParamConstant.PARAM_CANCEL_AGENT_LIST);
                boolean verifierChange = (registerAgentList != null && !registerAgentList.isEmpty()) || (cancelAgentList != null && !cancelAgentList.isEmpty());
                if(verifierChange){
                    //验证人列表信息
                    chain.getVerifierList().removeAll(cancelAgentList);
                    chain.getLogger().info("有验证人变化，创建验证人变化交易，最新轮次与上一轮共有的出块地址为：", chain.getVerifierList().toString());
                    Transaction verifierChangeTx = TxUtil.createVerifierChangeTx(registerAgentList, cancelAgentList, blockHeader.getExtendsData().getRoundStartTime(),chainId);
                    TxUtil.handleNewCtx(verifierChangeTx, chain, registerAgentList);
                }
            }
            chainManager.getChainHeaderMap().put(chainId, blockHeader);
        }catch (Exception e){
            chain.getLogger().error(e);
            return Result.getFailed(DATA_PARSE_ERROR);
        }
        return Result.getSuccess(SUCCESS);
    }

    private boolean broadCtxHash(Chain chain,NulsHash ctxHash, long cacheHeight, Map<Integer,Byte> crossStatusMap){
        int chainId = chain.getChainId();
        BroadCtxHashMessage message = new BroadCtxHashMessage();
        message.setConvertHash(ctxHash);
        Transaction ctx = ctxStatusService.get(ctxHash, chainId).getTx();
        try {
            if(ctx.getType() == config.getCrossCtxType() || ctx.getType() == TxType.CONTRACT_TOKEN_CROSS_TRANSFER){
                int toId = chainId;
                if(config.isMainNet()){
                    toId = AddressTool.getChainIdByAddress(ctx.getCoinDataInstance().getTo().get(0).getAddress());
                }else{
                    NulsHash convertHash = TxUtil.friendConvertToMain(chain, ctx, null, TxType.CROSS_CHAIN).getHash();
                    message.setConvertHash(convertHash);
                    chain.getLogger().info("广播跨链转账交易给主网，本链协议hash:{}对应的主网协议hash:{}",ctxHash.toHex(),convertHash.toHex());
                }
                byte broadStatus;
                if(crossStatusMap.containsKey(toId)){
                    broadStatus = crossStatusMap.get(toId);
                }else{
                    broadStatus = MessageUtil.canSendMessage(chain,toId);
                    crossStatusMap.put(toId, broadStatus);
                }
                if (broadStatus == 0) {
                    return true;
                }else if(broadStatus == 1){
                    return false;
                }
                return NetWorkCall.broadcast(toId, message, CommandConstant.BROAD_CTX_HASH_MESSAGE,true);
            }else if(ctx.getType() == TxType.VERIFIER_CHANGE){
                if(!chain.isMainChain()){
                    byte broadStatus;
                    if(crossStatusMap.containsKey(chainId)){
                        broadStatus = crossStatusMap.get(chainId);
                    }else{
                        broadStatus = MessageUtil.canSendMessage(chain,chainId);
                        crossStatusMap.put(chainId, broadStatus);
                    }
                    if (broadStatus == 0) {
                        return true;
                    }else if(broadStatus == 1){
                        return false;
                    }
                    boolean broadResult = NetWorkCall.broadcast(chainId, message, CommandConstant.BROAD_CTX_HASH_MESSAGE,true);
                    if(broadResult){
                        //更新本地验证人列表
                        VerifierChangeData verifierChangeData = new VerifierChangeData();
                        verifierChangeData.parse(ctx.getTxData(),0);
                        if(verifierChangeData.getCancelAgentList() != null && !verifierChangeData.getCancelAgentList().isEmpty()){
                            chain.getBroadcastVerifierList().removeAll(verifierChangeData.getCancelAgentList());
                        }
                        if(verifierChangeData.getRegisterAgentList() != null && !verifierChangeData.getRegisterAgentList().isEmpty()){
                            chain.getBroadcastVerifierList().addAll(verifierChangeData.getRegisterAgentList());
                        }
                        chain.setVerifierList(new ArrayList<>(chain.getBroadcastVerifierList()));
                        chain.getLogger().info("验证人变更，当前最新验证人列表为：{}",chain.getVerifierList().toString());
                    }
                    return broadResult;
                }else{
                    boolean broadResult = true;
                    if(chainManager.getRegisteredCrossChainList() == null || chainManager.getRegisteredCrossChainList().isEmpty() || chainManager.getRegisteredCrossChainList().size() == 1){
                        chain.getLogger().info("没有注册链信息");
                        return true;
                    }

                    VerifierChangeSendFailPO po = verifierChangeBroadFailedService.get(cacheHeight, chainId);
                    Set<Integer> broadFailChains = new HashSet<>();
                    if(po != null){
                        for (Integer toChainId : po.getChains()) {
                            byte broadStatus;
                            if(crossStatusMap.containsKey(chainId)){
                                broadStatus = crossStatusMap.get(chainId);
                            }else{
                                broadStatus = MessageUtil.canSendMessage(chain,chainId);
                                crossStatusMap.put(chainId, broadStatus);
                            }
                            if (broadStatus == 1) {
                                broadResult = false;
                                broadFailChains.add(toChainId);
                            }else if(broadStatus == 2){
                                if(!NetWorkCall.broadcast(toChainId, message, CommandConstant.BROAD_CTX_HASH_MESSAGE,true)){
                                    broadResult = false;
                                    broadFailChains.add(toChainId);
                                }
                            }
                        }
                    }else{
                        for (ChainInfo chainInfo:chainManager.getRegisteredCrossChainList()) {
                            int  toChainId = chainInfo.getChainId();
                            if(toChainId == chainId){
                                continue;
                            }
                            byte broadStatus;
                            if(crossStatusMap.containsKey(chainInfo.getChainId())){
                                broadStatus = crossStatusMap.get(chainInfo.getChainId());
                            }else{
                                broadStatus = MessageUtil.canSendMessage(chain,chainInfo.getChainId());
                                crossStatusMap.put(chainInfo.getChainId(), broadStatus);
                            }
                            if (broadStatus == 1) {
                                broadResult = false;
                                broadFailChains.add(toChainId);
                            }else if(broadStatus == 2){
                                if(!NetWorkCall.broadcast(toChainId, message, CommandConstant.BROAD_CTX_HASH_MESSAGE,true)){
                                    broadResult = false;
                                    broadFailChains.add(toChainId);
                                }
                            }
                        }
                    }
                    if(broadFailChains.isEmpty()){
                        verifierChangeBroadFailedService.delete(cacheHeight, chainId);
                    }else{
                        VerifierChangeSendFailPO failPO = new VerifierChangeSendFailPO(broadFailChains);
                        verifierChangeBroadFailedService.save(cacheHeight, failPO, chainId);
                    }
                    return broadResult;
                }
            }else if(ctx.getType() == TxType.VERIFIER_INIT){
                VerifierInitData verifierInitData = new VerifierInitData();
                verifierInitData.parse(ctx.getTxData(),0);
                return NetWorkCall.broadcast(verifierInitData.getRegisterChainId(), message, CommandConstant.BROAD_CTX_HASH_MESSAGE,true);
            }
            return true;
        }catch (Exception e){
            chain.getLogger().error(e);
            return false;
        }
    }
}