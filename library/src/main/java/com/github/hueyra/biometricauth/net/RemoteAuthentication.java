package com.github.hueyra.biometricauth.net;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.github.hueyra.biometricauth.model.BiometricConst;
import com.github.hueyra.biometricauth.model.Util;
import com.tencent.soter.wrapper.wrap_net.ISoterNetCallback;
import com.tencent.soter.wrapper.wrap_net.IWrapUploadSignature;

import org.json.JSONException;
import org.json.JSONObject;

public class RemoteAuthentication extends RemoteBase implements IWrapUploadSignature {

    private static final String SAMPLE_FINAL_JSON_PATH = BiometricConst.SAMPLE_EXTERNAL_PATH + "final_json.txt";
    private static final String SAMPLE_FINAL_SIGNATURE_PATH = BiometricConst.SAMPLE_EXTERNAL_PATH + "final_signature.bin";
    private static final String SAMPLE_FINAL_SALTLEN_PATH = BiometricConst.SAMPLE_EXTERNAL_PATH + "final_salt_len.txt";

    private static final String KEY_REQUEST_SIGNATURE_JSON = "signatureJson";
    private static final String KEY_REQUEST_SIGNATURE_DATA = "signature";
    private static final String KEY_REQUEST_SIGNATURE_SALT_LEN = "saltlen";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_RESULT_IS_AUTHENTICATED = "is_authenticated";

    private ISoterNetCallback<UploadSignatureResult> mFingerprintPayCallback;
    private IOnNormalPaymentCallback mNormalCallback;

    public RemoteAuthentication() {
        // fingerprint pay. do not provide password
    }

    public RemoteAuthentication(String password, IOnNormalPaymentCallback callback) {
        this.mNormalCallback = callback;
        if (Util.isNullOrNil(password)) {
            JSONObject requestJson = new JSONObject();
            try {
                requestJson.put(KEY_PASSWORD, password);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            setRequestJson(requestJson);
        }
    }

    @Override
    public void setRequest(@NonNull UploadSignatureRequest requestDataModel) {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put(KEY_REQUEST_SIGNATURE_JSON, requestDataModel.signatureJson);
            requestJson.put(KEY_REQUEST_SIGNATURE_DATA, requestDataModel.signatureData);
            requestJson.put(KEY_REQUEST_SIGNATURE_SALT_LEN, requestDataModel.signatureSaltLength);
            // save to file as sample. In real projects, you do not have to do it, just as a sample
            if(BiometricConst.IS_DEBUG_SAVE_DATA) {
                Util.saveTextToFile(requestDataModel.signatureJson, SAMPLE_FINAL_JSON_PATH);
                Util.saveBinaryToFile(Base64.decode(requestDataModel.signatureData, Base64.DEFAULT), SAMPLE_FINAL_SIGNATURE_PATH);
                Util.saveTextToFile("" + requestDataModel.signatureSaltLength, SAMPLE_FINAL_SALTLEN_PATH);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setRequestJson(requestJson);
    }

    @Override
    public void setCallback(ISoterNetCallback<UploadSignatureResult> callback) {
        this.mFingerprintPayCallback = callback;
    }

    @Override
    JSONObject getSimulateJsonResult(JSONObject requestJson) {
        JSONObject resultJson = new JSONObject();
        try {
            resultJson.put(KEY_RESULT_IS_AUTHENTICATED, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultJson;
    }

    @Override
    void onNetworkEnd(JSONObject resultJson) {
        if (resultJson != null) {
            boolean isVerified = resultJson.optBoolean(KEY_RESULT_IS_AUTHENTICATED, false);
            if(mFingerprintPayCallback != null) {
                mFingerprintPayCallback.onNetEnd(new UploadSignatureResult(isVerified));
            }
            if(mNormalCallback != null) {
                mNormalCallback.onPayEnd(isVerified);
            }
        } else {
            if(mFingerprintPayCallback != null) {
                mFingerprintPayCallback.onNetEnd(null);
            }
            if(mNormalCallback != null) {
                mNormalCallback.onPayEnd(false);
            }
        }
    }

    /**
     * Used in non-fingerprint payment
     */
    public interface IOnNormalPaymentCallback {
        void onPayEnd(boolean isSuccess);
    }

}
