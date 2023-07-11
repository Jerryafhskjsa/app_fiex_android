package com.black.frying.activity

import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.core.view.drawToBitmap
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.CommonApiServiceHelper
import com.black.base.api.UserApiServiceHelper
import com.black.base.lib.FryingSingleToast
import com.black.base.model.FryingLinesConfig
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.user.UserInfo
import com.black.base.util.*
import com.black.base.view.DeepControllerWindow
import com.black.frying.service.SocketService
import com.black.frying.util.UdeskUtil
import com.black.net.HttpCookieUtil
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ActivityMineBinding
import com.google.zxing.WriterException
import kotlinx.android.synthetic.main.activity_mine.fenxiang
import skin.support.SkinCompatManager
import skin.support.SkinCompatManager.SkinLoaderListener
import skin.support.content.res.SkinCompatResources
import java.util.Calendar

//我的界面
@Route(value = [RouterConstData.MINE])
class MineActivity : BaseActionBarActivity(), View.OnClickListener {
    companion object {
        private val TAG = MineActivity::class.java.simpleName
    }

    private var userInfo: UserInfo? = null
    private var url : String? = null
    private val fryingLinesConfig: MutableList<FryingLinesConfig?> = ArrayList()
    private val localLinesConfig: MutableList<FryingLinesConfig?> = ArrayList()
    private var currentServerConfig: FryingLinesConfig? = null
    private var imageLoader: ImageLoader? = null
    private var binding: ActivityMineBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_mine);
        binding?.bangzu?.setOnClickListener(this)
        binding?.tongyong?.setOnClickListener(this)
        binding?.fenxiang?.setOnClickListener(this)
        binding?.relVerifyStatus?.setOnClickListener(this)
        binding?.xianlu?.setOnClickListener(this)
        binding?.yijian?.setOnClickListener(this)
        imageLoader = ImageLoader(this)
        binding?.imgBack?.setOnClickListener(this)
        binding?.setting?.setOnClickListener(this)
        binding?.btnLogin?.setOnClickListener(this)
        binding?.btnLoginOut?.setOnClickListener(this)
        binding?.name?.setOnClickListener(this)
        binding?.userLayout?.setOnClickListener(this)
        binding?.uid?.setOnClickListener(this)
        binding?.darkMode?.setOnClickListener(this)
        binding?.lightMode?.setOnClickListener(this)
        binding?.nightModeToggle?.setOnCheckedChangeListener { _, isChecked ->
            val isNightMode = CookieUtil.getNightMode(mContext)
            //选中表示黑夜
            if (isNightMode != isChecked) {
                if (isChecked) {
                    SkinCompatManager.getInstance()
                        .loadSkin("night.skin", object : SkinLoaderListener {
                            override fun onStart() {}
                            override fun onSuccess() {
                                CookieUtil.setNightMode(mContext, true)
                                refreshNighModeViews(true)
                            }

                            override fun onFailed(errMsg: String) {
                                CookieUtil.setNightMode(mContext, false)
                                refreshNighModeViews(false)
                            }
                        }, SkinCompatManager.SKIN_LOADER_STRATEGY_ASSETS)
                } else {
                    SkinCompatManager.getInstance().restoreDefaultTheme()
                    CookieUtil.setNightMode(mContext, false)
                    refreshNighModeViews(false)
                }
            }
        }
        refreshNighModeViews(CookieUtil.getNightMode(mContext))
        binding?.notifications?.setOnClickListener(this)
        binding?.safeCenter?.setOnClickListener(this)
        binding?.helpCenter?.setOnClickListener(this)
        binding?.moreLanguage?.setOnClickListener(this)
        binding?.exchangeRates?.setOnClickListener(this)
        binding?.version?.setText(String.format("V%s",CommonUtil.getVersionName(mContext,"1.2.0")))
        val currentLanguage = LanguageUtil.getLanguageSetting(mContext)?.languageCode
        val exchange = ExchangeRatesUtil.getExchangeRatesSetting(mContext)?.rateCode
        if (currentLanguage == null || currentLanguage == 2 || currentLanguage == 0) {
            binding?.currentLanguage?.setText(R.string.language_english)
        } else {
            binding?.currentLanguage?.setText(R.string.language_vietnam)
        }
        if (exchange == null || exchange == 1) {
            binding?.currentExchangeRates?.setText(R.string.exchange_rates_usd)
        }
        else {
            binding?.currentExchangeRates?.setText(R.string.language_cny)
        }
        if (CommonUtil.isApkInDebug(applicationContext)) {
            binding?.serverSetting?.visibility = View.GONE
        } else {
            binding?.serverSetting?.visibility = View.GONE
        }
        binding?.serverSetting?.setOnClickListener(this)
        binding?.setting?.setOnClickListener(this)
        //initServiceApi()
        refreshUserViews()
        getNetworkLines(false)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }


    private fun getNetworkLines(showDialog: Boolean?) {
        CommonApiServiceHelper.getNetworkLines(
            this,
            object : Callback<HttpRequestResultDataList<FryingLinesConfig?>?>() {
                override fun error(type: Int, error: Any) {
                    if (localLinesConfig.size > 0) {
                        getLineSpeed(0, 0, showDialog, localLinesConfig[0])
                    }
                }

                override fun callback(returnData: HttpRequestResultDataList<FryingLinesConfig?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        var lines = returnData.data ?: return
                        fryingLinesConfig.clear()
                        var lineUrls:ArrayList<String?> = ArrayList()
                        for (i in lines.indices){
                            var config = lines[i]
                            config?.index = i
                            fryingLinesConfig.add(i,config)
                            lineUrls.add(config?.lineUrl)
                        }
                        if(fryingLinesConfig.size > 0){
                            CookieUtil.setServerHost(mContext,lineUrls)
                            getLineSpeed(0, 1, showDialog, fryingLinesConfig[0])
                        }
                    }
                }
            })
    }

    /**
     * type 0本地 1网络
     */
    private fun getLineSpeed(
        index: Int?,
        netType: Int?,
        showDialog: Boolean?,
        linesConfig: FryingLinesConfig?
    ) {
        linesConfig?.startTime = System.currentTimeMillis()
        CommonApiServiceHelper.getLinesSpeed(
            this,
            linesConfig?.lineUrl,
            object : Callback<HttpRequestResultString?>() {
                override fun error(type: Int, error: Any) {
                    linesConfig?.statusDes = getString(R.string.link_line_exception)
                    linesConfig?.endTime = Long.MAX_VALUE
                    when (netType) {
                        0 -> {
                            if (localLinesConfig.size > 0 && localLinesConfig.size-1 == index) {
                                if (showDialog == true) {
                                    showServerDialog(netType)
                                }
                                displayCurrentServer()
                            }else{
                                if (index != null) {
                                    getLineSpeed(
                                        index + 1,
                                        netType,
                                        showDialog,
                                        localLinesConfig[index + 1]
                                    )
                                }
                            }
                        }
                        1 -> {
                            if (fryingLinesConfig.size > 0 && fryingLinesConfig.size-1  == index) {
                                if (showDialog == true) {
                                    showServerDialog(netType)
                                }
                                displayCurrentServer()
                            }else{
                                if (index != null) {
                                    getLineSpeed(
                                        index + 1,
                                        netType,
                                        showDialog,
                                        fryingLinesConfig[index + 1]
                                    )
                                }
                            }
                        }
                    }
                }

                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        linesConfig?.endTime = System.currentTimeMillis()
                        linesConfig?.statusDes = getString(R.string.link_line_normal)
                        when (netType) {
                            0 -> {
                                if (localLinesConfig.size > 0 && localLinesConfig.size-1  == index) {
                                    if (showDialog == true) {
                                        showServerDialog(netType)
                                    }
                                    displayCurrentServer()
                                }else{
                                    if (index != null) {
                                        getLineSpeed(
                                            index + 1,
                                            netType,
                                            showDialog,
                                            localLinesConfig[index + 1]
                                        )
                                    }
                                }
                            }
                            1 -> {
                                if (fryingLinesConfig.size > 0 && fryingLinesConfig.size-1  == index) {
                                    if (showDialog == true) {
                                        showServerDialog(netType)
                                    }
                                    displayCurrentServer()
                                }else{
                                    if (index != null) {
                                        getLineSpeed(
                                            index + 1,
                                            netType,
                                            showDialog,
                                            fryingLinesConfig[index + 1]
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            })
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.img_back -> finish()
            R.id.btn_login -> {
                BlackRouter.getInstance().build(RouterConstData.LOGIN).go(mContext)
            }
            R.id.user_layout, R.id.name -> {
                val thisUserInfo = CookieUtil.getUserInfo(mContext)
                if (thisUserInfo == null) {
                    BlackRouter.getInstance().build(RouterConstData.LOGIN).go(mContext)
                } else {
                    BlackRouter.getInstance().build(RouterConstData.PERSON_INFO_CENTER).go(mContext)
                }
            }
            R.id.uid -> if (CommonUtil.copyText(
                    mContext,
                    if (userInfo!!.id == null) "" else userInfo!!.id
                )
            ) {
                FryingUtil.showToast(mContext, mContext.getString(R.string.copy_text_success))
            } else {
                FryingUtil.showToast(mContext, mContext.getString(R.string.copy_text_failed))
            }
            R.id.wallet -> BlackRouter.getInstance().build(RouterConstData.WALLET).go(mContext)
            R.id.bill_manage -> requestStoragePermissions(Runnable {
                val bundle = Bundle()
                bundle.putInt(ConstData.OPEN_TYPE, 1)
                BlackRouter.getInstance().build(RouterConstData.ENTRUST_RECORDS_NEW).with(bundle)
                    .go(mContext)
            })
            R.id.recommend -> BlackRouter.getInstance().build(RouterConstData.RECOMMEND)
                .go(mContext)
            R.id.consult ->  //客服咨询
                UdeskUtil.start(applicationContext)
            R.id.safe_center -> BlackRouter.getInstance().build(RouterConstData.SAFE_CENTER)
                .go(mContext)
            R.id.bangzu -> {
                //帮助中心
//                BlackRouter.getInstance().build(RouterConstData.PROMOTIONS).go(mContext);
                val bundle = Bundle()
                bundle.putString(ConstData.TITLE, mContext.getString(R.string.help_center))
                bundle.putString(ConstData.URL, UrlConfig.HELP_CENTER)
                BlackRouter.getInstance().build(RouterConstData.WEB_VIEW).with(bundle).go(mContext)
            }
            R.id.notifications -> BlackRouter.getInstance().build(RouterConstData.NOTIFICATION_LIST)
                .go(mContext)
            R.id.server_setting -> {
                if (fryingLinesConfig.size > 0) {
                    getLineSpeed(0, 1, true, fryingLinesConfig[0])
                }
                else if (localLinesConfig.size > 0) {
                    getLineSpeed(0, 0, true, localLinesConfig[0])
                }
            }
            R.id.more_language -> BlackRouter.getInstance().build(RouterConstData.LANGUAGE_SETTING)
                .go(mContext)
            R.id.exchange_rates -> BlackRouter.getInstance().build(RouterConstData.EXCHANGE_RATES)
                .go(mContext)
            R.id.setting -> BlackRouter.getInstance().build(RouterConstData.USER_SETTING)
                .go(mContext)
            R.id.info -> BlackRouter.getInstance().build(RouterConstData.ABOUT).go(mContext)
            R.id.dark_mode -> {
                setDarkMode()
            }
            R.id.light_mode -> {
                setDarkMode()
            }
            R.id.btn_login_out -> {
                if (userInfo != null) {
                    showLogoutDialog(View.OnClickListener {
                        val doLogout = Runnable {
                            FryingUtil.clearAllUserInfo(mContext)
                            sendSocketCommandChangedBroadcast(SocketUtil.COMMAND_USER_LOGOUT)
                            finish()
                        }
                        UserApiServiceHelper.logout(
                            mContext,
                            object : Callback<HttpRequestResultString?>() {
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
            R.id.tongyong -> {
                BlackRouter.getInstance().build(RouterConstData.USER_SETTING).go(mContext)
            }
            R.id.fenxiang -> {
                fenxiangdialog()
            }
            R.id.xianlu -> {
                BlackRouter.getInstance().build(RouterConstData.XIANLU)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .go(mContext)
            }
            R.id.rel_verify_status -> {
                    BlackRouter.getInstance().build(RouterConstData.REAL_NAME_RESULT).go(mContext)
            }
            R.id.yijian -> {
                BlackRouter.getInstance().build(RouterConstData.QUESTION_ACTIVITY).go(mContext)
            }
        }
    }

    private fun showServerDialog(type: Int?) {
        var displayList: MutableList<FryingLinesConfig?>? = null
        when (type) {
            0 -> displayList = localLinesConfig
            1 -> displayList = fryingLinesConfig
        }
        DeepControllerWindow(mContext as Activity,
            getString(R.string.server_setting),
            currentServerConfig,
            displayList,
            object : DeepControllerWindow.OnReturnListener<FryingLinesConfig?> {
                override fun onReturn(
                    window: DeepControllerWindow<FryingLinesConfig?>,
                    item: FryingLinesConfig?
                ) {
                    CookieUtil.setHostIndex(mContext, item?.index!!)
                    if (item.lineUrl != currentServerConfig?.lineUrl) {
                        displayCurrentServer()
                        currentServerConfig = item
                        CookieUtil.deleteUserInfo(mContext)
                        HttpCookieUtil.deleteCookies(mContext)
                        CookieUtil.deleteToken(mContext)
                        CookieUtil.setAccountProtectType(mContext, ConstData.ACCOUNT_PROTECT_NONE)
                        CookieUtil.setGesturePassword(mContext, null)
                        //CookieUtil.setAccountProtectJump(mContext, false);
                        CookieUtil.saveUserId(mContext, null)
                        CookieUtil.saveUserName(mContext, null)
                        closeSocketService()
                        BlackRouter.getInstance().build(RouterConstData.START_PAGE)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .go(mContext) { routeResult, error ->
                                if (routeResult) {
                                }
                            }
                    }
                }
            }).show()
    }

    private fun setDarkMode() {
        var isNightMode = CookieUtil.getNightMode(mContext)
        //选中表示黑夜
        isNightMode = !isNightMode
        if (isNightMode) {
            SkinCompatManager.getInstance().loadSkin("night.skin", object : SkinLoaderListener {
                override fun onStart() {}
                override fun onSuccess() {
                    CookieUtil.setNightMode(mContext, true)
                    refreshNighModeViews(true)
                }

                override fun onFailed(errMsg: String) {
                    CookieUtil.setNightMode(mContext, false)
                    refreshNighModeViews(false)
                }
            }, SkinCompatManager.SKIN_LOADER_STRATEGY_ASSETS)
        } else {
            SkinCompatManager.getInstance().restoreDefaultTheme()
            CookieUtil.setNightMode(mContext, false)
            refreshNighModeViews(false)
        }
    }

    private fun showLogoutDialog(resumeClickListener: View.OnClickListener?) {
        val contentView = LayoutInflater.from(mContext)
            .inflate(com.black.user.R.layout.dialog_logout_resume, null)
        val alertDialog = Dialog(mContext, com.black.user.R.style.AlertDialog)
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
            window.setWindowAnimations(com.black.user.R.style.anim_bottom_in_out)
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams =
            ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        alertDialog.setContentView(contentView, layoutParams)
        //        dialog.setContentView(viewDialog, layoutParams);
        contentView.findViewById<View>(com.black.user.R.id.btn_resume).setOnClickListener { v ->
            alertDialog.dismiss()
            resumeClickListener?.onClick(v)
        }
        contentView.findViewById<View>(com.black.user.R.id.btn_cancel)
            .setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    override fun onResume() {
        super.onResume()
        userInfo = CookieUtil.getUserInfo(this)
        if(userInfo != null){
            refreshUserViews()
        }else{
            binding?.btnLoginOut?.visibility = View.GONE
            reloadUserInfo()
        }
        fryingHelper.onResume()
    }

    fun closeSocketService() {
        val socketServiceIntent = Intent(this, SocketService::class.java)
        socketServiceIntent.setPackage(packageName)
        stopService(socketServiceIntent)
    }

    private fun initServiceApi() {
        for (i in UrlConfig.HOSTS.indices) {
            val hostUrl = UrlConfig.HOSTS
            var fryingLinesConfig = FryingLinesConfig()
            fryingLinesConfig.lineUrl = hostUrl
            fryingLinesConfig.index = i
            when (i) {
                0 -> fryingLinesConfig.zh = getString(R.string.link_line_one)
                1 -> fryingLinesConfig.zh = getString(R.string.link_line_two)
            }
            localLinesConfig.add(fryingLinesConfig)
        }
    }

    private fun displayCurrentServer() {
        currentServerConfig = if (fryingLinesConfig.size > 0) {
            CommonUtil.getItemFromList(fryingLinesConfig, UrlConfig.getIndex(mContext))
        } else {
            CommonUtil.getItemFromList(localLinesConfig, UrlConfig.getIndex(mContext))
        }
        Log.d("666666","displayCurrentServer url = "+currentServerConfig?.lineUrl)
        Log.d("666666","displayCurrentServer index = "+currentServerConfig?.index)
        val serverText = currentServerConfig?.speed
        binding?.currentServer?.text = serverText
    }

    private fun refreshNighModeViews(isNighMode: Boolean) {
        if (isNighMode) {
            binding?.nightModeToggle?.isChecked = true
            binding?.night?.visibility = View.VISIBLE
            binding?.darkModeImg?.setImageDrawable(resources.getDrawable(R.drawable.dark_mode_selected))
            binding?.dayModeImg?.setImageDrawable(resources.getDrawable(R.drawable.day_mode_normal))
        } else {
            binding?.nightModeToggle?.isChecked = false
            binding?.night?.visibility = View.GONE
            binding?.darkModeImg?.setImageDrawable(resources.getDrawable(R.drawable.dark_mode_normal))
            binding?.dayModeImg?.setImageDrawable(resources.getDrawable(R.drawable.day_mode_selected))
        }
        resetStatusBarTheme(!isNighMode)
    }

    //拉取用户数据
    private fun reloadUserInfo() {
        if (CookieUtil.getUserInfo(applicationContext) != null) {
            getUserInfo(object : Callback<UserInfo?>() {
                override fun callback(result: UserInfo?) {
                    if (result != null) {
                        userInfo = result
                        refreshUserViews()
                    }
                }

                override fun error(type: Int, error: Any) {}
            })
        }
    }

    //刷新用户信息
    private fun refreshUserViews() {
        //binding?.name?.setTextColor(SkinCompatResources.getColor(mContext, R.color.T1))
        if (userInfo != null) {
            if(userInfo?.headPortrait != null){
                binding?.iconAvatar?.let {
                    Glide.with(mContext)
                        .load(Uri.parse(userInfo?.headPortrait!!))
                        .apply(RequestOptions.bitmapTransform(CircleCrop()).error(R.drawable.gerenzhongxin))
                        .into(it)
                }
            }
            val userName = if (userInfo!!.username == null) "" else userInfo!!.username
            binding?.name?.text = String.format("%s", userName)
            var relVerifyBg: Drawable? = null
            var tvVerifyColor: Int? = null
            var tvVerifyText: String? = null
           /* when (userInfo?.idNoStatus) {
                ConstData.USER_VERIFY_NO -> {
                    relVerifyBg = getDrawable(R.drawable.bg_user_unverify_corner)
                    tvVerifyColor = getColor(R.color.T8)
                    tvVerifyText = getString(R.string.person_unchecked)
                }
                ConstData.USER_VERIFY_ED -> {
                    relVerifyBg = getDrawable(R.drawable.bg_user_verify_corner)
                    tvVerifyColor = getColor(R.color.T8)
                    tvVerifyText = getString(R.string.person_checked)
                }
                ConstData.USER_VERIFY_ING -> {
                    relVerifyBg = getDrawable(R.drawable.bg_user_under_verify_corner)
                    tvVerifyColor = getColor(R.color.T15)
                    tvVerifyText = getString(R.string.person_checking)
                }
                ConstData.USER_VERIFY_FAIL -> {
                    relVerifyBg = getDrawable(R.drawable.bg_user_verify_fail_corner)
                    tvVerifyColor = getColor(R.color.T10)
                    tvVerifyText = getString(R.string.purchase_failed)
                }
            }

            */
           /* binding?.relVerifyStatus?.background = relVerifyBg
            binding?.tvVerify?.text = tvVerifyText
            if (tvVerifyColor != null) {
                binding?.tvVerify?.setTextColor(tvVerifyColor)
            }
            
            */

            binding?.uuid?.visibility = View.VISIBLE
            binding?.relVerifyStatus?.visibility = View.VISIBLE
            binding?.uuid?.text = "UID:" + if (userInfo!!.id == null) "" else userInfo!!.id
            /*if (TextUtils.equals("email", userInfo!!.registerFrom)) {
                if (TextUtils.equals(userInfo!!.phoneSecurityStatus, "1") && TextUtils.equals(
                        userInfo!!.emailSecurityStatus,
                        "1"
                    ) && TextUtils.equals(userInfo!!.googleSecurityStatus, "1")
                ) {
                    binding?.safeLevel?.setText(R.string.level_high)
                } else if (TextUtils.equals(userInfo!!.emailSecurityStatus, "1")
                    && (TextUtils.equals(userInfo!!.phoneSecurityStatus, "1") || TextUtils.equals(
                        userInfo!!.googleSecurityStatus,
                        "1"
                    ))
                ) {
                    binding?.safeLevel?.setText(R.string.level_middle)
                } else {
                    binding?.safeLevel?.setText(R.string.level_low)
                }
            } else {
                if (TextUtils.equals(userInfo!!.phoneSecurityStatus, "1") && TextUtils.equals(
                        userInfo!!.emailSecurityStatus,
                        "1"
                    ) && TextUtils.equals(userInfo!!.googleSecurityStatus, "1")
                ) {
                    binding?.safeLevel?.setText(R.string.level_high)
                } else if (TextUtils.equals(
                        userInfo!!.phoneSecurityStatus,
                        "1"
                    ) && TextUtils.equals(userInfo!!.emailSecurityStatus, "1")
                ) {
                    binding?.safeLevel?.setText(R.string.level_middle)
                } else {
                    binding?.safeLevel?.setText(R.string.level_low)
                }
            }*/
            binding?.btnLoginOut?.visibility = View.VISIBLE
            binding?.loginStatus?.visibility = View.VISIBLE
            binding?.btnLogin?.visibility = View.GONE
        } else {
            binding?.name?.setText(R.string.please_login)
            binding?.uuid?.setText(R.string.denglutishi)
           // binding?.safeLevel?.text = ""
            binding?.btnLoginOut?.visibility = View.GONE
            binding?.btnLogin?.visibility = View.GONE
        }
    }

    override fun getUserInfo(callBack: Callback<UserInfo?>?) {
        if (CookieUtil.getToken(mContext) == null) {
            callBack?.callback(null)
            return
        }
        UserApiServiceHelper.getUserInfo(
            mContext,
            false,
            object : Callback<HttpRequestResultData<UserInfo?>?>() {
                override fun error(type: Int, error: Any) {
                    callBack?.error(0, error)
                }

                override fun callback(returnData: HttpRequestResultData<UserInfo?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        //获取信息成功，保存信息并跳转主页
                        CookieUtil.saveUserInfo(mContext, returnData.data)
                        callBack?.callback(returnData.data)
                    } else {
                        callBack?.error(
                            0,
                            mContext.getString(R.string.alert_login_failed_try_again)
                        )
                    }
                }
            })
    }

    private fun fenxiangdialog(){
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.haibao_dialog, null)
        val dialog = Dialog(mContext, com.black.wallet.R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.CENTER
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams =
            ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        url = UrlConfig.getHost(mContext) + "/auth/register/" + CookieUtil.getUserInfo(mContext)?.inviteCode
        val uri = Uri.parse(url)
        //val secret = uri.getQueryParameter("secret") //解析参数
        if (!TextUtils.isEmpty(url)) { //显示密钥，并进行下一步
            var qrcodeBitmap: Bitmap? = null
            try {
                qrcodeBitmap = CommonUtil.createQRCode(url, 72, 0)
            } catch (e: WriterException) {
                CommonUtil.printError(mContext, e)
            }
            dialog.findViewById<ImageView>(R.id.image).setImageBitmap(qrcodeBitmap)
        }
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.btn_resume).setOnClickListener { v ->
            //获取view缓存
            dialog.window!!.decorView.isDrawingCacheEnabled = true
            dialog.window!!.decorView.buildDrawingCache()
            val bmp: Bitmap =  dialog.window?.decorView!!.drawingCache
            val contentResolver: ContentResolver = mContext.contentResolver
            //这里"IMG"+ Calendar.getInstance().time如果没有可能会出现报错
            val uri = Uri.parse(
                MediaStore.Images.Media.insertImage(
                    contentResolver,
                    bmp,
                    "IMG" + Calendar.getInstance().time,
                    null
                )
            )
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/jpeg"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(Intent.createChooser(intent, "分享"))
            dialog.dismiss()
        }
    }


}