package com.black.im.interfaces

import android.graphics.drawable.Drawable

interface IMessageProperties {
    /// @payeeName 设置头像
    /// @{

    /// @payeeName 设置头像
/// @{
    /**
     * 获取默认头像
     *
     * @return
     */
    fun getAvatar(): Int

    /**
     * 设置默认头像，默认与左边与右边的头像相同
     *
     * @param resId
     */
    fun setAvatar(resId: Int)

    /**
     * 获取头像圆角
     *
     * @return
     */
    fun getAvatarRadius(): Int

    /**
     * 设置头像圆角
     *
     * @param radius
     */
    fun setAvatarRadius(radius: Int)

    /**
     * 获得头像大小
     *
     * @return
     */
    fun getAvatarSize(): IntArray?

    /**
     * 设置头像大小
     *
     * @param size
     */
    fun setAvatarSize(size: IntArray?)

    /// @}
    /// @payeeName 设置昵称样式
    /// @{

    /// @}
/// @payeeName 设置昵称样式
/// @{
    /**
     * 获得昵称文字大小
     *
     * @return
     */
    fun getNameFontSize(): Int

    /**
     * 设置昵称文字大小
     *
     * @param size
     */
    fun setNameFontSize(size: Int)

    /**
     * 获取昵称文字颜色
     *
     * @return
     */
    fun getNameFontColor(): Int

    /**
     * 设置昵称文字颜色
     *
     * @param color
     */
    fun setNameFontColor(color: Int)

    /**
     * 获取左边昵称显示状态
     *
     * @return
     */
    fun getLeftNameVisibility(): Int

    /**
     * 设置左边昵称是否显示
     *
     * @param visibility
     */
    fun setLeftNameVisibility(visibility: Int)

    /**
     * 获取右边昵称显示状态
     *
     * @return
     */
    fun getRightNameVisibility(): Int

    /**
     * 设置右边昵称是否显示
     *
     * @param visibility
     */
    fun setRightNameVisibility(visibility: Int)

    /// @}
    /// @payeeName 设置气泡
    /// @{

    /// @}
/// @payeeName 设置气泡
/// @{
    /**
     * 获取右边聊天气泡的背景
     *
     * @return
     */
    fun getRightBubble(): Drawable?

    /**
     * 设置右边聊天气泡的背景
     *
     * @param drawable
     */
    fun setRightBubble(drawable: Drawable?)

    /**
     * 获取左边聊天气泡的背景
     *
     * @return
     */
    fun getLeftBubble(): Drawable?

    /**
     * 设置左边聊天气泡的背景
     *
     * @param drawable
     */
    fun setLeftBubble(drawable: Drawable?)

    /// @}
    /// @payeeName 设置聊天内容
    /// @{

    /// @}
/// @payeeName 设置聊天内容
/// @{
    /**
     * 获取聊天内容字体大小
     *
     * @return
     */
    fun getChatContextFontSize(): Int

    /**
     * 设置聊天内容字体大小
     *
     * @param size
     */
    fun setChatContextFontSize(size: Int)

    /**
     * 获取右边聊天内容字体颜色
     *
     * @return
     */
    fun getRightChatContentFontColor(): Int

    /**
     * 设置右边聊天内容字体颜色
     *
     * @param color
     */
    fun setRightChatContentFontColor(color: Int)

    /**
     * 获取左边聊天内容字体颜色
     *
     * @return
     */
    fun getLeftChatContentFontColor(): Int

    /**
     * 设置左边聊天内容字体颜色
     *
     * @param color
     */
    fun setLeftChatContentFontColor(color: Int)

    /// @}
    /// @payeeName 设置聊天时间
    /// @{

    /// @}
/// @payeeName 设置聊天时间
/// @{
    /**
     * 获取聊天时间的背景
     *
     * @return
     */
    fun getChatTimeBubble(): Drawable?

    /**
     * 设置聊天时间的背景
     *
     * @param drawable
     */
    fun setChatTimeBubble(drawable: Drawable?)

    /**
     * 获取聊天时间的文字大小
     *
     * @return
     */
    fun getChatTimeFontSize(): Int

    /**
     * 设置聊天时间的字体大小
     *
     * @param size
     */
    fun setChatTimeFontSize(size: Int)

    /**
     * 获取聊天时间的字体颜色
     *
     * @return
     */
    fun getChatTimeFontColor(): Int

    /**
     * 设置聊天时间的字体颜色
     *
     * @param color
     */
    fun setChatTimeFontColor(color: Int)

    /// @}
    /// @payeeName 设置聊天的提示信息
    /// @{

    /// @}
/// @payeeName 设置聊天的提示信息
/// @{
    /**
     * 获取聊天提示信息的背景
     *
     * @return
     */
    fun getTipsMessageBubble(): Drawable?

    /**
     * 设置聊天提示信息的背景
     *
     * @param drawable
     */
    fun setTipsMessageBubble(drawable: Drawable?)

    /**
     * 获取聊天提示信息的文字大小
     *
     * @return
     */
    fun getTipsMessageFontSize(): Int

    /**
     * 设置聊天提示信息的文字大小
     *
     * @param size
     */
    fun setTipsMessageFontSize(size: Int)

    /**
     * 获取聊天提示信息的文字颜色
     *
     * @return
     */
    fun getTipsMessageFontColor(): Int

    /**
     * 设置聊天提示信息的文字颜色
     *
     * @param color
     */
    fun setTipsMessageFontColor(color: Int)

}
