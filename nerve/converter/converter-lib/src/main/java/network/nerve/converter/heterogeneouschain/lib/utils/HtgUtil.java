/**
 * MIT License
 * <p>
 * Copyright (c) 2019-2020 nerve.network
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package network.nerve.converter.heterogeneouschain.lib.utils;

import io.nuls.base.basic.AddressTool;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.model.StringUtils;
import network.nerve.converter.enums.AssetName;
import network.nerve.converter.heterogeneouschain.lib.context.HtgConstant;
import network.nerve.converter.heterogeneouschain.lib.model.HtgAccount;
import network.nerve.converter.heterogeneouschain.lib.model.HtgUnconfirmedTxPo;
import network.nerve.converter.heterogeneouschain.lib.model.Token20TransferDTO;
import network.nerve.converter.model.bo.HeterogeneousTransactionInfo;
import org.ethereum.crypto.ECKey;
import org.springframework.beans.BeanUtils;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.*;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: Mimi
 * @date: 2020-02-26
 */
public class HtgUtil {

    public static HeterogeneousTransactionInfo newTransactionInfo(Transaction tx, int nerveChainId) {
        HeterogeneousTransactionInfo txInfo = new HeterogeneousTransactionInfo();
        txInfo.setTxHash(tx.getHash());
        txInfo.setBlockHeight(tx.getBlockNumber().longValue());
        txInfo.setFrom(tx.getFrom());
        txInfo.setTo(tx.getTo());
        txInfo.setValue(tx.getValue());
        txInfo.setNerveAddress(covertNerveAddressByEthTx(tx, nerveChainId));
        return txInfo;
    }

    public static HeterogeneousTransactionInfo newTransactionInfo(HtgUnconfirmedTxPo txPo) {
        HeterogeneousTransactionInfo txInfo = new HeterogeneousTransactionInfo();
        BeanUtils.copyProperties(txPo, txInfo);
        return txInfo;
    }

    public static HtgAccount createAccount(String prikey) {
        Credentials credentials = Credentials.create(prikey);
        ECKeyPair ecKeyPair = credentials.getEcKeyPair();
        byte[] pubKey = ecKeyPair.getPublicKey().toByteArray();
        HtgAccount account = new HtgAccount();
        account.setAddress(credentials.getAddress());
        account.setPubKey(pubKey);
        account.setPriKey(ecKeyPair.getPrivateKey().toByteArray());
        account.setEncryptedPriKey(new byte[0]);
        ECKey ecKey = ECKey.fromPrivate(Numeric.hexStringToByteArray(prikey));
        account.setCompressedPublicKey(Numeric.toHexStringNoPrefix(ecKey.getPubKeyPoint().getEncoded(true)));
        return account;
    }

    public static boolean isEmptyList(List list) {
        if (list != null && list.size() > 0) {
            return false;
        }
        return true;
    }

    public static String covertNerveAddressByEthTx(Transaction tx, int nerveChainId) {
        BigInteger ethPublicKey = extractEthPublicKey(tx);
        return covertNerveAddress(ethPublicKey, nerveChainId);
    }

    public static String genEthAddressByCompressedPublickey(String compressedPublickey) {
        ECKey ecKey = ECKey.fromPublicOnly(Numeric.hexStringToByteArray(compressedPublickey));
        String orginPubkeyStr = HtgConstant.HEX_PREFIX + Numeric.toHexStringNoPrefix(ecKey.getPubKey()).substring(2);
        return HtgConstant.HEX_PREFIX + Keys.getAddress(orginPubkeyStr);
    }

    public static List<Object> parseEvent(String eventData, Event event) {
        List<Type> typeList = FunctionReturnDecoder.decode(eventData, event.getParameters());
        return typeList.stream().map(type -> type.getValue()).collect(Collectors.toList());
    }

    public static List<Object> parseInput(String inputData, List<TypeReference<Type>> parameters) {
        if(StringUtils.isBlank(inputData)) {
            return null;
        }
        if(inputData.length() < 10) {
            return null;
        }
        inputData = HtgConstant.HEX_PREFIX + inputData.substring(10);
        List<Type> typeList = FunctionReturnDecoder.decode(inputData, parameters);
        return typeList.stream().map(type -> type.getValue()).collect(Collectors.toList());
    }

    public static <T>  T[] list2array(List<T> list) {
        if(list == null || list.isEmpty()) {
            return null;
        }
        T[] array = (T[]) Array.newInstance(list.get(0).getClass(), list.size());
        return list.toArray(array);
    }

    public static Function getNameERC20Function() {
        return new Function(
                HtgConstant.METHOD_VIEW_ERC20_NAME,
                List.of(),
                List.of(new TypeReference<Utf8String>() {}));
    }
    public static Function getSymbolERC20Function() {
        return new Function(
                HtgConstant.METHOD_VIEW_ERC20_SYMBOL,
                List.of(),
                List.of(new TypeReference<Utf8String>() {}));
    }
    public static Function getDecimalsERC20Function() {
        return new Function(
                HtgConstant.METHOD_VIEW_ERC20_DECIMALS,
                List.of(),
                List.of(new TypeReference<Uint8>() {}));
    }


    public static Function getIsCompletedFunction(String nerveTxHash) {
        return new Function(
                HtgConstant.METHOD_VIEW_IS_COMPLETED_TRANSACTION,
                List.of(new Utf8String(nerveTxHash)),
                List.of(new TypeReference<Bool>() {
                })
        );
    }

    public static Function getAllManagersFunction() {
        return new Function(
                HtgConstant.METHOD_VIEW_ALL_MANAGERS_TRANSACTION,
                List.of(),
                List.of(new TypeReference<DynamicArray<Address>>() {
                })
        );
    }


    private static BigInteger extractEthPublicKey(Transaction tx) {
        ECDSASignature signature = new ECDSASignature(Numeric.decodeQuantity(tx.getR()), Numeric.decodeQuantity(tx.getS()));
        byte[] hashBytes = getRawTxHashBytes(tx);

        for (int i = 0; i < 4; i++) {
            BigInteger recoverPubKey = Sign.recoverFromSignature(i, signature, hashBytes);
            if (recoverPubKey != null) {
                String address = HtgConstant.HEX_PREFIX + Keys.getAddress(recoverPubKey);
                if (tx.getFrom().toLowerCase().equals(address.toLowerCase())) {
                    return recoverPubKey;
                }
            }
        }
        return null;
    }

    private static byte[] getRawTxHashBytes(Transaction tx) {
        String data = "";
        if (StringUtils.isNotBlank(tx.getInput()) && !HtgConstant.HEX_PREFIX.equals(tx.getInput().toLowerCase())) {
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

    private static String covertNerveAddress(BigInteger ethPublickey, int nerveChainId) {
        String pub = Numeric.toHexStringNoPrefix(ethPublickey);
        pub = leftPadding(pub, "0", 128);
        String pubkeyFromEth = HtgConstant.PUBLIC_KEY_UNCOMPRESSED_PREFIX + pub;
        io.nuls.core.crypto.ECKey ecKey = io.nuls.core.crypto.ECKey.fromPublicOnly(HexUtil.decode(pubkeyFromEth));
        return AddressTool.getAddressString(ecKey.getPubKeyPoint().getEncoded(true), nerveChainId);
    }

    public static String leftPadding(String orgin, String padding, int total) {
        return padding.repeat(total - orgin.length()) + orgin;
    }


    public static Function getCreateOrSignWithdrawFunction(String nerveTxHash, String toAddress, BigInteger value, boolean isContractAsset, String contractAddressERC20, String signatureHexData) {
        return new Function(
                HtgConstant.METHOD_CREATE_OR_SIGN_WITHDRAW,
                List.of(new Utf8String(nerveTxHash),
                        new Address(toAddress),
                        new Uint256(value),
                        new Bool(isContractAsset),
                        new Address(contractAddressERC20),
                        new DynamicBytes(Numeric.hexStringToByteArray(signatureHexData))),
                List.of(new TypeReference<Type>() {
                })
        );
    }

    public static Function getCreateOrSignManagerChangeFunction(String nerveTxHash, List<Address> addList, List<Address> removeList, int orginTxCount, String signatureHexData) {
        return new Function(
                HtgConstant.METHOD_CREATE_OR_SIGN_MANAGERCHANGE,
                List.of(new Utf8String(nerveTxHash),
                        new DynamicArray(Address.class, addList),
                        new DynamicArray(Address.class, removeList),
                        new Uint8(orginTxCount),
                        new DynamicBytes(Numeric.hexStringToByteArray(signatureHexData))),
                List.of(new TypeReference<Type>() {
                })
        );
    }

    public static Function getCreateOrSignUpgradeFunction(String nerveTxHash, String upgradeContract, String signatureHexData) {
        return new Function(
                HtgConstant.METHOD_CREATE_OR_SIGN_UPGRADE,
                List.of(new Utf8String(nerveTxHash),
                        new Address(upgradeContract),
                        new DynamicBytes(Numeric.hexStringToByteArray(signatureHexData))),
                List.of(new TypeReference<Type>() {
                })
        );
    }

    public static Function getCrossOutFunction(String to, BigInteger value, String erc20) {
        return new Function(
                HtgConstant.METHOD_CROSS_OUT,
                List.of(new Utf8String(to),
                        new Uint256(value),
                        new Address(erc20)),
                List.of(new TypeReference<Type>() {
                })
        );
    }

    public static Function getIsMinterERC20Function(String erc20) {
        return new Function(
                HtgConstant.METHOD_VIEW_IS_MINTER_ERC20,
                List.of(new Address(erc20)),
                List.of(new TypeReference<Bool>() {
                })
        );
    }

    public static String encoderWithdraw(String txKey, String toAddress, BigInteger value, Boolean isContractAsset, String erc20, byte version) {
        StringBuilder sb = new StringBuilder();
        sb.append(Numeric.toHexString(txKey.getBytes(StandardCharsets.UTF_8)));
        sb.append(Numeric.cleanHexPrefix(toAddress));
        sb.append(leftPadding(value.toString(16), "0", 64));
        sb.append(isContractAsset ? "01" : "00");
        sb.append(Numeric.cleanHexPrefix(erc20));
        sb.append(String.format("%02x", version & 255));
        byte[] hash = Hash.sha3(Numeric.hexStringToByteArray(sb.toString()));
        return Numeric.toHexString(hash);
    }

    public static String encoderChange(String txKey, String[] adds, int count, String[] removes, byte version) {
        StringBuilder sb = new StringBuilder();
        sb.append(Numeric.toHexString(txKey.getBytes(StandardCharsets.UTF_8)));
        for (String add : adds) {
            sb.append(leftPadding(Numeric.cleanHexPrefix(add), "0", 64));
        }
        sb.append(leftPadding(Integer.toHexString(count), "0", 2));
        for (String remove : removes) {
            sb.append(leftPadding(Numeric.cleanHexPrefix(remove), "0", 64));
        }
        sb.append(String.format("%02x", version & 255));
        byte[] hash = Hash.sha3(Numeric.hexStringToByteArray(sb.toString()));
        return Numeric.toHexString(hash);
    }

    public static String encoderUpgrade(String txKey, String upgradeContract, byte version) {
        StringBuilder sb = new StringBuilder();
        sb.append(Numeric.toHexString(txKey.getBytes(StandardCharsets.UTF_8)));
        sb.append(Numeric.cleanHexPrefix(upgradeContract));
        sb.append(String.format("%02x", version & 255));
        byte[] hash = Hash.sha3(Numeric.hexStringToByteArray(sb.toString()));
        return Numeric.toHexString(hash);
    }

    public static String dataSign(String hashStr, String prikey) {
        byte[] hash = Numeric.hexStringToByteArray(hashStr);
        Credentials credentials = Credentials.create(prikey);
        Sign.SignatureData signMessage = Sign.signMessage(hash, credentials.getEcKeyPair(), false);
        byte[] signed = new byte[65];
        System.arraycopy(signMessage.getR(), 0, signed, 0, 32);
        System.arraycopy(signMessage.getS(), 0, signed, 32, 32);
        System.arraycopy(signMessage.getV(), 0, signed, 64, 1);
        String signedHex = Numeric.toHexStringNoPrefix(signed);
        return signedHex;
    }

    public static Boolean verifySign(String signAddress, String vHash, String signed) {
        signed = Numeric.cleanHexPrefix(signed);
        if (signed.length() != 130) {
            return false;
        }
        String r = "0x" + signed.substring(0, 64);
        String s = "0x" + signed.substring(64, 128);
        ECDSASignature signature = new ECDSASignature(Numeric.decodeQuantity(r), Numeric.decodeQuantity(s));
        byte[] hashBytes = Numeric.hexStringToByteArray(vHash);
        signAddress = signAddress.toLowerCase();
        for (int i = 0; i < 4; i++) {
            BigInteger recover = Sign.recoverFromSignature(i, signature, hashBytes);
            if (recover != null) {
                String address = "0x" + Keys.getAddress(recover);
                if (signAddress.equals(address.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    static BigDecimal MAXIMUM = new BigDecimal("1.5");
    static BigDecimal MEDIAN = new BigDecimal("1.25");
    static BigDecimal SMALL = new BigDecimal("1.1");

    /**
     * 在网络平均价格和用户提供的价格之间，取一个合适的值
     */
    public static BigDecimal calcNiceGasPriceOfWithdraw(AssetName currentNetworkAssetName, BigDecimal gasPriceNetwork, BigDecimal gasPriceSupport) {
        // 非以太网络，用户给多少手续费，就花费多少手续费
        if (currentNetworkAssetName != AssetName.ETH) {
            return gasPriceSupport;
        }
        // 以太网络，最大花费当前网络平均手续费的1.5倍
        BigDecimal maximumPrice;
        if ((maximumPrice = gasPriceNetwork.multiply(MAXIMUM)).compareTo(gasPriceSupport) <= 0) {
            return maximumPrice;
        } else {
            return gasPriceSupport;
        }
        /*BigDecimal maximumPrice, medianPrice, smallPrice;
        if ((maximumPrice = gasPriceNetwork.multiply(MAXIMUM)).compareTo(gasPriceSupport) <= 0) {
            return maximumPrice;
        } else if ((medianPrice = gasPriceNetwork.multiply(MEDIAN)).compareTo(gasPriceSupport) <= 0) {
            return medianPrice;
        } else if ((smallPrice = gasPriceNetwork.multiply(SMALL)).compareTo(gasPriceSupport) <= 0) {
            return smallPrice;
        } else {
            return gasPriceSupport;
        }*/
    }

    public static BigDecimal calcGasPriceOfWithdraw(AssetName otherMainAsset, BigDecimal otherMainAssetUSD, BigDecimal otherMainAssetAmount, BigDecimal currentMainAssetUSD, int hAssetId) {
        if (hAssetId == 0) {
            return null;
        }
        BigDecimal gasLimit;
        if (hAssetId > 1) {
            gasLimit = BigDecimal.valueOf(210000L);
        } else {
            gasLimit = BigDecimal.valueOf(190000L);
        }
        BigDecimal otherMainAssetNumber = otherMainAssetAmount.movePointLeft(otherMainAsset.decimals());
        BigDecimal gasPrice = calcGasPriceByOtherMainAsset(otherMainAssetUSD, otherMainAssetNumber, currentMainAssetUSD, gasLimit);
        return gasPrice;
    }

    private static BigDecimal calcGasPriceByOtherMainAsset(BigDecimal otherMainAssetUSD, BigDecimal otherMainAssetNumber, BigDecimal currentMainAssetUSD, BigDecimal gasLimit) {
        BigDecimal gasPrice = otherMainAssetUSD.multiply(otherMainAssetNumber).divide(currentMainAssetUSD.multiply(gasLimit), 18, RoundingMode.DOWN).movePointRight(18);
        return gasPrice;
    }

    public static BigDecimal calcOtherMainAssetOfWithdraw(AssetName otherMainAsset, BigDecimal otherMainAssetUSD, BigDecimal gasPrice, BigDecimal currentMainAssetUSD, int hAssetId) {
        if (hAssetId == 0) {
            return null;
        }
        BigDecimal gasLimit;
        if (hAssetId > 1) {
            gasLimit = BigDecimal.valueOf(210000L);
        } else {
            gasLimit = BigDecimal.valueOf(190000L);
        }
        BigDecimal otherMainAssetAmount = calcOtherMainAssetByGasPrice(otherMainAsset, otherMainAssetUSD, gasPrice, currentMainAssetUSD, gasLimit);
        // 当NVT作为手续费时，向上取整
        if (otherMainAsset == AssetName.NVT) {
            otherMainAssetAmount = otherMainAssetAmount.divide(BigDecimal.TEN.pow(8), 0, RoundingMode.UP).movePointRight(8);
        }
        return otherMainAssetAmount;
    }

    private static BigDecimal calcOtherMainAssetByGasPrice(AssetName otherMainAsset, BigDecimal otherMainAssetUSD, BigDecimal gasPrice, BigDecimal currentMainAssetUSD, BigDecimal gasLimit) {
        BigDecimal otherMainAssetAmount = currentMainAssetUSD.multiply(gasPrice).multiply(gasLimit).movePointRight(otherMainAsset.decimals()).movePointLeft(18).divide(otherMainAssetUSD, 0, RoundingMode.UP);
        return otherMainAssetAmount;
    }

    public static BigDecimal calcGasPriceOfWithdrawByMainAssetProtocol15(BigDecimal amount, int hAssetId) {
        if (hAssetId == 0) {
            return null;
        }
        BigDecimal gasLimit;
        if (hAssetId > 1) {
            gasLimit = BigDecimal.valueOf(210000L);
        } else {
            gasLimit = BigDecimal.valueOf(190000L);
        }
        BigDecimal gasPrice = amount.divide(gasLimit, 0, RoundingMode.DOWN);
        return gasPrice;
    }

    public static BigDecimal calcMainAssetOfWithdrawProtocol15(BigDecimal gasPrice, int hAssetId) {
        if (hAssetId == 0) {
            return null;
        }
        BigDecimal gasLimit;
        if (hAssetId > 1) {
            gasLimit = BigDecimal.valueOf(210000L);
        } else {
            gasLimit = BigDecimal.valueOf(190000L);
        }
        return gasPrice.multiply(gasLimit);
    }

    public static BigInteger[] sortByInsertionAsc(BigInteger[] orders, BigInteger value) {
        return sortByInsertion(orders, value, true);
    }
    public static BigInteger[] sortByInsertionDsc(BigInteger[] orders, BigInteger value) {
        return sortByInsertion(orders, value, false);
    }

    private static BigInteger[] sortByInsertion(BigInteger[] orders, BigInteger value, boolean aes) {
        if (orders == null) {
            return new BigInteger[]{value};
        }
        int length = orders.length;
        BigInteger order, tmp1 = value, tmp2 = null;
        for (int i = 0; i < length; i++) {
            order = orders[i];
            if (order == null) {
                orders[i] = tmp1;
                break;
            }
            if (tmp2 != null){
                tmp2 = orders[i];
                orders[i] = tmp1;
                tmp1 = tmp2;
                continue;
            }
            int compare = orders[i].compareTo(tmp1);
            if (aes) {
                if (compare < 0) {
                    continue;
                }
            } else {
                if (compare > 0) {
                    continue;
                }
            }
            tmp2 = orders[i];
            orders[i] = tmp1;
            tmp1 = tmp2;

        }
        return orders;
    }

    public static BigInteger[] emptyFillZero(BigInteger[] amounts) {
        if (amounts == null) {
            return null;
        } else {
            int length = amounts.length;

            for(int i = 0; i < length; ++i) {
                if (amounts[i] == null) {
                    amounts[i] = BigInteger.ZERO;
                }
            }

            return amounts;
        }
    }

    public static org.web3j.protocol.core.methods.response.Transaction genEthTransaction(String hash, String txHex) throws SignatureException {
        if (StringUtils.isBlank(txHex)) {
            return null;
        }
        if (StringUtils.isBlank(hash)) {
            hash = Numeric.toHexString(Hash.sha3(Numeric.hexStringToByteArray(txHex)));
        }
        org.web3j.protocol.core.methods.response.Transaction etx = new Transaction();
        SignedRawTransaction tx = (SignedRawTransaction) TransactionDecoder.decode(txHex);
        Sign.SignatureData signatureData = tx.getSignatureData();
        BigInteger bv = Numeric.toBigInt(signatureData.getV());
        etx.setHash(hash);
        etx.setFrom(tx.getFrom());
        etx.setTo(tx.getTo());
        etx.setNonce(tx.getNonce().toString());
        etx.setValue(tx.getValue().toString());
        etx.setGas(tx.getGasLimit().toString());
        etx.setGasPrice(tx.getGasPrice().toString());
        etx.setInput(tx.getData());
        etx.setR(Numeric.toHexStringNoPrefix(signatureData.getR()));
        etx.setS(Numeric.toHexStringNoPrefix(signatureData.getS()));
        etx.setV(bv.longValue());
        etx.setBlockNumber("-1");
        return etx;
    }

    public static Token20TransferDTO parseToken20TransferEvent(Log log) {
        try {
            String contractAddress = log.getAddress();
            List<String> topics = log.getTopics();
            String from = new Address(new BigInteger(Numeric.hexStringToByteArray(topics.get(1)))).getValue();
            String to = new Address(new BigInteger(Numeric.hexStringToByteArray(topics.get(2)))).getValue();
            BigInteger value = new BigInteger(Numeric.hexStringToByteArray(log.getData()));
            return new Token20TransferDTO(from, to, value, contractAddress);
        } catch (Exception e) {
            return null;
        }
    }

    public static List<Token20TransferDTO> parseToken20Transfer(TransactionReceipt txReceipt) {
        List<Token20TransferDTO> resultList = new ArrayList<>();
        List<Log> logs = txReceipt.getLogs();
        if (logs != null && logs.size() > 0) {
            for (Log log : logs) {
                if (log.getTopics().size() == 0) {
                    continue;
                }
                String eventHash = log.getTopics().get(0);
                if (HtgConstant.EVENT_HASH_ERC20_TRANSFER.equals(eventHash)) {
                    Token20TransferDTO dto = HtgUtil.parseToken20TransferEvent(log);
                    if (dto == null) {
                        continue;
                    }
                    resultList.add(dto);
                }
            }
        }
        return resultList;
    }
}
