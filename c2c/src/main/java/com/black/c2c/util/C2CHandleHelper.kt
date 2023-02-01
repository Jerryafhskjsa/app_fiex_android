package com.black.c2c.util

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.view.Gravity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2CAgreement
import com.black.base.model.c2c.C2COrder
import com.black.base.model.c2c.C2CSeller
import com.black.base.model.c2c.C2CSupportCoin
import com.black.base.model.user.UserInfo
import com.black.base.util.*
import com.black.base.view.ConfirmDialog
import com.black.base.view.ConfirmDialog.OnConfirmCallback
import com.black.c2c.R
import com.black.c2c.view.C2CCreateWidget
import com.black.lib.permission.PermissionHelper
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources

class C2CHandleHelper(private val context: Activity, private val permissionHelper: PermissionHelper, fryingHelper: FryingHelper, private val userInfo: UserInfo?, private val c2CSeller: C2CSeller?, private val direction: String, private val supportCoin: C2CSupportCoin) {
    private val c2CHandleCheckHelper: C2CHandleCheckHelper = C2CHandleCheckHelper(context, permissionHelper, fryingHelper)

    fun handle() {
        if (userInfo != null && userInfo.isRealName()) {
            if (TextUtils.equals(direction, C2COrder.ORDER_SELL)) {
                checkMoneyPassword()
            } else {
                checkUserAgree()
            }
        } else {
            ConfirmDialog(context, "实名认证",
                    "开通C2C交易，需要完成实名认证。",
                    object : OnConfirmCallback {
                        override fun onConfirmClick(confirmDialog: ConfirmDialog) {
                            CommonUtil.checkActivityAndRun(context) {
                                permissionHelper.requestCameraPermissions(Runnable {
                                    permissionHelper.requestStoragePermissions(Runnable {
                                        BlackRouter.getInstance().build(RouterConstData.PERSON_INFO_CENTER).go(context) { routeResult, _ ->
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

    private fun checkMoneyPassword() {
        c2CHandleCheckHelper.checkBindPaymentMethod(Runnable {
            checkUserAgree()
        })
    }

    private fun checkUserAgree() {
        C2CApiServiceHelper.isAgree(context, object : NormalCallback<HttpRequestResultData<C2CAgreement?>?>(context) {
            override fun callback(agreementResult: HttpRequestResultData<C2CAgreement?>?) {
                if (agreementResult != null && agreementResult.code == HttpRequestResult.SUCCESS) {
                    if (agreementResult.data != null && agreementResult.data!!.agreest != null && agreementResult.data!!.agreest!!) {
                        checkBindPaymentMethod()
                    } else {
                        val color = SkinCompatResources.getColor(context, R.color.T7)
                        val agreementText = "我已理解并同意<a href=\"" + UrlConfig.URL_C2C_RULE + "\">《SoeasyEX Global C2C交易用户服务协议》</a>的全部内容"
                        val agreementTextSpanned: Spanned?
                        agreementTextSpanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Html.fromHtml(agreementText, Html.FROM_HTML_MODE_LEGACY)
                        } else {
                            Html.fromHtml(agreementText)
                        }
                        val confirmDialog = ConfirmDialog(context,
                                "同意服务协议",
                                agreementTextSpanned,
                                object : OnConfirmCallback {
                                    override fun onConfirmClick(confirmDialog: ConfirmDialog) {
                                        C2CApiServiceHelper.agree(context, userInfo!!.id, object : NormalCallback<HttpRequestResultString?>(context) {
                                            override fun callback(returnData: HttpRequestResultString?) {
                                                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                                                    confirmDialog.dismiss()
                                                    checkBindPaymentMethod()
                                                } else {
                                                    FryingUtil.showToast(context, if (returnData == null) "null" else returnData.msg)
                                                }
                                            }
                                        })
                                    }

                                })
                        confirmDialog.setTitleGravity(Gravity.LEFT)
                        confirmDialog.setMessageGravity(Gravity.LEFT)
                        confirmDialog.setConfirmText("同意")
                        confirmDialog.messageView.movementMethod = BlackLinkMovementMethod(BlackLinkClickListener("服务协议"))
                        confirmDialog.messageView.setLinkTextColor(color)
                        confirmDialog.show()
                    }
                } else {
                    FryingUtil.showToast(context, if (agreementResult == null) "null" else agreementResult.msg)
                }
            }
        })
    }

    fun checkBindPaymentMethod() {
        c2CHandleCheckHelper.checkBindPaymentMethod(Runnable {
            showBuyDialog(c2CSeller)
        })
    }

    private fun showBuyDialog(c2CSeller: C2CSeller?) {
        val c2CCreateWidget = C2CCreateWidget(context, direction, c2CSeller, supportCoin)
        c2CCreateWidget.setOnC2CHandlerListener(object : C2CCreateWidget.OnC2CHandlerListener {
            override fun onCancel(widget: C2CCreateWidget?) {
                widget?.dismiss()
            }

            override fun onConfirm(widget: C2CCreateWidget?) {
                val amount = widget?.amount
                if (amount == null) {
                    FryingUtil.showToast(context, context.getString(R.string.alert_c2c_create_amount_error, context.getString(if (TextUtils.equals(direction, "BID")) R.string.c2c_buy else R.string.c2c_sell)))
                    return
                }
                widget.dismiss()
                createOrder(widget, NumberUtil.formatNumberNoGroup(amount))
            }
        })
        c2CCreateWidget.show()
    }

    private fun createOrder(widget: C2CCreateWidget?, amountText: String) {
        val callback: Callback<HttpRequestResultString?> = object : NormalCallback<HttpRequestResultString?>(context) {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    widget!!.dismiss()
                    val bundle = Bundle()
                    bundle.putString(ConstData.C2C_ORDER_ID, returnData.data)
                    bundle.putString(ConstData.C2C_DIRECTION, direction)
                    BlackRouter.getInstance().build(RouterConstData.C2C_ORDER_DETAIL).with(bundle).go(context)
                } else {
                    FryingUtil.showToast(context, if (returnData == null) "null" else returnData.msg)
                }
            }
        }
        if (TextUtils.equals(direction, C2COrder.ORDER_SELL)) {
            C2CApiServiceHelper.createC2COrderSell(context, c2CSeller?.coinType, direction, amountText, c2CSeller?.id, null, callback)
        } else {
            C2CApiServiceHelper.createC2COrderBuy(context, c2CSeller?.coinType, direction, amountText, c2CSeller?.id, null, callback)
        }
    }
}