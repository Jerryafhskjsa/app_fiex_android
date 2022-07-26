package com.black.base.model.clutter

import com.black.base.model.BaseAdapterItem

class Forum : BaseAdapterItem() {
    var id: Long? = null
    var channelName: String? = null
    var channelAccount: String? = null
    var iconUrl: String? = null
    var orCodeUrl: String? = null
    var createTime: Long? = null
    var status: Int? = null
}
