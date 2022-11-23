package com.black.base.util

import android.R
import com.black.net.HttpRequestResult

object ConstData {

    //资金类型
    enum class BalanceType{
        SPOT,
        CONTRACT
    }


    const val DEPTH_SPOT_TYPE = 0
    const val DEPTH_CONTRACT_U_TYPE = 1
    const val DEPTH_CONTRACT_COIN_TYPE = 2

    const val CONTRACT_REC_HOLD_AMOUNT = 0
    const val CONTRACT_REC_WITH_LIMIE = 1
    const val CONTRACT_REC_CURRENT = 2

    const val USER_VERIFY_NO = "0"
    const val USER_VERIFY_ED = "1"
    const val USER_VERIFY_ING = "2"
    const val USER_VERIFY_FAIL = "3"

    const val TAB_HOME = 0
    const val TAB_QUOTATION = 1
    const val TAB_TRANSATION = 2
    const val TAB_CONTRACT = 3
    const val TAB_ASSET = 4
    //交易对默认精度
    const val DEFAULT_PRECISION = 6

    const val DEFAULT_PSW_LEN = 8
    const val FRYING_PASSWORD = "frying" //不可修改

    const val IMAGE_MAX_SIZE = 4 * 1024 * 1024 //4M
            .toLong()
    const val ACTION_SOCKET_DATA_CHANGED = "com.bioko.exchange.order.data.changed"
    const val SD_DISK_PATH = "data/data/com.fbsex.exchange/download"
    const val CACHE_PATH = "data/data/com.fbsex.exchange/fbsex/cache"

    const val TEMP_IMG_NAME = "tmp_image.png"
    const val TEMP_IMG_NAME_01 = "tmp_image_01.png"
    const val TEMP_IMG_NAME_02 = "tmp_image_02.png"
    const val TEMP_IMG_NAME_03 = "tmp_image_03.png"

    const val REQUEST_STORAGE_PR = 100 //sd卡权限请求
    const val REQUEST_CAMERA_PR = 200 //摄像头权限请求
    const val REQUEST_CALL_PR = 300 //打电话权限请求
    const val REQUEST_MICROPHONE_PR = 400 //录音（麦克风）权限请求

    //边缘空白
    const val ROUND_BLANK = 5f
    //上下文字区域高度
    const val TOP_TEXT_HEIGHT = 10f
    const val BOTTOM_TEXT_HEIGHT = 10f
    //文字间距
    const val TEXT_SPACING = 10f
    //文字间距
    const val POPPER_TEXT_PADDING = 4f
    //弹出窗口选中圆点尺寸
    const val POPPER_POINT_INNER_RADIUS = 2f
    const val POPPER_POINT_OUTER_RADIUS = 12f
    //副图MACD柱宽度
    const val MACD_COLUMN_WIDTH = 1f

    const val ONE_SECOND_MILLIS = 1000
    const val GET_CODE_LOCK_TIME = 60
    const val APP_STAY_BACKGROUND_TIME = 60

    //认证类型code
    const val AUTHENTICATE_CODE_MAIL = -10021
    const val AUTHENTICATE_CODE_PHONE = -10022
    const val AUTHENTICATE_CODE_GOOGLE = -10023
    const val AUTHENTICATE_CODE_GOOGLE_OR_PHONE = -10024


    //认证类型
    const val AUTHENTICATE_TYPE_NONE = 0
    const val AUTHENTICATE_TYPE_PHONE = 1
    const val AUTHENTICATE_TYPE_MAIL = 2
    const val AUTHENTICATE_TYPE_GOOGLE = 4
    const val AUTHENTICATE_TYPE_PASSWORD = 8
    const val GESTURE_PASSWORD_MAX_COUNT = 5

    //极验认证ID
    const val CAPTCHA_ID = "ab5c8dd0b99f4aa2b251e3eb3f9b6d8c"
    //    public final static String CAPTCHA_ID = "f59f7455a53748468c6b8ba402a775d3";

    //微信APP_ID
//    public final static String WECHAT_APP_ID = "wx9fcd3b33b8166d7b";
//    public final static String WECHAT_APP_SECRET = "fbe87e6105c4d0255697266d87159a89";
//    public final static String WECHAT_APP_ID = "wx1b056f8d81a1f979";
//    public final static String WECHAT_APP_SECRET = "de28d97a2882874e92392232224a0628";
    const val WECHAT_APP_ID = "wx341c8923d1e41c65"
    const val WECHAT_APP_SECRET = "b31b69176818287db066e9474c3b59a7"

    //在线客服配置
//    public final static String UDESK_DOMAIN = "fbskj.udesk.cn";
//    public final static String UDESK_APP_ID = "68481f4ed9a6b7ca";
//    public final static String UDESK_APP_KEY = "7ddafed9570e2be075507d801df7ff2a";
    const val UDESK_DOMAIN = "huaningkej.s2.udesk.cn"
    const val UDESK_APP_ID = "d11c751b12a5fe11"
    const val UDESK_APP_KEY = "158ebef635d7be79182f96fb75246c8b"
    //腾讯im
    const val TENCENT_APP_ID = 1400274389
    //push key
    const val PUSH_APP_ID = "3fade9a117e24"
    const val PUSH_SECRET_KEY = "794a5ad59dfffdbf10af5fb48b5f0b3a"
    const val PUSH_ACCESS_ID = "2100346023"
    const val PUSH_ACCESS_KEY = "A3P777DZU1IJ"
    /*----------Pref KEys-------*/
    const val IS_NEW_APP = "is_new_app"
    const val TOKEN = "token"
    const val TYPE = "type"
    const val VERIFY_CODE = "verify_code"
    const val PHONE_CAPTCHA = "phone_captcha"
    const val MAIL_CAPTCHA = "mail_captcha"
    const val GOOGLE_CODE = "google_code"
    const val USER_INFO = "user_info"
    const val USER_NAME = "user_name"
    const val USER_ID = "user_id"
    const val ACCOUNT_PROTECT_TYPE = "account_protect_type"
    const val ACCOUNT_PROTECT_JUMP = "account_protect_jump"
    const val GESTURE_PASSWORD = "gesture_password"
    const val GESTURE_PASSWORD_FAILED_COUNT = "gesture_password_failed_count"
    const val CURRENT_PAIR = "current_pair"
    const val CURRENT_FUTURE_U_PAIR = "current_future_u_pair"
    const val CURRENT_PAIR_LEVER = "current_pair_lever"
    const val REQUEST_CAMERA = "request_camera"
    const val REQUEST_STORAGE = "request_storage"
    const val MAIN_EYE = "main_eye"
    const val SHOW_TRADE_WARNING = "show_trade_warning"
    const val WALLET_COIN_FILTER = "wallet_coin_filter"
    const val UPDATE_PAIRS = "update_pairs"
    const val UPDATE_VERSION = "update_version"
    const val NIGHT_MODE = "night_mode"
    const val CELL_PRICE = "cell_price"
    const val BUY_PRICE = "buy_price"
    const val HOST_INDEX = "host_index"
    const val HOST_DATA = "host_data"
    const val PAIR_SEARCH_HISTORY = "pair_search_history"
    const val COIN_SEARCH_HISTORY = "coin_search_history"
    const val LEVEL_TYPE = "level_type"
    /*----------Pref KEys-------*/

    /*----------intent KEys-------*/
    const val NAME = "payeeName"
    const val TITLE = "title"
    const val COUNTRY_CODE = "country_code"
    const val ACCOUNT = "account"
    const val COIN_TYPE = "coin_type"
    const val WALLET = "wallet"
    const val WALLET_BILL = "wallet_bill"
    const val COIN_WALLET = "coin_wallet"
    const val WALLET_INFO = "wallet_info"
    const val WALLET_MEMO_NEEDED = "wallet_memo_needed"
    const val PAIR = "pair"
    const val SOCKET_DATA_CHANGED_TYPE = "socket_data_changed_type"
    const val OPEN_TYPE = "open_type"
    const val C2C_ORDER_NO = "c2c_order_no"
    const val C2C_ORDER_INFO = "c2c_order_info"
    const val GESTURE_PASSWORD_CHECK_RESULT = "gesture_password_check_result"
    const val FINGER_PRINT_CHECK_RESULT = "finger_print_check_result"
    const val HOME_FRAGMENT_INDEX = "home_fragment_index"
    const val TRANSACTION_INDEX = "transaction_index"
    const val TRANSACTION_TYPE = "transaction_type"
    const val MAIN_INDEX = "main_index"
    const val URL = "url"
    const val IMAGE_LOCAL_PATH = "image_path"
    const val CONTENT = "content"
    const val TOKEN_INVALID = "token_invalid"
    const val IS_SUCCESS = "is_success"
    const val C2C_ORDER = "c2c_order"
    const val C2C_ORDER_ID = "c2c_order_id"
    const val C2C_ORDER_DATA = "c2c_order_data"
    const val C2C_DIRECTION = "c2c_direction"
    const val C2C_SUPPORT_COIN = "c2c_support_coin"
    const val C2C_SUPPORT_COINS = "c2c_support_coins"
    const val HOME_PAGE_INDEX = "home_page_index"
    const val ACTION_BAR_LAYOUT_ID = "action_bar_layout_id"
    const val FOR_RESULT = "for_result"
    const val WALLET_HANDLE_TYPE = "wallet_handle_type"
    const val WALLET_LIST = "wallet_list"
    const val BALANCE_LIST = "user_balance_list"
    const val WALLET_WITHDRAW_ADDRESS = "wallet_withdraw_address"
    const val COUNTRY = "country"
    const val REGULAR = "regular"
    const val NEXT_ACTION = "next_action"
    const val CHECK_UN_BACK = "check_un_back"
    const val REGULAR_SUPPORT = "regular_support"
    const val NEWS = "news"
    const val ADD_USER = "add_user"
    const val IDENTITY_NO = "identity_no"
    const val BIRTH = "birth"
    const val TRADE_ORDER = "trade_order"
    const val AMOUNT_PRECISION = "amount_precision"
    const val COIN_INFO = "coin_info"
    const val COIN_CHAIN = "coin_chain"
    const val COIN_ADDRESS = "coin_address"
    const val PROMOTIONS_BUY = "promotions_buy"
    const val PROMOTIONS_BUY_FIVE = "promotions_buy_five"
    const val FACTION_ITEM = "faction_item"
    const val FACTION_ID = "faction_id"
    const val FACTION_USER_INFO = "faction_user_info"
    const val DEMAND = "demand"
    const val IM_GROUP_ID = "im_group_id"
    const val IM_GROUP_NAME = "im_group_name"
    const val IM_CHAT_ROOM_ENABLE = "im_chat_room_enable"
    const val IM_USER_ID = "im_user_id"
    const val IM_GROUP_MEMBER_LIST = "im_group_member_list"
    const val PAYMENT_METHOD_TYPE = "payment_method_type"
    const val FACTION_MEMBER_PROFILE = "faction_member_profile"
    const val FACTION_MEMBER_PROFILE_DEFAULT = "faction_member_profile_default"
    const val MONEY_PASSWORD_TYPE = "money_password_type"
    const val DEMAND_COINS = "demand_coins"
    const val REGULAR_COINS = "regular_coins"
    const val MONEY_RECORD_TYPE = "money_record_type"
    const val DEMAND_STATUS = "demand_status"
    const val REGULAR_STATUS = "regular_status"
    const val RED_PACKET = "red_packet"
    const val RED_PACKET_ID = "packetId"
    const val RED_PACKET_TEXT = "packetText"
    const val LOAN_CONFIG_LIST = "loan_config_list"
    const val LOAN_RECORD = "loan_record"
    const val SUPPORT_COIN_LIST = "support_coin_list"
    /*----------intent KEys-------*/

    /*----------intent KEys ROUTER-------*/
    const val ROUTER_COIN_TYPE = "cointype"
    const val ROUTER_PAIR = "pair"
    /*----------intent KEys ROUTER-------*/

    /*-------Socket s数据变化类型 -------*/
    const val UNKNOWN = 0 //未知
    @Deprecated("")
    val USER_ORDER_DEAL = 1 //用户订单成交
    @Deprecated("")
    val USER_ORDER_REFRESH = 6 //用户订单交易情况刷新
    @Deprecated("")
    val USER_MINING_REFRESH = 7 //交易数据刷新
    const val USER_PAIR_ALL_REFRESH = 8 //交易对个人相关数据刷新
    const val ALL_PAIR_STATUS_REFRESH = 2 //所有交易对刷新
    const val CURRENT_PAIR_STATUS_REFRESH = 3 //当前交易对刷新
    const val QUOTATION_ORDER_REFRESH = 4 //行情订单数据时刷新
    const val QUOTATION_DEAL_REFRESH = 5 //行情订单成交数据时刷新
    const val KLINE_ADD = 6 //K线数据增量
    const val KLINE_ALL = 7 //K线数据全量
    const val USER_INFO_CHANGED = 9 //用户数据有更新
    /*-------Socket s数据变化类型 -------*/

    /*----------activity for result KEys-------*/
    const val TAKE_PICTURE = 1
    const val CROP_PICTURE = 3
    const val CHOOSE_PICTURE = 2
    const val CHOOSE_COUNTRY_CODE = 101
    const val CHOOSE_COIN = 102
    const val SCANNIN_GREQUEST_CODE = 103
    const val WALLET_ADDRESS_ADD = 104
    const val WALLET_ADDRESS_MANAGE = 105
    const val REAL_NAME_AUTHENTICATE = 106
    const val GESTURE_PASSWORD_CHECK = 107
    const val CHOOSE_COIN_RECHARGE = 108
    const val CHOOSE_COIN_WITHDRAW = 109
    const val GOOGLE_BIND = 110
    const val GESTURE_PASSWORD_SETTING = 111
    const val FINGER_PRINT_CHECK = 112
    const val FINGER_PRINT_SETTING = 113
    const val CREATE_RED_PACKET = 114
    const val LOAN_ADD_DEPOSIT = 115
    const val LOAN_BACK = 116
    const val LEVER_PAIR_CHOOSE = 117
    /*----------activity KEys-------*/

    const val DEFAULT_TIME_OUT = 15000
    const val HTTP_GET = "GET"
    const val HTTP_POST = "POST"
    const val HTTP_DELETE = "DELETE"

    const val ERROR_UNKNOWN = HttpRequestResult.ERROR_UNKNOWN
    const val ERROR_NORMAL = HttpRequestResult.ERROR_NORMAL
    const val ERROR_TOKEN_INVALID = HttpRequestResult.ERROR_TOKEN_INVALID
    const val ERROR_MISS_MONEY_PASSWORD = HttpRequestResult.ERROR_MISS_MONEY_PASSWORD

    const val SOCKET_HANDLER = "socket_handler"
    const val HTTP_HANDLER = "http_handler"
    const val OTHER_HANDLER = "other_handler"

    val SIDE_BAR_TITLE = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")

    const val TAB_COIN = 4 // 币币交易
    const val TAB_LEVER = 8 //杠杆交易

    const val TAB_EXCHANGE = 0
    const val TAB_WITHDRAW = 1

    const val TAB_DEMAND = 1
    const val TAB_REGULAR = 2

    val STATE_DEFAULT = intArrayOf()
    val STATE_CHECKED = intArrayOf(R.attr.state_checked)
    val STATE_DISABLED = intArrayOf(-R.attr.state_enabled)

    const val ACCOUNT_PROTECT_NONE = 0
    const val ACCOUNT_PROTECT_GESTURE = 1
    const val ACCOUNT_PROTECT_FINGER = 2

    const val MONEY_PASSWORD_SET = 0 //设置资金密码
    const val MONEY_PASSWORDR_RESET = 1 //重置资金密码

    const val HOME_TAB_HOT = 0
    const val HOME_TAB_RAISE_BAND = 1
    const val HOME_TAB_FAIL_BAND = 2
    const val HOME_TAB_VOLUME_BAND = 3

    const val ASSET_SUPPORT_ACCOUNT_TYPE = "asset_support_account_type"
    const val ASSET_SUPPORT_SPOT_ACCOUNT_TYPE = "asset_support_spot_account_type"
    const val ASSET_SUPPORT_OTHER_ACCOUNT_TYPE = "asset_support_other_account_type"
}