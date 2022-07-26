package com.black.frying.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.UserApiServiceHelper
import com.black.base.model.FryingServerConfig
import com.black.base.model.HttpRequestResultData
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
        binding?.setting?.setOnClickListener(this)
        binding?.info?.setOnClickListener(this)
        binding?.name?.setOnClickListener(this)
        binding?.userLayout?.setOnClickListener(this)
        binding?.uuid?.setOnClickListener(this)
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
        binding?.wallet?.setOnClickListener(this)
        binding?.billManage?.setOnClickListener(this)
        binding?.recommend?.setOnClickListener(this)
        binding?.consult?.setOnClickListener(this)
        binding?.safeCenter?.setOnClickListener(this)
        binding?.helpCenter?.setOnClickListener(this)
        binding?.moreLanguage?.setOnClickListener(this)
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
        binding?.forum?.setOnClickListener(this)
        binding?.serverSetting?.setOnClickListener(this)
        binding?.setting?.setOnClickListener(this)
        binding?.info?.setOnClickListener(this)

        initServiceApi()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun onClick(view: View) {
        when (view.id) {
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
            R.id.forum -> BlackRouter.getInstance().build(RouterConstData.FORUM).go(mContext)
            R.id.safe_center -> BlackRouter.getInstance().build(RouterConstData.SAFE_CENTER).go(mContext)
            R.id.help_center -> {
                //帮助中心
//                BlackRouter.getInstance().build(RouterConstData.PROMOTIONS).go(mContext);
                val bundle = Bundle()
                bundle.putString(ConstData.TITLE, mContext.getString(R.string.help_center))
                bundle.putString(ConstData.URL, UrlConfig.HELP_CENTER)
                BlackRouter.getInstance().build(RouterConstData.WEB_VIEW).with(bundle).go(mContext)
            }
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
            R.id.setting -> BlackRouter.getInstance().build(RouterConstData.USER_SETTING).go(mContext)
            R.id.info -> BlackRouter.getInstance().build(RouterConstData.ABOUT).go(mContext)
        }
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
            binding?.day?.visibility = View.GONE
        } else {
            binding?.nightModeToggle?.isChecked = false
            binding?.night?.visibility = View.GONE
            binding?.day?.visibility = View.VISIBLE
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
            //            if (TextUtils.equals(userInfo.idNoStatus, "1")) {
//                String userName = userInfo.realName == null ? "" : userInfo.realName.length() > 8 ? userInfo.realName.substring(0, 8) : userInfo.realName;
//                nameView.setText(String.format("HI,%s >", userName));
//            } else if (TextUtils.equals(userInfo.idNoStatus, "2")) {
//                nameView.setText(R.string.in_audit);
//            } else if (TextUtils.equals(userInfo.idNoStatus, "3")) {
//                nameView.setTextColor(SkinCompatResources.getColor(mContext, R.color.T5));
//                nameView.setText(R.string.audit_failed);
//            } else {
//                nameView.setTextColor(SkinCompatResources.getColor(mContext, R.color.T5));
//                nameView.setText(R.string.not_real_name);
//            }
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
        } else {
            binding?.name?.setText(R.string.please_login)
            binding?.uuid?.visibility = View.GONE
            binding?.uuid?.setText(R.string.welcome_fbsex)
            binding?.safeLevel?.text = ""
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