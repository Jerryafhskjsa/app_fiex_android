package com.black.clutter.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActivity
import com.black.base.api.CommonApiServiceHelper
import com.black.base.model.FryingLinesConfig
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.Update
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.clutter.R
import com.black.clutter.databinding.ActivityAboutUsBinding
import com.black.clutter.databinding.ActivityXainLuBinding
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil

@Route(value = [RouterConstData.XIANLU])
class XianLuActivity : BaseActivity(), View.OnClickListener {
    private val fryingLinesConfig: MutableList<FryingLinesConfig?> = ArrayList()
    private val localLinesConfig: MutableList<FryingLinesConfig?> = ArrayList()
    private var binding: ActivityXainLuBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_xain_lu)
        binding?.line1?.setOnClickListener(this)
        binding?.line2?.setOnClickListener(this)
        getNetworkLines(false)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return "线路切换"
    }

    override fun onClick(view: View) {
        val i = view.id
        if ( i == R.id.line1){

        }
        if (i == R.id.line2){

        }
    }

    private fun getNetworkLines(showDialog: Boolean?) {
        CommonApiServiceHelper.getNetworkLines(
            this,
            object : Callback<HttpRequestResultDataList<FryingLinesConfig?>?>() {
                override fun error(type: Int, error: Any) {
                    if (localLinesConfig.size > 0) {
                        getLineSpeed(0, 0, showDialog, localLinesConfig[0])
                    }
                }

                override fun callback(returnData: HttpRequestResultDataList<FryingLinesConfig?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        var lines = returnData.data ?: return
                        binding?.line1?.setText(lines[0]?.lineUrl)
                        binding?.shudu?.setText(lines[0]?.speed)
                        fryingLinesConfig.clear()
                        var lineUrls:ArrayList<String?> = ArrayList()
                        for (i in lines.indices){
                            var config = lines[i]
                            config?.index = i
                            fryingLinesConfig.add(i,config)
                            lineUrls.add(config?.lineUrl)
                        }
                        if(fryingLinesConfig.size > 0){
                            CookieUtil.setServerHost(mContext,lineUrls)
                            getLineSpeed(0, 1, showDialog, fryingLinesConfig[0])
                        }
                    }
                }
            })
    }

    /**
     * type 0本地 1网络
     */
    private fun getLineSpeed(
        index: Int?,
        netType: Int?,
        showDialog: Boolean?,
        linesConfig: FryingLinesConfig?
    ) {
        linesConfig?.startTime = System.currentTimeMillis()
        CommonApiServiceHelper.getLinesSpeed(
            this,
            linesConfig?.lineUrl,
            object : Callback<HttpRequestResultString?>() {
                override fun error(type: Int, error: Any) {
                    linesConfig?.statusDes = getString(R.string.link_line_exception)
                    linesConfig?.endTime = Long.MAX_VALUE
                    when (netType) {
                        0 -> {
                            if (localLinesConfig.size > 0 && localLinesConfig.size-1 == index) {
                                if (showDialog == true) {
                                   // showServerDialog(netType)
                                }
                                //displayCurrentServer()
                            }else{
                                if (index != null) {
                                    getLineSpeed(
                                        index + 1,
                                        netType,
                                        showDialog,
                                        localLinesConfig[index + 1]
                                    )
                                }
                            }
                        }
                        1 -> {
                            if (fryingLinesConfig.size > 0 && fryingLinesConfig.size-1  == index) {
                                if (showDialog == true) {
                                    //showServerDialog(netType)
                                }
                               // displayCurrentServer()
                            }else{
                                if (index != null) {
                                    getLineSpeed(
                                        index + 1,
                                        netType,
                                        showDialog,
                                        fryingLinesConfig[index + 1]
                                    )
                                }
                            }
                        }
                    }
                }

                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        linesConfig?.endTime = System.currentTimeMillis()
                        linesConfig?.statusDes = getString(R.string.link_line_normal)
                        when (netType) {
                            0 -> {
                                if (localLinesConfig.size > 0 && localLinesConfig.size-1  == index) {
                                    if (showDialog == true) {
                                        //showServerDialog(netType)
                                    }
                                    //displayCurrentServer()
                                }else{
                                    if (index != null) {
                                        getLineSpeed(
                                            index + 1,
                                            netType,
                                            showDialog,
                                            localLinesConfig[index + 1]
                                        )
                                    }
                                }
                            }
                            1 -> {
                                if (fryingLinesConfig.size > 0 && fryingLinesConfig.size-1  == index) {
                                    if (showDialog == true) {
                                        //showServerDialog(netType)
                                    }
                                    //displayCurrentServer()
                                }else{
                                    if (index != null) {
                                        getLineSpeed(
                                            index + 1,
                                            netType,
                                            showDialog,
                                            fryingLinesConfig[index + 1]
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            })
    }
}