package com.ddd.common.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.Optional;

/**
 * 泛型类型解析工具
 * 用于查找泛型接口的实现类
 *
 * @author anthem37
 * @date 2025/8/14 11:05:23
 */
@Slf4j
public class GenericTypeResolver {

    /**
     * 查找实现了指定泛型接口的实现类
     *
     * @param implementations 所有可能的实现类集合
     * @param interfaceClass  泛型接口类
     * @param targetType      目标类型（泛型参数）
     * @param <T>             接口类型
     * @param <R>             实现类类型
     * @return 匹配的实现类，如果没有找到则返回空
     */
    public static <T, R extends T> Optional<R> findImplementation(
            Collection<R> implementations,
            Class<T> interfaceClass,
            Class<?> targetType) {

        log.debug("查找 {} 接口的实现，目标类型: {}", interfaceClass.getSimpleName(), targetType.getSimpleName());

        for (R implementation : implementations) {
            if (isImplementationFor(implementation, interfaceClass, targetType)) {
                log.debug("找到匹配的实现: {}", implementation.getClass().getSimpleName());
                return Optional.of(implementation);
            }
        }

        log.debug("未找到匹配的实现");
        return Optional.empty();
    }

    /**
     * 检查实现类是否匹配指定的接口和目标类型
     *
     * @param implementation 实现类实例
     * @param interfaceClass 接口类
     * @param targetType     目标类型
     * @return 是否匹配
     */
    private static boolean isImplementationFor(Object implementation, Class<?> interfaceClass, Class<?> targetType) {
        Class<?> implClass = implementation.getClass();

        // 检查当前类的接口
        if (checkInterfaces(implClass, interfaceClass, targetType)) {
            return true;
        }

        // 检查当前类的父类
        return checkSuperClass(implClass, interfaceClass, targetType);
    }

    /**
     * 检查类的接口是否匹配
     *
     * @param clazz          要检查的类
     * @param interfaceClass 接口类
     * @param targetType     目标类型
     * @return 是否匹配
     */
    private static boolean checkInterfaces(Class<?> clazz, Class<?> interfaceClass, Class<?> targetType) {
        // 检查直接实现的接口
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                if (parameterizedType.getRawType().equals(interfaceClass)) {
                    if (checkTypeArguments(parameterizedType.getActualTypeArguments(), targetType)) {
                        return true;
                    }
                }
            }

            // 检查接口的父接口
            if (genericInterface instanceof Class) {
                Class<?> interfaceType = (Class<?>) genericInterface;
                if (checkInterfaces(interfaceType, interfaceClass, targetType)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 检查类的父类是否匹配
     *
     * @param clazz          要检查的类
     * @param interfaceClass 接口类
     * @param targetType     目标类型
     * @return 是否匹配
     */
    private static boolean checkSuperClass(Class<?> clazz, Class<?> interfaceClass, Class<?> targetType) {
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            // 检查父类是否直接实现了接口
            Type genericSuperClass = clazz.getGenericSuperclass();
            if (genericSuperClass instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericSuperClass;
                if (parameterizedType.getRawType().equals(interfaceClass)) {
                    if (checkTypeArguments(parameterizedType.getActualTypeArguments(), targetType)) {
                        return true;
                    }
                }
            }

            // 递归检查父类
            try {
                if (isImplementationFor(superClass.newInstance(), interfaceClass, targetType)) {
                    return true;
                }
            } catch (InstantiationException | IllegalAccessException e) {
                // 如果无法实例化父类，则检查父类的接口
                return checkInterfaces(superClass, interfaceClass, targetType);
            }
        }

        return false;
    }

    /**
     * 检查类型参数是否匹配目标类型
     *
     * @param typeArguments 类型参数数组
     * @param targetType    目标类型
     * @return 是否匹配
     */
    private static boolean checkTypeArguments(Type[] typeArguments, Class<?> targetType) {
        if (typeArguments.length > 0) {
            Type typeArg = typeArguments[0];

            // 直接类型匹配
            if (typeArg.equals(targetType)) {
                return true;
            }

            // 类型是Class且是目标类型的父类或接口
            if (typeArg instanceof Class) {
                Class<?> typeArgClass = (Class<?>) typeArg;
                if (typeArgClass.isAssignableFrom(targetType)) {
                    return true;
                }
            }

            // 通配符类型匹配
            if (typeArg instanceof WildcardType) {
                WildcardType wildcardType = (WildcardType) typeArg;

                // 检查上界
                Type[] upperBounds = wildcardType.getUpperBounds();
                for (Type upperBound : upperBounds) {
                    if (upperBound instanceof Class) {
                        Class<?> upperBoundClass = (Class<?>) upperBound;
                        if (upperBoundClass.isAssignableFrom(targetType)) {
                            return true;
                        }
                    }
                }

                // 检查下界
                Type[] lowerBounds = wildcardType.getLowerBounds();
                for (Type lowerBound : lowerBounds) {
                    if (lowerBound instanceof Class) {
                        Class<?> lowerBoundClass = (Class<?>) lowerBound;
                        if (targetType.isAssignableFrom(lowerBoundClass)) {
                            return true;
                        }
                    }
                }
            }

            // 参数化类型匹配
            if (typeArg instanceof ParameterizedType) {
                ParameterizedType parameterizedTypeArg = (ParameterizedType) typeArg;
                Type rawType = parameterizedTypeArg.getRawType();
                if (rawType instanceof Class) {
                    Class<?> rawTypeClass = (Class<?>) rawType;
                    return rawTypeClass.isAssignableFrom(targetType);
                }
            }
        }

        return false;
    }
}
