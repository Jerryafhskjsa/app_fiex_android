package com.black.im.action

import android.graphics.Bitmap

class PopMenuAction {
    var actionName: String? = null
    var icon: Bitmap? = null
    var iconResId = 0
    var actionClickListener: PopActionClickListener? = null
}