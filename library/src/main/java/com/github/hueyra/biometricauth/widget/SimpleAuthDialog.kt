package com.github.hueyra.biometricauth.widget

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.github.hueyra.biometricauth.R

/**
 * Created by zhujun.
 * Date : 2021/10/20
 * Desc : 简单认证对话框
 */
class SimpleAuthDialog(context: Context) : CustomStatusView.OnSucOrFaiAnimEndListener {

    private val alertDialog: AlertDialog
    private val mSadSvLoading: CustomStatusView
    private val mSadIvImg: ImageView
    private val mSadTvText: TextView
    private var mAnimatorSet: AnimatorSet? = null
    private var mAuthSuccessEndLoading = false
    private var mIsShowing: Boolean
    private var mOnAuthActionListener: OnAuthActionListener? = null

    init {
        val builder = AlertDialog.Builder(context, R.style.CustomDialogBgNull)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_auth_simple, null)
        mSadSvLoading = view.findViewById(R.id.sad_sv_loading)
        mSadIvImg = view.findViewById(R.id.sad_iv_img)
        mSadTvText = view.findViewById(R.id.sad_tv_text)
        mSadSvLoading.setOnSucOrFaiAnimEndListener(this)
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

    fun loading() {
        mSadSvLoading.visibility = View.VISIBLE
        mSadIvImg.visibility = View.GONE
        mSadTvText.text = "处理中..."
        mSadSvLoading.onLoading()
        show()
    }

    fun loading(hint: String?) {
        mSadSvLoading.visibility = View.VISIBLE
        mSadIvImg.visibility = View.GONE
        mSadTvText.text = hint
        mSadSvLoading.onLoading()
        show()
    }

    fun authWithFaceID() {
        mSadSvLoading.visibility = View.GONE
        mSadIvImg.setImageResource(R.mipmap.ic_face_id_white)
        mSadIvImg.visibility = View.VISIBLE
        mSadTvText.text = "面容识别"
        show()
        startAuthAnim()
    }

    fun authWithTouchID() {
        mSadSvLoading.visibility = View.GONE
        mSadIvImg.setImageResource(R.mipmap.ic_touch_id_white)
        mSadIvImg.visibility = View.VISIBLE
        mSadTvText.text = "指纹识别"
        show()
        startAuthAnim()
    }

    private fun startAuthAnim() {
        mSadIvImg.scaleX = 1f
        mSadIvImg.scaleY = 1f
        mSadIvImg.alpha = 1f
        val scaleX = ObjectAnimator.ofFloat(mSadIvImg, "scaleX", 1f, 1.1f, 0.9f, 1f)
        scaleX.repeatCount = -1
        val scaleY = ObjectAnimator.ofFloat(mSadIvImg, "scaleY", 1f, 1.1f, 0.9f, 1f)
        scaleY.repeatCount = -1
        val alpha = ObjectAnimator.ofFloat(mSadIvImg, "alpha", 1.0f, 0.9f)
        alpha.repeatCount = -1
        mAnimatorSet = AnimatorSet()
        mAnimatorSet!!.play(scaleX).with(scaleY).with(alpha)
        mAnimatorSet!!.duration = 1000
        mAnimatorSet!!.start()
    }

    fun authSuccess() {
        mSadIvImg.visibility = View.GONE
        if (mAnimatorSet != null) {
            mAnimatorSet!!.cancel()
            mAnimatorSet = null
        }
        mSadSvLoading.visibility = View.VISIBLE
        show()
        mSadSvLoading.onSuccess()
    }

    fun isShowing(): Boolean {
        return mIsShowing
    }

    fun authSuccessEndLoading() {
        mAuthSuccessEndLoading = true
        authSuccess()
    }

    fun show() {
        alertDialog.show()
        mIsShowing = true
    }

    fun hide() {
        alertDialog.hide()
        mIsShowing = false
    }

    fun cancel() {
        if (mAnimatorSet != null) {
            mAnimatorSet!!.cancel()
            mAnimatorSet = null
        }
        alertDialog.dismiss()
        alertDialog.cancel()
        mIsShowing = false
    }

    override fun onSucOrFaiAnimEnd() {
        if (mOnAuthActionListener != null) {
            mOnAuthActionListener!!.onAuthSuccessAnimEnd()
        }
        if (!mAuthSuccessEndLoading) {
            hide()
        } else {
            loading("验证成功")
        }
    }

}