package com.ddd.common.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 * 提供常用的字符串操作
 *
 * @author anthem37
 * @date 2025/8/15 16:00:00
 */
public final class StringUtils {

    private StringUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 判断字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串是否为空白
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * 判断字符串是否不为空白
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 安全的字符串比较
     */
    public static boolean equals(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equals(str2);
    }

    /**
     * 忽略大小写的字符串比较
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equalsIgnoreCase(str2);
    }

    /**
     * 字符串默认值
     */
    public static String defaultIfEmpty(String str, String defaultStr) {
        return isEmpty(str) ? defaultStr : str;
    }

    /**
     * 字符串默认值（空白）
     */
    public static String defaultIfBlank(String str, String defaultStr) {
        return isBlank(str) ? defaultStr : str;
    }

    /**
     * 首字母大写
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 首字母小写
     */
    public static String uncapitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * 驼峰转下划线
     */
    public static String camelToUnderscore(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.replaceAll("([A-Z])", "_$1").toLowerCase();
    }

    /**
     * 下划线转驼峰
     */
    public static String underscoreToCamel(String str) {
        if (isEmpty(str)) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        String[] parts = str.split("_");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (i == 0) {
                result.append(part.toLowerCase());
            } else {
                result.append(capitalize(part.toLowerCase()));
            }
        }
        return result.toString();
    }

    /**
     * 字符串连接
     */
    public static String join(String delimiter, String... elements) {
        if (elements == null || elements.length == 0) {
            return "";
        }
        return String.join(delimiter, elements);
    }

    /**
     * 集合连接为字符串
     */
    public static String join(String delimiter, Collection<String> elements) {
        if (CollectionUtils.isEmpty(elements)) {
            return "";
        }
        return String.join(delimiter, elements);
    }

    /**
     * 字符串分割
     */
    public static String[] split(String str, String delimiter) {
        if (isEmpty(str)) {
            return new String[0];
        }
        return str.split(Pattern.quote(delimiter));
    }

    /**
     * 去除前后空白
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 安全的子字符串
     */
    public static String substring(String str, int start, int end) {
        if (str == null) {
            return null;
        }
        if (start < 0) {
            start = 0;
        }
        if (end > str.length()) {
            end = str.length();
        }
        if (start > end) {
            return "";
        }
        return str.substring(start, end);
    }

    /**
     * 字符串包含检查
     */
    public static boolean contains(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        return str.contains(searchStr);
    }

    /**
     * 忽略大小写的包含检查
     */
    public static boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        return str.toLowerCase().contains(searchStr.toLowerCase());
    }

    /**
     * 字符串重复
     */
    public static String repeat(String str, int count) {
        if (str == null || count <= 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append(str);
        }
        return result.toString();
    }

    /**
     * 左填充
     */
    public static String leftPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }
        int padLen = size - str.length();
        if (padLen <= 0) {
            return str;
        }
        return repeat(String.valueOf(padChar), padLen) + str;
    }

    /**
     * 右填充
     */
    public static String rightPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }
        int padLen = size - str.length();
        if (padLen <= 0) {
            return str;
        }
        return str + repeat(String.valueOf(padChar), padLen);
    }

    /**
     * 掩码处理（如手机号、身份证等敏感信息）
     */
    public static String mask(String str, int start, int end, char maskChar) {
        if (isEmpty(str)) {
            return str;
        }
        if (start < 0) {
            start = 0;
        }
        if (end > str.length()) {
            end = str.length();
        }
        if (start >= end) {
            return str;
        }
        
        StringBuilder result = new StringBuilder();
        result.append(str.substring(0, start));
        result.append(repeat(String.valueOf(maskChar), end - start));
        result.append(str.substring(end));
        return result.toString();
    }
}