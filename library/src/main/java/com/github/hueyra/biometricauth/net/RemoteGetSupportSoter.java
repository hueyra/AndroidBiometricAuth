package com.github.hueyra.biometricauth.net;

import androidx.annotation.NonNull;

import com.tencent.soter.wrapper.wrap_net.ISoterNetCallback;
import com.tencent.soter.wrapper.wrap_net.IWrapGetSupportNet;

import org.json.JSONException;
import org.json.JSONObject;

public class RemoteGetSupportSoter extends RemoteBase implements IWrapGetSupportNet {

    private static final String KEY_REQUEST_DEVICE_REQUEST_JSON = "request";
    private static final String KEY_RESULT_IS_SUPPORT = "is_support";
    private ISoterNetCallback<GetSupportResult> mCallback = null;

    @Override
    public void setRequest(@NonNull GetSupportRequest requestDataModel) {
        JSONObject request = new JSONObject();
        try {
            request.put(KEY_REQUEST_DEVICE_REQUEST_JSON, requestDataModel.requestJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setRequestJson(request);
    }

    @Override
    public void setCallback(ISoterNetCallback<GetSupportResult> callback) {
        mCallback = callback;
    }

    @Override
    JSONObject getSimulateJsonResult(JSONObject requestJson) {
        JSONObject result = new JSONObject();
        try {
            result.put(KEY_RESULT_IS_SUPPORT, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
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
                mCallback.onNetEnd(new GetSupportResult(resultJson.optBoolean(KEY_RESULT_IS_SUPPORT, false)));
            }
        }
    }

}
