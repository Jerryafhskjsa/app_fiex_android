package com.black.util;

import androidx.annotation.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class NumberUtil {
    //数字格式化
    private static final NumberFormat nf = NumberFormat.getNumberInstance();
    //数字格式化
    private static final NumberFormat nf2 = NumberFormat.getNumberInstance();
    //数字格式化
    private static final NumberFormat nf3 = NumberFormat.getNumberInstance();
    //数字格式化
    private static final NumberFormat nf4 = NumberFormat.getNumberInstance();
    //数字格式化
    private static final NumberFormat nf5 = NumberFormat.getNumberInstance();
    //数字格式化
    private static final NumberFormat nf6 = NumberFormat.getNumberInstance();
    //数字格式化
    private static final NumberFormat nf7 = NumberFormat.getNumberInstance();
    //数字格式化
    private static final NumberFormat nf8 = NumberFormat.getNumberInstance();
    //数字格式化
    private static final NumberFormat nf9 = NumberFormat.getNumberInstance();

    static {
        nf.setMaximumFractionDigits(8);
        nf2.setMaximumFractionDigits(2);
        nf3.setMaximumFractionDigits(8);
        nf3.setGroupingUsed(false);
        nf4.setMaximumFractionDigits(8);
        nf5.setMaximumFractionDigits(8);
        nf5.setGroupingUsed(false);
        nf9.setGroupingUsed(false);
    }

    public static String formatNumber(Number number) {
        return number == null ? null : nf.format(number);
    }

    public static String formatNumber2(Number number) {
        return number == null ? null : nf2.format(number == null ? 0.0d : number);
    }

    public static String formatNumberNoGroup(Number number) {
        return nf3.format(number == null ? 0.0d : number);
    }

    public static String formatNumber(Number number, int scale) {
        nf4.setMaximumFractionDigits(scale);
        return number == null ? null : nf4.format(number);
    }

    public static String formatNumber(Number number, int minScale, int maxScale) {
        nf7.setMaximumFractionDigits(maxScale);
        nf7.setMinimumFractionDigits(minScale);
        return number == null ? null : nf7.format(number);
    }

    public static String formatNumberHardScale(Number number, int scale) {
        nf6.setMaximumFractionDigits(scale);
        nf6.setMinimumFractionDigits(scale);
        return number == null ? null : nf6.format(number);
    }

    public static String formatNumberNoGroupHardScale(Number number, int scale) {
        return formatNumberNoGroup(number, scale, scale);
    }

    public static String formatNumberNoGroupHardScale(Number number, RoundingMode roundingMode, int scale) {
        return formatNumberNoGroup(number, roundingMode, scale, scale);
    }

    public static String formatNumberNoGroup(Number number, int scale) {
        nf5.setMaximumFractionDigits(scale);
        return nf5.format(number == null ? 0.0d : number);
    }

    public static String formatNumberNoGroup(Number number, int minScale, int maxScale) {
        return formatNumberNoGroup(number, null, minScale, maxScale);
    }

    public static String formatNumberNoGroupScale(Number number, RoundingMode roundingMode, int minScale, int maxScale) {
        if (number == null) {
            return "0";
        }
        return formatNumberNoGroup(number, roundingMode, minScale, maxScale);
    }

    public static String formatNumberNoGroup(Number number, RoundingMode roundingMode, int minScale, int maxScale) {
        minScale = minScale < 0 ? 0 : minScale;
        maxScale = maxScale < 0 ? 0 : maxScale;
        String formatString = formatNumberNoGroupDot(number, roundingMode, maxScale);
        if (minScale < maxScale) {
            int checkCount = maxScale - minScale;
            for (int i = 0; i < checkCount; i++) {
                Character lastCh = getChar(formatString, formatString.length() - 1);
                if (lastCh != null && lastCh == '0') {
                    formatString = formatString.substring(0, formatString.length() - 1);
                } else {
                    break;
                }
            }
        }
        if (formatString.endsWith(".")) {
            formatString = formatString.substring(0, formatString.length() - 1);
        }
        return formatString;
    }

    public static String formatNumberDynamicScale(Number number, int maxLength, int minScale, int maxScale) {
        if (number == null || number.doubleValue() == 0) {
            return "0.00";
        }
        //计算小数位数
        double log10 = log10(number.doubleValue());
        int log10Int = (int) log10 + 1;
        if (log10Int + maxScale > maxLength) {
            maxScale = maxLength - log10Int;
        }
        if (log10Int + minScale > maxLength) {
            minScale = maxLength - log10Int;
        }
        if (maxScale < 0) {
            maxScale = 0;
        }
        if (minScale < 0) {
            minScale = 0;
        }
        nf8.setMaximumFractionDigits(maxScale);
        nf8.setMinimumFractionDigits(minScale);
        return nf8.format(number);
    }

    /**
     * @param number
     * @param maxLength 数字总长度
     * @param minScale  最小小数位
     * @param maxScale  最大小数位
     * @return
     */
    public static String formatNumberDynamicScaleNoGroup(Number number, int maxLength, int minScale, int maxScale) {
        if (number == null || number.doubleValue() == 0) {
            return "0.00";
        }
        //计算小数位数
        double log10 = log10(number.doubleValue());
        int log10Int = (int) log10 + 1;
        if (log10Int + maxScale > maxLength) {
            maxScale = maxLength - log10Int;
        }
        if (log10Int + minScale > maxLength) {
            minScale = maxLength - log10Int;
        }
        if (maxScale < 0) {
            maxScale = 0;
        }
        if (minScale < 0) {
            minScale = 0;
        }
        nf9.setMaximumFractionDigits(maxScale);
        nf9.setMinimumFractionDigits(minScale);
        return nf9.format(number);
    }

    public static Double log(double value, double base) {
        double logBase = Math.log(base);
        if (logBase == 0) {
            return null;
        }
        return Math.log(value) / Math.log(base);
    }

    public static Double log10(double value) {
        return log(value, 10.0);
    }

    public static String formatFloatNumber(Double value) {
        if (value != null) {
            if (value.doubleValue() != 0.00) {
                DecimalFormat df = new DecimalFormat("########.00000000");
                return df.format(value.doubleValue());
            } else {
                return "0.00";
            }
        }
        return "";
    }

    /**
     * 格式化固定小数位数
     *
     * @param number
     * @param scale
     * @return
     */
    public static String formatNumberNoGroupDot(Number number, RoundingMode roundingMode, int scale) {
        if (number == null) {
            return "0";
        }
        scale = scale < 0 ? 0 : scale;
        StringBuilder format = new StringBuilder("#####0.");
        for (int i = 0; i < scale; i++) {
            format.append("0");
        }
        DecimalFormat df = new DecimalFormat(format.toString());
        if (roundingMode != null) {
            df.setRoundingMode(roundingMode);
        }
        BigDecimal bigDecimal = number instanceof BigDecimal ? (BigDecimal) number : new BigDecimal(number.doubleValue());
        String result = df.format(bigDecimal);
        return result;
    }

    public static String formatNumberK2(Number number) {
        DecimalFormat df = new DecimalFormat("###,##0.00");
        return number == null ? null : df.format(number);
    }

    public static Character getChar(String source, int index) {
        if (source == null || source.length() < 1 || index < 0 || source.length() < index + 1) {
            return null;
        }
        return source.charAt(index);
    }


    public static BigDecimal toBigDecimal(@NonNull String value){
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
