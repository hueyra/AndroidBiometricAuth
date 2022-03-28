package com.github.hueyra.biometricauth.model;

import com.tencent.soter.wrapper.wrap_core.SoterProcessErrCode;

/**
 * Created by zhujun.
 * Date : 2021/08/14
 * Desc : __
 */
public interface BiometricAuthCallback {
    /**
     * 显示加载，Soter
     *
     * @param msg 消息
     */
    void onShowLoading(String msg);

    /**
     * 隐藏加载，Soter
     */
    void onHideLoading();

    /**
     * 打开识别认证的对话框，Soter使用
     *
     * @param biometricType 类别
     */
    void onShowAuthDialog(int biometricType);

    /**
     * 隐藏识别认证的对话框，Soter使用
     *
     * @param isSuccess 是否成功
     */
    void onHideAuthDialog(boolean isSuccess);

    /**
     * 再试一次
     *
     * @param biometricType 类别
     */
    void onNotifyAuthDialogOnceAgain(int biometricType);

    /**
     * 开始识别
     */
    void onAuthenticationStart();

    /**
     * 识别成功
     */
    void onAuthenticationSucceeded();

    /**
     * 系统生物识别出错
     *
     * @param biometricType 类别
     * @param err           错误码
     * @param msg           错误信息
     */
    void onAuthenticationError(int biometricType, int err, String msg);

    /**
     * 系统生物识别失败
     *
     * @param biometricType 类别
     */
    void onAuthenticationFailed(int biometricType);

    /**
     * 系统生物识别取消
     *
     * @param biometricType 类别
     */
    void onAuthenticationCancelled(int biometricType);

    /**
     * Soter执行出错，需要处理的是:
     * {@link SoterProcessErrCode.ERR_AUTHKEY_NOT_FOUND};
     * {@link SoterProcessErrCode.ERR_AUTHKEY_ALREADY_EXPIRED};
     * {@link SoterProcessErrCode.ERR_ASK_NOT_EXIST};
     * {@link SoterProcessErrCode.ERR_SIGNATURE_INVALID};
     * <p>
     * 仅当这些情况 error_msg会返回「签名错误」，此时需要重新准备AuthKey
     *
     * @param err {@link SoterProcessErrCode}
     * @param msg 错误信息
     */
    void onAuthenticationSoterError(int err, String msg);

    /**
     * Soter识别成功
     *
     * @param biometricType 类型
     */
    void onOpenSoterAuthSucceeded(int biometricType);

    /**
     * Soter识别失败
     *
     * @param biometricType 类型
     */
    void onOpenSoterAuthFailed(int biometricType);

    /**
     * 打开系统生物识别成功
     */
    void onOpenSysAuthSucceeded();

    /**
     * 打开系统生物识别失败
     */
    void onOpenSysAuthFailed();
}
