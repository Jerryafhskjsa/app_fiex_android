package com.black.im.interfaces

import android.view.View
import android.widget.EditText
import com.black.im.fragment.BaseInputFragment
import com.black.im.model.InputMoreActionUnit
import com.black.im.widget.InputLayout
import com.black.im.widget.MessageLayout

/**
 * 输入区域 [InputLayout] 实现了一般消息的输入，包括文本、表情、图片、音频、视频、文件等，<br></br>
 * 并且配合 [MessageLayout.setOnCustomMessageDrawListener] 可以完成自定义消息的发送与展示。
 * <br></br>另外也可以根据需要对上面的功能进行删除或替换。
 */
interface IInputLayout {
    /**
     * disable 语音输入后，会隐藏按钮
     *
     * @param disable
     */
    fun disableAudioInput(disable: Boolean)

    /**
     * disable 表情输入后，会隐藏按钮
     *
     * @param disable
     */
    fun disableEmojiInput(disable: Boolean)

    /**
     * disable 更多功能后，会隐藏按钮
     *
     * @param disable
     */
    fun disableMoreInput(disable: Boolean)

    /**
     * 替换点击“+”弹出的面板
     *
     * @param fragment
     */
    fun replaceMoreInput(fragment: BaseInputFragment?)

    /**
     * 替换点击“+”响应的事件
     *
     * @param listener
     */
    fun replaceMoreInput(listener: View.OnClickListener?)

    /**
     * disable 发送图片后，会隐藏更多面板上的按钮
     *
     * @param disable
     */
    fun disableSendPhotoAction(disable: Boolean)

    /**
     * disable 拍照后，会隐藏更多面板上的按钮
     *
     * @param disable
     */
    fun disableCaptureAction(disable: Boolean)

    /**
     * disable 录像后，会隐藏更多面板上的按钮
     *
     * @param disable
     */
    fun disableVideoRecordAction(disable: Boolean)

    /**
     * disable 发送文件后，会隐藏更多面板上的按钮
     *
     * @param disable
     */
    fun disableSendFileAction(disable: Boolean)

    /**
     * disable 发送红包后，会隐藏更多面板上的按钮
     *
     * @param disable
     */
    fun disableSendRedPacketAction(disable: Boolean)

    /**
     * 增加更多面板上的事件单元
     *
     * @param action 事件单元 [InputMoreActionUnit]，可以自定义显示的图片、标题以及点击事件
     */
    fun addAction(action: InputMoreActionUnit)

    /**
     * 获取输入框View
     *
     * @return 输入框EditText
     */
    val inputText: EditText?
}