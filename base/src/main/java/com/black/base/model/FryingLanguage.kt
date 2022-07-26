package com.black.base.model

import java.util.*

class FryingLanguage(locale: Locale, languageCode: Int, languageText: String) {
    companion object {
        const val Chinese = 1
        const val Japanese = 2
        const val Korean = 3
        const val English = 4
        const val Russian = 5
    }

    var locale: Locale?
    var languageCode //s1-中，2-日，3-韩，4-英，5-俄
            : Int
    var languageText //s1-简体中文，2-Japanese，3-Korean，4-English，5-俄
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