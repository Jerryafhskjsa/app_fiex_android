package com.black.base.observe

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.Gravity
import com.black.base.R
import com.black.base.api.C2CApiService
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.c2c.C2CAgreement
import com.black.base.model.user.PaymentMethod
import com.black.base.util.BlackLinkClickListener
import com.black.base.util.BlackLinkMovementMethod
import com.black.base.util.CookieUtil
import com.black.base.util.UrlConfig
import com.black.net.HttpRequestResult
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import skin.support.content.res.SkinCompatResources

class Test(private val context: Context) {
    fun test2() {}
    fun test() { //        agreeC2CRule2();
        checkLoginUser()
                .flatMap { aBoolean: Boolean? ->
                    if (aBoolean != null && aBoolean) {
                        agreeC2CRule()
                    } else Observable.error(RuntimeException("请登录后使用"))
                }
                .flatMap { aBoolean: Boolean? ->
                    Log.e("Test", "checkC2CAgree:$aBoolean")
                    if (aBoolean == null || !aBoolean) {
                        Observable.error(RuntimeException("请实名后使用"))
                    } else checkC2CAgree()
                }
                .flatMap { aBoolean: Boolean? ->
                    Log.e("Test", "checkBindPaymentMethod:$aBoolean")
                    if (aBoolean == null || !aBoolean) {
                        Observable.error(RuntimeException("请同意用户协议后使用"))
                    } else checkBindPaymentMethod()
                }
                .subscribe(object : Observer<Boolean> {
                    override fun onSubscribe(d: Disposable) {
                        Log.e("subscribe", "Disposable:$d")
                    }

                    override fun onNext(aBoolean: Boolean) {
                        Log.e("subscribe", "onNext aBoolean:$aBoolean")
                    }

                    override fun onError(e: Throwable) {
                        Log.e("subscribe", "onError e:$e")
                    }

                    override fun onComplete() {
                        Log.e("subscribe", "onComplete e:")
                    }
                })
    }

    private fun agreeC2CRule2() {
        val color = SkinCompatResources.getColor(context, R.color.T7)
        val agreementText = "我已理解并同意<a href=\"" + UrlConfig.URL_C2C_RULE + "\">《FBSEX Global C2C交易用户服务协议》</a>的全部内容"
        var agreementTextSpanned: Spanned? = null
        agreementTextSpanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(agreementText, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(agreementText)
        }
        Log.e("Test", "agreementTextSpanned:$agreementTextSpanned")
        val confirmDialog = ObservableConfirmDialog(context, "同意服务协议", agreementTextSpanned)
        Log.e("Test", "confirmDialog:" + 1)
        confirmDialog.setTitleGravity(Gravity.LEFT)
        Log.e("Test", "confirmDialog:" + 2)
        confirmDialog.setMessageGravity(Gravity.LEFT)
        Log.e("Test", "confirmDialog:" + 3)
        confirmDialog.setConfirmText("同意")
        Log.e("Test", "confirmDialog:" + 4)
        confirmDialog.messageView.movementMethod = BlackLinkMovementMethod(BlackLinkClickListener("服务协议"))
        Log.e("Test", "confirmDialog:" + 5)
        confirmDialog.messageView.setLinkTextColor(color)
        confirmDialog
                .dialogToObservable()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<ObservableConfirmDialog?> {
                    override fun onSubscribe(d: Disposable) {
                        Log.e("subscribe", "Disposable:$d")
                    }

                    override fun onNext(aBoolean: ObservableConfirmDialog) {
                        Log.e("subscribe", "onNext aBoolean:$aBoolean")
                    }

                    override fun onError(e: Throwable) {
                        Log.e("subscribe", "onError e:$e")
                    }

                    override fun onComplete() {
                        Log.e("subscribe", "onComplete e:")
                    }
                })
    }

    private fun agreeC2CRule(): Observable<Boolean> {
        val color = SkinCompatResources.getColor(context, R.color.T7)
        val agreementText = "我已理解并同意<a href=\"" + UrlConfig.URL_C2C_RULE + "\">《FBSEX Global C2C交易用户服务协议》</a>的全部内容"
        var agreementTextSpanned: Spanned? = null
        agreementTextSpanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(agreementText, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(agreementText)
        }
        Log.e("Test", "agreementTextSpanned:$agreementTextSpanned")
        val confirmDialog = ObservableConfirmDialog(context, "同意服务协议", agreementTextSpanned)
        Log.e("Test", "confirmDialog:" + 1)
        confirmDialog.setTitleGravity(Gravity.LEFT)
        Log.e("Test", "confirmDialog:" + 2)
        confirmDialog.setMessageGravity(Gravity.LEFT)
        Log.e("Test", "confirmDialog:" + 3)
        confirmDialog.setConfirmText("同意")
        Log.e("Test", "confirmDialog:" + 4)
        confirmDialog.messageView.movementMethod = BlackLinkMovementMethod(BlackLinkClickListener("服务协议"))
        Log.e("Test", "confirmDialog:" + 5)
        confirmDialog.messageView.setLinkTextColor(color)
        return confirmDialog
                .dialogToObservable()
                .subscribeOn(AndroidSchedulers.mainThread())
                .flatMap { dialog ->
                    dialog.dismiss()
                    ApiManager.build(context).getService(C2CApiService::class.java)
                            ?.agree()
                            ?.flatMap { agreeResult: HttpRequestResultString? ->
                                if (agreeResult != null && agreeResult.code == HttpRequestResult.SUCCESS) {
                                    dialog.dismiss()
                                    Observable.just(true)
                                } else {
                                    Observable.error(RuntimeException(if (agreeResult == null) "null" else agreeResult.msg))
                                }
                            }?.subscribeOn(Schedulers.io())
                }
                .subscribeOn(AndroidSchedulers.mainThread())
//        Log.e("Test", "agreeC2CRule:" + "agreeC2CRule");
//        int color = SkinCompatResources.getColor(context, R.color.T7);
//        String agreementText = "我已理解并同意<a href=\"" + UrlConfig.URL_C2C_RULE + "\">《FBSEX Global C2C交易用户服务协议》</a>的全部内容";
//        Spanned agreementTextSpanned = null;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//            agreementTextSpanned = Html.fromHtml(agreementText, FROM_HTML_MODE_LEGACY);
//        } else {
//            agreementTextSpanned = Html.fromHtml(agreementText);
//        }
//        Log.e("Test", "agreementTextSpanned:" + agreementTextSpanned);
//        ObservableConfirmDialog confirmDialog = new ObservableConfirmDialog(context, "同意服务协议", agreementTextSpanned);
//        Log.e("Test", "confirmDialog:" + 1);
//        confirmDialog.setTitleGravity(Gravity.LEFT);
//        Log.e("Test", "confirmDialog:" + 2);
//        confirmDialog.setMessageGravity(Gravity.LEFT);
//        Log.e("Test", "confirmDialog:" + 3);
//        confirmDialog.setConfirmText("同意");
//        Log.e("Test", "confirmDialog:" + 4);
//        confirmDialog.getMessageView().setMovementMethod(new BlackLinkMovementMethod(new BlackLinkClickListener("服务协议")));
//        Log.e("Test", "confirmDialog:" + 5);
//        confirmDialog.getMessageView().setLinkTextColor(color);
//        Log.e("Test", "confirmDialog:" + confirmDialog);
//        return confirmDialog.show()
//                .flatMap(new Function<ObservableConfirmDialog, ObservableSource<Boolean>>() {
//                    @Override
//                    public ObservableSource<Boolean> apply(ObservableConfirmDialog observeConfirmDialog) throws Exception {
//                        observeConfirmDialog.dismiss();
//                        return ApiManager.build(context).getService(C2CApiService.class)
//                                .agree()
//                                .flatMap(new Function<HttpRequestResultString, ObservableSource<Boolean>>() {
//                                    @Override
//                                    public ObservableSource<Boolean> apply(HttpRequestResultString agreeResult) throws Exception {
//                                        if (agreeResult != null && agreeResult.code == HttpRequestResult.SUCCESS) {
//                                            confirmDialog.dismiss();
//                                            return Observable.just(true);
//                                        } else {
//                                            return Observable.error(new RuntimeException(agreeResult == null ? "null" : agreeResult.msg));
//                                        }
//                                    }
//                                }).subscribeOn(Schedulers.io());
//                    }
//                }).subscribeOn(AndroidSchedulers.mainThread());
    }

    private fun checkC2CAgree(): Observable<Boolean>? {
        Log.e("checkC2CAgree", "Thread:" + Thread.currentThread())
        return ApiManager.build(context).getService(C2CApiService::class.java)
                ?.isAgree()
                ?.flatMap { agreementResult: HttpRequestResultData<C2CAgreement?>? ->
                    Log.e("checkC2CAgree", "apply:" + Thread.currentThread())
                    if (agreementResult != null && agreementResult.code == HttpRequestResult.SUCCESS) {
                        if (agreementResult.data != null && true == agreementResult.data?.agreest) {
                            Observable.just(true)
                        } else {
                            agreeC2CRule()
                        }
                    } else {
                        Observable.error(RuntimeException(if (agreementResult == null) "null" else agreementResult.msg))
                    }
                }?.subscribeOn(Schedulers.io())
    }

    private fun checkLoginUser(): Observable<Boolean> {
        Log.e("checkLoginUser", "Thread:" + Thread.currentThread())
        val userInfo = CookieUtil.getUserInfo(context)
        return Observable.just(userInfo != null).subscribeOn(AndroidSchedulers.mainThread())
    }

    private fun checkRealName(): Observable<Boolean> {
        Log.e("checkRealName", "Thread:" + Thread.currentThread())
        val userInfo = CookieUtil.getUserInfo(context)
        return Observable.just(userInfo != null && userInfo.isRealName()).subscribeOn(AndroidSchedulers.mainThread())
    }

    private fun checkBindPaymentMethod(): Observable<Boolean>? {
        Log.e("checkBindPaymentMethod", "Thread:" + Thread.currentThread())
        return ApiManager.build(context).getService(C2CApiService::class.java)
                ?.getPaymentMethodAll("1")
                ?.flatMap { returnData: HttpRequestResultDataList<PaymentMethod?>? ->
                    Log.e("checkBindPaymentMethod", "apply:" + Thread.currentThread())
                    var hasPaymentMethod = false
                    if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                        val paymentMethods = returnData.data
                        if (paymentMethods != null && !paymentMethods.isEmpty()) {
                            for (paymentMethod in paymentMethods) {
                                if (paymentMethod?.isAvailable != null && paymentMethod.isAvailable == PaymentMethod.IS_ACTIVE) {
                                    hasPaymentMethod = true
                                    break
                                }
                            }
                        }
                    }
                    Observable.just(hasPaymentMethod)
                }?.subscribeOn(Schedulers.io())
    }

}
