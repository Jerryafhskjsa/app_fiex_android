package com.black.im.util

import android.content.Context
import com.black.im.config.GeneralConfig
import com.black.im.config.TUIKitConfigs
import com.black.im.interfaces.IMEventListener
import com.black.im.manager.C2CChatManagerKit
import com.black.im.manager.ConversationManagerKit
import com.black.im.manager.FaceManager
import com.black.im.manager.MessageRevokedManager
import com.black.im.util.FileUtil.initPath
import com.tencent.imsdk.*
import com.tencent.imsdk.ext.message.TIMMessageReceiptListener
import com.tencent.imsdk.friendship.TIMFriendPendencyInfo
import com.tencent.imsdk.friendship.TIMFriendshipListener
import java.util.*

object TUIKitImpl {
    private val TAG = TUIKitImpl::class.java.simpleName
    var appContext: Context? = null
        private set
    private var sConfigs: TUIKitConfigs? = null
    private val sIMEventListeners: MutableList<IMEventListener> = ArrayList()
    var defaultAvatarSize = 0
        private set

    /**
     * TUIKit的初始化函数
     *
     * @param context  应用的上下文，一般为对应应用的ApplicationContext
     * @param sdkAppID 您在腾讯云注册应用时分配的sdkAppID
     * @param configs  TUIKit的相关配置项，一般使用默认即可，需特殊配置参考API文档
     */
    fun init(context: Context, sdkAppID: Int, configs: TUIKitConfigs?) {
        TUIKitLog.e("TUIKit", "init tuikit version: " + BuildConfig.VERSION_NAME)
        appContext = context
        sConfigs = configs
        if (sConfigs?.generalConfig == null) {
            val generalConfig = GeneralConfig()
            sConfigs?.setGeneralConfig(generalConfig)
        }
        sConfigs?.generalConfig?.setAppCacheDir( context.filesDir.path)
        initIM(context, sdkAppID)
        val displayMetrics = context.resources.displayMetrics
        defaultAvatarSize = (displayMetrics.density * 40).toInt()
        BackgroundTasks.initInstance()
        initPath() // 取决于app什么时候获取到权限，即使在application中初始化，首次安装时，存在获取不到权限，建议app端在activity中再初始化一次，确保文件目录完整创建
        FaceManager.loadFaceFiles()
    }

    fun login(userid: String?, usersig: String?, callback: IUIKitCallBack) {
        TIMManager.getInstance().login(userid, usersig, object : TIMCallBack {
            override fun onError(code: Int, desc: String) {
                callback.onError(TAG, code, desc)
            }

            override fun onSuccess() {
                callback.onSuccess(null)
            }
        })
    }

    private fun initIM(context: Context, sdkAppID: Int) {
        var sdkConfig = sConfigs?.sdkConfig
        if (sdkConfig == null) {
            sdkConfig = TIMSdkConfig(sdkAppID)
            sConfigs?.setSdkConfig(sdkConfig)
        }
        val generalConfig = sConfigs?.generalConfig
        sdkConfig.logLevel = generalConfig?.logLevel ?: 0
        sdkConfig.enableLogPrint(generalConfig?.isLogPrint ?: false)
        TIMManager.getInstance().init(context, sdkConfig)
        val userConfig = TIMUserConfig()
        userConfig.isReadReceiptEnabled = true
        userConfig.messageReceiptListener = TIMMessageReceiptListener { receiptList -> C2CChatManagerKit.instance?.onReadReport(receiptList) }
        userConfig.userStatusListener = object : TIMUserStatusListener {
            override fun onForceOffline() {
                for (l in sIMEventListeners) {
                    l.onForceOffline()
                }
                unInit()
            }

            override fun onUserSigExpired() {
                for (l in sIMEventListeners) {
                    l.onUserSigExpired()
                }
                unInit()
            }
        }
        userConfig.connectionListener = object : TIMConnListener {
            override fun onConnected() {
                NetWorkUtils.sIMSDKConnected = true
                for (l in sIMEventListeners) {
                    l.onConnected()
                }
            }

            override fun onDisconnected(code: Int, desc: String) {
                NetWorkUtils.sIMSDKConnected = false
                for (l in sIMEventListeners) {
                    l.onDisconnected(code, desc)
                }
            }

            override fun onWifiNeedAuth(name: String) {
                for (l in sIMEventListeners) {
                    l.onWifiNeedAuth(name)
                }
            }
        }
        userConfig.refreshListener = object : TIMRefreshListener {
            override fun onRefresh() {}
            override fun onRefreshConversation(conversations: List<TIMConversation>) {
                ConversationManagerKit.instance.onRefreshConversation(conversations)
                for (l in sIMEventListeners) {
                    l.onRefreshConversation(conversations)
                }
            }
        }
        userConfig.groupEventListener = TIMGroupEventListener { elem ->
            for (l in sIMEventListeners) {
                l.onGroupTipsEvent(elem)
            }
        }
        userConfig.friendshipListener = object : TIMFriendshipListener {
            override fun onAddFriends(list: List<String>) {
                TUIKitLog.i(TAG, "onAddFriends: " + list.size)
            }

            override fun onDelFriends(list: List<String>) {
                TUIKitLog.i(TAG, "onDelFriends: " + list.size)
            }

            override fun onFriendProfileUpdate(list: List<TIMSNSChangeInfo>) {
                TUIKitLog.i(TAG, "onFriendProfileUpdate: " + list.size)
            }

            override fun onAddFriendReqs(list: List<TIMFriendPendencyInfo>) {
                TUIKitLog.i(TAG, "onAddFriendReqs: " + list.size)
            }
        }
        TIMManager.getInstance().addMessageListener { msgs ->
            for (l in sIMEventListeners) {
                l.onNewMessages(msgs)
            }
            false
        }
        userConfig.messageRevokedListener = MessageRevokedManager.instance
        TIMManager.getInstance().userConfig = userConfig
    }

    fun unInit() {
        ConversationManagerKit.instance.destroyConversation()
    }

    val configs: TUIKitConfigs?
        get() {
            if (sConfigs == null) {
                sConfigs = TUIKitConfigs.configs
            }
            return sConfigs
        }

    fun addIMEventListener(listener: IMEventListener?) {
        if (listener != null && !sIMEventListeners.contains(listener)) {
            sIMEventListeners.add(listener)
        }
    }

    fun removeIMEventListener(listener: IMEventListener?) {
        if (listener == null) {
            sIMEventListeners.clear()
        } else {
            sIMEventListeners.remove(listener)
        }
    }

}