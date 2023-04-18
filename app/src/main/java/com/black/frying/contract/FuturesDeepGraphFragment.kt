package com.black.frying.contract

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.black.frying.contract.state.FutureGlobalStateViewModel
import com.black.frying.contract.viewmodel.FuturesTransactionInfoDisplayViewModel
import com.black.frying.view.ContractDeepViewBinding
import com.black.lib.view.ProgressDrawable
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentLayoutFuturesDeepGraphDisplayBinding
import com.fbsex.exchange.databinding.FuturesLayoutDeepGraphBinding
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder
import skin.support.content.res.SkinCompatResources

class FuturesDeepGraphFragment : Fragment() {
    enum class ShowMode {
        DEFAULT,
        SELL,
        BUY;

      public  fun ifDefault() = this ==DEFAULT
    }

    companion object {
        const val TAG = "FuturesDeepGraphFragment"
        fun newInstance() = FuturesDeepGraphFragment()
    }

    private  val viewModel: FuturesTransactionInfoDisplayViewModel by lazy {
      ViewModelProvider(this).get(FuturesTransactionInfoDisplayViewModel::class.java)
    }
    private val globalViewModel: FutureGlobalStateViewModel by lazy {
        ViewModelProvider(
            requireActivity()
        )[FutureGlobalStateViewModel::class.java]
    }

    private val binding: FuturesLayoutDeepGraphBinding by lazy {
        FuturesLayoutDeepGraphBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        globalViewModel.printThis()
        setupBindView()

        setupBindData()
    }
    private val colorTransparent: Int by lazy {  SkinCompatResources.getColor(context, R.color.transparent) }
    private val colorT7A10: Int by lazy {  SkinCompatResources.getColor(context, R.color.T7_ALPHA10)}
    private val colorT5A10: Int by lazy { SkinCompatResources.getColor(context, R.color.T5_ALPHA10)}

    private fun setupBindView() {
        binding.apply {
            //set rv
            rvUpList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = object:CommonAdapter<Array<String?>?>(context,R.layout.futures_layout_deep_graph_item,viewModel.sellList){
                    override fun convert(holder: ViewHolder?, t: Array<String?>?, position: Int) {
                        holder?.getView<ViewGroup>(R.id.handicap_buy_layout_01)?.let {
                            background = ProgressDrawable(colorT5A10, colorTransparent, ProgressDrawable.RIGHT)
                        }

                        holder?.getView<TextView>(R.id.price_buy_01)?.let {
                            it.text = t?.get(0)
                        }
                        holder?.getView<TextView>(R.id.count_buy_01)?.let {
                            it.text = t?.get(1)
                        }
                    }
                }
            }

            rvBtmList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = object:CommonAdapter<Array<String?>?>(context,R.layout.futures_layout_deep_graph_item,viewModel.buyList){
                    override fun convert(holder: ViewHolder?, t: Array<String?>?, position: Int) {
                        holder?.getView<ViewGroup>(R.id.handicap_buy_layout_01)?.let {
                            background = ProgressDrawable(colorT7A10, colorTransparent, ProgressDrawable.RIGHT)
                        }

                        holder?.getView<TextView>(R.id.price_buy_01)?.let {
                            it.text = t?.get(0)
                        }
                        holder?.getView<TextView>(R.id.count_buy_01)?.let {
                            it.text = t?.get(1)
                        }
                    }
                }
            }

            displayType.setOnClickListener {
                viewModel.clickShowMode()
            }
        }
    }

    val maxDoubleShowCount = 5

    private fun setupBindData() {
       val showCount =  if(viewModel.showMode.value!!.ifDefault()){maxDoubleShowCount}else maxDoubleShowCount*2
        globalViewModel.tradeOrderDepthLiveData.observe(viewLifecycleOwner){
            it.a?.let {
                viewModel.sellList.apply {
                    clear()
                    addAll(it.take(showCount).toList())
                }
                binding.rvUpList?.adapter?.notifyDataSetChanged()
            }
            it.b?.let {
                viewModel.buyList.apply {
                    clear()
                    addAll(it.take(showCount).toList())
                }
                binding.rvBtmList?.adapter?.notifyDataSetChanged()
            }
        }

        viewModel.showMode.observe(viewLifecycleOwner) {
            when (it) {
                ShowMode.DEFAULT -> {
                    binding.apply {
                        rvBtmList.visibility = View.VISIBLE
                        rvUpList.visibility = View.VISIBLE
                        displayType.setImageDrawable(
                            SkinCompatResources.getDrawable(
                                context,
                                R.drawable.icon_trade_deep
                            )
                        )
                    }
                }
                ShowMode.SELL -> {
                    binding.apply {
                        rvUpList.visibility = View.VISIBLE
                        rvBtmList.visibility = View.GONE
                        displayType.setImageDrawable(
                            SkinCompatResources.getDrawable(
                                context,
                                R.drawable.icon_trade_deep_02
                            )
                        )
                    }
                }
                ShowMode.BUY -> {
                    binding.apply {
                        rvUpList.visibility = View.GONE
                        rvBtmList.visibility = View.VISIBLE
                        displayType.setImageDrawable(
                            SkinCompatResources.getDrawable(
                                context,
                                R.drawable.icon_trade_deep_03
                            )
                        )
                    }
                }
                else -> {}
            }
        }
    }

}