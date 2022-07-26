package com.black.im.model

class MessageTyping {
    companion object {
        const val TYPE_TYPING = 14
        const val EDIT_START = "EIMAMSG_InputStatus_Ing"
        const val EDIT_END = "EIMAMSG_InputStatus_End"
    }

    var userAction = 0
    var actionParam = ""
}