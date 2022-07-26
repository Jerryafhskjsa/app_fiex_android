package com.black.net;

public class HttpRequestResult {
    public final static int ERROR_TOKEN_INVALID_CODE = 403;
    public final static int ERROR_MISS_MONEY_PASSWORD_CODE = -20001;
    public static final int SUCCESS = 0;//成功
    public static final int NOTWORK_ERROR = 1;//网络异常
    public static final int JSON_ERROR = 2;//json解析异常
    public static final int IO_ERROR = 4;//读写异常
    public static final int OTHER_ERROR = 8;//其他异常
    public final static int ERROR_UNKNOWN = 16;
    public final static int ERROR_NORMAL = 32;
    public final static int ERROR_TOKEN_INVALID = 64;
    public final static int ERROR_MISS_MONEY_PASSWORD = 128;
}
