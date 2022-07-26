package com.black.im.interfaces

import android.view.View

/**
 * 自定义消息的容器
 */
interface ICustomMessageViewGroup {
    /**
     * 把自定义消息的整个view添加到容器里
     *
     * @param view 自定义消息的整个view
     */
    fun addMessageItemView(view: View?)

    /**
     * 把自定义消息的内容区域view添加到容器里
     *
     * @param view 自定义消息的内容区域view
     */
    fun addMessageContentView(view: View?)
}