package network.nerve.swap.rpc.cmd;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.TxType;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.Parameters;
import io.nuls.core.rpc.model.TypeDescriptor;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import network.nerve.swap.config.ConfigBean;
import network.nerve.swap.constant.SwapConstant;
import network.nerve.swap.model.Chain;
import network.nerve.swap.model.NerveToken;
import network.nerve.swap.model.bo.NonceBalance;
import network.nerve.swap.model.business.AddLiquidityBus;
import network.nerve.swap.model.business.RemoveLiquidityBus;
import network.nerve.swap.model.business.SwapTradeBus;
import network.nerve.swap.model.business.stable.StableAddLiquidityBus;
import network.nerve.swap.model.business.stable.StableRemoveLiquidityBus;
import network.nerve.swap.model.business.stable.StableSwapTradeBus;
import network.nerve.swap.rpc.call.LedgerCall;
import network.nerve.swap.utils.SwapDBUtil;
import network.nerve.swap.utils.SwapUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static network.nerve.swap.constant.SwapCmdConstant.*;

public class SwapTxSendTest {
    static String awardFeeSystemAddressPublicKey = "d60fc83130dbe5537d4f1e1e35c533f1a396b8b7d641d717b2d1eb1245d0d796";
    static String awardFeeSystemAddress;
    static String address20 = "TNVTdTSPVcqUCdfVYWwrbuRtZ1oM6GpSgsgF5";
    static String address21 = "TNVTdTSPNEpLq2wnbsBcD8UDTVMsArtkfxWgz";
    static String address22 = "TNVTdTSPRyJgExG4HQu5g1sVxhVVFcpCa6fqw";
    static String address23 = "TNVTdTSPUR5vYdstWDHfn5P8MtHB6iZZw3Edv";
    static String address24 = "TNVTdTSPPXtSg6i5sPPrSg3TfFrhYHX5JvMnD";
    static String address25 = "TNVTdTSPT5KdmW1RLzRZCa5yc7sQCznp6fES5";
    static String address26 = "TNVTdTSPPBao2pGRc5at7mSdBqnypJbMqrKMg";
    static String address27 = "TNVTdTSPLqKoNh2uiLAVB76Jyq3D6h3oAR22n";
    static String address28 = "TNVTdTSPNkjaFbabm5P73m7VHBRQef4NDsgYu";
    static String address29 = "TNVTdTSPRMtpGNYRx98WkoqKnExU9pWDQjNPf";
    static String address30 = "TNVTdTSPEn3kK94RqiMffiKkXTQ2anRwhN1J9";
    static String address31 = "TNVTdTSPRnXkDiagy7enti1KL75NU5AxC9sQA";
    static String address32 = "TNVTdTSPQj7T5LiVmL974o2YRWa1YPJJzJHhn";
    static String address33 = "TNVTdTSPKy4iLwK6XC52VNqVSnk1vncF5Z2mu";
    static String address34 = "TNVTdTSPRgkZKoNaeAUj6H3UWH29D5ftv7LNN";
    static String address35 = "TNVTdTSPVhoYssF5cgMVGWRYsdai9KLs9rotk";
    static String address36 = "TNVTdTSPN9pNrMMEmhZsNYVn9Lcyu3cxSUbAL";
    static String address37 = "TNVTdTSPLnyxJ4gWi3L4mr6sSQrcfqLqPbCkP";
    static String address38 = "TNVTdTSPJiFqnqW2sGNVZ1do2C6tFoLv7DBgE";
    static String address39 = "TNVTdTSPTgPV9AjgQKFvdT1eviWisVMG7Naah";
    static String address40 = "TNVTdTSPGvLeBDxQiWRH3jZTcrYKwSF2axCfy";
    static String address41 = "TNVTdTSPPyoYQNDgfbF83P3kWJz9bvrNej1RW";
    static String address42 = "TNVTdTSPTNWUw7YiRLuwpFiPiUcpYQbzRU8LT";
    static String address43 = "TNVTdTSPSxopb3jVAdDEhx49S6iaA2CiPa3oa";
    static String address44 = "TNVTdTSPNc8YhE5h7Msd8R9Vebd5DG9W38Hd6";
    static String address45 = "TNVTdTSPPDMJA6eFRAb47vC2Lzx662nj3VVhg";
    static String address46 = "TNVTdTSPMXhkD6FJ9htA9H3aDEVVg8DNoriur";
    static String address47 = "TNVTdTSPPENjnLifQrJ4EK6tWp1HaDhnW5h7y";
    static String address48 = "TNVTdTSPLmeuz7aVsdb2WTcGXKFmcKowTfk46";
    static String address49 = "TNVTdTSPF1mBVywX7BR674SZbaHBn3JoPhyJi";
    static String address50 = "TNVTdTSPHR7jCTZwtEB6FS1BZuBe7RVjshEsB";
    static String address51 = "TNVTdTSPRrYndMR8JZ4wJovLDbRp2o4gGWDAp";

    static String agentAddress;
    static String packageAddress;
    static String packageAddressPrivateKey;
    String packageAddressZP = "TNVTdTSPLEqKWrM7sXUciM2XbYPoo3xDdMtPd";
    String packageAddressNE = "TNVTdTSPNeoGxTS92S2r1DZAtJegbeucL8tCT";
    String packageAddressHF = "TNVTdTSPLpegzD3B6qaVKhfj6t8cYtnkfR7Wx";// 0x16534991E80117Ca16c724C991aad9EAbd1D7ebe
    String packageAddress6 = "TNVTdTSPF9nBiba1vk4PqRkyQaYqwoAJX95xn";// 0x8F05AE1C759b8dB56ff8124A89bb1305ECe17B65
    String packageAddress7 = "TNVTdTSPKDqbuQc6cF3m41CcQKRvzmXSQzouy";// 0xd29E172537A3FB133f790EBE57aCe8221CB8024F
    String packageAddress8 = "TNVTdTSPS9g9pGmjEo2gjjGKsNBGc22ysz25a";// 0x54eAB3868B0090E6e1a1396E0e54F788a71B2b17
    String packageAddressPrivateKeyZP = "b54db432bba7e13a6c4a28f65b925b18e63bcb79143f7b894fa735d5d3d09db5";
    String packageAddressPrivateKeyNE = "188b255c5a6d58d1eed6f57272a22420447c3d922d5765ebb547bc6624787d9f";
    String packageAddressPrivateKeyHF = "fbcae491407b54aa3904ff295f2d644080901fda0d417b2b427f5c1487b2b499";
    String packageAddressPrivateKey6 = "43DA7C269917207A3CBB564B692CD57E9C72F9FCFDB17EF2190DD15546C4ED9D";
    String packageAddressPrivateKey7 = "0935E3D8C87C2EA5C90E3E3A0509D06EB8496655DB63745FAE4FF01EB2467E85";
    String packageAddressPrivateKey8 = "CCF560337BA3DE2A76C1D08825212073B299B115474B65DE4B38B587605FF7F2";

    private Chain chain;
    static int chainId = 5;
    static int assetId = 1;
    static int ethChainId = 101;
    private String from;
    static String version = "1.0";
    static String password = "nuls123456";//"nuls123456";

    static String OKUSD_OKT_8 = "0x10B382863647C4610050A69fBc1E582aC29fE58A";
    static String HUSD_HT_18 = "0x10B382863647C4610050A69fBc1E582aC29fE58A";
    static String BUSD_BNB_18 = "0x02e1aFEeF2a25eAbD0362C4Ba2DC6d20cA638151";
    static String USDX_ETH = "0xB058887cb5990509a3D0DD2833B2054E4a7E4a55";
    static String USDX_BNB = "0xb6D685346106B697E6b2BbA09bc343caFC930cA3";
    static String USDX_HT = "0x03Cf96223BD413eb7777AFE3cdb689e7E851CB32";
    static String USDX_OKT = "0x74A163fCd791Ec7AaB2204ffAbf1A1DFb8854883";

    static String DXA_BNB_8 = "0x3139dbe1bf7feb917cf8e978b72b6ead764b0e6c";
    static String GOAT_BNB_9 = "0xba0147e9c99b0467efe7a9c51a2db140f1881db5";
    static String SAFEMOON_BNB_9 = "0x7be69eb38443d3a632cb972df840013d667365e6";

    //int ethAssetId = 0;
    //int bscAssetId = 0;
    //int htAssetId = 0;
    //int oktAssetId = 0;
    //int swapLpAssetId_nvt8usdx_eth;
    //int swapLpAssetId_nvt8usdx_bnb;
    //int swapLpAssetId_dxa8usdx_bnb;
    //int swapLpAssetId_goat9usdx_bnb;
    //int swapLpAssetId_nvt8safemoon9;
    //int swapLpAssetId_goat9safemoon9;
    int stableLpAssetId = 19;
    protected NerveToken nvt = new NerveToken(chainId, 1);
    protected NerveToken usdx_eth;
    protected NerveToken usdx_bnb;
    protected NerveToken usdx_ht;
    protected NerveToken usdx_okt;

    protected NerveToken dxa8_bnb;
    protected NerveToken goat9_bnb;
    protected NerveToken safemoon9_bnb;

    protected NerveToken busd_18;
    protected NerveToken husd_18;
    protected NerveToken okusd_8;

    protected NerveToken swap_lp_nvt8usdx_eth;
    protected NerveToken swap_lp_nvt8usdx_bnb;
    protected NerveToken swap_lp_dxa8usdx_bnb;
    protected NerveToken swap_lp_goat9usdx_bnb;
    protected NerveToken swap_lp_nvt8safemoon9;
    protected NerveToken swap_lp_goat9safemoon9;
    protected NerveToken stable_swap_lp = new NerveToken(chainId, stableLpAssetId);
    protected String stablePairAddress = "TNVTdTSQg3efAdkXb3T7Q8AbTa6DsxYuQXa8f";

    @BeforeClass
    public static void beforeClass() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger("io.netty");
        logger.setAdditive(false);
        logger.setLevel(Level.ERROR);
    }

    @Before
    public void before() throws Exception {
        AddressTool.addPrefix(5, "TNVT");
        NoUse.mockModule();
        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":8771");
        chain = new Chain();
        chain.setConfig(new ConfigBean(chainId, assetId, "UTF-8"));
        from = address31;
        awardFeeSystemAddress = AddressTool.getStringAddressByBytes(AddressTool.getAddressByPubKeyStr(awardFeeSystemAddressPublicKey, chainId));

        usdx_eth = this.findAssetIdByAddress(101, USDX_ETH);
        usdx_bnb = this.findAssetIdByAddress(102, USDX_BNB);
        usdx_ht = this.findAssetIdByAddress(103, USDX_HT);
        usdx_okt = this.findAssetIdByAddress(104, USDX_OKT);
        goat9_bnb = this.findAssetIdByAddress(102, GOAT_BNB_9);
        dxa8_bnb = this.findAssetIdByAddress(102, DXA_BNB_8);
        safemoon9_bnb = this.findAssetIdByAddress(102, SAFEMOON_BNB_9);
        busd_18 = this.findAssetIdByAddress(102, BUSD_BNB_18);
        husd_18 = this.findAssetIdByAddress(103, HUSD_HT_18);
        okusd_8 = this.findAssetIdByAddress(104, OKUSD_OKT_8);
        swap_lp_nvt8usdx_eth = this.getPairLPToken(nvt.str(), usdx_eth.str());
        swap_lp_nvt8usdx_bnb = this.getPairLPToken(nvt.str(), usdx_bnb.str());
        swap_lp_dxa8usdx_bnb = this.getPairLPToken(dxa8_bnb.str(), usdx_bnb.str());
        swap_lp_goat9usdx_bnb = this.getPairLPToken(goat9_bnb.str(), usdx_bnb.str());
        swap_lp_nvt8safemoon9 = this.getPairLPToken(nvt.str(), safemoon9_bnb.str());
        swap_lp_goat9safemoon9 = this.getPairLPToken(goat9_bnb.str(), safemoon9_bnb.str());
    }

    private NerveToken findAssetIdByAddress(int heterogeneousChainId, String contractAddress) throws Exception {
        return this.findAssetIdByAddress(heterogeneousChainId, contractAddress, false);
    }

    private NerveToken findAssetIdByAddress(int heterogeneousChainId, String contractAddress, boolean debug) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("heterogeneousChainId", heterogeneousChainId);
        params.put("contractAddress", contractAddress);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CV.abbr, "cv_get_heterogeneous_chain_asset_info_by_address", params);
        Map responseData = (Map) cmdResp.getResponseData();
        Map result = (Map) responseData.get("cv_get_heterogeneous_chain_asset_info_by_address");
        if (result == null) {
            return new NerveToken();
        }
        Integer chainId = (Integer) result.get("chainId");
        Integer assetId = (Integer) result.get("assetId");
        if (debug) {
            System.out.println(JSONUtils.obj2PrettyJson(cmdResp));
        }
        return new NerveToken(chainId, assetId);
    }

    private void create0(Map<String, Object> params) {
        params.put("tokenAStr", usdx_bnb.str());
        params.put("tokenBStr", nvt.str());
    }

    private void create1(Map<String, Object> params) {
        params.put("tokenAStr", usdx_eth.str());
        params.put("tokenBStr", nvt.str());
    }

    private void create2(Map<String, Object> params) {
        params.put("tokenAStr", usdx_bnb.str());
        params.put("tokenBStr", dxa8_bnb.str());
    }

    private void create3(Map<String, Object> params) {
        params.put("tokenAStr", usdx_bnb.str());
        params.put("tokenBStr", goat9_bnb.str());
    }

    private void create4(Map<String, Object> params) {
        params.put("tokenAStr", safemoon9_bnb.str());
        params.put("tokenBStr", nvt.str());
    }

    private void create5(Map<String, Object> params) {
        params.put("tokenAStr", safemoon9_bnb.str());
        params.put("tokenBStr", goat9_bnb.str());
    }

    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "账户地址"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "账户密码"),
            @Parameter(parameterName = "tokenAStr", parameterType = "String", parameterDes = "资产A的类型，示例：1-1"),
            @Parameter(parameterName = "tokenBStr", parameterType = "String", parameterDes = "资产B的类型，示例：1-1")
    })
    @Test
    public void swapCreatePair() throws Exception {
        Map<String, Object> params = new HashMap<>();
        //this.create0(params);
        this.create1(params);
        //this.create2(params);
        //this.create3(params);
        //this.create4(params);
        //this.create5(params);

        this.sendTx(from, SWAP_CREATE_PAIR, params);
    }

    private void addLiquidityNvt8usdx_bnb(Map<String, Object> params) {
        String amountA = "10000";
        String amountB = "535";
        amountA = new BigDecimal(amountA).scaleByPowerOfTen(8).toPlainString();
        amountB = new BigDecimal(amountB).scaleByPowerOfTen(6).toPlainString();
        params.put("tokenAStr", nvt.str());
        params.put("tokenBStr", usdx_bnb.str());
        params.put("amountA", amountA);
        params.put("amountB", amountB);
    }

    private void addLiquidityNvt8usdx_eth(Map<String, Object> params) {
        String amountA = "10000";
        String amountB = "535";
        amountA = new BigDecimal(amountA).scaleByPowerOfTen(8).toPlainString();
        amountB = new BigDecimal(amountB).scaleByPowerOfTen(6).toPlainString();
        params.put("tokenAStr", nvt.str());
        params.put("tokenBStr", usdx_eth.str());
        params.put("amountA", amountA);
        params.put("amountB", amountB);
    }

    private void addLiquidityDxa8usdx_bnb(Map<String, Object> params) {
        String amountA = "150";
        String amountB = "100";
        amountA = new BigDecimal(amountA).scaleByPowerOfTen(8).toPlainString();
        amountB = new BigDecimal(amountB).scaleByPowerOfTen(6).toPlainString();
        params.put("tokenAStr", dxa8_bnb.str());
        params.put("tokenBStr", usdx_bnb.str());
        params.put("amountA", amountA);
        params.put("amountB", amountB);
    }

    private void addLiquidityGoat9usdx_bnb(Map<String, Object> params) {
        String amountA = "3000";
        String amountB = "100";
        amountA = new BigDecimal(amountA).scaleByPowerOfTen(9).toPlainString();
        amountB = new BigDecimal(amountB).scaleByPowerOfTen(6).toPlainString();
        params.put("tokenAStr", goat9_bnb.str());
        params.put("tokenBStr", usdx_bnb.str());
        params.put("amountA", amountA);
        params.put("amountB", amountB);
    }

    private void addLiquidityNvt8safemoon9(Map<String, Object> params) {
        String amountA = "100";
        String amountB = "2000";
        amountA = new BigDecimal(amountA).scaleByPowerOfTen(8).toPlainString();
        amountB = new BigDecimal(amountB).scaleByPowerOfTen(9).toPlainString();
        params.put("tokenAStr", nvt.str());
        params.put("tokenBStr", safemoon9_bnb.str());
        params.put("amountA", amountA);
        params.put("amountB", amountB);
    }

    private void addLiquidityGoat9safemoon9(Map<String, Object> params) {
        String amountA = "300";
        String amountB = "2800";
        amountA = new BigDecimal(amountA).scaleByPowerOfTen(9).toPlainString();
        amountB = new BigDecimal(amountB).scaleByPowerOfTen(9).toPlainString();
        params.put("tokenAStr", goat9_bnb.str());
        params.put("tokenBStr", safemoon9_bnb.str());
        params.put("amountA", amountA);
        params.put("amountB", amountB);
    }

    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "账户地址"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "账户密码"),
            @Parameter(parameterName = "amountA", parameterType = "String", parameterDes = "添加的资产A的数量"),
            @Parameter(parameterName = "amountB", parameterType = "String", parameterDes = "添加的资产B的数量"),
            @Parameter(parameterName = "tokenAStr", parameterType = "String", parameterDes = "资产A的类型，示例：1-1"),
            @Parameter(parameterName = "tokenBStr", parameterType = "String", parameterDes = "资产B的类型，示例：1-1"),
            @Parameter(parameterName = "amountAMin", parameterType = "String", parameterDes = "资产A最小添加值"),
            @Parameter(parameterName = "amountBMin", parameterType = "String", parameterDes = "资产B最小添加值"),
            @Parameter(parameterName = "deadline", parameterType = "long", parameterDes = "过期时间"),
            @Parameter(parameterName = "to", parameterType = "String", parameterDes = "流动性份额接收地址")
    })
    @Test
    public void swapAddLiquidity() throws Exception {
        Map<String, Object> params = new HashMap<>();
        //this.addLiquidityNvt8usdx_bnb(params);
        this.addLiquidityNvt8usdx_eth(params);
        //this.addLiquidityDxa8usdx_bnb(params);
        //this.addLiquidityGoat9usdx_bnb(params);
        //this.addLiquidityNvt8safemoon9(params);
        //this.addLiquidityGoat9safemoon9(params);
        BigInteger[] minAmounts = this.calMinAmountOnSwapAddLiquidity(params);
        params.put("amountAMin", minAmounts[0].toString());
        params.put("amountBMin", minAmounts[1].toString());
        params.put("deadline", deadline());
        params.put("to", address32);
        this.sendTx(from, SWAP_ADD_LIQUIDITY, params);
    }

    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "账户地址"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "账户密码"),
            @Parameter(parameterName = "amountLP", parameterType = "String", parameterDes = "移除的资产LP的数量"),
            @Parameter(parameterName = "tokenLPStr", parameterType = "String", parameterDes = "资产LP的类型，示例：1-1"),
            @Parameter(parameterName = "tokenAStr", parameterType = "String", parameterDes = "资产A的类型，示例：1-1"),
            @Parameter(parameterName = "tokenBStr", parameterType = "String", parameterDes = "资产B的类型，示例：1-1"),
            @Parameter(parameterName = "amountAMin", parameterType = "String", parameterDes = "资产A最小移除值"),
            @Parameter(parameterName = "amountBMin", parameterType = "String", parameterDes = "资产B最小移除值"),
            @Parameter(parameterName = "deadline", parameterType = "long", parameterDes = "过期时间"),
            @Parameter(parameterName = "to", parameterType = "String", parameterDes = "资产接收地址")
    })
    @Test
    public void swapRemoveLiquidity() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("amountLP", "23130066012");
        params.put("tokenLPStr", "5-20");
        params.put("tokenAStr", nvt.str());
        params.put("tokenBStr", usdx_eth.str());
        BigInteger[] minAmounts = this.calMinAmountOnSwapRemoveLiquidity(params);
        System.out.println(String.format("minAmounts: %s", Arrays.deepToString(minAmounts)));
        params.put("amountAMin", minAmounts[0].toString());
        params.put("amountBMin", minAmounts[1].toString());
        params.put("deadline", deadline());
        params.put("to", address31);
        this.sendTx(address32, SWAP_REMOVE_LIQUIDITY, params);
    }

    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "账户地址"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "账户密码"),
            @Parameter(parameterName = "amountIn", parameterType = "String", parameterDes = "卖出的资产数量"),
            @Parameter(parameterName = "tokenPath", parameterType = "String[]", parameterDes = "币币交换资产路径，路径中最后一个资产，是用户要买进的资产，如卖A买B: [A, B] or [A, C, B]"),
            @Parameter(parameterName = "amountOutMin", parameterType = "String", parameterDes = "最小买进的资产数量"),
            @Parameter(parameterName = "feeTo", parameterType = "String", parameterDes = "交易手续费取出一部分给指定的接收地址"),
            @Parameter(parameterName = "deadline", parameterType = "long", parameterDes = "过期时间"),
            @Parameter(parameterName = "to", parameterType = "String", parameterDes = "资产接收地址")
    })
    @Test
    public void swapTokenTrade() throws Exception {
        //String tokenIn = nvt.str();
        //String amountIn = "50";
        //amountIn = new BigDecimal(amountIn).scaleByPowerOfTen(8).toPlainString();
        //String tokenOut = usdx_bnb.str();

        String tokenIn = usdx_bnb.str();
        String amountIn = "5";
        amountIn = new BigDecimal(amountIn).scaleByPowerOfTen(6).toPlainString();
        String tokenOut = nvt.str();

        //String[] pairs = new String[]{"TNVTdTSQ4vfckRXy2GWUykx174o3np9b7TC5q",
        //                                "TNVTdTSQBq61Gw6s8R9g8Jd7p6M2859Wn7kXW",
        //                                "TNVTdTSQBbFyEMRzhmXGdFRjQXBurtkgmgHvu",
        //                                "TNVTdTSQCTbG8b6Xq4qFN3pFikKrwsdfJRbKJ",
        //                                "TNVTdTSQJCuJoXDHUWH9c6pVydCiGc6Ees79i",
        //                                "TNVTdTSQ9MxeQxwX5sYW9xoCrXZt6FLvcchZu"};
        String[] pairs = new String[]{"TNVTdTSQB2TVBWTB4p656AzJAEDvgaXr7coUG",
                                        "TNVTdTSQHqLpFewLDaz76CqwfvqrHqhSmNqP7"};
        Map map = this.bestTradeExactIn(tokenIn, amountIn, tokenOut, 3, pairs);
        List<String> path = (List<String>) map.get("tokenPath");
        System.out.println(String.format("tokenPath: %s", path.toString()));
        Map outMap = (Map) map.get("tokenAmountOut");

        Map<String, Object> params = new HashMap<>();
        params.put("amountIn", amountIn);
        params.put("tokenPath", path.toArray(new String[path.size()]));
        BigInteger amountOutMin = new BigInteger(outMap.get("amount").toString());
        System.out.println(String.format("amountOutMin: %s", amountOutMin));
        params.put("amountOutMin", amountOutMin);
        params.put("feeTo", address51);
        params.put("deadline", deadline());
        params.put("to", address32);
        this.sendTx(from, SWAP_TOKEN_TRADE, params);
    }


    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "账户地址"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "账户密码"),
            @Parameter(parameterName = "coins", parameterType = "String[]", parameterDes = "资产类型列表，示例：[1-1, 1-2]"),
            @Parameter(parameterName = "symbol", parameterType = "String", parameterDes = "LP名称")
    })
    @Test
    public void stableSwapCreatePair() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("coins", new String[]{"5-3","5-5","5-6"});
        params.put("symbol", "QOPN");
        this.sendTx(from, STABLE_SWAP_CREATE_PAIR, params);
    }

    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "账户地址"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "账户密码"),
            @Parameter(parameterName = "amounts", parameterType = "String[]", parameterDes = "添加的资产数量列表"),
            @Parameter(parameterName = "tokens", parameterType = "String[]", parameterDes = "添加的资产类型列表，示例：[1-1, 1-2]"),
            @Parameter(parameterName = "pairAddress", parameterType = "String", parameterDes = "交易对地址"),
            @Parameter(parameterName = "deadline", parameterType = "long", parameterDes = "过期时间"),
            @Parameter(parameterName = "to", parameterType = "String", parameterDes = "流动性份额接收地址")
    })
    @Test
    public void stableSwapAddLiquidity() throws Exception {
        String amountHt = new BigDecimal("500").scaleByPowerOfTen(18).toPlainString();
        String amountBnb = new BigDecimal("2000").scaleByPowerOfTen(18).toPlainString();
        String amountEth = new BigDecimal("300").scaleByPowerOfTen(6).toPlainString();
        String amountOkt = new BigDecimal("200").scaleByPowerOfTen(8).toPlainString();

        Map<String, Object> params = new HashMap<>();
        params.put("amounts", new String[]{amountHt, amountBnb, amountEth, amountOkt});
        params.put("tokens", new String[]{husd_18.str(), busd_18.str(), usdx_eth.str(), okusd_8.str()});
        params.put("pairAddress", stablePairAddress);
        params.put("deadline", deadline());
        params.put("to", address32);
        this.sendTx(from, STABLE_SWAP_ADD_LIQUIDITY, params);
    }

    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "账户地址"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "账户密码"),
            @Parameter(parameterName = "amountLP", parameterType = "String", parameterDes = "移除的资产LP的数量"),
            @Parameter(parameterName = "tokenLPStr", parameterType = "String", parameterDes = "资产LP的类型，示例：1-1"),
            @Parameter(parameterName = "receiveOrderIndexs", parameterType = "int[]", parameterDes = "按币种索引顺序接收资产"),
            @Parameter(parameterName = "pairAddress", parameterType = "String", parameterDes = "交易对地址"),
            @Parameter(parameterName = "deadline", parameterType = "long", parameterDes = "过期时间"),
            @Parameter(parameterName = "to", parameterType = "String", parameterDes = "资产接收地址")
    })
    @Test
    public void stableSwapRemoveLiquidity() throws Exception {
        //NulsHash txHash = NulsHash.fromHex("1bc19b3450d8ad6ae96963012b124671f0cbb87964c16de59bf90df648b1c6ea");
        //byte[] stablePairAddressBytes = AddressTool.getAddress(txHash.getBytes(), chainId, SwapConstant.STABLE_PAIR_ADDRESS_TYPE);
        //String stablePairAddress = AddressTool.getStringAddressByBytes(stablePairAddressBytes);

        Map<String, Object> params = new HashMap<>();
        params.put("amountLP", "700000000000000000000");
        params.put("tokenLPStr", stable_swap_lp.str());
        params.put("receiveOrderIndexs", new int[]{0, 2, 3, 1});
        params.put("pairAddress", stablePairAddress);
        params.put("deadline", deadline());
        params.put("to", address32);
        this.sendTx(address32, STABLE_SWAP_REMOVE_LIQUIDITY, params);
    }

    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "账户地址"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "账户密码"),
            @Parameter(parameterName = "amountsIn", parameterType = "String[]", parameterDes = "卖出的资产数量列表"),
            @Parameter(parameterName = "tokensIn", parameterType = "String[]", parameterDes = "卖出的资产类型列表"),
            @Parameter(parameterName = "tokenOutIndex", parameterType = "int", parameterDes = "买进的资产索引"),
            @Parameter(parameterName = "feeTo", parameterType = "String", parameterDes = "交易手续费取出一部分给指定的接收地址"),
            @Parameter(parameterName = "pairAddress", parameterType = "String", parameterDes = "交易对地址"),
            @Parameter(parameterName = "deadline", parameterType = "long", parameterDes = "过期时间"),
            @Parameter(parameterName = "to", parameterType = "String", parameterDes = "资产接收地址")
    })
    @Test
    public void stableSwapTokenTrade() throws Exception {
        //NulsHash txHash = NulsHash.fromHex("1bc19b3450d8ad6ae96963012b124671f0cbb87964c16de59bf90df648b1c6ea");
        //byte[] stablePairAddressBytes = AddressTool.getAddress(txHash.getBytes(), chainId, SwapConstant.STABLE_PAIR_ADDRESS_TYPE);
        //String stablePairAddress = AddressTool.getStringAddressByBytes(stablePairAddressBytes);

        Map<String, Object> params = new HashMap<>();
        params.put("amountsIn", new String[]{"500000000"});
        params.put("tokensIn", new String[]{usdx_eth.str()});
        params.put("tokenOutIndex", 1);
        params.put("feeTo", address51);
        params.put("pairAddress", stablePairAddress);
        params.put("deadline", deadline());
        params.put("to", address32);
        this.sendTx(from, STABLE_SWAP_TOKEN_TRADE, params);
    }

    @Test
    public void calcStablePairAddress() {
        NulsHash txHash = NulsHash.fromHex("3095e9371bd03e903bcab502ec00bb7c6212114b438baa3d35865a57294ef63f");
        byte[] stablePairAddressBytes = AddressTool.getAddress(txHash.getBytes(), chainId, SwapConstant.STABLE_PAIR_ADDRESS_TYPE);
        String stablePairAddress = AddressTool.getStringAddressByBytes(stablePairAddressBytes);
        System.out.println(stablePairAddress);
    }
    @Test
    public void getPairInfo() throws Exception {
        Map map = this.getSwapPairInfo(nvt.str(), usdx_eth.str());
        System.out.println(JSONUtils.obj2PrettyJson(map));
    }

    @Test
    public void getStablePairInfo() throws Exception {
        //NulsHash txHash = NulsHash.fromHex("05e310975c23979a5bb8af587412c2b3c5006216e3eedc72756e2baac693ac94");
        //byte[] stablePairAddressBytes = AddressTool.getAddress(txHash.getBytes(), chainId, SwapConstant.STABLE_PAIR_ADDRESS_TYPE);
        //String stablePairAddress = AddressTool.getStringAddressByBytes(stablePairAddressBytes);

        Map map = this.getStableSwapPairInfo(stablePairAddress);
        System.out.println(JSONUtils.obj2PrettyJson(map));
    }

    @Test
    public void getResult() throws Exception {
        String hash = "05e310975c23979a5bb8af587412c2b3c5006216e3eedc72756e2baac693ac94";
        Map map = this.getSwapResultInfo(hash);
        System.out.println(JSONUtils.obj2PrettyJson(map));
        System.out.println();
        Object bus = this.desBusStr(map.get("txType"), map.get("business"));
        System.out.println(bus != null ? JSONUtils.obj2PrettyJson(bus) : "");
    }

    @Test
    public void getBestSwapPath() throws Exception {
        String tokenIn = usdx_eth.str();
        String amountIn = "3000000";
        String tokenOut = goat9_bnb.str();
        String[] pairs = new String[]{"TNVTdTSQ4vfckRXy2GWUykx174o3np9b7TC5q",
                "TNVTdTSQBq61Gw6s8R9g8Jd7p6M2859Wn7kXW",
                "TNVTdTSQBbFyEMRzhmXGdFRjQXBurtkgmgHvu",
                "TNVTdTSQCTbG8b6Xq4qFN3pFikKrwsdfJRbKJ",
                "TNVTdTSQJCuJoXDHUWH9c6pVydCiGc6Ees79i",
                "TNVTdTSQ9MxeQxwX5sYW9xoCrXZt6FLvcchZu"};
        Map map = this.bestTradeExactIn(tokenIn, amountIn, tokenOut, 3, pairs);
        System.out.println(JSONUtils.obj2PrettyJson(map));
    }

    @Test
    public void getAmountOut() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        params.put("amountIn", "5541714423");
        params.put("tokenPath", new String[]{"5-1", "5-10"});
        BigInteger amountOut = this.calMinAmountOnSwapTokenTrade(params);
        System.out.println(amountOut);
    }

    protected static Map<Integer, Class> busClassMap = new HashMap<>();

    static {
        busClassMap.put(TxType.SWAP_ADD_LIQUIDITY, AddLiquidityBus.class);
        busClassMap.put(TxType.SWAP_REMOVE_LIQUIDITY, RemoveLiquidityBus.class);
        busClassMap.put(TxType.SWAP_TRADE, SwapTradeBus.class);
        busClassMap.put(TxType.SWAP_ADD_LIQUIDITY_STABLE_COIN, StableAddLiquidityBus.class);
        busClassMap.put(TxType.SWAP_REMOVE_LIQUIDITY_STABLE_COIN, StableRemoveLiquidityBus.class);
        busClassMap.put(TxType.SWAP_TRADE_STABLE_COIN, StableSwapTradeBus.class);
    }

    protected Object desBusStr(Object txType, Object busStr) {
        if (txType == null || busStr == null) {
            return null;
        }
        Class aClass = busClassMap.get(Integer.parseInt(txType.toString()));
        if (aClass == null) {
            return null;
        }
        System.out.println(aClass.getSimpleName());
        return SwapDBUtil.getModel(HexUtil.decode(busStr.toString()), aClass);
    }

    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "amountA", parameterType = "String", parameterDes = "添加的资产A的数量"),
            @Parameter(parameterName = "amountB", parameterType = "String", parameterDes = "添加的资产B的数量"),
            @Parameter(parameterName = "tokenAStr", parameterType = "String", parameterDes = "资产A的类型，示例：1-1"),
            @Parameter(parameterName = "tokenBStr", parameterType = "String", parameterDes = "资产B的类型，示例：1-1")
    })
    protected BigInteger[] calMinAmountOnSwapAddLiquidity(Map<String, Object> params) throws Exception {
        HashMap data = this.getData(SWAP_MIN_AMOUNT_ADD_LIQUIDITY, params);
        return new BigInteger[]{
                new BigInteger(data.get("amountAMin").toString()),
                new BigInteger(data.get("amountBMin").toString())
        };
    }

    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "amountLP", parameterType = "String", parameterDes = "移除的资产LP的数量"),
            @Parameter(parameterName = "tokenAStr", parameterType = "String", parameterDes = "资产A的类型，示例：1-1"),
            @Parameter(parameterName = "tokenBStr", parameterType = "String", parameterDes = "资产B的类型，示例：1-1")
    })
    protected BigInteger[] calMinAmountOnSwapRemoveLiquidity(Map<String, Object> params) throws Exception {
        HashMap data = this.getData(SWAP_MIN_AMOUNT_REMOVE_LIQUIDITY, params);
        return new BigInteger[]{
                new BigInteger(data.get("amountAMin").toString()),
                new BigInteger(data.get("amountBMin").toString())
        };
    }

    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "amountIn", parameterType = "String", parameterDes = "卖出的资产数量"),
            @Parameter(parameterName = "tokenPath", parameterType = "String[]", parameterDes = "币币交换资产路径，路径中最后一个资产，是用户要买进的资产，如卖A买B: [A, B] or [A, C, B]")
    })
    protected BigInteger calMinAmountOnSwapTokenTrade(Map<String, Object> params) throws Exception {
        HashMap data = this.getData(SWAP_MIN_AMOUNT_TOKEN_TRADE, params);
        return new BigInteger(data.get("amountOutMin").toString());
    }

    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "tokenInStr", parameterType = "String", parameterDes = "卖出资产的类型，示例：1-1"),
            @Parameter(parameterName = "tokenInAmount", requestType = @TypeDescriptor(value = String.class), parameterDes = "卖出资产数量"),
            @Parameter(parameterName = "tokenOutStr", parameterType = "String", parameterDes = "买进资产的类型，示例：1-1"),
            @Parameter(parameterName = "maxPairSize", requestType = @TypeDescriptor(value = int.class), parameterDes = "交易最深路径"),
            @Parameter(parameterName = "pairs", requestType = @TypeDescriptor(value = String[].class), parameterDes = "当前网络所有交易对列表")
    })
    protected Map bestTradeExactIn(String tokenInStr, String tokenInAmount, String tokenOutStr, int maxPairSize, String[] pairs) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("tokenInStr", tokenInStr);
        params.put("tokenInAmount", tokenInAmount);
        params.put("tokenOutStr", tokenOutStr);
        params.put("maxPairSize", maxPairSize);
        params.put("pairs", pairs);
        HashMap data = this.getData(BEST_TRADE_EXACT_IN, params);
        return (Map) (data);
    }

    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "tokenAStr", parameterType = "String", parameterDes = "资产A的类型，示例：1-1"),
            @Parameter(parameterName = "tokenBStr", parameterType = "String", parameterDes = "资产B的类型，示例：1-1")
    })
    protected Map getSwapPairInfo(String tokenAStr, String tokenBStr) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("tokenAStr", tokenAStr);
        params.put("tokenBStr", tokenBStr);
        HashMap data = this.getData(SWAP_PAIR_INFO, params);
        return (Map) (data);
    }

    protected NerveToken getPairLPToken(String tokenAStr, String tokenBStr) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("tokenAStr", tokenAStr);
        params.put("tokenBStr", tokenBStr);
        try {
            HashMap data = this.getData(SWAP_PAIR_INFO, params);
            if (data != null) {
                Map map = (Map) data.get("po");
                if (map != null) {
                    return SwapUtils.parseTokenStr(map.get("tokenLP").toString());
                }
            }
            return new NerveToken();
        } catch (Exception e) {
            return new NerveToken();
        }
    }

    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "pairAddress", parameterType = "String", parameterDes = "交易对地址")
    })
    protected Map getStableSwapPairInfo(String pairAddress) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("pairAddress", pairAddress);
        HashMap data = this.getData(STABLE_SWAP_PAIR_INFO, params);
        return (Map) (data);
    }

    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "txHash", parameterType = "String", parameterDes = "交易hash")
    })
    protected Map getSwapResultInfo(String txHash) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("txHash", txHash);
        HashMap data = this.getData(SWAP_RESULT_INFO, params);
        return (Map) (data.get("value"));
    }

    @Test
    public void proposal() throws Exception {
        //账户已存在则覆盖 If the account exists, it covers.
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);

        params.put("type", (byte) 9);// ProposalTypeEnum.ADDCOIN
        params.put("content", "5-6");
        params.put("businessAddress", stablePairAddress);// stablePairAddress
        params.put("voteRangeType", (byte) 1);// ProposalVoteRangeTypeEnum.BANK
        params.put("remark", "稳定币增加币种测试");
        params.put("address", "TNVTdTSPLpegzD3B6qaVKhfj6t8cYtnkfR7Wx");
        params.put("password", password);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CV.abbr, "cv_proposal", params);
        System.out.println(JSONUtils.obj2PrettyJson(cmdResp));
        HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("cv_proposal");
        String hash = (String) result.get("value");
        String txHex = (String) result.get("hex");
        Log.debug("hash:{}", hash);
        Log.debug("txHex:{}", txHex);

    }

    protected long deadline() {
        return System.currentTimeMillis() / 1000 + 300;
    }

    protected void sendTx(String from, String cmd, Map<String, Object> _params) throws Exception {
        Log.info("hash:{}", this.callSwap(from, cmd, _params));
    }

    protected HashMap getData(String cmd, Map<String, Object> _params) throws Exception {
        return (HashMap) this.callSwap(null, cmd, _params);
    }

    protected Object callSwap(String from, String cmd, Map<String, Object> _params) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, version);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("address", from);
        params.put("password", password);
        params.putAll(_params);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.SW.abbr, cmd, params);
        if (cmdResp.isSuccess()) {
            Object result = ((HashMap) cmdResp.getResponseData()).get(cmd);
            return result;
        } else {
            //System.out.println(JSONUtils.obj2PrettyJson(cmdResp));
            throw new Exception(formatError(cmdResp));
        }
    }

    protected String formatError(Response cmdResp) {
        if (cmdResp == null || cmdResp.isSuccess()) {
            return "success";
        }
        return String.format("code: %s, msg: %s", cmdResp.getResponseErrorCode(), cmdResp.getResponseComment());
    }

    @Test
    public void getBalance() throws Exception {
        getBalanceByAddress("address31-用户地址", address31);
        getBalanceByAddress("address32-接收地址", address32);
        getBalanceByAddress("Swap-pair地址", SwapUtils.getStringPairAddress(chainId, dxa8_bnb, usdx_bnb));
        getBalanceByAddress("Stable-Swap-pair地址", stablePairAddress);
        getBalanceByAddress("接收手续费的系统地址", awardFeeSystemAddress);
        getBalanceByAddress("address51-接收手续费的交易指定地址", address51);
    }

    protected void getBalanceByAddress(String address) throws Exception {
        this.getBalanceByAddress("", address);
    }

    protected void getBalanceByAddress(String title, String address) throws Exception {
        System.out.println();
        System.out.println(String.format("%s address: %s", title, address));

        this.balanceInfoPrint("　主资产NVT", nvt, address);

        this.balanceInfoPrint("Ethereum-资产USDX", usdx_eth, address);
        this.balanceInfoPrint("BSC-资产USDX", usdx_bnb, address);
        this.balanceInfoPrint("BSC-资产DXA", dxa8_bnb, address);
        this.balanceInfoPrint("BSC-资产GOAT", goat9_bnb, address);
        this.balanceInfoPrint("BSC-资产SAFEMOON", safemoon9_bnb, address);
        this.balanceInfoPrint("HT-资产USDX", usdx_ht, address);
        this.balanceInfoPrint("OKT-资产USDX", usdx_okt, address);
        this.balanceInfoPrint("BSC-资产BUSD", busd_18, address);
        this.balanceInfoPrint("HT-资产HUSD", husd_18, address);
        this.balanceInfoPrint("OKT-资产OKUSD", okusd_8, address);

        this.balanceInfoPrint("Stable-LP资产", stable_swap_lp, address);

        this.balanceInfoPrint("Swap-LP资产(nvt8usdx_bnb)", swap_lp_nvt8usdx_bnb, address);
        this.balanceInfoPrint("Swap-LP资产(nvt8usdx_eth)", swap_lp_nvt8usdx_eth, address);
        this.balanceInfoPrint("Swap-LP资产(dxa8usdx_bnb)", swap_lp_dxa8usdx_bnb, address);
        this.balanceInfoPrint("Swap-LP资产(goat9usdx_bnb)", swap_lp_goat9usdx_bnb, address);
        this.balanceInfoPrint("Swap-LP资产(nvt8safemoon9)", swap_lp_nvt8safemoon9, address);
        this.balanceInfoPrint("Swap-LP资产(goat9safemoon9)", swap_lp_goat9safemoon9, address);
    }

    private void balanceInfoPrint(String desc, NerveToken token, String address) {
        BigInteger balance = LedgerCall.getBalance(chainId, token.getChainId(), token.getAssetId(), address);
        System.out.println(String.format("%s %s-%s: %s", desc, token.getChainId(), token.getAssetId(), balance));
    }

    @Test
    public void getNonceAndBalance() throws Exception {
        NonceBalance b = LedgerCall.getBalanceNonce(chainId, chainId, assetId, "TNVTdTSPyT1GGPrbahr9qo7S87dMBatx9NHtP");
        System.out.println(b.getAvailable());
        System.out.println(HexUtil.encode(b.getNonce()));
    }

    protected Map<String, Object> getTxCfmClient(String hash) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("txHash", hash);
        Response dpResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getConfirmedTxClient", params);
        Map record = (Map) dpResp.getResponseData();
        Log.debug(JSONUtils.obj2PrettyJson(record));
        return (Map) record.get("tx_getConfirmedTxClient");
    }

    @Test
    public void getTx() throws Exception {
        String txStr = (String) (getTxCfmClient("6280be66a9d0b7bef774dfc8839bf0d4b08023b5234d942a1de04a073d85f9c9").get("tx"));
        System.out.println(txStr);
        Transaction tx = Transaction.getInstance(HexUtil.decode(txStr), Transaction.class);//最后一条
        System.out.println(tx.format());
    }

    @Test
    public void ledgerAssetQueryOne() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, 5);

        params.put("assetChainId", 5);
        params.put("assetId", 1);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "lg_get_asset", params);
        System.out.println(JSONUtils.obj2PrettyJson(cmdResp));
    }

    @Test
    public void ledgerAssetQueryAll() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "lg_get_all_asset", params);
        System.out.println(JSONUtils.obj2PrettyJson(cmdResp));
    }

    @Test
    public void ledgerAssetInChainQuery() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", chainId);
        params.put("assetId", 2);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getAssetRegInfoByAssetId", params);
        System.out.println(JSONUtils.obj2PrettyJson(cmdResp));
    }

    @Test
    public void ledgerAssetInChainQueryWhole() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getAssetRegInfo", params);
        System.out.println(JSONUtils.obj2PrettyJson(cmdResp));
    }

    @Test
    public void newTx() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put(Constants.CHAIN_ID, chainId);
        params.put("tx", "3f00d225ed6013746573742072656d61726b20636f6e74656e74490000000000000000000000000000000000000000000000000000000000000000050001b9978dbea4abebb613b6be2d0d66b4179d2511cb00b226ed60030500010005000100050001008c0117050001b9978dbea4abebb613b6be2d0d66b4179d2511cb0500010000a90dae0400000000000000000000000000000000000000000000000000000008e97fb78a6b5c97b400011705000444bbf88ae1b9bff4e07827fb363eea3362f868f40500010000a90dae04000000000000000000000000000000000000000000000000000000000000000000000000");
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_newTx", params);
        System.out.println(JSONUtils.obj2PrettyJson(cmdResp));
    }

}
