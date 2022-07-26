package com.black.c2c.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.lib.FryingSingleToast
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.c2c.C2CDetail
import com.black.base.model.c2c.C2COrder
import com.black.base.model.c2c.C2CPayment
import com.black.base.model.user.PaymentMethod
import com.black.base.util.*
import com.black.base.view.ConfirmDialog
import com.black.base.view.ConfirmDialog.OnConfirmCallback
import com.black.base.view.DeepControllerWindow
import com.black.c2c.R
import com.black.c2c.databinding.ActivityC2cOrderDetailBinding
import com.black.im.util.IMHelper
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.google.gson.Gson
import skin.support.content.res.SkinCompatResources
import java.math.RoundingMode
import java.util.*

@Route(value = [RouterConstData.C2C_ORDER_DETAIL], beforePath = RouterConstData.LOGIN)
class C2COrderDetailActivity : BaseActionBarActivity(), View.OnClickListener, QRefreshLayout.OnRefreshListener {
    private var imageLoader: ImageLoader? = null
    private var orderId: String? = null
    private var direction: String? = null
    private var c2COrder: C2COrder? = null

    private var binding: ActivityC2cOrderDetailBinding? = null

    private var titleView: TextView? = null
    private var titleBigView: TextView? = null

    private var c2CDetail: C2CDetail? = null
    private var currentPayment: C2CPayment? = null
    private var listRefreshTimeLoacal: Long = 0
    private var listRefreshTime: Long = 0
    private val handler = Handler()
    private var timerCommand: TimerCommand? = null
    private var command: GetNewMessageCommand? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_order_detail)
        imageLoader = ImageLoader(this)
        orderId = intent.getStringExtra(ConstData.C2C_ORDER_ID)
        direction = intent.getStringExtra(ConstData.C2C_DIRECTION)
        val c2cOrderString = intent.getStringExtra(ConstData.C2C_ORDER_DATA)
        try {
            c2COrder = if (TextUtils.isEmpty(c2cOrderString)) null else gson.fromJson(c2cOrderString, C2COrder::class.java)
        } catch (ignored: Exception) {
        }

        titleView = findViewById(R.id.action_bar_title)
        titleBigView = findViewById(R.id.action_bar_title_big)

        binding?.warning?.setOnClickListener(this)
        binding?.phone?.setOnClickListener(this)
        binding?.talk?.setOnClickListener(this)
        binding?.warning2?.setOnClickListener(this)
        binding?.phone2?.setOnClickListener(this)
        binding?.talk2?.setOnClickListener(this)
        binding?.merchantTypeLayout?.setOnClickListener(this)
        binding?.merchantName?.setOnClickListener(this)
        binding?.merchantQrcode?.setOnClickListener(this)
        binding?.payAccount?.setOnClickListener(this)
        binding?.bankName?.setOnClickListener(this)
        binding?.orderNumber?.setOnClickListener(this)

        val isBuy = C2COrder.ORDER_BUY == direction
        binding?.buyInfoLayout?.visibility = View.GONE
        binding?.countDown?.visibility = View.GONE
        binding?.moneyTitle?.setText(if (isBuy) "应付款" else "应收款")
        binding?.saleNoteLayout?.visibility = if (isBuy) View.GONE else View.VISIBLE
        binding?.buyInfoNote?.visibility = if (isBuy) View.VISIBLE else View.GONE

        binding?.btnPay?.setOnClickListener(this)
        binding?.btnRelease?.setOnClickListener(this)
        binding?.btnCancel?.setOnClickListener(this)

        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(this))
        binding?.refreshLayout?.setOnRefreshListener(this)
        c2COrderDetail
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.number_default)
    }

    override fun onAppBarStatusChanged(isAppBarHidden: Boolean) {
        binding?.topLayout?.visibility = if (isAppBarHidden) View.VISIBLE else View.GONE
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.warning, R.id.warning2 -> {
                //打开注意事项H5页面
                val bundle = Bundle()
                bundle.putString(ConstData.TITLE, "注意事项")
                bundle.putString(ConstData.URL, UrlConfig.URL_C2C_WARNING)
                BlackRouter.getInstance().build(RouterConstData.WEB_VIEW).with(bundle).go(mContext)
            }
            R.id.phone, R.id.phone2 -> {
                //获取电话号码并拨号
                requestCallPermissions(Runnable {
                    val phoneNumber = if (c2CDetail == null) null else c2CDetail!!.contact
                    val uri = String.format("tel:%s", phoneNumber)
                    val phoneIntent = Intent(Intent.ACTION_CALL)
                    phoneIntent.data = Uri.parse(uri)
                    try {
                        startActivity(phoneIntent)
                    } catch (ex: ActivityNotFoundException) {
                        FryingUtil.showToast(mContext, "拨打电话失败", FryingSingleToast.ERROR)
                    }
                })
            }
            R.id.talk, R.id.talk2 -> {
                if (c2CDetail != null) {
                    if (c2CDetail!!.isEnd) {
                        FryingUtil.showToast(mContext, String.format("订单%s，不可联系对方", c2CDetail!!.getStatusDisplay(this)), FryingSingleToast.ERROR)
                    } else {
                        //查看详情
                        val bundle = Bundle()
                        bundle.putString(ConstData.C2C_ORDER_ID, c2CDetail!!.id.toString())
                        bundle.putString(ConstData.C2C_DIRECTION, c2CDetail!!.direction)
                        bundle.putString(ConstData.C2C_ORDER_DATA, Gson().toJson(c2CDetail))
                        fryingHelper.checkUserAndDoing(Runnable {
                            val userInfo = CookieUtil.getUserInfo(mContext)
                            val merchantId = if (c2CDetail == null) null else NumberUtil.formatNumberNoGroup(c2CDetail!!.merchantId)
                            val userIdHeader = IMHelper.getUserIdHeader(mContext)
                            val userId = userInfo?.id
                            IMHelper.startWithIMActivity(this@C2COrderDetailActivity, this@C2COrderDetailActivity, userIdHeader + userId, RouterConstData.C2C_ORDER_DETAIL_TALK_NEW, bundle, null, null)
                        }, 0)
                    }
                }
            }
            R.id.merchant_type_layout -> {
                if (c2CDetail != null && c2CDetail!!.storePayment != null && c2CDetail!!.storePayment!!.isNotEmpty() && (c2CDetail!!.status == 0 || c2CDetail!!.status == 3)) {
                    DeepControllerWindow(this, "", currentPayment, c2CDetail!!.storePayment!!,
                            object : DeepControllerWindow.OnReturnListener<C2CPayment?> {
                                override fun onReturn(window: DeepControllerWindow<C2CPayment?>, item: C2CPayment?) {
                                    window.dismiss()
                                    currentPayment = item
                                    onPaymentChanged(currentPayment)
                                }

                            }).show()
                }
            }
            R.id.merchant_name -> {
                if (currentPayment != null && currentPayment!!.payeeName != null && CommonUtil.copyText(mContext, currentPayment!!.payeeName)) {
                    FryingUtil.showToast(mContext, mContext.getString(R.string.copy_text_success))
                } else {
                    FryingUtil.showToast(mContext, mContext.getString(R.string.copy_text_failed))
                }
            }
            R.id.merchant_qrcode -> {
                val url = if (currentPayment == null || currentPayment!!.url == null) null else UrlConfig.getHost(this) + currentPayment!!.url
                if (currentPayment != null) {
                    if (TextUtils.equals(currentPayment!!.type, C2CPayment.BANK)) {
                        if (currentPayment != null && currentPayment!!.bankName != null && CommonUtil.copyText(mContext, currentPayment!!.bankName)) {
                            FryingUtil.showToast(mContext, mContext.getString(R.string.copy_text_success))
                        } else {
                            FryingUtil.showToast(mContext, mContext.getString(R.string.copy_text_failed))
                        }
                    } else if (CommonUtil.isUrl(url)) {
                        val bundle = Bundle()
                        bundle.putString(ConstData.URL, url)
                        BlackRouter.getInstance().build(RouterConstData.SHOW_BIG_IMAGE).with(bundle).go(this)
                    } else {
                        FryingUtil.showToast(mContext, "查看失败")
                    }
                } else {
                    FryingUtil.showToast(mContext, "查看失败")
                }
            }
            R.id.pay_account -> {
                if (currentPayment != null && currentPayment!!.account != null && CommonUtil.copyText(mContext, currentPayment!!.account)) {
                    FryingUtil.showToast(mContext, mContext.getString(R.string.copy_text_success))
                } else {
                    FryingUtil.showToast(mContext, mContext.getString(R.string.copy_text_failed))
                }
            }
            R.id.bank_name -> {
                if (currentPayment != null && currentPayment!!.branchBankName != null && CommonUtil.copyText(mContext, currentPayment!!.branchBankName)) {
                    FryingUtil.showToast(mContext, mContext.getString(R.string.copy_text_success))
                } else {
                    FryingUtil.showToast(mContext, mContext.getString(R.string.copy_text_failed))
                }
            }
            R.id.order_number -> {
                if (c2CDetail != null && c2CDetail!!.id != null && CommonUtil.copyText(mContext, NumberUtil.formatNumberNoGroup(c2CDetail!!.id))) {
                    FryingUtil.showToast(mContext, mContext.getString(R.string.copy_text_success))
                } else {
                    FryingUtil.showToast(mContext, mContext.getString(R.string.copy_text_failed))
                }
            }
            R.id.btn_pay -> {
                val payMessage = String.format("确认已向 %s %s %s付款%s CNY.\n未付款点击确认按钮将视为恶意操作。\n恶意操作3次将无法再进行法币操作",
                        if (currentPayment == null || currentPayment!!.payeeName == null) nullAmount else currentPayment!!.payeeName,
                        if (currentPayment == null) nullAmount else currentPayment!!.getPayTypeText(this),
                        if (currentPayment == null || currentPayment!!.account == null) nullAmount else currentPayment!!.account,
                        if (c2CDetail == null) nullAmount else NumberUtil.formatNumberNoGroup(c2CDetail!!.totalPrice, 2, 2))
                ConfirmDialog(this, "提示",
                        payMessage,
                        object : OnConfirmCallback {
                            override fun onConfirmClick(confirmDialog: ConfirmDialog) {
                                C2CApiServiceHelper.confirmPaid(mContext, orderId, if (currentPayment == null) null else NumberUtil.formatNumberNoGroup(currentPayment!!.id), object : NormalCallback<HttpRequestResultString?>() {
                                    override fun callback(returnData: HttpRequestResultString?) {
                                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                                            confirmDialog.dismiss()
                                            c2COrderDetail
                                        } else {
                                            FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                                        }
                                    }
                                })
                            }

                        }).show()
            }
            R.id.btn_release -> {
                ConfirmDialog(this, "提示",
                        "确认已收款？",
                        object : OnConfirmCallback {
                            override fun onConfirmClick(confirmDialog: ConfirmDialog) {
                                C2CApiServiceHelper.releaseCoin(mContext, orderId, object : NormalCallback<HttpRequestResultString?>() {
                                    override fun callback(returnData: HttpRequestResultString?) {
                                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                                            confirmDialog.dismiss()
                                            c2COrderDetail
                                        } else {
                                            FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                                        }
                                    }
                                })
                            }

                        }).show()
            }
            R.id.btn_cancel -> {
                ConfirmDialog(this, "确认取消交易",
                        "如已向商家付款，请千万不要取消交易。\n取消规则：买家当日累计3笔取消，会限制当日买入功能。",
                        object : OnConfirmCallback {
                            override fun onConfirmClick(confirmDialog: ConfirmDialog) {
                                C2CApiServiceHelper.cancelOrder(mContext, orderId, object : NormalCallback<HttpRequestResultString?>() {
                                    override fun callback(returnData: HttpRequestResultString?) {
                                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                                            confirmDialog.dismiss()
                                            c2COrderDetail
                                        } else {
                                            FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                                        }
                                    }
                                })
                            }

                        }).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkListAddTimer()
        if (command != null) {
            handler.removeCallbacks(command)
        }
        command = GetNewMessageCommand()
        handler.post(command)
    }

    override fun onStop() {
        super.onStop()
        if (timerCommand != null) {
            handler.removeCallbacks(timerCommand)
            timerCommand = null
        }
        if (command != null) {
            handler.removeCallbacks(command)
            command = null
        }
    }

    private val c2COrderDetail: Unit
        get() {
            C2CApiServiceHelper.getC2COrderDetail(this, orderId, object : NormalCallback<HttpRequestResultData<C2CDetail?>?>() {
                override fun error(type: Int, error: Any?) {
                    super.error(type, error)
                    binding?.refreshLayout?.setRefreshing(false)
                }

                override fun callback(returnData: HttpRequestResultData<C2CDetail?>?) {
                    binding?.refreshLayout?.setRefreshing(false)
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        refreshOrderDetail(returnData.data)
                    }
                }
            })
        }

    fun refreshOrderDetail(c2CDetail: C2CDetail?) {
        this.c2CDetail = c2CDetail
        listRefreshTimeLoacal = System.currentTimeMillis()
        listRefreshTime = SystemClock.elapsedRealtime()
        checkListAddTimer()
        if (c2CDetail == null) {
            return
        }
        val isBuy = C2COrder.ORDER_BUY == direction
        val visible = if (isBuy || c2CDetail.selectPayment != null) View.VISIBLE else View.GONE
        binding?.buyInfoLayout?.visibility = visible
        FryingUtil.setCoinIcon(this, binding?.coinIcon, imageLoader, c2CDetail.coinType)
        binding?.action?.text = String.format("%s%s", if (c2CDetail.isBuy) "购买" else "出售", c2CDetail.coinType)
        titleView!!.text = c2CDetail.getStatusDisplay(this)
        titleBigView!!.text = c2CDetail.getStatusDisplay(this)
        binding?.money?.text = String.format("%s %s", NumberUtil.formatNumberNoGroupHardScale(c2CDetail.totalPrice, RoundingMode.FLOOR, 2), getString(R.string.cny))
        binding?.price?.text = String.format("单价 %s %s", NumberUtil.formatNumberNoGroupHardScale(c2CDetail.price, 2), getString(R.string.cny))
        binding?.amount?.text = String.format("数量 %s %s", NumberUtil.formatNumberNoGroup(c2CDetail.amount, RoundingMode.FLOOR, 0, 4), c2CDetail.coinType)
        binding?.orderNumber?.text = NumberUtil.formatNumberNoGroup(c2CDetail.id)
        refreshButtons(c2CDetail)
        currentPayment = if (c2CDetail.selectPayment != null) c2CDetail.selectPayment else currentPayment
        val userPaymentList = ArrayList<C2CPayment?>()
        if (c2CDetail.userPayment != null && true == c2CDetail.userPayment?.isNotEmpty()) {
            for (i in c2CDetail.userPayment!!.indices) {
                val c2CPayment = c2CDetail.userPayment!![i]
                if (c2CPayment?.isAvailable != null && c2CPayment.isAvailable == PaymentMethod.IS_ACTIVE) {
                    userPaymentList.add(c2CPayment)
                }
            }
        }
        c2CDetail.userPayment = userPaymentList
        val storePaymentList = ArrayList<C2CPayment?>()
        if (c2CDetail.storePayment != null && c2CDetail.storePayment!!.isNotEmpty()) {
            for (i in c2CDetail.storePayment!!.indices) {
                val c2CPayment = c2CDetail.storePayment!![i]
                if (c2CPayment?.isAvailable != null && c2CPayment.isAvailable == PaymentMethod.IS_ACTIVE) {
                    storePaymentList.add(c2CPayment)
                }
            }
        }
        c2CDetail.storePayment = storePaymentList
        //买单,如果selectPayment 为空，选择默认第一个商家支付方式
        if (c2CDetail.isBuy && currentPayment == null) {
            currentPayment = CommonUtil.getItemFromList(c2CDetail.storePayment, 0)
        }
        //控制是否能选择支付方式
        binding?.merchantTypeLayout?.isEnabled = c2CDetail.selectPayment == null && isBuy
        onPaymentChanged(currentPayment)
    }

    private fun refreshButtons(c2COrder: C2COrder) {
        if (C2COrder.ORDER_BUY == c2COrder.direction) {
            binding?.btnRelease?.visibility = View.GONE
            if (0 == c2COrder.status) {
                binding?.btnCancel?.visibility = View.VISIBLE
                binding?.btnPay?.visibility = View.VISIBLE
            } else {
                binding?.btnCancel?.visibility = View.GONE
                binding?.btnPay?.visibility = View.GONE
            }
        } else {
            binding?.btnPay?.visibility = View.GONE
            when (c2COrder.status) {
                0 -> {
                    binding?.btnCancel?.visibility = View.VISIBLE
                    binding?.btnRelease?.visibility = View.GONE
                }
                5 -> {
                    binding?.btnCancel?.visibility = View.GONE
                    binding?.btnRelease?.visibility = View.VISIBLE
                }
                else -> {
                    binding?.btnCancel?.visibility = View.GONE
                    binding?.btnRelease?.visibility = View.GONE
                }
            }
        }
        if (0 == c2COrder.status || 2 == c2COrder.status) {
            binding?.btnCancel?.visibility = View.VISIBLE
        } else {
            binding?.btnCancel?.visibility = View.GONE
        }
    }

    private fun onPaymentChanged(payment: C2CPayment?) {
        val userInfo = CookieUtil.getUserInfo(this)
        binding?.merchantName?.text = if (payment?.payeeName != null) payment.payeeName else nullAmount
        binding?.buyInfoNote?.text = String.format("请使用本人%s%s向以下账户自行转账。\n付款时请勿做任何备注,否则可能导致交易失败,资金冻结", if (userInfo?.realName == null) "" else "(" + userInfo.realName + ")", payment?.getPayTypeText(this)
                ?: "")
        binding?.payIcon?.setImageDrawable(if (payment == null) null else if (payment.payIconRes == 0) null else SkinCompatResources.getDrawable(this, payment.payIconRes))
        val contentTitle = if (payment == null) nullAmount else if (TextUtils.equals(payment.type, C2CPayment.BANK)) "开户银行" else "收款二维码"
        binding?.content?.text = contentTitle
        val accountTitle = if (payment == null) nullAmount else if (TextUtils.equals(payment.type, C2CPayment.BANK)) "卡号" else payment.getPayTypeText(this) + "账号"
        binding?.payAccountTitle?.text = accountTitle
        binding?.payAccount?.text = if (payment?.account == null) nullAmount else payment.account
        if (payment != null && TextUtils.equals(payment.type, C2CPayment.BANK)) {
            binding?.merchantQrcode?.text = if (payment.bankName == null) nullAmount else payment.bankName
            CommonUtil.setTextViewCompoundDrawable(binding?.merchantQrcode, SkinCompatResources.getDrawable(this, R.drawable.icon_c2c_copy), 2)
            binding?.bankNameLayout?.visibility = View.VISIBLE
        } else {
            binding?.merchantQrcode?.text = ""
            CommonUtil.setTextViewCompoundDrawable(binding?.merchantQrcode, SkinCompatResources.getDrawable(this, R.drawable.icon_c2c_qrcode), 2)
            binding?.bankNameLayout?.visibility = View.GONE
        }
        binding?.merchantType?.text = payment?.getPayTypeText(this) ?: nullAmount
        binding?.bankName?.text = if (payment?.branchBankName == null) nullAmount else payment.branchBankName
    }

    override fun onRefresh() {
        c2COrderDetail
    }

    private fun checkListAddTimer() {
        val hasNew = c2CDetail != null && c2CDetail!!.status == 0 && C2COrder.ORDER_BUY == direction
        if (hasNew) {
            binding?.countDown?.visibility = View.VISIBLE
            timerCommand = TimerCommand()
            handler.post(timerCommand)
        } else {
            binding?.countDown?.visibility = View.GONE
        }
    }

    //刷新倒计时
    private fun refreshCountdown(): Boolean {
        val loseTime = SystemClock.elapsedRealtime() - listRefreshTime
        val liveTime = (if (c2CDetail?.expireTime == null) 0 else c2CDetail?.expireTime!!) - loseTime - listRefreshTimeLoacal
        binding?.countDown?.text = String.format("请在 %s 内付款给卖家", getTimeDisplay(if (c2CDetail!!.expireTime == null) 0 else c2CDetail!!.expireTime!!, loseTime + listRefreshTimeLoacal))
        return liveTime > 0
    }

    private fun getTimeDisplay(finishTime: Long, thisTime: Long): String {
        var result = "00:00:00"
        var rangeTime = finishTime - thisTime
        if (rangeTime <= 0) {
            return "00:00"
        }
        if (rangeTime > 1000) {
            rangeTime /= 1000
            val d = (rangeTime / 24 / 3600).toInt()
            rangeTime %= (24 * 3600)
            val sb = StringBuilder()
            if (d > 0) {
                sb.append(d).append("d ")
            }
            val h = (rangeTime / 3600).toInt()
            rangeTime %= 3600
            val m = (rangeTime / 60).toInt()
            rangeTime %= 60
            val s = rangeTime.toInt()
            if (h > 0) {
                sb.append(CommonUtil.twoBit(h)).append(":")
            }
            sb.append(CommonUtil.twoBit(m)).append(":").append(CommonUtil.twoBit(s))
            result = sb.toString()
        }
        return result
    }

    private inner class TimerCommand : Runnable {
        override fun run() {
            if (refreshCountdown()) {
                handler.postDelayed(this, 1000)
            } else {
                c2COrderDetail
            }
        }
    }

    private inner class GetNewMessageCommand : Runnable {
        override fun run() {
            c2COrderDetail
            handler.postDelayed(this, 5000)
        }
    }
}