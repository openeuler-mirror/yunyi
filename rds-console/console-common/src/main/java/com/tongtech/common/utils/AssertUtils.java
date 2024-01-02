package com.tongtech.common.utils;

import com.tongtech.common.exception.ServiceException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 断言工具类 方便快速判空 并抛出异常
 */
public class AssertUtils {
    /**
     * 非空对象
     *
     * @param object          判空对象
     * @param exceptionString 抛出异常所携带的信息
     */
    public static void ObjectIsNull(@NotNull Object object, String exceptionString) {
        if (object == null)
            throw new ServiceException(exceptionString);
    }

    /**
     * String 类型判断空 并抛出异常
     *
     * @param string          String对象
     * @param exceptionString 抛出异常的信息
     */
    public static void StringIsNull(@NotNull @NotBlank String string, String exceptionString) {
        if (StringUtils.isEmpty(string))
            throw new ServiceException(exceptionString);
    }

    /**
     * String 类型判断空 并抛出异常
     *
     * @param list            list 对象
     * @param exceptionString 抛出异常的信息
     */
    public static void ListIsNullOrSizeZero(@NotNull List list, String exceptionString) {
        if (list == null || list.size() == 0)
            throw new ServiceException(exceptionString);
    }

    public static void IntegerValueGreaterZero(Integer integer, String exceptionString) {
        if (integer <= 0)
            throw new ServiceException(exceptionString);
    }
}
