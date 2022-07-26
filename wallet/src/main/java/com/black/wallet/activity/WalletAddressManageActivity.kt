package com.black.wallet.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActivity
import com.black.base.adapter.interfaces.OnSwipeItemClickListener
import com.black.base.api.WalletApiService
import com.black.base.api.WalletApiServiceHelper
import com.black.base.lib.verify.Target
import com.black.base.lib.verify.VerifyType
import com.black.base.lib.verify.VerifyWindowObservable
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.wallet.CoinInfo
import com.black.base.model.wallet.WalletWithdrawAddress
import com.black.base.net.NormalObserver2
import com.black.base.util.*
import com.black.lib.view.SwipeItemLayout.OnSwipeItemTouchListener
import com.black.net.HttpRequestResult
import com.black.net.RequestFunction2
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.wallet.BR
import com.black.wallet.R
import com.black.wallet.adapter.WalletAddressAdapter
import com.black.wallet.databinding.ActivityWalletAddressManageBinding
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import skin.support.content.res.SkinCompatResources

@Route(value = [RouterConstData.WALLET_ADDRESS_MANAGE])
class WalletAddressManageActivity : BaseActivity(), View.OnClickListener, AdapterView.OnItemClickListener {
    private var coinInfo: CoinInfo? = null
    private var coinType: String? = null
    private var binding: ActivityWalletAddressManageBinding? = null
    private var adapter: WalletAddressAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        coinInfo = intent.getParcelableExtra(ConstData.COIN_INFO)
        coinType = intent.getStringExtra(ConstData.COIN_TYPE)
        if (coinInfo == null || coinType == null) {
            finish()
            return
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_address_manage)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(this, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        binding?.recyclerView?.addOnItemTouchListener(OnSwipeItemTouchListener(this))
        adapter = WalletAddressAdapter(this, BR.listItemWalletWtihdrawAddressModel, null)
        adapter?.setOnSwipeItemClickListener(object : OnSwipeItemClickListener {
            override fun deleteClick(position: Int) {
                val address = adapter?.getItem(position)
                address?.let {
                    doDelete(address)
                }
            }

            override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
                val address = adapter?.getItem(position)
                val resultData = Intent()
                resultData.putExtra(ConstData.WALLET_WITHDRAW_ADDRESS, address)
                setResult(Activity.RESULT_OK, resultData)
                finish()
            }
        })
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false
        binding?.btnAdd?.setOnClickListener(this)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.extract_address_manage, if (coinType == null) "" else coinType)
    }

    override fun onResume() {
        super.onResume()
        walletAddressList
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.btn_add) {
            val bundle = Bundle()
            bundle.putString(ConstData.COIN_TYPE, coinType)
            bundle.putParcelable(ConstData.COIN_INFO, coinInfo)
            BlackRouter.getInstance().build(RouterConstData.WALLET_ADDRESS_ADD).with(bundle)
                    .withRequestCode(ConstData.WALLET_ADDRESS_ADD).go(this)
        }
    }

    private fun doDelete(address: WalletWithdrawAddress) {
        val userInfo = CookieUtil.getUserInfo(this) ?: return
        val target = Target.buildFromUserInfo(userInfo)
        val verifyWindow = VerifyWindowObservable.getVerifyWindowMultiple(this, if (userInfo.registerFromMail()) VerifyType.MAIL else VerifyType.PHONE, target)
        verifyWindow
                .show()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { returnTarget: Target? ->
                    if (returnTarget == null) {
                        verifyWindow.dismiss()
                        Observable.empty()
                    } else {
                        showLoading()
                        val deleteResult = HttpRequestResultString()
                        deleteResult.code = HttpRequestResult.SUCCESS
//                        Observable.just(deleteResult)
                        ApiManager.build(mContext).getService(WalletApiService::class.java)
                                ?.deleteWalletAddress(address.id.toString(), if (userInfo.registerFromMail()) returnTarget.mailCode else returnTarget.phoneCode)
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
                }
                .compose(RxJavaHelper.observeOnMainThread())
                .subscribe(object : NormalObserver2<HttpRequestResultString?>(this) {
                    override fun onComplete() {
                        super.onComplete()
                    }


                    override fun callback(result: HttpRequestResultString?) {
                        if (result != null && result.code == HttpRequestResult.SUCCESS) {
                            adapter?.removeItem(address)
                            adapter?.notifyDataSetChanged()
                        } else {
                            FryingUtil.showToast(mContext, if (result == null) getString(R.string.error_data) else result.msg)
                        }
                    }
                })
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        val address = adapter?.getItem(position)
        val resultData = Intent()
        resultData.putExtra(ConstData.WALLET_WITHDRAW_ADDRESS, address)
        setResult(Activity.RESULT_OK, resultData)
        finish()
    }

    private val walletAddressList: Unit
        get() {
            WalletApiServiceHelper.getWalletAddressList(this, 1, 10, coinType, object : NormalCallback<HttpRequestResultDataList<WalletWithdrawAddress?>?>() {
                override fun callback(returnData: HttpRequestResultDataList<WalletWithdrawAddress?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        adapter?.data = returnData.data
                        adapter?.notifyDataSetChanged()
                    } else {
                        FryingUtil.showToast(mContext, if (returnData == null) getString(R.string.error_data) else returnData.msg)
                    }
                }
            })
        }
}