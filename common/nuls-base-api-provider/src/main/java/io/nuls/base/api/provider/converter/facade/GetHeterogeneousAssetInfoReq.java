package io.nuls.base.api.provider.converter.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2020-06-12 11:59
 * @Description: 获取异构资产信息
 */
public class GetHeterogeneousAssetInfoReq extends BaseReq {

    private int assetId;

    public GetHeterogeneousAssetInfoReq(int assetId) {
        this.assetId = assetId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }
}
