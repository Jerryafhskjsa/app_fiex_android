package com.black.wallet.activity

import android.animation.ValueAnimator
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.PairApiServiceHelper
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.SuccessObserver
import com.black.base.model.socket.PairStatus
import com.black.base.model.wallet.WalletLeverDetail
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.*
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.black.wallet.R
import com.black.wallet.databinding.ActivityWalletLeverDetailBinding
import io.reactivex.Observer
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max
import kotlin.math.min


@Route(value = [RouterConstData.WALLET_LEVER_DETAIL], beforePath = RouterConstData.LOGIN)
class WalletLeverDetailActivity : BaseActionBarActivity(), View.OnClickListener {
    private var pair: String? = null
    private var coinType: String? = null
    private var set: String? = null
    private val coinTypeList = ArrayList<String>()

    private val minDegree = (-130.0).toFloat()
    private val maxDegree = 130.0.toFloat()
    private var lastDegree = minDegree

    private var binding: ActivityWalletLeverDetailBinding? = null

    private var leverDetail: WalletLeverDetail? = null
    private var precision = 6

    private var leverDetailObserver: Observer<WalletLeverDetail?>? = createLeverDetailObserver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pair = intent.getStringExtra(ConstData.PAIR)
        val coinArray = pair?.split("_")
        if (coinArray == null || coinArray.size != 2) {
            finish()
            return
        } else {
            coinType = coinArray[0]
            set = coinArray[1]
            coinTypeList.add(coinArray[0])
            coinTypeList.add(coinArray[1])
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_lever_detail)

        binding?.pair?.setText(pair?.replace("_", "/") ?: nullAmount)

        binding?.riskRate?.setText(String.format("风险率 %s%%", nullAmount))
        binding?.explodePrice?.setText(String.format("爆仓价 %s%s", nullAmount, nullAmount))
        binding?.coinType?.setText(coinType ?: nullAmount)
        binding?.set?.setText(set ?: nullAmount)

        binding?.btnTransfer?.setOnClickListener(this)
        binding?.btnBorrow?.setOnClickListener(this)
        binding?.btnBack?.setOnClickListener(this)

        val dm = resources.displayMetrics
        binding?.riskPointer?.pivotX = dm.density * 9
        binding?.riskPointer?.pivotY = dm.density * 26

        binding?.riskPointer?.rotation = minDegree
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return "逐仓账户"
    }

    //设置风险图标旋转角度
    private fun setRiskRotation(rotation: Float) {
        var degree = min(rotation, maxDegree)
        degree = max(degree, minDegree)
        val startDegree = lastDegree
        val animator = ValueAnimator.ofFloat(0f, degree - startDegree)
        animator.duration = 300
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener {
            val thisDegree: Float = it.animatedValue as Float
            binding?.riskPointer?.rotation = thisDegree + startDegree
        }
        animator.start()
        lastDegree = rotation
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_transfer -> {
                FryingUtil.checkAndAgreeLeverProtocol(mContext, Runnable {
                    val bundle = Bundle()
                    bundle.putString(ConstData.PAIR, pair)
                    BlackRouter.getInstance().build(RouterConstData.WALLET_TRANSFER).with(bundle).go(this)
                })
            }
            R.id.btn_back -> {
                val bundle = Bundle()
                bundle.putString(ConstData.PAIR, pair)
                BlackRouter.getInstance().build(RouterConstData.LEVER_BACK).with(bundle).go(this)
            }
            R.id.btn_borrow -> {
                val bundle = Bundle()
                bundle.putString(ConstData.PAIR, pair)
                BlackRouter.getInstance().build(RouterConstData.LEVER_BORROW).with(bundle).go(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getWalletLeverDetail(true)
        if (leverDetailObserver == null) {
            leverDetailObserver = createLeverDetailObserver()
        }
        SocketDataContainer.subscribeUserLeverDetailObservable(leverDetailObserver)
        val bundle = Bundle()
        bundle.putString(ConstData.PAIR, pair)
        SocketUtil.sendSocketCommandBroadcast(this, SocketUtil.COMMAND_LEVER_DETAIL_START, bundle)
    }

    override fun onStop() {
        super.onStop()
        if (leverDetailObserver != null) {
            SocketDataContainer.removeUserLeverDetailObservable(leverDetailObserver)
        }
        SocketUtil.sendSocketCommandBroadcast(this, SocketUtil.COMMAND_LEVER_DETAIL_FINISH)
    }

    private fun createLeverDetailObserver(): Observer<WalletLeverDetail?>? {
        return object : SuccessObserver<WalletLeverDetail?>() {
            override fun onSuccess(value: WalletLeverDetail?) {
                if (value != null) {
                    SocketDataContainer.computeWalletLeverDetailTotal(mContext, value)
                            ?.compose(RxJavaHelper.observeOnMainThread())
                            ?.subscribe(HttpCallbackSimple(mContext, false, object : NormalCallback<WalletLeverDetail>() {

                                override fun error(type: Int, error: Any?) {
                                }

                                override fun callback(returnData: WalletLeverDetail?) {
                                    if (returnData != null && TextUtils.equals(returnData.pair, pair)) {
                                        leverDetail = returnData
                                        showDetail()
                                    }
                                }

                            }))
                }
            }
        }
    }

    private fun getWalletLeverDetail(force: Boolean) {
        SocketDataContainer.getWalletLeverDetailTotal(this, pair, force)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(this, false, object : NormalCallback<WalletLeverDetail>() {

                    override fun callback(returnData: WalletLeverDetail?) {
                        if (returnData != null && TextUtils.equals(returnData.pair, pair)) {
                            leverDetail = returnData
                            showDetail()
                        }
                    }

                }))
    }

    private fun showDetail() {
        val checkRiskRate = leverDetail?.riskRate == null || leverDetail?.riskRate == BigDecimal.ZERO
        val checkExplodePrice = leverDetail?.burstPrice == null || leverDetail?.burstPrice == BigDecimal.ZERO
        binding?.riskRate?.setText(String.format("风险率 %s%s",
                if (checkRiskRate) nullAmount else if (leverDetail?.riskRate!! > BigDecimal(2)) ">200.00" else NumberUtil.formatNumberNoGroupHardScale(leverDetail?.riskRate!! * BigDecimal(100), 2),
                if (checkRiskRate) nullAmount else "%"))
        if (leverDetail?.riskRate == null || leverDetail?.riskRate == BigDecimal.ZERO || leverDetail?.riskRate!! >= BigDecimal(2)) {
            binding?.explodePrice?.setText(String.format("爆仓价 %s%s", nullAmount, nullAmount))
        } else {
            binding?.explodePrice?.setText(String.format("爆仓价 %s%s",
                    if (checkExplodePrice) nullAmount else NumberUtil.formatNumberNoGroupScale(leverDetail?.burstPrice, RoundingMode.FLOOR, 0, precision),
                    if (checkExplodePrice || leverDetail?.afterCoinType == null) nullAmount else leverDetail?.afterCoinType))
        }

        binding?.total?.setText(String.format("%s %s",
                if (leverDetail?.totalCNY == null) "0.00" else NumberUtil.formatNumberNoGroupScale(leverDetail?.totalCNY, RoundingMode.FLOOR, 2, 2),
                "CNY"))
        binding?.totalBorrow?.setText(String.format("%s %s",
                if (leverDetail?.totalDebtCNY == null) "0.00" else NumberUtil.formatNumberNoGroupScale(leverDetail?.totalDebtCNY, RoundingMode.FLOOR, 2, 2),
                "CNY"))
        binding?.usable?.setText(String.format("%s %s",
                if (leverDetail?.netAssetsCNY == null) "0.00" else NumberUtil.formatNumberNoGroupScale(leverDetail?.netAssetsCNY, RoundingMode.FLOOR, 2, 2),
                "CNY"))

        binding?.coinType?.setText(leverDetail?.coinType ?: nullAmount)
        binding?.set?.setText(leverDetail?.afterCoinType ?: nullAmount)
        binding?.couldBorrowCoin?.setText(if (leverDetail?.coinMaxBorrow == null) nullAmount else NumberUtil.formatNumberNoGroupScale(leverDetail?.coinMaxBorrow, RoundingMode.FLOOR, 0, 8))
        binding?.hasBorrowCoin?.setText(if (leverDetail?.coinBorrow == null) nullAmount else NumberUtil.formatNumberNoGroupScale(leverDetail?.coinBorrow, RoundingMode.FLOOR, 0, 8))
        binding?.interestCoin?.setText(if (leverDetail?.coinInterest == null) nullAmount else NumberUtil.formatNumberNoGroupScale(leverDetail?.coinInterest, RoundingMode.FLOOR, 0, 8))
        binding?.usableCoin?.setText(if (leverDetail?.coinAmount == null) nullAmount else NumberUtil.formatNumberNoGroupScale(leverDetail?.coinAmount, RoundingMode.FLOOR, 0, 8))
        binding?.frozeCoin?.setText(if (leverDetail?.coinFroze == null) nullAmount else NumberUtil.formatNumberNoGroupScale(leverDetail?.coinFroze, RoundingMode.FLOOR, 0, 8))
        binding?.totalCoin?.setText(if (leverDetail?.totalAmount == null) nullAmount else NumberUtil.formatNumberNoGroupScale(leverDetail?.totalAmount, RoundingMode.FLOOR, 0, 8))
        binding?.couldBorrowSet?.setText(if (leverDetail?.afterCoinMaxBorrow == null) nullAmount else NumberUtil.formatNumberNoGroupScale(leverDetail?.afterCoinMaxBorrow, RoundingMode.FLOOR, 0, 8))
        binding?.hasBorrowSet?.setText(if (leverDetail?.afterCoinBorrow == null) nullAmount else NumberUtil.formatNumberNoGroupScale(leverDetail?.afterCoinBorrow, RoundingMode.FLOOR, 0, 8))
        binding?.interestSet?.setText(if (leverDetail?.afterCoinInterest == null) nullAmount else NumberUtil.formatNumberNoGroupScale(leverDetail?.afterCoinInterest, RoundingMode.FLOOR, 0, 8))
        binding?.usableSet?.setText(if (leverDetail?.afterCoinAmount == null) nullAmount else NumberUtil.formatNumberNoGroupScale(leverDetail?.afterCoinAmount, RoundingMode.FLOOR, 0, 8))
        binding?.frozeSet?.setText(if (leverDetail?.afterCoinFroze == null) nullAmount else NumberUtil.formatNumberNoGroupScale(leverDetail?.afterCoinFroze, RoundingMode.FLOOR, 0, 8))
        binding?.totalSet?.setText(if (leverDetail?.afterTotalAmount == null) nullAmount else NumberUtil.formatNumberNoGroupScale(leverDetail?.afterTotalAmount, RoundingMode.FLOOR, 0, 8))

        //计算图像旋转角度
        val riskRate = leverDetail?.riskRate
        val minRate = leverDetail?.burstRate
        val maxRate = leverDetail?.outRate
        if (riskRate == null || riskRate == BigDecimal.ZERO || minRate == null || maxRate == null || minRate >= maxRate) {
            setRiskRotation((minDegree))
        } else {
            val rateRange = maxRate - minRate
            if (rateRange <= BigDecimal.ZERO) {
                setRiskRotation((minDegree))
            } else {
                var degree: Float = (BigDecimal(minDegree.toDouble()) + (maxRate - riskRate) / rateRange * (BigDecimal(minDegree.toDouble()) - (BigDecimal(minDegree.toDouble())))).toFloat()
                degree = min(degree, maxDegree)
                degree = max(degree, minDegree)
                setRiskRotation((degree))
            }
        }
    }

    private fun getPairInfo() {
        PairApiServiceHelper.getTradePairInfo(this, pair, object : Callback<HttpRequestResultDataList<PairStatus?>?>() {
            override fun error(type: Int, error: Any) {}
            override fun callback(returnData: HttpRequestResultDataList<PairStatus?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS && returnData.data != null) {
                    for (pairStatus in returnData.data!!) {
                        if (TextUtils.equals(pair, pairStatus?.pairName)) {
//                            val supportingPrecisionList = pairStatus?.supportingPrecisionList
//                            var maxPrecision = CommonUtil.getMax(supportingPrecisionList)
//                            maxPrecision = if (maxPrecision == null || maxPrecision == 0) 6 else maxPrecision
//                            precision = maxPrecision
                            showDetail()
                            break
                        }
                    }
                }
            }
        })
    }
}