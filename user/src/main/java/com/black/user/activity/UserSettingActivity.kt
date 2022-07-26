package com.black.user.activity

import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.UserApiServiceHelper
import com.black.base.model.HttpRequestResultString
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.util.SocketUtil
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivityUserSettingBinding
import com.black.util.Callback

@Route(value = [RouterConstData.USER_SETTING], beforePath = RouterConstData.LOGIN)
class UserSettingActivity : BaseActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityUserSettingBinding = DataBindingUtil.setContentView(this, R.layout.activity_user_setting)
        binding.push.setOnClickListener(this)
        binding.changeUser.setOnClickListener(this)
        binding.logout.setOnClickListener(this)
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
            R.id.logout -> {
                if (CookieUtil.getUserInfo(mContext) != null) {
                    showLogoutDialog(View.OnClickListener {
                        val doLogout = Runnable {
                            //清空所有
                            //                                CookieUtil.deleteUserInfo(mContext);
                            //                                CookieUtil.deleteToken(mContext);
                            //                                CookieUtil.setMainEyeStatus(mContext, true);
                            //                                //CookieUtil.setAccountProtectType(getActivity(), 0);
                            //                                //CookieUtil.setGesturePassword(getActivity(), null);
                            //                                //CookieUtil.setAccountProtectJump(getActivity(), false);
                            //                                //CookieUtil.saveUserId(getActivity(), null);
                            //                                //CookieUtil.saveUserName(getActivity(), null);
                            //                                DataBaseUtil.clear(mContext);
                            FryingUtil.clearAllUserInfo(mContext)
                            sendPairChangedBroadcast(SocketUtil.COMMAND_USER_LOGOUT)
                            finish()
                        }
                        UserApiServiceHelper.logout(mContext, object : Callback<HttpRequestResultString?>() {
                            override fun error(type: Int, error: Any) {
                                doLogout.run()
                            }

                            override fun callback(returnData: HttpRequestResultString?) {
                                doLogout.run()
                            }
                        })
                    })
                }
            }
        }
    }

    private fun showLogoutDialog(resumeClickListener: View.OnClickListener?) {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_logout_resume, null)
        val alertDialog = Dialog(mContext, R.style.AlertDialog)
        //        alertDialog.setContentView(contentView);
//                new AlertDialog.Builder(mActivity).setView(contentView).create();
//        int height = display.getHeight();
        val window = alertDialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.BOTTOM
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            //设置dialog动画
            window.setWindowAnimations(R.style.anim_bottom_in_out)
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams = ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        alertDialog.setContentView(contentView, layoutParams)
        //        dialog.setContentView(viewDialog, layoutParams);
        contentView.findViewById<View>(R.id.btn_resume).setOnClickListener { v ->
            alertDialog.dismiss()
            resumeClickListener?.onClick(v)
        }
        contentView.findViewById<View>(R.id.btn_cancel).setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }
}