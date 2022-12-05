package com.black.clutter.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.CommonApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.Update
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.clutter.R
import com.black.clutter.databinding.ActivityAboutUsBinding
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil

@Route(value = [RouterConstData.ABOUT])
class AboutUsActivity : BaseActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityAboutUsBinding = DataBindingUtil.setContentView(this, R.layout.activity_about_us)
        binding.currentVersion.setText(String.format("FIEX V%s", CommonUtil.getVersionName(this, "1.0.0")))
        binding.checkUpdate.setOnClickListener(this)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.about_us)
    }

    override fun onClick(view: View) {
        val i = view.id
        if ( i == R.id.check_update){
            checkUpdate(false)
        }
    }

    fun checkUpdate(silent: Boolean) {
        CommonApiServiceHelper.checkUpdate(this, !silent, object : Callback<HttpRequestResultData<Update?>?>() {
            override fun error(type: Int, error: Any) {
                FryingUtil.showToast(mContext, error.toString())
            }

            override fun callback(returnData: HttpRequestResultData<Update?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    val update = returnData.data ?: return
                    if (update.version != null && update.version != CommonUtil.getVersionName(mContext, null)) {
                        //需要更新
                        FryingUtil.showUpdateDialog(mContext as Activity, update)
                    } else {
                        if (!silent) {
                            FryingUtil.showToast(mContext, getString(R.string.last_version))
                        }
                    }
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) getString(R.string.error_data) else returnData.msg)
                }
            }
        })
    }
}