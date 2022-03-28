package com.hueyra.biometricauth

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.hueyra.biometricauth.BiometricAuth
import com.github.hueyra.biometricauth.model.BiometricAuthCallback
import com.github.hueyra.biometricauth.model.BiometricConst
import com.github.hueyra.biometricauth.widget.AuthErrorDialog
import com.github.hueyra.biometricauth.widget.OnAuthActionListener
import com.github.hueyra.biometricauth.widget.SimpleAuthDialog

/**
 * Created by zhujun.
 * Date : 2021/10/19
 * Desc : 授权面容ID页面
 */
class AuthFaceIDActivity : AppCompatActivity(), BiometricAuthCallback, OnAuthActionListener {

    private val mSimpleAuthDialog by lazy { SimpleAuthDialog(this) }
    private val mAuthErrorDialog by lazy { AuthErrorDialog(this) }

    private val mAfaRivHead: ImageView by lazy { findViewById(R.id.afa_riv_head) }
    private val mAfaTvName: TextView by lazy { findViewById(R.id.afa_tv_name) }
    private val mAfaLlAuth: LinearLayout by lazy { findViewById(R.id.afa_ll_auth) }
    private val mAfaIvAuthImg: ImageView by lazy { findViewById(R.id.afa_iv_auth_img) }
    private val mAfaIvAuthText: TextView by lazy { findViewById(R.id.afa_iv_auth_text) }

    private var mCurrentAuthTime = 1//当前识别的次数
    private var mCurrentFromLoginActivity = false//当前是否是从登录页面过来的


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_face_id)
        setUserInfo()
        mAfaLlAuth.setOnClickListener { startAuth() }
        startAuth()
    }

    override fun onDestroy() {
        super.onDestroy()
        BiometricAuth.getInstance().cancelAuth()
    }

    //设置用户的基础信息
    private fun setUserInfo() {

        mAfaRivHead.setImageResource(R.mipmap.ic_launcher_round)
        mAfaTvName.text = "UserName"

        mAfaIvAuthImg.setImageResource(R.mipmap.ic_face_id_green)
        mAfaIvAuthText.text = ("点击授权使用面容识别")
        //设置Listener
        mSimpleAuthDialog.setOnAuthActionListener(this)
        mAuthErrorDialog.setOnAuthActionListener(this)
    }

    //开始识别认证
    private fun startAuth() {
        //这边主要做一个判断，如果是超过5次，就直接报识别太多次了
        if (mCurrentAuthTime >= 5) {
            mAuthErrorDialog.authErrorTooManyTimes(true)
        } else {
            //设置回调
            BiometricAuth.getInstance().setBiometricAuthCallback(this)
            val check = BiometricAuth.getInstance().canAuthenticate(this)
            //检查一下能不能支持生物识别
            if (check.isSuccess) {
                //如果是支持的，那么用soter看一下能不能用人脸，最后调用认证是通过soter api实现的
                val soterCheck = BiometricAuth.getInstance().isSupportFaceAuth(this)
                if (soterCheck.isSuccess) {
                    //soter也支持就可以认证了
                    if (!BiometricAuth.getInstance().isOpenSoterFaceAuth) {
                        BiometricAuth.getInstance().openSoterFaceAuth(this)
                    } else {
                        BiometricAuth.getInstance().authenticateWithSoterFace(this)
                    }
                } else {
                    Toast.makeText(this, soterCheck.errorMsg, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, check.errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onShowLoading(msg: String?) {
        if (!mAuthErrorDialog.isShowing()) {
            mSimpleAuthDialog.loading()
        }
    }

    override fun onHideLoading() {
    }

    override fun onShowAuthDialog(biometricType: Int) {
        if (!mAuthErrorDialog.isShowing()) {
            mSimpleAuthDialog.authWithFaceID()
        }
    }

    override fun onHideAuthDialog(isSuccess: Boolean) {
    }

    override fun onNotifyAuthDialogOnceAgain(biometricType: Int) {
        if (mCurrentAuthTime == 2) {
            //直接提示识别错误对话框
            mSimpleAuthDialog.hide()
            BiometricAuth.getInstance().cancelAuth()
            mAuthErrorDialog.authErrorWithFaceID()
        } else if (mCurrentAuthTime == 3) {
            //直接提示识别错误对话框
            mSimpleAuthDialog.hide()
            BiometricAuth.getInstance().cancelAuth()
            mAuthErrorDialog.authErrorWithFaceID()
        } else if (mCurrentAuthTime >= 4) {
            //结束，不识别
            mSimpleAuthDialog.hide()
            BiometricAuth.getInstance().cancelAuth()
            mAuthErrorDialog.authErrorTooManyTimes()
        }
        mCurrentAuthTime++
    }

    override fun onAuthenticationStart() {
    }

    override fun onAuthenticationSucceeded() {
        saveAuthAndNavi()
        if (mSimpleAuthDialog.isShowing()) {
            mSimpleAuthDialog.authSuccess()
        } else {
            mAuthErrorDialog.authSuccess()
        }
    }

    //识别成功后，直接保存数据，然后跳转页面，注意跳转的逻辑是在authSuccess动画执行完后
    private fun saveAuthAndNavi() {
        //
    }

    override fun onAuthenticationError(biometricType: Int, err: Int, msg: String?) {
        if (err == 10308 || msg?.startsWith("Too many failed") == true) {
            //错误次数过多，传感器被锁定了
            mCurrentAuthTime = 100
            mSimpleAuthDialog.hide()
            mAuthErrorDialog.authErrorTooManyTimes(true)
        }
    }

    override fun onAuthenticationFailed(biometricType: Int) {
    }

    override fun onAuthenticationCancelled(biometricType: Int) {
    }

    override fun onAuthenticationSoterError(err: Int, msg: String) {
        //当签名出错的时候，需要重新生成签名并验证，参考BiometricAuthManager.getSoterAuthResultErrorMsg
        if (msg.contains(BiometricConst.AUTH_KEY_ERROR)) {
            startAuth()
        }
    }

    override fun onOpenSoterAuthSucceeded(biometricType: Int) {
    }

    override fun onOpenSoterAuthFailed(biometricType: Int) {
    }

    override fun onOpenSysAuthSucceeded() {
    }

    override fun onOpenSysAuthFailed() {
    }

    override fun onAuthAgain() {
        startAuth()
        mAuthErrorDialog.authAgain()
    }

    override fun onAuthCancel() {
        BiometricAuth.getInstance().cancelAuth()
    }

    //这个就是AuthSuccessAnimEnd，跳转
    override fun onAuthSuccessAnimEnd() {
        mSimpleAuthDialog.cancel()
        mAuthErrorDialog.cancel()
        finish()
    }
}