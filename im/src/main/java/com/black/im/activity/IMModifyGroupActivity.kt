package com.black.im.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.im.R
import com.black.im.databinding.ActivityModifyGroupInfoBinding
import com.black.im.provider.GroupInfoProvider
import com.black.im.util.IUIKitCallBack
import com.black.router.annotation.Route
import com.tencent.imsdk.TIMCallBack
import com.tencent.imsdk.TIMGroupManager
import com.tencent.imsdk.TIMGroupMemberRoleType
import com.tencent.imsdk.TIMValueCallBack
import com.tencent.imsdk.ext.group.TIMGroupDetailInfoResult
import com.tencent.imsdk.ext.group.TIMGroupSelfInfo

//修改群名称、简介
@Route(value = [RouterConstData.IM_MODIFY_GROUP])
class IMModifyGroupActivity : BaseActionBarActivity(), View.OnClickListener {
    private var provider: GroupInfoProvider? = null
    private var groupId: String? = null

    private var binding: ActivityModifyGroupInfoBinding? = null

    private var isModify = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        provider = GroupInfoProvider()
        groupId = intent.getStringExtra(ConstData.IM_GROUP_ID)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_modify_group_info)
        binding?.groupName?.isEnabled = false
        binding?.groupIntroduction?.isEnabled = false
        binding?.btnModify?.setOnClickListener(this)
        binding?.btnModify?.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        groupInfo
        selfInfo
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return "修改群资料"
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btn_modify) {
            if (!isModify) {
                binding?.btnModify?.text = "保存"
                binding?.groupName?.isEnabled = true
                binding?.groupIntroduction?.isEnabled = true
                isModify = true
            } else {
                val groupName = binding?.groupName?.text.toString()
                if (groupName.trim { it <= ' ' }.isEmpty()) {
                    FryingUtil.showToastError(mContext, "请填写群名称")
                    return
                }
                val infoParam = TIMGroupManager.ModifyGroupInfoParam(groupId!!)
                //修改群名称
                infoParam.groupName = groupName
                //修改群简介
                infoParam.introduction = binding?.groupIntroduction?.text.toString()
                TIMGroupManager.getInstance().modifyGroupInfo(infoParam, object : TIMCallBack {
                    override fun onError(i: Int, s: String) {
                        FryingUtil.showToastError(mContext, s)
                    }

                    override fun onSuccess() {
                        FryingUtil.showToast(mContext, "修改成功")
                    }
                })
            }
        }
    }

    private val groupInfo: Unit
        get() {
            provider!!.loadGroupPublicInfo(groupId, object : IUIKitCallBack {
                override fun onSuccess(data: Any?) {
                    if (data is TIMGroupDetailInfoResult) {
                        showGroupDetail(data)
                    } else {
                        finish()
                    }
                }

                override fun onError(module: String?, errCode: Int, errMsg: String?) {
                    finish()
                }
            })
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
        if (selfInfo != null && (selfInfo.role == TIMGroupMemberRoleType.ROLE_TYPE_OWNER || selfInfo.role == TIMGroupMemberRoleType.ROLE_TYPE_ADMIN)) {
            binding?.btnModify?.visibility = View.VISIBLE
        } else {
            binding?.btnModify?.visibility = View.GONE
        }
    }

    private fun showGroupDetail(detail: TIMGroupDetailInfoResult) {
        binding?.groupName?.setText(if (TextUtils.isEmpty(detail.groupName)) "FBS聊天室" else detail.groupName)
        binding?.groupIntroduction?.setText(if (TextUtils.isEmpty(detail.groupIntroduction)) "" else detail.groupIntroduction)
    }
}