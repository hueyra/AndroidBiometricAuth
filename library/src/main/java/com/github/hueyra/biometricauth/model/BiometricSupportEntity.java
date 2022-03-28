package com.github.hueyra.biometricauth.model;

import androidx.annotation.NonNull;

/**
 * Created by zhujun.
 * Date : 2021/08/17
 * Desc : 生物识别支持的情况
 */
public class BiometricSupportEntity {

    // 初始化是否成功
    private boolean initSuccess;
    // 系统biometric是否支持
    private boolean biometricSupport;
    // 指纹识别是否支持，使用soter检查
    private boolean fingerprintAuthSupport;
    // 人脸识别是否支持，使用soter检查
    private boolean faceAuthSupport;

    public boolean isInitSuccess() {
        return initSuccess;
    }

    public void setInitSuccess(boolean initSuccess) {
        this.initSuccess = initSuccess;
    }

    public boolean isBiometricSupport() {
        return biometricSupport;
    }

    public void setBiometricSupport(boolean biometricSupport) {
        this.biometricSupport = biometricSupport;
    }

    public boolean isFingerprintAuthSupport() {
        return fingerprintAuthSupport;
    }

    public void setFingerprintAuthSupport(boolean fingerprintAuthSupport) {
        this.fingerprintAuthSupport = fingerprintAuthSupport;
    }

    public boolean isFaceAuthSupport() {
        return faceAuthSupport;
    }

    public void setFaceAuthSupport(boolean faceAuthSupport) {
        this.faceAuthSupport = faceAuthSupport;
    }

    @NonNull
    @Override
    public String toString() {
        return "Biometric -> {" +
                "initSuccess=" + initSuccess +
                ", biometricSupport=" + biometricSupport +
                ", fingerprintAuthSupport=" + fingerprintAuthSupport +
                ", faceAuthSupport=" + faceAuthSupport +
                '}';
    }
}
