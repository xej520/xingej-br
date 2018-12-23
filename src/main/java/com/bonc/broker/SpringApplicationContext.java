/*
 * 文件名：SpringApplicationContext.java
 * 版权：Copyright by www.bonc.com.cn
 * 描述：
 * 修改人：ke_wang
 * 修改时间：2016年10月8日
 * 跟踪单号：
 * 修改单号：
 * 修改内容：
 */

package com.bonc.broker;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * SpringApplicationContext
 *
 * @author ke_wang
 * @version 2016年10月8日
 * @see SpringApplicationContext
 * @since
 */
@Component
public class SpringApplicationContext implements ApplicationContextAware {
    /**
     *
     */
    public static ApplicationContext CONTEXT;

    @Override
    public void setApplicationContext(final ApplicationContext context) throws BeansException {
        CONTEXT = context;
    }


    public static <T> T getBean(Class<T> requiredType) {
        return CONTEXT.getBean(requiredType);
    }

    // 通过name获取 Bean.
    public static Object getBean(String name) {
        Object aObject = CONTEXT.getBean(name);
        return aObject;
    }
}
