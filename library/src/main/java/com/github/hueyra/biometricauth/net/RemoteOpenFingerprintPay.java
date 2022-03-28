package com.github.hueyra.biometricauth.net;

import androidx.annotation.NonNull;

import com.tencent.soter.wrapper.wrap_net.ISoterNetCallback;
import com.tencent.soter.wrapper.wrap_net.IWrapUploadSignature;

import org.json.JSONException;
import org.json.JSONObject;

public class RemoteOpenFingerprintPay extends RemoteBase implements IWrapUploadSignature {

    private static final String KEY_REQUEST_SIGNATURE_JSON = "signatureJson";
    private static final String KEY_REQUEST_SIGNATURE = "signature";
    private static final String KEY_REQUEST_VERIFY_SALT_LENGTH = "saltlen";
    private static final String KEY_REQUEST_PWD_DIGEST = "pwdDigest";
    private static final String KEY_RESULT_IS_OPEN_SUCCESS = "isOpenSuccess";

    private ISoterNetCallback<UploadSignatureResult> mCallback = null;
    private String mPwdDigest = null;

    public RemoteOpenFingerprintPay(String pwdDigest) {
        this.mPwdDigest = pwdDigest;
    }

    @Override
    public void setRequest(@NonNull UploadSignatureRequest requestDataModel) {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put(KEY_REQUEST_SIGNATURE_JSON, requestDataModel.signatureJson);
            requestJson.put(KEY_REQUEST_SIGNATURE, requestDataModel.signatureData);
            requestJson.put(KEY_REQUEST_VERIFY_SALT_LENGTH, requestDataModel.signatureSaltLength);
            requestJson.put(KEY_REQUEST_PWD_DIGEST, mPwdDigest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setRequestJson(requestJson);
    }

    @Override
    public void setCallback(ISoterNetCallback<UploadSignatureResult> callback) {
        this.mCallback = callback;
    }

    @Override
    public void execute() {
        super.execute();
    }

    @Override
    JSONObject getSimulateJsonResult(JSONObject requestJson) {
        JSONObject resultJson = new JSONObject();
        try {
            resultJson.put(KEY_RESULT_IS_OPEN_SUCCESS, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultJson;
    }

    @Override
    void onNetworkEnd(JSONObject resultJson) {
        if(mCallback != null) {
            if(resultJson != null) {
                boolean isOpenSuccess = resultJson.optBoolean(KEY_RESULT_IS_OPEN_SUCCESS, false);
                mCallback.onNetEnd(new UploadSignatureResult(isOpenSuccess));
            } else {
                mCallback.onNetEnd(null);
            }
        }
    }
}
