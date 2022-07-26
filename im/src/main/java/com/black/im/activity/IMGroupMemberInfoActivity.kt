package com.black.im.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.UserApiServiceHelper
import com.black.base.lib.FryingSingleToast
import com.black.base.model.HttpRequestResultString
import com.black.base.model.user.UserInfo
import com.black.base.util.*
import com.black.base.view.ConfirmDialog
import com.black.base.view.ConfirmDialog.OnConfirmCallback
import com.black.base.view.ImageSelectorHelper
import com.black.base.view.ImageSelectorHelper.OnImageGetListener
import com.black.im.R
import com.black.im.databinding.ActivityImGroupMenberInfoBinding
import com.black.im.util.IMHelper.getUserIdHeader
import com.black.im.util.IMHelper.imLogin
import com.black.im.util.IMHelper.updateAvatar
import com.black.lib.permission.PermissionHelper
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.Size
import com.tencent.imsdk.*
import com.tencent.imsdk.ext.group.TIMGroupSelfInfo
import java.io.File
import java.util.*

@Route(value = [RouterConstData.IM_GROUP_MEMBER])
class IMGroupMemberInfoActivity : BaseActionBarActivity(), View.OnClickListener, OnImageGetListener {
    private var userInfo: UserInfo? = null
    private var groupId: String? = null
    private var memberUserId: String? = null
    private var isSelf = false
    private var selfInfo: TIMGroupSelfInfo? = null
    private var memberInfo: TIMGroupMemberInfo? = null
    private var memberProfile: TIMUserProfile? = null
    private val userAvatar: String? = null
    private val nickName: String? = null

    private var binding: ActivityImGroupMenberInfoBinding? = null

    //    private View btnRemoveMessage;
    private var imageSelectorHelper: ImageSelectorHelper? = null
    private var imageLoader: ImageLoader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_im_group_menber_info)
        binding?.iconAvatar?.setOnClickListener(this)
        binding?.nickName?.setOnClickListener(this)
        binding?.btnSilence?.setOnClickListener(this)
        //        (btnRemoveMessage = findViewById(R.id.btn_remove_message)).setOnClickListener(this);

        binding?.btnChangeToManager?.setOnClickListener(this)
        userInfo = CookieUtil.getUserInfo(this)
        memberUserId = intent.getStringExtra(ConstData.IM_USER_ID)
        groupId = intent.getStringExtra(ConstData.IM_GROUP_ID)
        if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(memberUserId)) {
            finish()
        }
        val selfId = TIMManager.getInstance().loginUser
        isSelf = TextUtils.equals(selfId, memberUserId)
        binding?.nickName?.isEnabled = isSelf
        imageLoader = ImageLoader(this)
        imageSelectorHelper = ImageSelectorHelper(this)
        imageSelectorHelper?.addOnImageGetListener(this)
    }

    override fun onResume() {
        super.onResume()
        getSelfInfo()
        getMemberInfo()
        getMemberProfile()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return "个人信息"
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.icon_avatar) {
            val selfId = TIMManager.getInstance().loginUser
            if (TextUtils.equals(selfId, memberUserId)) {
                //自己修改头像
                val userIdHeader = getUserIdHeader(mContext)
                imLogin(mContext, userIdHeader + userInfo?.id, object : Callback<Boolean?>() {
                    override fun callback(returnData: Boolean?) {
                        if (returnData != null && returnData) {
                            imageSelectorHelper?.showSelectPhoto(mContext as Activity, mContext as PermissionHelper, Size(480, 480))
                        } else {
                            FryingUtil.showToast(mContext, "初始化失败，稍后重试！")
                        }
                    }

                    override fun error(type: Int, error: Any) {}
                })
            } else {
                //点击其他人查看大图
                if (memberProfile != null && !TextUtils.isEmpty(memberProfile?.faceUrl)) {
                    val bundle = Bundle()
                    bundle.putString(ConstData.URL, memberProfile?.faceUrl)
                    BlackRouter.getInstance().build(RouterConstData.SHOW_BIG_IMAGE).with(bundle).go(this)
                }
            }
        } else if (id == R.id.nick_name) {
            BlackRouter.getInstance().build(RouterConstData.NICK_NAME_CHANGE).go(mContext)
        } else if (id == R.id.btn_silence) {
            ConfirmDialog(this,
                    "提示", String.format("确定将 %s 禁言24小时", binding?.nickName?.text.toString()),
                    object : OnConfirmCallback {
                        override fun onConfirmClick(confirmDialog: ConfirmDialog) {
                            val param = TIMGroupManager.ModifyMemberInfoParam(groupId!!, memberUserId!!)
                            param.silence = 24 * 3600.toLong()
                            TIMGroupManager.getInstance().modifyMemberInfo(param, object : TIMCallBack {
                                override fun onError(code: Int, desc: String) {
                                    FryingUtil.showToast(mContext, "禁言失败，请稍后重试", FryingSingleToast.ERROR)
                                }

                                override fun onSuccess() {
                                    FryingUtil.showToast(mContext, "禁言成功")
                                    getMemberInfo()
                                    getSelfInfo()
                                }
                            })
                        }

                    })
                    .show()
        } else if (id == R.id.btn_change_to_manager) {
            val param = TIMGroupManager.ModifyMemberInfoParam(groupId!!, memberUserId!!)
            param.roleType = TIMGroupMemberRoleType.ROLE_TYPE_ADMIN
            TIMGroupManager.getInstance().modifyMemberInfo(param, object : TIMCallBack {
                override fun onError(code: Int, desc: String) {
                    FryingUtil.showToast(mContext, "设为管理员失败，请稍后重试", FryingSingleToast.ERROR)
                }

                override fun onSuccess() {
                    getMemberInfo()
                    getSelfInfo()
                }
            })
            //            TIMGroupManager.getInstance().modifyGroupOwner(groupId, memberUserId, new TIMCallBack() {
//                @Override
//                public void onError(int code, String desc) {
//                    FryingUtil.showToast(mContext, "转让群主失败，请稍后重试", FryingSingleToast.ERROR);
//                }
//
//                @Override
//                public void onSuccess() {
//                    getMemberInfo();
//                    getSelfInfo();
//                }
//            });
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imageSelectorHelper?.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun onImageGet(path: String?) {
        UserApiServiceHelper.uploadPublic(mContext, "file", File(path), object : NormalCallback<HttpRequestResultString?>() {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) { //上传成功
                    val avatarUrl = UrlConfig.getHost(mContext) + returnData.data
                    UserApiServiceHelper.modifyUserInfo(mContext, avatarUrl, null, object : NormalCallback<HttpRequestResultString?>() {
                        override fun callback(modifyResult: HttpRequestResultString?) {
                            if (modifyResult != null && modifyResult.code == HttpRequestResult.SUCCESS) {
                                val bitmap = ImageSelectorHelper.getImageAndSave(path, currentImagePath)
                                binding?.iconAvatar?.setImageBitmap(bitmap)
                                getUserInfo(object : NormalCallback<UserInfo?>() {
                                    override fun callback(returnData: UserInfo?) {
                                        userInfo = returnData
                                    }
                                })
                            } else {
                                FryingUtil.showToast(mContext, modifyResult?.msg)
                            }
                        }
                    })
                    updateAvatar(avatarUrl, object : Callback<Boolean?>() {
                        override fun error(type: Int, error: Any) {
                            FryingUtil.showToast(mContext, "头像同步失败")
                        }

                        override fun callback(returnData: Boolean?) {
                            FryingUtil.showToast(mContext, "头像同步成功")
                            getMemberInfo()
                        }
                    })
                } else {
                    FryingUtil.showToast(mContext, returnData?.msg)
                }
            }
        })
    }

    private val currentImagePath: String
        get() = CommonUtil.getCatchFilePath(this) + File.separator + ConstData.TEMP_IMG_NAME_01

    private fun getSelfInfo() {
        TIMGroupManager.getInstance().getSelfInfo(groupId!!, object : TIMValueCallBack<TIMGroupSelfInfo?> {
            override fun onError(i: Int, s: String) {
                FryingUtil.showToast(mContext, "获取用户信息失败，请稍后重试", FryingSingleToast.ERROR)
                finish()
            }

            override fun onSuccess(timGroupSelfInfo: TIMGroupSelfInfo?) {
                selfInfo = timGroupSelfInfo
                refreshSelfInfo()
            }
        })
    }

    private fun refreshSelfInfo() {
        if (selfInfo == null) {
            FryingUtil.showToast(mContext, "获取用户信息失败，请稍后重试", FryingSingleToast.ERROR)
            finish()
            return
        }
        checkButtonChangeToManager()
    }

    private fun getMemberInfo() {
        val otherUsers = ArrayList<String?>()
        otherUsers.add(memberUserId)
        TIMGroupManager.getInstance().getGroupMembersInfo(groupId!!, otherUsers, object : TIMValueCallBack<List<TIMGroupMemberInfo>?> {
            override fun onError(i: Int, s: String) {
                FryingUtil.showToast(mContext, "获取用户信息失败，请稍后重试", FryingSingleToast.ERROR)
                finish()
            }

            override fun onSuccess(timGroupMemberInfos: List<TIMGroupMemberInfo>?) {
                memberInfo = null
                if (timGroupMemberInfos != null) {
                    for (timGroupMemberInfo in timGroupMemberInfos) {
                        if (TextUtils.equals(timGroupMemberInfo.user, memberUserId)) {
                            memberInfo = timGroupMemberInfo
                            break
                        }
                    }
                }
                refreshMemberInfo()
            }
        })
    }

    private fun refreshMemberInfo() {
        if (memberInfo == null) {
            FryingUtil.showToast(mContext, "获取用户信息失败，请稍后重试", FryingSingleToast.ERROR)
            finish()
            return
        }
        checkButtonChangeToManager()
    }

    private fun getMemberProfile() {
        val idList: MutableList<String?> = ArrayList()
        idList.add(memberUserId)
        TIMFriendshipManager.getInstance().getUsersProfile(idList, true, object : TIMValueCallBack<List<TIMUserProfile?>?> {
            override fun onError(i: Int, s: String) {}
            override fun onSuccess(timUserProfiles: List<TIMUserProfile?>?) {
                refreshMemberProfile()
            }
        })
    }

    private fun refreshMemberProfile() {
        memberProfile = TIMFriendshipManager.getInstance().queryUserProfile(memberUserId)
        if (memberProfile != null && memberProfile?.faceUrl != null && binding?.iconAvatar != null) {
            imageLoader?.loadImage(binding?.iconAvatar!!, memberProfile?.faceUrl, R.drawable.icon_avatar, true)
        }
        binding?.nickName?.text = if (memberProfile == null || memberProfile?.nickName == null) "" else memberProfile?.nickName
    }

    private fun checkButtonChangeToManager() {
        if (selfInfo == null || memberInfo == null || isSelf) {
            binding?.btnChangeToManager?.visibility = View.GONE
            binding?.btnSilence?.visibility = View.GONE
            //            btnRemoveMessage.setVisibility(View.GONE);
        } else {
            val sefRole = selfInfo?.role
            val memberRole = memberInfo?.role
            if (!isSelf &&
                    (sefRole == TIMGroupMemberRoleType.ROLE_TYPE_OWNER ||
                            sefRole == TIMGroupMemberRoleType.ROLE_TYPE_ADMIN && memberRole != TIMGroupMemberRoleType.ROLE_TYPE_OWNER && memberRole != TIMGroupMemberRoleType.ROLE_TYPE_ADMIN)) {
                binding?.btnSilence?.visibility = View.VISIBLE
                //                btnRemoveMessage.setVisibility(View.VISIBLE);
            } else {
                binding?.btnSilence?.visibility = View.GONE
                //                btnRemoveMessage.setVisibility(View.GONE);
            }
            if (sefRole == TIMGroupMemberRoleType.ROLE_TYPE_OWNER && memberRole != TIMGroupMemberRoleType.ROLE_TYPE_OWNER && memberRole != TIMGroupMemberRoleType.ROLE_TYPE_ADMIN) {
                binding?.btnChangeToManager?.visibility = View.VISIBLE
            } else {
                binding?.btnChangeToManager?.visibility = View.GONE
            }
        }
    }
}