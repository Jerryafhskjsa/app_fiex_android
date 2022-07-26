package com.black.im.interfaces

import com.black.im.widget.TitleBarLayout

interface ILayout {
    /**
     * 获取标题栏
     *
     * @return
     */
    val titleBar: TitleBarLayout?

    /**
     * 设置该 Layout 的父容器
     *
     * @param parent
     */
    fun setParentLayout(parent: Any?)
}