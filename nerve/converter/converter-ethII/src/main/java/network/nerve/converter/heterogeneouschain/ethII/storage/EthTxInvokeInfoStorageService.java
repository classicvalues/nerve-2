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

package network.nerve.converter.heterogeneouschain.ethII.storage;

import network.nerve.converter.heterogeneouschain.eth.model.EthUnconfirmedTxPo;
import network.nerve.converter.heterogeneouschain.ethII.model.EthWaitingTxPo;

import java.util.List;

/**
 * @author: Mimi
 * @date: 2020-02-20
 */
public interface EthTxInvokeInfoStorageService {

    int save(String nerveTxHash, EthWaitingTxPo ethTxPo) throws Exception;

    EthWaitingTxPo findEthWaitingTxPo(String nerveTxHash);

    void deleteByTxHash(String nerveTxHash) throws Exception;

    boolean existNerveTxHash(String nerveTxHash);

    List<EthWaitingTxPo> findAllWaitingTxPo();

    int saveSentEthTx(String nerveTxHash) throws Exception;

    boolean ifSentEthTx(String nerveTxHash) throws Exception;

    public void deleteSentEthTx(String nerveTxHash) throws Exception;

    int saveCompletedNerveTx(String nerveTxHash) throws Exception;

    boolean ifCompletedNerveTx(String nerveTxHash) throws Exception;
}
