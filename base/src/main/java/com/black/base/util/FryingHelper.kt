package com.black.base.util

import android.app.Activity
import android.os.Bundle
import com.black.router.BlackRouter

class FryingHelper(private val activity: Activity) {
    companion object {
        const val DEMAND_INDEX = 2
        const val MINE_INDEX = 4
    }

    private var loginCallback: Runnable? = null
    fun checkUserAndDoing(callback: Runnable?, homeFragmentIndex: Int) {
        if (CookieUtil.getUserInfo(activity) == null) {
            loginAndCallback(callback, homeFragmentIndex)
        } else {
            callback?.run()
        }
    }


    fun onResume() {
        if (loginCallback != null && CookieUtil.getUserInfo(activity) != null) {
            val runnable: Runnable = loginCallback!!
            runnable.run()
        }
        loginCallback = null
    }

    fun loginAndCallback(callback: Runnable?, homeFragmentIndex: Int) {
        loginCallback = callback
        val bundle = Bundle()
        bundle.putBoolean(ConstData.FOR_RESULT, true)
        bundle.putInt(ConstData.HOME_FRAGMENT_INDEX, homeFragmentIndex)
        BlackRouter.getInstance().build(RouterConstData.LOGIN)
                .with(bundle).go(activity)
    }
}