package com.black.base.model.user

import android.content.ContentValues
import android.database.Cursor

class User {
    var id: Int? = null
    var telCountryCode: String? = null
    var userName: String? = null
    var uid: String? = null
    var password: String? = null
    var token: String? = null
    var ucToken: String? = null
    var apiToken: String? = null
    var ticket: String? = null
    var loginDate: Long? = null
    var isCurrentUser = false

    constructor()
    constructor(cursor: Cursor?) {
        if (cursor == null) {
            return
        }
        id = cursor.getInt(cursor.getColumnIndex("_id"))
        telCountryCode = cursor.getString(cursor.getColumnIndex("county_code"))
        userName = cursor.getString(cursor.getColumnIndex("user_name"))
        uid = cursor.getString(cursor.getColumnIndex("uid"))
        password = cursor.getString(cursor.getColumnIndex("password"))
        token = cursor.getString(cursor.getColumnIndex("token"))
        ucToken = cursor.getString(cursor.getColumnIndex("uc_token"))
        apiToken = cursor.getString(cursor.getColumnIndex("api_token"))
        ticket = cursor.getString(cursor.getColumnIndex("ticket"))
        loginDate = cursor.getLong(cursor.getColumnIndex("login_date"))
        isCurrentUser = cursor.getInt(cursor.getColumnIndex("is_current_user")) == 1
    }

    val contentValues: ContentValues
        get() {
            val values = ContentValues()
            values.put("county_code", telCountryCode)
            values.put("user_name", userName)
            values.put("uid", uid)
            values.put("password", password)
            values.put("token", token)
            values.put("uc_token", ucToken)
            values.put("api_token", apiToken)
            values.put("ticket", ticket)
            values.put("login_date", loginDate)
            values.put("is_current_user", if (isCurrentUser) 1 else 0)
            return values
        }
}