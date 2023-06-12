package com.black.frying.fragment

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import com.black.base.api.FutureApiServiceHelper
import com.black.base.fragment.BaseFragment
import com.black.base.model.ContractRecordTabBean
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.PagingData
import com.black.base.model.SuccessObserver
import com.black.base.model.future.*
import com.black.base.model.socket.PairStatus
import com.black.base.util.*
import com.black.base.widget.AutoHeightViewPager
import com.black.base.widget.SpanTextView
import com.black.frying.adapter.ContractPlanTabAdapter
import com.black.frying.viewmodel.ContractPositionViewModel
import com.black.util.Callback
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentHomePageContractDetailBinding
import com.tencent.imsdk.friendship.TIMPendencyType
import io.reactivex.Observer
import skin.support.content.res.SkinCompatResources
import java.math.BigDecimal
import kotlin.collections.ArrayList

/**
 * @author 合约计划委托列表页
 */
class ContractPlanTabFragment : BaseFragment(),
    AdapterView.OnItemClickListener,
    View.OnClickListener,
    ContractPositionViewModel.OnContractPositionModelListener,
    HomePageContractFragment.OnTabModelListener,
    ContractPlanTabAdapter.OnTabModelListener{
    private var type: ContractRecordTabBean? = null

    private var binding: FragmentHomePageContractDetailBinding? = null
    private var mViewPager: AutoHeightViewPager? = null

    private var adapter: ContractPlanTabAdapter? = null
    private var dataList: ArrayList<PlansBean?>? = ArrayList()
    private var viewModel: ContractPositionViewModel? = null

    //异步获取数据
    private var handlerThread: HandlerThread? = null
    private var socketHandler: Handler? = null
    private var onTabModelListener: OnTabModelListener? = null
    private var contractSize: String? = null
    private var pairObserver: Observer<ArrayList<PairStatus?>?>? = null
    private var planUnionBean = PlanUnionBean()
    private var positionObservers : Observer<UserPositionBean?>? = createPositionObserver()
    private var orderObservers : Observer<UserOrderBean?>? = createOrderObserver()
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
        contractSize = viewModel?.getContractSize()
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
        adapter = ContractPlanTabAdapter(mContext!!, dataList)
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
        if (positionObservers == null) {
            positionObservers = createPositionObserver()
        }
        SocketDataContainer.subscribePositionObservable(positionObservers)

        if (orderObservers == null) {
            orderObservers = createOrderObserver()
        }
        SocketDataContainer.subscribeFutureOrderObservable(orderObservers)
        getPlanData(Constants.UNFINISHED)
    }

    override fun onStop() {
        super.onStop()
        if (pairObserver != null) {
            SocketDataContainer.removePairObservable(pairObserver)
            pairObserver == null
        }
        if (positionObservers != null) {
            SocketDataContainer.removePositionObservable(positionObservers)
            positionObservers = null
        }
        if (orderObservers != null) {
            SocketDataContainer.removeFutureOrderObservable(orderObservers)
            orderObservers = null
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
                if (entrustType == 0) {
                    FryingUtil.showToast(activity, getString(R.string.null_bills))
                    return
                }
                val futureSecond = FutureSecond.getFutureSecondSetting(mContext!!)?.futureCode
                if (futureSecond == 0 || futureSecond == null) {
                    FutureApiServiceHelper.cancelALlPlan(
                        activity,
                        symbol = null,
                        true,
                        object : Callback<HttpRequestResultBean<String>?>() {
                            override fun callback(returnData: HttpRequestResultBean<String>?) {
                                if (returnData != null) {
                                    FryingUtil.showToast(activity, "Success")
                                    getPlanData(Constants.UNFINISHED)
                                }
                            }

                            override fun error(type: Int, error: Any?) {
                                FryingUtil.showToast(activity, error.toString())
                            }
                        })
                } else {
                    val contentView =
                        LayoutInflater.from(mContext).inflate(R.layout.future_second_dialog, null)
                    val dialog = Dialog(mContext!!, R.style.AlertDialog)
                    val window = dialog.window
                    if (window != null) {
                        val params = window.attributes
                        //设置背景昏暗度
                        params.dimAmount = 0.2f
                        params.gravity = Gravity.CENTER
                        params.width = WindowManager.LayoutParams.MATCH_PARENT
                        params.height = WindowManager.LayoutParams.WRAP_CONTENT
                        window.attributes = params
                    }
                    //设置dialog的宽高为屏幕的宽高
                    val display = resources.displayMetrics
                    val layoutParams =
                        ViewGroup.LayoutParams(
                            display.widthPixels,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    dialog.setContentView(contentView, layoutParams)
                    dialog.show()
                    dialog.findViewById<SpanTextView>(R.id.title).text = "撤销全部计划委托"
                    dialog.findViewById<SpanTextView>(R.id.neirong).text = "是否确认将永续合约所有计划委托全部撤销？"
                    dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->
                        dialog.dismiss()
                    }
                    dialog.findViewById<View>(R.id.btn_confirm).setOnClickListener { v ->
                        FutureApiServiceHelper.cancelALlPlan(
                            activity,
                            symbol = null,
                            true,
                            object : Callback<HttpRequestResultBean<String>?>() {
                                override fun callback(returnData: HttpRequestResultBean<String>?) {
                                    if (returnData != null) {
                                        FryingUtil.showToast(activity, "Success")
                                        getPlanData(Constants.UNFINISHED)
                                    }
                                }

                                override fun error(type: Int, error: Any?) {
                                    FryingUtil.showToast(activity, error.toString())
                                }
                            })
                        dialog.dismiss()
                    }
                }
            }
        }
    }

    override fun refresh2() {
        Log.d("jkhkjhkjhjk","sss")
        getPlanData(Constants.UNFINISHED)
    }
    override fun refresh() {
        getPlanData(Constants.UNFINISHED)
    }

    override fun onGetPositionData(positionList: ArrayList<PositionBean?>?) {
    }

    override fun onFundingRate(fundRate: FundingRateBean?) {

    }

    override fun onLeverageDetail(leverageBracket: LeverageBracketBean?) {

    }

    override fun onMarketPrice(marketPrice: MarkPriceBean?) {

    }

    override fun onPosition(positionList: UserPositionBean?) {
        Log.d("22213213",positionList.toString())
    }

    /**
     * 获取当前委托列表数据
     */
    private fun getPlanData(state: String?) {
        var symbol:String? = viewModel?.getCurrentPairSymbol()
        if(SharedPreferenceUtils.getData(Constants.PLAN_ALL_CHECKED,true) as Boolean){
            symbol = null
        }
        FutureApiServiceHelper.getPlanList(context,symbol, state, false,
            object : Callback<HttpRequestResultBean<PagingData<PlansBean?>?>?>() {
                override fun error(type: Int, error: Any?) {
                }

                override fun callback(returnData: HttpRequestResultBean<PagingData<PlansBean?>?>?) {
                    if (returnData != null) {
                        planUnionBean.planList = returnData.result?.items
                        val count = planUnionBean.planList?.size!!
                        entrustType = planUnionBean.planList?.size
                        /*for (i in 1..count){
                            planUnionBean.planList!![i]?.amount = BigDecimal(planUnionBean.planList!![i]?.origQty)
                                .multiply(BigDecimal(planUnionBean.planList!![i]?.price))
                                .multiply(BigDecimal(contractSize.toString())).toString()
                        }

                         */
                        adapter?.data = planUnionBean.planList
                        adapter?.notifyDataSetChanged()
                        Log.d("ajksdhkjhdakj", count.toString())
                        onTabModelListener?.onCount(count)
                    }
                    else{
                        onTabModelListener?.onCount(0)
                    }
                }
            })
    }

    private fun createPositionObserver(): Observer<UserPositionBean?> {
        return object : SuccessObserver<UserPositionBean?>() {
            override fun onSuccess(value:UserPositionBean?) {
                Log.d("ahsdjkhak", value.toString())
                getPlanData(Constants.UNFINISHED)
            }
        }
    }

    private fun createOrderObserver(): Observer<UserOrderBean?> {
        return object : SuccessObserver<UserOrderBean?>() {
            override fun onSuccess(value:UserOrderBean?) {
                Log.d("ahsdjkhak", value.toString())
                getPlanData(Constants.UNFINISHED)
            }
        }
    }

    interface OnTabModelListener {
        fun onCount(count: Int?)
    }

    companion object {
        fun newInstance(type: ContractRecordTabBean?): ContractPlanTabFragment {
            val args = Bundle()
            val fragment = ContractPlanTabFragment()
            fragment.arguments = args
            fragment.type = type
            return fragment
        }
    }
}