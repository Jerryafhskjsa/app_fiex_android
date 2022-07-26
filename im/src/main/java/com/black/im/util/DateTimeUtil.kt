package com.black.im.util

import java.util.*

object DateTimeUtil {
    private const val minute = 60 * 1000.toLong()  // 1分钟
    private const val hour = 60 * minute // 1小时
    private const val day = 24 * hour // 1天
    private const val month = 31 * day // 月
    private const val year = 12 * month // 年
    /**
     * 返回文字描述的日期
     *
     * @param date
     * @return
     */
    fun getTimeFormatText(date: Date?): String? {
        if (date == null) {
            return null
        }
        val calendar = Calendar.getInstance()
        val currentDayIndex = calendar[Calendar.DAY_OF_YEAR]
        val currentYear = calendar[Calendar.YEAR]
        calendar.time = date
        val msgYear = calendar[Calendar.YEAR]
        val msgDayIndex = calendar[Calendar.DAY_OF_YEAR]
        val msgMinute = calendar[Calendar.MINUTE]
        var msgTimeStr = calendar[Calendar.HOUR_OF_DAY].toString() + ":"
        msgTimeStr = if (msgMinute < 10) {
            msgTimeStr + "0" + msgMinute
        } else {
            msgTimeStr + msgMinute
        }
        val msgDayInWeek = calendar[Calendar.DAY_OF_WEEK]
        msgTimeStr = if (currentDayIndex == msgDayIndex) {
            return msgTimeStr
        } else {
            if (currentDayIndex - msgDayIndex == 1 && currentYear == msgYear) {
                "昨天 $msgTimeStr"
            } else if (currentDayIndex - msgDayIndex > 1 && currentYear == msgYear) { //本年消息
                //不同周显示具体月，日，注意函数：calendar.get(Calendar.MONTH) 一月对应0，十二月对应11
                Integer.valueOf(calendar[Calendar.MONTH] + 1).toString() + "月" + calendar[Calendar.DAY_OF_MONTH] + "日 " + msgTimeStr + " "
            } else { // 1、非正常时间，如currentYear < msgYear，或者currentDayIndex < msgDayIndex
                //2、非本年消息（currentYear > msgYear），如：历史消息是2018，今年是2019，显示年、月、日
                msgYear.toString() + "年" + Integer.valueOf(calendar[Calendar.MONTH] + 1) + "月" + calendar[Calendar.DAY_OF_MONTH] + "日" + msgTimeStr + " "
            }
        }
        return msgTimeStr
    }

    fun formatSeconds(seconds: Long): String {
        var timeStr = seconds.toString() + "秒"
        if (seconds > 60) {
            val second = seconds % 60
            var min = seconds / 60
            timeStr = min.toString() + "分" + second + "秒"
            if (min > 60) {
                min = seconds / 60 % 60
                var hour = seconds / 60 / 60
                timeStr = hour.toString() + "小时" + min + "分" + second + "秒"
                if (hour % 24 == 0L) {
                    val day = seconds / 60 / 60 / 24
                    timeStr = day.toString() + "天"
                } else if (hour > 24) {
                    hour = seconds / 60 / 60 % 24
                    val day = seconds / 60 / 60 / 24
                    timeStr = day.toString() + "天" + hour + "小时" + min + "分" + second + "秒"
                }
            }
        }
        return timeStr
    }
}