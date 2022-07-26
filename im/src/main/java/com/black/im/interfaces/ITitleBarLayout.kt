package com.black.im.interfaces

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * 会话列表窗口 [ConversationLayout]、聊天窗口 [ChatLayout] 等都自带标题栏，<br></br>
 * 标题栏设计为左中右三部分标题，左边可为图片+文字，中间为文字，右边也可为图片+文字，这些区域返回的都是标准的<br></br>
 * Android View，可以根据业务需要对这些 View 进行交互响应处理。
 */
interface ITitleBarLayout {
    /**
     * 设置左边标题的点击事件
     *
     * @param listener
     */
    fun setOnLeftClickListener(listener: View.OnClickListener?)

    /**
     * 设置右边标题的点击事件
     *
     * @param listener
     */
    fun setOnRightClickListener(listener: View.OnClickListener?)

    /**
     * 设置标题
     *
     * @param title    标题内容
     * @param position 标题位置
     */
    fun setTitle(title: String?, position: POSITION?)

    /**
     * 返回左边标题区域
     *
     * @return
     */
    val leftGroup: LinearLayout?

    /**
     * 返回右边标题区域
     *
     * @return
     */
    val rightGroup: LinearLayout?

    /**
     * 返回左边标题的图片
     *
     * @return
     */
    fun getLeftIcon(): ImageView?

    /**
     * 设置左边标题的图片
     *
     * @param resId
     */
    fun setLeftIcon(resId: Int)

    /**
     * 返回右边标题的图片
     *
     * @return
     */
    fun getRightIcon(): ImageView?

    /**
     * 设置右边标题的图片
     *
     * @param resId
     */
    fun setRightIcon(resId: Int)

    /**
     * 返回左边标题的文字
     *
     * @return
     */
    val leftTitle: TextView?

    /**
     * 返回中间标题的文字
     *
     * @return
     */
    val middleTitle: TextView?

    /**
     * 返回右边标题的文字
     *
     * @return
     */
    val rightTitle: TextView?

    /**
     * 标题区域的枚举值
     */
    enum class POSITION {
        /**
         * 左边标题
         */
        LEFT,
        /**
         * 中间标题
         */
        MIDDLE,
        /**
         * 右边标题
         */
        RIGHT
    }
}