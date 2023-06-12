package com.black.base.model

class FutureSecondChange ( futureCode: Int, futureTrue: Boolean) {
    companion object {
        const val one = 0
        const val two = 1
    }


    var futureCode
            : Int
    var futureText
            : Boolean

    init {
        this.futureCode = futureCode
        this.futureText = futureTrue
    }

    val isValid: Boolean
        get() = futureCode == 0 || futureCode == 1

     fun toBoolean(): Boolean {
        return futureText
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || !isValid) {
            return false
        }
        if (other is FutureSecondChange) {
            return if ( !other.isValid) {
                false
            } else  futureCode == other.futureCode && futureText == other.futureText
        }
        return false
    }

}