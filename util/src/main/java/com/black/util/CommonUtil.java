package com.black.util;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.FingerprintManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.PermissionChecker;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class CommonUtil {
    public static final SimpleDateFormat SDF_YMD = new SimpleDateFormat("yyyy/MM/dd");
    public static final SimpleDateFormat SDF_YMD_HMS = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    public static final SimpleDateFormat SDF_MD_HM = new SimpleDateFormat("MM/dd HH:mm");
    public static final SimpleDateFormat SDF_HM = new SimpleDateFormat("HH:mm");
    public static final Map<String, SimpleDateFormat> sdfMap = new HashMap<>();
    final static int BUFFER_SIZE = 4096;

    /**
     * 定义script的正则表达式
     */
    private static final String REGEX_SCRIPT = "<script[^>]*?>[\\s\\S]*?<\\/script>";
    /**
     * 定义style的正则表达式
     */
    private static final String REGEX_STYLE = "<style[^>]*?>[\\s\\S]*?<\\/style>";
    /**
     * 定义HTML标签的正则表达式
     */
    private static final String REGEX_HTML = "<[^>]+>";
    /**
     * 定义空格回车换行符
     */
    private static final String REGEX_SPACE = "\\s*|\t|\r|\n";

    /**
     * md5加密字符串
     *
     * @param s
     * @return
     */
    public static String MD5(String s) {
        String result = "";
        try {
            result = MD5(s.getBytes());
        } catch (Exception ignored) {
        }
        return result;
    }

    /*
     * MD5 加密字节
     */
    public static String MD5(byte[] buffer) {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(buffer);
            byte[] md = mdTemp.digest();
            int j = md.length;
            char[] str = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

    static {
        sdfMap.put("yyyy/MM/dd", SDF_YMD);
        sdfMap.put("yyyy/MM/dd HH:mm:ss", SDF_YMD_HMS);
        sdfMap.put("MM/dd HH:mm", SDF_MD_HM);
        sdfMap.put("HH:mm", SDF_HM);
    }

    public static SimpleDateFormat getSdf(String format) {
        SimpleDateFormat sdf = sdfMap.get(format);
        if (sdf == null) {
            sdf = new SimpleDateFormat(format);
            sdfMap.put(format, sdf);
        }
        return sdf;
    }

    public static String formatTimestamp(String format, long times) {
        return formatDate(format, new Date(times));
    }

    public static String formatDate(String format, Date date) {
        if (date == null) {
            return null;
        }
        return getSdf(format).format(date);
    }

    public static Date parseDate(String format, String dateString) {
        if (dateString == null || dateString.length() == 0) {
            return null;
        }
        try {
            return getSdf(format).parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String getDeviceId(Context context) {
//        String deviceId = null;
//        System.out.println("permission:" + ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE));
//        if (selfPermissionGranted(context, Manifest.permission.READ_PHONE_STATE)) {
//            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//            deviceId = tm.getDeviceId();
//        }
//        System.out.println("deviceId:" + deviceId);
//        return deviceId;
        String m_szDevIDShort = "35" + "/" +//we make this look like a valid IMEI
                Build.BOARD.length() % 10 + "/" +
                Build.BRAND.length() % 10 + "/" +
                Build.CPU_ABI.length() % 10 + "/" +
                Build.DEVICE.length() % 10 + "/" +
                Build.DISPLAY.length() % 10 + "/" +
                Build.HOST.length() % 10 + "/" +
                Build.ID.length() % 10 + "/" +
                Build.MANUFACTURER.length() % 10 + "/" +
                Build.MODEL.length() % 10 + "/" +
                Build.PRODUCT.length() % 10 + "/" +
                Build.TAGS.length() % 10 + "/" +
                Build.TYPE.length() % 10 + "/" +
                Build.USER.length() % 10 + "/"; //13 digits
        return MD5(m_szDevIDShort);
    }

    public static boolean selfPermissionGranted(Context context, String permission) {
        try {
            final PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            int targetSdkVersion = info.applicationInfo.targetSdkVersion;
            boolean ret = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (targetSdkVersion >= Build.VERSION_CODES.M) {
                    ret = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
                } else {
                    ret = PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED;
                }
            } else {
                return true;
            }
            return ret;
        } catch (PackageManager.NameNotFoundException e) {
            CommonUtil.printError(context, e);
        }
        return false;
    }

//    public static boolean selfPermissionGranted(Context context, String permission) {
//        return PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED;
//    }

    /**
     * 检查网络是否已连接
     *
     * @param context
     * @param networkType
     * @return
     */
    public static boolean isConnected(Context context, int networkType) {
        return isConnected((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE), networkType);
    }

    /**
     * 检查网络是否已连接
     *
     * @param connectivityManager
     * @param networkType
     * @return
     */
    public static boolean isConnected(ConnectivityManager connectivityManager, int networkType) {
        try {
            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(networkType);
            if (networkInfo == null) {
                return false;
            }
            return networkInfo.isConnected();
        } catch (Exception e) {

        }
        return false;
    }

    /**
     * 解析返回的内容为字符串
     *
     * @param object
     * @return
     * @throws IOException
     */
    public static String parseHttpResult(Object object) throws IOException, IOException {
        if (object == null) {
            return null;
        }
        if (object instanceof String) {
            return object.toString();
        } else if (object instanceof InputStream) {
            return streamToString((InputStream) object);
        }
        return null;
    }

    public static String streamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private static final int SERVER_PORT = 50030;//端口号
    private static final String SERVER_IP = "218.206.176.146";//连接IP
    private static final String CLIENT_KET_PASSWORD = "123456";//私钥密码
    private static final String CLIENT_TRUST_PASSWORD = "123456";//信任证书密码
    private static final String CLIENT_AGREEMENT = "TLS";//使用协议
    private static final String CLIENT_KEY_MANAGER = "X509";//密钥管理器
    private static final String CLIENT_TRUST_MANAGER = "X509";//
    private static final String CLIENT_KEY_KEYSTORE = "BKS";//密库，这里用的是BouncyCastle密库
    private static final String CLIENT_TRUST_KEYSTORE = "BKS";//
    private static final String ENCONDING = "utf-8";//字符集

    private static SSLContext sslContext;

    /**
     * 获取证书认证上下文
     *
     * @param context
     * @return
     */
    public static SSLContext getSSLContext(final Context context) {
        try {
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
//                public X509Certificate[] getAcceptedIssuers() {
//                    return new X509Certificate[0];
//                }
//
//                public void checkClientTrusted(X509Certificate[] certs, String authType) {
//                }
//
//                public void checkServerTrusted(X509Certificate[] certs, String authType) {
//                    X509Certificate certificate = certs == null ? null : certs.length == 0 ? null : certs[0];
//                    if(certificate == null || certificate.equals(getServerCertificate(context))){
//                        throw new RuntimeException("证书验证失败！");
//                    }
//                }
//            }};
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//            //取得KeyManagerFactory和TrustManagerFactory的X509密钥管理器实例
////            KeyManagerFactory keyManager = KeyManagerFactory.getInstance(CLIENT_KEY_MANAGER);
//            TrustManagerFactory trustManager = TrustManagerFactory.getInstance(CLIENT_TRUST_MANAGER);
//            //取得BKS密库实例
////            KeyStore kks= KeyStore.getInstance(CLIENT_KEY_KEYSTORE);
//            KeyStore tks = KeyStore.getInstance(CLIENT_TRUST_KEYSTORE);
//            //加客户端载证书和私钥,通过读取资源文件的方式读取密钥和信任证书
////            kks.load(getBaseContext()
////                    .getResources()
////                    .openRawResource(R.drawable.kclient),CLIENT_KET_PASSWORD.toCharArray());
//            tks.load(context.getResources().openRawResource(R.raw.scer), CLIENT_TRUST_PASSWORD.toCharArray());
//            //初始化密钥管理器
////            keyManager.init(kks,CLIENT_KET_PASSWORD.toCharArray());
//            trustManager.init(tks);
//            //初始化SSLContext
//            sslContext.init(null, trustManager.getTrustManagers(), null);
////            sslContext.init(null, trustAllCerts, null);

            if (sslContext != null) {
                return sslContext;
            } else {
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null);
                InputStream is = context.getAssets().open("server.cer");
                keyStore.setCertificateEntry("0", certificateFactory.generateCertificate(is));
                if (is != null) {
                    is.close();
                }
                sslContext = SSLContext.getInstance("TLS");

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

                trustManagerFactory.init(keyStore);
                sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            }


            return sslContext;
        } catch (Exception e) {
            CommonUtil.printError(context, e);
        }
        return null;
    }

    public static void md5Param(Map<String, String> params, String key) {
        if (params == null || TextUtils.isEmpty(key)) {
            return;
        }
        String value = params.get(key);
        if (!TextUtils.isEmpty(value)) {
            params.put(key, MD5(value));
        }
    }

    //是否需要签名认证
    public static boolean needSSL(String url) {
        return false;
    }


    /**
     * 创建二维码
     *
     * @param str
     * @param widthAndHeight
     * @param margin
     * @return
     * @throws WriterException
     */
    public static Bitmap createQRCode(String str, int widthAndHeight, int margin) throws WriterException {
        if (str == null || str.trim().length() == 0) {
            return null;
        }
        final int BLACK = 0xff000000;
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.MARGIN, margin);
        BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight, hints);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = BLACK;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public static Bitmap createQRCode(String str, int widthAndHeight, int margin, int color) throws WriterException {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.MARGIN, margin);
        BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight, hints);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = color;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    /**
     * 检查指纹状态
     *
     * @return 0 未知，1 正常，2 版本过低， 4 没有指纹API，8 没有指纹权限， 16 未设置界面开启密码锁屏功能 32 没有录入指纹
     */
    public static int getFingerPrintStatus(Context context) {
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (hasFingerprintApi()) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                    result = 8;
                } else {
                    KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                    // 如果没有设置密码锁屏，则不能使用指纹识别
                    if (keyguardManager == null || !keyguardManager.isKeyguardSecure()) {
                        result = 16;
                    } else {
                        FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
                        // 如果没有录入指纹，则不能使用指纹识别
                        if (fingerprintManager == null || !fingerprintManager.hasEnrolledFingerprints()) {
                            result = 32;
                        } else {
                            result = 1;
                        }
                    }
                }
            } else {
                result = 4;
            }
        } else {
            result = 2;
        }

        return result;
    }

    //检查是否具有指纹API
    private static boolean hasFingerprintApi() {
        try {
            Class cls = Class.forName("android.hardware.fingerprint.FingerprintManager"); // 通过反射判断是否存在该类
            return cls != null;
        } catch (ClassNotFoundException e) {
        }
        return false;
    }

    public static Integer parseInt(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        if (obj instanceof String) {
            return parseInt((String) obj);
        }
        return null;
    }

    public static Integer parseInt(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {

        }
        return null;
    }

    public static Double parseDouble(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            if (obj instanceof Number) {
                return ((Number) obj).doubleValue();
            }
            if (obj instanceof String) {
                return Double.parseDouble((String) obj);
            }
        } catch (Exception e) {

        }
        return null;
    }

    @Nullable
    public static Double parseDouble(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {

        }
        return null;
    }

    public static Long parseLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof String) {
            return parseLong((String) obj);
        }
        return null;
    }

    public static Long parseLong(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        try {
            return Long.parseLong(str);
        } catch (Exception e) {

        }
        return null;
    }

    public static BigDecimal parseBigDecimal(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return parseBigDecimal((String) obj);
        } else {
            parseBigDecimal(obj.toString());
        }
        return null;
    }

    public static BigDecimal parseBigDecimal(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        try {
            return new BigDecimal(str);
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * 格式化原始格式数字
     *
     * @param number 数字
     * @return
     */
    public static String formatSourceDouble(double number) {
        NumberFormat nf = NumberFormat.getInstance();
        // 是否以逗号隔开, 默认true以逗号隔开,如[123,456,789.128]
        nf.setGroupingUsed(false);
        // 结果未做任何处理
        return nf.format(number);
    }

    /**
     *
     */
    public static Double lossAccuracy(Double number, int scale) {
        if (number == null) {
            return null;
        }
        BigDecimal b = new BigDecimal(number);
        return b.setScale(scale, BigDecimal.ROUND_FLOOR).doubleValue();
    }

    /**
     * 格式化数字
     *
     * @param number 数字
     * @param scale  小数位数
     * @return
     */
    public static String formatDouble(Double number, int scale) {
        number = number == null ? 0d : number;
        BigDecimal b = new BigDecimal(number);
//        double result = b.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
//        return String.valueOf(result);
        return b.setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 获取list指定位置数据，越界返回null
     *
     * @param data
     * @param position
     * @param <T>
     * @return
     */
    @Nullable
    public static <T> T getItemFromList(List<T> data, int position) {
        if (data == null || position < 0 || data.size() < position + 1) {
            return null;
        }
        return data.get(position);
    }

    public static <T extends Findable> T findItemFromList(List<T> data, Object key) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        for (T item : data) {
            if (key == null && item.getFindKey() == null) {
                return item;
            }
            if (key != null && key.equals(item.getFindKey())) {
                return item;
            }
        }
        return null;
    }

    /**
     * 获取list指定位置数据，越界返回null
     *
     * @param data
     * @param position
     * @param <T>
     * @return
     */
    public static <T> T getItemFromArray(T[] data, int position) {
        if (data == null || position < 0 || data.length < position + 1) {
            return null;
        }
        return data[position];
    }

    /**
     * 格式化数量
     *
     * @param amount
     * @return
     */
    public static String formatAmount(double amount) {
        return formatAmount(amount, 2);
    }

    /**
     * 格式化数量
     *
     * @param amount
     * @return
     */
    public static String formatAmount(double amount, int scale) {
        if (amount <= 0) {
            return "0.00";
        } else if (amount < 1000) {
            return NumberUtil.formatNumberNoGroup(amount, 4);
        } else if (amount < 1000000) {
            return NumberUtil.formatNumberNoGroup(amount / 1000, scale) + "K";
        } else {
            return NumberUtil.formatNumberNoGroup(amount / 1000000, scale) + "M";
        }
    }

    /**
     * 格式化数量
     *
     * @param amount
     * @return
     */
    public static String formatAmount(double amount, int minScale, int maxScale) {
        if (amount <= 0) {
            return "0.00";
        } else if (amount < 1000) {
            return NumberUtil.formatNumberNoGroup(amount, minScale, maxScale);
        } else if (amount < 1000000) {
            return NumberUtil.formatNumberNoGroup(amount / 1000, 2) + "K";
        } else {
            return NumberUtil.formatNumberNoGroup(amount / 1000000, 2) + "M";
        }
    }

    /**
     * 复制文本
     *
     * @param context
     * @param text
     * @return
     */
    public static boolean copyText(Context context, CharSequence text) {
        try {
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText(null, text));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void pasteText(Context context, Callback<String> callback) {
        if (context == null || callback == null) {
            return;
        }
        try {
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData abc = cm.getPrimaryClip();
            ClipData.Item item = abc.getItemAt(0);
            String text = item == null ? null : item.getText().toString();
            callback.callback(text);
        } catch (Exception e) {
            callback.error(0, e);
        }
    }
//    public static int getTotalHeightOfListView(ListView listView) {
//        ListAdapter mAdapter = listView.getAdapter();
//        if (mAdapter == null) {
//            return 0;
//        }
//        int totalHeight = 0;
//        for (int i = 0; i < mAdapter.getCount(); i++) {
//            View mView = mAdapter.getView(i, null, listView);
//            mView.measure(
//                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
//                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//            //mView.measure(0, 0);
//            totalHeight += mView.getMeasuredHeight();
//        }
//        return totalHeight;
//    }

    public static int getTotalHeightOfListView(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return 0;
        }

        DisplayMetrics dm = listView.getContext().getResources().getDisplayMetrics();
//        int w_screen = dm.widthPixels;
//
//        float dp = (dm.xdpi / DisplayMetrics.DENSITY_DEFAULT);
//
//        int totalHeight = 0;
//        int listViewWidth = w_screen - dip2px(listView.getContext(), 16);                                         //listView在布局时的宽度
//        int widthSpec = View.MeasureSpec.makeMeasureSpec(listViewWidth, View.MeasureSpec.AT_MOST);
//        for (int i = 0; i < listAdapter.getCount(); i++) {
//            View listItem = listAdapter.getView(i, null, listView);
//            listItem.measure(widthSpec, 0);
//
//            int itemHeight = listItem.getMeasuredHeight();
//            totalHeight += itemHeight;
//        }
//        // 减掉底部分割线的高度
//        int historyHeight = totalHeight
//                + (listView.getDividerHeight() * listAdapter.getCount() - 1);
        int historyHeight = (int) (dm.density * listAdapter.getCount() * 80);
        return historyHeight;

    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static void measureView(View view) {
        measureView1(view);
        int width = view.getMeasuredWidth();
        int height = view.getMeasuredHeight();
        if (width == 0 && height == 0) {
            measureView2(view);
        }
    }

    public static void measureView1(View view) {
        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(width, height);
    }


    public static void resetViewHeight(final View view, final float scale) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                CommonUtil.measureView(view);
                int width = view.getMeasuredWidth();
                if (width == 0) {
                    CommonUtil.measureView2(view);
                    width = view.getMeasuredWidth();
                }
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = (int) (width * scale);
                view.setLayoutParams(params);
                view.postInvalidate();
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    public static void measureView2(View view) {
        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY);
        int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY);
        view.measure(width, height);
    }

    public static String secretPhoneNumber(String phone) {
        return phone == null ? "null" : phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 判断是否为邮箱
     *
     * @param emails
     * @return
     */
    public static boolean isEmailNO(String emails) {
        String reg = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(emails);
        return m.matches();
    }

    public static String getCatchFilePath(Context context) {
        return Environment.getExternalStorageDirectory().getPath() + "/fbsex";
    }

    public static String getCatchFileSavePath(Context context) {
        return Environment.getExternalStorageDirectory().getPath() + "/fbsex/dowload";
    }

    public static byte[] inputStreamTOByte(InputStream in) throws IOException {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[BUFFER_SIZE];
        int count = -1;
        while ((count = in.read(data, 0, BUFFER_SIZE)) != -1)
            outStream.write(data, 0, count);

        data = null;
        return outStream.toByteArray();
    }

    public static ByteArrayInputStream getByteArrayInputStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
        } else {
            return new ByteArrayInputStream(sb.toString().getBytes());
        }
    }


    /**
     * 获取版本描述
     *
     * @param context
     * @param defaultValue
     * @return
     */
    public static String getVersionName(Context context, String defaultValue) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return defaultValue;
        }
    }

    public static int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;

        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
        }

        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                // We only recognize a subset of orientation tag values.
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                    default:
                        break;
                }
            }
        }

        return degree;
    }

    public static <T extends Comparable> T getMax(ArrayList<T> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        T result = source.get(0);
        for (int i = 1; i < source.size(); i++) {
            T item = source.get(i);
            if (item.compareTo(result) > 0) {
                result = item;
            }
        }
        return result;
    }

    public static String formatMoneyCNY(Number number) {
        return NumberUtil.formatNumberNoGroupHardScale(number, 4);
    }

    /**
     * 判断service是否存活
     *
     * @param context
     * @param clas
     * @return
     */
    public static boolean isServiceWorked(Context context, Class clas) {
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().equals(clas.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为debug包
     *
     * @param context
     * @return
     */
    public static boolean isApkInDebug(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static void printFormat(String s, String se2) {
    }

    public static double scaleDouble(double number, int scale, int roundingMode) {
        BigDecimal bg = new BigDecimal(number);
        return bg.setScale(scale, roundingMode).doubleValue();
    }

    //获取一个单位的价格，根据深度计算
    public static double getNumberDeepUnit(Number number) {
        String numberString = NumberUtil.formatNumberNoGroup(number);
        int dotIndex = numberString.indexOf(".");
        if (dotIndex == -1) {
            return 1;
        } else {
            int length = numberString.length();
            return Math.pow(10, -(length - 1 - dotIndex));
        }
    }

    //计算小数位数
    public static int getDotLength(Number number) {
        String numberString = NumberUtil.formatNumberNoGroup(number);
        int dotIndex = numberString.indexOf(".");
        if (dotIndex == -1) {
            return 0;
        } else {
            int length = numberString.length();
            return length - 1 - dotIndex;
        }
    }

    //计算数字长度
    public static int getNumberStringLength(Number number) {
        String numberString = NumberUtil.formatNumberNoGroup(number);
        return numberString.length();
    }

    public static JSONArray parseJSONArray(Object object) {
        JSONArray data = null;
        if (object instanceof JSONArray) {
            data = (JSONArray) object;
        } else {
            try {
                data = new JSONArray(String.valueOf(object));
            } catch (JSONException e) {

            }
        }
        return data;
    }

    public static JSONObject parseJSONObject(Object object) {
        JSONObject data = null;
        if (object instanceof JSONObject) {
            data = (JSONObject) object;
        } else {
            try {
                data = new JSONObject(String.valueOf(object));
            } catch (JSONException e) {
            }
        }
        return data;
    }

    public static void removeListItem(ArrayList<?> list, int start, int end) {
        if (list == null || list.isEmpty() || end < 0 || start < 0 || end < start) {
            return;
        }
        int count = list.size();
        int realEnd = Math.min(count, end);
        for (int i = realEnd; i > start; i--) {
            list.remove(i);
        }
    }

    public static void postHandleTask(Handler handler, Runnable runnable) {
        if (handler != null && handler.getLooper() != null && runnable != null) {
            handler.post(runnable);
        }
    }

    public static void postHandleTask(Handler handler, Runnable runnable, long delayTime) {
        if (handler != null && runnable != null) {
            handler.postDelayed(runnable, delayTime);
        }
    }

    /**
     * @param position 0 LEFT, 1 TOP, 2 RIGHT, 3 BOTTOM
     */
    public static void setTextViewCompoundDrawable(TextView textView, Drawable drawable, @IntRange(from = 0, to = 3) int position) {
        if (textView == null || position < 0 || position > 3) {
            return;
        }

        Drawable[] drawables = textView.getCompoundDrawables();
        if (drawables == null) {
            drawables = new Drawable[]{null, null, null, null};
        }
        drawables[position] = drawable;
        textView.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3]);
//        Drawable[] drawables = countryView.getCompoundDrawables();
//        drawables[2] = SkinCompatResources.getDrawable(mContext, R.drawable.icon_real_name_country_right_01);
//        countryView.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3]);
    }

    /**
     * 获取RadioGroup 选中项内容
     */
    public static String getRadioGroupCheckedValue(RadioGroup rg) {
        if (rg == null) {
            return null;
        }
        String returnString = "";
        int count = rg.getChildCount();
        for (int i = 0; i < count; i++) {
            RadioButton rb = (RadioButton) rg.getChildAt(i);
            if (rb.isChecked()) {
                returnString = rb.getText().toString();
                break;
            }
        }
        return returnString;
    }

    public static String getTextMaxLength(String text, int maxLength) {
        if (text != null) {
            int length = text.length();
            if (length > maxLength) {
                return text.substring(0, length - 1) + "...";
            }
        }
        return text;
    }

    public String getCheckBoxGroupCheckedView(List<CheckBox> checkBoxes, String separator, boolean getTagValue) {
        if (checkBoxes == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        if (checkBoxes != null && !checkBoxes.isEmpty()) {
            for (CheckBox cb : checkBoxes) {
                if (cb.isChecked()) {
                    if (getTagValue) {
                        sb.append((String) cb.getTag()).append(separator);
                    } else {
                        sb.append((String) cb.getText()).append(separator);
                    }
                }
            }
        }
        String value = sb.toString();
        value = value.length() > 0 ? value.substring(0, value.length() - separator.length()) : value;
        return value;
    }

    //根据角度计算旋转之后位置
    public static PointF getPointRotate(PointF center, double radius, double degree) {
        PointF point = new PointF();
        point.x = (float) (radius * Math.cos(degree / 180 * Math.PI) + center.x);
        point.y = (float) (radius * Math.sin(degree / 180 * Math.PI) + center.y);
        return point;
    }

    public static int setColorAlpha(int color, float alpha) {
        float r = ((color >> 16) & 0xff) / 255.0f;
        float g = ((color >> 8) & 0xff) / 255.0f;
        float b = ((color) & 0xff) / 255.0f;
        return ((int) (alpha * 255.0f + 0.5f) << 24) |
                ((int) (r * 255.0f + 0.5f) << 16) |
                ((int) (g * 255.0f + 0.5f) << 8) |
                (int) (b * 255.0f + 0.5f);
    }

    public static boolean activityIsActive(Activity activity) {
        if (activity == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return !activity.isFinishing() && !activity.isDestroyed();
        } else {
            return !activity.isFinishing();
        }
    }

    public static void checkActivityAndRun(Context context, Runnable runnable) {
        if (runnable == null) {
            return;
        }
        if (context instanceof Activity) {
            if (activityIsActive((Activity) context)) {
                runnable.run();
            }
        } else {
            if (context != null) {
                runnable.run();
            }
        }
    }

    public static void checkActivityAndRunOnUI(Context context, Runnable runnable) {
        if (context instanceof Activity) {
            if (activityIsActive((Activity) context)) {
                ((Activity) context).runOnUiThread(runnable);
            }
        }
    }

    public static String delHTMLTag(String htmlStr) {
        // 过滤html标签
        Pattern p_html = Pattern.compile(REGEX_HTML, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll("");
        // 过滤空格回车标签
        Pattern p_space = Pattern.compile(REGEX_SPACE, Pattern.CASE_INSENSITIVE);
        Matcher m_space = p_space.matcher(htmlStr);
        htmlStr = m_space.replaceAll("");
        return htmlStr.trim(); // 返回文本字符串
    }

    public static String removeHtml(String htmlStr) {
        if (htmlStr == null) {
            return "";
        }
        htmlStr = Html.fromHtml(htmlStr).toString();
        htmlStr = htmlStr.replace("&nbsp;", " ");
        //先将换行符保留，然后过滤标签
        Pattern p_enter = Pattern.compile("<br/>", Pattern.CASE_INSENSITIVE);
        Matcher m_enter = p_enter.matcher(htmlStr);
        htmlStr = m_enter.replaceAll("\n");

        // 过滤script标签
        Pattern p_script = Pattern.compile(REGEX_SCRIPT, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll("");
        // 过滤style标签
        Pattern p_style = Pattern.compile(REGEX_STYLE, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll("");

        //过滤html标签
        Pattern p_html = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        return m_html.replaceAll("");
    }


    public static String twoBit(int i) {
        return (i < 10 ? "0" : "") + i;
    }

    public static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    public static String getFileSize(long size) {
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = size < 0 ? 0 : (int) (Math.log10(size) / Math.log10(1024));
        digitGroups = digitGroups < 0 ? 0 : digitGroups > 4 ? 4 : digitGroups;
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static void printError(Context context, Throwable throwable) {
        if (context != null && throwable != null) {
            if (CommonUtil.isApkInDebug(context)) {
                throwable.printStackTrace();
            }
        }
    }

    public static byte[] getSignInfo(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;
            Signature sign = signs[0];
            return parseSignature(sign.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] parseSignature(byte[] signature) {
        if (signature == null) {
            return null;
        }
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(signature));
            return certificate.getEncoded();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isImageUrl(String url) {
        Pattern pattern = Pattern.compile("^(http://|https://).*?(\\.jpg|\\.png|\\.jpeg|\\.gif)$");
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }

    public static boolean isUrl(String url) {
        return Patterns.WEB_URL.matcher(url).matches();
    }

    public static boolean equalsBoolean(Boolean b1, Boolean b2) {
        if (b1 == null && b2 == null) {
            return true;
        }
        if (b1 != null && b2 != null) {
            return b1 == b2;
        }
        return false;
    }

    public static String toHexEncodingColor(int color) {
        String R, G, B;
        StringBuffer sb = new StringBuffer();
        R = Integer.toHexString(Color.red(color));
        G = Integer.toHexString(Color.green(color));
        B = Integer.toHexString(Color.blue(color));
        //判断获取到的R,G,B值的长度 如果长度等于1 给R,G,B值的前边添0
        R = R.length() == 1 ? "0" + R : R;
        G = G.length() == 1 ? "0" + G : G;
        B = B.length() == 1 ? "0" + B : B;
        sb.append(R);
        sb.append(G);
        sb.append(B);
        return sb.toString();
    }

    public static String toHexEncoding(int color) {
        String R, G, B;
        StringBuffer sb = new StringBuffer();
        R = Integer.toHexString(Color.red(color));
        G = Integer.toHexString(Color.green(color));
        B = Integer.toHexString(Color.blue(color));
        //判断获取到的R,G,B值的长度 如果长度等于1 给R,G,B值的前边添0
        R = R.length() == 1 ? "0" + R : R;
        G = G.length() == 1 ? "0" + G : G;
        B = B.length() == 1 ? "0" + B : B;
        sb.append("0x");
        sb.append(R);
        sb.append(G);
        sb.append(B);
        return sb.toString();
    }

    //关联tabLayout ViewPager 联动
    public static void joinTabLayoutViewPager(final TabLayout tabLayout, final ViewPager viewPager) {
        if (tabLayout == null || viewPager == null) {
            return;
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                TabLayout.Tab tab = tabLayout.getTabAt(position);
                if (tab != null) {
                    tab.select();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public static String setWebViewDataDefaultTextColor(String webVewData, int color) {
        if (TextUtils.isEmpty(webVewData)) {
            return webVewData;
        }
        String colorFormat = toHexEncodingColor(color);
        String defaultCss = "\n<style> \n" +
                "body{ \n" +
                " color: #" + colorFormat + "\n" +
                " } \n" +
                " </style> ";
        try {
            if (webVewData.contains("<head>")) {
                return webVewData.replace("<head>", "<head>" + defaultCss);
            } else if (webVewData.contains("<html>")) {
                return webVewData.replace("<html>", "<html><head>" + defaultCss + "</head>");
            }
            return webVewData;
        } catch (Exception e) {
        }
        return webVewData;
    }

    /**
     * 判断是否开启通知权限
     *
     * @param context
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean isNotificationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            boolean areNotificationsEnabled = notificationManagerCompat.areNotificationsEnabled();
            return areNotificationsEnabled;
        }

        String CHECK_OP_NO_THROW = "checkOpNoThrow";
        String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo appInfo = context.getApplicationInfo();
        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;

        Class appOpsClass = null;
        /* Context.APP_OPS_MANAGER */
        try {
            appOpsClass = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,
                    String.class);
            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);

            int value = (Integer) opPostNotificationValue.get(Integer.class);
            return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);

        } catch (Exception e) {
        }
        return false;
//        return NotificationManagerCompat.from(context).areNotificationsEnabled();
//        String CHECK_OP_NO_THROW = "checkOpNoThrow";
//        String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";
//
//        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
//        ApplicationInfo appInfo = context.getApplicationInfo();
//        String pkg = context.getApplicationContext().getPackageName();
//        int uid = appInfo.uid;
//
//        Class appOpsClass = null;
//        /* Context.APP_OPS_MANAGER */
//        try {
//            appOpsClass = Class.forName(AppOpsManager.class.getName());
//            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,
//                    String.class);
//            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
//
//            int value = (Integer) opPostNotificationValue.get(Integer.class);
//            return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
    }

    public static void gotoSet(Context context) {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= 26) {
            // android 8.0引导
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());
        } else if (Build.VERSION.SDK_INT >= 21) {
            // android 5.0-7.0
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
        } else {
            // 其他
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void resetTextViewHardWidth(TextView view) {
        if (view != null) {
            float width = view.getPaint().measureText(view.getText().toString());
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params == null) {
                params = new ViewGroup.LayoutParams((int) width, ViewGroup.LayoutParams.WRAP_CONTENT);
            } else {
                params.width = (int) width;
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
            view.setLayoutParams(params);
        }
    }

    public static void resetViewSize(View view, int width, int height) {
        if (view != null) {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params == null) {
                params = new ViewGroup.LayoutParams(width, height);
            } else {
                params.width = width;
                params.height = height;
            }
            view.setLayoutParams(params);
        }
    }
}
