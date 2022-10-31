package com.black.base.util

import android.content.Context
import com.black.util.CommonUtil

object UrlConfig {
    val HOSTS = arrayOf(
            "http://fiex.matchain.info",//fiex测试环境
            "https://fiex.io",//正式环境

    )

    fun getHost(context: Context): String {
        return HOSTS[getIndex(context)]
    }

    fun getFiexHost(context: Context,apiType:String?): String {
        var apiTypeDes = "/uc/"
        when(apiType){
            ApiType.URl_UC ->apiTypeDes = "/uc/"
            ApiType.URL_API -> apiTypeDes = "/api/"
            ApiType.URL_PRO -> apiTypeDes = "/pro/"
        }
        return HOSTS[getIndex(context)]+apiTypeDes
    }


    private val SOCKET_HOSTS_FIEX = arrayOf(
        "ws://fiex.matchain.info/socket",//测试环境
        "wss://fiex.io/socket",//正式环境
    )

    fun getIndex(context: Context): Int {
        if (!CommonUtil.isApkInDebug(context)) {
            return 0
        }
        val index = CookieUtil.getHostIndex(context)
        return if (index < 0 || index > HOSTS.size) 0 else index
    }

    fun getSocketHostFiex(context: Context): String {
        return SOCKET_HOSTS_FIEX[getIndex(context)]
    }
    //币种图标
    fun getCoinIconUrl(context: Context,pairName:String?):String{
        return getHost(context) +pairName
    }
    /***fiex***/


    fun getSocketHost(context: Context): String {
        return HOSTS[getIndex(context)]
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

    object ApiType{
        const val URl_UC = "uc"
        const val URL_API = "api"
        const val URL_PRO = "pro"
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
        const val URL_BALANCE_LIST = "balance/list"
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
        //K线
        const val URL_KLINE_HISTORY="public/kline"
        //fiex 获取所有交易对配置
        const val URL_HOME_CONFIG_LIST = "v1/public/symbol/list"
        //fiex 获取单个交易对配置信息
        const val URL_PAIR_SYMBOL_CONFIG = "v1/public/symbol/detail"
        //fiex 获取tickets
        const val URL_HOME_TICKERS = "public/tickers"
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
        const val URL_NOTICE_HOME = "https://support.fiex.io/api/v2/help_center/{language}/categories/5324590333455/articles"
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