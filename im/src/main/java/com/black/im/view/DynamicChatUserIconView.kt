package com.black.im.view

import com.black.im.model.chat.MessageInfo
import com.black.im.util.ScreenUtil


abstract class DynamicChatUserIconView : DynamicLayoutView<MessageInfo?>() {
    /**
     * 设置聊天头像圆角
     *
     * @param iconRadius
     */
    var iconRadius = -1
        set(iconRadius) {
            field = ScreenUtil.getPxByDp(iconRadius)
        }

}