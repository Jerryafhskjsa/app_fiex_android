package com.black.frying.model

import com.black.base.model.socket.PairStatus

class PairSearch : PairStatus() {
    companion object {
        const val PAIR = 0
        const val TITLE = 1
        const val DELETE = 2
    }

    @JvmField
    var type = PAIR

    override fun getType(): Int {
        return type
    }


}
