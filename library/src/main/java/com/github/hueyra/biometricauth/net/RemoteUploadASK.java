package com.github.hueyra.biometricauth.net;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.github.hueyra.biometricauth.model.BiometricConst;
import com.github.hueyra.biometricauth.model.Util;
import com.tencent.soter.wrapper.wrap_net.ISoterNetCallback;
import com.tencent.soter.wrapper.wrap_net.IWrapUploadKeyNet;

import org.json.JSONException;
import org.json.JSONObject;

public class RemoteUploadASK extends RemoteBase implements IWrapUploadKeyNet {

    private static final String SAMPLE_ASK_JSON_PATH = BiometricConst.SAMPLE_EXTERNAL_PATH + "ask_json.txt";
    private static final String SAMPLE_ASK_SIGNATURE_PATH = BiometricConst.SAMPLE_EXTERNAL_PATH + "ask_signature.bin";
    private static final String SAMPLE_ASK_PUBLIC_KEY_PEM_PATH = BiometricConst.SAMPLE_EXTERNAL_PATH + "ask_key.pem";

    private static final String KEY_REQUEST_KEY_JSON = "keyJson";
    private static final String KEY_REQUEST_SIGNATURE = "keySignature";
    private static final String KEY_RESULT = "is_verified";

    private ISoterNetCallback<UploadResult> mCallback = null;

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
            if(resultJson == null) {
                mCallback.onNetEnd(null);
            } else {
                boolean isUploadAndVerified = resultJson.optBoolean(KEY_RESULT, false);
                mCallback.onNetEnd(new UploadResult(isUploadAndVerified));
            }
        }

    }

    @Override
    public void setRequest(@NonNull UploadRequest requestDataModel) {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put(KEY_REQUEST_KEY_JSON, requestDataModel.mKeyJson);
            requestJson.put(KEY_REQUEST_SIGNATURE, requestDataModel.mKeyJsonSignature);
            // save to file as sample. In real projects, you do not have to do it, just as a sample
            if(BiometricConst.IS_DEBUG_SAVE_DATA) {
                Util.saveTextToFile(requestDataModel.mKeyJson, SAMPLE_ASK_JSON_PATH);
                Util.saveTextToFile(retrievePublicKeyFromJson(requestDataModel.mKeyJson), SAMPLE_ASK_PUBLIC_KEY_PEM_PATH);
                Util.saveBinaryToFile(Base64.decode(requestDataModel.mKeyJsonSignature, Base64.DEFAULT), SAMPLE_ASK_SIGNATURE_PATH);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

}
