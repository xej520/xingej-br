package com.bonc.broker.service;

import com.alibaba.fastjson.JSONObject;
import com.bonc.broker.exception.BrokerException;

/**
 * @author xingej
 */
public interface ICheckParameters {
	/**
	 * 校验-- 创建实例参数
	 *
	 * @param appType
	 * @param tenantId
	 * @param planId
	 * @param configuration
	 * @throws BrokerException
	 */
	void checkCreateInstanceParameters(String appType, String tenantId, String planId, JSONObject configuration) throws BrokerException;

	/**
	 * 校验--更新实例参数
	 *
	 * @param instanceId
	 * @param parameters
	 * @throws BrokerException
	 */
	void checkUpdateInstanceParameters(String instanceId, JSONObject parameters) throws BrokerException;

}
