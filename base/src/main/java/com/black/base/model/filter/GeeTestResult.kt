package com.black.base.model.filter

import com.google.gson.JsonObject

class GeeTestResult {
    var geetest_challenge: String? = null
    var geetest_seccode: String? = null
    var geetest_validate: String? = null
    fun toJsonString(): String {
        val jsonObject = JsonObject()
        jsonObject.addProperty("challenge", geetest_challenge)
        jsonObject.addProperty("validate", geetest_validate)
        jsonObject.addProperty("seccode", geetest_seccode)
        return jsonObject.toString()
    }
//    {"challenge":"9232b4e18703684285c4b4573733c2e64c",
//            "validate":"20f74a1147b0c7ce0dcb976df6a79405",
//            "seccode":"20f74a1147b0c7ce0dcb976df6a79405|jordan"}
//    {"geetest_challenge":"73f92250d0907e96616d831bad66bd96",
//            "geetest_seccode":"bd5ef7c4f72eef75b75ed14a14bbb59a|jordan",
//            "geetest_validate":"bd5ef7c4f72eef75b75ed14a14bbb59a"}
//        "challenge": "ffccf2039cc150d27194c9f4aeab7110",
//                "validate": null,
//                "seccode": null,
//                "success": 1,
//                "new_captcha": null,
//                "gt": "c69d5d7e28f3538f099eaa4a2b76dd4d"
}