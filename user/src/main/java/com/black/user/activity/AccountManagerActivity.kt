package com.black.user.activity

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.text.TextUtils
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.adapter.interfaces.OnSwipeItemClickListener
import com.black.base.api.UserApiService
import com.black.base.lib.verify.Target
import com.black.base.lib.verify.VerifyType
import com.black.base.lib.verify.VerifyWindowObservable
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultString
import com.black.base.model.user.User
import com.black.base.model.user.UserInfo
import com.black.base.net.NormalObserver2
import com.black.base.util.*
import com.black.lib.view.SwipeItemLayout.OnSwipeItemTouchListener
import com.black.net.HttpRequestResult
import com.black.net.RequestFunction
import com.black.net.RequestFunction2
import com.black.net.RequestObserveResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.user.BR
import com.black.user.R
import com.black.user.adapter.AccountAdapter
import com.black.user.databinding.ActivityAccountManagerBinding
import com.black.util.Callback
import com.black.util.CommonUtil
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers

@Route(value = [RouterConstData.ACCOUNT_MANAGER])
class AccountManagerActivity : BaseActionBarActivity(), View.OnClickListener {
    private var binding: ActivityAccountManagerBinding? = null
    private var adapter: AccountAdapter? = null

    //异步获取数据
    private var handlerThread: HandlerThread? = null
    private var socketHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_account_manager)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        binding?.recyclerView?.addOnItemTouchListener(OnSwipeItemTouchListener(this))
        adapter = AccountAdapter(mContext, BR.listItemAccountModel, null)
        adapter?.setOnAccountItemClickListener(object : OnSwipeItemClickListener {
            override fun deleteClick(position: Int) {
                val user = adapter?.getItem(position)
                user?.let {
                    if (!user.isCurrentUser) {
                        DataBaseUtil.removeUser(mContext, adapter?.getItem(position))
                        adapter?.removeItem(user)
                        adapter?.notifyDataSetChanged()
                    }
                }
            }

            override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
                changeUser(adapter?.getItem(position))
            }
        })
        binding?.recyclerView?.adapter = adapter
        //解决数据加载不完的问题
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        //解决数据加载完成后, 没有停留在顶部的问题
        binding?.recyclerView?.isFocusable = false
        findViewById<View>(R.id.add_account).setOnClickListener(this)
        initHandler()
    }

    override fun onResume() {
        super.onResume()
        getAllAccount()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String? {
        return getString(R.string.change_user)
    }

    override fun onClick(view: View) {
        if (view.id == R.id.add_account) {
            val bundle = Bundle()
            bundle.putBoolean(ConstData.ADD_USER, true)
            BlackRouter.getInstance().build(RouterConstData.LOGIN).with(bundle).go(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (socketHandler != null) {
            socketHandler!!.removeMessages(0)
            socketHandler = null
        }
        if (handlerThread != null) {
            handlerThread!!.quit()
            handlerThread = null
        }
    }

    private fun initHandler() {
        if (handlerThread == null) {
            handlerThread = HandlerThread(ConstData.SOCKET_HANDLER, Process.THREAD_PRIORITY_BACKGROUND)
            handlerThread!!.start()
            socketHandler = Handler(handlerThread!!.looper)
        }
    }

    private fun changeUser(user: User?) {
        if (user == null) {
            return
        }
        if (user.isCurrentUser) {
            return
        }
        //使用选中的token拉取用户信息，如果拉取成功，替换用户信息，如果拉取失败，使用登录接口登录;接口登录失败，弹出提示并替换回原来token
        val oldToken = CookieUtil.getToken(this) ?: return
        CookieUtil.saveToken(mContext, user.token)
        getUserInfo(object : Callback<UserInfo?>() {
            override fun error(type: Int, error: Any) {
                login(user, oldToken)
            }

            override fun callback(returnData: UserInfo?) {
                onGetTokenSuccess(user)
            }
        })
    }

    private fun getAllAccount() {
        CommonUtil.checkActivityAndRun(this) {
            val savedUsers = DataBaseUtil.getSavedUsers(mContext)
            runOnUiThread {
                adapter?.data = savedUsers
                adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun login(user: User, oldToken: String) {
        showLoading()
        ApiManager.build(this, true).getService(UserApiService::class.java)
                ?.getToken(user.userName, user.password, user.telCountryCode)
                ?.materialize()
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.flatMap(object : RequestFunction<HttpRequestResultString?, RequestObserveResult<HttpRequestResultString?>>() {
                    override fun afterRequest() {
                        hideLoading()
                    }

                    override fun applyResult(returnData: HttpRequestResultString?): Observable<RequestObserveResult<HttpRequestResultString?>> {
                        return if (returnData != null) {
                            when (returnData.code) {
                                HttpRequestResult.SUCCESS -> {
                                    //登录成功，获取用户信息，进入app
                                    val token = returnData.data
                                    if (TextUtils.isEmpty(token)) {
                                        FryingUtil.showToast(mContext, getString(R.string.get_token_failed))
                                    } else {
                                        CookieUtil.saveToken(mContext, token)
                                        onGetTokenSuccess(user)
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
                                    verifyObserve(oldToken, type, target, returnData.code!!, prefixAuth)
                                }
                                ConstData.AUTHENTICATE_CODE_PHONE -> {
                                    val prefixAuth = returnData.msg
                                    val target = Target()
                                    val type = VerifyType.PHONE
                                    val phoneArr: Array<String> = returnData.data?.split("#")?.toTypedArray()
                                            ?: return Observable.error(RuntimeException(getString(R.string.alert_server_error)))
                                    if (phoneArr.size >= 2) {
                                        target.poneCountyCode = phoneArr[0]
                                        target.phone = phoneArr[1]
                                    }
                                    verifyObserve(oldToken, type, target, returnData.code!!, prefixAuth)
                                }
                                ConstData.AUTHENTICATE_CODE_GOOGLE -> {
                                    val prefixAuth = returnData.msg
                                    val target = Target()
                                    val type = VerifyType.GOOGLE
                                    verifyObserve(oldToken, type, target, returnData.code!!, prefixAuth)
                                }
                                ConstData.AUTHENTICATE_CODE_GOOGLE_OR_PHONE -> {
                                    val prefixAuth = returnData.msg
                                    val target = Target()
                                    val phoneArr: Array<String> = returnData.data?.split("#")?.toTypedArray()
                                            ?: return Observable.error(RuntimeException(getString(R.string.alert_server_error)))
                                    if (phoneArr.size >= 2) {
                                        target.poneCountyCode = phoneArr[0]
                                        target.phone = phoneArr[1]
                                    }
                                    val type = VerifyType.PHONE or VerifyType.GOOGLE
                                    verifyObserve(oldToken, type, target, returnData.code!!, prefixAuth)
                                }
                                else -> {
                                    onChangeError(oldToken, returnData.msg)
                                    Observable.empty()
                                }
                            }
                        } else {
                            onChangeError(oldToken, getString(R.string.login_data_error))
                            Observable.empty()
                        }
                    }
                })
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : NormalObserver2<HttpRequestResultString?>(this) {
                    override fun afterRequest() {
                        hideLoading()
                    }

                    override fun error(type: Int, error: Any?) {
                        onChangeError(oldToken, error)
                    }

                    override fun callback(result: HttpRequestResultString?) {
                        if (result != null && result.code == HttpRequestResult.SUCCESS) {
                            val token = result.data
                            if (TextUtils.isEmpty(token)) {
                                FryingUtil.showToast(mContext, getString(R.string.get_token_failed))
                            } else {
                                CookieUtil.saveToken(mContext, token)
                                user.token = token
                                onGetTokenSuccess(user)
                            }
                        } else {
                            FryingUtil.showToast(mContext, if (result == null) getString(R.string.login_data_error) else result.msg)
                        }
                    }
                })
    }

    private fun verifyObserve(oldToken: String, type: Int, target: Target, errorCode: Int, prefixAuth: String?): Observable<RequestObserveResult<HttpRequestResultString?>> {
        hideSoftKeyboard()
        val verifyWindow = VerifyWindowObservable.getVerifyWindowSingle(this, type, true, target)
        return verifyWindow.show()
                .flatMap(object : Function<Target?, ObservableSource<Target>> {
                    @Throws(Exception::class)
                    override fun apply(target: Target): ObservableSource<Target> {
                        if (target == null) {
                            verifyWindow.dismiss()
                            return Observable.empty()
                        }
                        if (ConstData.AUTHENTICATE_CODE_MAIL == errorCode && TextUtils.isEmpty(target.mailCode)) {
                            onChangeError(oldToken, getString(R.string.alert_input_mail_code))
                            return Observable.empty()
                        }
                        if (ConstData.AUTHENTICATE_CODE_PHONE == errorCode && TextUtils.isEmpty(target.phoneCode)) {
                            onChangeError(oldToken, getString(R.string.alert_input_sms_code))
                            return Observable.empty()
                        }
                        if (ConstData.AUTHENTICATE_CODE_GOOGLE == errorCode && TextUtils.isEmpty(target.googleCode)) {
                            onChangeError(oldToken, getString(R.string.alert_input_google_code))
                            return Observable.empty()
                        }
                        if (ConstData.AUTHENTICATE_CODE_GOOGLE_OR_PHONE == errorCode && TextUtils.isEmpty(target.phoneCode) && TextUtils.isEmpty(target.googleCode)) {
                            onChangeError(oldToken, getString(R.string.alert_phone_or_google_code))
                            return Observable.empty()
                        }
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
    private fun loginSuffix(verifyWindow: VerifyWindowObservable, type: Int, target: Target, prefixAuth: String?): Observable<RequestObserveResult<HttpRequestResultString?>?>? {
        val phoneCode = if (type and VerifyType.PHONE == VerifyType.PHONE) target.phoneCode else null
        val emailCode = if (type and VerifyType.MAIL == VerifyType.MAIL) target.mailCode else null
        val googleCode = if (type and VerifyType.GOOGLE == VerifyType.GOOGLE) target.googleCode else null
        showLoading()
        return ApiManager.build(mContext, true).getService(UserApiService::class.java)
                ?.loginSuffix(prefixAuth, phoneCode, emailCode, googleCode)
                ?.materialize()
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.flatMap(object : RequestFunction2<HttpRequestResultString?, HttpRequestResultString?>() {
                    override fun afterRequest() {
                        hideLoading()
                    }

                    @Throws(Exception::class)
                    override fun applyResult(returnData: HttpRequestResultString?): HttpRequestResultString? {
                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                            runOnUiThread { verifyWindow.dismiss() }
                        }
                        return returnData
                    }
                })
    }

    private fun onChangeError(oldToken: String, error: Any?) {
        if (error != null) {
            FryingUtil.showToast(mContext, error.toString())
        }
        CookieUtil.saveToken(mContext, oldToken)
    }

    private fun onGetTokenSuccess(user: User) {
        getUserInfo(null)
        //修改登录历史状态
        DataBaseUtil.refreshCurrentUser(this, user)
        getAllAccount()
    }
}