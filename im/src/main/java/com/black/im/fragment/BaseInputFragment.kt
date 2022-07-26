package com.black.im.fragment

import com.black.im.interfaces.IChatLayout

open class BaseInputFragment : BaseFragment() {
    var chatLayout: IChatLayout? = null
        private set

    fun setChatLayout(layout: IChatLayout?): BaseInputFragment {
        chatLayout = layout
        return this
    }
}
