package com.black.base.model

class FryingStyleChange ( styleCode: Int, styleText: String) {
    companion object {
        const val greenUp = 0
        const val redUp = 1
    }


    var styleCode
            : Int
    var styleText
            : String

    init {
        this.styleCode = styleCode
        this.styleText = styleText
    }

    val isValid: Boolean
        get() = styleCode == 0 || styleCode == 1

    override fun toString(): String {
        return styleText
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || !isValid) {
            return false
        }
        if (other is FryingStyleChange) {
            return if ( !other.isValid) {
                false
            } else  styleCode == other.styleCode && styleText == other.styleText
        }
        return false
    }

}