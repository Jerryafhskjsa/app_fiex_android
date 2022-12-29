package com.black.frying.activity

import HomePageMainFragmentFiex
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TabHost
import android.widget.TextView
import androidx.fragment.app.FragmentTabHost
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.api.CommonApiServiceHelper
import com.black.base.api.WalletApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.Update
import com.black.base.model.c2c.C2CPrice
import com.black.base.model.clutter.GlobalAd
import com.black.base.model.socket.PairStatus
import com.black.base.model.user.UserInfo
import com.black.base.service.DownloadServiceHelper
import com.black.base.util.*
import com.black.base.view.PairStatusPopupWindow
import com.black.frying.FryingApplication
import com.black.frying.fragment.*
import com.black.frying.model.HomeTab
import com.black.frying.service.SocketService
import com.black.frying.service.socket.FiexSocketManager
import com.black.frying.util.UdeskUtil
import com.black.im.util.IMHelper
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.FragmentRouteHelper
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.fbsex.exchange.R
import skin.support.content.res.SkinCompatResources

@Route(value = [RouterConstData.HOME_PAGE])
class HomePageActivity : BaseActionBarActivity(), View.OnClickListener, FragmentRouteHelper {
    private val TAG = "HomePageActivity"

    private var tabHost: FragmentTabHost? = null

    private val tabs = arrayOfNulls<HomeTab>(5)
    var transactionIndex = -1
    var transactionTabType = -1
    private val transactionExtras = Bundle()

    private var backPressedToExitOnce = false
    private var currentDemandAdId: Long? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        tabHost = findViewById(android.R.id.tabhost)
        tabHost?.setup(this, supportFragmentManager, R.id.home_content)
        tabHost?.tabWidget?.setDividerDrawable(R.color.transparent)
        tabs[ConstData.TAB_HOME] = HomeTab(getString(R.string.home_tab_main), R.drawable.home_tab_main, HomePageMainFragmentFiex::class.java)
        tabs[ConstData.TAB_QUOTATION] = HomeTab(getString(R.string.home_tab_qutation), R.drawable.home_tab_qutation, HomePageQuotationFragmentMain::class.java)
//        tabs[2] = HomeTab(getString(R.string.home_tab_transaction), R.drawable.home_tab_transaction, HomePageTransactionFragment::class.java)
        tabs[ConstData.TAB_TRANSATION] = HomeTab(getString(R.string.home_tab_transaction), R.drawable.home_tab_transaction, HomePageTransactionFragmentFiex::class.java)
        tabs[ConstData.TAB_CONTRACT] = HomeTab(getString(R.string.home_tab_future), R.drawable.home_tab_futures, HomePageContractFragmentMain::class.java)
        tabs[ConstData.TAB_ASSET] = HomeTab(getString(R.string.home_tab_asset), R.drawable.home_tab_assets, HomePageAssetsFragment::class.java)
        for (i in tabs.indices) {
            val tab = tabs[i]!!
            val indicator = View.inflate(applicationContext, R.layout.home_page_tab_indicator, null)
            //            View indicator = layoutInflater.inflate(R.layout.home_page_tab_indicator, null);
            val iconView = indicator.findViewById<ImageView>(R.id.icon)
            val tvIndicator = indicator.findViewById<TextView>(R.id.tv_indicator)
            iconView.setImageResource(tab.topIconId)
            //            Drawable drawable = SkinCompatResources.getDrawable(this, tab.topIconId);
//
//            tv_indicator.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
            tvIndicator.setTextColor(SkinCompatResources.getColorStateList(this, R.color.home_tab_text_color))
            tvIndicator.text = tab.tabName
            tab.setIndicatorTextView(tvIndicator)
            val tabSpec = tabHost?.newTabSpec(tab.tabName)?.setIndicator(indicator)
            if (i == 2) {
                tabSpec?.let { tabHost?.addTab(it, tab.fragmentClass, transactionExtras) }
            } else {
                tabSpec?.let { tabHost?.addTab(it, tab.fragmentClass, null) }
            }
        }
        //资产fragment检查登录状态
        tabHost?.tabWidget?.getChildAt(ConstData.TAB_ASSET)?.setOnClickListener{
            if(CookieUtil.getUserInfo(mContext) == null){
                BlackRouter.getInstance().build(RouterConstData.LOGIN).go(mContext)
            }else{
                tabHost?.currentTab = ConstData.TAB_ASSET
                tabHost?.tabWidget?.requestFocus(View.FOCUS_FORWARD)
            }
        }
        PairStatusPopupWindow.reset()
        val routeFragmentIndex = intent.getIntExtra("routeFragmentIndex", -1)
        if (routeFragmentIndex != -1) {
            tabHost?.currentTab = routeFragmentIndex
        }
        showOpenNotificationDialog()
        UdeskUtil.initUdesk(applicationContext)
        //获取所有配置币种并且缓存
        WalletApiServiceHelper.getCoinInfoConfigAndCache(this, null)
        //获取所有现货交易对数据并缓存
        SocketDataContainer.initAllPairStatusData(this)
        /**
         * 先获取所有交易对，然后获取行情数据，并且组合到交易对数据中去
         */
        val coinBaseCallback: Callback<ArrayList<PairStatus?>?> =
            object : Callback<ArrayList<PairStatus?>?>() {
                override fun error(type: Int, error: Any) {
                    Log.d("666666","coinBaseCallback,error")
                }
                override fun callback(returnData: ArrayList<PairStatus?>?) {
                    Log.d("666666","coinBaseCallback,callback")
                    SocketDataContainer.cacheFuturePairStatusData(mContext)
                    SocketUtil.sendSocketCommandBroadcast(mContext,SocketUtil.COMMAND_FUTURE_TICKERS_START)
                }
            }
        val uBaseCallback: Callback<ArrayList<PairStatus?>?> =
            object : Callback<ArrayList<PairStatus?>?>() {
                override fun error(type: Int, error: Any) {
                    Log.d("666666","uBaseCallback,error")
                }
                override fun callback(returnData: ArrayList<PairStatus?>?) {
                    Log.d("666666","uBaseCallback,callback")
                    //获取合约币本位交易对行情数据
                    SocketDataContainer.getFuturesPairsWithSet(mContext,ConstData.PairStatusType.FUTURE_COIN,coinBaseCallback)
                }
            }
        //获取合约所有交易对数据并缓存
        SocketDataContainer.initAllFutureSymbolList(this,object :Callback<ArrayList<PairStatus>?>(){
            override fun callback(returnData: ArrayList<PairStatus>?) {
                Log.d("666666","initAllFutureSymbolList,callback")
                //获取合约U本位交易对行情数据
                SocketDataContainer.getFuturesPairsWithSet(mContext,ConstData.PairStatusType.FUTURE_U,uBaseCallback)
            }

            override fun error(type: Int, error: Any?) {
                Log.d("666666","initAllFutureSymbolList,error")
            }
        })
        checkUpdate(true)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun onResume() {
        super.onResume()
        resetStatusBarTheme(!CookieUtil.getNightMode(mContext))
        (mContext as HomePageActivity).resetSkinResources()
        (application as FryingApplication).initFilters()
        System.gc()
        getUserInfo(object : Callback<UserInfo?>() {
            override fun error(type: Int, error: Any) {
//                (application as FryingApplication).xgBind()
//                (application as FryingApplication).jPushBind();
            }

            override fun callback(userInfo: UserInfo?) {
//                (application as FryingApplication).xgBind()
//                (application as FryingApplication).jPushBind();
                if (userInfo?.id != null) {
                    val userIdHeader = IMHelper.getUserIdHeader(mContext)
                    IMHelper.imLogin(mContext, userIdHeader + userInfo.id, object : Callback<Boolean?>() {
                        override fun callback(returnData: Boolean?) {}
                        override fun error(type: Int, error: Any) {}
                    })
                }
            }

        })
        //获取c2c usdt价格
        C2CApiServiceHelper.getC2CPrice(mContext,object :Callback<C2CPrice?>(){
            override fun callback(returnData: C2CPrice?) {

            }

            override fun error(type: Int, error: Any?) {
            }

        })
        //判断socket的service 是否存活
        if (!CommonUtil.isServiceWorked(this, SocketService::class.java)) {
            startSocketService()
        }
        if (CookieUtil.getUserInfo(mContext) != null) {
            sendLoginBroadcast(SocketUtil.COMMAND_USER_LOGIN)
        }
//        getDialogAd()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        closeSocketService()
    }

    private fun startSocketService() {
        val socketServiceIntent = Intent(this, SocketService::class.java)
        socketServiceIntent.setPackage(packageName)
        //            startService(socketServiceIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(socketServiceIntent)
        } else {
            startService(socketServiceIntent)
        }
    }

    fun closeSocketService() {
        val socketServiceIntent = Intent(this, SocketService::class.java)
        socketServiceIntent.setPackage(packageName)
        stopService(socketServiceIntent)
    }

    // 连续点击两次返回键关闭程序
    override fun onBackPressed() {
        if (backPressedToExitOnce) {
            super.onBackPressed()
        } else {
            backPressedToExitOnce = true
            FryingUtil.showToast(this, getString(R.string.press_again_goback))
            Handler().postDelayed({ backPressedToExitOnce = false }, 2000)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        //判断socket的service 是否存活
        if (!CommonUtil.isServiceWorked(this, SocketService::class.java)) {
            val socketServiceIntent = Intent(this, SocketService::class.java)
            socketServiceIntent.setPackage(packageName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(socketServiceIntent)
            } else {
                startService(socketServiceIntent)
            }
        }
        val extras = intent?.extras
        if (extras != null && extras.containsKey("routeUri")) {
            BlackRouter.getInstance().build(extras.getString("routeUri")).with(extras).go(this)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
        }
    }

    private fun getDialogAd() {
        val language = LanguageUtil.getLanguageSetting(this)
        val lang = if (language != null && language.languageCode == 4) "4" else "1"
        CommonApiServiceHelper.getGlobalAd(this, lang, "top", object : NormalCallback<HttpRequestResultData<GlobalAd?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {}
            override fun callback(returnData: HttpRequestResultData<GlobalAd?>?) {
                if (returnData != null && returnData.code == 0 && returnData.data != null) {
                    val globalAd = returnData.data
                    if (currentDemandAdId == null || currentDemandAdId != globalAd!!.id) {
                        currentDemandAdId = globalAd!!.id
                        val displayMetrics = resources.displayMetrics
                        //來去图片并显示
                        DownloadServiceHelper.downloadImage(mContext, globalAd.content, (311 * displayMetrics.density).toInt(), (445 * displayMetrics.density).toInt(), false, object : NormalCallback<Bitmap?>(mContext!!) {
                            override fun error(type: Int, error: Any?) {}
                            override fun callback(returnData: Bitmap?) {
                                returnData?.let { showDialogAd(it, globalAd) }
                            }
                        })
                    }
                }
            }
        })
    }

    private fun showDialogAd(contentBitmap: Bitmap?, globalAd: GlobalAd?) {
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_global_ad, null)
        val closeView = contentView.findViewById<ImageView>(R.id.close)
        val actionView = contentView.findViewById<ImageView>(R.id.action)
        if (contentBitmap != null) {
            actionView.setImageBitmap(contentBitmap)
        }
        val alertDialog = AlertDialog.Builder(mContext, R.style.AlertDialog).setView(contentView).create()
        alertDialog.setCancelable(false)
        closeView.setOnClickListener { alertDialog.dismiss() }
        actionView.setOnClickListener {
            //点击跳转
            val bundle = Bundle()
            bundle.putString(ConstData.TITLE, globalAd!!.title)
            bundle.putString(ConstData.URL, globalAd.url)
            BlackRouter.getInstance().build(globalAd.url).with(bundle).go(mContext)
            alertDialog.dismiss()
        }
        alertDialog.setOnDismissListener { contentBitmap?.recycle() }
        alertDialog.show()
    }

    override fun openFragment(fragmentClass: Class<*>?, fragmentIndex: Int, extras: Bundle?) {
        if (fragmentIndex != -1) {
            tabHost!!.currentTab = fragmentIndex
            if (HomePageTransactionFragmentFiex::class.java.isAssignableFrom(fragmentClass!!)) {
                transactionIndex = extras?.getInt(ConstData.TRANSACTION_INDEX, -1) ?: -1
                transactionTabType = extras?.getInt(ConstData.TRANSACTION_TYPE, -1) ?: -1
                if (transactionIndex == 1 || transactionIndex == 2) {
                    val tabName = tabs[fragmentIndex]!!.tabName
                    val homePageTransactionFragmentFiex = supportFragmentManager.findFragmentByTag(tabs[fragmentIndex]!!.tabName) as HomePageTransactionFragmentFiex?
                    if (homePageTransactionFragmentFiex != null) {
                        homePageTransactionFragmentFiex.setTransactionType(transactionIndex)
                        homePageTransactionFragmentFiex.setTransactionTabType(transactionTabType)
                    } else {
                        transactionExtras.putInt(ConstData.TRANSACTION_INDEX, transactionIndex)
                        transactionExtras.putInt(ConstData.TRANSACTION_TYPE, transactionTabType)
                    }
                }
            }
        }
    }

    //当已经打开了的fragment在换肤过程中，部分元素不能够切换资源，需要手动调用
    fun resetSkinResources() {
        val mainFragment = supportFragmentManager.findFragmentByTag(tabs[0]!!.tabName) as HomePageMainFragmentFiex?
//        mainFragment?.resetSkinResources()
        val quotationFragment = supportFragmentManager.findFragmentByTag(tabs[1]!!.tabName) as HomePageQuotationFragmentMain?
//        quotationFragment?.resetSkinResources()
        val transactionFragment = supportFragmentManager.findFragmentByTag(tabs[2]!!.tabName) as HomePageTransactionFragmentFiex?
//        transactionFragment?.resetSkinResources()
//        val moneyFragment = supportFragmentManager.findFragmentByTag(tabs[3]!!.tabName) as HomePageMoneyFragment?
//        moneyFragment?.resetSkinResources()
    }

    private fun checkUpdate(silent: Boolean) {
        if (CommonUtil.isApkInDebug(this)) {
            return
        }
        CommonApiServiceHelper.checkUpdate(this, !silent, object : Callback<HttpRequestResultData<Update?>?>() {
            override fun error(type: Int, error: Any) {}
            override fun callback(returnData: HttpRequestResultData<Update?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    val update = returnData.data ?: return
                    if (update.version != null && update.version != CommonUtil.getVersionName(mContext, null)) {
                        //需要更新
                        if ((update.force != null && true == update.force)
                                || update.version != CookieUtil.getUpdateJumpVersion(mContext)) {
                            FryingUtil.showUpdateDialog(mContext as Activity, update)
                        }
                    } else {
                        if (!silent) {
                            FryingUtil.showToast(mContext, getString(R.string.last_version))
                        }
                    }
                }
            }
        })
    }

    private fun showOpenNotificationDialog() {
        if (!CommonUtil.isNotificationEnabled(this)) {
            val notificationDialog = AlertDialog.Builder(this)
                    .setTitle("友情提示").setMessage("APP需要开启通知权限，鉴于您禁用相关权限，请手动设置开启权限以获得更好体验。")
                    .setNegativeButton("取消") { dialog, which -> dialog.dismiss() }.setPositiveButton("设置") { dialog, which ->
                        //跳转设置界面
                        CommonUtil.gotoSet(mContext)
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .create()
            notificationDialog.setCanceledOnTouchOutside(true)
            notificationDialog.show()
        }
    }
}