package com.black.frying.fragment

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.DatePicker
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.api.FutureApiServiceHelper
import com.black.base.fragment.BaseFragment
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.PagingData
import com.black.base.model.wallet.CostBill
import com.black.base.model.wallet.Order
import com.black.base.model.wallet.Wallet
import com.black.base.util.*
import com.black.base.view.DeepControllerWindow
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.util.Callback
import com.black.wallet.BR
import com.black.wallet.R
import com.black.frying.adapter.CostAdapter
import com.black.wallet.databinding.FragmentDelegationBinding
import java.util.*

class CostFragment : BaseFragment(), View.OnClickListener,OnItemClickListener, QRefreshLayout.OnRefreshListener, QRefreshLayout.OnLoadListener, QRefreshLayout.OnLoadMoreCheckListener {
    companion object {
        private const val TYPE_U_CONTRACT = "USDT"
        private  var TYPE_ALL = ""
        private  var TYPE_FEE = ""
        private  var TYPE_TRANSFER = ""
        private  var TYPE_CLOSING = ""
        private  var TYPE_TAKEROVER = ""
        private  var TYPE_FORCED = ""
        private  var TYPE_FLOW = ""
        private  var TYPE_RAISE = ""
        private  var TYPE_MERGE = ""
        private  var TYPE_GRANT = ""
        private  var TYPE_DEDUCTION = ""
        private  var TYPE_RECOVER = ""
        private  var TYPE_CASHBACK = ""
    }
    private var wallet: Wallet? = null
    private var binding: FragmentDelegationBinding? = null
    private var layout: View? = null
    private var adapter: CostAdapter? = null
    private var currentPage = 1
    private var total = 0
    private var otherType = TYPE_U_CONTRACT
    private var typeList: MutableList<String>? = null
    private var type = TYPE_ALL
    private var startTime: Long? = null
    private var endTime: Long? = null
    private var list: MutableList<String>? = null
    private var oder = Order()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (layout != null) {
            return layout
        }
        wallet = arguments?.getParcelable(ConstData.WALLET)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_delegation, container, false)
        layout = binding?.root

        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        adapter = CostAdapter(mContext!!, BR.listItemFinancialRecordModel, null)
        adapter?.setOnItemClickListener(this)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false
        binding?.usdM?.setText("USDT")
        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(mContext!!))
        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.refreshLayout?.setOnLoadListener(this)
        binding?.refreshLayout?.setOnLoadMoreCheckListener(this)

        binding?.contractChoose?.setOnClickListener(this)
        binding?.start?.setOnClickListener(this)
        binding?.end?.setOnClickListener(this)
        binding?.btnAll?.setOnClickListener(this)
        typeList = ArrayList()
        typeList!!.add(TYPE_U_CONTRACT)
        TYPE_ALL = getString(R.string.all)
        TYPE_FEE = getString(R.string.fee)
        TYPE_TRANSFER = getString(R.string.exchange)
        TYPE_CLOSING = getString(R.string.close_positions)
        TYPE_TAKEROVER = getString(R.string.take_over)
        TYPE_FORCED = getString(R.string.qiang_ping)
        TYPE_FLOW = getString(R.string.capital_cost)
        TYPE_RAISE = getString(R.string.position_reduce)
        TYPE_MERGE = getString(R.string.merge)
        TYPE_GRANT = getString(R.string.distribution_of_experience_fund)
        TYPE_DEDUCTION = getString(R.string.experience_gold_deduction)
        TYPE_RECOVER = getString(R.string.experience_gold_recovery)
        TYPE_CASHBACK = getString(R.string.cash_return)
        list = ArrayList()
        list!!.add(TYPE_ALL)
        list!!.add(TYPE_FEE)
        list!!.add(TYPE_FLOW)
        list!!.add(TYPE_TAKEROVER)
        list!!.add(TYPE_TRANSFER)
        list!!.add(TYPE_CLOSING)
        list!!.add(TYPE_CASHBACK)
        list!!.add(TYPE_FORCED)
        list!!.add(TYPE_MERGE)
        list!!.add(TYPE_RAISE)
        list!!.add(TYPE_RECOVER)
        list!!.add(TYPE_GRANT)
        list!!.add(TYPE_DEDUCTION)
        type = TYPE_ALL
        getBalancesBills(otherType,type,startTime,endTime)
        return layout
    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.contract_choose -> {
                DeepControllerWindow(
                    mContext as Activity,
                    null,
                    otherType,
                    typeList,
                    object : DeepControllerWindow.OnReturnListener<String> {
                        override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                            window.dismiss()
                            otherType = item
                            getBalancesBills(otherType,type,startTime,endTime)
                            when (item) {
                                TYPE_U_CONTRACT -> {
                                    binding?.usdM?.setText("USDT")
                                }
                            }
                        }

                    }).show()
            }
            R.id.btn_all -> {
                DeepControllerWindow(
                    mContext as Activity,
                    null,
                    type,
                    list,
                    object : DeepControllerWindow.OnReturnListener<String> {
                        override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                            window.dismiss()
                            type = item
                            getBalancesBills(otherType,type,startTime,endTime)
                            binding?.all?.setText(item)
                        }

                    }).show()
            }
            R.id.start -> {
                chooseDialog(false)
            }
            R.id.end -> {
                val birthCode = binding?.start?.text.toString().trim { it <= ' ' }
                if (birthCode == "开始时间") {
                    FryingUtil.showToast(mContext, getString(R.string.please_choose_start_time))
                    return
                } else {
                    chooseDialog(true)
                }
            }
        }
    }
    fun getType(type: String?):String {
        return if (type == TYPE_ALL) {
            ""
        } else when (type) {
                    TYPE_FEE -> "FEE"
                    TYPE_TRANSFER -> "EXCHANGE"
                    TYPE_CLOSING -> "CLOSE_POSITION"
                    TYPE_TAKEROVER -> "TAKE_OVER"
                    TYPE_FORCED -> "QIANG_PING_MANAGER"
                    TYPE_FLOW -> "FUND"
                    TYPE_RAISE -> "ADL"
                    TYPE_MERGE -> "MERGE"
                    TYPE_GRANT -> "BONUS_GRANT"
                    TYPE_DEDUCTION -> "BONUS_DEDUCTION"
                    TYPE_RECOVER -> "BONUS_TAKE_BACK"
                    TYPE_CASHBACK -> "CASH_BACK"
            else -> ""
        }
    }
    private fun chooseDialog(isShowLoading: Boolean) {
        val calendar: Calendar = Calendar.getInstance()
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.date_choose_window, null)
        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH)
        var day = calendar.get(Calendar.DAY_OF_MONTH)
        val dialog = Dialog(mContext!!, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.BOTTOM
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            //设置dialog动画
            window.setWindowAnimations(R.style.anim_bottom_in_out)
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams = ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        val datePickerDialog: DatePicker = dialog.findViewById<DatePicker>(R.id.data_picker)
        val total = year * 10000 + (month + 1) * 100 + day
        dialog.findViewById<View>(R.id.btn_confirm).setOnClickListener { v ->
            if (!isShowLoading) {
                year = datePickerDialog.year
                month = datePickerDialog.month + 1
                day = datePickerDialog.dayOfMonth
                val total1 = year * 10000 + month * 100 + day
                val date = Date(year - 1900,month - 1,day)
                startTime = date.time
                getBalancesBills(otherType,type, startTime, endTime)
                if (total >= total1) {
                    binding?.start?.setText(
                        String.format(
                            Locale.getDefault(),
                            "%04d-%02d-%02d",
                            year,
                            month,
                            day
                        )
                    ).toString()
                }
                else{
                    FryingUtil.showToast(mContext, getString(R.string.please_choose_correct_time))
                    dialog.dismiss()
                }
            }
            else{
                year = datePickerDialog.year
                month = datePickerDialog.month + 1
                day = datePickerDialog.dayOfMonth
                val total2 = year * 10000 + month * 100 + day
                val date = Date(year - 1900,month - 1,day + 1 )
                val time = date.time
                if (total >= total2 && time >= startTime!!){
                    endTime = time
                    getBalancesBills(otherType, type, startTime, endTime)
                    binding?.end?.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day)).toString()
                }
                else{
                    FryingUtil.showToast(mContext, getString(R.string.please_choose_correct_time))
                    dialog.dismiss()
                }
            }

            dialog.dismiss()
        }

        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener {  v ->
            dialog.dismiss()
        }

    }


    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        val financialRecord = adapter?.getItem(position)
        val extras = Bundle()
    }

    override fun onRefresh() {
        currentPage = 1
        getBalancesBills(otherType,type,startTime,endTime)
    }

    override fun onLoad() {
        if (total > adapter?.count!!) {
            currentPage++
            getBalancesBills(otherType,type,startTime, endTime)
        } else {
            binding?.refreshLayout?.setLoading(false)
        }
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > adapter?.count!!
    }
 //获取资金流水
    private fun getBalancesBills(otherType: String?, type: String?, startTime: Long? , endTime: Long?) {
            FutureApiServiceHelper.getBalanceBills(otherType, "NEXT",20, TYPE_U_CONTRACT ,getType(type),  startTime , endTime  ,mContext,
                object : Callback<HttpRequestResultBean<PagingData<CostBill?>?>?>() {
                    override fun error(type: Int, error: Any?) {
                        binding?.refreshLayout?.setRefreshing(false)
                        binding?.refreshLayout?.setLoading(false)
                    }

                    override fun callback(returnData: HttpRequestResultBean<PagingData<CostBill?>?>?) {
                        binding?.refreshLayout?.setRefreshing(false)
                        binding?.refreshLayout?.setLoading(false)
                        if (returnData != null && returnData.returnCode == HttpRequestResult.SUCCESS) {
                            val oderList = returnData.result?.items
                            adapter?.data = oderList
                            adapter?.notifyDataSetChanged()
                        } else {
                            FryingUtil.showToast(
                                mContext,
                                if (returnData == null) "null" else returnData.msgInfo
                            )

                        }
                    }
                })

 }

}