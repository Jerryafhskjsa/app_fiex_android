package com.black.base.model.filter

import org.json.JSONException
import org.json.JSONObject

class GeeTestBean {
    var challenge: String? = null
    var validate: String? = null
    var seccode: String? = null
    var success: String? = null
    var new_captcha: String? = null
    var gt: String? = null
    fun toJson(): JSONObject {
        val jsonObject = JSONObject()
        try {
            jsonObject.accumulate("challenge", challenge)
            jsonObject.accumulate("validate", validate)
            jsonObject.accumulate("seccode", seccode)
            jsonObject.accumulate("success", success)
            jsonObject.accumulate("new_captcha", new_captcha)
            jsonObject.accumulate("gt", gt)
        } catch (e: JSONException) {
        }
        return jsonObject
    }
    //        "challenge": "ffccf2039cc150d27194c9f4aeab7110",
//                "validate": null,
//                "seccode": null,
//                "success": 1,
//                "new_captcha": null,
//                "gt": "c69d5d7e28f3538f099eaa4a2b76dd4d"
}