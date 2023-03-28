package com.black.wallet.fragment

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
import com.black.base.model.future.OrderBean
import com.black.base.model.wallet.Order
import com.black.base.model.wallet.Wallet
import com.black.base.util.*
import com.black.base.view.DeepControllerWindow
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.util.Callback
import com.black.wallet.BR
import com.black.wallet.R
import com.black.wallet.adapter.ContractOdersAdapter
import com.black.wallet.databinding.FragmentDelegationBinding
import java.util.*

class OdersFragment : BaseFragment(), View.OnClickListener,OnItemClickListener, QRefreshLayout.OnRefreshListener, QRefreshLayout.OnLoadListener, QRefreshLayout.OnLoadMoreCheckListener {
    companion object {
        private  var TYPE_U_CONTRACT = ""
        private  var TYPE_COIN_CONTRACT = ""
        private  var TYPE_ALL = ""
        private  var TYPE_BTC = "BTC_USDT"
        private  var TYPE_ETH = "ETH_USDT"
    }
    private var wallet: Wallet? = null
    private var binding: FragmentDelegationBinding? = null
    private var layout: View? = null
    private var adapter: ContractOdersAdapter? = null
    private var currentPage = 1
    private var total = 0
    private var otherType: String? = null
    private var typeList: MutableList<String>? = null
    private var type: String? = null
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
        adapter = ContractOdersAdapter(mContext!!, BR.listItemFinancialRecordModel, null)
        adapter?.setOnItemClickListener(this)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false

        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(mContext!!))
        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.refreshLayout?.setOnLoadListener(this)
        binding?.refreshLayout?.setOnLoadMoreCheckListener(this)
        binding?.contractChoose?.setOnClickListener(this)
        binding?.btnAll?.setOnClickListener(this)
        binding?.start?.setOnClickListener(this)
        binding?.end?.setOnClickListener(this)
        TYPE_U_CONTRACT = getString(R.string.usdt_base_contract)
        TYPE_COIN_CONTRACT = getString(R.string.coin_base_contract)
        TYPE_ALL = getString(R.string.all)
        otherType = TYPE_U_CONTRACT
        type = TYPE_ALL
        typeList = ArrayList()
        typeList!!.add(TYPE_U_CONTRACT)
        typeList!!.add(TYPE_COIN_CONTRACT)
        getHistoryList(otherType ,type ,oder.startTime ,oder.endTime)
        return layout
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.contract_choose -> {
                DeepControllerWindow(mContext as Activity, null, otherType, typeList, object : DeepControllerWindow.OnReturnListener<String> {
                    override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                        window.dismiss()
                        otherType = item
                        when(otherType){
                            TYPE_U_CONTRACT -> {
                                getHistoryList(otherType ,type ,oder.startTime ,oder.endTime)
                                binding?.usdM?.setText(R.string.usdt_base_contract)
                            }
                            TYPE_COIN_CONTRACT -> {
                                getHistoryList(otherType ,type ,oder.startTime ,oder.endTime)
                                binding?.usdM?.setText(R.string.coin_base_contract)
                                binding?.all?.setText(R.string.all)
                            }
                        }
                    }

                }).show()
            }
            R.id.btn_all -> {
                if (binding?.usdM?.text == getString(R.string.coin_base_contract)){
                    list = ArrayList()
                    list!!.add(TYPE_ALL)

                }
                else{
                    list = ArrayList()
                    list!!.add(TYPE_ALL)
                    list!!.add(TYPE_BTC)
                    list!!.add(TYPE_ETH)
                }
                DeepControllerWindow(mContext as Activity, null, type, list, object : DeepControllerWindow.OnReturnListener<String> {
                    override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                        window.dismiss()
                        type = item
                        when(item){
                            TYPE_ALL -> {
                                getHistoryList(otherType ,type ,oder.startTime ,oder.endTime)
                                binding?.all?.setText(R.string.all)
                            }
                            TYPE_BTC -> {
                                getHistoryList(otherType ,type ,oder.startTime ,oder.endTime)
                                binding?.all?.setText("BTCUSDT")
                            }
                            TYPE_ETH -> {
                                getHistoryList(otherType ,type ,oder.startTime ,oder.endTime)
                                binding?.all?.setText("ETHUSDT")
                            }
                        }


                    }

                }).show()
            }
            R.id.start -> {chooseDialog(false)}
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
                val date = Date(year - 1900,month - 1,day - 1)
                oder.startTime = date.time
                val total1 = year * 10000 + month * 100 + day
                if (total >= total1) {
                    getHistoryList(otherType ,type ,oder.startTime ,oder.endTime)
                    binding?.start?.setText(
                        String.format(
                            Locale.getDefault(),
                            "%04d-%02d-%02d",
                            year,
                            month,
                            day
                        )
                    )

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
                val date = Date(year - 1900,month - 1,day)
                val time =date.time
                val total2 = year * 10000 + month * 100 + day
                if (total >= total2 && time >= oder.startTime!!){
                    oder.endTime = time
                    getHistoryList(otherType ,type ,oder.startTime ,oder.endTime)
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
        getHistoryList(otherType ,type ,oder.startTime ,oder.endTime)
    }

    override fun onLoad() {
        if (total > adapter?.count!!) {
            currentPage++
            getHistoryList(otherType ,type ,oder.startTime ,oder.endTime)
        } else {
            binding?.refreshLayout?.setLoading(false)
        }
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > adapter?.count!!
    }

    //获取历史订单
    private fun getHistoryList(otherType: String? , type: String? , startTime: Long? , endTime: Long?) {
        if (otherType == TYPE_U_CONTRACT || otherType == null) {
            FutureApiServiceHelper.getHistoryList( if (type == TYPE_ALL) null else type ,null, "NEXT",20, startTime ,endTime , mContext ,false,
                object : Callback<HttpRequestResultBean<OrderBean>>() {
                    override fun error(type: Int, error: Any?) {
                        binding?.refreshLayout?.setRefreshing(false)
                        binding?.refreshLayout?.setLoading(false)
                    }

                    override fun callback(returnData: HttpRequestResultBean<OrderBean>?) {
                        binding?.refreshLayout?.setRefreshing(false)
                        binding?.refreshLayout?.setLoading(false)
                        if (returnData != null && returnData.returnCode == HttpRequestResult.SUCCESS) {
                            val oderList = returnData.result?.items
                            adapter?.data = oderList
                            adapter?.notifyDataSetChanged()
                        }
                        else {
                            FryingUtil.showToast(mContext, if (returnData == null) "sb" else returnData.msgInfo)
                        }
                    }
                })
        }

        else{
            FutureApiServiceHelper.getCoinHistoryList(if (type == TYPE_ALL) null else type, null,"NEXT", 20, startTime , endTime ,mContext,false,
                object : Callback<HttpRequestResultBean<OrderBean>>() {
                    override fun error(type: Int, error: Any?) {
                        binding?.refreshLayout?.setRefreshing(false)
                        binding?.refreshLayout?.setLoading(false)
                    }

                    override fun callback(returnData: HttpRequestResultBean<OrderBean>) {
                        binding?.refreshLayout?.setRefreshing(false)
                        binding?.refreshLayout?.setLoading(false)
                        if (returnData != null) {
                        }
                    }
                })
        }
    }


}