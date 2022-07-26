package com.black.lib.permission

import android.os.Build

object Permission {
    lateinit var CALENDAR: Array<String>
    lateinit var CAMERA: Array<String>
    lateinit var CONTACTS: Array<String>
    lateinit var LOCATION: Array<String>
    lateinit var MICROPHONE: Array<String>
    lateinit var PHONE: Array<String>
    lateinit var SENSORS: Array<String>
    lateinit var SMS: Array<String>
    lateinit var STORAGE: Array<String>

    init {
        if (Build.VERSION.SDK_INT < 23) {
            CALENDAR = arrayOf()
            CAMERA = arrayOf()
            CONTACTS = arrayOf()
            LOCATION = arrayOf()
            MICROPHONE = arrayOf()
            PHONE = arrayOf()
            SENSORS = arrayOf()
            SMS = arrayOf()
            STORAGE = arrayOf()
        } else {
            CALENDAR = arrayOf("android.permission.READ_CALENDAR", "android.permission.WRITE_CALENDAR")
            CAMERA = arrayOf("android.permission.CAMERA")
            CONTACTS = arrayOf("android.permission.READ_CONTACTS", "android.permission.WRITE_CONTACTS", "android.permission.GET_ACCOUNTS")
            LOCATION = arrayOf("android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION")
            MICROPHONE = arrayOf("android.permission.RECORD_AUDIO")
            PHONE = arrayOf("android.permission.READ_PHONE_STATE", "android.permission.CALL_PHONE", "android.permission.READ_CALL_LOG", "android.permission.WRITE_CALL_LOG", "android.permission.USE_SIP", "android.permission.PROCESS_OUTGOING_CALLS")
            SENSORS = arrayOf("android.permission.BODY_SENSORS")
            SMS = arrayOf("android.permission.SEND_SMS", "android.permission.RECEIVE_SMS", "android.permission.READ_SMS", "android.permission.RECEIVE_WAP_PUSH", "android.permission.RECEIVE_MMS")
            STORAGE = arrayOf("android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE")
        }
    }
}