package com.black.base.util

import android.content.Context
import android.os.Bundle
import com.black.base.util.BlackLinkMovementMethod.LinkClickListener
import com.black.router.BlackRouter

class BlackLinkClickListener(private val title: String) : LinkClickListener {
    override fun onLinkClick(context: Context?, mURL: String?): Boolean {
        val bundle = Bundle()
        bundle.putString(ConstData.TITLE, title)
        bundle.putString(ConstData.URL, mURL)
        BlackRouter.getInstance().build(mURL).with(bundle).go(context)
        return true
    }

}