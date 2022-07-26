package com.black.im.util

import android.content.Context
import com.black.im.config.TUIKitConfigs
import com.black.im.interfaces.IMEventListener

object TUIKit {
    /**
     * TUIKit的初始化函数
     *
     * @param context  应用的上下文，一般为对应应用的ApplicationContext
     * @param sdkAppID 您在腾讯云注册应用时分配的sdkAppID
     * @param configs  TUIKit的相关配置项，一般使用默认即可，需特殊配置参考API文档
     */
    fun init(context: Context, sdkAppID: Int, configs: TUIKitConfigs?) {
        TUIKitImpl.init(context, sdkAppID, configs)
    }

    /**
     * 释放一些资源等，一般可以在退出登录时调用
     */
    fun unInit() {
        TUIKitImpl.unInit()
    }

    /**
     * 获取TUIKit保存的上下文Context，该Context会长期持有，所以应该为Application级别的上下文
     *
     * @return
     */
    val appContext: Context
        get() = TUIKitImpl.appContext!!

    /**
     * 获取TUIKit的全部配置
     *
     * @return
     */
    val configs: TUIKitConfigs
        get() = TUIKitImpl.configs!!

    /**
     * 设置TUIKit的IM消息的全局监听
     *
     * @param listener
     */
    fun addIMEventListener(listener: IMEventListener?) {
        TUIKitImpl.addIMEventListener(listener)
    }

    /**
     * 删除TUIKit的IM消息的全局监听
     *
     * @param listener 如果为空，则删除全部的监听
     */
    fun removeIMEventListener(listener: IMEventListener?) {
        TUIKitImpl.removeIMEventListener(listener)
    }

    /**
     * 用户IM登录
     *
     * @param userid   用户名
     * @param usersig  从业务服务器获取的usersig
     * @param callback 登录是否成功的回调
     */
    fun login(userid: String?, usersig: String?, callback: IUIKitCallBack) {
        TUIKitImpl.login(userid, usersig, callback)
    }

    val defaultAvatarSize: Int
        get() = TUIKitImpl.defaultAvatarSize
}
