package com.black.frying.fragment

import android.graphics.drawable.ColorDrawable
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import com.black.base.api.FutureApiServiceHelper
import com.black.base.fragment.BaseFragment
import com.black.base.model.ContractRecordTabBean
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.PagingData
import com.black.base.model.future.*
import com.black.base.model.socket.PairStatus
import com.black.base.util.*
import com.black.base.widget.AutoHeightViewPager
import com.black.frying.adapter.ContactCurrentAdapter
import com.black.frying.adapter.ContractPlanTabAdapter
import com.black.frying.viewmodel.ContractPositionViewModel
import com.black.util.Callback
import com.black.wallet.fragment.DelegationFragment
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentHomePageContractDetailBinding
import com.tencent.imsdk.friendship.TIMPendencyType
import io.reactivex.Observer
import skin.support.content.res.SkinCompatResources
import kotlin.collections.ArrayList

/**
 * @author 合约计划委托列表页
 */
class ContractCurrentFragment : BaseFragment(),
    AdapterView.OnItemClickListener,
    View.OnClickListener,
    ContractPositionViewModel.OnContractPositionModelListener {
    private var type: ContractRecordTabBean? = null

    private var binding: FragmentHomePageContractDetailBinding? = null
    private var mViewPager: AutoHeightViewPager? = null

    private var adapter: ContactCurrentAdapter? = null
    private var dataList: ArrayList<OrderBeanItem>? = null
    private var viewModel: ContractPositionViewModel? = null

    //异步获取数据
    private var handlerThread: HandlerThread? = null
    private var socketHandler: Handler? = null
    private var onTabModelListener: OnTabModelListener? = null

    private var pairObserver: Observer<ArrayList<PairStatus?>?>? = null
    private var planUnionBean = PlanUnionBean()
    private var entrustType:Int?  = 0//0限价委托，1计划委托

    /**
     * adapter height when viewpager in scrollview
     */
    fun setAutoHeightViewPager(viewPager: AutoHeightViewPager?) {
        mViewPager = viewPager
    }

    fun setOnTabModeListener(tabListener: OnTabModelListener) {
        onTabModelListener = tabListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (binding != null) {
            return binding?.root
        }
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home_page_contract_detail,
            container,
            false
        )
        viewModel = ContractPositionViewModel(mContext!!, this)
        binding?.btnLimitPrice?.setOnClickListener(this)
        binding?.btnPlan?.setOnClickListener(this)
        binding?.allDone?.setOnClickListener(this)
        binding?.allDone?.text = getString(R.string.contract_fast_cancel)
        binding?.contractWithLimit?.isChecked = SharedPreferenceUtils.getData(Constants.PLAN_ALL_CHECKED,true) as Boolean
        binding?.contractWithLimit?.setOnCheckedChangeListener { buttonView, isChecked ->
            SharedPreferenceUtils.putData(Constants.PLAN_ALL_CHECKED,isChecked)
        }
        val drawable = ColorDrawable()
        drawable.color = SkinCompatResources.getColor(activity, R.color.L1)
        drawable.alpha = (0xff * 0.3).toInt()
        val emptyView = inflater.inflate(R.layout.list_view_empty, null)
        val group = binding?.listView?.parent as ViewGroup
        group.addView(emptyView)
        binding?.listView?.emptyView = emptyView
        adapter = ContactCurrentAdapter(mContext!!,dataList)
        binding?.listView?.adapter = adapter
        binding?.listView?.onItemClickListener = this
        if (arguments != null) {
            val position = arguments!!.getInt(AutoHeightViewPager.POSITION)
            binding?.root?.let { mViewPager?.setViewPosition(it, position) }
        }
        return binding?.root
    }


    override fun onResume() {
        super.onResume()
        handlerThread = HandlerThread(ConstData.SOCKET_HANDLER, Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread?.start()
        socketHandler = Handler(handlerThread?.looper)
        getLimitPricePlanData()
    }

    override fun onStop() {
        super.onStop()
        if (pairObserver != null) {
            SocketDataContainer.removePairObservable(pairObserver)
            pairObserver == null
        }
        if (socketHandler != null) {
            socketHandler?.removeMessages(0)
            socketHandler = null
        }
        if (handlerThread != null) {
            handlerThread?.quit()
        }
    }

    override fun doResetSkinResources() {
        val drawable = ColorDrawable()
        drawable.color = SkinCompatResources.getColor(activity, R.color.L1_ALPHA30)
        binding?.listView?.divider = drawable
        binding?.listView?.dividerHeight = 1
//        adapter?.resetSkinResources()
//        adapter?.notifyDataSetChanged()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        activity?.let {
        }
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.all_done -> {
                if (dataList?.size == 0) {
                    FryingUtil.showToast(activity, getString(R.string.null_bills))
                    return
                }
                FutureApiServiceHelper.closeAll(
                    activity,
                    symbol = null,
                    true,
                    object : Callback<HttpRequestResultBean<String>?>() {
                        override fun callback(returnData: HttpRequestResultBean<String>?) {
                            if (returnData != null) {
                                FryingUtil.showToast(activity, "Success")
                                getLimitPricePlanData()
                                onTabModelListener?.onCount(0)
                            }
                        }

                        override fun error(type: Int, error: Any?) {
                            FryingUtil.showToast(activity, error.toString())
                        }
                    })
            }
        }
    }


    override fun onGetPositionData(positionList: ArrayList<PositionBean?>?) {
    }

    override fun onFundingRate(fundRate: FundingRateBean?) {

    }

    override fun onLeverageDetail(leverageBracket: LeverageBracketBean?) {

    }

    override fun onMarketPrice(marketPrice: MarkPriceBean?) {

    }

    /**
     * 获取当前限价委托
     */
    private fun getLimitPricePlanData() {
        var symbol:String? = viewModel?.getCurrentPairSymbol()
        if(SharedPreferenceUtils.getData(Constants.PLAN_ALL_CHECKED,true) as Boolean){
            symbol = null
        }
            FutureApiServiceHelper.getOrderList(1, 10, symbol, Constants.UNFINISHED, context, false,
                object : Callback<HttpRequestResultBean<OrderBean>>() {
                    override fun error(type: Int, error: Any?) {

                    }

                    override fun callback(returnData: HttpRequestResultBean<OrderBean>?) {
                        if (returnData != null) {
                            dataList = returnData.result?.items
                            adapter?.data = dataList
                            adapter?.notifyDataSetChanged()
                            val count = planUnionBean.limitPriceList?.size!!
                            onTabModelListener?.onCount(count)
                        }
                    }
                })
        }

    interface OnTabModelListener {
        fun onCount(count: Int?)
    }

    companion object {
        fun newInstance(type: ContractRecordTabBean?): ContractCurrentFragment {
            val args = Bundle()
            val fragment = ContractCurrentFragment()
            fragment.arguments = args
            fragment.type = type
            return fragment
        }
    }
}