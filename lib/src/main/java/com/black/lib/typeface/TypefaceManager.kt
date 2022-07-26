package com.black.lib.typeface

import android.content.Context
import android.graphics.Typeface
import java.lang.ref.SoftReference
import java.util.*

object TypefaceManager {
    const val CHINESE_BOLD = 1
    const val OTHER_BOLD = 4
    const val CHINESE_NORMAL = 2
    const val OTHER_NORMAL = 8
    private val typefaceCache: MutableMap<String, SoftReference<Typeface>?> = HashMap()

    fun init(context: Context?) {
//        get(context, "PINGFANG BOLD.TTF");
//        get(context, "DINPro-Medium.ttf");
//        get(context, "PINGFANG MEDIUM.TTF");
//        get(context, "PINGFANG MEDIUM.TTF");
    }

    operator fun get(context: Context, path: String): Typeface? {
        val ref: SoftReference<Typeface>? = typefaceCache[path]
        var typeface = ref?.get()
        if (typeface == null) {
            typeface = Typeface.createFromAsset(context.assets, path)
            typefaceCache[path] = SoftReference(typeface)
        }
        return typeface
    }

    operator fun get(context: Context?, type: Int): Typeface? {
//        if (CHINESE_BOLD == type) {
//            return get(context, "PINGFANG BOLD.TTF");
//        } else if (OTHER_BOLD == type) {
//            return get(context, "DINPro-Medium.ttf");
//        } else if (CHINESE_NORMAL == type) {
//            return get(context, "PINGFANG MEDIUM.TTF");
//        } else if (OTHER_NORMAL == type) {
//            return get(context, "DINPro.otf");
//        }
        return null
    }

    fun put(path: String, typefaceSpan: Typeface) {
        typefaceCache[path] = SoftReference(typefaceSpan)
    }

    fun clear() {
        typefaceCache.clear()
    }
}