package network.nerve.pocbft.utils.manager;

import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import network.nerve.pocbft.constant.ConsensusConstant;
import network.nerve.pocbft.constant.ConsensusErrorCode;
import network.nerve.pocbft.model.bo.Chain;
import network.nerve.pocbft.rpc.call.CallMethodUtils;
import network.nerve.pocbft.utils.ConsensusAwardUtil;
import network.nerve.pocbft.utils.compare.BlockHeaderComparator;
import network.nerve.pocbft.v1.RoundController;

import java.util.Iterator;
import java.util.List;

/**
 * 链区块管理类
 * Chain Block Management Class
 *
 * @author tag
 * 2018/12/20
 */
@Component
public class BlockManager {

    @Autowired
    private PunishManager punishManager;

    @Autowired
    private AgentManager agentManager;

    /**
     * 收到最新区块头，更新链区块缓存数据
     * Receive the latest block header, update the chain block cache entity
     *
     * @param chain       chain info
     * @param blockHeader block header
     */
    public void addNewBlock(Chain chain, BlockHeader blockHeader, int download) {
        //保存共识奖励结算信息
        ConsensusAwardUtil.saveAndSwitchSettleRecord(chain, blockHeader);
        /*
        如果新增区块有轮次变化，则删除最小轮次区块
         */
        BlockHeader newestHeader = chain.getBestHeader();
        BlockExtendsData newestExtendsData = newestHeader.getExtendsData();
        BlockExtendsData receiveExtendsData = blockHeader.getExtendsData();
        long receiveRoundIndex = receiveExtendsData.getRoundIndex();
        if (chain.getBlockHeaderList().size() > 0) {
            BlockExtendsData lastExtendsData = chain.getBlockHeaderList().get(0).getExtendsData();
            long lastRoundIndex = lastExtendsData.getRoundIndex();
            if (receiveRoundIndex > newestExtendsData.getRoundIndex() && (receiveRoundIndex - ConsensusConstant.INIT_BLOCK_HEADER_COUNT > lastRoundIndex)) {
                Iterator<BlockHeader> iterator = chain.getBlockHeaderList().iterator();
                while (iterator.hasNext()) {
                    lastExtendsData = iterator.next().getExtendsData();
                    if (lastExtendsData.getRoundIndex() == lastRoundIndex) {
                        iterator.remove();
                    } else if (lastExtendsData.getRoundIndex() > lastRoundIndex) {
                        break;
                    }
                }
                //清理轮次缓存
                punishManager.clear(chain);
            }
        }

        //这里切换轮次
        RoundController roundController = SpringLiteContext.getBean(RoundController.class);
        roundController.switchPackingIndex(receiveExtendsData.getRoundIndex(), receiveExtendsData.getRoundStartTime(),
                receiveExtendsData.getPackingIndexOfRound() + 1, blockHeader.getTime() + chain.getConfig().getPackingInterval());

        chain.getBlockHeaderList().add(blockHeader);
        chain.setBestHeader(blockHeader);
        chain.getLogger().info("区块保存，高度为：" + blockHeader.getHeight() + " , txCount: " + blockHeader.getTxCount() + ",本地最新区块高度为：" + chain.getBestHeader().getHeight() + ", 轮次:" + receiveExtendsData.getRoundIndex() + "\n\n");


        //如果存在没保存公钥节点则保存节点公钥
        if (!chain.getUnBlockAgentList().isEmpty()) {
            agentManager.setPubkey(chain, blockHeader.getBlockSignature().getPublicKey());
        }

// 准备修改共识奖励计算的触发器
//        chain.getConsensusCache().getBlockHeaderQueue().offer(blockHeader);

    }

    /**
     * 链分叉，区块回滚
     * Chain bifurcation, block rollback
     *
     * @param chain  chain info
     * @param height block height
     */
    public void chainRollBack(Chain chain, int height) {
        if (height > 0) {
            throw new NulsRuntimeException(ConsensusErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        chain.getLogger().info("区块开始回滚，回滚到的高度：" + height);
        List<BlockHeader> headerList = chain.getBlockHeaderList();
        headerList.sort(new BlockHeaderComparator());
        BlockHeader originalBlocHeader = chain.getBestHeader();
        BlockExtendsData originalExtendsData = originalBlocHeader.getExtendsData();
        long originalRound = originalExtendsData.getRoundIndex();
        BlockHeader rollBackHeader;
        for (int index = headerList.size() - 1; index >= 0; index--) {
            if (headerList.get(index).getHeight() >= height) {
                rollBackHeader = headerList.remove(index);
                ConsensusAwardUtil.rollbackAndSwitchSettleRecord(chain, rollBackHeader, headerList.get(index - 1));
            } else {
                break;
            }
        }
        chain.setBlockHeaderList(headerList);
        chain.setBestHeader(headerList.get(headerList.size() - 1));
        BlockHeader newestBlocHeader = chain.getBestHeader();
        BlockExtendsData bestExtendsData = newestBlocHeader.getExtendsData();
        long currentRound = bestExtendsData.getRoundIndex();
        //如果有轮次变化，回滚之后如果本地区块不足指定轮次的区块，则需向区块获取区块补足并回滚本地
        if (currentRound != originalRound) {
            BlockHeader lastestBlocHeader = chain.getBlockHeaderList().get(0);
            BlockExtendsData lastestExtendsData = lastestBlocHeader.getExtendsData();
            long minRound = lastestExtendsData.getRoundIndex();
            int localRoundCount = (int) (currentRound - minRound + 1);
            int diffRoundCount = ConsensusConstant.INIT_BLOCK_HEADER_COUNT - localRoundCount;
            if (diffRoundCount > 0) {
                try {
                    CallMethodUtils.getRoundBlockHeaders(chain, diffRoundCount, lastestBlocHeader.getHeight());
                } catch (Exception e) {
                    chain.getLogger().error(e);
                }
            }
            long roundIndex;
            //回滚轮次
            if (bestExtendsData.getPackingIndexOfRound() > 1) {
                roundIndex = bestExtendsData.getRoundIndex();
            } else {
                roundIndex = bestExtendsData.getRoundIndex() - 1;
            }
//            roundManager.rollBackRound(chain, roundIndex);
        }
        chain.getLogger().info("区块回滚成功，回滚到的高度为：" + height + ",本地最新区块高度为：" + chain.getBestHeader().getHeight());
    }
}
