package com.github.hueyra.biometricauth.net;

import org.json.JSONObject;

/**
 * Created by zhujun
 * 网络封装结构体。注意，真实项目中，请使用真正的网络实现
 */
abstract class RemoteBase {

    private JSONObject mRequestJson = null;
    private JSONObject mResultJson = null;

    public void execute() {
        JSONObject result = getSimulateJsonResult(mRequestJson);
        if (result == null) {
            mResultJson = null;
        } else {
            mResultJson = getSimulateJsonResult(mRequestJson);
        }
        onNetworkEnd(mResultJson);
    }

    protected void setRequestJson(JSONObject requestJson) {
        if (requestJson != null) {
            mRequestJson = requestJson;
        } else {
            mRequestJson = null;
        }
    }

    // 目前使用的模拟网络回包模式，因此需要每一个网络类型指定本次请求的模拟回包。真正的项目中，请使用真实的网络回包数据
    abstract JSONObject getSimulateJsonResult(JSONObject requestJson);

    abstract void onNetworkEnd(JSONObject resultJson);

}
