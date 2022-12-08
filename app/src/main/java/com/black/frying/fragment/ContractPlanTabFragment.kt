package com.black.frying.fragment

import android.graphics.drawable.ColorDrawable
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import com.black.base.api.FutureApiServiceHelper
import com.black.base.fragment.BaseFragment
import com.black.base.model.ContractRecordTabBean
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.PagingData
import com.black.base.model.future.PlansBean
import com.black.base.model.future.PositionBean
import com.black.base.model.socket.PairStatus
import com.black.base.util.*
import com.black.base.widget.AutoHeightViewPager
import com.black.frying.adapter.ContractPlanTabAdapter
import com.black.router.BlackRouter
import com.black.util.Callback
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentHomePageContractDetailBinding
import io.reactivex.Observer
import skin.support.content.res.SkinCompatResources
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author 合约计划委托列表页
 */
class ContractPlanTabFragment : BaseFragment(), AdapterView.OnItemClickListener,
    View.OnClickListener {
    private var type: ContractRecordTabBean? = null

    private var binding: FragmentHomePageContractDetailBinding? = null
    private var mViewPager: AutoHeightViewPager? = null

    private var adapter: ContractPlanTabAdapter? = null
    private var dataList:ArrayList<PlansBean?>? = ArrayList()
    private val dataMap: MutableMap<String?, PairStatus?> = HashMap()

    //异步获取数据
    private var handlerThread: HandlerThread? = null
    private var socketHandler: Handler? = null
    private var onTabModelListener: OnTabModelListener? = null

    private var pairObserver: Observer<ArrayList<PairStatus?>?>? = null
    private var gettingPairsData: Boolean? = false

    /**
     * adapter height when viewpager in scrollview
     */
    fun setAutoHeightViewPager(viewPager: AutoHeightViewPager?) {
        mViewPager = viewPager
    }

    fun setOnTabModeListener(tabListener:OnTabModelListener){
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
        getPlanData("UNFINISHED")
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
            R.id.btn_action -> BlackRouter.getInstance().build(RouterConstData.DEAR_PAIR_SEARCH)
                .go(mContext) { _, _ -> }
        }
    }

    /**
     * 获取当前委托列表数据
     */
    private fun getPlanData(state:String?){
        FutureApiServiceHelper.getPlanList(context,state, false,
            object : Callback<HttpRequestResultBean<PagingData<PlansBean?>?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("iiiiii-->planData--error", error.toString());
                }

                override fun callback(returnData: HttpRequestResultBean<PagingData<PlansBean?>?>?) {
                    if (returnData != null) {
//                        dataList = returnData.result
                        Log.d("iiiiii-->planDataSize = ", returnData.result?.items?.size.toString())
                        adapter?.data = returnData.result?.items
                        onTabModelListener?.onCount(returnData.result?.items?.size)
                        adapter?.notifyDataSetChanged()
                    }
                }
            })
    }

    interface OnTabModelListener {
        fun onCount(count:Int?)
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