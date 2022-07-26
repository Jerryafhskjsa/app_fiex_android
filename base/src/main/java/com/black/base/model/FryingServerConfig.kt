package com.black.base.model

class FryingServerConfig(var index: Int, var title: String) {
    override fun toString(): String {
        return title
    }

    override fun equals(other: Any?): Boolean {
        return if (other == null || other !is FryingServerConfig) {
            false
        } else index == other.index
    }

}