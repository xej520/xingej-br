package com.bonc.broker.service;

import com.alibaba.fastjson.JSONObject;

/**
 * @author xingej
 */
public interface IBindingWorker {
	/**
	 * 构建绑定信息
	 * @param instanceId
	 * @return
	 */
	JSONObject buildBindingInfo(String instanceId);
}
