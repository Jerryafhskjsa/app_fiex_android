package com.black.im.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.DecimalFormat

object NetWorkUtils {
    /**
     * Network type is unknown
     */
    const val NETWORK_TYPE_UNKNOWN = 0
    /**
     * Current network is GPRS
     */
    const val NETWORK_TYPE_GPRS = 1
    /**
     * Current network is EDGE
     */
    const val NETWORK_TYPE_EDGE = 2
    /**
     * Current network is UMTS
     */
    const val NETWORK_TYPE_UMTS = 3
    /**
     * Current network is CDMA: Either IS95A or IS95B
     */
    const val NETWORK_TYPE_CDMA = 4
    /**
     * Current network is EVDO revision 0
     */
    const val NETWORK_TYPE_EVDO_0 = 5
    /**
     * Current network is EVDO revision A
     */
    const val NETWORK_TYPE_EVDO_A = 6
    /**
     * Current network is 1xRTT
     */
    const val NETWORK_TYPE_1xRTT = 7
    /**
     * Current network is HSDPA
     */
    const val NETWORK_TYPE_HSDPA = 8
    /**
     * Current network is HSUPA
     */
    const val NETWORK_TYPE_HSUPA = 9
    /**
     * Current network is HSPA
     */
    const val NETWORK_TYPE_HSPA = 10
    /**
     * Current network is iDen
     */
    const val NETWORK_TYPE_IDEN = 11
    /**
     * Current network is EVDO revision B
     */
    const val NETWORK_TYPE_EVDO_B = 12
    /**
     * Current network is LTE
     */
    const val NETWORK_TYPE_LTE = 13
    /**
     * Current network is eHRPD
     */
    const val NETWORK_TYPE_EHRPD = 14
    /**
     * Current network is HSPA+
     */
    const val NETWORK_TYPE_HSPAP = 15
    // 适配低版本手机
    private const val NETWORK_TYPE_UNAVAILABLE = -1
    // private static final int NETWORK_TYPE_MOBILE = -100;
    private const val NETWORK_TYPE_WIFI = -101
    private const val NETWORK_CLASS_WIFI = -101
    private const val NETWORK_CLASS_UNAVAILABLE = -1
    /**
     * Unknown network class.
     */
    private const val NETWORK_CLASS_UNKNOWN = 0
    /**
     * Class of broadly defined "2G" networks.
     */
    private const val NETWORK_CLASS_2_G = 1
    /**
     * Class of broadly defined "3G" networks.
     */
    private const val NETWORK_CLASS_3_G = 2
    /**
     * Class of broadly defined "4G" networks.
     */
    private const val NETWORK_CLASS_4_G = 3
    var sIMSDKConnected = false
    private val df = DecimalFormat("#.##")
    fun isWifiAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected && networkInfo
                .type == ConnectivityManager.TYPE_WIFI
    }

    /**
     * 获取MAC地址
     *
     * @param context
     * @return
     */
    fun getMacAddress(context: Context?): String? {
        if (context == null) {
            return ""
        }
        var localMac: String? = null
        if (isWifiAvailable(context)) {
            localMac = getWifiMacAddress(context)
        }
        if (localMac != null && localMac.length > 0) {
            localMac = localMac.replace(":", "-").toLowerCase()
            return localMac
        }
        localMac = macFromCallCmd
        if (localMac != null) {
            localMac = localMac.replace(":", "-").toLowerCase()
        }
        return localMac
    }

    private fun getWifiMacAddress(context: Context): String? {
        var localMac: String? = null
        try {
            val wifi = context
                    .getSystemService(Context.WIFI_SERVICE) as WifiManager
            val info = wifi.connectionInfo
            if (wifi.isWifiEnabled) {
                localMac = info.macAddress
                if (localMac != null) {
                    localMac = localMac.replace(":", "-").toLowerCase()
                    return localMac
                }
            }
        } catch (e: Exception) {
        }
        return null
    }// 对该行数据进行解析
// 例如：eth0 Link encap:Ethernet HWaddr 00:16:E8:3E:DF:67

    /**
     * 通过callCmd("busybox ifconfig","HWaddr")获取mac地址
     *
     * @return Mac Address
     * @attention 需要设备装有busybox工具
     */
    private val macFromCallCmd: String?
        private get() {
            var result = ""
            result = callCmd("busybox ifconfig", "HWaddr")
            if (result == null || result.length <= 0) {
                return null
            }
            Log.v("tag", "cmd result : $result")
            // 对该行数据进行解析
            // 例如：eth0 Link encap:Ethernet HWaddr 00:16:E8:3E:DF:67
            if (result.length > 0 && result.contains("HWaddr") == true) {
                val Mac = result.substring(result.indexOf("HWaddr") + 6,
                        result.length - 1)
                if (Mac.length > 1) {
                    result = Mac.replace(" ".toRegex(), "")
                }
            }
            return result
        }

    fun callCmd(cmd: String?, filter: String?): String {
        var result = ""
        var line = ""
        try {
            val proc = Runtime.getRuntime().exec(cmd)
            val `is` = InputStreamReader(proc.inputStream)
            val br = BufferedReader(`is`)
            // 执行命令cmd，只取结果中含有filter的这一行
            while (br.readLine().also { line = it } != null
                    && line.contains(filter!!) == false) {
            }
            result = line
        } catch (e: Exception) {
        }
        return result
    }

    /**
     * 网络是否可用
     *
     * @param context
     * @return
     */
    fun IsNetWorkEnable(context: Context): Boolean {
        try {
            val connectivity = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    ?: return false
            val info = connectivity.activeNetworkInfo
            if (info != null && info.isConnected) { // 判断当前网络是否已经连接
                if (info.state == NetworkInfo.State.CONNECTED) {
                    return true
                }
            }
        } catch (e: Exception) {
        }
        return false
    }

    /**
     * 格式化大小
     *
     * @param size
     * @return
     */
    fun formatSize(size: Long): String {
        var unit = "B"
        var len = size.toFloat()
        if (len > 900) {
            len /= 1024f
            unit = "KB"
        }
        if (len > 900) {
            len /= 1024f
            unit = "MB"
        }
        if (len > 900) {
            len /= 1024f
            unit = "GB"
        }
        if (len > 900) {
            len /= 1024f
            unit = "TB"
        }
        return df.format(len.toDouble()) + unit
    }

    fun formatSizeBySecond(size: Long): String {
        var unit = "B"
        var len = size.toFloat()
        if (len > 900) {
            len /= 1024f
            unit = "KB"
        }
        if (len > 900) {
            len /= 1024f
            unit = "MB"
        }
        if (len > 900) {
            len /= 1024f
            unit = "GB"
        }
        if (len > 900) {
            len /= 1024f
            unit = "TB"
        }
        return df.format(len.toDouble()) + unit + "/s"
    }

    fun format(size: Long): String {
        var unit = "B"
        var len = size.toFloat()
        if (len > 1000) {
            len /= 1024f
            unit = "KB"
            if (len > 1000) {
                len /= 1024f
                unit = "MB"
                if (len > 1000) {
                    len /= 1024f
                    unit = "GB"
                }
            }
        }
        return df.format(len.toDouble()) + "\n" + unit + "/s"
    }

    /**
     * 获取网络类型
     *
     * @return
     */
    fun getCurrentNetworkType(context: Context): String {
        val networkClass = getNetworkClass(context)
        var type = "未知"
        when (networkClass) {
            NETWORK_CLASS_UNAVAILABLE -> type = "无"
            NETWORK_CLASS_WIFI -> type = "Wi-Fi"
            NETWORK_CLASS_2_G -> type = "2G"
            NETWORK_CLASS_3_G -> type = "3G"
            NETWORK_CLASS_4_G -> type = "4G"
            NETWORK_CLASS_UNKNOWN -> type = "未知"
        }
        return type
    }

    private fun getNetworkClassByType(networkType: Int): Int {
        return when (networkType) {
            NETWORK_TYPE_UNAVAILABLE -> NETWORK_CLASS_UNAVAILABLE
            NETWORK_TYPE_WIFI -> NETWORK_CLASS_WIFI
            NETWORK_TYPE_GPRS, NETWORK_TYPE_EDGE, NETWORK_TYPE_CDMA, NETWORK_TYPE_1xRTT, NETWORK_TYPE_IDEN -> NETWORK_CLASS_2_G
            NETWORK_TYPE_UMTS, NETWORK_TYPE_EVDO_0, NETWORK_TYPE_EVDO_A, NETWORK_TYPE_HSDPA, NETWORK_TYPE_HSUPA, NETWORK_TYPE_HSPA, NETWORK_TYPE_EVDO_B, NETWORK_TYPE_EHRPD, NETWORK_TYPE_HSPAP -> NETWORK_CLASS_3_G
            NETWORK_TYPE_LTE -> NETWORK_CLASS_4_G
            else -> NETWORK_CLASS_UNKNOWN
        }
    }

    private fun getNetworkClass(context: Context): Int {
        var networkType = NETWORK_TYPE_UNKNOWN
        try {
            val network = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
            if (network != null && network.isAvailable
                    && network.isConnected) {
                val type = network.type
                if (type == ConnectivityManager.TYPE_WIFI) {
                    networkType = NETWORK_TYPE_WIFI
                } else if (type == ConnectivityManager.TYPE_MOBILE) {
                    val telephonyManager = context.getSystemService(
                            Context.TELEPHONY_SERVICE) as TelephonyManager
                    networkType = telephonyManager.networkType
                }
            } else {
                networkType = NETWORK_TYPE_UNAVAILABLE
            }
        } catch (ex: Exception) {
        }
        return getNetworkClassByType(networkType)
    }

    fun getWifiRssi(context: Context): String {
        var asu = 85
        try {
            val network = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
            if (network != null && network.isAvailable
                    && network.isConnected) {
                val type = network.type
                if (type == ConnectivityManager.TYPE_WIFI) {
                    val wifiManager = context
                            .getSystemService(Context.WIFI_SERVICE) as WifiManager
                    val wifiInfo = wifiManager.connectionInfo
                    if (wifiInfo != null) {
                        asu = wifiInfo.rssi
                    }
                }
            }
        } catch (e: Exception) {
        }
        return asu.toString() + "dBm"
    }

    fun getWifiSsid(context: Context): String? {
        var ssid = ""
        try {
            val network = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
            if (network != null && network.isAvailable
                    && network.isConnected) {
                val type = network.type
                if (type == ConnectivityManager.TYPE_WIFI) {
                    val wifiManager = context
                            .getSystemService(Context.WIFI_SERVICE) as WifiManager
                    val wifiInfo = wifiManager.connectionInfo
                    if (wifiInfo != null) {
                        ssid = wifiInfo.ssid
                        if (ssid == null) {
                            ssid = ""
                        }
                        ssid = ssid.replace("\"".toRegex(), "")
                    }
                }
            }
        } catch (e: Exception) {
        }
        return ssid
    }

    /**
     * 检查sim卡状态
     *
     * @param
     * @return
     */
    fun checkSimState(context: Context): Boolean {
        val tm = context
                .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return if (tm.simState == TelephonyManager.SIM_STATE_ABSENT
                || tm.simState == TelephonyManager.SIM_STATE_UNKNOWN) {
            false
        } else true
    }
}