package com.black.base.util

import com.black.base.util.GeeTestHelper.GeeTestApi1Callback
import com.black.base.util.GeeTestHelper.GeeTestApi2Callback

interface GeeTestCallback {
    fun onApi1(api1Callback: GeeTestApi1Callback?)
    fun onApi2(result: String?, api2Callback: GeeTestApi2Callback?)
}
