package com.ddd.infrastructure.converter;

import com.ddd.common.converter.ConverterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Spring框架实现的转换器管理器
 * 完全依赖Spring容器管理转换器Bean
 *
 * @author anthem37
 * @date 2025/8/19 11:00:00
 */
@Slf4j
public class SpringConverterManager implements ConverterRegistry.ConverterManager, ApplicationContextAware {

    private ApplicationContext applicationContext;

    public SpringConverterManager() {
        ConverterRegistry.setConverterManager(this);
        log.info("SpringConverterManager 已初始化");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void register(String key, Object converter) {
        log.warn("Spring环境下建议将转换器配置为Bean，忽略手动注册: {}", key);
    }

    @Override
    public <T> T getConverter(String key, Class<T> type) {
        if (key == null || type == null || applicationContext == null) {
            return null;
        }

        try {
            Object bean = applicationContext.getBean(key);
            return type.isInstance(bean) ? type.cast(bean) : null;
        } catch (BeansException e) {
            return null;
        }
    }

    @Override
    public <T> T getConverter(Class<T> converterClass) {
        if (converterClass == null || applicationContext == null) {
            return null;
        }

        try {
            return applicationContext.getBean(converterClass);
        } catch (BeansException e) {
            return null;
        }
    }

    @Override
    public void remove(String key) {
        log.warn("Spring环境下Bean生命周期由容器管理，不支持手动移除: {}", key);
    }

    @Override
    public boolean contains(String key) {
        return key != null && applicationContext != null && applicationContext.containsBean(key);
    }
}
