package com.black.im.model.face

import android.graphics.Bitmap
import java.util.*

class FaceGroup {
    var groupId = 0
    var desc: String? = null
    var groupIcon: Bitmap? = null
    var pageRowCount = 0
    var pageColumnCount = 0
    var faces: ArrayList<Emoji?>? = null
}