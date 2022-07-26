package com.black.im.util

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.lib.permission.PermissionHelper
import com.black.router.BlackRouter
import com.black.util.Callback
import com.tencent.imsdk.*
import com.tencent.imsdk.ext.group.TIMGroupBaseInfo
import com.tencent.imsdk.ext.group.TIMGroupSelfInfo
import java.util.*

object IMHelper {
    private var IS_LOGIN = false
    fun getUserIdHeader(context: Context): String {
        return "fbs_" + if (FryingUtil.isReal(context)) "REAL_" else "DEV_"
    }

    fun getSign(context: Context, userId: String?, callback: Callback<String?>) {
        val userInfo = CookieUtil.getUserInfo(context)
        val userSig = userInfo?.timUserSig
        if (TextUtils.isEmpty(userSig)) {
            callback.error(403, "must login")
        } else {
            callback.callback(userSig)
        }
    }

    fun imLogin(context: Context?, userId: String?, callback: Callback<Boolean?>?) {
        if (context == null || callback == null) {
            return
        }
        if (IS_LOGIN) {
            callback.callback(true)
        } else {
            getSign(context, userId, object : Callback<String?>() {
                override fun error(type: Int, error: Any) {
                    IS_LOGIN = false
                    callback.error(type, error)
                }

                override fun callback(userSig: String?) {
                    TUIKit.login(userId, userSig, object : IUIKitCallBack {
                        override fun onError(module: String?, code: Int, desc: String?) {
                            IS_LOGIN = false
                            callback.callback(false)
                        }

                        override fun onSuccess(data: Any?) {
                            IS_LOGIN = true
                            callback.callback(true)
                        }
                    })
                }
            })
        }
    }

    fun logout(context: Context, autoLogin: Boolean) {
        IS_LOGIN = false
        IMPreference.saveLoginStatus(context, false)
    }

    fun startWithIMActivity(permissionHelper: PermissionHelper, context: Context?, imUserId: String?, routeUri: String?, bundle: Bundle?, requestCode: Int?, flags: Int?) {
        permissionHelper.requestCameraPermissions(Runnable {
            permissionHelper.requestStoragePermissions(Runnable {
                permissionHelper.requestMicrophonePermissions(Runnable {
                    imLogin(context, imUserId, object : Callback<Boolean?>() {
                        override fun callback(returnData: Boolean?) {
                            if (returnData != null && returnData) {
                                val router = BlackRouter.getInstance().build(routeUri)
                                if (bundle != null && !bundle.isEmpty) {
                                    router.with(bundle)
                                }
                                if (requestCode != null) {
                                    router.withRequestCode(requestCode)
                                }
                                if (flags != null) {
                                    router.addFlags(flags)
                                }
                                router.go(context)
                            } else {
                                FryingUtil.showToast(context, "初始化失败，稍后重试！")
                            }
                        }

                        override fun error(type: Int, error: Any) {}
                    })
                })
            })
        })
    }

    fun startWithIMGroupActivity(permissionHelper: PermissionHelper, context: Context?, imUserId: String?, groupId: String?, routeUri: String?, bundle: Bundle, requestCode: Int?, flags: Int?) {
        if (TextUtils.isEmpty(groupId)) {
            return
        }
        permissionHelper.requestCameraPermissions(Runnable {
            permissionHelper.requestStoragePermissions(Runnable {
                permissionHelper.requestMicrophonePermissions(object : Runnable {
                    var groupInfo: TIMGroupBaseInfo? = null
                    private fun startActivity() {
                        val router = BlackRouter.getInstance().build(routeUri)
                        var bundle1 = bundle.clone() as Bundle
                        if (bundle1 == null) {
                            bundle1 = Bundle()
                        }
                        if (groupInfo != null) {
                            bundle1.putString(ConstData.IM_GROUP_NAME, groupInfo!!.groupName)
                            bundle1.putString(ConstData.IM_GROUP_ID, groupInfo!!.groupId)
                        }
                        if (!bundle1.isEmpty) {
                            router.with(bundle1)
                        }
                        if (requestCode != null) {
                            router.withRequestCode(requestCode)
                        }
                        if (flags != null) {
                            router.addFlags(flags)
                        }
                        router.go(context)
                    }

                    override fun run() {
                        imLogin(context, imUserId, object : Callback<Boolean?>() {
                            override fun callback(returnData: Boolean?) {
                                if (returnData != null && returnData) {
                                    TIMGroupManager.getInstance().getSelfInfo(groupId!!, object : TIMValueCallBack<TIMGroupSelfInfo?> {
                                        override fun onError(i: Int, s: String) {
                                            TIMGroupManager.getInstance().applyJoinGroup(groupId, "", object : TIMCallBack {
                                                override fun onError(code: Int, error: String) {
                                                    if (10013 == code) { //已加入群组
                                                        startActivity()
                                                    } else {
                                                        FryingUtil.showToast(context, "进入失败，请稍后重试！")
                                                    }
                                                }

                                                override fun onSuccess() {
                                                    startActivity()
                                                }
                                            })
                                        }

                                        override fun onSuccess(timGroupSelfInfo: TIMGroupSelfInfo?) {
                                            startActivity()
                                        }
                                    })
                                    //                                            TIMGroupManager.getInstance().getGroupList(new TIMValueCallBack<List<TIMGroupBaseInfo>>() {
                                    //                                                @Override
                                    //                                                public void onError(int i, String s) {
                                    //                                                    FryingUtil.showToast(context, "进入失败，请稍后重试！");
                                    //                                                }
                                    //
                                    //                                                @Override
                                    //                                                public void onSuccess(List<TIMGroupBaseInfo> timGroupBaseInfos) {
                                    //                                                    boolean isJoin = false;
                                    //                                                    if (timGroupBaseInfos != null && !timGroupBaseInfos.isEmpty()) {
                                    //                                                        for (TIMGroupBaseInfo groupBaseInfo : timGroupBaseInfos) {
                                    //                                                            if (TextUtils.equals(groupBaseInfo.getGroupId(), groupId)) {
                                    //                                                                groupInfo = groupBaseInfo;
                                    //                                                                isJoin = true;
                                    //                                                                break;
                                    //                                                            }
                                    //
                                    //                                                        }
                                    //                                                    }
                                    //                                                    if (isJoin) {
                                    //                                                        startActivity();
                                    //                                                    } else {
                                    //                                                        TIMGroupManager.getInstance().applyJoinGroup(groupId, "", new TIMCallBack() {
                                    //
                                    //                                                            @Override
                                    //                                                            public void onError(int i, String s) {
                                    //                                                                FryingUtil.showToast(context, "进入失败，请稍后重试！");
                                    //                                                            }
                                    //
                                    //                                                            @Override
                                    //                                                            public void onSuccess() {
                                    //                                                                startActivity();
                                    //                                                            }
                                    //                                                        });
                                    //                                                    }
                                    //                                                }
                                    //                                            });
                                } else {
                                    FryingUtil.showToast(context, "初始化失败，稍后重试！")
                                }
                            }

                            override fun error(type: Int, error: Any) {
                                FryingUtil.showToast(context, "初始化失败，稍后重试！")
                            }
                        })
                    }
                })
            })
        })
    }

    fun getUserUID(userId: String?): String? {
        if (TextUtils.isEmpty(userId)) {
            return userId
        }
        val idArr = userId!!.split("_").toTypedArray()
        return if (idArr != null && idArr.size > 0) {
            idArr[idArr.size - 1]
        } else userId
    }

    fun updateAvatar(avatarUrl: String?, callback: Callback<Boolean?>?) {
        updateProfile(avatarUrl, null, null, null, null, callback)
    }

    fun updateNickName(nickName: String?, callback: Callback<Boolean?>?) {
        updateProfile(null, nickName, null, null, null, callback)
    }

    fun updateProfile(avatarUrl: String?, nickName: String?, signature: String?, location: String?, allowType: String?, callback: Callback<Boolean?>?) {
        val hashMap = HashMap<String, Any?>()
        // 头像
        if (!TextUtils.isEmpty(avatarUrl)) {
            hashMap[TIMUserProfile.TIM_PROFILE_TYPE_KEY_FACEURL] = avatarUrl
        }
        // 昵称
        if (!TextUtils.isEmpty(nickName)) {
            hashMap[TIMUserProfile.TIM_PROFILE_TYPE_KEY_NICK] = nickName
        }
        // 个性签名
        if (!TextUtils.isEmpty(signature)) {
            hashMap[TIMUserProfile.TIM_PROFILE_TYPE_KEY_SELFSIGNATURE] = signature
        }
        // 地区
        hashMap[TIMUserProfile.TIM_PROFILE_TYPE_KEY_LOCATION] = if (!TextUtils.isEmpty(location)) location else "sz" // TODO 不加SDK会有个崩溃
        // 加我验证方式
        if (!TextUtils.isEmpty(allowType)) {
            hashMap[TIMUserProfile.TIM_PROFILE_TYPE_KEY_ALLOWTYPE] = allowType
        }
        TIMFriendshipManager.getInstance().modifySelfProfile(hashMap, object : TIMCallBack {
            override fun onError(i: Int, s: String) {
                callback?.error(ConstData.ERROR_NORMAL, s)
            }

            override fun onSuccess() {
                callback?.callback(true)
            }
        })
    }
}
