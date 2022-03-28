package com.github.hueyra.biometricauth.widget

/**
 * Created by zhujun.
 * Date : 2021/10/20
 * Desc : __
 */
interface OnAuthActionListener {
    fun onAuthAgain()
    fun onAuthCancel()
    fun onAuthSuccessAnimEnd()
}