package com.black.base.util

import android.content.Context
import android.util.Log
import com.black.util.CommonUtil
import java.net.URL

object UrlConfig {
    val HOSTS = arrayOf(
        "http://abexxx.net",//fiex测试环境
        "http://www.soeasyex.com",//正式环境
    )

    var serverHost = ArrayList<String?>()

    fun getHost(context: Context): String? {
        if (serverHost.size > 0) {
            return serverHost[getIndex(context)]
        }
        return HOSTS[getIndex(context)]
    }

    //https://fiex.io/futures/fapi/market/v1/public/q/deal?symbol=btc_usdt&num=30
    fun getFiexHost(context: Context, apiType: String?): String {
        var apiTypeDes = "/uc/"
        when (apiType) {
            ApiType.URl_UC -> apiTypeDes = "/uc/"
            ApiType.URL_FIC -> apiTypeDes = "/api/"
            ApiType.URL_API -> apiTypeDes = "/otc/api/"
            ApiType.URL_PRO -> apiTypeDes = "/pro/"
            ApiType.URL_FUT_F -> apiTypeDes = "/futures/fapi/"
            ApiType.URL_FUT_D -> apiTypeDes = "/futures/dapi/"
        }
        var index = getIndex(context)
        val serverHost = CookieUtil.getServerHost(context)
        if (serverHost != null && serverHost.size > 0) {
            return serverHost[index] + apiTypeDes
        }
        if (index > HOSTS.size - 1) {
            index = 0
        }
        return HOSTS[index] + apiTypeDes
    }


    private val SOCKET_HOSTS_SOEASTEX = arrayOf(
        "wss://abexxx.net/ws/",//测试
        "wss://soeasyex.com/ws/",//正式
    )

    private val SPOT_SOCKET_HOSTS_SOEASTEX = arrayOf(
        "wss://abexxx.net/socket",//测试
        "wss://soeasyex.com/socket",//正式
    )


    fun getIndex(context: Context): Int {
        if (!CommonUtil.isApkInDebug(context)) {
            return 0
        }
        return CookieUtil.getHostIndex(context)
    }

    /**
     * type->"user","market"
     */
    fun getSocketHostSoeasyEx(context: Context?,type:String?):String{
        return SOCKET_HOSTS_SOEASTEX[getIndex(context!!)]+type
    }


    fun getSpotSocketHostSoeasyEx(context: Context?):String{
        return SPOT_SOCKET_HOSTS_SOEASTEX[getIndex(context!!)]
    }

    //币种图标
    fun getCoinIconUrl(context: Context, pairName: String?): String {
        return getHost(context) + pairName
    }

    /***fiex***/


    fun getSocketHost(context: Context): String {
        return HOSTS[getIndex(context)]
    }

    fun getFinancalUrl(mContext: Context): String {
        return  "http://financial.abexxx.net/financing?from=app"
    }

    fun getFactionRuleUrl(mContext: Context): String {
        return getHost(mContext) + "/pages/league_info/"
    }

    //用户协议
    fun getUrlProcote(context: Context): String {
        return getHost(context) + "/#/help/instruction/31"
    }


    //首页
    fun getUrlHome(context: Context?): String { //        return getHost(context) + "/#/help/apps";
        return "https://www.fbsex.co/download/android.html"
    }

    //邀请地址
    fun getUrlInvite(context: Context): String {
        return getHost(context) + "/extenal/register/%s"
    }

    //公告中心 https://fbsexco.zendesk.com/hc/en-us   https://fbsexco.zendesk.com/hc/zh-cn
    fun getUrlNoticeAll(context: Context?): String {
        return "https://fbsexco.zendesk.com/hc/%s"
    }

    //聚宝盆协议
    fun getUrlDemandProfile(context: Context?): String {
        return "https://fbsexco.zendesk.com/hc/zh-cn/articles/360037936733"
    }

    //活期宝规则
    fun getUrlDemandRule(context: Context?): String {
        return "https://fbsexco.zendesk.com/hc/zh-cn/articles/360037997473"
    }

    //运算力规则
    fun getUrlCloudPowerRule(context: Context): String {
        return getHost(context) + "/pages/mining_info/"
    }

    //运算力协议
    fun getUrlCloudPowerProfile(context: Context?): String {
        return "https://fbsexco.zendesk.com/hc/zh-cn/articles/360039691034"
    }

    //帮助中心
    const val HELP_CENTER = "https://fbsexco.zendesk.com/hc/zh-cn/categories/360002181154"

    //c2c注意事项
    const val URL_C2C_WARNING = "https://fbsexco.zendesk.com/hc/zh-cn/articles/360036825954"

    //c2c用户协议
    const val URL_C2C_RULE = "https://fbsexco.zendesk.com/hc/zh-cn/articles/360037651953"

    //杠杆交易用户协议
    const val URL_LEVER_RULE = "https://fbsexco.zendesk.com/hc/zh-cn/articles/360041746133"

    object ApiType {
        const val URl_UC = "uc"
        const val URL_API = "otc/api"
        const val URL_FIC = "api"
        const val URL_PRO = "pro"
        const val URL_FUT_F = "fut_f" //合约U本位
        const val URL_FUT_D = "fut_d" //合约币本位
    }

    object User {
        //登录
        const val URL_TOKEN = "user/token"

        //ticket
        const val URL_TICKET = "/user/get/ticket"

        //trade_login
        const val URL_TRADE_LOGIN = "user/trade/login"

        //user_login
        const val URL_USER_LOGIN = "user/login"

        //otc_login
        const val  OTC_LOGIN = "otc/user/login"

        //fic_login
        const val  FIC_LOGIN = "financial/user/login"

        //ws_token
        const val URL_WS_TOKEN = "user/ws-token"

        //退出登录
        const val URL_LOGOUT = "user/logout"

        //登录验证
        const val URL_LOGIN_SUFFIX = "user/token/suffix"

        //获取验证码
        const val URL_SEND_VERIFY_CODE = "verify-code/send"

        //获取验证码02
        const val URL_SEND_VERIFY_CODE_02 = "user/verify-code/send"

        //注册
        const val URL_REGISTER = "user/register"

        //获取用户信息
        const val URL_USER_INFO = "user/infos"

        //重置密码
        const val URL_RESET_PASSWORD = "user/password/reset"

        //重置密码验证
        const val URL_RESET_PASSWORD_SUFFIX = "user/password/reset/suffix"

        //修改密码
        const val URL_CHANGE_PASSWORD = "user/password/update"

        //GOOGLE密钥
        const val URL_GOOGLE_CODE = "google"

        //开启/关闭认证 开启关闭类型 action：0-开启手机，1-关闭手机，2-开启google，3-关闭google，4-开启邮箱
        const val URL_ENABLE_SECURITY = "user/security/enable"

        //身份认证-绑定
        const val URL_PHONE_SECURITY = "user/security/phone"

        //手机号修改认证
        const val URL_IDENTITY_BIND = "user/identity/auth"

        //身份认证-上传图片
        const val URL_UPLOAD = "commons/upload"

        //身份认证-智能认证
        const val URL_IDENTITY_BIND_AI = "user/identity/auth/living"

        //查询用户邀请的人数量
        const val URL_RECOMMEND_COUNT = "user/invited/people/count"

        //邀请的人详细信息
        const val URL_RECOMMEND_DETAIL = "user/invited/people/info"

        //邀请奖励折合BTC总数
        const val URL_RECOMMEND_TOTAL_BTC = "user/invited/rewards/btc"

        //查询用户邀请的人数量
        const val URL_RECOMMEND_TOP = "user/invited/rewards/ranking"

        //资金密码
        const val URL_MONEY_PASSWORD = "user/money-password/open"

        //取消资金密码
        const val URL_MONEY_PASSWORD_REMOVE = "user/money-password/close"

        //重置资金密码
        const val URL_MONEY_PASSWORD_RESET = "user/money-password/update"

        //上传图片公网
        const val URL_UPLOAD_PUBLIC = "commons/upload/public"

        //修改昵称，头像
        const val URL_USER_INFO_UPDATE = "user/profile/update"

        //查询PUSH开关
        const val URL_PUSH_SWITCH_LIST = "user/push/switch/list"

        //查询PUSH开关
        const val URL_PUSH_SWITCH_CHANGE = "user/push/switch/update"

        //检查是否可以进入聊天室
        const val URL_CHECK_CHAT_ENABLE = "user/chat/enable"

        //检查是否可以进入主聊天室
        const val URL_CHECK_MAIN_CHAT_ENABLE = "user/main/chat/check"

        //同意杠杆交易协议
        const val URL_AGREE_LEVER_PROTOCOL = "user/openlever/update"
    }

    object C2C {
        //OTC用户相关接口
        //C2C用户账户
        const val URL_C2C_ACCOUNT = "otc/user/account"
        //申请收款人
        const val  URL_C2C_APPLY_PAYEE = "otc/user/applyPayee"
        //删除数据
        const val URL_C2C_DELETE = "otc/user/deleteReceipt"
        //作为收款人信息
        const val URL_C2C_PAYEE = "otc/user/getPayee"
        //获取所有收付款信息
        const val URL_C2C_RECEIPT = "otc/user/getReceipt"
        //otc用户信息
        const val URL_C2C_USERINFO = "otc/user/info"
        //开通
        const val URL_C2C_OPEN = "otc/user/open"
        //拒绝成为收款人
        const val URL_C2C_REFUSE_PAYEE = "otc/user/refusePayee"
        //设置收付款
        const val URL_C2C_SET_RECEIPT = "otc/user/setReceipt"
        //解绑收款人
        const val URL_C2C_UNBIND_PAYEE = "otc/user/unbindPayee"
        //修改收付款状态
        const val URL_C2C_UPDATE = "otc/user/updateReceiptStatus"


        //OTC广告相关接口

        //广告参数配置信息
        const val URL_C2C_CONFIG = "otc/advertising/config"
        //广告参数配置信息
        const val URL_C2C_CONFIG_V2 = "otc/advertising/config/V2"
        //新增广告
        const val URL_C2C_CREATE = "otc/advertising/create"
        //otc动态价格,根据价格类型、币种、法币、方向查询唯一价格
        const val URL_C2C_CURRENT_PRICE = "otc/advertising/currentPrice"
        //删除广告
        const val URL_C2C_AD_DELETE = "otc/advertising/delete"
        //otc指数价格
        const val URL_C2C_INDEX_PRICE = "otc/advertising/indexPrice"
        //广告详情
        const val URL_C2C_AD_INFO = "otc/advertising/info"
        //首页广告
        const val URL_C2C_AD_LIST = "otc/advertising/list"
        //商家详情页广告
        const val URL_C2C_AD_MERCHANT_PAGE = "otc/advertising/merchantPage"
        //我的广告
        const val URL_C2C_MY_LIST = "otc/advertising/myList"
        //otc动态价格
        const val URL_C2C_PRICE = "otc/advertising/price"
        //发布广告
        const val URL_C2C_PUBLISH = "otc/advertising/publish"
        //快速下单广告查询
        const val URL_C2C_QUICK_PUBLISH = "otc/advertising/quickAdvertising"
        //快速下单配置
        const val URL_C2C_QUICK_CONFIG = "otc/advertising/quickOrderConfig"
        //下架广告
        const val URL_C2C_SOLD_OUT = "otc/advertising/soldOut"
        //广告支持币种
        const val URL_C2C_SUPPORT_COIN = "otc/advertising/supportCoin"
        //修改广告
        const val URL_C2C_AD_UPDATE = "otc/advertising/update"

//

        //OTC聊天
        //创建c2c订单-回复 图片
        const val URL_C2C_CREATE_IMG = "otc/order/reply/createImg"
        //创建c2c订单-回复文字
        const val URL_C2C_CRATE_TXT = "otc/order/reply/createTxt"
        //c2c订单-回复列表
        const val URL_C2C_REPLY_LIST = "otc/order/reply/list"
        //c2c订单-拉取消息
        const val URL_C2C_REPLY_PULL = "otc/order/reply/pull"
        //c2c订单-实时推送消息
        const val URL_C2C_REAL_TIME = "otc/order/reply/realtime"
        //
        //OTC订单
        //撤单
        const val URL_C2C_CANCEL = "otc/order/cancel"
        //确认付款
        const val URL_C2C_CONFIRM_PAY = "otc/order/confirmPayment"
        //确认付款并收币
        const val URL_C2C_CP = "otc/order/confirmReceipt"
        //下单V2
        const val URL_C2C_CREATE_V2 = "otc/order/createV2"
        //根据订单Id获取卖方收款方式
        const val URL_C2C_GP = "otc/order/getReceipt"
        //订单详情
        const val URL_C2C_ORDER_INFO = "otc/order/info"
        //订单详情V2
        const val URL_C2C_OI_V2 = "otc/order/info/v2"
        //我的订单
        const val URL_C2C_OL = "otc/order/list"
        //操作台订单
        const val URL_C2C_OO = "otc/order/operatorOrders"
        //未完成订单数
        const val URL_C2C_UFC = "otc/order/unfinishedCount"
        //

        //商家相关
        //添加收款人
        const val URL_C2C_ADD_P = "otc/merchant/addPayee"
        //申请商家
        const val URL_C2C_MCA = "otc/merchant/apply"
        //昵称校验
        const val URL_MCN = "otc/merchant/checkName"
        //获取商家信息
        const val URL_C2C_GM = "otc/merchant/getMerchant"
        //获取收款人列表
        const val URL_C2C_GPL = "otc/merchant/getPayeeList"
        //商家信息
        const val URL_C2C_MCP = "otc/merchant/merchantPage"
        //退出商家
        const val URL_C2C_MP = "otc/merchant/quit"
        //能否退出商家
        const val URL_C2C_QVF = "otc/merchant/quitVerify"
        //解绑收款人
        const val URL_MUP = "otc/merchant/unbindPayee"
        //升级商家
        const val URL_C2C_UMC = "otc/merchant/upgradeMerchant"
        //

        //谷歌验证
        //checkAuth
        const val URL_CA = "otc/check/auth"
        //verifyCode
        const val URL_VFC = "otc/verify-code"
        //上传文件
        const val URL_CU = "otc/commons/upload"
        //登陆
        const val URL_LOGIN = " otc/user/login"
        //
        //OTC申述
        //申诉操作
        const val URL_C2C_ALLEGE = "otc/allege/allege"
        //申述详情
        const val URL_C2C_ALLEGE_INFO = "otc/allege/info"
        //

        //C2C价格 商户列表
        const val URL_C2C_MERCHANT = "c2c/store/list"
        //C2C下单
        const val URL_C2C_CREATE_ORDER = "c2c/order/create"
        const val URL_C2C_CREATE_ORDER_BUY = "c2c/order/buy"
        const val URL_C2C_CREATE_ORDER_SELL = "c2c/order/sell"

        //订单列表 get
        const val URL_C2C_ORDER_LIST = "c2c/order/list"

        //订单留言板列表 get
        const val URL_C2C_ORDER_DETAIL_LIST = "c2c/order/reply/list"

        //创建订单回复     post
        const val URL_C2C_ORDER_DETAIL_CREATE = "c2c/order/reply/create"

        //查询订详情 状态
        const val URL_C2C_ORDER_DETAIL = "c2c/order/detail"

        //商家或用户放币
        const val URL_C2C_ORDER_RELEASE = "c2c/order/finish"

        //商家或用户确认打款
        const val URL_C2C_ORDER_CONFIRM = "c2c/order/pay"

        //查商家或用户取消订单
        const val URL_C2C_ORDER_CANCEL = "c2c/order/cancel"

        //查询是否签订协议：
        const val URL_C2C_IS_AGREE = "c2c/agreement/sign/status"

        //签订协议：
        const val URL_C2C_AGREE = "c2c/agreement/sign"

        //C2C支持币种
        const val URL_C2C_COIN_TYPE = "c2c/support/coin/list"

        //C2C支付方式全部
        const val URL_C2C_PAYMENT_METHOD_ALL = "c2c/pay-method/account/list"

        //C2C支付方式 删除
        const val URL_C2C_PAYMENT_METHOD_DELETE = "c2c/pay-method/account/unbind"

        //C2C支付方式 添加
        const val URL_C2C_PAYMENT_METHOD_ADD = "c2c/pay-method/account/bind"

        //C2C支付方式 （修改状态）激活
        const val URL_C2C_PAYMENT_METHOD_UPDATE = "c2c/pay-method/account/update"

        //C2C快捷商户列表
        const val URL_C2C_MERCHANT_FAST = "c2c/store/fast/list"
    }

    object Trade {
        //创建交易订单new
        const val URL_CREATE_TRADE_ORDER_NEW = "trade/order/create"

        //取消交易订单new
        const val URL_URL_CANCEL_TRADE_ORDER_NEW = "trade/order/cancel"

        //获取交易委托记录
        const val URL_TRADE_ORDERS_RECORD = "trade/order/list"

        //获取交易历史委托记录
        const val URL_TRADE_ORDERS_HISTORY_RECORD = "trade/order/history"

        //获取当前交易对的深度
        const val URL_TRADE_ORDERS_DEPTH = "public/depth"

        //获取当前交易对的成交
        const val URL_TRADE_ORDERS_DEAL = "public/deal"
    }

    object Wallet {
        /***fiex***/
        //获取用户24小时提现额度
        const val URL_USER_WITHDRAW_QUOTA = "withdraw/quota"

        //获取用户资产
        const val URL_BALANCE_LIST = "balance/list?type=SPOT,CONTRACT,OTC,FINANCIAL"

        //币种列表配置
        const val URL_COINS = "config/coins"

        //查询支持的账户划转类型 pro
        const val URL_GET_SUPPORT_ACCOUNT = "wallet/getSupportAccount"

        //查询可支持划转的币种
        const val URL_GET_SUPPORT_COIN = "wallet/getSupportCoin"

        //划转
        const val URL_TRANSFER = "wallet/transfer"

        //划转记录
        const val URL_WALLET_TRANSFER_RECORD = "wallet/transfer/list"

        //综合账单查询
        const val URL_WALLET_BILL_FIEX = "balance/bills"

        /***fiex***/

        //现货资产 钱包
        const val URL_WALLET = "wallet"


        ///现货资产提币 资产币信息
//        public final static String URL_WITHDRAW = "config/withdraw";
        const val URL_WITHDRAW = "config/withdraw/v2"

        ///现货资产提币记录
        const val URL_WITHDRAW_QUERY = "withdraw/query"

        ///现货资产冲币记录
        const val URL_DEPOSIT = "deposit/query"

        ///现货资产冲币、提币记录
        const val URL_WALLET_RECORD = "deposit/withdraw/query"

        ///现货资产提币s 申请
        const val URL_WITHDRAW_CREATE = "withdraw/create"

        ///现货资产提币 撤销
        const val URL_WITHDRAW_CANCEL = "withdraw/cancel"

        ///冲币地址
        const val URL_RECHARGE_ADDRESS_POST = "wallet/address"

        //综合账单类型配置
        const val URL_WALLET_BILL_TYPE = "bill/conf/all"

        //综合账单查询
        const val URL_WALLET_BILL = "user/bill"

        //查询所有财务账单
        const val URL_FINANCE_LIST = "transaction/list/v2"

        //获取币种下链名称列表
        const val URL_LIAN_IN_COIN = "u/address/v2"

        //地址列表
        const val URL_ADDRESS_LIST = "withdraw/address/list"

        //添加地址
        const val URL_ADDRESS_ADD = "withdraw/address/add"

        //更改地址
        const val URL_ADDRESS_UPDATE = "withdraw/address/update"

        //删除地址
        const val URL_ADDRESS_DELETE = "withdraw/address/remove"

        //划转
        const val URL_WALLET_TRANSFER = "lever/transfer"

        //杠杆钱包详情
        const val URL_WALLET_LEVER_DETAIL = "lever/assets/detail"

        //杠杆借币/还币
        const val URL_LEVER_BORROW = "lever/borrow"

        //杠杆借币/还币记录
        const val URL_LEVER_BORROW_RECORD = "lever/borrow/record"
    }

    object Config {
        //获取网络线路
        const val URL_NETWORK_LINES = "app/line/url/findAll"

        //线路测速
        const val RUL_LINE_SPEED = "app/line/url/speed"

        //K线
        const val URL_KLINE_HISTORY = "public/kline"

        //fiex 获取所有交易对配置
        const val URL_HOME_CONFIG_LIST = "v1/public/symbol/list"

        //fiex 获取单个交易对配置信息
        const val URL_PAIR_SYMBOL_CONFIG = "v1/public/symbol/detail"

        //fiex 获取tickets
        const val URL_HOME_TICKERS = "public/tickers"

        //fiex 交易对行情
        const val URL_SYMBOL_TICKER = "public/ticker"

        //fiex 获取首首页折线图
        const val URL_HOME_KLine = "public/tickers-Kline"

        //{"code":0,"msg":"success","data":[{"coinType":"USDT","name":"USDT","hot":true,"type":1,"open":true},{"coinType":"ETH","name":"ETH","hot":false,"type":2,"open":true}]}
        //pro
        //所有币种列表
        const val URL_SET_LIST = "config/set/list"

        //国家区码列表
        const val URL_COUNTRY_CODE_LIST = "country/list"

        //公告 /{language}/page/{pageNum}/{pageSize} 3个参数分别是语言，页数，每页显示多少条 1-中，2-日，3-韩，4-英，5-俄
        const val URL_NOTICE = "announcement/search/{language}/page/{pageNum}/{pageSize}"

        //bannerList
//        public final static String URL_BANNER_LIST = "commons/bannerList";
        const val URL_BANNER_LIST = "commons/banner/list"

        //获取有序的交易对信息
        const val URL_ORDERED_PAIRS = "config/trade_pairs"

        //查询收藏交易对
        const val URL_DEAR_PAIRS = "user/pair/collection/list"

        //查询交易区
        const val URL_PAIR_SET = "config/pairs/markets"

        //热门币种
        const val URL_HOT_PAIRS = "config/pairs/hot"

        //交易对价格深度 获取交易对信息，深度 ?pair=BKK_USDT,不传拉取所有
        const val URL_PAIRS_DEEPS = "config/pairs"

        //收藏交易对
        const val URL_PAIR_COLLECT = "user/pair/collection/add"

        //取消收藏交易对
        const val URL_PAIR_COLLECT_CANCEL = "user/pair/collection/remove"

        //查询收藏交易对
        const val URL_SEARCH_COLLECT_PAIRS = "user/pair/collection/list"

        //快讯
        const val URL_NEWS = "news/list"

        //社区list
        const val URL_FORUM_LIST = "community/account/list"

        //币种简介
        const val URL_PAIR_DESCRIPTION = "coin/introduction/detail"

        //公告首頁
        //https://fbsexco.zendesk.com/api/v2/help_center/zh-cn/categories/360002181134/articles.json?page=1&per_page=3
        const val URL_NOTICE_HOME =
            "https://support.fiex.io/api/v2/help_center/{language}/categories/5324590333455/articles"

        //弹出广告
        const val URL_GLOBAL_AD = "notice/currentPhone"

        ///币种顺序
        const val URL_COIN_ORDERS_LIST = "config/currency/type"

        ///USDT折算价格 pro
        const val URL_USDT_CNY_PRICE = "config/price"

        ///GEETEST 初始化参数
        const val URL_GEETEST_INIT = "gt/init"

        //专属海报
        const val URL_MY_POSTER = "poster/list"

        //邀请地址
        const val URL_INVITE_URL = "poster/invited/url"

        //检查更新
        const val URL_UPDATE = "commons/app/version/check"

    }

    //合约相关
    object Future {

        const val URL_DEPTH = "market/v1/public/q/depth";

        //交易对配置列表
        const val URL_SYMBOL_LIST = "market/v1/public/symbol/list"

        //调整合约倍数
        const val URL_ADJUST_LEVERAGE = "user/v1/position/adjust-leverage"

        //一键全部平仓
        const val URL_CLOSE_ALL = "user/v1/position/close-all"

        //限价委托和市价委托撤销
        const val URL_CANCEL_ALL = "trade/v1/order/cancel-all"

        //撤销所有止盈止损
        const val URL_CANCEL_ALL_PROFIT_STOP = "trade/v1/entrust/cancel-all-profit-stop"

        //根据id撤销止盈止损
        const val URL_CANCEL_PROFIT_STOP_BY_ID = "trade/v1/entrust/cancel-profit-stop"

        //撤销所有计划委托
        const val URL_CANCEL_ALL_PLAN = "trade/v1/entrust/cancel-all-plan"

        //根据id撤销计划委托
        const val URL_CANCEL_PLAN_BY_ID = "trade/v1/entrust/cancel-plan"
        //获取所有交易对的标记价格
        const val URL_MARK_PRICE = "market/v1/public/q/mark-price"

        const val URL_SYMBOL_MARK_PRICE = "market/v1/public/q/symbol-mark-price"

        const val URL_SYMBOL_INDEX_PRICE = "market/v1/public/q/symbol-index-price"

        const val ULR_FUNDING_RATE = "market/v1/public/q/funding-rate"

        const val URL_COIN_LIST = "market/v1/public/symbol/coins"

        const val URL_DEAL_LIST = "market/v1/public/q/deal"//实时成交

        const val URL_AGG_TICKER = "public/q/agg-ticker"//聚合行情

        const val URL_ACCOUNT_INFO = "user/v1/account/info"

        const val URL_LOGIN = "user/v1/user/login"

        const val URL_POSITION_ADL = "user/v1/position/adl"

        const val URL_POSITION_LIST = "user/v1/position/list"//查询持仓信息

        const val URL_PROFIT_LIST = "trade/v1/entrust/profit-list"//查询止盈止损

        const val URL_PLAN_LIST = "trade/v1/entrust/plan-list"//查询计划委托

        const val URL_LEVERAGE_BRACKET_LIST = "user/v1/leverage/bracket/list"

        const val URL_LEVERAGE_BRACKET_DETAIL = "user/v1/leverage/bracket/detail"

        const val URL_OPEN_ACCOUNT = "user/v1/account/open"

        const val URL_TICKERS = "market/v1/public/q/tickers"

        const val URL_SYMBOL_TICKER = "market/v1/public/q/ticker"

        const val ULR_BALANCE_DETAIL = "user/v1/balance/detail"

        const val ULR_BALANCE_LIST = "user/v1/balance/list"

        const val URL_BALANCE_BILLS = "user/v1/balance/bills"

        const val ULR_ORDER_CREATE = "trade/v1/order/create"

        const val ULR_USER_STEP_RATE = "user/v1/user/step-rate/getUserStepRate"

        const val ULR_ORDER_LIST = "trade/v1/order/list"

        const val URL_AUTO_MARGIN = "user/v1/position/auto-margin"

        const val URL_LISTEN_KEY = "user/v1/user/listen-key"

        const val URL_LIST_HISTORY = "trade/v1/order/list-history"

        const val URL_FUNDING_RATE_LIST = "user/v1/balance/funding-rate-list"

    }


    object Money {
        //发售列表
        const val URL_PROMOTIONS_LIST = "foundation/list"

        //抢购币
//        public final static String URL_PROMOTIONS_ADD = "foundation/addFoundationOrder";
        const val URL_PROMOTIONS_ADD = "foundation/app/join"

        //抢购 申购记录?pageNum=1&pageSize=5
        const val URL_PROMOTIONS_RECORD = "foundation/user/order/list"

        //申购列表
        const val URL_PROMOTIONS_BUY = "purchase/list"

        //申购
        const val URL_PROMOTIONS_BUY_CREATE = "purchase/join"

        //申购项目详情
        const val URL_PROMOTIONS_BUY_DETAIL = "purchase/detail"

        //申购用户信息
        const val URL_PROMOTIONS_BUY_USER_INFO = "purchase/user/info"

        //申购记录
        const val URL_PROMOTIONS_BUY_RECORD = "purchase/user/order/list"

        //5折申购列表
        const val URL_PROMOTIONS_BUY_FIVE = "purchase/half-off/list"

        //5折申购
        const val URL_PROMOTIONS_BUY_FIVE_CREATE = "purchase/half-off/join"

        //5折申购项目详情
        const val URL_PROMOTIONS_BUY_FIVE_DETAIL = "purchase/detail"

        //5折申购记录
        const val URL_PROMOTIONS_BUY_FIVE_RECORD = "purchase/half-off/user/order/list"

        //聚宝盆币种配置  活
        const val URL_DEMAND_CONFIG = "current/confs"

        //聚宝盆转入  活
        const val URL_DEMAND_CHANGE_IN = "current/in"

        //聚宝盆奖励记录  活
        const val URL_DEMAND_REWARD_RECORD = "current/interest"

        //聚宝盆锁仓记录  活
        const val URL_DEMAND_LOCK_RECORD = "current/lock/records"

        //聚宝盆转出  活
        const val URL_DEMAND_CHANGE_OUT = "current/out"

        //聚宝盆批量转出  活
        const val URL_DEMAND_CHANGE_OUT_BATCH = "current/out/batch"

        //聚宝盆币种配置  定
        const val URL_REGULAR_CONFIG = "pledge/conf"

        //聚宝盆锁仓记录  定
        const val URL_REGULAR_LOCK_RECORD = "pledge/lock/record"

        //聚宝盆锁仓历史记录  定
        const val URL_REGULAR_LOCK_HISTORY = "pledge/lock/history"

        //聚宝盆转入  定
        const val URL_REGULAR_CHANGE_IN = "pledge/add"

        //聚宝盆转出  定 违约
        const val URL_REGULAR_CHANGE_OUT = "pledge/unlock"

        //抵押借贷配置
        const val URL_LOAN_CONFIG = "borrowing/check/infos"

        //抵押借贷创建
        const val URL_LOAN_CREATE = "borrowing/order/add"

        //抵押借贷记录
        const val URL_LOAN_RECORD = "borrowing/order/list"

        //抵押借贷追加保证金
        const val URL_LOAN_ADD_DEPOSIT = "borrowing/append"

        //抵押借贷还贷
        const val URL_LOAN_BACK = "borrowing/repay"

        //聚宝盆首页配置
        const val URL_MONEY_HOME = "cornucopia/infos"

        //抵押借贷追加保证金记录
        const val URL_LOAN_ADD_DEPOSIT_RECORD = "borrowing/bond/list"

        //抵押借贷订单详情
        const val URL_LOAN_RECORD_DETAIL = "borrowing/order/detail"

        //云算力配置
        const val URL_CLOUD_POWER_CONFIG = "mining/confs"

        //云算力购买
        const val URL_CLOUD_POWER_BUY = "mining/buy"

        //云算力持仓
        const val URL_CLOUD_POWER_HOLD_RECORD = "mining/list"

        //云算力购买历史
        const val URL_CLOUD_POWER_BUY_RECORD = "mining/order/history"

        //云算力收益
        const val URL_CLOUD_POWER_REWARD_RECORD = "mining/interest/history"

        //云算力BTC理论收益
        const val URL_CLOUD_POWER_BTC_INCOME = "mining/btc/income"

        //云算力个人收益和持仓
        const val URL_CLOUD_POWER_SUMMARY = "mining/account"
    }

    object Community {
        //笑傲江湖门派列表
        const val URL_FACTION_LIST = "league/list"

        //笑傲江湖门派详情
        const val URL_FACTION_DETAIL = "league/detail"

        //笑傲江湖门派公告
        const val URL_FACTION_NOTICE = "commons/foot/detail"

        //笑傲江湖门派成员列表
        const val URL_FACTION_MEMBER_LIST = "league/member/list"

        //笑傲江湖个人信息
        const val URL_FACTION_USER_INFO = "league/user/info"

        //笑傲江湖存入控制
        const val URL_FACTION_CONFIG = "league/config"

        //笑傲江湖加入
        const val URL_FACTION_LOCK = "league/user/lock"

        //笑傲江湖退出
        const val URL_FACTION_UNLOCK = "league/user/unlock"

        //笑傲江湖竞选门主
        const val URL_FACTION_BECOME = "league/user/owner/become"

        //笑傲江湖继任门主
        const val URL_FACTION_KEEP = "league/user/owner/keep"

        //发红包
        const val URL_RED_PACKET_SEND = "red-packet/send"

        //红包详情
        const val URL_RED_PACKET_DETAIL = "red-packet/{id}/detail"

        //红包汇总信息
        const val URL_RED_PACKET_SUMMARY = "red-packet/{id}"

        //抢红包
        const val URL_RED_PACKET_OPEN = "red-packet/{id}/grab"
    }
}