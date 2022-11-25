package com.black.base.model.future

data class DeepFullBean(
    val a: List<List<String>>,  //ask 卖单队列， [价格，数量]
    val b: List<List<String>>,  //bid 买单队列
    val id: String,
    val s: String  //交易对
)