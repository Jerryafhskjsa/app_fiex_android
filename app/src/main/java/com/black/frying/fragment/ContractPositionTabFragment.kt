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
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.black.base.api.FutureApiServiceHelper
import com.black.base.fragment.BaseFragment
import com.black.base.model.ContractRecordTabBean
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.SuccessObserver
import com.black.base.model.future.*
import com.black.base.util.*
import com.black.base.widget.AutoHeightViewPager
import com.black.frying.adapter.ContractPositionTabAdapter
import com.black.frying.service.FutureService
import com.black.frying.viewmodel.ContractPositionViewModel
import com.black.frying.viewmodel.ContractViewModel
import com.black.util.Callback
import com.black.util.CommonUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentHomePageContractDetailBinding
import io.reactivex.Observer
import skin.support.content.res.SkinCompatResources
import kotlin.collections.ArrayList

/**
 * @author 合约持仓列表页
 */
class ContractPositionTabFragment : BaseFragment(),
    AdapterView.OnItemClickListener,
    View.OnClickListener,
    ContractPositionViewModel.OnContractPositionModelListener {
    private var type: ContractRecordTabBean? = null

    private var binding: FragmentHomePageContractDetailBinding? = null
    private var mViewPager: AutoHeightViewPager? = null
    private var viewModel: ContractPositionViewModel? = null
    private var price: Double? = null //标记价格
    private var adapter: ContractPositionTabAdapter? = null
    private var dataList: ArrayList<PositionBean?> = ArrayList()
    //异步获取数据
    private var handlerThread: HandlerThread? = null
    private var socketHandler: Handler? = null
    private var all:Boolean? = null
    private var num: Int = 0
    private var futureMarkPriceObserver: Observer<MarkPriceBean?>? = null
    private var positionObservers : Observer<UserPositionBean?>? = createPositionObserver()
    private var onTabModelListener: OnTabModelListener? = null

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
        binding?.allDone?.setOnClickListener(this)
        binding?.entrustType?.visibility = View.GONE
        binding?.contractWithLimit?.setOnCheckedChangeListener { buttonView, isChecked ->
            SharedPreferenceUtils.putData(Constants.PLAN_ALL_CHECKED,isChecked)
            viewModel?.getPositionData(isChecked)
        }
        val drawable = ColorDrawable()
        drawable.color = SkinCompatResources.getColor(activity, R.color.L1)
        drawable.alpha = (0xff * 0.3).toInt()
        val emptyView = inflater.inflate(R.layout.list_view_empty, null)
        val group = binding?.listView?.parent as ViewGroup
        group.addView(emptyView)
        binding?.listView?.emptyView = emptyView

        adapter = ContractPositionTabAdapter(mContext!!, dataList)
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
        viewModel?.getMarketPrice(CookieUtil.getCurrentFutureUPair(context!!))
        viewModel?.getFundRate(CookieUtil.getCurrentFutureUPair(context!!))
        all = SharedPreferenceUtils.getData(Constants.POSITION_ALLL_CHECKED,true) as Boolean
        if (all != null) {
            binding?.contractWithLimit?.isChecked = all!!
        }
        if (LoginUtil.isFutureLogin(context)) {
            viewModel?.getPositionAdlList(context)
            viewModel?.getLeverageBracketDetail()
            viewModel?.getPositionData(all)
        }
        if (futureMarkPriceObserver == null) {
            futureMarkPriceObserver = createMarkPriceObserver()
        }
        SocketDataContainer.subscribeMarkPriceObservable(futureMarkPriceObserver)

        if (positionObservers == null) {
            positionObservers = createPositionObserver()
        }
        SocketDataContainer.subscribePositionObservable(positionObservers)
    }

    override fun onStop() {
        super.onStop()
        if (futureMarkPriceObserver != null) {
            SocketDataContainer.removeMarkPriceObservable(futureMarkPriceObserver)
            futureMarkPriceObserver = null
        }
        if (positionObservers != null) {
            SocketDataContainer.removePositionObservable(positionObservers)
            positionObservers = null
        }
        if (socketHandler != null) {
            socketHandler?.removeMessages(0)
            socketHandler = null
        }
        if (handlerThread != null) {
            handlerThread?.quit()
        }
    }

    private fun createPositionObserver(): Observer<UserPositionBean?> {
        return object : SuccessObserver<UserPositionBean?>() {
            override fun onSuccess(value:UserPositionBean?) {
                Log.d("ahsdjkhak", value.toString())
                viewModel?.getPositionData(all)
            }
        }
    }
    private fun createMarkPriceObserver(): Observer<MarkPriceBean?> {
        return object : SuccessObserver<MarkPriceBean?>() {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onSuccess(value: MarkPriceBean?) {
                Log.d("333333","value.p = "+value?.p)
                Log.d("333333","value.s = "+value?.s)
                viewModel?.doUpdate(activity,value?.p,value?.s,true)
            }
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
                val futureSecond = FutureSecond.getFutureSecondSetting(mContext!!)?.futureCode
                if (num == 0) {
                    return
                }
                if (futureSecond == 0 || futureSecond == null) {
                    FutureApiServiceHelper.closeAll(
                        activity,
                        true,
                        object : Callback<HttpRequestResultBean<String>?>() {
                            override fun callback(returnData: HttpRequestResultBean<String>?) {
                                if (returnData != null) {
                                    var all: Boolean? = SharedPreferenceUtils.getData(
                                        Constants.POSITION_ALLL_CHECKED,
                                        true
                                    ) as Boolean
                                    FryingUtil.showToast(mContext, "Success")
                                    viewModel?.getPositionData(binding?.contractWithLimit?.isChecked)
                                }
                            }

                            override fun error(type: Int, error: Any?) {
                                FryingUtil.showToast(activity, error.toString())
                            }
                        })
                }
                else{
                    val contentView = LayoutInflater.from(mContext).inflate(R.layout.zong_quan_yi_dialog, null)
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
                        ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
                    dialog.setContentView(contentView, layoutParams)
                    dialog.show()
                    dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->

                        dialog.dismiss()
                    }
                }
            }
        }
    }

    override fun onGetPositionData(positionList: ArrayList<PositionBean?>?) {
        CommonUtil.checkActivityAndRunOnUI(mContext) {
            onTabModelListener?.onCount(positionList?.size ?: 0)
            num = positionList?.size?: 0
                adapter?.data = null
                adapter?.notifyDataSetChanged()
                adapter?.data = positionList
                adapter?.notifyDataSetChanged()
        }
    }

    override fun onFundingRate(fundRate: FundingRateBean?) {

    }

    override fun onLeverageDetail(leverageBracket: LeverageBracketBean?) {

    }

    override fun onPosition(positionList: UserPositionBean?) {
        Log.d("222",positionList.toString())
    }

    override fun onMarketPrice(marketPrice: MarkPriceBean?) {
        //adapter?.set(marketPrice?.p)

    }


    interface OnTabModelListener {
        fun onCount(count: Int?)
    }

    companion object {
        fun newInstance(type: ContractRecordTabBean?): ContractPositionTabFragment {
            val args = Bundle()
            val fragment = ContractPositionTabFragment()
            fragment.arguments = args
            fragment.type = type
            return fragment
        }
    }
}