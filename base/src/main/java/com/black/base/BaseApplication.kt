package com.black.base

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import android.webkit.WebView
import androidx.multidex.MultiDex
import com.black.base.model.FryingExchangeRates
import com.black.base.model.FryingLanguage
import com.black.base.model.filter.*
import com.black.base.model.wallet.WalletBillType
import java.util.*

open class BaseApplication : Application() {
    companion object {
        lateinit var instance: BaseApplication
            private set
        private var languages: ArrayList<FryingLanguage?>? = null
        private var exchangeRates: ArrayList<FryingExchangeRates?>? = null
        var checkTokenError = true
        var isXGRegister = false
        var hasInitJGPush = false
        var isXGBind = false
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        instance = this
        MultiDex.install(base)
    }

    override fun onCreate() {
        super.onCreate()
        //清理webview的缓存
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = getProcessName(this)
            if ("com.fbsex.exchange" != processName) { //判断不等于默认进程名称
                WebView.setDataDirectorySuffix(processName)
            }
        }
        WebView(this).destroy()
    }

    fun getProcessName(context: Context?): String? {
        if (context == null) return null
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (processInfo in manager.runningAppProcesses) {
            if (processInfo.pid == Process.myPid()) {
                return processInfo.processName
            }
        }
        return null
    }

    fun initLanguageItems(context: Context) {
        languages = ArrayList()
        languages!!.add(FryingLanguage(Locale.CHINESE, 0, context.getString(R.string.language_chinese)))
        languages!!.add( FryingLanguage(Locale.TAIWAN, 1, context.getString(R.string.language_chinese_tw)))
        languages!!.add( FryingLanguage(Locale.ENGLISH, 2, context.getString(R.string.language_english)))
        languages!!.add( FryingLanguage(Locale.JAPANESE, 3, context.getString(R.string.language_janpanese)))
        languages!!.add( FryingLanguage(Locale.UK, 4, context.getString(R.string.language_english_uk)))
    }

    fun initExchangeItems(context: Context){
        exchangeRates = ArrayList()
        exchangeRates!!.add(FryingExchangeRates(0, context.getString(R.string.cny)))
        exchangeRates!!.add(FryingExchangeRates(1, context.getString(R.string.exchange_rates_usd)))
        exchangeRates!!.add(FryingExchangeRates(2, context.getString(R.string.exchange_rates_jpy)))
        exchangeRates!!.add(FryingExchangeRates(3, context.getString(R.string.exchange_rates_krw)))
        exchangeRates!!.add(FryingExchangeRates(4, context.getString(R.string.exchange_rates_vnd)))
    }

    open fun getLanguage(type:Int): FryingLanguage? {
        if (languages == null || languages!!.isEmpty()) {
            initLanguageItems(this)
        }
        return languages?.get(type)
    }

    open fun getExhcangeRates(type:Int): FryingExchangeRates? {
        if (exchangeRates == null || exchangeRates!!.isEmpty()) {
            initExchangeItems(this)
        }
        return exchangeRates?.get(type)
    }

    fun initFilters() {
        CoinFilter.init(this)
        DateFilter.init(this)
        DaysFilter.init(this)
        DemandStatus.init(this)
        EntrustStatus.init(this)
        EntrustType.init(this)
        FinancialType.init(this)
        RecommendPeopleFilter.init(this)
        RecommendRewardFilter.init(this)
        RegularStatus.init(this)
        WalletBillType.init(this)
        DemandRecordStatus.init(this)
        RegularRecordStatus.init(this)
    }
}
