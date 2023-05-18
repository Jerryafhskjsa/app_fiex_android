package com.black.util;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class NumberUtils {
    public static final Long LONG_ZERO = 0L;
    public static final Long LONG_ONE = 1L;
    public static final Long LONG_MINUS_ONE = -1L;
    public static final Integer INTEGER_ZERO = 0;
    public static final Integer INTEGER_ONE = 1;
    public static final Integer INTEGER_MINUS_ONE = -1;
    public static final Short SHORT_ZERO = Short.valueOf((short)0);
    public static final Short SHORT_ONE = Short.valueOf((short)1);
    public static final Short SHORT_MINUS_ONE = Short.valueOf((short)-1);
    public static final Byte BYTE_ZERO = 0;
    public static final Byte BYTE_ONE = 1;
    public static final Byte BYTE_MINUS_ONE = -1;
    public static final Double DOUBLE_ZERO = 0.0;
    public static final Double DOUBLE_ONE = 1.0;
    public static final Double DOUBLE_MINUS_ONE = -1.0;
    public static final Float FLOAT_ZERO = 0.0F;
    public static final Float FLOAT_ONE = 1.0F;
    public static final Float FLOAT_MINUS_ONE = -1.0F;

    public static String formatRoundUp(Object number, int minFractionDigits, int maxFractionDigits) {
        return format(number, minFractionDigits, maxFractionDigits, RoundingMode.UP);
    }

    public static String formatRoundDown(Object number, int minFractionDigits, int maxFractionDigits) {
        return format(number, minFractionDigits, maxFractionDigits, RoundingMode.DOWN);
    }

    public static String formatRoundHalfUp(Object number, int minFractionDigits, int maxFractionDigits) {
        return format(number, minFractionDigits, maxFractionDigits, RoundingMode.HALF_UP);
    }

    public static String format(Object number, int minFractionDigits, int maxFractionDigits, RoundingMode roundingMode) {
        return format(number, minFractionDigits, maxFractionDigits, roundingMode, Integer.MAX_VALUE);
    }

    public static String format(Object number, int minFractionDigits, int maxFractionDigits, RoundingMode roundingMode, int maxLength) {
        return format(number, minFractionDigits, maxFractionDigits, roundingMode, maxLength, false, String.valueOf(0));
    }

    public static String format(Object number, int minFractionDigits, int maxFractionDigits, RoundingMode roundingMode, boolean sign) {
        return format(number, minFractionDigits, maxFractionDigits, roundingMode, Integer.MAX_VALUE, sign, String.valueOf(0));
    }

    public static String format(Object number, int minFractionDigits, int maxFractionDigits, RoundingMode roundingMode, int maxLength, boolean sign, String defaultValue) {
        try {
            if (number == null) {
                return defaultValue;
            } else {
                String format = formatNumberNoGroupDot(innerConvertDecimal(number), roundingMode, maxFractionDigits);
                if (minFractionDigits < maxFractionDigits) {
                    int checkCount = maxFractionDigits - minFractionDigits;

                    for(int i = 0; i < checkCount; ++i) {
                        Character lastCh = getChar(format, format.length() - 1);
                        if (lastCh == null || lastCh != '0') {
                            break;
                        }

                        format = format.substring(0, format.length() - 1);
                    }
                }

                if (format.length() > maxLength) {
                    format = format.substring(0, maxLength);
                }

                if (format.endsWith(".")) {
                    format = format.substring(0, format.length() - 1);
                }

                return format;
            }
        } catch (Exception var11) {
            return defaultValue;
        }
    }

    public static String formatNumberNoGroupDot(Number number, RoundingMode roundingMode, int scale) {
        if (number == null) {
            return "0";
        } else {
            scale = scale < 0 ? 0 : scale;
            StringBuilder format = new StringBuilder("#####0.");

            for(int i = 0; i < scale; ++i) {
                format.append("0");
            }

            DecimalFormat df = new DecimalFormat(format.toString());
            if (roundingMode != null) {
                df.setRoundingMode(roundingMode);
            }

            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator('.');
            df.setDecimalFormatSymbols(symbols);
            BigDecimal bigDecimal = number instanceof BigDecimal ? (BigDecimal)number : new BigDecimal(number.doubleValue());
            String result = df.format(bigDecimal);
            return result;
        }
    }

    public static Character getChar(String source, int index) {
        return source != null && source.length() >= 1 && index >= 0 && source.length() >= index + 1 ? source.charAt(index) : null;
    }

    private static BigDecimal innerConvertDecimal(Object number) throws NumberFormatException, ArithmeticException {
        if (number instanceof Long || number instanceof Integer || number instanceof Short || number instanceof Byte || number instanceof AtomicInteger || number instanceof AtomicLong || number instanceof BigInteger && ((BigInteger)number).bitLength() < 64) {
            return new BigDecimal(((Number)number).longValue());
        } else if (number instanceof BigDecimal) {
            return (BigDecimal)number;
        } else if (number instanceof BigInteger) {
            return new BigDecimal((BigInteger)number);
        } else {
            BigDecimal bigDecimal;
            if (number instanceof Float) {
                bigDecimal = new BigDecimal(String.valueOf(number));
                return bigDecimal.scale() < 16 ? bigDecimal : new BigDecimal((double)(Float)number);
            } else if (number instanceof Double) {
                bigDecimal = new BigDecimal(String.valueOf(number));
                return bigDecimal.scale() < 16 ? bigDecimal : BigDecimal.valueOf((Double)number);
            } else if (number instanceof String) {
                return ((String)number).isEmpty() ? BigDecimal.ZERO : new BigDecimal((String)number);
            } else {
                return String.valueOf(number).isEmpty() ? BigDecimal.ZERO : new BigDecimal(String.valueOf(number));
            }
        }
    }

    public static BigDecimal add(Object b1, Object b2) {
        return add(b1, b2, new BigDecimal(0));
    }

    public static BigDecimal add(Object b1, Object b2, BigDecimal defaultValue) {
        try {
            BigDecimal bc1 = innerConvertDecimal(b1);
            BigDecimal bc2 = innerConvertDecimal(b2);
            return bc1.add(bc2);
        } catch (Exception var5) {
            return defaultValue;
        }
    }

    public static BigDecimal subtract(Object b1, Object b2) {
        return subtract(b1, b2, new BigDecimal(0));
    }

    public static BigDecimal subtract(Object b1, Object b2, BigDecimal defaultValue) {
        try {
            BigDecimal bc1 = innerConvertDecimal(b1);
            BigDecimal bc2 = innerConvertDecimal(b2);
            return bc1.subtract(bc2);
        } catch (Exception var5) {
            return defaultValue;
        }
    }

    public static BigDecimal multiply(Object b1, Object b2) {
        return multiply(b1, b2, new BigDecimal(0));
    }

    public static BigDecimal multiply(Object b1, Object b2, BigDecimal defaultValue) {
        try {
            BigDecimal bc1 = innerConvertDecimal(b1);
            BigDecimal bc2 = innerConvertDecimal(b2);
            return bc1.multiply(bc2);
        } catch (Exception var5) {
            return defaultValue;
        }
    }

    public static BigDecimal divide(Object b1, Object b2) {
        return divide(b1, b2, 16);
    }

    public static BigDecimal divide(Object b1, Object b2, int scale) {
        return divide(b1, b2, scale, RoundingMode.FLOOR);
    }

    public static BigDecimal divide(Object b1, Object b2, int scale, RoundingMode roundingMode) {
        return divide(b1, b2, scale, roundingMode, new BigDecimal(0));
    }

    public static BigDecimal divide(Object b1, Object b2, int scale, RoundingMode roundingMode, BigDecimal defaultValue) {
        try {
            BigDecimal bc1 = innerConvertDecimal(b1);
            BigDecimal bc2 = innerConvertDecimal(b2);
            return bc1.divide(bc2, scale, roundingMode);
        } catch (Exception var7) {
            return defaultValue;
        }
    }

    public static BigDecimal min(BigDecimal b1, BigDecimal b2) {
        return b1 != null && b2 != null ? b1.min(b2) : new BigDecimal(0);
    }

    public static BigDecimal min(BigDecimal... array) {
        try {
            if (array != null && array.length > 0) {
                BigDecimal min = array[0];
                BigDecimal[] var2 = array;
                int var3 = array.length;

                for(int var4 = 0; var4 < var3; ++var4) {
                    BigDecimal bigDecimal = var2[var4];
                    if (bigDecimal.compareTo(min) < 0) {
                        min = bigDecimal;
                    }
                }

                return min;
            }
        } catch (Throwable var6) {
        }

        return new BigDecimal(Integer.MIN_VALUE);
    }

    public static BigDecimal max(BigDecimal... array) {
        try {
            if (array != null && array.length > 0) {
                BigDecimal max = array[0];
                BigDecimal[] var2 = array;
                int var3 = array.length;

                for(int var4 = 0; var4 < var3; ++var4) {
                    BigDecimal bigDecimal = var2[var4];
                    if (bigDecimal.compareTo(max) > 0) {
                        max = bigDecimal;
                    }
                }

                return max;
            }
        } catch (Throwable var6) {
        }

        return new BigDecimal(Integer.MAX_VALUE);
    }

    public static boolean inClosedRange(Object value, Object min, Object max) {
        try {
            BigDecimal bdValue = innerConvertDecimal(value);
            BigDecimal bdMin = innerConvertDecimal(min);
            BigDecimal bdMax = innerConvertDecimal(max);
            return inClosedRange(bdValue, bdMin, bdMax);
        } catch (Exception var6) {
            return false;
        }
    }

    public static boolean inClosedRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value != null && min != null && max != null) {
            return max.compareTo(min) > 0 && value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
        } else {
            return false;
        }
    }

    public static boolean inOpenedRange(Object value, Object min, Object max) {
        try {
            BigDecimal bdValue = innerConvertDecimal(value);
            BigDecimal bdMin = innerConvertDecimal(min);
            BigDecimal bdMax = innerConvertDecimal(max);
            return inOpenedRange(bdValue, bdMin, bdMax);
        } catch (Exception var6) {
            return false;
        }
    }

    public static boolean inOpenedRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value != null && min != null && max != null) {
            return max.compareTo(min) > 0 && value.compareTo(min) > 0 && value.compareTo(max) < 0;
        } else {
            return false;
        }
    }

    public NumberUtils() {
    }

    public static int toInt(String str) {
        return toInt(str, 0);
    }

    public static Integer toInt(String str, Integer defaultValue) {
        if (str == null) {
            return defaultValue;
        } else {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException var3) {
                return defaultValue;
            }
        }
    }

    public static long toLong(String str) {
        return toLong(str, 0L);
    }

    public static Long toLong(String str, Long defaultValue) {
        if (str == null) {
            return defaultValue;
        } else {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException var3) {
                return defaultValue;
            }
        }
    }

    public static float toFloat(String str) {
        return toFloat(str, 0.0F);
    }

    public static float toFloat(String str, float defaultValue) {
        if (str == null) {
            return defaultValue;
        } else {
            try {
                return Float.parseFloat(str);
            } catch (NumberFormatException var3) {
                return defaultValue;
            }
        }
    }

    public static double toDouble(String str) {
        return toDouble(str, 0.0);
    }

    public static Double toDouble(String str, Double defaultValue) {
        if (str == null) {
            return defaultValue;
        } else {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException var3) {
                return defaultValue;
            }
        }
    }

    public static byte toByte(String str) {
        return toByte(str, (byte)0);
    }

    public static byte toByte(String str, byte defaultValue) {
        if (str == null) {
            return defaultValue;
        } else {
            try {
                return Byte.parseByte(str);
            } catch (NumberFormatException var3) {
                return defaultValue;
            }
        }
    }

    public static short toShort(String str) {
        return toShort(str, (short)0);
    }

    public static short toShort(String str, short defaultValue) {
        if (str == null) {
            return defaultValue;
        } else {
            try {
                return Short.parseShort(str);
            } catch (NumberFormatException var3) {
                return defaultValue;
            }
        }
    }

    public static BigDecimal toBigDecimal(String str) {
        return toBigDecimal(str, new BigDecimal(0));
    }

    public static BigDecimal toBigDecimal(String str, BigDecimal defaultValue) {
        try {
            return new BigDecimal(str);
        } catch (Exception var3) {
            return defaultValue;
        }
    }

    public static Number createNumber(String str) throws NumberFormatException {
        if (str == null) {
            return null;
        } else if (StringUtils.isBlank(str)) {
            throw new NumberFormatException("A blank string is not a valid number");
        } else {
            String[] hex_prefixes = new String[]{"0x", "0X", "-0x", "-0X", "#", "-#"};
            int pfxLen = 0;
            String[] var3 = hex_prefixes;
            int hexDigits = hex_prefixes.length;

            String exp;
            for(int var5 = 0; var5 < hexDigits; ++var5) {
                exp = var3[var5];
                if (str.startsWith(exp)) {
                    pfxLen += exp.length();
                    break;
                }
            }

            char lastChar;
            if (pfxLen > 0) {
                lastChar = 0;

                for(hexDigits = pfxLen; hexDigits < str.length(); ++hexDigits) {
                    lastChar = str.charAt(hexDigits);
                    if (lastChar != '0') {
                        break;
                    }

                    ++pfxLen;
                }

                hexDigits = str.length() - pfxLen;
                if (hexDigits <= 16 && (hexDigits != 16 || lastChar <= '7')) {
                    return (Number)(hexDigits <= 8 && (hexDigits != 8 || lastChar <= '7') ? createInteger(str) : createLong(str));
                } else {
                    return createBigInteger(str);
                }
            } else {
                lastChar = str.charAt(str.length() - 1);
                int decPos = str.indexOf(46);
                int expPos = str.indexOf(101) + str.indexOf(69) + 1;
                int numDecimals = 0;
                String mant;
                String dec;
                if (decPos <= -1) {
                    if (expPos > -1) {
                        if (expPos > str.length()) {
                            throw new NumberFormatException(str + " is not a valid number.");
                        }

                        mant = getMantissa(str, expPos);
                    } else {
                        mant = getMantissa(str);
                    }

                    dec = null;
                } else {
                    if (expPos > -1) {
                        if (expPos < decPos || expPos > str.length()) {
                            throw new NumberFormatException(str + " is not a valid number.");
                        }

                        dec = str.substring(decPos + 1, expPos);
                    } else {
                        dec = str.substring(decPos + 1);
                    }

                    mant = getMantissa(str, decPos);
                    numDecimals = dec.length() + decPos + 1;
                }

                if (!Character.isDigit(lastChar) && lastChar != '.') {
                    if (expPos > -1 && expPos < str.length() - 1) {
                        exp = str.substring(expPos + 1, str.length() - 1);
                    } else {
                        exp = null;
                    }

                    String numeric = str.substring(0, str.length() - 1);
                    boolean allZeros = isAllZeros(mant) && isAllZeros(exp);
                    switch (lastChar) {
                        case 'D':
                        case 'd':
                            break;
                        case 'F':
                        case 'f':
                            try {
                                Float f = createFloat(numeric);
                                if (f.isInfinite() || f == 0.0F && !allZeros) {
                                    break;
                                }

                                return f;
                            } catch (NumberFormatException var18) {
                                break;
                            }
                        case 'L':
                        case 'l':
                            if (dec == null && exp == null && (numeric.charAt(0) == '-' && isDigits(numeric.substring(1)) || isDigits(numeric))) {
                                try {
                                    return createLong(numeric);
                                } catch (NumberFormatException var14) {
                                    return createBigInteger(numeric);
                                }
                            }

                            throw new NumberFormatException(str + " is not a valid number.");
                        default:
                            throw new NumberFormatException(str + " is not a valid number.");
                    }

                    try {
                        Double d = createDouble(numeric);
                        if (!d.isInfinite() && ((double)d.floatValue() != 0.0 || allZeros)) {
                            return d;
                        }
                    } catch (NumberFormatException var17) {
                    }

                    try {
                        return createBigDecimal(numeric);
                    } catch (NumberFormatException var16) {
                        throw new NumberFormatException(str + " is not a valid number.");
                    }
                } else {
                    if (expPos > -1 && expPos < str.length() - 1) {
                        exp = str.substring(expPos + 1, str.length());
                    } else {
                        exp = null;
                    }

                    if (dec == null && exp == null) {
                        try {
                            return createInteger(str);
                        } catch (NumberFormatException var15) {
                            try {
                                return createLong(str);
                            } catch (NumberFormatException var13) {
                                return createBigInteger(str);
                            }
                        }
                    } else {
                        boolean allZeros = isAllZeros(mant) && isAllZeros(exp);

                        try {
                            if (numDecimals <= 7) {
                                Float f = createFloat(str);
                                if (!f.isInfinite() && (f != 0.0F || allZeros)) {
                                    return f;
                                }
                            }
                        } catch (NumberFormatException var20) {
                        }

                        try {
                            if (numDecimals <= 16) {
                                Double d = createDouble(str);
                                if (!d.isInfinite() && (d != 0.0 || allZeros)) {
                                    return d;
                                }
                            }
                        } catch (NumberFormatException var19) {
                        }

                        return createBigDecimal(str);
                    }
                }
            }
        }
    }

    private static String getMantissa(String str) {
        return getMantissa(str, str.length());
    }

    private static String getMantissa(String str, int stopPos) {
        char firstChar = str.charAt(0);
        boolean hasSign = firstChar == '-' || firstChar == '+';
        return hasSign ? str.substring(1, stopPos) : str.substring(0, stopPos);
    }

    private static boolean isAllZeros(String str) {
        if (str == null) {
            return true;
        } else {
            for(int i = str.length() - 1; i >= 0; --i) {
                if (str.charAt(i) != '0') {
                    return false;
                }
            }

            return str.length() > 0;
        }
    }

    public static Float createFloat(String str) {
        return str == null ? null : Float.valueOf(str);
    }

    public static Double createDouble(String str) {
        return str == null ? null : Double.valueOf(str);
    }

    public static Integer createInteger(String str) {
        return str == null ? null : Integer.decode(str);
    }

    public static Long createLong(String str) {
        return str == null ? null : Long.decode(str);
    }

    public static BigInteger createBigInteger(String str) {
        if (str == null) {
            return null;
        } else {
            int pos = 0;
            int radix = 10;
            boolean negate = false;
            if (str.startsWith("-")) {
                negate = true;
                pos = 1;
            }

            if (!str.startsWith("0x", pos) && !str.startsWith("0X", pos)) {
                if (str.startsWith("#", pos)) {
                    radix = 16;
                    ++pos;
                } else if (str.startsWith("0", pos) && str.length() > pos + 1) {
                    radix = 8;
                    ++pos;
                }
            } else {
                radix = 16;
                pos += 2;
            }

            BigInteger value = new BigInteger(str.substring(pos), radix);
            return negate ? value.negate() : value;
        }
    }

    public static BigDecimal createBigDecimal(String str) {
        if (str == null) {
            return null;
        } else if (StringUtils.isBlank(str)) {
            throw new NumberFormatException("A blank string is not a valid number");
        } else if (str.trim().startsWith("--")) {
            throw new NumberFormatException(str + " is not a valid number.");
        } else {
            return new BigDecimal(str);
        }
    }

    public static long min(long... array) {
        validateArray(array);
        long min = array[0];

        for(int i = 1; i < array.length; ++i) {
            if (array[i] < min) {
                min = array[i];
            }
        }

        return min;
    }

    public static int min(int... array) {
        validateArray(array);
        int min = array[0];

        for(int j = 1; j < array.length; ++j) {
            if (array[j] < min) {
                min = array[j];
            }
        }

        return min;
    }

    public static short min(short... array) {
        validateArray(array);
        short min = array[0];

        for(int i = 1; i < array.length; ++i) {
            if (array[i] < min) {
                min = array[i];
            }
        }

        return min;
    }

    public static byte min(byte... array) {
        validateArray(array);
        byte min = array[0];

        for(int i = 1; i < array.length; ++i) {
            if (array[i] < min) {
                min = array[i];
            }
        }

        return min;
    }

    public static double min(double... array) {
        validateArray(array);
        double min = array[0];

        for(int i = 1; i < array.length; ++i) {
            if (Double.isNaN(array[i])) {
                return Double.NaN;
            }

            if (array[i] < min) {
                min = array[i];
            }
        }

        return min;
    }

    public static float min(float... array) {
        validateArray(array);
        float min = array[0];

        for(int i = 1; i < array.length; ++i) {
            if (Float.isNaN(array[i])) {
                return Float.NaN;
            }

            if (array[i] < min) {
                min = array[i];
            }
        }

        return min;
    }

    public static long max(long... array) {
        validateArray(array);
        long max = array[0];

        for(int j = 1; j < array.length; ++j) {
            if (array[j] > max) {
                max = array[j];
            }
        }

        return max;
    }

    public static int max(int... array) {
        validateArray(array);
        int max = array[0];

        for(int j = 1; j < array.length; ++j) {
            if (array[j] > max) {
                max = array[j];
            }
        }

        return max;
    }

    public static short max(short... array) {
        validateArray(array);
        short max = array[0];

        for(int i = 1; i < array.length; ++i) {
            if (array[i] > max) {
                max = array[i];
            }
        }

        return max;
    }

    public static byte max(byte... array) {
        validateArray(array);
        byte max = array[0];

        for(int i = 1; i < array.length; ++i) {
            if (array[i] > max) {
                max = array[i];
            }
        }

        return max;
    }

    public static double max(double... array) {
        validateArray(array);
        double max = array[0];

        for(int j = 1; j < array.length; ++j) {
            if (Double.isNaN(array[j])) {
                return Double.NaN;
            }

            if (array[j] > max) {
                max = array[j];
            }
        }

        return max;
    }

    public static float max(float... array) {
        validateArray(array);
        float max = array[0];

        for(int j = 1; j < array.length; ++j) {
            if (Float.isNaN(array[j])) {
                return Float.NaN;
            }

            if (array[j] > max) {
                max = array[j];
            }
        }

        return max;
    }

    private static void validateArray(Object array) {
        if (array == null) {
            throw new IllegalArgumentException("The Array must not be null");
        } else {
            Validate.isTrue(Array.getLength(array) != 0, "Array cannot be empty.", new Object[0]);
        }
    }

    public static long min(long a, long b, long c) {
        if (b < a) {
            a = b;
        }

        if (c < a) {
            a = c;
        }

        return a;
    }

    public static int min(int a, int b, int c) {
        if (b < a) {
            a = b;
        }

        if (c < a) {
            a = c;
        }

        return a;
    }

    public static short min(short a, short b, short c) {
        if (b < a) {
            a = b;
        }

        if (c < a) {
            a = c;
        }

        return a;
    }

    public static byte min(byte a, byte b, byte c) {
        if (b < a) {
            a = b;
        }

        if (c < a) {
            a = c;
        }

        return a;
    }

    public static double min(double a, double b, double c) {
        return Math.min(Math.min(a, b), c);
    }

    public static float min(float a, float b, float c) {
        return Math.min(Math.min(a, b), c);
    }

    public static long max(long a, long b, long c) {
        if (b > a) {
            a = b;
        }

        if (c > a) {
            a = c;
        }

        return a;
    }

    public static int max(int a, int b, int c) {
        if (b > a) {
            a = b;
        }

        if (c > a) {
            a = c;
        }

        return a;
    }

    public static short max(short a, short b, short c) {
        if (b > a) {
            a = b;
        }

        if (c > a) {
            a = c;
        }

        return a;
    }

    public static byte max(byte a, byte b, byte c) {
        if (b > a) {
            a = b;
        }

        if (c > a) {
            a = c;
        }

        return a;
    }

    public static double max(double a, double b, double c) {
        return Math.max(Math.max(a, b), c);
    }

    public static float max(float a, float b, float c) {
        return Math.max(Math.max(a, b), c);
    }

    public static boolean isDigits(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        } else {
            for(int i = 0; i < str.length(); ++i) {
                if (!Character.isDigit(str.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }

    public static boolean isNumber(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        } else {
            char[] chars = str.toCharArray();
            int sz = chars.length;
            boolean hasExp = false;
            boolean hasDecPoint = false;
            boolean allowSigns = false;
            boolean foundDigit = false;
            int start = chars[0] == '-' ? 1 : 0;
            int i;
            if (sz > start + 1 && chars[start] == '0') {
                if (chars[start + 1] == 'x' || chars[start + 1] == 'X') {
                    i = start + 2;
                    if (i == sz) {
                        return false;
                    }

                    while(i < chars.length) {
                        if ((chars[i] < '0' || chars[i] > '9') && (chars[i] < 'a' || chars[i] > 'f') && (chars[i] < 'A' || chars[i] > 'F')) {
                            return false;
                        }

                        ++i;
                    }

                    return true;
                }

                if (Character.isDigit(chars[start + 1])) {
                    for(i = start + 1; i < chars.length; ++i) {
                        if (chars[i] < '0' || chars[i] > '7') {
                            return false;
                        }
                    }

                    return true;
                }
            }

            --sz;

            for(i = start; i < sz || i < sz + 1 && allowSigns && !foundDigit; ++i) {
                if (chars[i] >= '0' && chars[i] <= '9') {
                    foundDigit = true;
                    allowSigns = false;
                } else if (chars[i] == '.') {
                    if (hasDecPoint || hasExp) {
                        return false;
                    }

                    hasDecPoint = true;
                } else if (chars[i] != 'e' && chars[i] != 'E') {
                    if (chars[i] != '+' && chars[i] != '-') {
                        return false;
                    }

                    if (!allowSigns) {
                        return false;
                    }

                    allowSigns = false;
                    foundDigit = false;
                } else {
                    if (hasExp) {
                        return false;
                    }

                    if (!foundDigit) {
                        return false;
                    }

                    hasExp = true;
                    allowSigns = true;
                }
            }

            if (i < chars.length) {
                if (chars[i] >= '0' && chars[i] <= '9') {
                    return true;
                } else if (chars[i] != 'e' && chars[i] != 'E') {
                    if (chars[i] == '.') {
                        return !hasDecPoint && !hasExp ? foundDigit : false;
                    } else if (!allowSigns && (chars[i] == 'd' || chars[i] == 'D' || chars[i] == 'f' || chars[i] == 'F')) {
                        return foundDigit;
                    } else if (chars[i] != 'l' && chars[i] != 'L') {
                        return false;
                    } else {
                        return foundDigit && !hasExp && !hasDecPoint;
                    }
                } else {
                    return false;
                }
            } else {
                return !allowSigns && foundDigit;
            }
        }
    }

    public static boolean isParsable(String str) {
        if (StringUtils.endsWith(str, ".")) {
            return false;
        } else {
            return StringUtils.startsWith(str, "-") ? isDigits(StringUtils.replaceOnce(str.substring(1), ".", "")) : isDigits(StringUtils.replaceOnce(str, ".", ""));
        }
    }

    public static int compare(int x, int y) {
        if (x == y) {
            return 0;
        } else {
            return x < y ? -1 : 1;
        }
    }

    public static int compare(long x, long y) {
        if (x == y) {
            return 0;
        } else {
            return x < y ? -1 : 1;
        }
    }

    public static int compare(short x, short y) {
        if (x == y) {
            return 0;
        } else {
            return x < y ? -1 : 1;
        }
    }

    public static int compare(byte x, byte y) {
        return x - y;
    }

    public static int compare(Object x, Object y) {
        try {
            BigDecimal xBd = innerConvertDecimal(x);
            BigDecimal yBd = innerConvertDecimal(y);
            return xBd.compareTo(yBd);
        } catch (Throwable var4) {
            return 0;
        }
    }

    public static boolean isZero(Object x) {
        try {
            BigDecimal xBd = innerConvertDecimal(x);
            return xBd.compareTo(BigDecimal.ZERO) == 0;
        } catch (Exception var2) {
            return false;
        }
    }

    public static boolean isPositiveNumber(Object x) {
        try {
            BigDecimal xBd = innerConvertDecimal(x);
            return xBd.compareTo(BigDecimal.ZERO) > 0;
        } catch (Exception var2) {
            return false;
        }
    }

    public static boolean isNegativeNumber(Object x) {
        try {
            BigDecimal xBd = innerConvertDecimal(x);
            return xBd.compareTo(BigDecimal.ZERO) < 0;
        } catch (Exception var2) {
            return false;
        }
    }

    public static String moveDecimalPoint(String source, int direction, int moveBit) {
        BigDecimal toNumber = toBigDecimal(source, (BigDecimal)null);
        if (toNumber != null && moveBit > 0) {
            boolean isNegativeNumber = toNumber.compareTo(BigDecimal.ZERO) < 0;
            String numberString = source.replace(".", "").replace("-", "");
            int count = numberString.length();
            int pointIndex = source.indexOf(".");
            if (pointIndex == -1) {
                pointIndex = count;
            }

            int resultPointIndex;
            String result;
            int addZeroCount;
            StringBuilder addZero;
            int i;
            if (direction == 1) {
                resultPointIndex = pointIndex - moveBit;
                if (resultPointIndex > 0) {
                    result = numberString.substring(0, resultPointIndex) + "." + numberString.substring(resultPointIndex, count);
                } else {
                    addZeroCount = Math.abs(resultPointIndex);
                    addZero = new StringBuilder();

                    for(i = 0; i < addZeroCount; ++i) {
                        addZero.append("0");
                    }

                    result = "0." + addZero.toString() + numberString;
                }

                if (isNegativeNumber) {
                    result = "-" + result;
                }

                return result;
            } else if (direction != 2) {
                return source;
            } else {
                resultPointIndex = pointIndex + moveBit;
                if (resultPointIndex >= count) {
                    addZeroCount = resultPointIndex - count;
                    addZero = new StringBuilder();

                    for(i = 0; i < addZeroCount; ++i) {
                        addZero.append("0");
                    }

                    result = numberString + addZero.toString();
                } else {
                    result = numberString.substring(0, resultPointIndex) + "." + numberString.substring(resultPointIndex, count);
                }

                if (isNegativeNumber) {
                    result = "-" + result;
                }

                return result;
            }
        } else {
            return source;
        }
    }
}
