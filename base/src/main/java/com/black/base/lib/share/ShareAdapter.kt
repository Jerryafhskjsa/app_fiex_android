package com.black.base.lib.share


interface ShareAdapter {
    val shareContentType: Int

    fun share(shareType: String?)
}
