package com.black.base.model.future

data class ADLBean(
    val longQuantile: Int, //多头adl
    val shortQuantile: Int, //空头adl
    val symbol: String
)