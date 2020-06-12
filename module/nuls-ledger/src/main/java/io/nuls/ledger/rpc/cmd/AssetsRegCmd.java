/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
package io.nuls.ledger.rpc.cmd;

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.ledger.config.LedgerConfig;
import io.nuls.ledger.constant.CmdConstant;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.model.po.LedgerAsset;
import io.nuls.ledger.service.AssetRegMngService;
import io.nuls.ledger.utils.LoggerUtil;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 资产登记与管理接口
 *
 * @author lanjinsheng .
 * @date 2019/10/22
 */
@Component
public class AssetsRegCmd extends BaseLedgerCmd {
    @Autowired
    LedgerConfig ledgerConfig;
    @Autowired
    AssetRegMngService assetRegMngService;


    /**
     * 链内异构链资产登记接口
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_CHAIN_ASSET_HETEROGENEOUS_REG, version = 1.0,
            description = "链内异构链资产登记接口")
    @Parameters(value = {
            @Parameter(parameterName = "assetName", requestType = @TypeDescriptor(value = String.class), parameterDes = "资产名称: 大、小写字母、数字、下划线（下划线不能在两端）1~20字节"),
            @Parameter(parameterName = "initNumber", requestType = @TypeDescriptor(value = BigInteger.class), parameterDes = "资产初始值"),
            @Parameter(parameterName = "decimalPlace", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-18]", parameterDes = "资产最小分割位数"),
            @Parameter(parameterName = "assetSymbol", requestType = @TypeDescriptor(value = String.class), parameterDes = "资产单位符号: 大、小写字母、数字、下划线（下划线不能在两端）1~20字节"),
            @Parameter(parameterName = "address", requestType = @TypeDescriptor(value = String.class), parameterDes = "新资产地址"),
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "chainId", valueType = int.class, description = "链id"),
                    @Key(name = "assetId", valueType = int.class, description = "资产id")
            })
    )
    public Response chainAssetHeterogeneousReg(Map params) {
        Map<String, Object> rtMap = new HashMap<>(4);
        try {
            LoggerUtil.COMMON_LOG.debug("Heterogeneous asset register params={}", JSONUtils.obj2json(params));
            params.put("chainId", ledgerConfig.getChainId());
            LedgerAsset asset = new LedgerAsset();
            asset.map2pojo(params, LedgerConstant.HETEROGENEOUS_CROSS_CHAIN_ASSET_TYPE);
            int assetId = assetRegMngService.registerHeterogeneousAsset(asset.getChainId(), asset);
            rtMap.put("assetId", assetId);
            rtMap.put("chainId", asset.getChainId());
            LoggerUtil.COMMON_LOG.debug("return={}", JSONUtils.obj2json(rtMap));
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            return failed(e.getMessage());
        }
        return success(rtMap);
    }

    /**
     * 链内异构链资产登记回滚
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_CHAIN_ASSET_HETEROGENEOUS_ROLLBACK, version = 1.0,
            description = "链内异构链资产登记回滚")
    @Parameters(value = {
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterDes = "资产Id"),
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "value", valueType = boolean.class, description = "成功true,失败false")
            })
    )
    public Response chainAssetHeterogeneousRollBack(Map params) {
        Map<String, Object> rtMap = new HashMap<>(1);
        try {
            assetRegMngService.rollBackHeterogeneousAsset(ledgerConfig.getChainId(), Integer.parseInt(params.get("assetId").toString()));
            rtMap.put("value", true);
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            return failed(e.getMessage());
        }
        return success(rtMap);
    }

    /**
     * 查看链内注册资产信息
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_CHAIN_ASSET_REG_INFO, version = 1.0,
            description = "查看链内注册资产信息")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "运行链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "assetType", requestType = @TypeDescriptor(value = int.class), parameterDes = "资产类型")

    })
    @ResponseData(name = "返回值", description = "返回一个list对象",
            responseType = @TypeDescriptor(value = List.class, collectionElement = Map.class, mapKeys = {
                    @Key(name = "assetId", valueType = int.class, description = "资产id"),
                    @Key(name = "assetType", valueType = int.class, description = "资产类型"),
                    @Key(name = "assetOwnerAddress", valueType = String.class, description = "资产所有者地址"),
                    @Key(name = "initNumber", valueType = BigInteger.class, description = "资产初始化值"),
                    @Key(name = "decimalPlace", valueType = int.class, description = "小数点分割位数"),
                    @Key(name = "assetName", valueType = String.class, description = "资产名"),
                    @Key(name = "assetSymbol", valueType = String.class, description = "资产符号"),
                    @Key(name = "txHash", valueType = String.class, description = "交易hash值")
            })
    )
    public Response getAssetRegInfo(Map params) {
        Map<String, Object> rtMap = new HashMap<>(1);
        try {
            if (null == params.get("assetType")) {
                params.put("assetType", "0");
            }
            List<Map<String, Object>> assets = assetRegMngService.getLedgerRegAssets(Integer.valueOf(params.get("chainId").toString()), Integer.valueOf(params.get("assetType").toString()));
            rtMap.put("assets", assets);
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            return failed(e.getMessage());
        }
        return success(rtMap);
    }

    /**
     * 查看链内注册资产信息-通过资产id
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_CHAIN_ASSET_REG_INFO_BY_ASSETID, version = 1.0,
            description = "通过资产id查看链内注册资产信息")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "运行链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = String.class), parameterValidRange = "[1-65535]", parameterDes = "资产id")

    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "assetId", valueType = int.class, description = "资产id"),
                    @Key(name = "assetType", valueType = int.class, description = "资产类型"),
                    @Key(name = "assetOwnerAddress", valueType = String.class, description = "资产所有者地址"),
                    @Key(name = "initNumber", valueType = BigInteger.class, description = "资产初始化值"),
                    @Key(name = "decimalPlace", valueType = int.class, description = "小数点分割位数"),
                    @Key(name = "assetName", valueType = String.class, description = "资产名"),
                    @Key(name = "assetSymbol", valueType = String.class, description = "资产符号"),
                    @Key(name = "txHash", valueType = String.class, description = "交易hash值")
            })
    )
    public Response getAssetRegInfoByAssetId(Map params) {
        Map<String, Object> rtMap = new HashMap<>(1);
        try {
            rtMap = assetRegMngService.getLedgerRegAsset(Integer.valueOf(params.get("chainId").toString()), Integer.valueOf(params.get("assetId").toString()));
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            return failed(e.getMessage());
        }
        return success(rtMap);
    }
}
