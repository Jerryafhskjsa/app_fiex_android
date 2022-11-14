package com.black.user.activity

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.UserApiServiceHelper
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivityGoogleGetKeyBinding
import com.black.util.CommonUtil
import com.google.zxing.WriterException

//获取谷歌密钥
@Route(value = [RouterConstData.GOOGLE_GET_KEY], beforePath = RouterConstData.LOGIN)
class GoogleGetKeyActivity : BaseActivity(), View.OnClickListener {
    private var binding: ActivityGoogleGetKeyBinding? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_google_get_key)
        binding?.btnCopy?.setOnClickListener(this)
        binding?.btnNext?.setOnClickListener(this)
        binding?.btnNext?.isEnabled = false
        googleKey
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.bind_google_title)
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.btn_copy) {
            if (CommonUtil.copyText(mContext, binding?.qrcodeText?.text.toString().trim { it <= ' ' })) {
                FryingUtil.showToast(mContext, getString(R.string.copy_text_success))
            } else {
                FryingUtil.showToast(mContext, getString(R.string.copy_text_failed))
            }
        } else if (i == R.id.btn_next) {
            BlackRouter.getInstance().build(RouterConstData.GOOGLE_BIND).go(this) { routeResult, _ ->
                if (routeResult) {
                    finish()
                }
            }
        }
    }//显示密钥，并进行下一步//解析参数//解析url参数，获取secret参数，即google密钥

    //获取谷歌密钥
    private val googleKey: Unit
        get() {
            UserApiServiceHelper.getGoogleKey(mContext, object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS && !TextUtils.isEmpty(returnData.data)) {
                        //解析url参数，获取secret参数，即google密钥
                        val uri = Uri.parse(returnData.data)
                        val secret = uri.getQueryParameter("secret") //解析参数
                        if (!TextUtils.isEmpty(returnData.data)) { //显示密钥，并进行下一步
                            var qrcodeBitmap: Bitmap? = null
                            try {
                                qrcodeBitmap = CommonUtil.createQRCode(returnData.data, 300, 0)
                            } catch (e: WriterException) {
                                CommonUtil.printError(mContext, e)
                            }
                            binding?.qrcodeImage?.setImageBitmap(qrcodeBitmap)
                            binding?.qrcodeText?.text = secret?.toString()
                            binding?.btnNext?.isEnabled = true
                        }
                    } else {
                        FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                        binding?.btnNext?.isEnabled = false
                    }
                }
            })
        }
}