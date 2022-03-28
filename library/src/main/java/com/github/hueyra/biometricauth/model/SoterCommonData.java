package com.github.hueyra.biometricauth.model;

import android.content.Context;
import android.content.SharedPreferences;

public class SoterCommonData {

    private static final String KEY_IS_SOTER_FINGERPRINT_AUTH_OPENED = "isSoterFingerprintAuthOpened";

    private static final String KEY_IS_SOTER_FACEID_AUTH_OPENED = "isSoterFaceidAuthOpened";

    private static final String KEY_IS_SYS_BIOMETRIC_AUTH_OPENED = "isSysBiometricAuthOpened";

    private static SoterCommonData sInstance = null;
    private boolean isSoterFingerprintAuthOpened = false;
    private boolean isSoterFaceidAuthOpened = false;
    private boolean isSysBiometricAuthOpened = false;

    public static SoterCommonData getInstance() {
        if (sInstance == null) {
            synchronized (SoterCommonData.class) {
                if (sInstance == null) {
                    sInstance = new SoterCommonData();
                }
                return sInstance;
            }
        } else {
            return sInstance;
        }
    }

    public void init(Context context) {
        SharedPreferences sp = context.getSharedPreferences("Soter", Context.MODE_PRIVATE);
        isSoterFingerprintAuthOpened = sp.getBoolean(KEY_IS_SOTER_FINGERPRINT_AUTH_OPENED, false);
        isSoterFaceidAuthOpened = sp.getBoolean(KEY_IS_SOTER_FACEID_AUTH_OPENED, false);
        isSysBiometricAuthOpened = sp.getBoolean(KEY_IS_SYS_BIOMETRIC_AUTH_OPENED, false);
    }

    public boolean isSoterFingerprintAuthOpened() {
        return isSoterFingerprintAuthOpened;
    }

    public void setSoterFingerprintAuthOpened(Context context, boolean soterFingerprintAuthOpened) {
        isSoterFingerprintAuthOpened = soterFingerprintAuthOpened;
        SharedPreferences sp = context.getSharedPreferences("Soter", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(KEY_IS_SOTER_FINGERPRINT_AUTH_OPENED, soterFingerprintAuthOpened).apply();
    }

    public boolean isSoterFaceidAuthOpened() {
        return isSoterFaceidAuthOpened;
    }

    public void setSoterFaceidAuthOpened(Context context, boolean soterFaceidAuthOpened) {
        isSoterFaceidAuthOpened = soterFaceidAuthOpened;
        SharedPreferences sp = context.getSharedPreferences("Soter", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(KEY_IS_SOTER_FACEID_AUTH_OPENED, soterFaceidAuthOpened).apply();
    }

    public boolean isSysBiometricAuthOpened() {
        return isSysBiometricAuthOpened;
    }

    public void setSysBiometricAuthOpened(Context context, boolean sysBiometricAuthOpened) {
        isSysBiometricAuthOpened = sysBiometricAuthOpened;
        SharedPreferences sp = context.getSharedPreferences("Soter", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(KEY_IS_SYS_BIOMETRIC_AUTH_OPENED, sysBiometricAuthOpened).apply();
    }
}
