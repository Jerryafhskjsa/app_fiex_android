package com.black.base.model.future

class PlanUnionBean(
    var planList: ArrayList<PlansBean?>? = null, //计划委托列表
    var limitPriceList: ArrayList<OrderBeanItem>? = null, //限价委托列表
)