package com.bonc.broker.service.redis.base;

import com.alibaba.fastjson.JSONObject;
import com.bonc.broker.common.Global;
import com.bonc.broker.common.GlobalHelp;
import com.bonc.broker.common.K8sClient;
import com.bonc.broker.controller.mode.BindingRedisOutK8s;
import com.bonc.broker.controller.mode.DashboardUrl;
import com.bonc.broker.entity.ServiceInstance;
import com.bonc.broker.service.DaoService;
import com.bonc.broker.service.IBindingWorker;
import com.bonc.broker.service.model.crd.redis.DoneableRedis;
import com.bonc.broker.service.model.crd.redis.RedisList;
import com.bonc.broker.service.model.redis.BindingNode;
import com.bonc.broker.service.model.redis.RedisCluster;
import com.bonc.broker.service.model.redis.ServiceStatus;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xingej
 */
@Service
public class BindingWorkerImpl implements IBindingWorker {
	/**
	 * 日志记录
	 */
	private static Logger logger = LoggerFactory.getLogger(BindingWorkerImpl.class);

	private MixedOperation<RedisCluster, RedisList, DoneableRedis, Resource<RedisCluster, DoneableRedis>> k8sClientForRedis = K8sClient
			.getK8sClientForRedis();
	@Autowired
	private DaoService daoService;


	@Override
	public JSONObject buildBindingInfo(String instanceId) {
		// 1. 获取instance_id对应的redisCluster对象
		ServiceInstance serviceInstance = daoService.getServiceInstance(instanceId);
		if (null == serviceInstance) {
			logger.error("[buildBindingInfoForRedis]--->get serviceInstance failed" + "instanceId:\t" + instanceId);
			return null;
		}

		logger.info("[buildBindingInfoForRedis]--instanceID:\t" + instanceId + "\t" + JSONObject.toJSONString(serviceInstance));

		RedisCluster redisCluster = null;
		try {
			redisCluster = k8sClientForRedis.inNamespace(serviceInstance.getTenantId())
					.withName(serviceInstance.getServiceName()).get();
		} catch (Exception e) {
			logger.error("[buildBindingInfoForRedis]--->error:\n" + e.getMessage());
		}

		// 2. 转换成json格式
		logger.info("[buildBindingInfoForRedis]---get redisCluster from k8s ok!---instanceID:\t" + instanceId + "\tserviceName:\t" + serviceInstance.getServiceName());

		JSONObject credentials = buildCredentials(redisCluster);
		logger.info("[buildBindingInfoForRedis]---build credentials info:\n" + credentials.toJSONString());

		// 3. 返回
		return credentials;
	}

	private JSONObject buildCredentials(RedisCluster redisCluster) {

		JSONObject bindingObject = new JSONObject();
		JSONObject credentialsObject = new JSONObject();

		String password = redisCluster.getSpec().getPassword();

		Map<String, ServiceStatus> services = redisCluster.getStatus().getServices();
		Map<String, String> portAll = new HashMap<>(16);
		for (Map.Entry<String, ServiceStatus> entry : services.entrySet()) {
			portAll.put(entry.getValue().getRole(), String.valueOf(entry.getValue().getNodePort()));
		}
		logger.info("[buildCredentials]---build credentials info、port:\n" + JSONObject.toJSONString(portAll));
		Map<String, BindingNode> serverNodes = redisCluster.getStatus().getBindings();
		for (Map.Entry<String, BindingNode> entry : serverNodes.entrySet()) {
			JSONObject nodeObject = new JSONObject();
			BindingRedisOutK8s bindingRedisOutK8s = new BindingRedisOutK8s();

			bindingRedisOutK8s.setHost(entry.getValue().getBindIp());
			bindingRedisOutK8s.setPassword(password);
			String role = entry.getValue().getRole();
			for (Map.Entry<String, String> port : portAll.entrySet()) {
				if (port.getKey().equalsIgnoreCase(role)) {
					bindingRedisOutK8s.setPort(port.getValue());
					break;
				}
			}
			bindingRedisOutK8s.setRole(role);

			nodeObject.put("outside_k8s", bindingRedisOutK8s);
			credentialsObject.put(entry.getKey(), nodeObject);
		}

		List<DashboardUrl> redisDashboardUrl = GlobalHelp.buildDashBoardUrlObject(GlobalHelp.buildDashBoardUrl(Global.REDIS, redisCluster.getMetadata().getName()), "redis dashboard url");
		credentialsObject.put("dashboard_urls", redisDashboardUrl);

		bindingObject.put("credentials", credentialsObject);
		bindingObject.put("syslog_drain_url", null);
		bindingObject.put("route_service_url", null);
		bindingObject.put("volume_mounts", null);

		return bindingObject;
	}

}
