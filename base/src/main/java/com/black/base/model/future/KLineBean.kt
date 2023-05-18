package com.black.base.model.future

data class KLineBean(
    val s: String, //交易对
    val o: String, // open 开盘价
    val c: String,    //cloes 收盘价
    val h: String, //high 最高价
    val l: String, //low 最低价
    val a: String, //amount 成交量
    val v: String, //volume 成交额
    val ch: String,   //change 涨跌幅
    val t: Long //123124124   //时间戳

)