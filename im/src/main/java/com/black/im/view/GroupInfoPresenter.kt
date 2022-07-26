package com.black.im.view

import android.app.Activity
import com.black.im.provider.GroupInfoProvider
import com.black.im.util.IUIKitCallBack
import com.black.im.util.TUIKitConstants
import com.black.im.util.TUIKitLog
import com.black.im.util.ToastUtil.toastLongMessage
import com.black.im.widget.GroupInfoLayout

class GroupInfoPresenter(private val mInfoLayout: GroupInfoLayout) {
    private val mProvider: GroupInfoProvider = GroupInfoProvider()

    fun loadGroupInfo(groupId: String?, callBack: IUIKitCallBack) {
        mProvider.loadGroupInfo(groupId, object : IUIKitCallBack {
            override fun onSuccess(data: Any?) {
                callBack.onSuccess(data)
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {
                TUIKitLog.e("loadGroupInfo", "$errCode:$errMsg")
                callBack.onError(module, errCode, errMsg)
                toastLongMessage(errMsg)
            }
        })
    }

    fun modifyGroupName(name: String?) {
        mProvider.modifyGroupInfo(name!!, TUIKitConstants.Group.MODIFY_GROUP_NAME, object : IUIKitCallBack {
            override fun onSuccess(data: Any?) {
                mInfoLayout.onGroupInfoModified(name, TUIKitConstants.Group.MODIFY_GROUP_NAME)
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {
                TUIKitLog.e("modifyGroupName", "$errCode:$errMsg")
                toastLongMessage(errMsg)
            }
        })
    }

    fun modifyGroupNotice(notice: String?) {
        mProvider.modifyGroupInfo(notice!!, TUIKitConstants.Group.MODIFY_GROUP_NOTICE, object : IUIKitCallBack {
            override fun onSuccess(data: Any?) {
                mInfoLayout.onGroupInfoModified(notice, TUIKitConstants.Group.MODIFY_GROUP_NOTICE)
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {
                TUIKitLog.e("modifyGroupNotice", "$errCode:$errMsg")
                toastLongMessage(errMsg)
            }
        })
    }

    val nickName: String
        get() {
            var nickName: String? = ""
            if (mProvider.selfGroupInfo != null) {
                if (mProvider.selfGroupInfo?.detail != null) {
                    nickName = mProvider.selfGroupInfo?.detail?.nameCard
                }
            }
            return nickName ?: ""
        }

    fun modifyMyGroupNickname(nickname: String?) {
        mProvider.modifyMyGroupNickname(nickname, object : IUIKitCallBack {
            override fun onSuccess(data: Any?) {
                mInfoLayout.onGroupInfoModified(nickname
                        ?: "", TUIKitConstants.Group.MODIFY_MEMBER_NAME)
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {
                TUIKitLog.e("modifyMyGroupNickname", "$errCode:$errMsg")
                toastLongMessage(errMsg)
            }
        })
    }

    fun deleteGroup() {
        mProvider.deleteGroup(object : IUIKitCallBack {
            override fun onSuccess(data: Any?) {
                (mInfoLayout.context as Activity).finish()
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {
                TUIKitLog.e("deleteGroup", "$errCode:$errMsg")
                toastLongMessage(errMsg)
            }
        })
    }

    fun setTopConversation(flag: Boolean) {
        mProvider.setTopConversation(flag)
    }

    fun quitGroup() {
        mProvider.quitGroup(object : IUIKitCallBack {
            override fun onSuccess(data: Any?) {
                (mInfoLayout.context as Activity).finish()
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {
                (mInfoLayout.context as Activity).finish()
                TUIKitLog.e("quitGroup", "$errCode:$errMsg")
            }
        })
    }

    fun modifyGroupInfo(value: Int, type: Int) {
        mProvider.modifyGroupInfo(value, type, object : IUIKitCallBack {
            override fun onSuccess(data: Any?) {
                mInfoLayout.onGroupInfoModified(data
                        ?: 0, TUIKitConstants.Group.MODIFY_GROUP_JOIN_TYPE)
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {
                toastLongMessage("modifyGroupInfo fail :$errCode=$errMsg")
            }
        })
    }
}