package com.black.base.model.future

data class TickerBean(
    val a: String, //24小时成交量
    val c: String, //最新价
    val h: String, //24小时最高价
    val l: String, //24小时最低价
    val o: String, //24小时前第一笔成交价
    val r: String, //24小时涨跌幅
    val s: String, //交易对
    val t: Long,   //时间
    val v: String  //24小时成交额
)