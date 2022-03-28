package com.github.hueyra.biometricauth.model;

import com.github.hueyra.biometricauth.BuildConfig;

public class BiometricConst {

    public static final String AUTH_KEY_ERROR = "签名错误";

    public static final int SCENE_FINGERPRINT = 1;

    public static final int SCENE_FACEID = 2;

    public static final boolean IS_DEBUG_SAVE_DATA = false;

    public static String SAMPLE_EXTERNAL_PATH = "";

    public static boolean IS_DEBUG = BuildConfig.DEBUG;
}
