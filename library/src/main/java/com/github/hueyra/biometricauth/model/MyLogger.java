package com.github.hueyra.biometricauth.model;

import static com.github.hueyra.biometricauth.model.BiometricConst.IS_DEBUG;

import android.util.Log;

public class MyLogger {

    public static void d(String TAG, String msg) {
        if (IS_DEBUG) {
            Log.d(TAG, msg);
        }
    }

}
