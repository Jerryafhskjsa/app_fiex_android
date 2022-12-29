package com.black.base.model

class FryingLinesConfig() {
    var index:Int? = null
    var id:Int? = null
    var lineUrl:String? = null
    var en:String? = null
    var zh:String? = null
    var status:Int? = null
    var statusDes:String? = null
    var isDel:Boolean? = null
    var startTime:Long? = null
    var endTime:Long? = null
    set(value) {
        field = value
        speed = if(value!! - startTime!! > 10000){
            ">10000ms"
        }else{
            (value!! - startTime!!).toString()+"ms"
        }

    }
    var speed:String? = null


}