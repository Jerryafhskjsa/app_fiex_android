package com.black.user.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import com.black.base.BaseApplication
import com.black.base.activity.BaseActivity
import com.black.base.model.FryingStyleChange
import com.black.base.model.FutureSecondChange
import com.black.base.util.ExchangeRatesUtil
import com.black.base.util.FryingUtil
import com.black.base.util.FutureSecond
import com.black.base.util.LanguageUtil
import com.black.base.util.RouterConstData
import com.black.base.util.StyleChangeUtil
import com.black.base.widget.SpanCheckedTextView
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivityUserSettingBinding
import com.black.util.CommonUtil
import kotlinx.android.synthetic.main.activity_user_setting.btn_future
import kotlinx.android.synthetic.main.activity_user_setting.view.btn_future
import org.intellij.lang.annotations.Language

@Route(value = [RouterConstData.USER_SETTING], beforePath = RouterConstData.LOGIN)
class UserSettingActivity : BaseActivity(), View.OnClickListener {
    private var application: BaseApplication? = null
    private var style: Int? = 0
    private var future: Int? = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityUserSettingBinding = DataBindingUtil.setContentView(this, R.layout.activity_user_setting)
        binding.push.setOnClickListener(this)
        binding.changeUser.setOnClickListener(this)
        binding.aboutUs.setOnClickListener(this)
        binding.moreLanguage.setOnClickListener(this)
        binding.jijiaSetting.setOnClickListener(this)
        binding.btnFuture.setOnClickListener(this)
        binding.styleSetting.setOnClickListener(this)
        binding.version.setText(String.format("V%s" , CommonUtil.getVersionName(mContext,"1.0.0")))
        application = getApplication() as BaseApplication

        style = StyleChangeUtil.getStyleChangeSetting(mContext)?.styleCode
        future = FutureSecond.getFutureSecondSetting(mContext)?.futureCode
        val language = LanguageUtil.getLanguageSetting(mContext)?.languageCode
        val rate = ExchangeRatesUtil.getExchangeRatesSetting(mContext)?.rateCode
        binding.btnFuture.isChecked = !(future == null || future == 0)
        if (style == null || style == 0) {
            binding.redDown.setText(R.string.red_down)
        } else {
            binding.redDown.setText(getString(R.string.red_up))
        }
        if (language == null || language == 2) {
            binding.currentLanguage.setText(R.string.language_chinese)
        } else {
            binding.currentLanguage.setText(getString(R.string.language_english))
        }
        if (rate == null || rate == 0) {
            binding.rate.setText(R.string.cny)
        } else {
            binding.rate.setText(getString(R.string.usd))
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
            R.id.btn_future -> {
                if (view.btn_future.isChecked) {
                    view.btn_future.tag = application!!.getFutureSecond(FutureSecondChange.two)
                    futureChange(view.btn_future.tag as FutureSecondChange)
                }
                else{
                    view.btn_future.tag = application!!.getFutureSecond(FutureSecondChange.one)
                    futureChange(view.btn_future.tag as FutureSecondChange)
                }
            }
            R.id.more_language ->{
                BlackRouter.getInstance().build(RouterConstData.LANGUAGE_SETTING).go(this)
            }
            R.id.jijia_setting ->{
                BlackRouter.getInstance().build(RouterConstData.EXCHANGE_RATES).go(this)
            }
        }
    }
    @SuppressLint("MissingInflatedId")
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
        alertDialog.show()
        if (style == null || style == 0) {
            contentView.findViewById<SpanCheckedTextView>(R.id.green_up).isChecked = false
            contentView.findViewById<View>(R.id.bar_a).visibility = View.VISIBLE
            contentView.findViewById<View>(R.id.bar_b).visibility = View.GONE
        } else {
            contentView.findViewById<SpanCheckedTextView>(R.id.red_up).isChecked = false
            contentView.findViewById<View>(R.id.bar_a).visibility = View.GONE
            contentView.findViewById<View>(R.id.bar_b).visibility = View.VISIBLE
        }
        contentView.findViewById<View>(R.id.green_up).setOnClickListener { v ->
            v.tag = application!!.getStyleChange(FryingStyleChange.greenUp)
            contentView.findViewById<SpanCheckedTextView>(R.id.green_up).isChecked = false
            contentView.findViewById<View>(R.id.bar_a).visibility = View.VISIBLE
            contentView.findViewById<View>(R.id.bar_b).visibility = View.GONE
            alertDialog.dismiss()
            change(v.tag as FryingStyleChange)
            finish()
        }
        contentView.findViewById<View>(R.id.red_up).setOnClickListener { v ->
            contentView.findViewById<SpanCheckedTextView>(R.id.red_up).isChecked = false
            contentView.findViewById<View>(R.id.bar_a).visibility = View.GONE
            contentView.findViewById<View>(R.id.bar_b).visibility = View.VISIBLE
            alertDialog.dismiss()
            v.tag = application!!.getStyleChange(FryingStyleChange.redUp)
            change(v.tag as FryingStyleChange)
        }
        contentView.findViewById<ImageView>(R.id.btn_cancel).setOnClickListener { v ->
            alertDialog.dismiss()
        }
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

    private fun futureChange(styleChange: FutureSecondChange){
        if(styleChange != FutureSecond.getFutureSecondSetting(this)){
            FutureSecond.setFutureSecondSetting(this,styleChange)
            FryingUtil.showToast(mContext, getString(com.black.base.R.string.change_successful))
            BlackRouter.getInstance().build(RouterConstData.START_PAGE)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .go(this)
        }
    }
}