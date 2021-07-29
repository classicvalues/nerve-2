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
package network.nerve.swap.model.dto;

import network.nerve.swap.model.NerveToken;

import java.math.BigInteger;

/**
 * @author: PierreLuo
 * @date: 2021/4/1
 */
public class AddLiquidityDTO {

    private byte[] fromX;
    private byte[] fromY;
    private byte[] pairAddress;
    private NerveToken tokenX;
    private NerveToken tokenY;
    private BigInteger amountX;
    private BigInteger amountY;

    public AddLiquidityDTO(byte[] fromX, byte[] fromY, byte[] pairAddress, NerveToken tokenX, NerveToken tokenY, BigInteger amountX, BigInteger amountY) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.pairAddress = pairAddress;
        this.tokenX = tokenX;
        this.tokenY = tokenY;
        this.amountX = amountX;
        this.amountY = amountY;
    }

    public byte[] getFromX() {
        return fromX;
    }

    public void setFromX(byte[] fromX) {
        this.fromX = fromX;
    }

    public byte[] getFromY() {
        return fromY;
    }

    public void setFromY(byte[] fromY) {
        this.fromY = fromY;
    }

    public byte[] getPairAddress() {
        return pairAddress;
    }

    public void setPairAddress(byte[] pairAddress) {
        this.pairAddress = pairAddress;
    }

    public NerveToken getTokenX() {
        return tokenX;
    }

    public void setTokenX(NerveToken tokenX) {
        this.tokenX = tokenX;
    }

    public NerveToken getTokenY() {
        return tokenY;
    }

    public void setTokenY(NerveToken tokenY) {
        this.tokenY = tokenY;
    }

    public BigInteger getAmountX() {
        return amountX;
    }

    public void setAmountX(BigInteger amountX) {
        this.amountX = amountX;
    }

    public BigInteger getAmountY() {
        return amountY;
    }

    public void setAmountY(BigInteger amountY) {
        this.amountY = amountY;
    }
}
