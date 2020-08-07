package network.nerve.converter.heterogeneouschain.eth.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.v2.SDKContext;
import io.nuls.v2.model.dto.RestFulResult;
import io.nuls.v2.util.RestFulUtil;
import network.nerve.converter.heterogeneouschain.eth.base.Base;
import network.nerve.converter.heterogeneouschain.eth.constant.EthConstant;
import network.nerve.converter.heterogeneouschain.eth.context.EthContext;
import network.nerve.converter.heterogeneouschain.eth.helper.EthParseTxHelper;
import network.nerve.converter.heterogeneouschain.eth.model.EthSendTransactionPo;
import network.nerve.converter.heterogeneouschain.eth.utils.EthUtil;
import network.nerve.converter.model.bo.HeterogeneousTransactionInfo;
import org.ethereum.crypto.HashUtil;
import org.junit.Test;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static network.nerve.converter.heterogeneouschain.eth.constant.EthConstant.ETH_GAS_LIMIT_OF_USDT;


public class ETHWalletApiTest extends Base {

    @Test
    public void getBlock() throws Exception {
        Long height = 7370853L;
        EthBlock.Block block = ethWalletApi.getBlockByHeight(height);
        //Block block1 = ethWalletApi.getBlock(height);
        System.out.println();
    }

    @Test
    public void getBlockWithoutTransaction() throws IOException {
        Long height = 7370853L;
        EthBlock.Block block = ethWalletApi.getWeb3j().ethGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(height)), false).send().getBlock();
        System.out.println();
    }

    @Test
    public void txRemarkTest() throws Exception {
        String mainEthRpcAddress = "https://mainnet.infura.io/v3/e51e9f10a4f647af81d5f083873f27a5";
        Web3j web3j = Web3j.build(new HttpService(mainEthRpcAddress));
        ethWalletApi.setWeb3j(web3j);
        String txHash = "0xb1ed364e4333aae1da4a901d5231244ba6a35f9421d4607f7cb90d60bf45578a";
        Transaction tx = ethWalletApi.getTransactionByHash(txHash);
        String remark = new String(Numeric.hexStringToByteArray(tx.getInput()), StandardCharsets.UTF_8);
        System.out.println(remark);
    }

    @Test
    public void getTxJson() throws Exception {
        String txHash = "0x93be98e9cb6d920a453bdc9113513585a225960dc3ad3a1efdac9839e9c0069b";
        Transaction tx = ethWalletApi.getTransactionByHash(txHash);
        System.out.println(JSONUtils.obj2json(tx));
        HeterogeneousTransactionInfo info = EthUtil.newTransactionInfo(tx);
    }

    @Test
    public void withdrawTest() throws Exception {
        Set<String> set = new HashSet<>();
        set.add("0xe24973ff71d061f403cb45ddde97e034484ee7d3");
        //EthContext.MULTY_SIGN_ADDRESS_HISTORY_SET = set;
        String txHash = "0xcdd7609b888eb261e1d6c4b0d9efe762a4e194c1291d9c0af8980cc42f6a262f";
        Transaction tx = ethWalletApi.getTransactionByHash(txHash);
        TransactionReceipt txReceipt = ethWalletApi.getTxReceipt(txHash);
        EthParseTxHelper helper = new EthParseTxHelper();
        HeterogeneousTransactionInfo info = helper.parseWithdrawTransaction(tx, txReceipt);
        System.out.println(info.toString());
    }

    @Test
    public void getTxAndPublicKeyTest() throws Exception {
        setMain();
        String txHash = "0x855d092b64df8a6346e691a33071fb8fac197e45ee4ac8769386b4121e80da6d";
        Transaction tx = ethWalletApi.getTransactionByHash(txHash);
        converPublicKey(tx);
    }

    @Test
    public void covertNerveAddressByEthTxTest() throws Exception {
        EthContext.NERVE_CHAINID = 2;
        String txHash = "0x3c39e32ffafdee0705687e2c348aab226dcd08c1226e13053ee563de077c4b69";
        Transaction tx = ethWalletApi.getTransactionByHash(txHash);
        System.out.println(EthUtil.covertNerveAddressByEthTx(tx));
    }

    @Test
    public void hexTest() {
        System.out.println(HexUtil.decode("ff"));
    }


    @Test
    public void blockPublicKeyTest() throws Exception {
        Long height = 7936920L;
        EthBlock.Block block = ethWalletApi.getBlockByHeight(height);
        List<EthBlock.TransactionResult> list = block.getTransactions();
        for (EthBlock.TransactionResult txResult : list) {
            Transaction tx = (Transaction) txResult;
            converPublicKey(tx);
        }
        System.out.println();
    }

    private byte[] getRawTxHashBytes(Transaction tx) {
        String data = "";
        if (StringUtils.isNotBlank(tx.getInput()) && !"0x".equals(tx.getInput().toLowerCase())) {
            data = tx.getInput();
        }
        RawTransaction rawTx = RawTransaction.createTransaction(
                tx.getNonce(),
                tx.getGasPrice(),
                tx.getGas(),
                tx.getTo(),
                tx.getValue(),
                data);
        byte[] rawTxEncode;
        if (tx.getChainId() != null) {
            rawTxEncode = TransactionEncoder.encode(rawTx, tx.getChainId());
        } else {
            rawTxEncode = TransactionEncoder.encode(rawTx);
        }
        byte[] hashBytes = Hash.sha3(rawTxEncode);
        return hashBytes;
    }

    /**
     * 遍历每一个可能的v值，来得到正确的公钥
     */
    private void converPublicKey(Transaction tx) {
        ECDSASignature signature = new ECDSASignature(Numeric.decodeQuantity(tx.getR()), Numeric.decodeQuantity(tx.getS()));
        byte[] hashBytes = getRawTxHashBytes(tx);

        boolean exist = false;
        boolean success = false;
        List<String> errorList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            BigInteger recover = Sign.recoverFromSignature(i, signature, hashBytes);
            if (recover != null) {
                exist = true;
                String address = "0x" + Keys.getAddress(recover);
                if (tx.getFrom().toLowerCase().equals(address.toLowerCase())) {
                    success = true;
                    System.out.println("成功: " + i + ": " + address + String.format("tx hash: %s, tx index: %s, tx data length: %s, tx chainId: %s", tx.getHash(), tx.getTransactionIndex(), tx.getInput().length(), tx.getChainId()));
                    break;
                } else {
                    errorList.add(address);
                }
            }
        }
        if (!exist) {
            System.err.println(String.format("异常: error, tx hash: %s, tx index: %s, tx data length: %s, tx chainId: %s", tx.getHash(), tx.getTransactionIndex(), tx.getInput().length(), tx.getChainId()));
        }
        if (!success) {
            System.err.println(String.format("失败: tx from: %s, parse from: %s, tx hash: %s, tx index: %s, tx data length: %s, tx chainId: %s", tx.getFrom(), errorList, tx.getHash(), tx.getTransactionIndex(), tx.getInput().length(), tx.getChainId()));
        }
    }

    @Test
    public void txPublicKeyTest() throws SignatureException, IOException {
        ObjectMapper objectMapper = JSONUtils.getInstance();
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String txJson = "{\"hash\":\"0x855d092b64df8a6346e691a33071fb8fac197e45ee4ac8769386b4121e80da6d\",\"nonce\":6,\"blockHash\":\"0x578fcc55a574980bb44398d47ab9de98f9da75137b6da4380d730477e273a70c\",\"blockNumber\":9569417,\"transactionIndex\":60,\"from\":\"0x87e1f9c3e730cf47bc49a03a856875801e0ece50\",\"to\":\"0x9b4e2b4b13d125238aa0480dd42b4f6fc71b37cc\",\"value\":0,\"gasPrice\":7000000000,\"gas\":62090,\"input\":\"0xa9059cbb000000000000000000000000f173805f1e3fe6239223b17f0807596edc2830120000000000000000000000000000000000000000000000000de0b6b3a7640000\",\"creates\":null,\"publicKey\":null,\"raw\":null,\"r\":\"0x39950dcaa3a777889aa3fcc8664fd88fcfa132962a266af4403e126071b042ba\",\"s\":\"0x2bea3f724b531993d45aa6bfd9c20ca247179951c3720e359bbf2aa2e2e9d1cb\",\"v\":37,\"nonceRaw\":\"0x6\",\"blockNumberRaw\":\"0x920489\",\"transactionIndexRaw\":\"0x3c\",\"valueRaw\":\"0x0\",\"gasPriceRaw\":\"0x1a13b8600\",\"gasRaw\":\"0xf28a\",\"chainId\":1}";
        //String txJson = "{\"hash\":\"0xe547b2ceed87fcf22b80cfb4c01236994967ee437e84e2fe689e88c5fee2d693\",\"nonce\":21,\"blockHash\":\"0x014a0b9ef207990060ec839d01dce40d6f243be43a3a93de666a31cfa8bb3fe4\",\"blockNumber\":7419804,\"transactionIndex\":3,\"from\":\"0xf173805f1e3fe6239223b17f0807596edc283012\",\"to\":\"0xf173805f1e3fe6239223b17f0807596edc283012\",\"value\":600000000000000,\"gasPrice\":20000000000,\"gas\":21000,\"input\":\"0x\",\"creates\":null,\"publicKey\":null,\"raw\":null,\"r\":\"0xe4c001eec77fe05bf03873745ed68cf1f2f648fbd2e2c9e51b1586e7767e483c\",\"s\":\"0x7bd1c92c85c68ed11f919797320609f08796af6cccd50916858d4dbff7933ae5\",\"v\":27,\"chainId\":null,\"nonceRaw\":\"0x15\",\"blockNumberRaw\":\"0x71379c\",\"transactionIndexRaw\":\"0x3\",\"valueRaw\":\"0x221b262dd8000\",\"gasPriceRaw\":\"0x4a817c800\",\"gasRaw\":\"0x5208\"}";
        Transaction tx = JSONUtils.json2pojo(txJson, Transaction.class);

        String data = "";
        if (StringUtils.isNotBlank(tx.getInput()) && !"0x".equals(tx.getInput().toLowerCase())) {
            data = tx.getInput();
        }
        ECDSASignature signature = new ECDSASignature(Numeric.decodeQuantity(tx.getR()), Numeric.decodeQuantity(tx.getS()));
        RawTransaction rawTx = RawTransaction.createTransaction(
                new BigInteger(tx.getNonceRaw()),
                new BigInteger(tx.getGasPriceRaw()),
                new BigInteger(tx.getGasRaw()),
                tx.getTo(),
                new BigInteger(tx.getValueRaw()),
                data);
        byte[] rawTxEncode;
        if (tx.getChainId() != null) {
            rawTxEncode = TransactionEncoder.encode(rawTx, tx.getChainId());
        } else {
            rawTxEncode = TransactionEncoder.encode(rawTx);
        }
        byte[] hashBytes = Hash.sha3(rawTxEncode);

        //String hash = "5eb0711c34fbd673be0c9112024bd9f66554d3e18dcb1acd97524168617c137d";
        boolean exist = false;
        boolean success = false;
        List<String> errorList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            BigInteger recover = Sign.recoverFromSignature(i, signature, hashBytes);
            if (recover != null) {
                exist = true;
                String address = "0x" + Keys.getAddress(recover);
                if (tx.getFrom().toLowerCase().equals(address.toLowerCase())) {
                    success = true;
                    System.out.println("成功: " + i + ": " + address + String.format("tx hash: %s, tx index: %s, tx data length: %s, tx chainId: %s", tx.getHash(), tx.getTransactionIndexRaw(), tx.getInput().length(), tx.getChainId()));
                    break;
                } else {
                    errorList.add(address);
                }
            }
        }
        if (!exist) {
            System.err.println(String.format("异常: error, tx hash: %s, tx index: %s, tx data length: %s, tx chainId: %s", tx.getHash(), tx.getTransactionIndexRaw(), tx.getInput().length(), tx.getChainId()));
        }
        if (!success) {
            System.err.println(String.format("失败: tx from: %s, parse from: %s, tx hash: %s, tx index: %s, tx data length: %s, tx chainId: %s", tx.getFrom(), errorList, tx.getHash(), tx.getTransactionIndexRaw(), tx.getInput().length(), tx.getChainId()));
        }
    }

    @Test
    public void hashTest() {
        String data = "f8a9068501a13b860082f28a949b4e2b4b13d125238aa0480dd42b4f6fc71b37cc80b844a9059cbb000000000000000000000000f173805f1e3fe6239223b17f0807596edc2830120000000000000000000000000000000000000000000000000de0b6b3a764000025a039950dcaa3a777889aa3fcc8664fd88fcfa132962a266af4403e126071b042baa02bea3f724b531993d45aa6bfd9c20ca247179951c3720e359bbf2aa2e2e9d1cb";
        System.out.println(Numeric.decodeQuantity("0x" + data).toString(16));
        //System.out.println(Hex.decode(HexUtil.decode(data)));
//        System.out.println(HexUtil.encode(Hex.encode(HexUtil.decode(data))));
        System.out.println(HexUtil.encode(Hash.sha3(HexUtil.decode(data))));
        System.out.println(HexUtil.encode(HashUtil.sha3(HexUtil.decode(data))));
        System.out.println(HexUtil.encode(HashUtil.sha256(HexUtil.decode(data))));
        System.out.println(HexUtil.encode(HashUtil.ripemd160(HexUtil.decode(data))));
        System.out.println(HexUtil.encode(HashUtil.doubleDigest(HexUtil.decode(data))));
    }

    @Test
    public void txDecoderTest() {
        String data = "0xf8a9068501a13b860082f28a949b4e2b4b13d125238aa0480dd42b4f6fc71b37cc80b844a9059cbb000000000000000000000000f173805f1e3fe6239223b17f0807596edc2830120000000000000000000000000000000000000000000000000de0b6b3a764000025a039950dcaa3a777889aa3fcc8664fd88fcfa132962a266af4403e126071b042baa02bea3f724b531993d45aa6bfd9c20ca247179951c3720e359bbf2aa2e2e9d1cb";
        RawTransaction decode = TransactionDecoder.decode(data);
        System.out.println();
    }

    @Test
    public void validateContractAddress() throws IOException {
        String address1 = "0xf173805F1e3fE6239223B17F0807596Edc283012";
        String address2 = "0xB058887cb5990509a3D0DD2833B2054E4a7E4a55";
        System.out.println(ethWalletApi.getWeb3j().ethGetCode(address1,
                DefaultBlockParameter.valueOf("latest")).send().getCode());
        System.out.println(ethWalletApi.getWeb3j().ethGetCode(address2,
                DefaultBlockParameter.valueOf("latest")).send().getCode());
    }

    @Test
    public void sendETHTx() throws Exception {
        String txHash = ethWalletApi.sendETH("0xf173805F1e3fE6239223B17F0807596Edc283012", "0xD15FDD6030AB81CEE6B519645F0C8B758D112CD322960EE910F65AD7DBB03C2B",
                "0xf173805F1e3fE6239223B17F0807596Edc283012",
                EthContext.getFee(),
                EthConstant.ETH_GAS_LIMIT_OF_ETH,
                EthContext.getEthGasPrice());
        System.out.println(txHash);
    }

    @Test
    public void getMainNetTxReceipt() throws Exception {
        setMain();
        // 直接调用erc20合约
        String directTxHash = "0x2ec039c38d07910f457a634c2c2048c989930c54eebac4e3363993e5d77e3fd6";
        // 合约内部调用erc20合约
        String innerCallTxHash = "0xa8fa6cdf285a317318b2a93f9c9f5b5cfb33c98aa8a4db4147da6c78ed357263";
        TransactionReceipt txReceipt = ethWalletApi.getTxReceipt(directTxHash);
        System.out.println();
        //0x000000000000000000000000c11d9943805e56b630a401d4bd9a29550353efa1000000000000000000000000000000000000000000000000016345785d8a0000
    }

    @Test
    public void getTestNetTxReceipt() throws Exception {
        // 直接调用erc20合约
        String directTxHash = "0xc3d6e4c1e67fa0d47189cd32203a6b1fa9c4b79eeb7fb6de86cf807b018f02bc";
        TransactionReceipt txReceipt = ethWalletApi.getTxReceipt(directTxHash);
        System.out.println(txReceipt);
        //System.out.println(JSONUtils.obj2json(txReceipt));
        //EthParseTxHelper helper = new EthParseTxHelper();
        //boolean bool = helper.parseWithdrawTxReceipt(txReceipt, new HeterogeneousTransactionBaseInfo());
        //System.out.println(bool);
    }

    String privateKey3 = "59F770E9C44075DE07F67BA7A4947C65B7C3A0046B455997D1E0F854477222C8";
    String from3 = "0x09534d4692F568BC6e9bef3b4D84d48f19E52501";

    String privateKey4 = "09EF77E9D4DD02D52AB08762A0D28874B5B0F2D85E9AAB3BB011FCEB51E7EB37";
    String from4 = "0xF3c90eF58eC31805af11CE5FA6d39E395c66441f";

    String privateKey5 = "156B8A1DBEC2E2D032796464A059E569095BB4E0F0E491AC3A4B29A9EDCAB3A8";
    String from5 = "0x6afb1F9Ca069bC004DCF06C51B42992DBD90Adba";

    String privateKey6 = "43DA7C269917207A3CBB564B692CD57E9C72F9FCFDB17EF2190DD15546C4ED9D";
    String from6 = "0x8F05AE1C759b8dB56ff8124A89bb1305ECe17B65";

    String privateKey7 = "0935E3D8C87C2EA5C90E3E3A0509D06EB8496655DB63745FAE4FF01EB2467E85";
    String from7 = "0xd29E172537A3FB133f790EBE57aCe8221CB8024F";

    String privateKey8 = "CCF560337BA3DE2A76C1D08825212073B299B115474B65DE4B38B587605FF7F2";
    String from8 = "0x54eAB3868B0090E6e1a1396E0e54F788a71B2b17";

    String privateKey9 = "4594348E3482B751AA235B8E580EFEF69DB465B3A291C5662CEDA6459ED12E39";
    String from9 = "0xc11D9943805e56b630A401D4bd9A29550353EFa1";

    private String getPrivateKey() {
        return privateKey6;
    }

    private String getFrom() {
        return from6;
    }

    /*
     添加`8`为管理员
     3: 0x6ba6da0b69a3593424c67e8b3f4b68aba62491c60af7214931336d5417eea3b7
     4: 0x033a35016431428a129009fd90b606facd53f86e7d42a821c8a7ec117cde8ad1
     5: 0x624323fe8b97e0b799e23c1ad10713040b7bc6d7a90883f078078d36d4427760
     6: 0x9b10d33660b26e568bf9fc9d224cee494657071249fadd8e14f502904f944286 (done)
     */
    @Test
    public void callContractTest() throws Exception {
        //加载凭证，用私钥
        Credentials credentials = Credentials.create(getPrivateKey());
        String contractAddress = "0x945aDdcdCB42A99aae77A10Eb220C4e04f4f7b5b";
        // args: "zxczxc",["0x54eAB3868B0090E6e1a1396E0e54F788a71B2b17"],[]
        EthGetTransactionCount transactionCount = ethWalletApi.getWeb3j().ethGetTransactionCount(getFrom(), DefaultBlockParameterName.PENDING).sendAsync().get();
        BigInteger nonce = transactionCount.getTransactionCount();
//0xa3c1f84900000000000000000000000000000000000000000000000000000000000000a0000000000000000000000000c11d9943805e56b630a401d4bd9a29550353efa1000000000000000000000000000000000000000000000000016345785d8a00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000067765727765720000000000000000000000000000000000000000000000000000
        //创建RawTransaction交易对象
        Function function = new Function(
                "createOrSignManagerChange",
                List.of(new Utf8String("zxczxc"), new DynamicArray(Address.class, List.of(new Address("0x54eAB3868B0090E6e1a1396E0e54F788a71B2b17"))),
                        new DynamicArray(Address.class, List.of()), new Uint8(1)),
                List.of(new TypeReference<Type>() {
                })
        );

        String encodedFunction = FunctionEncoder.encode(function);

        RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                EthContext.getEthGasPrice(),
                BigInteger.valueOf(300000L),
                contractAddress, encodedFunction
        );
        //签名Transaction，这里要对交易做签名
        byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signMessage);
        //发送交易
        EthSendTransaction ethSendTransaction = ethWalletApi.getWeb3j().ethSendRawTransaction(hexValue).sendAsync().get();
        System.out.println(ethSendTransaction.getTransactionHash());
    }

    /**
     * view/constant函数返回值解析测试
     */
    @Test
    public void viewContractTest() throws Exception {
        String contractAddress = "0xD7AF5cb5D3008D20d2F22F4612Fb5589E44699Ac";
        //创建RawTransaction交易对象
        Function allManagersFunction = new Function(
                "allManagers",
                List.of(),
                List.of(new TypeReference<DynamicArray<Address>>() {
                })
        );

        Function ifManagerFunction = new Function(
                "ifManager",
                List.of(new Address("0x54eab3868b0090e6e1a1396e0e54f788a71b2b17")),
                List.of(new TypeReference<Bool>() {
                })
        );

        Function pendingWithdrawTransactionFunction = new Function(
                "pendingWithdrawTransaction",
                List.of(new Utf8String("qweqwe")),
                List.of(
                        new TypeReference<Address>(){},
                        new TypeReference<Address>(){},
                        new TypeReference<Uint256>(){},
                        new TypeReference<Bool>(){},
                        new TypeReference<Address>(){},
                        new TypeReference<Uint8>(){}
                )
        );

        // ((DynamicArray<Address>)typeList.get(1)).getValue().get(0).getTypeAsString()
        Function pendingManagerChangeTransactionFunction = new Function(
                "pendingManagerChangeTransaction",
                List.of(new Utf8String("bnmbnm")),
                List.of(
                        new TypeReference<Address>(){},
                        new TypeReference<DynamicArray<Address>>(){},
                        new TypeReference<DynamicArray<Address>>(){},
                        new TypeReference<Uint8>(){},
                        new TypeReference<Uint8>(){}
                )
        );

        Function function = ifManagerFunction;

        String encode = FunctionEncoder.encode(function);
        org.web3j.protocol.core.methods.request.Transaction ethCallTransaction = org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(null, contractAddress, encode);
        EthCall ethCall = ethWalletApi.getWeb3j().ethCall(ethCallTransaction, DefaultBlockParameterName.PENDING).sendAsync().get();
        String value = ethCall.getResult();
        List<Type> typeList = FunctionReturnDecoder.decode(value, function.getOutputParameters());
        List results = new ArrayList();
        for(Type type : typeList) {
            results.add(type.getValue());
        }
        System.out.println(JSONUtils.obj2json(typeList));
        System.out.println(JSONUtils.obj2json(results));
    }

    /**
     * 合约事件解析测试
     */
    @Test
    public void eventTest() {
        // Event definition
        final Event MY_EVENT = new Event("MyEvent",
                Arrays.<TypeReference<?>>asList(
                        new TypeReference<Address>(true) {
                        },
                        new TypeReference<Bytes32>(true) {
                        },
                        new TypeReference<Uint8>(false) {
                        }));

        // Event definition hash
        final String MY_EVENT_HASH = EventEncoder.encode(MY_EVENT);

        TransactionReceipt txReceipt = null;
        List<org.web3j.protocol.core.methods.response.Log> logs = txReceipt.getLogs();
        org.web3j.protocol.core.methods.response.Log log = logs.get(0);
        List<String> topics = log.getTopics();
        String eventHash = topics.get(0);
        if (eventHash.equals(MY_EVENT_HASH)) { // Only MyEvent.
            // address indexed _arg1
            Address arg1 = (Address) FunctionReturnDecoder.decodeIndexedValue(log.getTopics().get(1), new TypeReference<Address>() {
            });
            // bytes32 indexed _arg2
            Bytes32 arg2 = (Bytes32) FunctionReturnDecoder.decodeIndexedValue(log.getTopics().get(2), new TypeReference<Bytes32>() {
            });
            // uint8 _arg3
            Uint8 arg3 = (Uint8) FunctionReturnDecoder.decodeIndexedValue(log.getTopics().get(3), new TypeReference<Uint8>() {
            });
        }
    }

    /**
     * 合约事件`TransactionWithdrawCompleted`解析测试
     */
    @Test
    public void eventTransactionWithdrawCompletedTest() throws Exception {
        // Event definition
        final Event MY_EVENT = new Event("TransactionWithdrawCompleted",
                Arrays.<TypeReference<?>>asList(
                        new TypeReference<DynamicArray<Address>>(true) {},
                        new TypeReference<Utf8String>(false) {}
                ));

        // Event definition hash
        final String MY_EVENT_HASH = EventEncoder.encode(MY_EVENT);

        String directTxHash = "0xf7b5b85886e954250e031d112a8f007384c2dda1e2f6c8ead1a330a863e1d234";
        TransactionReceipt txReceipt = ethWalletApi.getTxReceipt(directTxHash);
        List<org.web3j.protocol.core.methods.response.Log> logs = txReceipt.getLogs();
        org.web3j.protocol.core.methods.response.Log log = logs.get(1);
        List<String> topics = log.getTopics();
        String eventHash = topics.get(0);
        if (eventHash.equals(MY_EVENT_HASH)) { // Only MyEvent.
            List<Type> typeList = FunctionReturnDecoder.decode(log.getData(), MY_EVENT.getParameters());
            System.out.println();
        }
    }

    @Test
    public void changeEventTest() throws Exception {
        // 0x92756cd02612b2bcbbc2b0399f6469b500d4e81fb563e1004494872c12b6dd60 / nerveHash: b5fc4cc3b236ff0bbf4890bf62ed3f032c4cb499a1d6f71a4a2d0d462f32ecfe
        // 0x024f40dc947b30693f8c037441d9b925aa87610fd9ab04c33c3dfb8648209fe9 / nerveHash: ff5914633cde854dd6bd25f915a9ca1376a64e18f16df458d28c24a193535375
        String directTxHash = "0x024f40dc947b30693f8c037441d9b925aa87610fd9ab04c33c3dfb8648209fe9";
        TransactionReceipt txReceipt = ethWalletApi.getTxReceipt(directTxHash);
        List<org.web3j.protocol.core.methods.response.Log> logs = txReceipt.getLogs();
        org.web3j.protocol.core.methods.response.Log log = logs.get(0);
        List<Object> eventResult = EthUtil.parseEvent(log.getData(), EthConstant.EVENT_TRANSACTION_MANAGER_CHANGE_COMPLETED);
        System.out.println(JSONUtils.obj2PrettyJson(eventResult));
    }

    @Test
    public void eventHashTest() {
        System.out.println(String.format("event: %s, hash: %s", EthConstant.EVENT_DEPOSIT_FUNDS.getName(), EventEncoder.encode(EthConstant.EVENT_DEPOSIT_FUNDS)));
        System.out.println(String.format("event: %s, hash: %s", EthConstant.EVENT_TRANSFER_FUNDS.getName(), EventEncoder.encode(EthConstant.EVENT_TRANSFER_FUNDS)));
        System.out.println(String.format("event: %s, hash: %s", EthConstant.EVENT_TRANSACTION_WITHDRAW_COMPLETED.getName(), EventEncoder.encode(EthConstant.EVENT_TRANSACTION_WITHDRAW_COMPLETED)));
        System.out.println(String.format("event: %s, hash: %s", EthConstant.EVENT_TRANSACTION_MANAGER_CHANGE_COMPLETED.getName(), EventEncoder.encode(EthConstant.EVENT_TRANSACTION_MANAGER_CHANGE_COMPLETED)));
    }

    @Test
    public void functionReturnDecoderTest() throws JsonProcessingException {
        Function allManagersFunction = new Function(
                "allManagers",
                List.of(),
                List.of(new TypeReference<DynamicArray<Address>>() {
                })
        );
        Function ifManagerFunction = new Function(
                "ifManager",
                List.of(new Address("0x54eab3868b0090e6e1a1396e0e54f788a71b2b17")),
                List.of(new TypeReference<Bool>() {
                })
        );
        String valueOfAllManagers = "0x0000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000600000000000000000000000009534d4692f568bc6e9bef3b4d84d48f19e52501000000000000000000000000f3c90ef58ec31805af11ce5fa6d39e395c66441f0000000000000000000000006afb1f9ca069bc004dcf06c51b42992dbd90adba0000000000000000000000008f05ae1c759b8db56ff8124a89bb1305ece17b65000000000000000000000000d29e172537a3fb133f790ebe57ace8221cb8024f00000000000000000000000054eab3868b0090e6e1a1396e0e54f788a71b2b17";
        String valueOfIfManager = "0x0000000000000000000000000000000000000000000000000000000000000001";
        List<Type> typeOfAllManager = FunctionReturnDecoder.decode(valueOfAllManagers, allManagersFunction.getOutputParameters());
        List<Type> typeOfIfManager = FunctionReturnDecoder.decode(valueOfIfManager, ifManagerFunction.getOutputParameters());
        System.out.println(JSONUtils.obj2json(typeOfAllManager));
        System.out.println(JSONUtils.obj2json(typeOfIfManager));
    }

    @Test
    public void txInputWithdrawDecoderTest() throws JsonProcessingException {
        // 46b4c37e
        String input = "0x46b4c37e00000000000000000000000000000000000000000000000000000000000000a00000000000000000000000001ecdaa0af57cafcdead950323ecd79eadc0d649b000000000000000000000000000000000000000000000000002386f26fc1000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000004061363265653835366435623563633761366635353735353135663336306365623363316434666331346438363538383833613466373135393434343931363839";
        List<Object> typeList = EthUtil.parseInput(input, EthConstant.INPUT_WITHDRAW);
        System.out.println(JSONUtils.obj2PrettyJson(typeList));

    }

    @Test
    public void txInputChangeDecoderTest() throws JsonProcessingException {
        String changeInput = "0xbdeaa8ba000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000e00000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000403764353638636639663964336335336536633433636539323738346365633231333061323332356133366134373233646564373935316666373039373665343100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000d29e172537a3fb133f790ebe57ace8221cb8024f";
        List<Object> typeListOfChange = EthUtil.parseInput(changeInput, EthConstant.INPUT_CHANGE);
        System.out.println(JSONUtils.obj2PrettyJson(typeListOfChange));
    }

    @Test
    public void ethEstimateGasTest() throws Exception {
        String contractAddress = "0xf7c9bb7e6d6b8f603f17f6af0713d99ee285bddb";
        // args: "bnmbnm",["0xc11D9943805e56b630A401D4bd9A29550353EFa1","0x11eFE2A9CF96175AB241e4A88A6b79C4f1c70389"],[]
        Function function = new Function(
                "createOrSignManagerChange",
                List.of(new Utf8String("0129833793e14165d04c4961bf1a83c1596539d5365a0a588c48a9f1e5e0172c"),
                        new DynamicArray(Address.class, List.of(new Address("0x9458de7f162a5a35faf52f6b4c715187ec2269e5"))),
                        new DynamicArray(Address.class, List.of()), new Uint8(1)),
                List.of(new TypeReference<Type>() {})
        );

        String encodedFunction = FunctionEncoder.encode(function);

        org.web3j.protocol.core.methods.request.Transaction tx = new org.web3j.protocol.core.methods.request.Transaction(
                "0xdd7cbedde731e78e8b8e4b2c212bc42fa7c09d03",//364151
                null,
                BigInteger.valueOf(1_00_0000_0000_0000_0000L),
                BigInteger.valueOf(10000000L),
                contractAddress,
                null,
                encodedFunction
        );

        EthEstimateGas estimateGas = ethWalletApi.getWeb3j().ethEstimateGas(tx).send();
        if(estimateGas.getResult() != null) {
            System.out.println(String.format("gasLimit: %s, 详情: %s", estimateGas.getResult(), JSONUtils.obj2PrettyJson(estimateGas)));
        } else {
            System.out.println(JSONUtils.obj2PrettyJson(estimateGas.getError()));
        }
    }

    @Test
    public void ethCallTest() throws Exception {
        String contractAddress = "0xf7c9bb7e6d6b8f603f17f6af0713d99ee285bddb";
        // args: "bnmbnm",["0xc11D9943805e56b630A401D4bd9A29550353EFa1","0x11eFE2A9CF96175AB241e4A88A6b79C4f1c70389"],[]
        Function function = new Function(
                "createOrSignManagerChange",
                List.of(new Utf8String("0129833793e14165d04c4961bf1a83c1596539d5365a0a588c48a9f1e5e0172c"),
                        new DynamicArray(Address.class, List.of(new Address("0x9458de7f162a5a35faf52f6b4c715187ec2269e5"))),
                        new DynamicArray(Address.class, List.of()), new Uint8(1)),
                List.of(new TypeReference<Type>() {})
        );

        String encodedFunction = FunctionEncoder.encode(function);

        org.web3j.protocol.core.methods.request.Transaction tx = new org.web3j.protocol.core.methods.request.Transaction(
                "0xdd7cbedde731e78e8b8e4b2c212bc42fa7c09d03",//364151
                null,
                BigInteger.valueOf(1_00_0000_0000_0000_0000L),
                BigInteger.valueOf(10000000L),
                contractAddress,
                null,
                encodedFunction
        );

        EthCall ethCall = ethWalletApi.getWeb3j().ethCall(tx, DefaultBlockParameterName.LATEST).send();
        if(ethCall.isReverted()) {
            System.out.println(ethCall.getRevertReason());
            return;
        }
        System.out.println("验证成功");
    }

    @Test
    public void getCurrentGasPrice() throws IOException {
        // default ropsten
        //setRinkeby();
        setMain();
        BigInteger gasPrice = ethWalletApi.getWeb3j().ethGasPrice().send().getGasPrice();
        System.out.println(gasPrice);
        System.out.println(new BigDecimal(gasPrice).divide(BigDecimal.TEN.pow(9)).toPlainString());
    }

    @Test
    public void getCurrentNonce() throws Exception {
        //setMain();
        System.out.println(ethWalletApi.getNonce("0xf173805F1e3fE6239223B17F0807596Edc283012"));
        System.out.println(ethWalletApi.getLatestNonce("0xf173805F1e3fE6239223B17F0807596Edc283012"));
    }

    @Test
    public void overrideTest() throws Exception {
        String from = "0x09534d4692F568BC6e9bef3b4D84d48f19E52501";
        String fromPriKey = "59f770e9c44075de07f67ba7a4947c65b7c3a0046b455997d1e0f854477222c8";
        for(int i = 86; i <= 104; i++) {
            String hash = ethWalletApi.sendETHWithNonce(from, fromPriKey, from, BigDecimal.ZERO, EthConstant.ETH_GAS_LIMIT_OF_ETH,
                    BigInteger.valueOf(5L).multiply(BigInteger.TEN.pow(9)),
                    BigInteger.valueOf(i));
            System.out.println(String.format("hash is %s", hash));
        }
    }

    @Test
    public void overrideOneTest() throws Exception {
        // 设置eth主网
        setMain();
        String from = "0x09534d4692F568BC6e9bef3b4D84d48f19E52501";
        String fromPriKey = "59f770e9c44075de07f67ba7a4947c65b7c3a0046b455997d1e0f854477222c8";
        String hash = ethWalletApi.sendETHWithNonce(from, fromPriKey, from, BigDecimal.ZERO, EthConstant.ETH_GAS_LIMIT_OF_ETH,
                BigInteger.valueOf(105L).multiply(BigInteger.TEN.pow(9)),//gas price
                BigInteger.valueOf(18989345));// nonce
        System.out.println(String.format("hash is %s", hash));
    }

    private Set<String> allManagers(String contract) throws Exception {
        Function allManagersFunction = new Function(
                "allManagers",
                List.of(),
                List.of(new TypeReference<DynamicArray<Address>>() {
                })
        );
        Function function = allManagersFunction;
        String encode = FunctionEncoder.encode(function);
        org.web3j.protocol.core.methods.request.Transaction ethCallTransaction = org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(null, contract, encode);
        EthCall ethCall = ethWalletApi.getWeb3j().ethCall(ethCallTransaction, DefaultBlockParameterName.PENDING).sendAsync().get();
        String value = ethCall.getResult();
        List<Type> typeList = FunctionReturnDecoder.decode(value, function.getOutputParameters());
        List<String> results = new ArrayList();
        for(Type type : typeList) {
            results.add(type.getValue().toString());
        }
        String resultStr = results.get(0).substring(1, results.get(0).length() - 1);
        String[] resultArr = resultStr.split(",");
        Set<String> resultList = new HashSet<>();
        for(String result : resultArr) {
            resultList.add(result.trim().toLowerCase());
        }
        return resultList;
    }

    String[] prikeyOfSeeds;
    String contractAddress;
    boolean newMode = false;
    String apiURL;
    private void localdev() {
        newMode = true;
        prikeyOfSeeds = new String[]{
                "b54db432bba7e13a6c4a28f65b925b18e63bcb79143f7b894fa735d5d3d09db5",
                "188b255c5a6d58d1eed6f57272a22420447c3d922d5765ebb547bc6624787d9f",
                "fbcae491407b54aa3904ff295f2d644080901fda0d417b2b427f5c1487b2b499",
                "f89e2563ec7c977cafa2efa41551bd3651d832d587b7d8d8912ebc2b91e24bbd",
                "8c6715620151478cdd4ee8c95b688f2c2112a21a266f060973fa776be3f0ebd7"
        };
        contractAddress = "0xce0fb0b8075f8f54517d939cb4887ba42d97a23a";
        apiURL = "http://192.168.1.70:17004/api/converter/bank";
    }

    private void localdevII() {
        newMode = true;
        prikeyOfSeeds = new String[]{
                "b54db432bba7e13a6c4a28f65b925b18e63bcb79143f7b894fa735d5d3d09db5",
                "188b255c5a6d58d1eed6f57272a22420447c3d922d5765ebb547bc6624787d9f",
                "fbcae491407b54aa3904ff295f2d644080901fda0d417b2b427f5c1487b2b499"
        };
        contractAddress = "0x4A05428eC53195e4657739e7622E04594F8c4020";
        apiURL = "http://192.168.1.70:17004/api/converter/bank";
    }

    private void testnet() {
        newMode = true;
        prikeyOfSeeds = new String[]{
                "978c643313a0a5473bf65da5708766dafc1cca22613a2480d0197dc99183bb09",
                "6e905a55d622d43c499fa844c05db46859aed9bb525794e2451590367e202492",
                "d48b870f2cf83a739a134cd19015ed96d377f9bc9e6a41108ac82daaca5465cf",
                "be8b657d9d84992463270814bbd7b7683079d7a2ea326bd1e75375863ef29d16"
        };
        contractAddress = "0x44f4eA5028992D160Dc0dc9A3cB93a2e4C913611";
        apiURL = "http://seeda.nuls.io:17004/api/converter/bank";
    }

    @Test
    public void overrideSeedTest() throws Exception {
        localdev();
        //testnet();
        for(int i = 0, length = prikeyOfSeeds.length; i < length; i++) {
            String fromPriKey = prikeyOfSeeds[i];
            Credentials credentials = Credentials.create(fromPriKey);
            String from = credentials.getAddress();
            String hash = ethWalletApi.sendETHWithNonce(from, fromPriKey, from, BigDecimal.ZERO, EthConstant.ETH_GAS_LIMIT_OF_ETH,
                    BigInteger.valueOf(105L).multiply(BigInteger.TEN.pow(9)),
                    ethWalletApi.getLatestNonce(from));
            System.out.println(String.format("hash is %s", hash));
        }
    }

    @Test
    public void collection() throws Exception {
        setMain();
        List<String[]> list = new ArrayList<>();
        list.add(new String[]{"", ""});
        list.add(new String[]{"", ""});
        list.add(new String[]{"", ""});
        list.add(new String[]{"", ""});
        list.add(new String[]{"", ""});
        list.add(new String[]{"", ""});
        list.add(new String[]{"", ""});
        EthContext.setEthGasPrice(BigInteger.valueOf(40L).multiply(BigInteger.TEN.pow(9)));
        String to = "0x1C7b75db2F8983C3B318D957D3A789f89003e0C0";
        String from, fromPriKey;
        for(String[] info : list) {
            from = info[0];
            fromPriKey = info[1];
            BigInteger fromBalance = ethWalletApi.getBalance(from).toBigInteger();
            BigInteger fee = BigInteger.valueOf(21000L).multiply(EthContext.getEthGasPrice());
            BigInteger value = fromBalance.subtract(fee);
            BigDecimal ethValue = ETHWalletApi.convertWeiToEth(value);
            String txHash = ethWalletApi.sendETH(from, fromPriKey, to, ethValue, BigInteger.valueOf(21000L), EthContext.getEthGasPrice());
            System.out.println(String.format("[%s]向[%s]转账%s个ETH, 手续费: %s, 交易hash: %s", from, to, ETHWalletApi.convertWeiToEth(value).toPlainString(), ETHWalletApi.convertWeiToEth(fee).toPlainString(), txHash));
        }
    }

    @Test
    public void balanceOfContractManagerSet() throws Exception {
        localdev();
        //testnet();
        BigInteger gasPrice = BigInteger.valueOf(10L).multiply(BigInteger.TEN.pow(9));
        String from = "0x09534d4692F568BC6e9bef3b4D84d48f19E52501";
        String fromPriKey = "59f770e9c44075de07f67ba7a4947c65b7c3a0046b455997d1e0f854477222c8";
        System.out.println("查询当前合约管理员列表，请等待……");
        Set<String> all = this.allManagers(contractAddress);
        System.out.println(String.format("size : %s", all.size()));
        String sendAmount = "0.1";
        for (String address : all) {
            BigDecimal balance = ethWalletApi.getBalance(address).movePointLeft(18);
            System.out.print(String.format("address %s : %s", address, balance.toPlainString()));
            if (balance.compareTo(new BigDecimal(sendAmount)) < 0) {
                String txHash = ethWalletApi.sendETH(from, fromPriKey, address, new BigDecimal(sendAmount), BigInteger.valueOf(21000L), gasPrice);
                System.out.print(String.format(", 向[%s]转账%s个ETH, 交易hash: %s", address, sendAmount, txHash));
            }
            System.out.println();
        }
    }



    @Test
    public void compareManagersBetweenNerveAndContract() throws Exception {
//        localdev();
        // 测试网环境合约
        testnet();

        Set<String> managerFromNerve = new HashSet<>();
        SDKContext.wallet_url = "";
        RestFulResult<List<Object>> result = RestFulUtil.getList(apiURL, null);
        List<Object> data = result.getData();
        for(Object obj : data) {
            // ((Map)((List)dataMap.get("heterogeneousAddresses")).get(0)).get("address")
            Map dataMap = (Map) obj;
            String address = (String) ((Map) ((List) dataMap.get("heterogeneousAddresses")).get(0)).get("address");
            managerFromNerve.add(address);
        }
        System.out.println(String.format("   nerve size: %s, 详情: %s", managerFromNerve.size(), managerFromNerve));

        Set<String> managerFromContract = this.allManagers(contractAddress);
        for(String addr : managerFromContract) {
            if (!managerFromNerve.contains(addr)) {
                System.out.println(String.format("Nerve没有address: %s", addr));
            }
        }
        System.out.println();
        System.out.println(String.format("contract size: %s, 详情: %s", managerFromContract.size(), managerFromContract));
        for(String addr : managerFromNerve) {
            if (!managerFromContract.contains(addr)) {
                System.out.println(String.format("合约没有address: %s", addr));
            }
        }

    }

    @Test
    public void resetContract() throws Exception {
        // 数据准备
        EthContext.setEthGasPrice(BigInteger.valueOf(10L).multiply(BigInteger.TEN.pow(9)));

        // 本地开发合约重置
        localdev();
        //localdevII();

        // 测试网合约重置
        //testnet();

        // 种子管理员地址
        String[] seeds = new String[prikeyOfSeeds.length];
        int i = 0;
        for(String pri : prikeyOfSeeds) {
            seeds[i++] = Credentials.create(pri).getAddress().toLowerCase();
        }
        System.out.println("查询当前合约管理员列表，请等待……");
        Set<String> all = this.allManagers(contractAddress);
        System.out.println(String.format("当前合约管理员列表: %s", Arrays.toString(all.toArray())));
        // 应剔除的管理员
        for(String seed : seeds) {
            all.remove(seed);
        }
        if (all.size() == 0) {
            System.out.println("[结束]合约已重置管理员");
            return;
        }
        String key = "change-" + System.currentTimeMillis();
        System.out.println(String.format("交易Key: %s", key));
        System.out.println(String.format("剔除的管理员: %s", Arrays.toString(all.toArray())));
        List<Address> addressList = all.stream().map(a -> new Address(a)).collect(Collectors.toList());
        List<Type> inputTypeList;
        if (newMode) {
            inputTypeList = List.of(
                    new Utf8String(key),
                    new DynamicArray(Address.class, List.of()),
                    new DynamicArray(Address.class, addressList),
                    new Uint8(1)
            );
        } else {
            inputTypeList = List.of(
                    new Utf8String(key),
                    new DynamicArray(Address.class, List.of()),
                    new DynamicArray(Address.class, addressList)
            );
        }
        Function changeFunction = new Function(
                "createOrSignManagerChange",
                inputTypeList,
                List.of(new TypeReference<Type>() {}));
        i = 0;
        for(String pri : prikeyOfSeeds) {
            String seed = seeds[i++];
            System.out.println(String.format("种子管理员[%s]发送多签交易", seed));
            EthSendTransactionPo po = ethWalletApi.callContract(seed, pri, contractAddress, BigInteger.valueOf(800000L), changeFunction);
            System.out.println(String.format("  交易已发出, hash: %s", po.getTxHash()));
            if(i == prikeyOfSeeds.length) {
                System.out.println("[完成]多签交易已全部发送完成");
                break;
            }
            System.out.println("等待8秒发送下一个多签交易");
            TimeUnit.SECONDS.sleep(8);
        }
        while (true) {
            System.out.println("等待10秒查询进度");
            TimeUnit.SECONDS.sleep(10);
            if(this.allManagers(contractAddress).size() == seeds.length) {
                System.out.println(String.format("[结束]合约已重置管理员"));
                break;
            }
        }

    }

    @Test
    public void addManagerBySeeder() throws Exception {
        // 数据准备
        EthContext.setEthGasPrice(BigInteger.valueOf(30L).multiply(BigInteger.TEN.pow(9)));
        List<String> list = new ArrayList<>();
        //list.add("0x09534d4692F568BC6e9bef3b4D84d48f19E52501");
        //list.add("0xF3c90eF58eC31805af11CE5FA6d39E395c66441f");
        //list.add("0x6afb1F9Ca069bC004DCF06C51B42992DBD90Adba");

        //list.add("0x8F05AE1C759b8dB56ff8124A89bb1305ECe17B65");
        //list.add("0xd29E172537A3FB133f790EBE57aCe8221CB8024F");
        list.add("0x54eAB3868B0090E6e1a1396E0e54F788a71B2b17");

        // 本地开发合约重置
        localdev();

        // 测试网合约重置
        //testnet();

        // 种子管理员地址
        String[] seeds = new String[prikeyOfSeeds.length];
        int i = 0;
        for(String pri : prikeyOfSeeds) {
            seeds[i++] = Credentials.create(pri).getAddress().toLowerCase();
        }
        System.out.println("查询当前合约管理员列表，请等待……");
        Set<String> all = this.allManagers(contractAddress);
        System.out.println(String.format("当前合约管理员列表: %s", Arrays.toString(all.toArray())));
        // 检查是否存在非种子管理员
        for(String seed : seeds) {
            all.remove(seed);
        }
        if (all.size() != 0) {
            System.out.println(String.format("[错误]合约存在非种子管理员，请移除非种子管理员再执行, 列表: %s", Arrays.toString(all.toArray())));
            return;
        }
        String key = "add-" + System.currentTimeMillis();
        System.out.println(String.format("交易Key: %s", key));
        System.out.println(String.format("添加的管理员: %s", Arrays.toString(list.toArray())));
        List<Address> addressList = list.stream().map(a -> new Address(a)).collect(Collectors.toList());
        List<Type> inputTypeList;
        if (newMode) {
            inputTypeList = List.of(
                    new Utf8String(key),
                    new DynamicArray(Address.class, addressList),
                    new DynamicArray(Address.class, List.of()),
                    new Uint8(1)
            );
        } else {
            inputTypeList = List.of(
                    new Utf8String(key),
                    new DynamicArray(Address.class, addressList),
                    new DynamicArray(Address.class, List.of())
            );
        }
        Function changeFunction = new Function(
                "createOrSignManagerChange",
                inputTypeList,
                List.of(new TypeReference<Type>() {}));
        i = 0;
        for(String pri : prikeyOfSeeds) {
            String seed = seeds[i++];
            System.out.println(String.format("种子管理员[%s]发送多签交易", seed));
            EthSendTransactionPo po = ethWalletApi.callContract(seed, pri, contractAddress, BigInteger.valueOf(800000L), changeFunction);
            System.out.println(String.format("  交易已发出, hash: %s", po.getTxHash()));
            if(i == prikeyOfSeeds.length) {
                System.out.println("[完成]多签交易已全部发送完成");
                break;
            }
            System.out.println("等待8秒发送下一个多签交易");
            TimeUnit.SECONDS.sleep(8);
        }
        while (true) {
            System.out.println("等待10秒查询进度");
            TimeUnit.SECONDS.sleep(10);
            if(this.allManagers(contractAddress).size() == seeds.length + list.size()) {
                System.out.println(String.format("[结束]合约已添加管理员"));
                break;
            }
        }

    }

    @Test
    public void upgradeContract() throws Exception {
        // 数据准备
        EthContext.setEthGasPrice(BigInteger.valueOf(30L).multiply(BigInteger.TEN.pow(9)));
        String superAdmin = "";
        String superAdminPrivatekey = "";
        String oldContract = "";
        String newContract = "";
        String USDX = "0xB058887cb5990509a3D0DD2833B2054E4a7E4a55";

        // 种子管理员地址
        String[] seeds = new String[prikeyOfSeeds.length];
        int i = 0;
        for(String pri : prikeyOfSeeds) {
            seeds[i++] = Credentials.create(pri).getAddress().toLowerCase();
        }
        System.out.println("查询当前合约管理员列表，请等待……");
        Set<String> all = this.allManagers(contractAddress);
        System.out.println(String.format("当前合约管理员列表: %s", Arrays.toString(all.toArray())));
        // 检查是否存在非种子管理员
        for(String seed : seeds) {
            all.remove(seed);
        }
        if (all.size() != 0) {
            System.out.println(String.format("[错误]合约存在非种子管理员，请移除非种子管理员再执行, 列表: %s", Arrays.toString(all.toArray())));
            return;
        }
        String key = "upgrade-" + System.currentTimeMillis();
        System.out.println(String.format("交易Key: %s", key));
        List<Type> inputTypeList = List.of(new Utf8String(key));
        Function changeFunction = new Function(
                "createOrSignUpgrade",
                inputTypeList,
                List.of(new TypeReference<Type>() {}));
        i = 0;
        for(String pri : prikeyOfSeeds) {
            String seed = seeds[i++];
            System.out.println(String.format("种子管理员[%s]发送多签交易", seed));
            EthSendTransactionPo po = ethWalletApi.callContract(seed, pri, contractAddress, BigInteger.valueOf(800000L), changeFunction);
            System.out.println(String.format("  交易已发出, hash: %s", po.getTxHash()));
            if(i == prikeyOfSeeds.length) {
                System.out.println("[完成]多签交易已全部发送完成");
                break;
            }
            System.out.println("等待8秒发送下一个多签交易");
            TimeUnit.SECONDS.sleep(8);
        }
        while (true) {
            System.out.println("等待10秒查询进度");
            TimeUnit.SECONDS.sleep(10);
            if(isUpgrade(oldContract)) {
                System.out.println(String.format("旧合约已升级"));
                break;
            }
        }
        System.out.println("-=-=-=-=-=-[开始执行资产转移]=-=-=-=-=-=-=");
        // upgradeContractS1 upgradeContractS2
        BigDecimal ethBalance = ethWalletApi.getBalance(oldContract);
        List<Type> input1 = List.of();
        Function upgradeFunction1 = new Function(
                "upgradeContractS1",
                input1,
                List.of(new TypeReference<Type>() {}));
        EthSendTransactionPo po1 = ethWalletApi.callContract(superAdmin, superAdminPrivatekey, oldContract, BigInteger.valueOf(800000L), upgradeFunction1);
        System.out.println(String.format("[ETH资产]转移第一步hash: %s", po1.getTxHash()));
        while (ethWalletApi.getTxReceipt(po1.getTxHash()) == null) {
            System.out.println("等待8秒查询[ETH资产]转移第一步结果");
            TimeUnit.SECONDS.sleep(8);
        }
        System.out.println(String.format("[ETH资产]转移第一步完成"));
        String txHash2 = ethWalletApi.sendETH(superAdmin, superAdminPrivatekey, newContract, ethBalance, BigInteger.valueOf(800000L), EthContext.getEthGasPrice());
        System.out.println(String.format("[ETH资产]转移第二步hash: %s", txHash2));
        while (ethWalletApi.getTxReceipt(po1.getTxHash()) == null) {
            System.out.println("等待8秒查询[ETH资产]转移第二步结果");
            TimeUnit.SECONDS.sleep(8);
        }
        System.out.println(String.format("[ETH资产]转移完成，转移数额: %s", ETHWalletApi.convertWeiToEth(ethBalance.toBigInteger()).toPlainString()));
        //ethWalletApi.sendETH()
        BigInteger USDXBalance = ethWalletApi.getERC20Balance(oldContract, USDX);
        List<Type> input2 = List.of(
                new Address(USDX),
                new Address(newContract),
                new Uint256(USDXBalance)
        );
        Function upgradeFunction2 = new Function(
                "upgradeContractS2",
                input2,
                List.of(new TypeReference<Type>() {}));
        EthSendTransactionPo po2 = ethWalletApi.callContract(superAdmin, superAdminPrivatekey, oldContract, BigInteger.valueOf(800000L), upgradeFunction2);
        System.out.println(String.format("[USDX资产]转移hash: %s", po2.getTxHash()));
        while (ethWalletApi.getTxReceipt(po2.getTxHash()) == null) {
            System.out.println("等待8秒查询[USDX资产]转移结果");
            TimeUnit.SECONDS.sleep(8);
        }
        System.out.println(String.format("[USDX资产]转移完成"));
    }

    private boolean isUpgrade(String contract) throws Exception {
        Function ifManagerFunction = new Function(
                "ifManager",
                List.of(new Address(contract)),
                List.of(new TypeReference<Bool>() {
                })
        );
        Function function = ifManagerFunction;
        String encode = FunctionEncoder.encode(function);
        org.web3j.protocol.core.methods.request.Transaction ethCallTransaction = org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(null, contractAddress, encode);
        EthCall ethCall = ethWalletApi.getWeb3j().ethCall(ethCallTransaction, DefaultBlockParameterName.PENDING).sendAsync().get();
        String value = ethCall.getResult();
        List<Type> typeList = FunctionReturnDecoder.decode(value, function.getOutputParameters());
        List results = new ArrayList();
        for(Type type : typeList) {
            results.add(type.getValue());
        }
        return (boolean) results.get(0);
    }

    @Test
    public void sendETH() throws Exception {
        setMain();
        EthContext.setEthGasPrice(BigInteger.valueOf(32L).multiply(BigInteger.TEN.pow(9)));
        String from = "";
        String fromPriKey = "";
        String to = "";
        String txHash = ethWalletApi.sendETH(from, fromPriKey, to, new BigDecimal("0.01"), BigInteger.valueOf(80000L), EthContext.getEthGasPrice());
        System.out.println(String.format("send to %s, value is 0.01, hash is %s", to, txHash));
    }
    @Test
    public void sendERC20() throws Exception {
        setMain();
        EthContext.setEthGasPrice(BigInteger.valueOf(32L).multiply(BigInteger.TEN.pow(9)));
        String from = "";
        String fromPriKey = "";
        String to = "";
        BigInteger value = BigInteger.valueOf(100L).multiply(BigInteger.valueOf(10L).pow(18));
        String contractAddress = "";
        //加载转账所需的凭证，用私钥
        Credentials credentials = Credentials.create(fromPriKey);
        //获取nonce，交易笔数
        BigInteger nonce = BigInteger.valueOf(10L);

        //创建RawTransaction交易对象
        Function function = new Function(
                "transfer",
                Arrays.asList(new Address(to), new Uint256(value)),
                Arrays.asList(new TypeReference<Type>() {
                }));

        String encodedFunction = FunctionEncoder.encode(function);

        RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                EthContext.getEthGasPrice(),
                BigInteger.valueOf(80000L),
                contractAddress, encodedFunction
        );
        //签名Transaction，这里要对交易做签名
        byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signMessage);
        //发送交易
        EthSendTransaction ethSendTransaction = ethWalletApi.getWeb3j().ethSendRawTransaction(hexValue).sendAsync().get();

        System.out.println(String.format("send to %s, value is 100, hash is %s", to, ethSendTransaction.getTransactionHash()));
    }
    @Test
    public void test() {
        System.out.println(new BigInteger("10000000000000000000000000").divide(BigInteger.valueOf(10L).pow(18)));
    }
    @Test
    public void maintestSendEth() throws Exception {
        setMain();
        EthContext.setEthGasPrice(BigInteger.valueOf(22L).multiply(BigInteger.TEN.pow(9)));
        String admin = "";
        String adminPk = "";
        String recharge1 = "";
        String recharge2 = "";
        String erc20Owner = "";
        List<String> list = new ArrayList<>();
        list.add("");
        list.add("");
        list.add("");
        list.add("");
        list.add("");
        BigDecimal seedAmount = new BigDecimal("0.06");
        for(String address : list) {
            String txHash = ethWalletApi.sendETH(admin, adminPk, address, seedAmount, BigInteger.valueOf(30000L), EthContext.getEthGasPrice());
            System.out.println(String.format("send to %s, value is %s, hash is %s", address, seedAmount, txHash));
        }

        String txHash = ethWalletApi.sendETH(admin, adminPk, erc20Owner, new BigDecimal("0.01"), BigInteger.valueOf(30000L), EthContext.getEthGasPrice());
        System.out.println(String.format("send to %s, value is 0.01, hash is %s", erc20Owner, txHash));

        txHash = ethWalletApi.sendETH(admin, adminPk, recharge1, new BigDecimal("0.01"), BigInteger.valueOf(30000L), EthContext.getEthGasPrice());
        System.out.println(String.format("send to %s, value is 0.01, hash is %s", recharge1, txHash));

        txHash = ethWalletApi.sendETH(admin, adminPk, recharge2, new BigDecimal("0.03"), BigInteger.valueOf(30000L), EthContext.getEthGasPrice());
        System.out.println(String.format("send to %s, value is 0.03, hash is %s", recharge2, txHash));
    }


    private BigDecimal ethToWei(String eth) {
        BigDecimal _eth = new BigDecimal(eth);
        return _eth.movePointRight(18);
    }



    /**
     * 弃用，原因如下
     * 1. 当tx存在chainId时，转换会失败，原因是有chainId时，v变量大于了34
     * 2. 没有chainId, tx有一个错误的v值，也会造成转换失败
     * 3. 因此，使用Sign.recoverFromSignature，遍历每一个可能的v值，来得到正确的公钥
     */
    @Deprecated
    private void publicKey(Transaction tx) {
        byte[] hashBytes = getRawTxHashBytes(tx);

        //String hash = "5eb0711c34fbd673be0c9112024bd9f66554d3e18dcb1acd97524168617c137d";
        byte v = (byte) (tx.getV());
        byte[] r = Numeric.hexStringToByteArray(tx.getR());
        byte[] s = Numeric.hexStringToByteArray(tx.getS());

        Sign.SignatureData signatureData = new Sign.SignatureData(v, r, s);
        try {
            BigInteger pKey = Sign.signedMessageHashToKey(hashBytes, signatureData);
            //System.out.println("p: " + Numeric.encodeQuantity(pKey));
            //BigInteger pKey = converPublicKey(tx);
            String address = "0x" + Keys.getAddress(pKey);
            if (tx.getFrom().toLowerCase().equals(address.toLowerCase())) {
                System.out.println("成功: " + v + ": " + address + String.format("tx hash: %s, tx index: %s, tx data length: %s, tx chainId: %s", tx.getHash(), tx.getTransactionIndex(), tx.getInput().length(), tx.getChainId()));
            } else {
                System.err.println(String.format("失败: tx from: %s, parse from: %s, tx hash: %s, tx index: %s, tx data length: %s, tx chainId: %s", tx.getFrom(), address, tx.getHash(), tx.getTransactionIndex(), tx.getInput().length(), tx.getChainId()));
            }
        } catch (Exception e) {
            System.err.println(String.format("异常: %s error, tx hash: %s, tx index: %s, tx data length: %s, tx chainId: %s", v, tx.getHash(), tx.getTransactionIndex(), tx.getInput().length(), tx.getChainId()));
        }
    }

}