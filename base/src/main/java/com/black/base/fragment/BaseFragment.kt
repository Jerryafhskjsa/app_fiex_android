package com.black.base.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.black.base.R
import com.black.base.activity.BaseActionBarActivity
import com.black.base.activity.BaseActivity
import com.black.base.api.UserApiServiceHelper
import com.black.base.model.HttpRequestResultBase
import com.black.base.model.HttpRequestResultData
import com.black.base.model.user.UserInfo
import com.black.base.util.*
import com.black.base.util.FryingUtil.printError
import com.black.base.util.FryingUtil.showToast
import com.black.base.viewmodel.BaseViewModel
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.RouteCheckHelper
import com.black.util.Callback
import com.black.util.CommonUtil
import com.google.gson.Gson

open class BaseFragment : Fragment(), RouteCheckHelper {
    companion object {
        private val TAG = BaseFragment::class.java.simpleName
        const val MAIN_INDEX = 0
        const val TRADE_INDEX = 2
        const val MONEY_INDEX = 3
        const val MINE_INDEX = 3
        const val C2C_INDEX = 3
        const val DEMAND_INDEX = 3
        const val STATE_UNKNOWN = 0
        const val STATE_CREATE = 1
        const val STATE_RESUME = 2
        const val STATE_PAUSE = 4
    }

    protected lateinit var nullAmount: String
    protected var mContext: BaseActionBarActivity? = null
    protected var mFragment: Fragment? = null
    protected var inflater: LayoutInflater? = null
    protected var gson = Gson()
    protected lateinit var prefs: SharedPreferences
    protected lateinit var fryingHelper: FryingHelper
    protected var state = STATE_UNKNOWN

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        //Log.e("onAttach", this + "," + getActivity());
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        //Log.e("onAttach", this + "," + getActivity());
        nullAmount = getString(R.string.number_default)
        mFragment = this
        mContext = activity as BaseActionBarActivity
        inflater = LayoutInflater.from(activity)
        fryingHelper = FryingHelper(activity)
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        //Log.e("onAttachFragment", this + "," + getActivity());
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Log.e("onCreate", this + "," + getActivity());
        retainInstance = true
        prefs = activity!!.getSharedPreferences(activity!!.packageName, Context.MODE_PRIVATE)
        state = STATE_CREATE
    }

    override fun onResume() {
        super.onResume()
        state = STATE_RESUME
        if (isNeedResetRes) {
            resetSkinResources()
        }
        fryingHelper?.onResume()
        //Log.e("onResume", this + "," + getActivity());
        getViewModel()?.onResume()
    }

    override fun onStart() {
        super.onStart()
        //Log.e("onStart", this + "," + getActivity());
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //Log.e("onSaveInstanceState", this + "," + getActivity());
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { //Log.e("onCreateView", this + "," + getActivity());
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Log.e("onViewCreated", this + "," + getActivity());
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //Log.e("onDestroyView", this + "," + getActivity());
    }

    override fun onPause() {
        super.onPause()
        state = STATE_PAUSE
        //Log.e("onPause", this + "," + getActivity());
        getViewModel()?.onPause()
    }

    override fun onStop() {
        super.onStop()
        //Log.e("onStop", this + "," + getActivity());
        getViewModel()?.onStop()
    }

    override fun onDetach() {
        //Log.e("onDetach", this + "," + getActivity());
        super.onDetach()
        mContext = null
        try {
            val childFragmentManager = Fragment::class.java.getDeclaredField("mChildFragmentManager")
            childFragmentManager.isAccessible = true
            childFragmentManager[this] = null
        } catch (e: NoSuchFieldException) {
            throw RuntimeException(e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        }
    }

    override fun onDestroy() { //Log.e("onDestroy", this + "," + getActivity());
        super.onDestroy()
        getViewModel()?.onDestroy()
    }

    protected open fun getViewModel(): BaseViewModel<*>? {
        return null
    }

    var isNeedResetRes = false
    fun resetSkinResources() {
        if (isVisible) {
            isNeedResetRes = false
            doResetSkinResources()
        } else {
            isNeedResetRes = true
        }
    }

    open fun doResetSkinResources() {}
    //    protected void openActivity(Class activity) {
//        openActivity(activity, null);
//    }
//
//    protected void openActivity(Class activity, Bundle extras) {
//        Intent intent = new Intent(getActivity(), activity);
//        intent.setPackage(getActivity().getPackageName());
//        if (extras != null) {
//            intent.putExtras(extras);
//        }
//        startActivity(intent);
//    }
//
//    protected void openActivityForResult(Class activity, int requestCode) {
//        openActivityForResult(activity, requestCode, null);
//    }
//
//    protected void openActivityForResult(Class activity, int requestCode, Bundle extras) {
//        Intent intent = new Intent(getActivity(), activity);
//        intent.setPackage(getActivity().getPackageName());
//        if (extras != null) {
//            intent.putExtras(extras);
//        }
//        startActivityForResult(intent, requestCode);
//    }
//发送数据更新通知
    protected fun sendPairChangedBroadcast(type: Int) {
        if (activity == null) {
            return
        }
        val intent = Intent()
        intent.action = SocketUtil.ACTION_SOCKET_COMMAND
        intent.setPackage(activity!!.packageName)
        intent.putExtra(SocketUtil.SOCKET_COMMAND, type)
        activity!!.sendBroadcast(intent)
    }

    protected fun getUserInfo(callBack: Callback<UserInfo?>?) {
        if (activity == null) {
            return
        }
        if (CookieUtil.getToken(activity!!) == null) {
            callBack?.callback(null)
        }
        UserApiServiceHelper.getUserInfo(mContext, false, object : Callback<HttpRequestResultData<UserInfo?>?>() {
            override fun error(type: Int, error: Any) {
                callBack?.error(0, error)
            }

            override fun callback(returnData: HttpRequestResultData<UserInfo?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    //获取信息成功，保存信息并跳转主页
                    CookieUtil.saveUserInfo(activity!!, returnData.data)
                    callBack?.callback(returnData.data)
                } else {
                    callBack?.error(0, getString(R.string.alert_login_failed_try_again))
                }
            }
        })
    }

    protected fun setContentView(): Int {
        return 0
    }

    protected fun lazyLoad() {}
    protected var loginCallback: Runnable? = null
    //    protected void checkUserAndDoing(Runnable callback, int homeFragmentIndex) {
//        if (CookieUtil.getUserInfo(getActivity()) == null) {
//            loginAndCallback(callback, homeFragmentIndex);
//        } else {
//            callback.run();
//        }
//    }
    protected fun loginAndCallback(callback: Runnable?, homeFragmentIndex: Int) {
        loginCallback = callback
        val bundle = Bundle()
        bundle.putInt(ConstData.HOME_FRAGMENT_INDEX, homeFragmentIndex)
        //        openActivity(CommonUtil.getLoginActivity(mContext), bundle);
    }

    protected fun openShareWindow() { //        ((HomePageActivity) getActivity()).showShareView();
    }

    protected inner abstract class NormalCallback<T> : Callback<T>() {
        override fun error(type: Int, error: Any) {
            when (type) {
                ConstData.ERROR_NORMAL -> showToast(activity, error.toString())
                ConstData.ERROR_TOKEN_INVALID -> if (activity is BaseActionBarActivity || activity is BaseActivity) {
                    (activity as BaseActionBarActivity).onTokenError(error)
                }
                ConstData.ERROR_UNKNOWN ->  //根據情況處理，error 是返回的HttpRequestResultError
                    if (error != null && error is HttpRequestResultBase) {
                        showToast(activity, error.message)
                    }
            }
        }
    }

    protected fun initSpinnerSelectItem(spinner: Spinner, list: List<*>?) {
        val spAdapter: ArrayAdapter<*> = ArrayAdapter<Any>(mContext, R.layout.spinner_item_2, list)
        spAdapter.setDropDownViewResource(R.layout.spinner_drop_down_item)
        spinner.adapter = spAdapter
    }

    protected fun initSpinnerSelectItem(spinner: Spinner, list: Array<Any>?) {
        val spAdapter: ArrayAdapter<*> = ArrayAdapter(mContext, R.layout.spinner_item_2, list)
        spAdapter.setDropDownViewResource(R.layout.spinner_drop_down_item)
        spinner.adapter = spAdapter
    }

    protected fun postHandleTask(handler: Handler?, runnable: Runnable?) {
        CommonUtil.postHandleTask(handler, runnable)
    }

    protected fun postHandleTask(handler: Handler?, runnable: Runnable?, delayTime: Long) {
        CommonUtil.postHandleTask(handler, runnable, delayTime)
    }

    override fun routeCheck(uri: String, beforePath: String?, requestCode: Int, flags: Int, extras: Bundle?) {
        if (beforePath != null && beforePath.contains(RouterConstData.LOGIN)) {
            fryingHelper.checkUserAndDoing(Runnable {
                val bundle = Bundle()
                if (extras != null) {
                    bundle.putAll(extras)
                }
                BlackRouter.getInstance().build(uri)
                        .with(bundle)
                        .withRequestCode(requestCode)
                        .addFlags(flags)
                        .goFinal(mFragment) { routeResult, error -> error?.let { printError(it) } }
            }, 1)
        }
    }
}
