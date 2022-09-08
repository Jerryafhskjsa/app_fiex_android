package com.black.user.activity

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.util.RouterConstData
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivityUserSettingBinding

@Route(value = [RouterConstData.USER_SETTING], beforePath = RouterConstData.LOGIN)
class UserSettingActivity : BaseActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityUserSettingBinding = DataBindingUtil.setContentView(this, R.layout.activity_user_setting)
        binding.push.setOnClickListener(this)
        binding.changeUser.setOnClickListener(this)
        binding.aboutUs.setOnClickListener(this)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.user_setting)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.push -> {
                BlackRouter.getInstance().build(RouterConstData.PUSH_SETTING).go(this)
            }
            R.id.change_user -> {
                BlackRouter.getInstance().build(RouterConstData.ACCOUNT_MANAGER).go(this)
            }
            R.id.about_us ->{
                BlackRouter.getInstance().build(RouterConstData.ABOUT).go(this)
            }
        }
    }
}