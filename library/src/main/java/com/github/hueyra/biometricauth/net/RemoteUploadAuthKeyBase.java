package com.github.hueyra.biometricauth.net;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.github.hueyra.biometricauth.model.BiometricConst;
import com.github.hueyra.biometricauth.model.Util;
import com.tencent.soter.wrapper.wrap_net.ISoterNetCallback;
import com.tencent.soter.wrapper.wrap_net.IWrapUploadKeyNet;

import org.json.JSONException;
import org.json.JSONObject;

abstract class RemoteUploadAuthKeyBase extends RemoteBase implements IWrapUploadKeyNet {

    private static final String KEY_REQUEST_KEY_JSON = "keyJson";
    private static final String KEY_REQUEST_SIGNATURE = "signature";
    private static final String KEY_RESULT = "result";

    private static final String SAMPLE_AUTH_KEY_JSON_PATH = BiometricConst.SAMPLE_EXTERNAL_PATH + "auth_key_json.txt";
    private static final String SAMPLE_AUTH_KEY_SIGNATURE_PATH = BiometricConst.SAMPLE_EXTERNAL_PATH + "auth_key_signature.bin";
    private static final String SAMPLE_AUTH_KEY_PUBLIC_KEY_PEM_PATH = BiometricConst.SAMPLE_EXTERNAL_PATH + "auth_key.pem";

    private ISoterNetCallback<UploadResult> mCallback = null;

    @Override
    public void setRequest(@NonNull UploadRequest requestDataModel) {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put(KEY_REQUEST_KEY_JSON, requestDataModel.mKeyJson);
            requestJson.put(KEY_REQUEST_SIGNATURE, requestDataModel.mKeyJsonSignature);
            // save to file as sample. In real projects, you do not have to do it, just as a sample
            if(BiometricConst.IS_DEBUG_SAVE_DATA) {
                Util.saveTextToFile(requestDataModel.mKeyJson, SAMPLE_AUTH_KEY_JSON_PATH);
                Util.saveTextToFile(retrievePublicKeyFromJson(requestDataModel.mKeyJson), SAMPLE_AUTH_KEY_PUBLIC_KEY_PEM_PATH);
                Util.saveBinaryToFile(Base64.decode(requestDataModel.mKeyJsonSignature, Base64.DEFAULT), SAMPLE_AUTH_KEY_SIGNATURE_PATH);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setExtraJson(requestJson);
        setRequestJson(requestJson);
    }

    private String retrievePublicKeyFromJson(String jsonStr) {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            return jsonObject.getString("pub_key");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setCallback(ISoterNetCallback<UploadResult> callback) {
        this.mCallback = callback;
    }

    @Override
    JSONObject getSimulateJsonResult(JSONObject requestJson) {
        JSONObject resultJson = new JSONObject();
        try {
            resultJson.put(KEY_RESULT, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultJson;
    }

    @Override
    public void execute() {
        super.execute();
    }

    @Override
    void onNetworkEnd(JSONObject resultJson) {
        if(mCallback != null) {
            mCallback.onNetEnd(new UploadResult(resultJson != null && resultJson.optBoolean(KEY_RESULT, false)));
        }
    }

    // 对于业务方来说，上传或者更新authkey很多情况下是需要带上业务信息的，如登录密码、支付密码等，因此需要额外的接口添加剩余字段
    abstract void setExtraJson(JSONObject requestJson);
}
