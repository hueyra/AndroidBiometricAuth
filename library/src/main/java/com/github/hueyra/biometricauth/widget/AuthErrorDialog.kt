package com.github.hueyra.biometricauth.widget

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.github.hueyra.biometricauth.R

/**
 * Created by zhujun.
 * Date : 2021/10/20
 * Desc : __
 */
class AuthErrorDialog(context: Context) : CustomStatusView.OnSucOrFaiAnimEndListener {

    private val alertDialog: AlertDialog
    private var mCurrentIsFaceAuth = false
    private val mEadSvLoading: CustomStatusView
    private val mEadIvImg: ImageView
    private val mEadTvTitle: TextView
    private val mEadTvHint: TextView
    private val mEadVAuthAgainLine: View
    private val mEadTvAuthAgain: TextView
    private val mEadTvAuthCancel: TextView
    private var mOnAuthActionListener: OnAuthActionListener? = null
    private var mScaleXAnim: ObjectAnimator? = null
    private var mScaleYAnim: ObjectAnimator? = null
    private var mAlphaAnim: ObjectAnimator? = null
    private var mAnimatorSet: AnimatorSet? = null
    private var mIsShowing: Boolean
    private var mAuthSuccessEndLoading = false

    init {
        val builder = AlertDialog.Builder(context, R.style.CustomDialogBgGery)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_auth_error, null)
        mEadSvLoading = view.findViewById(R.id.ead_sv_loading)
        mEadIvImg = view.findViewById(R.id.ead_iv_img)
        mEadTvTitle = view.findViewById(R.id.ead_tv_title)
        mEadTvHint = view.findViewById(R.id.ead_tv_hint)
        mEadVAuthAgainLine = view.findViewById(R.id.ead_v_auth_again_line)
        mEadTvAuthAgain = view.findViewById(R.id.ead_tv_auth_again)
        mEadTvAuthCancel = view.findViewById(R.id.ead_tv_auth_cancel)
        mEadTvAuthCancel.text = "取消"
        mEadTvAuthCancel.setOnClickListener {
            if (mOnAuthActionListener != null) {
                mOnAuthActionListener!!.onAuthCancel()
                hide()
            }
        }
        mEadTvAuthAgain.setOnClickListener {
            if (mOnAuthActionListener != null) {
                mOnAuthActionListener!!.onAuthAgain()
            }
        }
        mEadSvLoading.setOnSucOrFaiAnimEndListener(this)
        builder.setView(view)
        alertDialog = builder.create()
        alertDialog.setCancelable(false)
        mIsShowing = false
    }

    fun setCancelable(b: Boolean) {
        try {
            alertDialog.setCancelable(b)
        } catch (w: Exception) {
            //
        }
    }

    fun setOnAuthActionListener(listener: OnAuthActionListener?) {
        mOnAuthActionListener = listener
    }

    //人脸认证错误
    fun authErrorWithFaceID() {
        mCurrentIsFaceAuth = true
        authError()
    }

    //指纹认证错误
    fun authErrorWithTouchID() {
        mCurrentIsFaceAuth = false
        authError()
    }

    fun authAgain(isFace: Boolean) {
        mCurrentIsFaceAuth = isFace
        authAgain()
    }

    fun loading(hint: String?) {
        mEadSvLoading.visibility = View.VISIBLE
        mEadSvLoading.onLoading()
        mEadIvImg.visibility = View.GONE
        mEadTvTitle.text = if (mCurrentIsFaceAuth) "面容识别" else "指纹识别"
        if (!TextUtils.isEmpty(hint)) {
            mEadTvHint.text = hint
        } else {
            mEadTvHint.text = "处理中..."
        }
        mEadTvAuthAgain.text = if (mCurrentIsFaceAuth) "再次尝试面容识别" else "再次尝试指纹识别"
        mEadVAuthAgainLine.visibility = View.VISIBLE
        mEadTvAuthAgain.visibility = View.VISIBLE
        show()
    }

    fun authAgain() {
        mEadSvLoading.visibility = View.GONE
        mEadIvImg.setImageResource(if (mCurrentIsFaceAuth) R.mipmap.ic_face_id_gery else R.mipmap.ic_touch_id_gery)
        mEadIvImg.visibility = View.VISIBLE
        mEadTvTitle.text = if (mCurrentIsFaceAuth) "面容识别" else "指纹识别"
        mEadTvHint.text = "再试一次"
        mEadTvAuthAgain.text = if (mCurrentIsFaceAuth) "再次尝试面容识别" else "再次尝试指纹识别"
        mEadVAuthAgainLine.visibility = View.VISIBLE
        mEadTvAuthAgain.visibility = View.VISIBLE
        startAuthAnim()
        show()
    }

    fun authSuccessEndLoading() {
        mAuthSuccessEndLoading = true
        authSuccess()
    }

    fun authSuccess() {
        mEadTvTitle.text = if (mCurrentIsFaceAuth) "面容识别" else "指纹识别"
        mEadTvHint.text = "再试一次"
        mEadSvLoading.visibility = View.VISIBLE
        mEadIvImg.visibility = View.GONE
        cancelAuthAnim()
        show()
        mEadSvLoading.onSuccess()
    }

    fun authErrorTooManyTimes(isFace: Boolean) {
        mCurrentIsFaceAuth = isFace
        authErrorTooManyTimes()
    }

    fun authErrorTooManyTimes() {
        mEadSvLoading.visibility = View.GONE
        cancelAuthAnim()
        mEadIvImg.setImageResource(if (mCurrentIsFaceAuth) R.mipmap.ic_face_id_gery else R.mipmap.ic_touch_id_gery)
        mEadIvImg.visibility = View.VISIBLE
        mEadTvTitle.text = if (mCurrentIsFaceAuth) "未能识别面孔" else "未能识别指纹"
        mEadTvHint.text = "请使用其他方式"
        mEadVAuthAgainLine.visibility = View.GONE
        mEadTvAuthAgain.visibility = View.GONE
        show()
        startErrorAnim()
    }

    @SuppressLint("SetTextI18n")
    private fun authError() {
        mEadSvLoading.visibility = View.GONE
        cancelAuthAnim()
        mEadIvImg.setImageResource(if (mCurrentIsFaceAuth) R.mipmap.ic_face_id_gery else R.mipmap.ic_touch_id_gery)
        mEadIvImg.visibility = View.VISIBLE
        mEadTvTitle.text = if (mCurrentIsFaceAuth) "未能识别面孔" else "未能识别指纹"
        mEadTvHint.text = "再试一次"
        mEadTvAuthAgain.text = if (mCurrentIsFaceAuth) "再次尝试面容识别" else "再次尝试指纹识别"
        mEadVAuthAgainLine.visibility = View.VISIBLE
        mEadTvAuthAgain.visibility = View.VISIBLE
        show()
        startErrorAnim()
    }

    private fun startAuthAnim() {
        mScaleXAnim = ObjectAnimator.ofFloat(mEadIvImg, "scaleX", 1f, 1.1f, 0.9f, 1f)
        mScaleXAnim?.repeatCount = -1
        mScaleYAnim = ObjectAnimator.ofFloat(mEadIvImg, "scaleY", 1f, 1.1f, 0.9f, 1f)
        mScaleYAnim?.repeatCount = -1
        mAlphaAnim = ObjectAnimator.ofFloat(mEadIvImg, "alpha", 1.0f, 0.9f)
        mAlphaAnim?.repeatCount = -1
        //
        mScaleXAnim?.duration = 1000
        mScaleYAnim?.duration = 1000
        mAlphaAnim?.duration = 1000
        //
        mScaleXAnim?.start()
        mScaleYAnim?.start()
        mAlphaAnim?.start()
    }

    private fun cancelAuthAnim() {
        if (mScaleXAnim != null) {
            mScaleXAnim!!.repeatCount = 0
            mScaleXAnim!!.cancel()
            mScaleXAnim = null
        }
        if (mScaleYAnim != null) {
            mScaleYAnim!!.repeatCount = 0
            mScaleYAnim!!.cancel()
            mScaleYAnim = null
        }
        if (mAlphaAnim != null) {
            mAlphaAnim!!.repeatCount = 0
            mAlphaAnim!!.cancel()
            mAlphaAnim = null
        }
        if (mAnimatorSet != null) {
            mAnimatorSet!!.cancel()
            mAnimatorSet = null
        }
        mEadIvImg.scaleX = 1f
        mEadIvImg.scaleY = 1f
        mEadIvImg.alpha = 1f
    }

    private fun startErrorAnim() {
        //  shake anim
        mEadIvImg.shakeShake({
            mEadIvImg.translationX = 0f
        }, true, 0f, -10f, 10f, 0f)
    }

    fun show() {
        alertDialog.show()
        mIsShowing = true
    }

    fun hide() {
        cancelAuthAnim()
        alertDialog.hide()
        mIsShowing = false
    }

    fun cancel() {
        cancelAuthAnim()
        alertDialog.dismiss()
        alertDialog.cancel()
        mIsShowing = false
    }

    fun isShowing(): Boolean {
        return mIsShowing
    }

    override fun onSucOrFaiAnimEnd() {
        if (mOnAuthActionListener != null) {
            mOnAuthActionListener!!.onAuthSuccessAnimEnd()
        }
        if (!mAuthSuccessEndLoading) {
            hide()
        } else {
            //识别成功后，需要请求数据的情况
            mEadIvImg.visibility = View.GONE
            mEadSvLoading.visibility = View.VISIBLE
            mEadSvLoading.onLoading()
            mEadTvTitle.text = if (mCurrentIsFaceAuth) "面容识别" else "指纹识别"
            mEadTvHint.text = "验证成功"
            if (mEadTvAuthAgain.visibility == View.VISIBLE) {
                mEadTvAuthAgain.isEnabled = false
            }
            mEadTvAuthCancel.isEnabled = false
        }
    }


}