package com.black.base.manager

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.black.base.R
import com.black.base.api.CommunityApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.community.RedPacket
import com.black.base.model.community.RedPacketPub
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil.showToast
import com.black.base.util.RouterConstData
import com.black.base.view.RedPacketGetWindow
import com.black.base.view.RedPacketGetWindow.OnRedPacketOpenListener
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.BlackRouterHelper
import com.black.router.BlackRouterImpl
import com.black.router.RouteDestination
import com.black.router.annotation.Route
import com.black.util.Callback

@Route(value = [RouterConstData.RED_PACKET], beforePath = RouterConstData.LOGIN)
class RedPacketRouterController : BlackRouterHelper {
    private var routeDestination: RouteDestination? = null
    private var redPacketId: String? = null
    private var redPacket: RedPacket? = null
    private var redPacketPub: RedPacketPub? = null

    override fun bindRouteDestination(routeDestination: RouteDestination): BlackRouterHelper {
        this.routeDestination = routeDestination
        val bundle = routeDestination.getExtras()
        redPacketId = bundle?.getString(ConstData.RED_PACKET_ID)
        redPacket = bundle?.getParcelable(ConstData.RED_PACKET)
        return this
    }

    override fun goContext(context: Context) {
        if (redPacket?.status != RedPacket.NEW) {
            redPacketDetailRouter.go(context)
        } else {
            checkRedPackageStatus(context, object : NormalCallback<Int?>(context) {
                override fun callback(returnData: Int?) {
                    if (returnData != null && returnData != RedPacket.NEW) {
                        if (routeDestination?.single != null) {
                            routeDestination?.single?.onSingleReceived(returnData, null)
                        }
                        redPacketDetailRouter.go(context)
                    } else {
                        showOpenPacketDialog(context, object : OnRedPacketOpenListener {
                            override fun onOpenResult(window: RedPacketGetWindow, result: Int) {
                                window.dismiss()
                                if (routeDestination?.single != null) {
                                    routeDestination?.single?.onSingleReceived(result, null)
                                }
                                redPacketDetailRouter.go(context)
                            }

                            override fun onOpenDetail(window: RedPacketGetWindow) {
                                redPacketDetailRouter.go(context)
                            }
                        })
                    }
                }
            })
        }
    }

    override fun goFragment(fragment: Fragment) {
        if (redPacket?.status != RedPacket.NEW) {
            redPacketDetailRouter.go(fragment)
        } else {
            checkRedPackageStatus(fragment.activity, object : NormalCallback<Int?>(fragment.activity!!) {
                override fun error(type: Int, error: Any?) {
                    super.error(type, error)
                }

                override fun callback(returnData: Int?) {
                    if (returnData != null && returnData != RedPacket.NEW) {
                        if (routeDestination?.single != null) {
                            routeDestination?.single?.onSingleReceived(returnData, null)
                        }
                        redPacketDetailRouter.go(fragment)
                    } else {
                        showOpenPacketDialog(context, object : OnRedPacketOpenListener {
                            override fun onOpenResult(window: RedPacketGetWindow, result: Int) {
                                window.dismiss()
                                if (routeDestination?.single != null) {
                                    routeDestination?.single?.onSingleReceived(result, null)
                                }
                                redPacketDetailRouter.go(fragment)
                            }

                            override fun onOpenDetail(window: RedPacketGetWindow) {
                                redPacketDetailRouter.go(fragment)
                            }
                        })
                    }
                }
            })
        }
    }

    private fun checkRedPackageStatus(context: Context?, callback: Callback<Int?>?) {
        CommunityApiServiceHelper.getRedPacketSummary(context, redPacket?.packetId, object : Callback<HttpRequestResultData<RedPacketPub?>?>() {
            override fun error(type: Int, error: Any) {
                callback?.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<RedPacketPub?>?) {
                if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                    redPacketPub = returnData.data
                    if (redPacketPub != null) {
                        if (callback != null) {
                            if (redPacketPub?.userIsGrabbed != null && redPacketPub?.userIsGrabbed!!) {
                                callback.callback(RedPacket.IS_OPEN)
                            } else if (redPacketPub?.status != null) {
                                when (redPacketPub?.status) {
                                    1 -> {
                                        callback.callback(RedPacket.IS_OVER)
                                    }
                                    2 -> {
                                        callback.callback(RedPacket.IS_OVER_TIME)
                                    }
                                    else -> {
                                        callback.callback(RedPacket.NEW)
                                    }
                                }
                            } else {
                                callback.callback(RedPacket.NEW)
                            }
                        }
                    } else {
                        showToast(context, context?.resources?.getString(R.string.error_data))
                    }
                } else {
                    showToast(context, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    private fun showOpenPacketDialog(context: Context, onRedPacketOpenListener: OnRedPacketOpenListener) {
//        RedPacket redPacket = new RedPacket();
//        redPacket.title = "恭喜发财";
//        redPacket.sendName = "fbx";
        val window = RedPacketGetWindow(context, redPacket)
        window.setOnRedPacketOpenListener(onRedPacketOpenListener)
        window.show()
    }

    private val redPacketDetailRouter: BlackRouterImpl
        get() {
            val bundle = Bundle()
            bundle.putParcelable(ConstData.RED_PACKET, if (redPacketPub == null) RedPacketPub(redPacket!!) else redPacketPub)
            return BlackRouter.getInstance().build(RouterConstData.RED_PACKET_DETAIL).with(bundle)
        }
}