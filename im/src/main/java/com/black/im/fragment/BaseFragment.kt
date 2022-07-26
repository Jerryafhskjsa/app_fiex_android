package com.black.im.fragment

import android.app.Fragment

open class BaseFragment : Fragment() {
    fun forward(fragment: Fragment?, hide: Boolean) {
        forward(id, fragment, null, hide)
    }

    fun forward(viewId: Int, fragment: Fragment?, name: String?, hide: Boolean) {
        val trans = fragmentManager.beginTransaction()
        if (hide) {
            trans.hide(this)
            trans.add(viewId, fragment)
        } else {
            trans.replace(viewId, fragment)
        }
        trans.addToBackStack(name)
        trans.commitAllowingStateLoss()
    }

    fun backward() {
        fragmentManager.popBackStack()
    }
}