package network.nerve.converter.rpc.call;

import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import network.nerve.converter.constant.ConverterErrorCode;
import network.nerve.converter.utils.LoggerUtil;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class SwapCall extends BaseCall {

    public static boolean isLegalCoinForAddStable(int chainId, String stablePairAddress, int assetChainId, int assetId) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);

            params.put("stablePairAddress", stablePairAddress);
            params.put("assetChainId", assetChainId);
            params.put("assetId", assetId);
            Map result = (Map) requestAndResponse(ModuleE.SW.abbr, "sw_is_legal_coin_for_add_stable", params);
            if (result == null || result.get("value") == null) {
                return false;
            }
            return (boolean) result.get("value");
        } catch (Exception e) {
            String msg = MessageFormat.format("Calling remote interface failed. module:{0} - interface:{1}", ModuleE.SW.abbr, "sw_is_legal_coin_for_add_stable");
            LoggerUtil.LOG.error(msg, e);
            return false;
        }
    }


    public static void addCoinForAddStable(int chainId, String stablePairAddress, int assetChainId, int assetId) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);

            params.put("stablePairAddress", stablePairAddress);
            params.put("assetChainId", assetChainId);
            params.put("assetId", assetId);
            boolean success;
            Map result = (Map) requestAndResponse(ModuleE.SW.abbr, "sw_add_coin_for_stable", params);
            if (result == null || result.get("value") == null) {
                success = false;
            } else {
                success = (boolean) result.get("value");
            }
            if (!success) {
                LoggerUtil.LOG.error("[提案添加币种] 币种添加失败. stablePairAddress: {}, asset:{}-{}", stablePairAddress, assetChainId, assetId);
                throw new NulsException(ConverterErrorCode.DATA_ERROR);
            }
        } catch (Exception e) {
            String msg = MessageFormat.format("Calling remote interface failed. module:{0} - interface:{1}", ModuleE.SW.abbr, "sw_add_coin_for_stable");
            LoggerUtil.LOG.error(msg, e);
            throw new NulsException(ConverterErrorCode.DATA_ERROR);
        }
    }
}
