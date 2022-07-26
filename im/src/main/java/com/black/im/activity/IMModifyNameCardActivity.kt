package com.black.im.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.lib.FryingSingleToast
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.im.R
import com.black.im.databinding.ActivityModifyNameCardBinding
import com.black.router.annotation.Route
import com.tencent.imsdk.TIMCallBack
import com.tencent.imsdk.TIMGroupManager
import com.tencent.imsdk.TIMManager
import com.tencent.imsdk.TIMValueCallBack
import com.tencent.imsdk.ext.group.TIMGroupSelfInfo

//修改群名片
@Route(value = [RouterConstData.IM_MODIFY_NAME_CARD])
class IMModifyNameCardActivity : BaseActionBarActivity(), View.OnClickListener {
    private var groupId: String? = null

    private var binding: ActivityModifyNameCardBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        groupId = intent.getStringExtra(ConstData.IM_GROUP_ID)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_modify_name_card)
        binding?.btnModify?.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        selfInfo
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return "修改群名片"
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btn_modify) {
            val param = TIMGroupManager.ModifyMemberInfoParam(groupId!!, TIMManager.getInstance().loginUser)
            param.nameCard = binding?.nameCard?.text.toString()
            TIMGroupManager.getInstance().modifyMemberInfo(param, object : TIMCallBack {
                override fun onError(i: Int, s: String) {
                    FryingUtil.showToast(mContext, s, FryingSingleToast.ERROR)
                }

                override fun onSuccess() {
                    FryingUtil.showToast(mContext, "修改成功")
                }
            })
        }
    }

    private val selfInfo: Unit
        get() {
            TIMGroupManager.getInstance().getSelfInfo(groupId!!, object : TIMValueCallBack<TIMGroupSelfInfo?> {
                override fun onError(i: Int, s: String) {
                    showSelfInfo(null)
                }

                override fun onSuccess(timGroupSelfInfo: TIMGroupSelfInfo?) {
                    showSelfInfo(timGroupSelfInfo)
                }
            })
        }

    private fun showSelfInfo(selfInfo: TIMGroupSelfInfo?) {
        binding?.nameCard?.setText(if (selfInfo == null || TextUtils.isEmpty(selfInfo.nameCard)) "" else selfInfo.nameCard)
    }
}