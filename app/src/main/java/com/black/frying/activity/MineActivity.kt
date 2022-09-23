package com.black.frying.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.UserApiServiceHelper
import com.black.base.model.FryingServerConfig
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.user.UserInfo
import com.black.base.util.*
import com.black.base.view.DeepControllerWindow
import com.black.frying.service.SocketService
import com.black.frying.util.UdeskUtil
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ActivityMineBinding
import skin.support.SkinCompatManager
import skin.support.SkinCompatManager.SkinLoaderListener
import skin.support.content.res.SkinCompatResources
import java.util.*

//我的界面
@Route(value = [RouterConstData.MINE])
class MineActivity : BaseActionBarActivity(), View.OnClickListener {
    private var userInfo: UserInfo? = null
    private val serverConfigs: ArrayList<FryingServerConfig> = ArrayList()
    private var imageLoader: ImageLoader? = null
    private var binding: ActivityMineBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_mine);
        imageLoader = ImageLoader(this)
        binding?.imgBack?.setOnClickListener(this)
        binding?.setting?.setOnClickListener(this)
        binding?.btnLogin?.setOnClickListener(this)
        binding?.btnLoginOut?.setOnClickListener(this)
        binding?.name?.setOnClickListener(this)
        binding?.userLayout?.setOnClickListener(this)
        binding?.uuid?.setOnClickListener(this)
        binding?.darkMode?.setOnClickListener(this)
        binding?.lightMode?.setOnClickListener(this)
        binding?.nightModeToggle?.setOnCheckedChangeListener { _, isChecked ->
            val isNightMode = CookieUtil.getNightMode(mContext)
            //选中表示黑夜
            if (isNightMode != isChecked) {
                if (isChecked) {
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
        }
        refreshNighModeViews(CookieUtil.getNightMode(mContext))
        binding?.notifications?.setOnClickListener(this)
        binding?.safeCenter?.setOnClickListener(this)
        binding?.helpCenter?.setOnClickListener(this)
        binding?.moreLanguage?.setOnClickListener(this)
        binding?.exchangeRates?.setOnClickListener(this)
        val currentLanguage = LanguageUtil.getLanguageSetting(applicationContext)
        if (currentLanguage == null) {
            binding?.currentLanguage?.setText(R.string.language_chinese)
        } else {
            binding?.currentLanguage?.setText(currentLanguage.languageText)
        }
        if (CommonUtil.isApkInDebug(applicationContext)) {
            binding?.serverSetting?.visibility = View.VISIBLE
            displayCurrentServer()
        } else {
            binding?.serverSetting?.visibility = View.GONE
        }
        binding?.serverSetting?.setOnClickListener(this)
        binding?.setting?.setOnClickListener(this)

        initServiceApi()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.img_back -> finish()
            R.id.btn_login ->{
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
            R.id.uuid -> if (CommonUtil.copyText(mContext, if (userInfo!!.id == null) "" else userInfo!!.id)) {
                FryingUtil.showToast(mContext, mContext.getString(R.string.copy_text_success))
            } else {
                FryingUtil.showToast(mContext, mContext.getString(R.string.copy_text_failed))
            }
            R.id.wallet -> BlackRouter.getInstance().build(RouterConstData.WALLET).go(mContext)
            R.id.bill_manage -> requestStoragePermissions(Runnable {
                val bundle = Bundle()
                bundle.putInt(ConstData.OPEN_TYPE, 1)
                BlackRouter.getInstance().build(RouterConstData.ENTRUST_RECORDS_NEW).with(bundle).go(mContext)
            })
            R.id.recommend -> BlackRouter.getInstance().build(RouterConstData.RECOMMEND).go(mContext)
            R.id.consult ->  //客服咨询
                UdeskUtil.start(applicationContext)
            R.id.safe_center -> BlackRouter.getInstance().build(RouterConstData.SAFE_CENTER).go(mContext)
            R.id.help_center -> {
                //帮助中心
//                BlackRouter.getInstance().build(RouterConstData.PROMOTIONS).go(mContext);
                val bundle = Bundle()
                bundle.putString(ConstData.TITLE, mContext.getString(R.string.help_center))
                bundle.putString(ConstData.URL, UrlConfig.HELP_CENTER)
                BlackRouter.getInstance().build(RouterConstData.WEB_VIEW).with(bundle).go(mContext)
            }
            R.id.notifications -> BlackRouter.getInstance().build(RouterConstData.NOTIFICATION_LIST).go(mContext)
            R.id.server_setting -> {
                //                LoadingDialog loadingDialog = FryingUtil.getLoadDialog(mContext, "");
//                loadingDialog.show();
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        loadingDialog.dismiss();
//                    }
//                }, 5000);
                DeepControllerWindow(mContext as Activity, getString(R.string.server_setting), currentServerConfig,
                        serverConfigs as List<FryingServerConfig?>?,
                        object : DeepControllerWindow.OnReturnListener<FryingServerConfig?> {
                            override fun onReturn(window: DeepControllerWindow<FryingServerConfig?>, item: FryingServerConfig?) {
                                if (item != currentServerConfig) {
                                    CookieUtil.setHostIndex(mContext, item!!.index)
                                    displayCurrentServer()
                                    CookieUtil.deleteUserInfo(mContext)
                                    CookieUtil.deleteToken(mContext)
                                    CookieUtil.setAccountProtectType(mContext, ConstData.ACCOUNT_PROTECT_NONE)
                                    CookieUtil.setGesturePassword(mContext, null)
                                    //                            CookieUtil.setAccountProtectJump(mContext, false);
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
            R.id.more_language -> BlackRouter.getInstance().build(RouterConstData.LANGUAGE_SETTING).go(mContext)
            R.id.exchange_rates -> BlackRouter.getInstance().build(RouterConstData.EXCHANGE_RATES).go(mContext)
            R.id.setting -> BlackRouter.getInstance().build(RouterConstData.USER_SETTING).go(mContext)
            R.id.info -> BlackRouter.getInstance().build(RouterConstData.ABOUT).go(mContext)
            R.id.dark_mode -> {
                setDarkMode()
            }
            R.id.light_mode -> {
                setDarkMode()
            }
            R.id.btn_login_out ->{
                if (userInfo != null) {
                    showLogoutDialog(View.OnClickListener {
                        val doLogout = Runnable {
                            FryingUtil.clearAllUserInfo(mContext)
                            sendSocketCommandChangedBroadcast(SocketUtil.COMMAND_USER_LOGOUT)
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

    private fun setDarkMode(){
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
        }else{
            SkinCompatManager.getInstance().restoreDefaultTheme()
            CookieUtil.setNightMode(mContext, false)
            refreshNighModeViews(false)
        }
    }

    private fun showLogoutDialog(resumeClickListener: View.OnClickListener?) {
        val contentView = LayoutInflater.from(mContext).inflate(com.black.user.R.layout.dialog_logout_resume, null)
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
        val layoutParams = ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        alertDialog.setContentView(contentView, layoutParams)
        //        dialog.setContentView(viewDialog, layoutParams);
        contentView.findViewById<View>(com.black.user.R.id.btn_resume).setOnClickListener { v ->
            alertDialog.dismiss()
            resumeClickListener?.onClick(v)
        }
        contentView.findViewById<View>(com.black.user.R.id.btn_cancel).setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    public override fun onResume() {
        super.onResume()
        userInfo = CookieUtil.getUserInfo(this)
        refreshUserViews()
        reloadUserInfo()
        fryingHelper.onResume()
    }

    fun closeSocketService() {
        val socketServiceIntent = Intent(this, SocketService::class.java)
        socketServiceIntent.setPackage(packageName)
        stopService(socketServiceIntent)
    }

    private fun initServiceApi() {
        for (i in UrlConfig.API_HOSTS.indices) {
            val apiHost = UrlConfig.API_HOSTS[i]
            serverConfigs.add(FryingServerConfig(i, apiHost))
        }
    }

    private fun displayCurrentServer() {
        val serverConfig = CommonUtil.getItemFromList(serverConfigs, UrlConfig.getIndex(mContext))
        val currentServer = if (serverConfig == null) "" else serverConfig.title
        binding?.currentServer?.text = currentServer
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
        imageLoader!!.loadImage(binding?.iconAvatar, if (userInfo == null) null else userInfo!!.headPortrait, com.black.user.R.drawable.icon_avatar, true)
        binding?.name?.setTextColor(SkinCompatResources.getColor(mContext, R.color.T1))
        if (userInfo != null) {
            val userName = if (userInfo!!.username == null) "" else userInfo!!.username
            binding?.name?.text = String.format("%s", userName)

            var relVerifyBg:Drawable? = null
            var tvVerifyColor:Int? = null
            var tvVerifyText:String? = null
            when(userInfo?.idNoStatus){
                ConstData.USER_VERIFY_NO ->{
                    relVerifyBg = getDrawable(R.drawable.bg_user_unverify_corner)
                    tvVerifyColor = getColor(R.color.T8)
                    tvVerifyText = getString(R.string.person_unchecked)
                }
                ConstData.USER_VERIFY_ED ->{
                    relVerifyBg = getDrawable(R.drawable.bg_user_verify_corner)
                    tvVerifyColor = getColor(R.color.T8)
                    tvVerifyText = getString(R.string.person_checked)
                }
                ConstData.USER_VERIFY_ING ->{
                    relVerifyBg = getDrawable(R.drawable.bg_user_under_verify_corner)
                    tvVerifyColor = getColor(R.color.T15)
                    tvVerifyText = getString(R.string.person_checking)
                }
                ConstData.USER_VERIFY_FAIL ->{
                    relVerifyBg = getDrawable(R.drawable.bg_user_verify_fail_corner)
                    tvVerifyColor = getColor(R.color.T10)
                    tvVerifyText = getString(R.string.purchase_failed)
                }
            }
            binding?.relVerifyStatus?.background = relVerifyBg
            binding?.tvVerify?.text = tvVerifyText
            if (tvVerifyColor != null) {
                binding?.tvVerify?.setTextColor(tvVerifyColor)
            }

            binding?.uuid?.visibility = View.VISIBLE
            binding?.uuid?.text = "UID:" + if (userInfo!!.id == null) "" else userInfo!!.id
            if (TextUtils.equals("email", userInfo!!.registerFrom)) {
                if (TextUtils.equals(userInfo!!.phoneSecurityStatus, "1") && TextUtils.equals(userInfo!!.emailSecurityStatus, "1") && TextUtils.equals(userInfo!!.googleSecurityStatus, "1")) {
                    binding?.safeLevel?.setText(R.string.level_high)
                } else if (TextUtils.equals(userInfo!!.emailSecurityStatus, "1")
                        && (TextUtils.equals(userInfo!!.phoneSecurityStatus, "1") || TextUtils.equals(userInfo!!.googleSecurityStatus, "1"))) {
                    binding?.safeLevel?.setText(R.string.level_middle)
                } else {
                    binding?.safeLevel?.setText(R.string.level_low)
                }
            } else {
                if (TextUtils.equals(userInfo!!.phoneSecurityStatus, "1") && TextUtils.equals(userInfo!!.emailSecurityStatus, "1") && TextUtils.equals(userInfo!!.googleSecurityStatus, "1")) {
                    binding?.safeLevel?.setText(R.string.level_high)
                } else if (TextUtils.equals(userInfo!!.phoneSecurityStatus, "1") && TextUtils.equals(userInfo!!.emailSecurityStatus, "1")) {
                    binding?.safeLevel?.setText(R.string.level_middle)
                } else {
                    binding?.safeLevel?.setText(R.string.level_low)
                }
            }
            binding?.btnLoginOut?.visibility = View.VISIBLE
            binding?.loginStatus?.visibility = View.VISIBLE
            binding?.btnLogin?.visibility = View.GONE
        } else {
            binding?.name?.setText(R.string.please_login)
            binding?.uuid?.visibility = View.GONE
            binding?.uuid?.setText(R.string.welcome_fbsex)
            binding?.safeLevel?.text = ""
            binding?.btnLoginOut?.visibility = View.GONE
            binding?.loginStatus?.visibility = View.GONE
            binding?.btnLogin?.visibility = View.VISIBLE
        }
    }

    override fun getUserInfo(callBack: Callback<UserInfo?>?) {
        if (CookieUtil.getToken(mContext) == null) {
            callBack?.callback(null)
            return
        }
        UserApiServiceHelper.getUserInfo(mContext, false, object : Callback<HttpRequestResultData<UserInfo?>?>() {
            override fun error(type: Int, error: Any) {
                callBack?.error(0, error)
            }

            override fun callback(returnData: HttpRequestResultData<UserInfo?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    //获取信息成功，保存信息并跳转主页
                    CookieUtil.saveUserInfo(mContext, returnData.data)
                    callBack?.callback(returnData.data)
                } else {
                    callBack?.error(0, mContext.getString(R.string.alert_login_failed_try_again))
                }
            }
        })
    }

    private val currentServerConfig: FryingServerConfig?
        get() = CommonUtil.getItemFromList(serverConfigs, UrlConfig.getIndex(applicationContext))

    companion object {
        private val TAG = MineActivity::class.java.simpleName
    }
}