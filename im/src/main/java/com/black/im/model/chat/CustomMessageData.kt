package com.black.im.model.chat

import com.google.gson.JsonObject

class CustomMessageData {
    companion object {
        const val TYPE_RED_PACKET = "FBSEX_RED_PACKET"
        const val TYPE_RED_PACKET_GOT = "FBSEX_RED_PACKET_OPEN"
    }

    var subType: String? = null
    var data: JsonObject? = null
}