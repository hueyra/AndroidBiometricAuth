package com.github.hueyra.biometricauth.net;

import org.json.JSONException;
import org.json.JSONObject;

public class RemoteUploadPayAuthKey extends RemoteUploadAuthKeyBase {

    private static final String KEY_PAY_PWD_DIGEST = "pwdDigest";

    private String mPayPwdDigest;

    public RemoteUploadPayAuthKey(String payPwdDigest) {
        this.mPayPwdDigest = payPwdDigest;
    }

    @Override
    void setExtraJson(JSONObject requestJson) {
        if(requestJson != null) {
            try {
                requestJson.put(KEY_PAY_PWD_DIGEST, mPayPwdDigest);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
