package com.bonc.broker.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BrokerCfg {
    private static Logger logger = LoggerFactory.getLogger(BrokerCfg.class);
    public static String MASTER;

    // 避免出现多线程问题，添加的锁
    private static final Object MASTER_URL_LOCK = new Object();

    @Value("${master.url}")
    private void setMASTER(String master_url) {
        synchronized (MASTER_URL_LOCK) {
            MASTER = master_url;
            logger.info("----->masterURL:\t" + MASTER);
        }
    }


}
