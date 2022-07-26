package com.black.user.activity

import android.os.Bundle
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.UserApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.user.PushSwitch
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.user.R
import com.black.user.databinding.ActivityPushSettingBinding
import skin.support.content.res.SkinCompatResources

@Route(value = [RouterConstData.PUSH_SETTING], beforePath = RouterConstData.LOGIN)
class PushSettingActivity : BaseActionBarActivity(), CompoundButton.OnCheckedChangeListener {
    private var binding: ActivityPushSettingBinding? = null
    private var pushSwitch: PushSwitch? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_push_setting)
        binding?.rechargePush?.trackDrawable = SkinCompatResources.getDrawable(this, R.drawable.bg_switch_track)
        binding?.rechargePush?.thumbDrawable = SkinCompatResources.getDrawable(this, R.drawable.icon_switch_thumb)
        binding?.rechargePush?.setOnCheckedChangeListener(this)
        binding?.extractPush?.trackDrawable = SkinCompatResources.getDrawable(this, R.drawable.bg_switch_track)
        binding?.extractPush?.thumbDrawable = SkinCompatResources.getDrawable(this, R.drawable.icon_switch_thumb)
        binding?.extractPush?.setOnCheckedChangeListener(this)
        binding?.tradePush?.trackDrawable = SkinCompatResources.getDrawable(this, R.drawable.bg_switch_track)
        binding?.tradePush?.thumbDrawable = SkinCompatResources.getDrawable(this, R.drawable.icon_switch_thumb)
        binding?.tradePush?.setOnCheckedChangeListener(this)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return "推送提醒设置"
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        val id = buttonView.id
        if (id == R.id.recharge_push) {
            if (pushSwitch != null && pushSwitch?.investSwitch != null) {
                if (isChecked != pushSwitch?.investSwitch) {
                    changeRechargeSwitch(isChecked)
                }
            } else {
                refreshSwitch()
            }
        } else if (id == R.id.extract_push) {
            if (pushSwitch != null && pushSwitch?.withdrawSwitch != null) {
                if (isChecked != pushSwitch?.withdrawSwitch) {
                    changeExtractSwitch(isChecked)
                }
            } else {
                refreshSwitch()
            }
        } else if (id == R.id.trade_push) {
            if (pushSwitch != null && pushSwitch?.orderSwitch != null) {
                if (isChecked != pushSwitch?.orderSwitch) {
                    changeTradeSwitch(isChecked)
                }
            } else {
                refreshSwitch()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        UserApiServiceHelper.getPushSwitchList(this, object : NormalCallback<HttpRequestResultData<PushSwitch?>?>() {
            override fun callback(returnData: HttpRequestResultData<PushSwitch?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    pushSwitch = returnData.data
                    refreshSwitch()
                } else {
                    FryingUtil.showToast(mContext, returnData?.msg)
                }
            }
        })
    }

    private fun refreshSwitch() {
        binding?.rechargePush?.isChecked = pushSwitch != null && pushSwitch?.investSwitch != null && pushSwitch?.investSwitch!!
        binding?.extractPush?.isChecked = pushSwitch != null && pushSwitch?.withdrawSwitch != null && pushSwitch?.withdrawSwitch!!
        binding?.tradePush?.isChecked = pushSwitch != null && pushSwitch?.orderSwitch != null && pushSwitch?.orderSwitch!!
    }

    private fun changeTradeSwitch(isChecked: Boolean) {
        val submitPushSwitch = PushSwitch.copyPushSwitch(pushSwitch)
        if (submitPushSwitch != null) {
            submitPushSwitch.orderSwitch = isChecked
            changeSwitch(submitPushSwitch)
        }
    }

    private fun changeExtractSwitch(isChecked: Boolean) {
        val submitPushSwitch = PushSwitch.copyPushSwitch(pushSwitch)
        if (submitPushSwitch != null) {
            submitPushSwitch.withdrawSwitch = isChecked
            changeSwitch(submitPushSwitch)
        } else {
            refreshSwitch()
        }
    }

    private fun changeRechargeSwitch(isChecked: Boolean) {
        val submitPushSwitch = PushSwitch.copyPushSwitch(pushSwitch)
        if (submitPushSwitch != null) {
            submitPushSwitch.investSwitch = isChecked
            changeSwitch(submitPushSwitch)
        }
    }

    private fun changeSwitch(submitPushSwitch: PushSwitch?) {
        if (submitPushSwitch != null) {
            UserApiServiceHelper.modifyPushSwitch(this,
                    submitPushSwitch.orderSwitch != null && true == submitPushSwitch.orderSwitch,
                    submitPushSwitch.investSwitch != null && true == submitPushSwitch.investSwitch,
                    submitPushSwitch.withdrawSwitch != null && true == submitPushSwitch.withdrawSwitch,
                    object : NormalCallback<HttpRequestResultString?>() {
                        override fun callback(returnData: HttpRequestResultString?) {
                            if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                                pushSwitch = PushSwitch.copyPushSwitch(submitPushSwitch)
                                refreshSwitch()
                            } else {
                                FryingUtil.showToast(mContext, returnData?.msg)
                            }
                        }
                    }
            )
        }
    }
}