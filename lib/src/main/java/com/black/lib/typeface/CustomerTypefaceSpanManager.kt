package com.black.lib.typeface

import android.content.Context
import android.text.style.TypefaceSpan
import java.lang.ref.SoftReference
import java.util.*

object CustomerTypefaceSpanManager {
    private val typefaceSpanCache: MutableMap<String, SoftReference<TypefaceSpan>> = HashMap()
    fun init(context: Context?) {
//        CHINESE_BOLD = get(context, "PINGFANG BOLD.TTF");
//        OTHER_BOLD = get(context, "DINPro-Medium.ttf");
//        CHINESE_NORMAL = get(context, "PINGFANG MEDIUM.TTF");
//        OTHER_NORMAL = get(context, "DINPro.otf");
//        get(context, "pingfang_sc_regular.ttf");
//        get(context, "pingfang_sc_semibold.otf");
    }

    operator fun get(context: Context?, path: String): TypefaceSpan {
        val ref = typefaceSpanCache[path]
        var typefaceSpan = ref?.get()
        if (typefaceSpan == null) {
            typefaceSpan = CustomerTypefaceSpan(context!!, path)
            typefaceSpanCache[path] = SoftReference<TypefaceSpan>(typefaceSpan)
        }
        return typefaceSpan
    }

    fun put(path: String, typefaceSpan: TypefaceSpan) {
        typefaceSpanCache[path] = SoftReference(typefaceSpan)
    }

    fun clear() {
        typefaceSpanCache.clear()
    }
}
