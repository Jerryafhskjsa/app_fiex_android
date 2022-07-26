package com.black.im.model.face

import android.graphics.Bitmap
import com.black.im.util.ScreenUtil.getPxByDp
import java.io.Serializable

class Emoji : Serializable {
    companion object {
        private val deaultSize = getPxByDp(32)
    }

    var desc: String? = null
    var filter: String? = null
    var icon: Bitmap? = null
    var width = deaultSize
    var height = deaultSize
}