package com.black.base.util

import com.black.base.util.FryingUtil.printError
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author zhangxiaohui
 * create at 2018/9/28
 */
object TimeUtil {
    val lastDayData: Date
        get() {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, -1)
            return cal.time
        }

    val lastDayCalendar: Calendar
        get() {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, -1)
            return cal
        }

    val lastDayString: String
        get() = dataToString(lastDayData)

    private fun dataToString(date: Date): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        return simpleDateFormat.format(date)
    }

    fun curDateToCalendar(date: Date?): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar
    }

    fun getTimeHm(date: Date?): String { //可根据需要自行截取数据显示
        val format = SimpleDateFormat("HH:mm")
        return format.format(date)
    }

    fun getTime(date: Date?): String { //可根据需要自行截取数据显示
        val format = SimpleDateFormat("yyyy-MM-dd")
        return format.format(date)
    }

    fun getTime(date: Date?, format: String?): String { //可根据需要自行截取数据显示
        val f = SimpleDateFormat(format)
        return f.format(date)
    }

    fun getMonth(date: Date?): String { //可根据需要自行截取数据显示
        val format = SimpleDateFormat("yyyy-MM")
        return format.format(date)
    }

    fun getCurData(date: Date?, yead: Int): Int { //可根据需要自行截取数据显示
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar[yead]
    }

    fun getVideoTime(date: Date?): String { //可根据需要自行截取数据显示
        val format = SimpleDateFormat("yyyyMMdd")
        return format.format(date)
    }

    fun getTimeByformat(timeStr: String?): Calendar {
        var date: Date? = null
        try {
            val format = SimpleDateFormat("yyyy-MM-dd")
            date = format.parse(timeStr)
        } catch (e: ParseException) {
            printError(e)
        }
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar
    }

    fun timeFormatToOther(timeStr: String?, oldFormat: String?, newFormat: String?): String {
        var newTime = ""
        try {
            val format = SimpleDateFormat(oldFormat)
            val date = format.parse(timeStr)
            val formatOther = SimpleDateFormat(newFormat)
            newTime = formatOther.format(date)
        } catch (e: ParseException) {
            printError(e)
        }
        return newTime
    }

    /**
     * 获取当前时间
     *
     * @param format
     * @return
     */
    fun getCurTime(format: String?): String? {
        return getTimeStr(System.currentTimeMillis(), format)
    }

    /**
     * 时间戳转格式时间
     *
     * @param timeStamp
     * @param format
     * @return
     */
    fun getTimeStr(timeStamp: Long, format: String?): String? {
        var date: String? = null
        date = try {
            val sf = SimpleDateFormat(format)
            sf.format(Date(timeStamp))
        } catch (e: Exception) {
            printError(e)
            return ""
        }
        return date
    }

    fun getTimeStr(time: Date?, format: String?): String? {
        var date: String? = null
        date = try {
            val sf = SimpleDateFormat(format)
            sf.format(time)
        } catch (e: Exception) {
            printError(e)
            return ""
        }
        return date
    }

    fun firstOneBigger(str1: String?, str2: String?): Boolean {
        var isBigger = false
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        var dt1: Date? = null
        var dt2: Date? = null
        try {
            dt1 = sdf.parse(str1)
            dt2 = sdf.parse(str2)
            if (dt1.time > dt2.time) {
                isBigger = true
            } else if (dt1.time < dt2.time) {
                isBigger = false
            }
        } catch (e: ParseException) {
            printError(e)
        }
        return isBigger
    }

    fun timeEqual(str1: String?, str2: String?): Boolean {
        var isEqual = false
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        var dt1: Date? = null
        var dt2: Date? = null
        try {
            dt1 = sdf.parse(str1)
            dt2 = sdf.parse(str2)
            isEqual = dt1.time == dt2.time
        } catch (e: ParseException) {
            printError(e)
        }
        return isEqual
    }

    fun formatSeconds(seconds: Long): String {
        var timeStr:String? = null
        var hourStr:String? = null
        var minStr:String? = null
        var secondStr:String? = null
        val second = seconds % 60
        if (seconds > 60) {
            var min = seconds / 60
            minStr = "$min:"
            secondStr = "$second"
            if (min > 60) {
                min = seconds / 60 % 60
                var hour = seconds / 60 / 60
                secondStr = if(second < 10){
                    "0$second"
                }else{
                    "$second"
                }
                minStr = if(min < 10){
                    "0$min:"
                }else{
                    "$min:"
                }
                hourStr = if(hour < 10){
                    "0$hour:"
                }else{
                    "$hour:"
                }
            }else{
                hourStr = "00:"
                secondStr = if(second < 10){
                    "0$second"
                }else{
                    "$second"
                }
            }
        }else{
            secondStr = if(second < 10){
                "0$second"
            }else{
                "$second"
            }
            hourStr = "00:"
            minStr = "00:"
        }
        timeStr = hourStr+minStr+secondStr
        return timeStr
    }
}