package com.black.base.model

import java.time.chrono.JapaneseDate
import java.util.*

class FryingLanguage(locale: Locale, languageCode: Int, languageText: String) {
    companion object {
        const val Chinese = 0
        const val Chinese_tw = 1
        const val English = 2
        const val Japanese = 3
        const val English_uk = 4
    }

    var locale: Locale?
    var languageCode //s1-中，2-繁，3-英，4-日，5-英_uk
            : Int
    var languageText //s1-简体中文，2-繁体中文，3-英语，4-日本语，5-英_uk
            : String

    init {
        this.locale = locale
        this.languageCode = languageCode
        this.languageText = languageText
    }

    val isValid: Boolean
        get() = languageCode == 1 || languageCode == 2 || languageCode == 3 || languageCode == 4 || languageCode == 5

    override fun toString(): String {
        return languageText
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || locale == null || !isValid) {
            return false
        }
        if (other is FryingLanguage) {
            return if (other.locale == null || !other.isValid) {
                false
            } else locale == other.locale && languageCode == other.languageCode
        }
        return false
    }

}