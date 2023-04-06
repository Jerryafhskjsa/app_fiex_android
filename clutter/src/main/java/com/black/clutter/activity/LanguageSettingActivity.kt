package com.black.clutter.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.black.base.BaseApplication
import com.black.base.activity.BaseActivity
import com.black.base.model.FryingExchangeRates
import com.black.base.model.FryingLanguage
import com.black.base.util.ExchangeRatesUtil
import com.black.base.util.FryingUtil
import com.black.base.util.LanguageUtil
import com.black.base.util.RouterConstData
import com.black.clutter.R
import com.black.clutter.databinding.ActivityLanguageSettingBinding
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import skin.support.content.res.SkinCompatResources

@Route(value = [RouterConstData.LANGUAGE_SETTING])
class LanguageSettingActivity : BaseActivity(), View.OnClickListener {
    private var application: BaseApplication? = null
    private var binding: ActivityLanguageSettingBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        application = getApplication() as BaseApplication
        initLanguage()
        onLanguageChanged()
    }

    private fun initLanguage(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_language_setting)
        binding?.chinese?.setOnClickListener(this)
        binding?.chinese?.tag = application!!.getLanguage(FryingLanguage.Chinese)
        binding?.chineseTw?.setOnClickListener(this)
        binding?.chineseTw?.tag = application!!.getLanguage(FryingLanguage.Chinese_tw)
        binding?.english?.setOnClickListener(this)
        binding?.english?.tag = application!!.getLanguage(FryingLanguage.English)
        binding?.vietnam?.setOnClickListener(this)
        binding?.vietnam?.tag = application!!.getLanguage(FryingLanguage.Vietnam)
        binding?.englishUk?.setOnClickListener(this)
        binding?.englishUk?.tag = application!!.getLanguage(FryingLanguage.English_uk)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.language)
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.chinese,R.id.chinese_tw,R.id.english,R.id.vietnam,R.id.english_uk -> changeLanguage(v.tag as FryingLanguage)
        }
    }

    private fun changeLanguage(language: FryingLanguage) {
        if (language.languageCode != 0  && language.languageCode != 3) {
            FryingUtil.showToast(this, getString(R.string.please_waiting))
            return
        }
        if (language != LanguageUtil.getLanguageSetting(this)) {
            LanguageUtil.changeAppLanguage(this, language, true)
            ExchangeRatesUtil.setExChangeRatesSetting(this, FryingExchangeRates(1, "USD"))
            onLanguageChanged()
            BlackRouter.getInstance().build(RouterConstData.START_PAGE)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .go(this)
        }
    }

    private fun onLanguageChanged() {
        var language = LanguageUtil.getLanguageSetting(this)
        refreshLanguageChecked(language, binding?.chinese)
        refreshLanguageChecked(language, binding?.chineseTw)
        refreshLanguageChecked(language, binding?.english)
        refreshLanguageChecked(language, binding?.vietnam)
        refreshLanguageChecked(language, binding?.englishUk)
    }

    private fun refreshLanguageChecked(language: FryingLanguage?, textView: TextView?) {
        if (language == null || textView == null) {
            return
        }
        var tag = textView.tag
        if(tag is FryingLanguage){
            if (language.languageCode == tag.languageCode ) {
                CommonUtil.setTextViewCompoundDrawable(textView, SkinCompatResources.getDrawable(this, R.drawable.icon_language_ok), 2)
            } else {
                CommonUtil.setTextViewCompoundDrawable(textView, null, 2)
            }
        }else{
            CommonUtil.setTextViewCompoundDrawable(textView, null, 2)
        }
    }
}