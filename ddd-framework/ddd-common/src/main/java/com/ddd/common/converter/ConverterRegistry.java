package com.ddd.common.converter;

import lombok.extern.slf4j.Slf4j;

/**
 * 转换器注册中心
 * 参考DomainEventPublisher的设计模式，由基础设施层提供具体实现
 *
 * @author anthem37
 * @date 2025/8/19 11:00:00
 */
@Slf4j
public class ConverterRegistry {

    private static ConverterManager converterManager;

    /**
     * 设置转换器管理器实现
     */
    public static void setConverterManager(ConverterManager manager) {
        ConverterRegistry.converterManager = manager;
    }

    /**
     * 注册转换器
     *
     * @param key       转换器键值
     * @param converter 转换器实例
     */
    public static void register(String key, Object converter) {
        if (converterManager != null) {
            converterManager.register(key, converter);
            return;
        }
        log.debug("未设置转换器管理器，转换器注册将被忽略: {}", key);
    }

    /**
     * 获取转换器
     *
     * @param key  转换器键值
     * @param type 转换器类型
     * @param <T>  转换器类型泛型
     * @return 转换器实例
     */
    public static <T> T getConverter(String key, Class<T> type) {
        if (converterManager != null) {
            return converterManager.getConverter(key, type);
        }
        log.debug("未设置转换器管理器，无法获取转换器: {}", key);
        return null;
    }

    /**
     * 根据类型获取转换器
     *
     * @param converterClass 转换器类型
     * @param <T>            转换器类型
     * @return 转换器实例
     */
    public static <T> T getConverter(Class<T> converterClass) {
        if (converterManager != null) {
            return converterManager.getConverter(converterClass);
        }
        log.debug("未设置转换器管理器，无法获取转换器: {}", converterClass.getSimpleName());
        return null;
    }

    /**
     * 移除转换器
     *
     * @param key 转换器键值
     */
    public static void remove(String key) {
        if (converterManager != null) {
            converterManager.remove(key);
        }
    }

    /**
     * 检查转换器是否存在
     *
     * @param key 转换器键值
     * @return 是否存在
     */
    public static boolean contains(String key) {
        if (converterManager != null) {
            return converterManager.contains(key);
        }
        return false;
    }

    /**
     * 转换器管理器接口（实现类需要把自己注册到ConverterRegistry）
     * 由基础设施层实现，可以利用Spring容器的能力
     */
    public interface ConverterManager {

        /**
         * 注册转换器
         *
         * @param key       转换器键值
         * @param converter 转换器实例
         */
        void register(String key, Object converter);

        /**
         * 获取转换器
         *
         * @param key  转换器键值
         * @param type 转换器类型
         * @param <T>  转换器类型泛型
         * @return 转换器实例
         */
        <T> T getConverter(String key, Class<T> type);

        /**
         * 根据类型获取转换器（利用Spring容器能力）
         *
         * @param converterClass 转换器类型
         * @param <T>            转换器类型
         * @return 转换器实例
         */
        <T> T getConverter(Class<T> converterClass);

        /**
         * 移除转换器
         *
         * @param key 转换器键值
         */
        void remove(String key);

        /**
         * 检查转换器是否存在
         *
         * @param key 转换器键值
         * @return 是否存在
         */
        boolean contains(String key);
    }
}
