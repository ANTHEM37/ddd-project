package com.ddd.common.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 集合工具类
 * 提供常用的集合操作
 *
 * @author anthem37
 * @date 2025/8/15 16:00:00
 */
public final class CollectionUtils {

    private CollectionUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 判断集合是否为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断集合是否不为空
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * 判断Map是否为空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 判断Map是否不为空
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 安全获取集合大小
     */
    public static int size(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    /**
     * 安全获取Map大小
     */
    public static int size(Map<?, ?> map) {
        return map == null ? 0 : map.size();
    }

    /**
     * 集合转换
     */
    public static <T, R> List<R> map(Collection<T> collection, Function<T, R> mapper) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        }
        return collection.stream().map(mapper).collect(Collectors.toList());
    }

    /**
     * 集合过滤
     */
    public static <T> List<T> filter(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        }
        return collection.stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * 集合分组
     */
    public static <T, K> Map<K, List<T>> groupBy(Collection<T> collection, Function<T, K> classifier) {
        if (isEmpty(collection)) {
            return new HashMap<>();
        }
        return collection.stream().collect(Collectors.groupingBy(classifier));
    }

    /**
     * 查找第一个匹配的元素
     */
    public static <T> Optional<T> findFirst(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection)) {
            return Optional.empty();
        }
        return collection.stream().filter(predicate).findFirst();
    }

    /**
     * 检查是否存在匹配的元素
     */
    public static <T> boolean anyMatch(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection)) {
            return false;
        }
        return collection.stream().anyMatch(predicate);
    }

    /**
     * 检查是否所有元素都匹配
     */
    public static <T> boolean allMatch(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection)) {
            return true;
        }
        return collection.stream().allMatch(predicate);
    }

    /**
     * 集合去重
     */
    public static <T> List<T> distinct(Collection<T> collection) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        }
        return collection.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 集合排序
     */
    public static <T extends Comparable<T>> List<T> sort(Collection<T> collection) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        }
        return collection.stream().sorted().collect(Collectors.toList());
    }

    /**
     * 集合排序（自定义比较器）
     */
    public static <T> List<T> sort(Collection<T> collection, Comparator<T> comparator) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        }
        return collection.stream().sorted(comparator).collect(Collectors.toList());
    }

    /**
     * 安全创建ArrayList
     */
    public static <T> List<T> safeList(Collection<T> collection) {
        return collection == null ? new ArrayList<>() : new ArrayList<>(collection);
    }

    /**
     * 安全创建HashSet
     */
    public static <T> Set<T> safeSet(Collection<T> collection) {
        return collection == null ? new HashSet<>() : new HashSet<>(collection);
    }

    /**
     * 分页获取数据
     */
    public static <T> List<T> page(List<T> list, int pageNum, int pageSize) {
        if (isEmpty(list) || pageNum < 1 || pageSize < 1) {
            return new ArrayList<>();
        }
        
        int fromIndex = (pageNum - 1) * pageSize;
        if (fromIndex >= list.size()) {
            return new ArrayList<>();
        }
        
        int toIndex = Math.min(fromIndex + pageSize, list.size());
        return list.subList(fromIndex, toIndex);
    }
}