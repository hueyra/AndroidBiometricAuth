package com.github.hueyra.biometricauth.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.CycleInterpolator

/**
 * Created by zhujun
 * Date : 2022-03-28
 * Desc : _
 */

/**
 * 任意一个View的Shake动画，左右摇晃
 *
 * @param animEndListener 动画结束后的回调事件
 *
 * */
fun View.shakeShake(animEndListener: (() -> Unit)?) {
    this.shakeShake(animEndListener, true)
}

/**
 * 任意一个View的Shake动画，左右摇晃
 *
 * @param animEndListener 动画结束后的回调事件
 * @param hapticFeedback 是否需要震动反馈
 *
 * */
fun View.shakeShake(animEndListener: (() -> Unit)?, hapticFeedback: Boolean) {
    this.shakeShake(animEndListener, hapticFeedback, -5f, 5f, 0f)
}

/**
 * 任意一个View的Shake动画，左右摇晃
 *
 * @param animEndListener 动画结束后的回调事件
 * @param hapticFeedback 是否需要震动反馈
 * @param values 左右距离
 *
 * */
fun View.shakeShake(animEndListener: (() -> Unit)?, hapticFeedback: Boolean, vararg values: Float) {
    val objectAnimator =
        ObjectAnimator.ofFloat(this, "translationX", *values)
    objectAnimator.interpolator = CycleInterpolator(5f)
    objectAnimator.duration = 300
    objectAnimator.addListener(object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {}
        override fun onAnimationEnd(animation: Animator) {
            animEndListener?.invoke()
        }

        override fun onAnimationCancel(animation: Animator) {}
        override fun onAnimationRepeat(animation: Animator) {}
    })
    objectAnimator.start()
    if (hapticFeedback) {
        this.hapticFeedbackError()
    }
}


fun View.hapticFeedbackError() {
    //震动两下
    this.performHapticFeedback(
        HapticFeedbackConstants.VIRTUAL_KEY,
        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
    )
    this.postDelayed({
        this.performHapticFeedback(
            HapticFeedbackConstants.VIRTUAL_KEY,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }, 150)
}

fun View.hapticFeedbackSuccess() {
    //震动一下
    this.performHapticFeedback(
        HapticFeedbackConstants.VIRTUAL_KEY,
        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
    )
}