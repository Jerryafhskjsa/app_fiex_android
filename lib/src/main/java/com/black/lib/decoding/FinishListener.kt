package com.black.lib.decoding

import android.app.Activity
import android.content.DialogInterface

/**
 * Simple listener used to exit the app in a few cases.
 *
 */
class FinishListener(private val activityToFinish: Activity) : DialogInterface.OnClickListener, DialogInterface.OnCancelListener, Runnable {
    override fun onCancel(dialogInterface: DialogInterface) {
        run()
    }

    override fun onClick(dialogInterface: DialogInterface, i: Int) {
        run()
    }

    override fun run() {
        activityToFinish.finish()
    }
}
