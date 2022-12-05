package com.black.user.activity

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.lib.FryingSingleToast
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.user.PaymentMethod
import com.black.base.util.*
import com.black.base.view.ConfirmDialog
import com.black.base.view.ConfirmDialog.OnConfirmCallback
import com.black.lib.refresh.QRefreshLayout
import com.black.lib.view.SwipeItemLayout.OnSwipeItemTouchListener
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.user.BR
import com.black.user.R
import com.black.user.adapter.PaymentMethodAdapter
import com.black.user.databinding.ActivityPaymentMothodManagerBinding
import com.black.util.CommonUtil

@Route(value = [RouterConstData.PAYMENT_METHOD_MANAGER], beforePath = RouterConstData.LOGIN)
class PaymentMethodManagerActivity : BaseActionBarActivity(), View.OnClickListener, QRefreshLayout.OnRefreshListener {
    private var binding: ActivityPaymentMothodManagerBinding? = null
    private var adapter: PaymentMethodAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_payment_mothod_manager)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        binding?.recyclerView?.addOnItemTouchListener(OnSwipeItemTouchListener(this))
        adapter = PaymentMethodAdapter(mContext, BR.listItemPaymentMethodModel, null)
        adapter?.setOnItemClickListener(object : PaymentMethodAdapter.OnMethodItemClickListener {
            override fun deleteClick(paymentMethod: PaymentMethod?) {
                paymentMethod?.let {
                    if (paymentMethod.isAvailable != null && paymentMethod.isAvailable == PaymentMethod.IS_ACTIVE) {
                        FryingUtil.showToast(mContext, "已激活收款方式不能删除！", FryingSingleToast.ERROR)
                    } else {
                        deletePaymentMethod(paymentMethod)
                    }
                }
            }

            override fun onQRCodeClick(paymentMethod: PaymentMethod?) {
                val url = if (paymentMethod == null) null else UrlConfig.getHost(mContext) + paymentMethod.url
                if (CommonUtil.isUrl(url)) {
                    val bundle = Bundle()
                    bundle.putString(ConstData.URL, url)
                    BlackRouter.getInstance().build(RouterConstData.SHOW_BIG_IMAGE).with(bundle).go(mContext)
                }
            }

            override fun onStatusUpdate(paymentMethod: PaymentMethod?) {
                paymentMethod?.let {
                    val newStatus = if (paymentMethod.isAvailable != null && paymentMethod.isAvailable == PaymentMethod.IS_ACTIVE) PaymentMethod.IS_NOT_ACTIVE else PaymentMethod.IS_ACTIVE
                    C2CApiServiceHelper.updatePaymentMethod(mContext, paymentMethod.id, newStatus, object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                        override fun callback(returnData: HttpRequestResultString?) {
                            if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                                paymentMethod.isAvailable = newStatus
                                adapter?.notifyDataSetChanged()
                            } else {
                                FryingUtil.showToast(mContext, if (returnData == null) getString(R.string.data_error) else returnData.msg)
                            }
                        }
                    })
                }
            }

            override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
            }

        })
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false

        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(this))
        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.addPaymentMethod?.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        paymentMethodList
        getUserInfo(null)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.payment_method)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.add_payment_method) {
            val userInfo = CookieUtil.getUserInfo(this)
            if (userInfo != null && userInfo.isRealName()) {
                checkMoneyPassword(Runnable {
                    requestCameraPermissions(Runnable {
                        requestStoragePermissions(Runnable {
                            BlackRouter.getInstance().build(RouterConstData.PAYMENT_METHOD_ADD).go(mContext)
                        })
                    })
                })
            } else {
                ConfirmDialog(mContext, "实名认证",
                        "添加收款方式，需要完成实名认证。",
                        object : OnConfirmCallback {
                            override fun onConfirmClick(confirmDialog: ConfirmDialog) {
                                CommonUtil.checkActivityAndRun(mContext) {
                                    requestCameraPermissions(Runnable {
                                        requestStoragePermissions(Runnable {
                                            BlackRouter.getInstance().build(RouterConstData.PERSON_INFO_CENTER).go(mContext) { routeResult, _ ->
                                                if (routeResult) {
                                                    confirmDialog.dismiss()
                                                }
                                            }
                                        })
                                    })
                                }
                            }

                        }).show()
            }
        }
    }

    override fun onRefresh() {
        paymentMethodList
    }

    private val paymentMethodList: Unit
        get() {
            C2CApiServiceHelper.getPaymentMethodAll(this, object : NormalCallback<HttpRequestResultDataList<PaymentMethod?>?>(mContext!!) {
                override fun error(type: Int, error: Any?) {
                    super.error(type, error)
                    binding?.refreshLayout?.setRefreshing(false)
                }

                override fun callback(returnData: HttpRequestResultDataList<PaymentMethod?>?) {
                    binding?.refreshLayout?.setRefreshing(false)
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        adapter?.data = returnData.data
                        adapter?.notifyDataSetChanged()
                    } else {
                        FryingUtil.showToast(mContext, if (returnData == null) getString(R.string.data_error) else returnData.msg)
                    }
                }
            })
        }

    private fun checkMoneyPassword(next: Runnable) {
        next.run()
//        val userInfo = CookieUtil.getUserInfo(this) ?: return
//        if (TextUtils.equals(userInfo.moneyPasswordStatus, "1")) {
//            next.run()
//        } else {
//            val confirmDialog = ConfirmDialog(this, "提示",
//                    "尊敬的FBSEX用户，您还未设置资金密码。不能进行该操作！",
//                    OnConfirmCallback { confirmDialog ->
//                        confirmDialog.dismiss()
//                        BlackRouter.getInstance().build(RouterConstData.SAFE_CENTER).go(mContext)
//                    })
//            confirmDialog.setTitleGravity(Gravity.LEFT)
//            confirmDialog.setMessageGravity(Gravity.LEFT)
//            confirmDialog.setConfirmText("去设置")
//            confirmDialog.show()
//        }
    }

    fun deletePaymentMethod(paymentMethod: PaymentMethod) {
        checkMoneyPassword(Runnable {
            C2CApiServiceHelper.deletePaymentMethod(mContext, paymentMethod.id, object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        adapter?.removeItem(paymentMethod)
                        adapter?.notifyDataSetChanged()
                    } else {
                        FryingUtil.showToast(mContext, if (returnData == null) getString(R.string.data_error) else returnData.msg)
                    }
                }
            })
        })
    }
}