package com.black.c2c.util

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.view.Gravity
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2CAgreement
import com.black.base.model.user.PaymentMethod
import com.black.base.util.*
import com.black.base.view.ConfirmDialog
import com.black.base.view.ConfirmDialog.OnConfirmCallback
import com.black.c2c.R
import com.black.lib.permission.PermissionHelper
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.util.CommonUtil
import skin.support.content.res.SkinCompatResources

class C2CHandleCheckHelper(private val context: Context, private val permissionHelper: PermissionHelper, private val fryingHelper: FryingHelper) {
    fun checkLoginUser(next: Runnable?) {
        fryingHelper.checkUserAndDoing(next, 1)
    }


    fun checkRealName(next: Runnable) {
        val userInfo = CookieUtil.getUserInfo(context)
        if (userInfo != null && userInfo.isRealName()) {
            next.run()
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

    fun checkC2CAgree(next: Runnable) {
        C2CApiServiceHelper.isAgree(context, object : NormalCallback<HttpRequestResultData<C2CAgreement?>?>(context) {
            override fun callback(agreementResult: HttpRequestResultData<C2CAgreement?>?) {
                if (agreementResult?.data != null && agreementResult.data?.agreest != null && agreementResult.data?.agreest!!) {
                    next.run()
                } else {
                    agreeC2CRule(next)
                }
            }
        })
    }

    fun checkBindPaymentMethod(next: Runnable) {
        C2CApiServiceHelper.getPaymentMethodActive(context, object : NormalCallback<HttpRequestResultDataList<PaymentMethod?>?>(context) {
            override fun callback(returnData: HttpRequestResultDataList<PaymentMethod?>?) {
                var hasPaymentMethod = false
                if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                    val paymentMethods = returnData.data
                    if (paymentMethods != null && paymentMethods.isNotEmpty()) {
                        for (paymentMethod in paymentMethods) {
                            if (paymentMethod?.isAvailable != null && paymentMethod.isAvailable == PaymentMethod.IS_ACTIVE) {
                                hasPaymentMethod = true
                                break
                            }
                        }
                    }
                }
                if (hasPaymentMethod) {
                    next.run()
                } else {
                    alertBindPaymentMethod("尊敬的SoeasyEX用户，您还未 设置或激收款方式，请前往设置并激活。")
                }
            }
        })
    }

    fun checkBindBankPaymentMethod(next: Runnable) {
        C2CApiServiceHelper.getPaymentMethodActive(context, object : NormalCallback<HttpRequestResultDataList<PaymentMethod?>?>(context) {
            override fun callback(returnData: HttpRequestResultDataList<PaymentMethod?>?) {
                var hasPaymentMethod = false
                if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                    val paymentMethods = returnData.data
                    if (paymentMethods != null && paymentMethods.isNotEmpty()) {
                        for (paymentMethod in paymentMethods) {
                            if (paymentMethod?.isAvailable != null && paymentMethod.isAvailable == PaymentMethod.IS_ACTIVE && TextUtils.equals(paymentMethod.type, PaymentMethod.BANK)) {
                                hasPaymentMethod = true
                                break
                            }
                        }
                    }
                }
                if (hasPaymentMethod) {
                    next.run()
                } else {
                    alertBindPaymentMethod("尊敬的SoeasyEX用户，您还未 设置或激银行卡收款方式，不可进行快捷买卖。请前往设置并激活。")
                }
            }
        })
    }

    private fun alertBindPaymentMethod(message: String) {
        val confirmDialog = ConfirmDialog(context, "收款设置提示",
                message,
                object : OnConfirmCallback {
                    override fun onConfirmClick(confirmDialog: ConfirmDialog) {
                        confirmDialog.dismiss()
                        BlackRouter.getInstance().build(RouterConstData.PAYMENT_METHOD_MANAGER).go(context)
                    }

                })
        confirmDialog.setTitleGravity(Gravity.LEFT)
        confirmDialog.setMessageGravity(Gravity.LEFT)
        confirmDialog.setConfirmText("去设置")
        confirmDialog.show()
    }

    private fun agreeC2CRule(next: Runnable) {
        val userInfo = CookieUtil.getUserInfo(context) ?: return
        val color = SkinCompatResources.getColor(context, R.color.T7)
        val agreementText = "我已理解并同意<a href=\"" + UrlConfig.URL_C2C_RULE + "\">《SoeasyEX Global C2C交易用户服务协议》</a>的全部内容"
        var agreementTextSpanned: Spanned? = null
        agreementTextSpanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(agreementText, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(agreementText)
        }
        val confirmDialog = ConfirmDialog(context, "同意服务协议",
                agreementTextSpanned,
                object : OnConfirmCallback {
                    override fun onConfirmClick(confirmDialog: ConfirmDialog) {
                        C2CApiServiceHelper.agree(context, userInfo.id, object : NormalCallback<HttpRequestResultString?>(context) {
                            override fun callback(returnData: HttpRequestResultString?) {
                                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                                    confirmDialog.dismiss()
                                    next.run()
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

    fun checkMoneyPassword(next: Runnable) {
        next.run()
//        val userInfo = CookieUtil.getUserInfo(context) ?: return
//        if (TextUtils.equals(userInfo.moneyPasswordStatus, "1")) {
//            next.run()
//        } else {
//            val confirmDialog = ConfirmDialog(context, "提示",
//                    "尊敬的SoeasyEX用户，您还未设置资金密码。不能进行该操作！",
//                    OnConfirmCallback { confirmDialog ->
//                        confirmDialog.dismiss()
//                        BlackRouter.getInstance().build(RouterConstData.SAFE_CENTER).go(context)
//                    })
//            confirmDialog.setTitleGravity(Gravity.LEFT)
//            confirmDialog.setMessageGravity(Gravity.LEFT)
//            confirmDialog.setConfirmText("去设置")
//            confirmDialog.show()
//        }
    }

}