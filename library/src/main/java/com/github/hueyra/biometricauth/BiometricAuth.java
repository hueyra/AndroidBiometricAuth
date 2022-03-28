package com.github.hueyra.biometricauth;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.github.hueyra.biometricauth.model.BiometricAuthCallback;
import com.github.hueyra.biometricauth.model.BiometricConst;
import com.github.hueyra.biometricauth.model.BiometricSupportEntity;
import com.github.hueyra.biometricauth.model.CheckResult;
import com.github.hueyra.biometricauth.model.MyLogger;
import com.github.hueyra.biometricauth.model.SoterCommonData;
import com.github.hueyra.biometricauth.net.RemoteAuthentication;
import com.github.hueyra.biometricauth.net.RemoteGetSupportSoter;
import com.github.hueyra.biometricauth.net.RemoteOpenFingerprintPay;
import com.github.hueyra.biometricauth.net.RemoteUploadASK;
import com.github.hueyra.biometricauth.net.RemoteUploadPayAuthKey;
import com.tencent.soter.core.SoterCore;
import com.tencent.soter.core.model.ConstantsSoter;
import com.tencent.soter.wrapper.SoterWrapperApi;
import com.tencent.soter.wrapper.wrap_biometric.SoterBiometricCanceller;
import com.tencent.soter.wrapper.wrap_biometric.SoterBiometricStateCallback;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessAuthenticationResult;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessCallback;
import com.tencent.soter.wrapper.wrap_core.SoterProcessErrCode;
import com.tencent.soter.wrapper.wrap_net.IWrapUploadSignature;
import com.tencent.soter.wrapper.wrap_task.AuthenticationParam;
import com.tencent.soter.wrapper.wrap_task.InitializeParam;

import java.io.File;

/**
 * Created by zhujun.
 * Date : 2021/08/14
 * Desc : 生物识别统一接口
 */
public class BiometricAuth {

    private final static String TAG = BiometricAuth.class.getSimpleName();

    private static final String SOTER_PWD_DIGEST = "soter";

    //生物识别认证回调接口
    private BiometricAuthCallback mBiometricAuthCallback;

    //系统识别对话框的title
    private String mPromptTitle;
    //系统识别对话框的子title
    private String mPromptSubTitle;

    //soter认证取消
    private SoterBiometricCanceller mSoterBiometricCanceller = null;

    //单例对象
    private volatile static BiometricAuth instance;

    //识别支持的实体
    private BiometricSupportEntity mBiometricSupportEntity;

    private BiometricAuth() {
    }

    public static BiometricAuth getInstance() {
        if (instance == null) {
            synchronized (BiometricAuth.class) {
                if (instance == null) {
                    instance = new BiometricAuth();
                }
            }
        }
        return instance;
    }

    public void setDebug(boolean debug) {
        BiometricConst.IS_DEBUG = debug;
    }

    public void init(Context context) {
        //初始化
        BiometricConst.SAMPLE_EXTERNAL_PATH = context.getExternalFilesDir(null).getAbsolutePath()
                + File.separator + "Sotor" + File.separator;
        SoterCommonData.getInstance().init(context);
        InitializeParam param = new InitializeParam.InitializeParamBuilder()
                .setGetSupportNetWrapper(new RemoteGetSupportSoter())
                .setScenes(BiometricConst.SCENE_FINGERPRINT, BiometricConst.SCENE_FACEID)
                .build();
        mBiometricSupportEntity = new BiometricSupportEntity();
        //soter 初始化
        SoterWrapperApi.init(context, result -> {
            MyLogger.d(TAG, "SoterWrapperApi init -> " + result.isSuccess());
            mBiometricSupportEntity.setInitSuccess(result.isSuccess());
            checkBiometricSupport(context);
        }, param);
    }

    /**
     * 检查生物识别支持情况
     *
     * @param context 上下文对象
     */
    public BiometricAuth checkBiometricSupport(Context context) {
        canAuthenticate(context);
        isSupportFingerprintAuth(context);
        isSupportFaceAuth(context);
        return this;
    }

    /**
     * 获取生物识别支持情况
     *
     * @return {@link BiometricSupportEntity} 生物识别支持
     */
    public BiometricSupportEntity getBiometricSupportInfo() {
        return mBiometricSupportEntity;
    }

    /**
     * 设置对话框的title
     *
     * @param title 对话框的标题
     */
    public BiometricAuth setPromptTitle(String title) {
        this.mPromptTitle = title;
        return this;
    }

    /**
     * 设置对话框的子title
     *
     * @param subTitle 对话框的子标题
     */
    public BiometricAuth setPromptSubTitle(String subTitle) {
        this.mPromptSubTitle = subTitle;
        return this;
    }

    /**
     * 设置生物识别的认证回调
     *
     * @param callback 回调
     */
    public BiometricAuth setBiometricAuthCallback(BiometricAuthCallback callback) {
        this.mBiometricAuthCallback = callback;
        return this;
    }

    /**
     * 取消生物识别
     */
    public BiometricAuth cancelAuth() {
        if (mSoterBiometricCanceller != null) {
            mSoterBiometricCanceller.asyncCancelBiometricAuthentication();
            mSoterBiometricCanceller = null;
        }
        return this;
    }

    public void releaseAll() {
        this.mPromptTitle = "";
        this.mPromptSubTitle = "";
        this.mBiometricAuthCallback = null;
    }

    /**
     * 在application的onTerminate中回调此方法，用于内存不足
     */
    public void onTerminate() {
        SoterWrapperApi.tryStopAllSoterTask();
    }

    /**
     * 使用BiometricManager去检查一下生物识别API支持情况
     *
     * @param context 上下文对象
     * @return {@link CheckResult} 检查结果
     */
    public CheckResult canAuthenticate(@NonNull Context context) {
        int result = BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG);
        mBiometricSupportEntity.setBiometricSupport(false);
        switch (result) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                mBiometricSupportEntity.setBiometricSupport(true);
                return new CheckResult(true, result, "可以进行生物识别技术进行身份验证");
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                return new CheckResult(false, result, "无法获取生物识别状态");
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                return new CheckResult(false, result, "无法使用生物识别，没有搭载可用的生物识别功能");
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                return new CheckResult(false, result, "无法使用生物识别，生物识别功能当前不可用");
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return new CheckResult(false, result, "无法使用生物识别，没有录入生物识别数据");
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                return new CheckResult(false, result, "无法使用生物识别，存在未知错误");
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                return new CheckResult(false, result, "无法使用生物识别，当前系统版本不支持");
        }
        return new CheckResult(false, result, "无法使用生物识别");
    }

    /**
     * 使用系统API去查看一下设备硬件支持情况
     *
     * @param context 上下文对象
     * @param feature 功能，fingerprint/face
     * @param minSDK  最低API版本
     * @return {@link CheckResult} 检查结果
     */
    private CheckResult hasSystemFeature(@NonNull Context context, String feature, int minSDK) {
        String biometricName = "";
        if (feature.contains("fingerprint")) {
            biometricName = "指纹识别";
        } else if (feature.contains("face")) {
            biometricName = "面容识别";
        }
        if (Build.VERSION.SDK_INT >= minSDK) {
            boolean b = context.getPackageManager().hasSystemFeature(feature);
            String msg;
            if (b) {
                msg = "此设备支持" + biometricName;
            } else {
                msg = "此设备不支持" + biometricName;
            }
            return new CheckResult(b, msg);
        } else {
            return new CheckResult(false, "此设备不支持" + biometricName);
        }
    }

    /**
     * 使用系统API去查看一下设备指纹硬件支持情况
     *
     * @param context 上下文对象
     * @return {@link CheckResult} 检查结果
     */
    public CheckResult hasFingerprintFeature(@NonNull Context context) {
        return hasSystemFeature(context, PackageManager.FEATURE_FINGERPRINT, 23);
    }

    /**
     * 使用系统API去查看一下设备人脸硬件支持情况
     *
     * @param context 上下文对象
     * @return {@link CheckResult} 检查结果
     */
    public CheckResult hasFaceFeature(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= 29) {
            return hasSystemFeature(context, PackageManager.FEATURE_FACE, 29);
        } else {
            return new CheckResult(false, "此设备不支持面容识别");
        }
    }

    /**
     * 使用SoterAPI去查看一下设备指纹硬件支持情况
     *
     * @param context 上下文对象
     * @return {@link CheckResult} 检查结果
     */
    public CheckResult isSupportFingerprintAuth(@NonNull Context context) {
        boolean isSupport = SoterCore.isSupportBiometric(context, ConstantsSoter.FINGERPRINT_AUTH);
        mBiometricSupportEntity.setFingerprintAuthSupport(isSupport);
        return new CheckResult(isSupport, isSupport ? "此设备支持指纹识别" : "此设备不支持指纹识别");
    }

    /**
     * 使用SoterAPI去查看一下设备人脸硬件支持情况
     *
     * @param context 上下文对象
     * @return {@link CheckResult} 检查结果
     */
    public CheckResult isSupportFaceAuth(@NonNull Context context) {
        boolean isSupport = SoterCore.isSupportBiometric(context, ConstantsSoter.FACEID_AUTH);
        mBiometricSupportEntity.setFaceAuthSupport(isSupport);
        return new CheckResult(isSupport, isSupport ? "此设备支持面容识别" : "此设备不支持面容识别");
    }

    /**
     * 是否打开了系统的生物识别
     *
     * @return Boolean 是否打开
     */
    public boolean isOpenSysBiometricAuth() {
        return SoterCommonData.getInstance().isSysBiometricAuthOpened();
    }

    /**
     * 是否打开了Soter指纹识别
     *
     * @return Boolean 是否打开
     */
    public boolean isOpenSoterFingerprintAuth() {
        return SoterCommonData.getInstance().isSoterFingerprintAuthOpened();
    }

    /**
     * 是否打开了Soter人脸识别
     *
     * @return Boolean 是否打开
     */
    public boolean isOpenSoterFaceAuth() {
        return SoterCommonData.getInstance().isSoterFaceidAuthOpened();
    }

    /**
     * 准备Soter认证的AuthKey
     *
     * @param callback      回调
     * @param biometricType 类别
     */
    private void prepareSoterAuthKey(IOnAuthKeyPrepared callback, int biometricType) {
        if (mBiometricAuthCallback != null) {
            mBiometricAuthCallback.onShowLoading("正在处理");
        }
        int scene = biometricType == ConstantsSoter.FINGERPRINT_AUTH
                ? BiometricConst.SCENE_FINGERPRINT : BiometricConst.SCENE_FACEID;
        //调用soter api准备
        SoterWrapperApi.prepareAuthKey(result -> {
            if (result.errCode == SoterProcessErrCode.ERR_OK) {
                if (callback != null) {
                    callback.onResult(true);
                }
            } else {
                if (callback != null) {
                    callback.onResult(false);
                }
            }
        }, false, true, scene, new RemoteUploadPayAuthKey(SOTER_PWD_DIGEST), new RemoteUploadASK());

    }

    /**
     * 打开Soter生物识别认证功能
     *
     * @param context       上下文对象
     * @param biometricType 类别
     */
    private void openSoterBiometricAuth(Context context, final int biometricType) {
        //首先需要准备soter的authKey
        prepareSoterAuthKey(isSuccess -> {
            if (mBiometricAuthCallback != null) {
                mBiometricAuthCallback.onHideLoading();
            }
            //如果准备成功了
            if (isSuccess) {
                //这个时候就可以使用生物识别了，此时让用户验证生物信息
                authenticateWithSoter(context, result -> {
                    //验证成功就保存打开的信息
                    if (result.isSuccess()) {
                        if (biometricType == ConstantsSoter.FINGERPRINT_AUTH) {
                            SoterCommonData.getInstance().setSoterFingerprintAuthOpened(context, true);
                        } else {
                            SoterCommonData.getInstance().setSoterFaceidAuthOpened(context, true);
                        }
                        //
                        if (mBiometricAuthCallback != null) {
                            mBiometricAuthCallback.onOpenSoterAuthSucceeded(biometricType);
                        }
                    } else {
                        if (mBiometricAuthCallback != null) {
                            mBiometricAuthCallback.onOpenSoterAuthFailed(biometricType);
                        }
                    }
                }, new RemoteOpenFingerprintPay(SOTER_PWD_DIGEST), biometricType);
            } else {
                if (mBiometricAuthCallback != null) {
                    mBiometricAuthCallback.onOpenSoterAuthFailed(biometricType);
                }
            }
        }, biometricType);
    }

    /**
     * 打开Soter指纹识别认证功能
     *
     * @param context 上下文对象
     */
    public void openSoterFingerprintAuth(Context context) {
        openSoterBiometricAuth(context, ConstantsSoter.FINGERPRINT_AUTH);
    }

    /**
     * 打开Soter人脸识别认证功能
     *
     * @param context 上下文对象
     */
    public void openSoterFaceAuth(Context context) {
        openSoterBiometricAuth(context, ConstantsSoter.FACEID_AUTH);
    }

    /**
     * 打开系统生物识别认证功能
     *
     * @param context 上下文对象
     */
    public void openSysBiometricAuth(@NonNull AppCompatActivity context) {
        //系统的不需要准备authKey，就直接调用认证API,让用户验证，通过就打开
        authenticateWithAndroidSystem(context, isSuccess -> {
            if (mBiometricAuthCallback != null) {
                if (isSuccess) {
                    SoterCommonData.getInstance().setSysBiometricAuthOpened(context, true);
                    mBiometricAuthCallback.onOpenSysAuthSucceeded();
                } else {
                    mBiometricAuthCallback.onOpenSysAuthFailed();
                }
            }

        });
    }

    /**
     * 关闭指纹认证
     */
    public void closeSoterFingerprintAuth(Context context) {
        SoterWrapperApi.removeAuthKeyByScene(BiometricConst.SCENE_FINGERPRINT);
        SoterCommonData.getInstance().setSoterFingerprintAuthOpened(context, false);
    }

    /**
     * 关闭人脸认证
     */
    public void closeSoterFaceAuth(Context context) {
        SoterWrapperApi.removeAuthKeyByScene(BiometricConst.SCENE_FACEID);
        SoterCommonData.getInstance().setSoterFaceidAuthOpened(context, false);
    }

    /**
     * 关闭系统认证
     */
    public void closeSysBiometricAuth(Context context) {
        SoterCommonData.getInstance().setSysBiometricAuthOpened(context, false);
    }

    /**
     * 使用Android系统API进行生物识别
     *
     * @param context activity
     */
    public void authenticateWithAndroidSystem(@NonNull AppCompatActivity context) {
        authenticateWithAndroidSystem(context, null);
    }

    /**
     * 使用Android系统API进行生物识别
     *
     * @param context  activity
     * @param callback callback
     */
    private void authenticateWithAndroidSystem(@NonNull AppCompatActivity context, IOnAuthKeyPrepared callback) {

        //创建BiometricPrompt
        BiometricPrompt biometricPrompt = new BiometricPrompt(context,
                ContextCompat.getMainExecutor(context), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (callback != null) {
                    callback.onResult(false);
                } else {
                    if (mBiometricAuthCallback != null) {
                        mBiometricAuthCallback.onAuthenticationError(0, errorCode, errString.toString());
                    }
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                if (callback != null) {
                    callback.onResult(true);
                } else {
                    if (mBiometricAuthCallback != null) {
                        mBiometricAuthCallback.onAuthenticationSucceeded();
                    }
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                if (callback != null) {
                    callback.onResult(false);
                } else {
                    if (mBiometricAuthCallback != null) {
                        mBiometricAuthCallback.onAuthenticationFailed(0);
                    }
                }
            }
        });
        //设置参数
        BiometricPrompt.PromptInfo sysPromptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(TextUtils.isEmpty(mPromptTitle) ? "生物识别" : mPromptTitle)
                .setSubtitle(TextUtils.isEmpty(mPromptSubTitle) ? "使用您的生物识别凭据验证" : mPromptSubTitle)
                .setNegativeButtonText("取消")
                .setAllowedAuthenticators(BIOMETRIC_STRONG)
                .build();
        //验证
        biometricPrompt.authenticate(sysPromptInfo);
    }

    /**
     * 使用SoterAPI进行指纹识别
     *
     * @param context activity
     */
    public void authenticateWithSoterFingerprint(Context context) {
        authenticateWithSoter(context, result -> {
            MyLogger.d(TAG, "authenticateWithSoter FINGERPRINT -> " + result.isSuccess());
            if (!result.isSuccess()) {
                if (mBiometricAuthCallback != null) {
                    mBiometricAuthCallback.onAuthenticationSoterError(result.errCode,
                            getSoterAuthResultErrorMsg(context, ConstantsSoter.FINGERPRINT_AUTH, result));
                }
            }
        }, new RemoteAuthentication(), ConstantsSoter.FINGERPRINT_AUTH);
    }

    /**
     * 使用SoterAPI进行人脸识别
     *
     * @param context activity
     */
    public void authenticateWithSoterFace(Context context) {
        authenticateWithSoter(context, result -> {
            if (!result.isSuccess()) {
                if (mBiometricAuthCallback != null) {
                    mBiometricAuthCallback.onAuthenticationSoterError(result.errCode,
                            getSoterAuthResultErrorMsg(context, ConstantsSoter.FACEID_AUTH, result));
                }
            }
        }, new RemoteAuthentication(), ConstantsSoter.FACEID_AUTH);
    }

    /**
     * 获取Soter认证结果的错误信息
     *
     * @param type   类别
     * @param result 结果
     */
    private String getSoterAuthResultErrorMsg(Context context, int type, SoterProcessAuthenticationResult result) {
        String str = ConstantsSoter.FINGERPRINT_AUTH == type ? "无法使用指纹识别，" : "无法使用面容识别，";
        if (result.errCode == SoterProcessErrCode.ERR_AUTHKEY_NOT_FOUND
                || result.errCode == SoterProcessErrCode.ERR_AUTHKEY_ALREADY_EXPIRED
                || result.errCode == SoterProcessErrCode.ERR_ASK_NOT_EXIST
                || result.errCode == SoterProcessErrCode.ERR_SIGNATURE_INVALID) {
            //签名错误，直接把这个关了重新打开
            if (ConstantsSoter.FINGERPRINT_AUTH == type) {
                closeSoterFingerprintAuth(context);
            } else {
                closeSoterFaceAuth(context);
            }
            return str + "AuthKey或ASK" + BiometricConst.AUTH_KEY_ERROR;
        } else if (result.errCode == SoterProcessErrCode.ERR_USER_CANCELLED) {
            return str + "用户取消";
        } else if (result.errCode == SoterProcessErrCode.ERR_FINGERPRINT_LOCKED ||
                result.errCode == SoterProcessErrCode.ERR_BIOMETRIC_LOCKED) {
            return str + "传感器被锁定";
        } else if (result.errCode == SoterProcessErrCode.ERR_NO_BIOMETRIC_ENROLLED ||
                result.errCode == SoterProcessErrCode.ERR_NO_FINGERPRINT_ENROLLED) {
            return str + "没有录入生物识别数据";
        } else {
            return str + "其他错误";
        }
    }

    /**
     * 真正的SoterAPI 认证
     *
     * @param context                上下文对象
     * @param processCallback        回调
     * @param uploadSignatureWrapper 更新签名的工具
     * @param biometricType          类别
     */
    private void authenticateWithSoter(
            Context context,
            SoterProcessCallback<SoterProcessAuthenticationResult> processCallback,
            IWrapUploadSignature uploadSignatureWrapper,
            final int biometricType) {
        if (mSoterBiometricCanceller != null) {
            mSoterBiometricCanceller = null;
        }
        //重新创建一个Canceller
        mSoterBiometricCanceller = new SoterBiometricCanceller();
        int scene = biometricType == ConstantsSoter.FINGERPRINT_AUTH
                ? BiometricConst.SCENE_FINGERPRINT : BiometricConst.SCENE_FACEID;
        // 通过Builder来构建认证请求
        AuthenticationParam param = new AuthenticationParam.AuthenticationParamBuilder()
                .setScene(scene) // 指定需要认证的场景。必须在init中初始化。必填
                .setBiometricType(biometricType)
                .setContext(context) // 指定当前上下文。必填。
                .setSoterBiometricCanceller(mSoterBiometricCanceller)
                //setIWrapGetChallengeStr(new RemoteGetChallengeStr()) // 用于获取挑战因子的网络封装结构体。如果在授权之前已经通过其他模块拿到后台挑战因子，则可以改为调用setPrefilledChallenge。如果两个方法都没有调用，则会引起错误。
                .setPrefilledChallenge("prefilled challenge") // 如果之前已经通过其他方式获取了挑战因子，则设置此字段。如果设置了该字段，则忽略获取挑战因子网络封装结构体的设置。如果两个方法都没有调用，则会引起错误。
                .setIWrapUploadSignature(uploadSignatureWrapper) // 用于上传最终结果的网络封装结构体。该结构体一般来说不独立存在，而是集成在最终授权网络请求中，该请求实现相关接口即可。选填，如果没有填写该字段，则要求应用方自行上传该请求返回字段。
                .setSoterBiometricStateCallback(new SoterBiometricStateCallback() { // 指纹回调仅仅用来更新UI相关，不建议在指纹回调中进行任何业务操作。选填。

                    // 指纹回调仅仅用来更新UI相关，不建议在指纹回调中进行任何业务操作
                    // Fingerprint state callbacks are only used for updating UI. Any logic operation is not welcomed.
                    @Override
                    public void onStartAuthentication() {
                        if (mBiometricAuthCallback != null) {
                            mBiometricAuthCallback.onAuthenticationStart();
                            mBiometricAuthCallback.onShowAuthDialog(biometricType);
                        }
                    }

                    @Override
                    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                    }

                    @Override
                    public void onAuthenticationSucceed() {
                        mSoterBiometricCanceller = null;
                        // 可以在这里做相应的UI操作
                        if (mBiometricAuthCallback != null) {
                            mBiometricAuthCallback.onHideAuthDialog(true);
                            mBiometricAuthCallback.onAuthenticationSucceeded();
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        if (mBiometricAuthCallback != null) {
                            mBiometricAuthCallback.onNotifyAuthDialogOnceAgain(biometricType);
                        }
                    }

                    @Override
                    public void onAuthenticationCancelled() {
                        mSoterBiometricCanceller = null;
                        if (mBiometricAuthCallback != null) {
                            mBiometricAuthCallback.onHideAuthDialog(false);
                            mBiometricAuthCallback.onAuthenticationCancelled(biometricType);
                        }
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errorString) {
                        mSoterBiometricCanceller = null;
                        if (mBiometricAuthCallback != null) {
                            mBiometricAuthCallback.onHideAuthDialog(false);
                            mBiometricAuthCallback.onAuthenticationError(biometricType, errorCode, errorString.toString());
                        }
                    }
                }).build();
        //SoterWrapperApi 发起认证
        SoterWrapperApi.requestAuthorizeAndSign(processCallback, param);
    }

    private interface IOnAuthKeyPrepared {
        void onResult(boolean isSuccess);
    }

}
