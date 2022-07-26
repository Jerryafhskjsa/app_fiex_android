package com.black.base.model.clutter

import java.util.*

class NoticeHome {
    var articles: ArrayList<NoticeHomeItem?>? = null

    inner class NoticeHomeItem {
        var html_url: String? = null
        var title: String? = null
    }
}
