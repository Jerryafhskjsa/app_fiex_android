package com.black.base.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.text.Html
import android.text.InputType
import android.text.Spanned
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.*
import androidx.core.content.FileProvider
import com.black.base.BaseApplication
import com.black.base.R
import com.black.base.api.UserApiService
import com.black.base.api.UserApiServiceHelper
import com.black.base.api.WalletApiServiceHelper
import com.black.base.lib.FryingSingleToast
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.Update
import com.black.base.model.socket.TradeOrder
import com.black.base.model.user.UserInfo
import com.black.base.model.wallet.CoinInfo
import com.black.base.model.wallet.CoinInfoType
import com.black.base.net.HttpCallbackSimple
import com.black.base.service.DownloadServiceHelper
import com.black.base.view.ConfirmDialog
import com.black.base.view.ConfirmDialog.OnConfirmCallback
import com.black.base.view.LoadingDialog
import com.black.base.widget.ObserveScrollView
import com.black.net.DownloadListener
import com.black.net.HttpCookieUtil
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.ImageUtil
import com.black.util.NumberUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import skin.support.content.res.SkinCompatResources
import java.io.File
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object FryingUtil {
    //是否需要签名认证
    fun needSSL(url: String?): Boolean {
        return false
    }

    /**
     * 显示提示
     *
     * @param context
     * @param messages
     */
    @JvmOverloads
    fun showToast(context: Context?, messages: String?, @FryingSingleToast.Type type: Int = FryingSingleToast.NORMAL) {
        if (context == null) {
            return
        }
        if (context is Activity) {
            CommonUtil.checkActivityAndRunOnUI(context) { FryingSingleToast.show(context, messages, type) }
        } else {
            FryingSingleToast.show(context, messages, type)
        }
    }

    fun showToastError(context: Context?, messages: String?) {
        showToast(context, messages, FryingSingleToast.ERROR)
    }

    fun setEditTextAddEyeControl(editText: EditText?, eyeView: CheckBox?) {
        if (editText == null || eyeView == null) {
            return
        }
        eyeView.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD //设置密码不可见
            } else {
                editText.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT //设置密码可见
            }
        })
    }

    fun setEditTextBottomLine(editText: EditText?, lineView: View?, def: Int, foc: Int) {
        if (editText == null || lineView == null) {
            return
        }
        editText.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            //#406ce9
            if (hasFocus) {
                lineView.setBackgroundColor(foc)
            } else {
                lineView.setBackgroundColor(def)
            }
        }
    }

    /**
     * 生成手势密码密文
     *
     * @param passwordPoints 原始点位数据
     * @return
     */
    fun createGesturePassword(passwordPoints: List<Any>?): String? {
        if (passwordPoints == null || passwordPoints.isEmpty()) {
            return null
        }
        val sb = StringBuilder()
        for (passwordPoint in passwordPoints) {
            sb.append(passwordPoint.toString())
        }
        return CommonUtil.MD5(sb.toString())
    }

    //计算挂单数量百分比,默认已排序
    fun computeTradeOrderWeightPercent(list: List<TradeOrder?>?, maxCount: Int) {
        if (list == null || list.isEmpty()) {
            return
        }
        //计算总量
        val count = min(list.size, maxCount)
        var total = 0.0
        for (i in 0 until count) {
            val tradeOrder = list[i]
            total += abs(tradeOrder?.exchangeAmount ?: 0.0)
        }
        var lastAmount = 0.0
        if (total != 0.0) {
            for (i in 0 until count) {
                val tradeOrder = list[i]
                lastAmount += tradeOrder?.exchangeAmount ?: 0.0
                tradeOrder?.weightPercent = abs(lastAmount / total)
                tradeOrder?.beforeAmount = lastAmount
            }
        } else {
            for (i in 0 until count) {
                val tradeOrder = list[i]
                tradeOrder?.weightPercent = 0.0
                tradeOrder?.beforeAmount = 0.0
            }
        }
    }

    //计算挂单数量百分比,默认已排序
    fun computeTradeOrderWeightPercent(bid: List<TradeOrder?>?, ask: List<TradeOrder?>?) {
        var bidList = bid
        var askList = ask
        bidList = bidList ?: ArrayList()
        askList = askList ?: ArrayList()
        //计算总量
        val bidCount = bidList.size
        val askCount = askList.size
        var bidTotal = 0.0
        for (i in 0 until bidCount) {
            val tradeOrder = bidList[i]
            bidTotal += abs(tradeOrder?.exchangeAmount ?: 0.0)
        }
        var askTotal = 0.0
        for (i in 0 until askCount) {
            val tradeOrder = askList[i]
            askTotal += abs(tradeOrder?.exchangeAmount ?: 0.0)
        }
        val total = max(bidTotal, askTotal)
        if (total != 0.0) {
            var lastAmount = 0.0
            for (i in 0 until bidCount) {
                val tradeOrder = bidList[i]
                lastAmount += tradeOrder?.exchangeAmount ?: 0.0
                tradeOrder?.weightPercent = abs(lastAmount / total)
                tradeOrder?.beforeAmount = lastAmount
            }
            lastAmount = 0.0
            for (i in 0 until askCount) {
                val tradeOrder = askList[i]
                lastAmount += tradeOrder?.exchangeAmount ?: 0.0
                tradeOrder?.weightPercent = abs(lastAmount / total)
                tradeOrder?.beforeAmount = lastAmount
            }
        } else {
            for (i in 0 until bidCount) {
                val tradeOrder = bidList[i]
                tradeOrder?.weightPercent = 0.0
                tradeOrder?.beforeAmount = 0.0
            }
            for (i in 0 until askCount) {
                val tradeOrder = askList[i]
                tradeOrder?.weightPercent = 0.0
                tradeOrder?.beforeAmount = 0.0
            }
        }
    }

    fun getLoadDialog(context: Context?, content: String?): LoadingDialog {
        val loadDialog = LoadingDialog(context!!)
        loadDialog.setCancelable(false)
        return loadDialog
    }

    fun getRouteAnnotation(activity: Activity): Route {
        return activity.javaClass.getAnnotation(Route::class.java)
    }

    fun checkRouteUri(`object`: Any?, checkUri: String?): Boolean {
        if (`object` == null || checkUri == null) {
            return false
        }
        val route = `object`.javaClass.getAnnotation(Route::class.java)
        if (route != null) {
            val value: Array<String> = route.value
            for (uri in value) {
                if (checkUri == uri) {
                    return true
                }
            }
        }
        return false
    }

    fun needShowProtectActivity(activity: Activity): Boolean {
        val route = activity.javaClass.getAnnotation(Route::class.java)
        if (route != null) {
            val value: Array<String> = route.value
            for (uri in value) {
                if (RouterConstData.GESTURE_PASSWORD_CHECK == uri || RouterConstData.FINGER_PRINT_CHECK == uri || RouterConstData.START_PAGE == uri || RouterConstData.GESTURE_PASSWORD_SETTING == uri || RouterConstData.ACCOUNT_PROTECT.equals(uri, ignoreCase = true)) {
                    return false
                }
            }
        }
        return true
    }

    fun checkActivityRouter(activity: Activity, routerKey: String): Boolean {
        val route = activity.javaClass.getAnnotation(Route::class.java)
        if (route != null) {
            val value: Array<String> = route.value
            for (uri in value) {
                if (routerKey == uri) {
                    return true
                }
            }
        }
        return false
    }

    fun getMaxBitmap(bitmap: Bitmap?): Bitmap? {
        var bitmap1: Bitmap = bitmap ?: return null
        val size = ImageUtil.getBitmapSize(bitmap1)
        val width = bitmap1.width
        val rowBytes = bitmap1.rowBytes
        var bytesPerPixel = rowBytes / width
        bytesPerPixel = if (bytesPerPixel < 1) 1 else bytesPerPixel
        if (size > ConstData.IMAGE_MAX_SIZE * bytesPerPixel) {
            val bestSize = ImageUtil.getMaxSize(bitmap1, ConstData.IMAGE_MAX_SIZE)
            bitmap1 = ImageUtil.zoomBitmap(bitmap1, bestSize.width, bestSize.height)
        }
        return bitmap1
    }

    fun getLanguageKey(context: Context?): String {
        val language = (if (context == null) null else LanguageUtil.getLanguageSetting(context))
                ?: return "en-us"
        return when (language.languageCode) {
            1 -> "en-us"
            2-> "zh-cn"
            else -> "en-us"
        }
    }

    fun controlHeaderHidden(scrollView: ObserveScrollView, view: View) {
        val paddingTop = view.paddingTop
        CommonUtil.measureView(view)
        val height = view.measuredHeight
        scrollView.addScrollListener(object : ObserveScrollView.ScrollListener {
            override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
                val scrollY = scrollView.scrollY
                var newTop = paddingTop + scrollY
                newTop = Math.max(newTop, 0)
                newTop = Math.min(newTop, height)
                view.setPadding(view.paddingLeft, -newTop, view.paddingRight, view.paddingBottom)
            }

        })
    }

    fun showUpdateDialog(activity: Activity, update: Update) {
        val contentView = LayoutInflater.from(activity).inflate(R.layout.dialog_update, null)
        val messageView = contentView.findViewById<TextView>(R.id.message)
        messageView.text = update.content
        val btnUpdateJump = contentView.findViewById<View>(R.id.btn_update_jump)
        val alertDialog = AlertDialog.Builder(activity, R.style.AlertDialog).setView(contentView).create()
        alertDialog.setCancelable(false)
        btnUpdateJump.setOnClickListener {
            alertDialog.dismiss()
        }
        //强制更新不能关闭
        if (update.force != null && true == update.force) {
            btnUpdateJump.visibility = View.INVISIBLE
        } else {
            btnUpdateJump.visibility = View.VISIBLE
        }
        val versionView = contentView.findViewById<TextView>(R.id.version)
        versionView.text = activity.getString(R.string.update_title, update.version)
        val downloadLayout = contentView.findViewById<View>(R.id.download_layout)
        val downloadPercentView = contentView.findViewById<TextView>(R.id.download_percent)
        val downloadCountView = contentView.findViewById<TextView>(R.id.download_count)
        val downloadSeekBar = contentView.findViewById<SeekBar>(R.id.seek_bar)
        val btnUpdate = contentView.findViewById<View>(R.id.btn_update)
        btnUpdate.setOnClickListener {
            val urlDownload = Runnable {
                alertDialog.dismiss()
                val uri = Uri.parse(UrlConfig.getUrlHome(activity))
                val intent = Intent(Intent.ACTION_VIEW, uri)
                activity.startActivity(intent)
            }
            if (!TextUtils.isEmpty(update.url)) {
                downloadApk(activity, ConstData.CACHE_PATH, update.url, object : DownloadListener {
                    override fun onStart() {
                        downloadLayout.visibility = View.VISIBLE
                        btnUpdateJump.visibility = View.INVISIBLE
                        btnUpdate.visibility = View.INVISIBLE
                    }

                    override fun onProgress(current: Long, total: Long) {
                        var percent: Float = if (total == 0L) 0f else current.toFloat() * 100 / total
                        percent = if (percent < 0) 0f else percent
                        downloadPercentView.text = String.format("%s%%", NumberUtil.formatNumberNoGroup(percent, 2, 2))
                        downloadCountView.text = String.format("%s/%s", CommonUtil.getFileSize(current), CommonUtil.getFileSize(total))
                        downloadSeekBar.progress = percent.toInt()
                    }

                    override fun onFinish(file: File) {
                        CookieUtil.setUpdateJumpVersion(activity, update.version)
                        alertDialog.dismiss()
                        installApk(activity, file)
                    }

                    override fun onFailure() {
                        btnUpdate.visibility = View.VISIBLE
                        if (update.force != null && true == update.force) {
                            btnUpdateJump.visibility = View.INVISIBLE
                        } else {
                            btnUpdateJump.visibility = View.VISIBLE
                        }
                        downloadLayout.visibility = View.INVISIBLE
                        urlDownload.run()
                        showToast(activity, activity.getString(R.string.apk_download_error))
                    }
                })
            } else {
                urlDownload.run()
            }
        }
        alertDialog.setOnDismissListener {
            if (!TextUtils.isEmpty(update.url)) {
                DownloadServiceHelper.cancelDownload(update.url, ConstData.CACHE_PATH)
            }
        }
        alertDialog.show()
    }

    private fun downloadApk(activity: Activity?, cachePath: String?, url: String?, downloadListener: DownloadListener?) {
        val file = File(ConstData.SD_DISK_PATH)
        if (!file.exists()) {
            file.mkdir()
        }
        val apkFile = File(ConstData.SD_DISK_PATH, "frying.apk")
        if (apkFile.exists()) {
            apkFile.delete()
        }
        DownloadServiceHelper.downloadFile(activity, cachePath, url, apkFile, downloadListener)
    }

    //下载完成  开始安装APK
    fun installApk(activity: Activity, apkFile: File?) {
        val intent = Intent(Intent.ACTION_VIEW)
        //放在此处
//由于没有在Activity环境下启动Activity,所以设置下面的标签
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val apkUri: Uri?
        //判断版本是否是 7.0 及 7.0 以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apkUri = FileProvider.getUriForFile(activity, "com.fbsex.exchange.fileProvider", apkFile!!)
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            apkUri = Uri.fromFile(apkFile)
        }
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        activity.startActivity(intent)
    }

    fun printError(throwable: Throwable?) {
        printError(null, throwable)
    }

    fun printError(context: Context?, throwable: Throwable?) {
        var context1 = context
        if (context1 == null) {
            context1 = BaseApplication.instance
        }
        CommonUtil.printError(context1, throwable)
    }

    //0 未知，1 正常，2 版本过低， 4 没有指纹API，8 没有指纹权限， 16 未设置界面开启密码锁屏功能 32 没有录入指纹
    fun getFingerPrintStatusText(context: Context, status: Int): String {
        return when (status) {
            1 -> context.getString(R.string.normal)
            2 -> context.getString(R.string.sdk_too_low)
            4 -> context.getString(R.string.no_api)
            8 -> context.getString(R.string.no_permission)
            16 -> context.getString(R.string.no_setting)
            32 -> context.getString(R.string.no_finger_print)
            else -> context.getString(R.string.unknown)
        }
    }

    fun clearAllUserInfo(context: Context) {
        CookieUtil.deleteUserInfo(context)
        CookieUtil.deleteToken(context)
        HttpCookieUtil.deleteCookies(context)
        CookieUtil.setMainEyeStatus(context, true)
        DataBaseUtil.clear(context)
        CookieUtil.setAccountProtectType(context, 0)
        CookieUtil.setGesturePassword(context, null)
        CookieUtil.setAccountProtectJump(context, false)
        CookieUtil.saveUserId(context, null)
        CookieUtil.saveUserName(context, null)
    }

    fun showSignError(activity: Activity?) {
//        AlertDialog dialog = new AlertDialog.Builder(activity)
//                .setMessage("检测到app为盗版，请到正规途径下载易健康！")
//                .setConfirmText("确认")
//                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                    @Override
//                    public void onClick(SweetAlertDialog sDialog) {
//                        sDialog.cancel();
//                    }
//                });
//        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                activity.finish();
//            }
//        });
//        dialog.show();
    }

    fun isReal(context: Context): Boolean {
        return UrlConfig.getIndex(context) == 0
    }

    //判断不可跳转通知的router,锁屏界面不可跳转
    fun cannotJumpNotificationRouter(currentActivity: Activity): Boolean {
        val route = currentActivity.javaClass.getAnnotation(Route::class.java)
        if (route != null) {
            val value: Array<String> = route.value
            for (uri in value) {
                if (RouterConstData.GESTURE_PASSWORD_CHECK == uri || RouterConstData.FINGER_PRINT_CHECK == uri || RouterConstData.START_PAGE == uri || RouterConstData.GESTURE_PASSWORD_SETTING == uri || RouterConstData.ACCOUNT_PROTECT.equals(uri, ignoreCase = true)
                        || RouterConstData.CHANGE_PASSWORD.equals(uri, ignoreCase = true)
                        || RouterConstData.FORGET_PASSWORD.equals(uri, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }

    fun getHtmlText(htmlText: String?): Spanned? {
        var textSpan: Spanned? = null
        textSpan = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(htmlText)
        }
        return textSpan
    }

    fun setHtmlText(textView: TextView?, text: String?) {
        if (textView == null) {
            return
        }
        if (TextUtils.isEmpty(text)) {
            textView.text = ""
        }
        var textSpan: Spanned? = null
        textSpan = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(text)
        }
        textView.text = textSpan
    }

    fun translateToHtmlTextAddColor(context: Context?, text: String, colorId: Int): String {
        if (TextUtils.isEmpty(text)) {
            return ""
        }
        val color = CommonUtil.toHexEncodingColor(SkinCompatResources.getColor(context, colorId))
        return "<font color=\"#$color\">$text</font>"
    }

    fun setCoinIcon(context: Context?, imageView: ImageView?, imageLoader: ImageLoader?, coinType: String?) {
        if (imageLoader == null || imageView == null) {
            return
        }
        WalletApiServiceHelper.getCoinInfo(context, coinType, object : Callback<CoinInfoType?>() {
            override fun error(type: Int, error: Any) {
                imageView.setImageBitmap(null)
            }

            override fun callback(returnData: CoinInfoType?) {
                if (returnData?.config?.get(0)?.coinConfigVO?.logosUrl != null) {
                    imageLoader.loadImage(imageView, returnData?.config?.get(0)?.coinConfigVO?.logosUrl!!)
                } else {
                    imageView.setImageBitmap(null)
                }
            }
        })
    }

    fun getCoinTypeIconRes(coinType: String?): Int {
        return if (coinType == null) {
            0
        } else when (coinType) {
            "BTC" -> R.drawable.icon_demand_btc
            "USDT" -> R.drawable.icon_demand_usdt
            "ETH" -> R.drawable.icon_demand_eth
            "EOS" -> R.drawable.icon_demand_eos
            "LTC" -> R.drawable.icon_demand_ltc
            else -> 0
        }
    }

    fun <T> observableWithHandler(handler: Handler?, data: T): Observable<T>? {
        return if (handler == null || handler.looper == null) {
            null
        } else Observable.just(data)
                .subscribeOn(AndroidSchedulers.from(handler.looper))
                .observeOn(AndroidSchedulers.mainThread())
    }

    //检查并同意杠杆交易协议
    fun checkAndAgreeLeverProtocol(context: Context, next: Runnable) {
        val userInfo = CookieUtil.getUserInfo(context) ?: return
        if (userInfo.openLever != null && true == userInfo.openLever) {
            next.run()
        } else {
            val color = SkinCompatResources.getColor(context, R.color.T7)
            val agreementText = "我已理解并同意<a href=\"" + UrlConfig.URL_LEVER_RULE + "\">《FBSEX Global 杠杆交易服务协议》</a>的全部内容"
            val agreementTextSpanned: Spanned?
            agreementTextSpanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(agreementText, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(agreementText)
            }
            val confirmDialog = ConfirmDialog(context, "同意服务协议", agreementTextSpanned, object : OnConfirmCallback {
                override fun onConfirmClick(confirmDialog: ConfirmDialog) {
                    ApiManager.build(context).getService(UserApiService::class.java)
                            ?.agreeLeverProtocol(true)
                            ?.compose(RxJavaHelper.observeOnMainThread())
                            ?.subscribe(HttpCallbackSimple(context, true, object : NormalCallback<HttpRequestResultString?>(context) {
                                override fun callback(returnData: HttpRequestResultString?) {
                                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                                        confirmDialog.dismiss()
                                        next.run()
                                        UserApiServiceHelper.getUserInfo(context, false, object : NormalCallback<HttpRequestResultData<UserInfo?>?>(context) {
                                            override fun callback(userResult: HttpRequestResultData<UserInfo?>?) {
                                                if (userResult?.code != null && userResult.code == HttpRequestResult.SUCCESS) {
                                                    if (userResult.data != null) {
                                                        CookieUtil.saveUserInfo(context, userResult.data)
                                                    }
                                                }
                                            }
                                        })
                                    } else {
                                        showToast(context, if (returnData == null) "null" else returnData.msg)
                                    }
                                }
                            }))
                }

            })
            confirmDialog.setTitleGravity(Gravity.LEFT)
            confirmDialog.setMessageGravity(Gravity.LEFT)
            confirmDialog.setConfirmText("同意")
            confirmDialog.messageView.movementMethod = BlackLinkMovementMethod(BlackLinkClickListener("服务协议"))
            confirmDialog.messageView.setLinkTextColor(color)
            confirmDialog.show()
        }
    }
}
