package com.bonc.broker.common;

import com.alibaba.fastjson.JSON;
import com.bonc.broker.SpringApplicationContext;
import com.bonc.broker.service.BaseExecuteThread;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author xingej
 */
@Component
public class ExecuteHelper {
	private static Logger logger = LoggerFactory.getLogger(ExecuteHelper.class);

	private static final ThreadFactory NAMED_THREADFACTORY = new ThreadFactoryBuilder().setNameFormat("bonc-broker-worker-%d").build();
	private static final ExecutorService POOL = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), NAMED_THREADFACTORY);

	public static void addPool(String appType, String optWorker, Map<String, String> params) {

		BaseExecuteThread<Map<String, String>> baseExecuteThread = null;

		logger.info("appType:" + appType);
		logger.info("optWorker:" + optWorker);
		logger.info("params map:" + JSON.toJSONString(params));

		String bean = GlobalHelp.getFullBeanPath(appType, optWorker);
		try {
			baseExecuteThread = (BaseExecuteThread<Map<String, String>>) SpringApplicationContext.getBean(Class.forName(bean));
			baseExecuteThread.setData(params);
		} catch (Exception e) {
			logger.error("实例化bean失败！", e);
			return;
		}
		logger.info("提交到线程池");
		POOL.submit(baseExecuteThread);
	}


	/**
	 * @param map
	 * @return
	 */
	private Map<String, String> multiplyUnitConfigMap(Map<String, String> map) {

		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (entry.getKey().equals("interactive_timeout")) {
				entry.setValue(String.valueOf(Integer.parseInt(entry.getValue()) * 60));
			}
			if (entry.getKey().equals("wait_timeout")) {
				entry.setValue(String.valueOf(Integer.parseInt(entry.getValue()) * 60));
			}
			if (entry.getKey().equals("lock_wait_timeout")) {
				entry.setValue(String.valueOf(Integer.parseInt(entry.getValue()) * 3600));
			}
		}
		return map;

	}


}
