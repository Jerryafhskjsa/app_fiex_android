package com.black.base.model.wallet

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.black.base.R
import com.black.base.model.BaseAdapterItem

 class CostBill(): BaseAdapterItem(), Parcelable {
        var createdTime: Long? = null
        var afterAmount: Double = 0.0
        var amount: Double = 0.0
        var side: String? = null
        var coin: String? = null
        var id: String? = null
        var type: String? = null
        var symbol: String? = null

     constructor(parcel: Parcel) : this() {
         createdTime = parcel.readValue(Long::class.java.classLoader) as? Long
         afterAmount = parcel.readValue(Double::class.java.classLoader) as Double
         amount = parcel.readValue(Double::class.java.classLoader) as Double
         side = parcel.readString()
         coin = parcel.readString()
         id = parcel.readString()
         type = parcel.readString()
         symbol = parcel.readString()
     }


     fun getType(context: Context): String {
         return if (type == null) {
             "--"
         } else when (type) {
             "EXCHANGE" -> context.getString(R.string.exchange)
             "FEE" -> context.getString(R.string.fee)
             "FUND" -> context.getString(R.string.capital_cost)
             "ADL" -> context.getString(R.string.position_reduce)
             "MERGE" -> context.getString(R.string.merge)
             "QIANG_PING_MANAGER" -> context.getString(R.string.qiang_ping)
             "TAKE_OVER" -> context.getString(R.string.take_over)
             "CLOSE_POSITION" -> context.getString(R.string.close_positions)
             else -> "--"
         }
         }

     fun getSymbol(context: Context): String {
         return if (symbol == null){
             "--"
         } else when(symbol) {
             "btc_usdt" -> "BTC_USDT" + context.getString(R.string.sustainable)
             else -> "--"
         }
     }
     override fun writeToParcel(parcel: Parcel, flags: Int) {
         parcel.writeString(coin)
         parcel.writeValue(createdTime)
         parcel.writeString(id)
         parcel.writeString(type)
         parcel.writeValue(amount)
         parcel.writeValue(afterAmount)
         parcel.writeValue(side)
         parcel.writeString(symbol)
     }

     override fun describeContents(): Int {
         return 0
     }

     companion object CREATOR : Parcelable.Creator<WalletBill> {
         override fun createFromParcel(parcel: Parcel): WalletBill {
             return WalletBill(parcel)
         }

         override fun newArray(size: Int): Array<WalletBill?> {
             return arrayOfNulls(size)
         }
     }
    }
