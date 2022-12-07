package com.black.base.model.future

data class ADLBean(
    val longQuantile: Int, //多头adl(1-5)
    val shortQuantile: Int, //空头adl(1-5)
    val symbol: String
)