package com.tongtech.common.utils;

/**
 * 用于对比两个值是否相同。
 */
public class CompareUtils {

    public static boolean notEquals(String a, String b) {
        return !isEquals(a, b);
    }

    public static boolean isEquals(String a, String b) {
        if(a == null) {
            return (b == null) ? true : false;
        }
        else {
            return a.equals(b);
        }
    }

    public static boolean notEquals(Integer a, Integer b) {
        return !isEquals(a, b);
    }

    public static boolean isEquals(Integer a, Integer b) {
        if(a == null) {
            return (b == null) ? true : false;
        }
        else {
            return a.equals(b);
        }
    }

    public static boolean notEquals(Long a, Long b) {
        return !isEquals(a, b);
    }

    public static boolean isEquals(Long a, Long b) {
        if(a == null) {
            return (b == null) ? true : false;
        }
        else {
            return a.equals(b);
        }
    }

    public static boolean notEquals(Boolean a, Boolean b) {
        return !isEquals(a, b);
    }

    public static boolean isEquals(Boolean a, Boolean b) {
        if(a == null) {
            return (b == null) ? true : false;
        }
        else {
            return a.equals(b);
        }
    }
}
