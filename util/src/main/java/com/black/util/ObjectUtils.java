package com.black.util;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

public class ObjectUtils {
    public static final Null NULL = new Null();

    public ObjectUtils() {
    }

    public static <T> T defaultIfNull(T object, T defaultValue) {
        return object != null ? object : defaultValue;
    }

    public static <T> T firstNonNull(T... values) {
        if (values != null) {
            Object[] var1 = values;
            int var2 = values.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                T val = (T) var1[var3];
                if (val != null) {
                    return val;
                }
            }
        }

        return null;
    }

    /** @deprecated */
    @Deprecated
    public static boolean equals(Object object1, Object object2) {
        if (object1 == object2) {
            return true;
        } else {
            return object1 != null && object2 != null ? object1.equals(object2) : false;
        }
    }

    public static boolean notEqual(Object object1, Object object2) {
        return !equals(object1, object2);
    }

    /** @deprecated */
    @Deprecated
    public static int hashCode(Object obj) {
        return obj == null ? 0 : obj.hashCode();
    }

    /** @deprecated */
    @Deprecated
    public static int hashCodeMulti(Object... objects) {
        int hash = 1;
        if (objects != null) {
            Object[] var2 = objects;
            int var3 = objects.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                Object object = var2[var4];
                int tmpHash = hashCode(object);
                hash = hash * 31 + tmpHash;
            }
        }

        return hash;
    }

    public static String identityToString(Object object) {
        if (object == null) {
            return null;
        } else {
            StringBuilder builder = new StringBuilder();
            identityToString(builder, object);
            return builder.toString();
        }
    }

    public static void identityToString(Appendable appendable, Object object) throws IOException {
        if (object == null) {
            throw new NullPointerException("Cannot get the toString of a null identity");
        } else {
            appendable.append(object.getClass().getName()).append('@').append(Integer.toHexString(System.identityHashCode(object)));
        }
    }

    public static void identityToString(StringBuffer buffer, Object object) {
        if (object == null) {
            throw new NullPointerException("Cannot get the toString of a null identity");
        } else {
            buffer.append(object.getClass().getName()).append('@').append(Integer.toHexString(System.identityHashCode(object)));
        }
    }

    public static void identityToString(StringBuilder builder, Object object) {
        if (object == null) {
            throw new NullPointerException("Cannot get the toString of a null identity");
        } else {
            builder.append(object.getClass().getName()).append('@').append(Integer.toHexString(System.identityHashCode(object)));
        }
    }

    /** @deprecated */
    @Deprecated
    public static String toString(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    /** @deprecated */
    @Deprecated
    public static String toString(Object obj, String nullStr) {
        return obj == null ? nullStr : obj.toString();
    }

    public static <T extends Comparable<? super T>> T min(T... values) {
        T result = null;
        if (values != null) {
            Comparable[] var2 = values;
            int var3 = values.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                T value = (T) var2[var4];
                if (compare(value, result, true) < 0) {
                    result = value;
                }
            }
        }

        return result;
    }

    public static <T extends Comparable<? super T>> T max(T... values) {
        T result = null;
        if (values != null) {
            Comparable[] var2 = values;
            int var3 = values.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                T value = (T) var2[var4];
                if (compare(value, result, false) > 0) {
                    result = value;
                }
            }
        }

        return result;
    }

    public static <T extends Comparable<? super T>> int compare(T c1, T c2) {
        return compare(c1, c2, false);
    }

    public static <T extends Comparable<? super T>> int compare(T c1, T c2, boolean nullGreater) {
        if (c1 == c2) {
            return 0;
        } else if (c1 == null) {
            return nullGreater ? 1 : -1;
        } else if (c2 == null) {
            return nullGreater ? -1 : 1;
        } else {
            return c1.compareTo(c2);
        }
    }

//    public static <T extends Comparable<? super T>> T median(T... items) {
//        Validate.notEmpty(items);
//        Validate.noNullElements(items);
//        TreeSet<T> sort = new TreeSet();
//        Collections.addAll(sort, items);
//        T result = (Comparable)sort.toArray()[(sort.size() - 1) / 2];
//        return result;
//    }
//
//    public static <T> T median(Comparator<T> comparator, T... items) {
//        Validate.notEmpty(items, "null/empty items", new Object[0]);
//        Validate.noNullElements(items);
//        Validate.notNull(comparator, "null comparator", new Object[0]);
//        TreeSet<T> sort = new TreeSet(comparator);
//        Collections.addAll(sort, items);
//        T result = sort.toArray()[(sort.size() - 1) / 2];
//        return result;
//    }

    public static <T> T clone(T obj) {
        if (!(obj instanceof Cloneable)) {
            return null;
        } else {
            Object result;
            if (obj.getClass().isArray()) {
                Class<?> componentType = obj.getClass().getComponentType();
                if (!componentType.isPrimitive()) {
                    result = ((Object[])obj).clone();
                } else {
                    int length = Array.getLength(obj);
                    result = Array.newInstance(componentType, length);

                    while(length-- > 0) {
                        Array.set(result, length, Array.get(obj, length));
                    }
                }
            } else {
                try {
                    Method clone = obj.getClass().getMethod("clone");
                    result = clone.invoke(obj);
                } catch (NoSuchMethodException var4) {
                    throw new RuntimeException("Cloneable type " + obj.getClass().getName() + " has no clone method", var4);
                } catch (IllegalAccessException var5) {
                    throw new RuntimeException("Cannot clone Cloneable type " + obj.getClass().getName(), var5);
                } catch (InvocationTargetException var6) {
                    throw new RuntimeException("Exception cloning Cloneable type " + obj.getClass().getName(), var6.getCause());
                }
            }

            return (T) result;
        }
    }

    public static <T> T cloneIfPossible(T obj) {
        T clone = clone(obj);
        return clone == null ? obj : clone;
    }

    public static boolean CONST(boolean v) {
        return v;
    }

    public static byte CONST(byte v) {
        return v;
    }

    public static byte CONST_BYTE(int v) throws IllegalArgumentException {
        if (v >= -128 && v <= 127) {
            return (byte)v;
        } else {
            throw new IllegalArgumentException("Supplied value must be a valid byte literal between -128 and 127: [" + v + "]");
        }
    }

    public static char CONST(char v) {
        return v;
    }

    public static short CONST(short v) {
        return v;
    }

    public static short CONST_SHORT(int v) throws IllegalArgumentException {
        if (v >= -32768 && v <= 32767) {
            return (short)v;
        } else {
            throw new IllegalArgumentException("Supplied value must be a valid byte literal between -32768 and 32767: [" + v + "]");
        }
    }

    public static int CONST(int v) {
        return v;
    }

    public static long CONST(long v) {
        return v;
    }

    public static float CONST(float v) {
        return v;
    }

    public static double CONST(double v) {
        return v;
    }

    public static <T> T CONST(T v) {
        return v;
    }

    public static class Null implements Serializable {
        private static final long serialVersionUID = 7092611880189329093L;

        Null() {
        }

        private Object readResolve() {
            return ObjectUtils.NULL;
        }
    }
}