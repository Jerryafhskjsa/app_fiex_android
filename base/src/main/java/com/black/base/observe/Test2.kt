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
import com.black.base.util.FryingUtil.showToastError
import com.black.base.util.UrlConfig
import com.black.net.HttpRequestResult
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import skin.support.content.res.SkinCompatResources

class Test2(private val context: Context) {
    fun test2() {}
    fun test() {
        createTest1()
                .flatMap { aBoolean: Boolean? ->
                    if (aBoolean != null && aBoolean) {
                        createTest2()
                    } else Observable.error(RuntimeException("error createTest1"))
                }
                .flatMap { aBoolean: Boolean? ->
                    if (aBoolean != null && aBoolean) {
                        createTest3()
                    } else Observable.error(RuntimeException("error createTest2"))
                }
                .flatMap { aBoolean: Boolean? ->
                    if (aBoolean != null && aBoolean) {
                        agreeC2CRule()
                    } else Observable.error(RuntimeException("error createTest3"))
                }
                .flatMap { aBoolean: Boolean? ->
                    if (aBoolean != null && aBoolean) {
                        checkBindPaymentMethod()
                    } else Observable.error(RuntimeException("error agreeC2CRule"))
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Boolean> {
                    override fun onSubscribe(d: Disposable) {
                        Log.e("subscribe", "Disposable:$d")
                    }

                    override fun onNext(aBoolean: Boolean) {
                        Log.e("subscribe", "onNext aBoolean:$aBoolean")
                    }

                    override fun onError(e: Throwable) {
                        Log.e("subscribe", "onError e:$e")
                        showToastError(context, e.message)
                    }

                    override fun onComplete() {
                        Log.e("subscribe", "onComplete e:")
                        showToastError(context, "onComplete")
                    }
                })
        //        agreeC2CRule2();
//        checkLoginUser()
//                .flatMap(new Function<Boolean, ObservableSource<Boolean>>() {
//                    @Override
//                    public ObservableSource<Boolean> apply(Boolean aBoolean) throws Exception {
//                        if (aBoolean != null && aBoolean) {
//                            return agreeC2CRule();
//                        }
//                        return Observable.error(new RuntimeException("请登录后使用"));
//                    }
//                })
//                .flatMap(new Function<Boolean, ObservableSource<Boolean>>() {
//                    @Override
//                    public ObservableSource<Boolean> apply(Boolean aBoolean) throws Exception {
//                        Log.e("Test", "checkC2CAgree:" + aBoolean);
//                        if (aBoolean == null || !aBoolean) {
//                            return Observable.error(new RuntimeException("请实名后使用"));
//                        }
//                        return checkC2CAgree();
//                    }
//                })
//                .flatMap(new Function<Boolean, ObservableSource<Boolean>>() {
//                    @Override
//                    public ObservableSource<Boolean> apply(Boolean aBoolean) throws Exception {
//                        Log.e("Test", "checkBindPaymentMethod:" + aBoolean);
//                        if (aBoolean == null || !aBoolean) {
//                            return Observable.error(new RuntimeException("请同意用户协议后使用"));
//                        }
//                        return checkBindPaymentMethod();
//                    }
//                })
//                .subscribe(new Observer<Boolean>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        Log.e("subscribe", "Disposable:" + d);
//                    }
//
//                    @Override
//                    public void onNext(Boolean aBoolean) {
//                        Log.e("subscribe", "onNext aBoolean:" + aBoolean);
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.e("subscribe", "onError e:" + e);
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        Log.e("subscribe", "onComplete e:");
//                    }
//                });
    }

    private fun createTest1(): Observable<Boolean> {
        return ObservableConfirmDialog(context, "提示", "测试1")
                .dialogToObservable()
                .subscribeOn(AndroidSchedulers.mainThread())
                .flatMap { observableConfirmDialog ->
                    observableConfirmDialog.dismiss()
                    Observable.just(true)
                }
    }

    private fun createTest2(): Observable<Boolean> {
        return ObservableConfirmDialog(context, "提示", "测试2")
                .dialogToObservable()
                .subscribeOn(AndroidSchedulers.mainThread())
                .flatMap { observableConfirmDialog ->
                    observableConfirmDialog.dismiss()
                    Observable.just(true)
                }
    }

    private fun createTest3(): Observable<Boolean> {
        return ObservableConfirmDialog(context, "提示", "测试3")
                .dialogToObservable()
                .subscribeOn(AndroidSchedulers.mainThread())
                .flatMap { observableConfirmDialog ->
                    observableConfirmDialog.dismiss()
                    Observable.just(true)
                }
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
                }.subscribeOn(AndroidSchedulers.mainThread())
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
                        if (agreementResult.data != null && true == agreementResult.data!!.agreest) {
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
                    if (returnData != null && returnData.code != null && returnData.code == HttpRequestResult.SUCCESS) {
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
