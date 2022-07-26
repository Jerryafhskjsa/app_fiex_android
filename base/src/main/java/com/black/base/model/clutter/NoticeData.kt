package com.black.base.model.clutter

import com.black.base.model.BaseAdapterItem
import java.util.*

class NoticeData {
    var list: ArrayList<Notice?>? = null
    var totalCount = 0

    //公告
    class Notice : BaseAdapterItem() {
        var id: String? = null
        var userId: String? = null
        var title: String? = null
        var content: String? = null
        var sorting: String? = null
        var createTime: Long? = null
        var updateTime: String? = null
        var status = 0
        var tag: String? = null
        var readednum: String? = null
        override fun getType(): Int {
            return NOTICE
        }

        override fun toString(): String {
            return content!!
        }
    }
}