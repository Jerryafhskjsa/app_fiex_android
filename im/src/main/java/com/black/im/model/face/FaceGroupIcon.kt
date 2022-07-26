package com.black.im.model.face

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout
import com.black.im.R

class FaceGroupIcon : RelativeLayout {
    private var faceTabIcon: ImageView? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.face_group_icon, this)
        faceTabIcon = findViewById(R.id.face_group_tab_icon)
    }

    fun setFaceTabIcon(bitmap: Bitmap?) {
        faceTabIcon!!.setImageBitmap(bitmap)
    }
}