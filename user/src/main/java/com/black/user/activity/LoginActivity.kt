package com.black.user.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.BaseApplication
import com.black.base.activity.BaseActivity
import com.black.base.api.*
import com.black.base.lib.verify.Target
import com.black.base.lib.verify.VerifyType
import com.black.base.lib.verify.VerifyWindowObservable
import com.black.base.lib.verify.VerifyWindowObservable.Companion.getVerifyWindowSingle
import com.black.base.manager.ApiManager
import com.black.base.model.*
import com.black.base.model.user.SuffixResult
import com.black.base.model.user.User
import com.black.base.model.user.UserInfo
import com.black.base.net.NormalObserver2
import com.black.base.util.*
import com.black.base.view.CountryChooseWindow
import com.black.base.view.CountryChooseWindow.OnCountryChooseListener
import com.black.base.widget.SpanTextView
import com.black.net.*
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivityLoginBinding
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.RSAUtil
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers

@Route(value = [RouterConstData.LOGIN])
class LoginActivity : BaseActivity(), View.OnClickListener {
    companion object {
        private const val FOR_GESTURE_PASSWORD = 102
        private const val TAG = "LoginActivity"
    }

    private var type = ConstData.AUTHENTICATE_TYPE_PHONE
    private var binding: ActivityLoginBinding? = null
    private var clickSource: Bundle? = null

    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            checkClickable()
        }

        override fun afterTextChanged(s: Editable) {}
    }

    private var thisCountry: CountryCode? = null
    private var chooseWindow: CountryChooseWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        clickSource = intent.extras
        binding?.phoneAccount?.addTextChangedListener(watcher)
        binding?.mailAccount?.addTextChangedListener(watcher)
        binding?.password?.addTextChangedListener(watcher)
        binding?.forgetPassword?.setOnClickListener(this)
        binding?.btnLogin?.setOnClickListener(this)
        binding?.phoneBar?.setOnClickListener(this)
        binding?.emailBar?.setOnClickListener(this)
        binding?.imgCountryCode?.setOnClickListener(this)

        binding?.root?.findViewById<SpanTextView>(R.id.text_action_bar_right)?.text =
            getString(R.string.register_title)
        binding?.root?.findViewById<SpanTextView>(R.id.text_action_bar_right)
            ?.setOnClickListener(this)
        if (thisCountry == null) {
            thisCountry = CountryCode()
            thisCountry?.code = "86"
        }
        chooseWindow = CountryChooseWindow(this, thisCountry, object : OnCountryChooseListener {
            override fun onCountryChoose(
                chooseWindow: CountryChooseWindow,
                countryCode: CountryCode?
            ) {
                chooseWindow.dismiss()
                thisCountry = countryCode
                binding?.countryCode?.tag = thisCountry?.code
                binding?.countryCode?.setText("+" + thisCountry?.code)
            }
        })
        initChooseWindowData()
        checkClickable()
    }

    override fun getTitleText(): String? {
        return getString(R.string.login_title)
    }

    override fun isStatusBarDark(): Boolean {
        return false
    }

    override fun needGeeTest(): Boolean {
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("777777", "onDestroy")
        BaseApplication.checkTokenError = true
    }

    override fun routeCheck(
        uri: String,
        beforePath: String?,
        requestCode: Int,
        flags: Int,
        extras: Bundle?
    ) {
        //不需要打开需要登录的目标
    }

    override fun onClick(v: View) {
        val id = v.id
        when (id) {
            R.id.forget_password -> {
                //找回密码
                val bundle = Bundle()
                bundle.putInt(ConstData.TYPE, type)
                if (type == ConstData.AUTHENTICATE_TYPE_PHONE) {
                    bundle.putString(
                        ConstData.ACCOUNT,
                        binding!!.phoneAccount.text.toString().trim { it <= ' ' })
                } else if (type == ConstData.AUTHENTICATE_TYPE_MAIL) {
                    bundle.putString(
                        ConstData.ACCOUNT,
                        binding!!.mailAccount.text.toString().trim { it <= ' ' })
                }
                BlackRouter.getInstance().build(RouterConstData.FORGET_PASSWORD_FIEX).with(bundle)
                    .go(mContext)
            }
            R.id.btn_login -> login()
            R.id.text_action_bar_right -> {//注册
                BlackRouter.getInstance().build(RouterConstData.REGISTER).go(mContext)
            }
            R.id.email_bar -> {
                type = ConstData.AUTHENTICATE_TYPE_MAIL
                changeLoinType(type)
            }
            R.id.phone_bar -> {
                type = ConstData.AUTHENTICATE_TYPE_PHONE
                changeLoinType(type)
            }
            R.id.img_country_code -> chooseCountryCode()

        }
    }

    private fun initChooseWindowData() {
        CommonApiServiceHelper.getCountryCodeList(
            this,
            false,
            object : NormalCallback<HttpRequestResultDataList<CountryCode?>?>(mContext!!) {
                override fun callback(returnData: HttpRequestResultDataList<CountryCode?>?) {
                    if (returnData != null && returnData.code == 0 && returnData.data != null) {
                        chooseWindow!!.setCountryList(returnData.data)
                    }
                }
            })
    }

    private fun chooseCountryCode() {
        chooseWindow!!.show(thisCountry)
    }

    private fun changeLoinType(loginType: Int?) {
        when (loginType) {
            ConstData.AUTHENTICATE_TYPE_PHONE -> {
                binding?.mailBarB?.visibility = View.GONE
                binding?.phoneBarB?.visibility = View.VISIBLE
                binding?.mailAccount?.visibility = View.GONE
                binding?.relPhone?.visibility = View.VISIBLE
                binding?.loginType?.text = getString(R.string.phone_number)
            }
            ConstData.AUTHENTICATE_TYPE_MAIL -> {
                binding?.mailBarB?.visibility = View.VISIBLE
                binding?.phoneBarB?.visibility = View.GONE
                binding?.mailAccount?.visibility = View.VISIBLE
                binding?.relPhone?.visibility = View.GONE
                binding?.loginType?.text = getString(R.string.email)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FOR_GESTURE_PASSWORD -> {
                setResult(resultCode)
                finish()
            }
        }
    }


    private fun checkClickable() {
        when (type) {
            ConstData.AUTHENTICATE_TYPE_PHONE -> binding!!.btnLogin.isEnabled =
                !(TextUtils.isEmpty(binding!!.phoneAccount.text.toString().trim { it <= ' ' })
                        || TextUtils.isEmpty(binding!!.password.text.toString().trim { it <= ' ' }))
            ConstData.AUTHENTICATE_TYPE_MAIL -> binding!!.btnLogin.isEnabled =
                !(TextUtils.isEmpty(binding!!.mailAccount.text.toString().trim { it <= ' ' })
                        || TextUtils.isEmpty(binding!!.password.text.toString().trim { it <= ' ' }))
            else -> binding!!.btnLogin.isEnabled = false
        }
    }

    private var user: User? = null
    private fun login() {
        if (type == ConstData.AUTHENTICATE_TYPE_PHONE) { //进行手机登录

            val userName: String = binding!!.phoneAccount.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(userName)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_not_phone))
                return
            }
            if (CommonUtil.isEmailNO(userName)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_rule_phone))
                return
            }
            var password: String = binding!!.password.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(password)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_password))
                return
            }
            password = RSAUtil.encryptDataByPublicKey(password)
            user = User()
            user!!.telCountryCode = thisCountry?.code
            user!!.userName = userName
            user!!.password = password
            doLogin(userName, password, thisCountry?.code)
        } else if (type == ConstData.AUTHENTICATE_TYPE_MAIL) { //进行邮箱登录
            val userName: String = binding!!.mailAccount.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(userName)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_not_mail))
                return
            }
            if (!CommonUtil.isEmailNO(userName)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_mail_rule))
                return
            }
            var password: String = binding!!.password.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(password)) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_password))
                return
            }
            password = RSAUtil.encryptDataByPublicKey(password)
            user = User()
            user!!.userName = userName
            user!!.password = password
            doLogin(userName, password, null)
        }
    }

    private fun doLogin(username: String, password: String, telCountryCode: String?) {
        showLoading()
        ApiManager.build(this, false, UrlConfig.ApiType.URl_UC)
            .getService<UserApiService>(UserApiService::class.java)
            ?.getToken(telCountryCode, username, password)
            ?.materialize()//Materialize将数据项和事件通知都当做数据项发射
            ?.subscribeOn(Schedulers.io())//事件产生的线程
            ?.observeOn(AndroidSchedulers.mainThread())//事件消费的线程
            ?.flatMap(object :
                RequestFunction<HttpRequestResultString?, RequestObserveResult<HttpRequestResultData<SuffixResult?>?>?>() {
                //Func1表示包装有返回值的方法，Actcion无返回值
                override fun afterRequest() {
                    hideLoading()
                }

                override fun applyResult(returnData: HttpRequestResultString?): Observable<RequestObserveResult<HttpRequestResultData<SuffixResult?>?>?>? {
                    return if (returnData != null) {
                        when (returnData.code) {
                            HttpRequestResult.SUCCESS -> {
                                //token获取成功，获取用户信息，进入app
                                val token = returnData.data
                                if (TextUtils.isEmpty(token)) {
                                    FryingUtil.showToast(
                                        mContext,
                                        getString(R.string.get_token_failed)
                                    )
                                } else {
                                    HttpCookieUtil.saveUcToken(mContext, token)
                                    CookieUtil.saveToken(mContext, token)
//                                        onGetTokenSuccess()
                                }
                                Observable.empty()
                            }
                            ConstData.AUTHENTICATE_CODE_MAIL -> {
                                val prefixAuth = returnData.msg
                                val target = Target()
                                val type = VerifyType.MAIL
                                val mailArr: Array<String> = prefixAuth?.split("#")?.toTypedArray()
                                    ?: return Observable.error(RuntimeException(getString(R.string.alert_server_error)))
                                target.mail = if (mailArr.size >= 2) mailArr[1] else null
                                verifyObserve(type, target, returnData.code!!, prefixAuth)
                            }
                            /**
                             * {
                            "msg": "038e9725-349b-4e09-9578-45e85f2a6d09#15308206311",
                            "code": -10022,
                            "data": "86#15308206311"
                            }
                             */
                            ConstData.AUTHENTICATE_CODE_PHONE -> {
                                val prefixAuth = returnData.msg
                                val target = Target()
                                val type = VerifyType.PHONE
                                val phoneArr: Array<String> =
                                    returnData.data?.split("#")?.toTypedArray()
                                        ?: return Observable.error(RuntimeException(getString(R.string.alert_server_error)))
                                if (phoneArr.size >= 2) {
                                    target.poneCountyCode = phoneArr[0]
                                    target.phone = phoneArr[1]
                                }
                                verifyObserve(type, target, returnData.code!!, prefixAuth)
                            }
                            ConstData.AUTHENTICATE_CODE_GOOGLE -> {
                                val prefixAuth = returnData.msg
                                val target = Target()
                                val type = VerifyType.GOOGLE
                                verifyObserve(type, target, returnData.code!!, prefixAuth)
                            }
                            ConstData.AUTHENTICATE_CODE_GOOGLE_OR_PHONE -> {
                                val prefixAuth = returnData.msg
                                val target = Target()
                                val phoneArr: Array<String> =
                                    returnData.data?.split("#")?.toTypedArray()
                                        ?: return Observable.error(RuntimeException(getString(R.string.alert_server_error)))
                                if (phoneArr.size >= 2) {
                                    target.poneCountyCode = phoneArr[0]
                                    target.phone = phoneArr[1]
                                }
                                val type = VerifyType.PHONE or VerifyType.GOOGLE
                                verifyObserve(type, target, returnData.code!!, prefixAuth)
                            }
                            else -> {
                                FryingUtil.showToast(mContext, returnData.msg)
                                Observable.empty()
                            }
                        }
                    } else {
                        FryingUtil.showToast(mContext, getString(R.string.login_data_error))
                        Observable.empty()
                    }
                }
            })
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(object : NormalObserver2<HttpRequestResultData<SuffixResult?>?>(this) {
                override fun afterRequest() {
                    hideLoading()
                }

                override fun callback(result: HttpRequestResultData<SuffixResult?>?) {
                    if (result != null && result.code == HttpRequestResult.SUCCESS) {
                        val ucToken = result?.data?.ucToken
                        val ticket = result?.data?.ticket
                        Log.d(TAG, "ucToken = " + ucToken)
                        Log.d(TAG, "ticket = " + ticket)
                        if (TextUtils.isEmpty(ucToken)) {
                            FryingUtil.showToast(mContext, getString(R.string.get_token_failed))
                        } else {
                            HttpCookieUtil.saveUcToken(mContext, ucToken)
                            CookieUtil.saveToken(mContext, ucToken)
                            Log.d(TAG, "currentThread = " + Thread.currentThread().name)
                            HttpCookieUtil.saveTicket(mContext, ticket)
                            if (user != null) {
                                user!!.token = ucToken
                                user!!.ucToken = ucToken
                                user!!.ticket = ticket
                            }
                            getProToken(mContext)
                            getOtcToken(mContext)
                        }
                    } else {
                        FryingUtil.showToast(
                            mContext,
                            if (result == null) getString(R.string.login_data_error) else result.msg
                        )
                    }
                }
            })

    }

    /**
     * 合约登录
     */
    private fun getFutureToken(context: Context) {
        FutureApiServiceHelper.getFutureToken(context!!, false,
            object : Callback<HttpRequestResultBean<String?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->getFutureToken", error.toString());
                }

                override fun callback(returnData: HttpRequestResultBean<String?>?) {
                    if (returnData != null) {
                        Log.d("ttttttt-->getFutureToken", returnData.toString());
                        var token = returnData.result
                        HttpCookieUtil.saveFutureToken(context, token)
                        getListenKey(context)
                    }
                }

            })
    }

    /**
     * 获取listen-key
     */
    private fun getListenKey(context: Context) {
        FutureApiServiceHelper.getListenKey(context!!, false,
            object : Callback<HttpRequestResultBean<String>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->getListenKey", error.toString());
                }

                override fun callback(returnData: HttpRequestResultBean<String>?) {
                    if (returnData != null) {
                        Log.d("ttttttt-->getListenKey", returnData.toString());
                        var listenKey = returnData.result
                        HttpCookieUtil.saveListenKey(context, listenKey)
                    }
                }

            })
    }


    //获取pro-token
    private fun getProToken(context: Context) {
        UserApiServiceHelper.getProToken(
            context!!,
            object : Callback<HttpRequestResultData<ProTokenResult?>?>() {
                override fun error(type: Int, error: Any?) {
                }

                override fun callback(result: HttpRequestResultData<ProTokenResult?>?) {
                    if (result != null && result.code == HttpRequestResult.SUCCESS) {
                        var proTokenResult: ProTokenResult? = result.data
                        var proToken = proTokenResult?.proToken
                        var proTokenExpiredTime = proTokenResult?.expireTime
                        HttpCookieUtil.saveProToken(context, proToken)
                        HttpCookieUtil.saveProTokenExpiredTime(
                            context,
                            proTokenExpiredTime.toString()
                        )
                        getWsToken(mContext)
                        getFutureToken(mContext)

                    }
                }
            })
    }

    //获取Otc-token
    private fun getOtcToken(context: Context) {
        C2CApiServiceHelper.getOtcToken(
            context,
            object : Callback<HttpRequestResultData<ProTokenResult?>?>() {
                override fun error(type: Int, error: Any?) {
                }

                override fun callback(result: HttpRequestResultData<ProTokenResult?>?) {
                    if (result != null && result.code == HttpRequestResult.SUCCESS) {
                        val otcTokenResult: ProTokenResult? = result.data
                        var otcToken = otcTokenResult?.proToken
                        var otcTokenExpiredTime = otcTokenResult?.expireTime
                        Log.d(TAG, "apiToken = " + otcToken)
                        Log.d(TAG, "expireTime = " + otcTokenExpiredTime)
                        HttpCookieUtil.saveApiToken(context, otcToken)
                        HttpCookieUtil.saveApiTokenExpiredTime(
                            context,
                            otcTokenExpiredTime.toString()
                        )

                    }
                }
            })
    }

    //获取ws-token
    private fun getWsToken(context: Context) {
        UserApiServiceHelper.getWsToken(context!!, object : Callback<HttpRequestResultString?>() {
            override fun error(type: Int, error: Any?) {
            }

            override fun callback(result: HttpRequestResultString?) {
                if (result != null && result.code == HttpRequestResult.SUCCESS) {
                    var wsToken = result.data
                    HttpCookieUtil.saveWsToken(context, wsToken)
                    onGetTokenSuccess()
                }
            }
        })
    }


    private fun verifyObserve(
        type: Int,
        target: Target,
        errorCode: Int,
        prefixAuth: String?
    ): Observable<RequestObserveResult<HttpRequestResultData<SuffixResult?>?>?>? {
        hideSoftKeyboard()
        val verifyWindow = getVerifyWindowSingle(this, type, true, target)
        return verifyWindow.show()
            .flatMap(object : Function<Target?, ObservableSource<Target>> {
                @Throws(Exception::class)
                override fun apply(target: Target): ObservableSource<Target> {
                    if (target == null) {
                        verifyWindow.dismiss()
                        return Observable.empty()
                    }
                    if (ConstData.AUTHENTICATE_CODE_MAIL == errorCode && TextUtils.isEmpty(target.mailCode)) {
                        FryingUtil.showToast(mContext, getString(R.string.alert_input_mail_code))
                        return Observable.empty()
                    }
                    if (ConstData.AUTHENTICATE_CODE_PHONE == errorCode && TextUtils.isEmpty(target.phoneCode)) {
                        FryingUtil.showToast(mContext, getString(R.string.alert_input_sms_code))
                        return Observable.empty()
                    }
                    if (ConstData.AUTHENTICATE_CODE_GOOGLE == errorCode && TextUtils.isEmpty(target.googleCode)) {
                        FryingUtil.showToast(mContext, getString(R.string.alert_input_google_code))
                        return Observable.empty()
                    }
                    if (ConstData.AUTHENTICATE_CODE_GOOGLE_OR_PHONE == errorCode && TextUtils.isEmpty(
                            target.phoneCode
                        ) && TextUtils.isEmpty(target.googleCode)
                    ) {
                        FryingUtil.showToast(
                            mContext,
                            getString(R.string.alert_phone_or_google_code)
                        )
                        return Observable.empty()
                    }
                    //verifyWindow.dismiss();
                    target.prefixAuth = prefixAuth
                    target.type = type
                    return Observable.just(target)
                }

            })
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(Schedulers.io())
            .flatMap { targetNew ->
                loginSuffix(verifyWindow, type, targetNew, prefixAuth)
                    ?: Observable.error(RuntimeException(getString(R.string.alert_server_error)))
            }
    }

    //使用验证码验证
    private fun loginSuffix(
        verifyWindow: VerifyWindowObservable,
        type: Int,
        target: Target,
        prefixAuth: String?
    ): Observable<RequestObserveResult<HttpRequestResultData<SuffixResult?>?>?>? {
        val phoneCode =
            if (type and VerifyType.PHONE == VerifyType.PHONE) target.phoneCode else null
        val emailCode = if (type and VerifyType.MAIL == VerifyType.MAIL) target.mailCode else null
        val googleCode =
            if (type and VerifyType.GOOGLE == VerifyType.GOOGLE) target.googleCode else null
        showLoading()
        return ApiManager.build(mContext, true, UrlConfig.ApiType.URl_UC)
            .getService(UserApiService::class.java)
            ?.loginSuffixResultObj(prefixAuth, phoneCode, emailCode, googleCode)
            ?.materialize()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.flatMap(object :
                RequestFunction2<HttpRequestResultData<SuffixResult?>?, HttpRequestResultData<SuffixResult?>?>() {
                override fun afterRequest() {
                    hideLoading()
                }

                @Throws(Exception::class)
                override fun applyResult(returnData: HttpRequestResultData<SuffixResult?>?): HttpRequestResultData<SuffixResult?>? {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        runOnUiThread { verifyWindow.dismiss() }
                    }
                    return returnData
                }
            })
    }

    private fun onGetTokenSuccess() {
        //密码登录成功，清空账号保护
//        CookieUtil.setAccountProtectType(mContext, 0);
//        CookieUtil.setGesturePassword(mContext, null);
        val oldUserId = CookieUtil.getUserId(this)
        getUserInfo(object : Callback<UserInfo?>() {
            override fun callback(result: UserInfo?) {
                if (result != null) {
                    //登录成功后重置检查token
                    //保存当前登录账号
                    user!!.uid = result.id
                    DataBaseUtil.saveUser(mContext, user)
                    FryingUtil.showToast(mContext, getString(R.string.login_success))
                    if (clickSource != null && clickSource!!.getBoolean(ConstData.ADD_USER)) {
                        setResult(Activity.RESULT_OK)
                        finish()
                        return
                    }
                    DataBaseUtil.refreshCurrentUser(mContext, user)
                    var forResult = false
                    val bundle = Bundle()
                    if (clickSource != null) {
                        forResult = clickSource!!.getBoolean(ConstData.FOR_RESULT)
                        bundle.putAll(clickSource)
                    }
                    //                    bundle.putInt(ConstData.HOME_FRAGMENT_INDEX, clickSource);
                    prefs.edit().putInt(ConstData.GESTURE_PASSWORD_FAILED_COUNT, 0).apply()
                    //登录成功之后，登录账号切换，
                    if (!TextUtils.equals(oldUserId, result.id)) {
                        CookieUtil.setAccountProtectType(mContext, ConstData.ACCOUNT_PROTECT_NONE)
                        CookieUtil.setGesturePassword(mContext, null)
                        //                        CookieUtil.setAccountProtectJump(mContext, false);
                        bundle.putInt(ConstData.OPEN_TYPE, 1)
                        bundle.putBoolean(ConstData.CHECK_UN_BACK, true)
                        if (forResult) {
                            BlackRouter.getInstance().build(RouterConstData.ACCOUNT_PROTECT)
                                .with(bundle)
                                .withRequestCode(FOR_GESTURE_PASSWORD)
                                .go(mContext)
                        } else {
                            BlackRouter.getInstance().build(RouterConstData.ACCOUNT_PROTECT)
                                .with(bundle).go(mContext)
                        }
                    } else {
                        if (forResult) {
                            setResult(Activity.RESULT_OK)
                        } else {
                            BlackRouter.getInstance().build(RouterConstData.HOME_PAGE).go(mContext)
                        }
                    }
                    finish()
                }
            }

            override fun error(type: Int, error: Any) {
                FryingUtil.showToast(mContext, error.toString())
            }
        }, true)
    }
}