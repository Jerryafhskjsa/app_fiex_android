package cn.udesk;

import android.content.Context;
import android.content.pm.ApplicationInfo;

public class CommonUtil {

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

    public static void printError(Context context, Throwable throwable) {
        if (context != null && throwable != null) {
            if (CommonUtil.isApkInDebug(context)) {
                throwable.printStackTrace();
            }
        }
    }
}
