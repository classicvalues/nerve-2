/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
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

package io.nuls.transaction.tx;

import io.nuls.base.data.CoinData;
import io.nuls.base.data.Transaction;
import io.nuls.core.crypto.HexUtil;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.utils.OrphanSort;
import io.nuls.transaction.utils.TxUtil;
import org.junit.Test;

import java.util.*;

/**
 * @author: Charlie
 * @date: 2020/6/8
 */
public class OrphanTxSortTest {

    @Test
    public void test() throws Exception {
        for (int y = 0; y < 1; y++) {
            List<Transaction> txs = getTxs();
            System.out.println("正确的顺序");
            Map<String, Integer> indexMap = new HashMap<>();
            int index = 1;
            for (int i = 0; i < txs.size(); i++) {
                Transaction tx = txs.get(i);
                CoinData coinData = TxUtil.getCoinData(tx);
                indexMap.put(tx.getHash().toHex(), index);
                System.out.println(index++ + " 正确的顺序: " + tx.getHash().toHex() + ", nonce:" + HexUtil.encode(coinData.getFrom().get(0).getNonce()));

//                for (int x = 0; x < coinData.getFrom().size(); x++) {
//                    CoinFrom from = coinData.getFrom().get(x);
//                    System.out.println("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tfrom-"+ x +"-nonce:" + HexUtil.encode(from.getNonce()));
////                    System.out.println();
//                }
            }
            int total = txs.size();
            List<TransactionNetPO> txList = new ArrayList<>();
            List<Integer> ide = randomIde(total);
            for (int i = 0; i < total; i++) {
                txList.add(new TransactionNetPO(txs.get(ide.get(i))));
            }
            System.out.println("Size:" + txList.size());

            System.out.println("排序前");
            for (TransactionNetPO tx : txList) {
                System.out.println("排序前的顺序: " + indexMap.get(tx.getTx().getHash().toHex()));
            }
            long start = System.currentTimeMillis();

            /**
             * 排序
             */
            OrphanSort sort = new OrphanSort();
            sort.rank(txList);

            long end = System.currentTimeMillis() - start;
            System.out.println("执行时间：" + end);
            System.out.println(txList.size());
            System.out.println("排序后");
            for (int i = 0; i < txList.size(); i++) {
                TransactionNetPO tx = txList.get(i);
                String hs = txs.get(i).getHash().toHex();
//                System.out.println("排序后的顺序-(正确性): " + tx.getTx().getHash().toHex() + ", " + tx.getTx().getHash().toHex().equals(hs));
                System.out.println("排序后的顺序- " + indexMap.get(tx.getTx().getHash().toHex()));
            }

        }
    }


    public static List<Integer> randomIde(int count) {
        List<Integer> list = new ArrayList<>();
        Set<Integer> set = new HashSet<>();
        while (true) {
            Random rand = new Random();
            //随机数范取值围应为0到list最大索引之间
            //根据公式rand.nextInt((list.size() - 1) - 0 + 1) + 0;
            int ran = rand.nextInt(count);
            if (set.add(ran)) {
                list.add(ran);
            }
            if (set.size() == count) {
                break;
            }
        }
        return list;
    }

    // 顺序
    private List<Transaction> getTxs() throws Exception {

        String s1 = "02006ff3dd5e0d7472616e736665722074657374008c01170200017fe9a685e43b3124e00fd9c8e4e59158baea634502000100400d030000000000000000000000000000000000000000000000000000000000080000000000000000000117020001f7ec6473df12e751d64cf20a8baa7edd50810f8102000100a08601000000000000000000000000000000000000000000000000000000000000000000000000006a210318f683066b45e7a5225779061512e270044cc40a45c924afcf78bb7587758ca0473045022100ebbabfc6284636503db17f483df703441bfbbde3e4a45ad36377ff0d9695b18202202ecdefd6638f9746c2123d18e8f5e12f3621c9f669689439bdd87bd0e9859919";
        String s2 = "02006ff3dd5e0d7472616e736665722074657374008c01170200017fe9a685e43b3124e00fd9c8e4e59158baea634502000100400d030000000000000000000000000000000000000000000000000000000000083906e6a0b67d6f21000117020001f7ec6473df12e751d64cf20a8baa7edd50810f8102000100a08601000000000000000000000000000000000000000000000000000000000000000000000000006a210318f683066b45e7a5225779061512e270044cc40a45c924afcf78bb7587758ca0473045022100d025b0d18d7dcab681c6baf0302ca2d7f2a4676cae8ba5412068756ef12cf42702200f0000e785ea4c3d518b4c7cc39901393b5cd35c684b49db1a7f1fb7ca9aee8a";
        String s3 = "02006ff3dd5e0d7472616e736665722074657374008c01170200017fe9a685e43b3124e00fd9c8e4e59158baea634502000100400d0300000000000000000000000000000000000000000000000000000000000855018ab9a04ee9b6000117020001f7ec6473df12e751d64cf20a8baa7edd50810f8102000100a086010000000000000000000000000000000000000000000000000000000000000000000000000069210318f683066b45e7a5225779061512e270044cc40a45c924afcf78bb7587758ca04630440220084f57bc9679249446fa43ee3b9106e2632952fce10c4d8693ae6fc2149238bf0220310350bc2664edd50cd5fe60b644a1b8a548b024bdbc9b611e0ab72b710dae5b";
        String s4 = "02006ff3dd5e0d7472616e736665722074657374008c01170200017fe9a685e43b3124e00fd9c8e4e59158baea634502000100400d03000000000000000000000000000000000000000000000000000000000008f63cccc51d24dcbc000117020001f7ec6473df12e751d64cf20a8baa7edd50810f8102000100a08601000000000000000000000000000000000000000000000000000000000000000000000000006a210318f683066b45e7a5225779061512e270044cc40a45c924afcf78bb7587758ca04730450221009ce9023cd961862eb9a077f16f06063d4132c9a0cf7c49a48ee4962b5257e790022027ef8820654e06bf5f493c17ff9cc017be2c1f775a179dfaa6405f09872beb1c";
        String s5 = "02006ff3dd5e0d7472616e736665722074657374008c01170200017fe9a685e43b3124e00fd9c8e4e59158baea634502000100400d0300000000000000000000000000000000000000000000000000000000000801422bee98b22cda000117020001f7ec6473df12e751d64cf20a8baa7edd50810f8102000100a086010000000000000000000000000000000000000000000000000000000000000000000000000069210318f683066b45e7a5225779061512e270044cc40a45c924afcf78bb7587758ca0463044022079a3a3cec7480a957c4931e8907216abcd6c72f2dcbdb46c88a8eaa67c734e65022058ffa90cbe9f3d0e0c3cbce9a44660f9036a5a5742220c0dad7e84d819cb3906";
        String s6 = "02006ff3dd5e0d7472616e736665722074657374008c01170200017fe9a685e43b3124e00fd9c8e4e59158baea634502000100400d0300000000000000000000000000000000000000000000000000000000000869bf13162062203c000117020001f7ec6473df12e751d64cf20a8baa7edd50810f8102000100a08601000000000000000000000000000000000000000000000000000000000000000000000000006a210318f683066b45e7a5225779061512e270044cc40a45c924afcf78bb7587758ca047304502210084968ab02e4967a1673bac5e0af0c5090046c9a615d9e3c89fff3f9e25a7569702201bd42d418be6bd91fa13b03b492a92810d34e145d55030609dd02bd0d0724a74";
        String s7 = "02006ff3dd5e0d7472616e736665722074657374008c01170200017fe9a685e43b3124e00fd9c8e4e59158baea634502000100400d0300000000000000000000000000000000000000000000000000000000000881f846d219952c40000117020001f7ec6473df12e751d64cf20a8baa7edd50810f8102000100a086010000000000000000000000000000000000000000000000000000000000000000000000000069210318f683066b45e7a5225779061512e270044cc40a45c924afcf78bb7587758ca046304402203937e6ed141cfa336a0bb667960e5c7fb184121e660ca47f97c0c60f81756d57022068bf7cd54845c8282e6eece5d154cbf670f4dbf08c7d8e2f213d7de70e51136f";
        String s8 = "02006ff3dd5e0d7472616e736665722074657374008c01170200017fe9a685e43b3124e00fd9c8e4e59158baea634502000100400d03000000000000000000000000000000000000000000000000000000000008b5852bc6d26cc8a9000117020001f7ec6473df12e751d64cf20a8baa7edd50810f8102000100a086010000000000000000000000000000000000000000000000000000000000000000000000000069210318f683066b45e7a5225779061512e270044cc40a45c924afcf78bb7587758ca0463044022077f32245af85b0100dd72810762c3092a0b886669f6f121c3f06a36d0f82c65502204b800080473f26c967470e53f5cff00d9ca5c14d087de55793f28c70dcfb2c6b";
        String s9 = "02006ff3dd5e0d7472616e736665722074657374008c01170200017fe9a685e43b3124e00fd9c8e4e59158baea634502000100400d030000000000000000000000000000000000000000000000000000000000086b3d2e36fa1f5998000117020001f7ec6473df12e751d64cf20a8baa7edd50810f8102000100a08601000000000000000000000000000000000000000000000000000000000000000000000000006a210318f683066b45e7a5225779061512e270044cc40a45c924afcf78bb7587758ca0473045022100cfc7bbbc61b8e7cb56a44f6724d442748c43a72fa1eee09ef105a66d895a285d022043e75ec47fca01c45e46b94e13070684f73a2411c80f2397a40192e58ffb1eed";
        String s10 = "02006ff3dd5e0d7472616e736665722074657374008c01170200017fe9a685e43b3124e00fd9c8e4e59158baea634502000100400d03000000000000000000000000000000000000000000000000000000000008bafdbbd5d3e3d636000117020001f7ec6473df12e751d64cf20a8baa7edd50810f8102000100a08601000000000000000000000000000000000000000000000000000000000000000000000000006a210318f683066b45e7a5225779061512e270044cc40a45c924afcf78bb7587758ca0473045022100fe38fdf598c991941292a79cc14c51a1032e818b847180ed00576f11a2c89c9302205d74a79407fe300bcd382e05cfd8f5ba8604a031d7d1531ca3594e2842ec5876";
        String s11 = "02006ff3dd5e0d7472616e736665722074657374008c01170200017fe9a685e43b3124e00fd9c8e4e59158baea634502000100400d0300000000000000000000000000000000000000000000000000000000000820bf20c9b0b8953a000117020001f7ec6473df12e751d64cf20a8baa7edd50810f8102000100a08601000000000000000000000000000000000000000000000000000000000000000000000000006a210318f683066b45e7a5225779061512e270044cc40a45c924afcf78bb7587758ca0473045022100da42e17d56ce0f0dece0d4a8779b746f52a829e6fab5d815c322b5b97c46d20302204f832406157aba6e8fc71eb5c3835d0316524b0ec2eef4b38fea165fbacdb0c5";
        String s12 = "02006ff3dd5e0d7472616e736665722074657374008c01170200017fe9a685e43b3124e00fd9c8e4e59158baea634502000100400d03000000000000000000000000000000000000000000000000000000000008becd48a42ae7bd24000117020001f7ec6473df12e751d64cf20a8baa7edd50810f8102000100a086010000000000000000000000000000000000000000000000000000000000000000000000000069210318f683066b45e7a5225779061512e270044cc40a45c924afcf78bb7587758ca046304402201cd61c8bc12336d5c85e77f8ce256d0e7563a75ed32f724b14f2968d6e1675160220670a916cc4f7b064026267ea93204bda391283dff8ce26fa3ecfd893af97188e";
        String s13 = "02006ff3dd5e0d7472616e736665722074657374008c01170200017fe9a685e43b3124e00fd9c8e4e59158baea634502000100400d0300000000000000000000000000000000000000000000000000000000000829ea871a66723b3e000117020001f7ec6473df12e751d64cf20a8baa7edd50810f8102000100a08601000000000000000000000000000000000000000000000000000000000000000000000000006a210318f683066b45e7a5225779061512e270044cc40a45c924afcf78bb7587758ca0473045022100cba535e48b3f4dabe06ac05502c6622dc50c8c831a058ff301325b7b5355e3a602200922f19dec231cc6af644bfb00752a4ec3802a2c7d91e67d7a5e3ff028d7df85";
        String s14 = "02006ff3dd5e0d7472616e736665722074657374008c01170200017fe9a685e43b3124e00fd9c8e4e59158baea634502000100400d030000000000000000000000000000000000000000000000000000000000089461ac0cc6311a41000117020001f7ec6473df12e751d64cf20a8baa7edd50810f8102000100a08601000000000000000000000000000000000000000000000000000000000000000000000000006a210318f683066b45e7a5225779061512e270044cc40a45c924afcf78bb7587758ca0473045022100f2b89c4b25a4dce8ff41f3a5b54a20fb85418cb7ef9e20ab2416d40fd5bbf94202207eca00525116bac8b9e0d8f51d83ca51ec68fb9bf7651f82ab12588219cc3a0a";
        String s15 = "02006ff3dd5e0d7472616e736665722074657374008c01170200017fe9a685e43b3124e00fd9c8e4e59158baea634502000100400d03000000000000000000000000000000000000000000000000000000000008de4abc64dbbd6ebb000117020001f7ec6473df12e751d64cf20a8baa7edd50810f8102000100a086010000000000000000000000000000000000000000000000000000000000000000000000000069210318f683066b45e7a5225779061512e270044cc40a45c924afcf78bb7587758ca046304402203f60c05d329f2437df896fccde5cd0720b060e2707e56e7b01a90cb9af51a42902201f2f532fe7bb40373dc64f079eb38cd14ef55b57094f0649c923f23d9428d007";

        List<Transaction> list = new ArrayList<>();
        list.add(TxUtil.getInstance(s1, Transaction.class));
        list.add(TxUtil.getInstance(s2, Transaction.class));
        list.add(TxUtil.getInstance(s3, Transaction.class));
        list.add(TxUtil.getInstance(s4, Transaction.class));
        list.add(TxUtil.getInstance(s5, Transaction.class));
        list.add(TxUtil.getInstance(s6, Transaction.class));
        list.add(TxUtil.getInstance(s7, Transaction.class));
        list.add(TxUtil.getInstance(s8, Transaction.class));
        list.add(TxUtil.getInstance(s9, Transaction.class));
        list.add(TxUtil.getInstance(s10, Transaction.class));
        list.add(TxUtil.getInstance(s11, Transaction.class));
        list.add(TxUtil.getInstance(s12, Transaction.class));
        list.add(TxUtil.getInstance(s13, Transaction.class));
        list.add(TxUtil.getInstance(s14, Transaction.class));
        list.add(TxUtil.getInstance(s15, Transaction.class));
        return list;
    }

    // 乱序
    private List<Transaction> getTxs2() throws Exception {
        String s1 = "e5003a0bda5e007a5e350860a922803ea827f6f285d9375a21248088dc2625541dd9c9696f4998250400015884fa407da3005067ce4bd6d29a8e4a2af7846102605bef00000000000000000000000000000000000000000000000000000000000c32ce4a020000000000000000000000000000000000000000000000000000000000d202170400015884fa407da3005067ce4bd6d29a8e4a2af7846104000300605bef000000000000000000000000000000000000000000000000000000000008f1a6292a88c0798300170400015884fa407da3005067ce4bd6d29a8e4a2af7846104000100a086010000000000000000000000000000000000000000000000000000000000082c2d09ee348fad0d0001170400015884fa407da3005067ce4bd6d29a8e4a2af7846104000300605bef0000000000000000000000000000000000000000000000000000000000feffffffffffffff692102446e0d4d132610b9ac3546fbae0d43cd7be1c4c62c3cb18ff8dc51691d141ad846304402205fee2462aa08f9af41acc565f32e39169a6399c1cf397ed24cfb30f5a94408a6022006a675e9c758bc2cbb84c02838c5781e1a6bfeb53c256d5b50f5c0bc969d67ab";
        String s2 = "e5003a0bda5e007a5e350860a922803ea827f6f285d9375a21248088dc2625541dd9c9696f4998250400015884fa407da3005067ce4bd6d29a8e4a2af78461024878e2050000000000000000000000000000000000000000000000000000000061a86e4b020000000000000000000000000000000000000000000000000000000000d202170400015884fa407da3005067ce4bd6d29a8e4a2af78461040003004878e20500000000000000000000000000000000000000000000000000000000088a4f23768f930d8e00170400015884fa407da3005067ce4bd6d29a8e4a2af7846104000100a086010000000000000000000000000000000000000000000000000000000000088a4f23768f930d8e0001170400015884fa407da3005067ce4bd6d29a8e4a2af78461040003004878e20500000000000000000000000000000000000000000000000000000000feffffffffffffff6a2102446e0d4d132610b9ac3546fbae0d43cd7be1c4c62c3cb18ff8dc51691d141ad84730450221008e5f61bcd7a7cfc26ed4deecc7ec4ebebb1dc9a23abb6ed170b88527a9dcfdf102202b3fc7d3b640cc024a0878d17ec3f785490426cde146f2b7f9da1b78cbe565b8";
        String s3 = "e5003a0bda5e007a5e350860a922803ea827f6f285d9375a21248088dc2625541dd9c9696f4998250400015884fa407da3005067ce4bd6d29a8e4a2af7846102adbb4402000000000000000000000000000000000000000000000000000000008c90e24a020000000000000000000000000000000000000000000000000000000000d202170400015884fa407da3005067ce4bd6d29a8e4a2af7846104000300adbb44020000000000000000000000000000000000000000000000000000000008039c174f07645dae00170400015884fa407da3005067ce4bd6d29a8e4a2af7846104000100a08601000000000000000000000000000000000000000000000000000000000008039c174f07645dae0001170400015884fa407da3005067ce4bd6d29a8e4a2af7846104000300adbb440200000000000000000000000000000000000000000000000000000000feffffffffffffff692102446e0d4d132610b9ac3546fbae0d43cd7be1c4c62c3cb18ff8dc51691d141ad8463044022057a263ad9ff837ec48b3f7e3bc1ca6782a03e58ef6a88fc1888079d84dbc8db702202815853bc21e689ef169c5b55148d0ef0fa28490732a32553ed61b226b523cdd";
        String s4 = "e5003a0bda5e007a5e350860a922803ea827f6f285d9375a21248088dc2625541dd9c9696f4998250400015884fa407da3005067ce4bd6d29a8e4a2af7846101a5bfd504000000000000000000000000000000000000000000000000000000004ced9b48020000000000000000000000000000000000000000000000000000000000d202170400015884fa407da3005067ce4bd6d29a8e4a2af78461040002006bc837da0100000000000000000000000000000000000000000000000000000008aeec084ad7cf9baf00170400015884fa407da3005067ce4bd6d29a8e4a2af7846104000100a08601000000000000000000000000000000000000000000000000000000000008cd3d8febe863b4a00001170400015884fa407da3005067ce4bd6d29a8e4a2af78461040002006bc837da01000000000000000000000000000000000000000000000000000000feffffffffffffff6a2102446e0d4d132610b9ac3546fbae0d43cd7be1c4c62c3cb18ff8dc51691d141ad8473045022100c3bc906861e09f995c390a0a4f50c074174c1cd69a47f8dafdde1b874d3637a10220641c51e1c12d06c4cbe9ca39153ad5575d90c5e1f059e2e30349a3245653b087";
        String s5 = "e5003a0bda5e007a5e350860a922803ea827f6f285d9375a21248088dc2625541dd9c9696f4998250400015884fa407da3005067ce4bd6d29a8e4a2af78461014421f902000000000000000000000000000000000000000000000000000000003d949a48020000000000000000000000000000000000000000000000000000000000d202170400015884fa407da3005067ce4bd6d29a8e4a2af78461040002005ad09b230100000000000000000000000000000000000000000000000000000008974914a32660666700170400015884fa407da3005067ce4bd6d29a8e4a2af7846104000100a08601000000000000000000000000000000000000000000000000000000000008974914a3266066670001170400015884fa407da3005067ce4bd6d29a8e4a2af78461040002005ad09b2301000000000000000000000000000000000000000000000000000000feffffffffffffff6a2102446e0d4d132610b9ac3546fbae0d43cd7be1c4c62c3cb18ff8dc51691d141ad847304502210087d7ea06099a9af9a84314adb65f127a3c74e34ddfc248dc227b90b43698bbaf022026ef433d7ef58db8221edef5c7ab28d2684de02a24ebbdbcea14ab425d5a6de3";
        String s6 = "e5003a0bda5e007a5e350860a922803ea827f6f285d9375a21248088dc2625541dd9c9696f4998250400015884fa407da3005067ce4bd6d29a8e4a2af784610118357c0200000000000000000000000000000000000000000000000000000000b67d6b47020000000000000000000000000000000000000000000000000000000000d202170400015884fa407da3005067ce4bd6d29a8e4a2af7846104000200e20141f30000000000000000000000000000000000000000000000000000000008b8eed337e30758a500170400015884fa407da3005067ce4bd6d29a8e4a2af7846104000100a08601000000000000000000000000000000000000000000000000000000000008b8eed337e30758a50001170400015884fa407da3005067ce4bd6d29a8e4a2af7846104000200e20141f300000000000000000000000000000000000000000000000000000000feffffffffffffff6a2102446e0d4d132610b9ac3546fbae0d43cd7be1c4c62c3cb18ff8dc51691d141ad8473045022100bbc69287f6098f10d1101933af5c8b780422f647b22ae0fc9b333d072e46ace902205c4aab1ce65896e67b15e9000d675905df1aea09b571da8cca3801132784f9b4";
        String s7 = "e5003a0bda5e007a35036ba33ea420bdd45d9737ea946d326a40b12d88ee1c5305fedc91b3a6c4460400015884fa407da3005067ce4bd6d29a8e4a2af7846101f618c70400000000000000000000000000000000000000000000000000000000ba020500000000000000000000000000000000000000000000000000000000000000d202170400015884fa407da3005067ce4bd6d29a8e4a2af78461040002003204040000000000000000000000000000000000000000000000000000000000084bb6260ec2926e5e00170400015884fa407da3005067ce4bd6d29a8e4a2af7846104000100a08601000000000000000000000000000000000000000000000000000000000008727dd8ef5e8d4bb80001170400015884fa407da3005067ce4bd6d29a8e4a2af78461040002003204040000000000000000000000000000000000000000000000000000000000feffffffffffffff6a2102446e0d4d132610b9ac3546fbae0d43cd7be1c4c62c3cb18ff8dc51691d141ad8473045022100c47c20f13f09da178d47fb9916960956a4bad86574a3c3fd084dd33e89baab4a02200a127200ca99513f6db8907bf20a742a20fe740c7c28cbd872e76aa4c815f3a8";
        String s8 = "e5003a0bda5e007a35036ba33ea420bdd45d9737ea946d326a40b12d88ee1c5305fedc91b3a6c4460400015884fa407da3005067ce4bd6d29a8e4a2af78461019e2e6e0000000000000000000000000000000000000000000000000000000000f7020500000000000000000000000000000000000000000000000000000000000000d202170400015884fa407da3005067ce4bd6d29a8e4a2af7846104000200a55c000000000000000000000000000000000000000000000000000000000000087fc97024f0f4bd8000170400015884fa407da3005067ce4bd6d29a8e4a2af7846104000100a086010000000000000000000000000000000000000000000000000000000000087fc97024f0f4bd800001170400015884fa407da3005067ce4bd6d29a8e4a2af7846104000200a55c000000000000000000000000000000000000000000000000000000000000feffffffffffffff6a2102446e0d4d132610b9ac3546fbae0d43cd7be1c4c62c3cb18ff8dc51691d141ad8473045022100b5fb6547754ffde0ab950026e5f3e1bf6169c183ba8effe23ef026d10301e607022038653392075a711a83ca275d368dc8754a48f69efcd9542e6bcfb7cbfdd57acc";
        String s9 = "e6003a0bda5e002062fbc9feece12155f9149918df782ddafccf72ed129f63e59982e331a50feaab8c01170400015884fa407da3005067ce4bd6d29a8e4a2af7846104000100a086010000000000000000000000000000000000000000000000000000000000084bb6260ec2926e5e0001170400015884fa407da3005067ce4bd6d29a8e4a2af784610400010000000000000000000000000000000000000000000000000000000000000000000000000000000000692102446e0d4d132610b9ac3546fbae0d43cd7be1c4c62c3cb18ff8dc51691d141ad8463044022013f9667c05445d9a6339fd28859b2982246a1c47a2925f8b3ce6322ca301626302200150f6c2f16ed658d2562bf74d85e3b949944713a43f0038f6aa1baf89e80e0a";
        String s10 = "e6003a0bda5e0020b6304973e60199ccd13a574814c42c567b1674d1f623663f7690138a87199fb48c01170400015884fa407da3005067ce4bd6d29a8e4a2af7846104000100a08601000000000000000000000000000000000000000000000000000000000008e1f16dd5931cb00c0001170400015884fa407da3005067ce4bd6d29a8e4a2af784610400010000000000000000000000000000000000000000000000000000000000000000000000000000000000692102446e0d4d132610b9ac3546fbae0d43cd7be1c4c62c3cb18ff8dc51691d141ad84630440220785903f6b5b3be54cded776bd6037771041a8dd3586b07b38c7c6fd6fbb28cbe022008387aeae9ab9161fae0bfff8724edb6bfaadda7671bf990863776402e2316d0";
        String s11 = "e5003a0bda5e007a35036ba33ea420bdd45d9737ea946d326a40b12d88ee1c5305fedc91b3a6c4460400015884fa407da3005067ce4bd6d29a8e4a2af7846102aded6e01000000000000000000000000000000000000000000000000000000008b060500000000000000000000000000000000000000000000000000000000000000d202170400015884fa407da3005067ce4bd6d29a8e4a2af7846102000100aded6e010000000000000000000000000000000000000000000000000000000008f4dd41e4ce147c4d00170400015884fa407da3005067ce4bd6d29a8e4a2af7846104000100a0860100000000000000000000000000000000000000000000000000000000000883ab7af34a9ea2030001170400015884fa407da3005067ce4bd6d29a8e4a2af7846102000100aded6e0100000000000000000000000000000000000000000000000000000000feffffffffffffff692102446e0d4d132610b9ac3546fbae0d43cd7be1c4c62c3cb18ff8dc51691d141ad846304402206f10db6433318beebba4a878005326a4bbdbf3901d574bc6aebac93cb18516f8022036fdc8dcfc55fbb5343d0b9964e1240d8286be28cef54430bde825a3b4027651";
        String s12 = "e5003a0bda5e007a35036ba33ea420bdd45d9737ea946d326a40b12d88ee1c5305fedc91b3a6c4460400015884fa407da3005067ce4bd6d29a8e4a2af784610255b19405000000000000000000000000000000000000000000000000000000002c080500000000000000000000000000000000000000000000000000000000000000d202170400015884fa407da3005067ce4bd6d29a8e4a2af784610200010055b19405000000000000000000000000000000000000000000000000000000000886f29cf08f9be19100170400015884fa407da3005067ce4bd6d29a8e4a2af7846104000100a0860100000000000000000000000000000000000000000000000000000000000886f29cf08f9be1910001170400015884fa407da3005067ce4bd6d29a8e4a2af784610200010055b1940500000000000000000000000000000000000000000000000000000000feffffffffffffff692102446e0d4d132610b9ac3546fbae0d43cd7be1c4c62c3cb18ff8dc51691d141ad846304402205f8fff1d863e241ac3ac5269cd135c1897fef09d0747a547f4b46f8b7fe64b36022047883add6e704b44de84c142c9b9907f48344ee2cb057ea98559d9acdea5a939";
        String s13 = "e5003a0bda5e007a35036ba33ea420bdd45d9737ea946d326a40b12d88ee1c5305fedc91b3a6c4460400015884fa407da3005067ce4bd6d29a8e4a2af7846102342a7e0100000000000000000000000000000000000000000000000000000000de080500000000000000000000000000000000000000000000000000000000000000d202170400015884fa407da3005067ce4bd6d29a8e4a2af7846102000100342a7e0100000000000000000000000000000000000000000000000000000000089c7598800faf73a200170400015884fa407da3005067ce4bd6d29a8e4a2af7846104000100a086010000000000000000000000000000000000000000000000000000000000089c7598800faf73a20001170400015884fa407da3005067ce4bd6d29a8e4a2af7846102000100342a7e0100000000000000000000000000000000000000000000000000000000feffffffffffffff6a2102446e0d4d132610b9ac3546fbae0d43cd7be1c4c62c3cb18ff8dc51691d141ad8473045022100989bad2d93a6b3f9538ed0656e70437330dbdac0a0c14b9425a806c9c3a9d16b0220052e7fa2508ccab60b79ce2158641ff295c7b88d74e4c893621a7eee4dc5f9d0";
        List<Transaction> list = new ArrayList<>();
        list.add(TxUtil.getInstance(s1, Transaction.class));
        list.add(TxUtil.getInstance(s2, Transaction.class));
        list.add(TxUtil.getInstance(s3, Transaction.class));
        list.add(TxUtil.getInstance(s4, Transaction.class));
        list.add(TxUtil.getInstance(s5, Transaction.class));
        list.add(TxUtil.getInstance(s6, Transaction.class));
        list.add(TxUtil.getInstance(s7, Transaction.class));
        list.add(TxUtil.getInstance(s8, Transaction.class));
        list.add(TxUtil.getInstance(s9, Transaction.class));
        list.add(TxUtil.getInstance(s10, Transaction.class));
        list.add(TxUtil.getInstance(s11, Transaction.class));
        list.add(TxUtil.getInstance(s12, Transaction.class));
        list.add(TxUtil.getInstance(s13, Transaction.class));
        return list;
    }
}
