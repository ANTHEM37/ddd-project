package io.github.anthem37.ddd.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 反射工具类
 * 提供常用的反射操作
 *
 * @author anthem37
 * @date 2025/8/15 16:00:00
 */
public final class ReflectionUtils {

    private ReflectionUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 获取泛型参数类型
     *
     * @param clazz 类
     * @param index 泛型参数索引
     * @return 泛型参数类型
     */
    public static Class<?> getGenericType(Class<?> clazz, int index) {
        Type genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length > index) {
                return (Class<?>) actualTypeArguments[index];
            }
        }
        return Object.class;
    }

    /**
     * 获取接口的泛型参数类型
     *
     * @param clazz         实现类
     * @param interfaceType 接口类型
     * @param index         泛型参数索引
     * @return 泛型参数类型
     */
    public static Class<?> getInterfaceGenericType(Class<?> clazz, Class<?> interfaceType, int index) {
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                if (parameterizedType.getRawType().equals(interfaceType)) {
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    if (actualTypeArguments.length > index) {
                        return (Class<?>) actualTypeArguments[index];
                    }
                }
            }
        }
        return Object.class;
    }

    /**
     * 获取所有字段（包括父类）
     *
     * @param clazz 类
     * @return 所有字段列表
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = clazz;

        while (currentClass != null && currentClass != Object.class) {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }

        return fields;
    }

    /**
     * 获取字段值
     *
     * @param object    对象
     * @param fieldName 字段名
     * @return 字段值
     */
    public static Object getFieldValue(Object object, String fieldName) {
        try {
            Field field = findField(object.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                return field.get(object);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("获取字段值失败: " + fieldName, e);
        }
        return null;
    }

    /**
     * 设置字段值
     *
     * @param object    对象
     * @param fieldName 字段名
     * @param value     字段值
     */
    public static void setFieldValue(Object object, String fieldName, Object value) {
        try {
            Field field = findField(object.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(object, value);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("设置字段值失败: " + fieldName, e);
        }
    }

    /**
     * 查找字段
     *
     * @param clazz     类
     * @param fieldName 字段名
     * @return 字段对象
     */
    public static Field findField(Class<?> clazz, String fieldName) {
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 调用方法
     *
     * @param object     对象
     * @param methodName 方法名
     * @param args       参数
     * @return 方法返回值
     */
    public static Object invokeMethod(Object object, String methodName, Object... args) {
        try {
            Class<?>[] paramTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i].getClass();
            }

            Method method = findMethod(object.getClass(), methodName, paramTypes);
            if (method != null) {
                method.setAccessible(true);
                return method.invoke(object, args);
            }
        } catch (Exception e) {
            throw new RuntimeException("调用方法失败: " + methodName, e);
        }
        return null;
    }

    /**
     * 查找方法
     *
     * @param clazz      类
     * @param methodName 方法名
     * @param paramTypes 参数类型
     * @return 方法对象
     */
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            try {
                return currentClass.getDeclaredMethod(methodName, paramTypes);
            } catch (NoSuchMethodException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }
}