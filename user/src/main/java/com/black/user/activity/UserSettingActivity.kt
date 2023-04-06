package com.black.user.activity

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.databinding.DataBindingUtil
import com.black.base.BaseApplication
import com.black.base.activity.BaseActivity
import com.black.base.model.FryingStyleChange
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.util.StyleChangeUtil
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivityUserSettingBinding
import com.black.util.CommonUtil

@Route(value = [RouterConstData.USER_SETTING], beforePath = RouterConstData.LOGIN)
class UserSettingActivity : BaseActivity(), View.OnClickListener {
    private var application: BaseApplication? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityUserSettingBinding = DataBindingUtil.setContentView(this, R.layout.activity_user_setting)
        binding.push.setOnClickListener(this)
        binding.changeUser.setOnClickListener(this)
        binding.aboutUs.setOnClickListener(this)
        binding.styleSetting.setOnClickListener(this)
        binding.version.setText(String.format("V%s" , CommonUtil.getVersionName(mContext,"1.2.0")))
        application = getApplication() as BaseApplication
        val style = StyleChangeUtil.getStyleChangeSetting(mContext)?.styleCode
        if (style == null || style == 0) {
            binding.redDown.setText(R.string.red_down)
        } else {
            binding.redDown.setText(getString(R.string.red_up))
        }
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
            R.id.style_setting ->{
                showChangeDialog()
            }
        }
    }
    private fun showChangeDialog(){
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_change_style,null)
        val alertDialog = Dialog(mContext, R.style.AlertDialog)
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
        contentView.findViewById<View>(R.id.green_up).setOnClickListener { v ->
            v.tag = application!!.getStyleChange(FryingStyleChange.greenUp)
            alertDialog.dismiss()
            change(v.tag as FryingStyleChange)
            finish()
        }
        contentView.findViewById<View>(R.id.red_up).setOnClickListener { v ->
            alertDialog.dismiss()
            v.tag = application!!.getStyleChange(FryingStyleChange.redUp)
            change(v.tag as FryingStyleChange)
        }
        alertDialog.show()
}
private fun change(styleChange: FryingStyleChange){
    if(styleChange != StyleChangeUtil.getStyleChangeSetting(this)){
        StyleChangeUtil.setStyleChangeSetting(this,styleChange)
        FryingUtil.showToast(mContext, getString(com.black.base.R.string.change_successful))
        BlackRouter.getInstance().build(RouterConstData.START_PAGE)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .go(this)
    }
}

}