package com.black.im.indexlib.suspension

/**
 * 介绍：分类悬停的接口
 */
interface ISuspensionInterface {
    //是否需要显示悬停title
    fun isShowSuspension(): Boolean

    //悬停的title
    fun getSuspensionTag(): String?
}