package com.bonc.broker.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bonc.broker.controller.mode.DashboardUrl;
import com.bonc.broker.exception.BrokerException;
import com.bonc.broker.service.BaseRequestBody;
import com.bonc.broker.service.BaseRequestBodyRedis;
import com.bonc.broker.service.model.base.MemoryCPU;
import com.bonc.broker.service.model.base.Resources;
import com.bonc.broker.util.PropertyUtil;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xingej
 */
public class GlobalHelp {
	private static Logger logger = LoggerFactory.getLogger(GlobalHelp.class);
	protected static KubernetesClient k8sClient = K8sClient.getK8sClient();

	/**
	 * 根据planID，来判断是mysql broker，还是redis broker
	 *
	 * @param planId
	 * @return
	 */
	public static String getCatalogType(String planId) {
		if (Global.PLAN_ID_MYSQL.contains(planId)) {
			return AppTypeConst.APPTYPE_MYSQL;
		}

		if (Global.PLAN_ID_REDIS.contains(planId)) {
			return AppTypeConst.APPTYPE_REDIS;
		}

		return null;
	}

	public static String getCatalogTypeByServiceId(String serviceId) {
		if (Global.SERVICE_ID_MYSQL.equals(serviceId)) {
			return AppTypeConst.APPTYPE_MYSQL;
		}

		if (Global.SERVICE_ID_REDIS.equals(serviceId)) {
			return AppTypeConst.APPTYPE_REDIS;
		}

		return null;
	}


	public static BaseRequestBody buildBaseRequestBody(String parameters) {
		BaseRequestBody baseRequestBody = new BaseRequestBody();

		JSONObject parametersJson = JSON.parseObject(parameters);

		String configuration = parametersJson.getString("configuration");

		JSONObject configurationJson = JSONObject.parseObject(configuration);

		// 0. 解析参数
		String version = configurationJson.getString("version");
		// type:MS,SINGLE,MM
		String type = configurationJson.getString("type");
		String serviceName = configurationJson.getString("serviceName");
		String password = configurationJson.getString("password");
		String cpu = configurationJson.getString("cpu");
		String memory = configurationJson.getString("memory");
		String capacity = configurationJson.getString("capacity");

		String replicas = configurationJson.getString("replicas");

		String tenantId = parametersJson.getString("tenant_id");
		String projectId = parametersJson.getString("project_id");
		String userId = parametersJson.getString("user_id");

		baseRequestBody.setCapacity(capacity);
		baseRequestBody.setCpu(cpu);
		baseRequestBody.setMemory(memory);
		baseRequestBody.setPassword(password);
		baseRequestBody.setReplicas(replicas);
		baseRequestBody.setServiceName(serviceName);
		baseRequestBody.setType(type);
		baseRequestBody.setVersion(version);

		baseRequestBody.setTenantId(tenantId);
		baseRequestBody.setProjectId(projectId);
		baseRequestBody.setUserId(userId);
		logger.info("----baseRequestBody---->\t" + JSONObject.toJSONString(baseRequestBody));

		return baseRequestBody;

	}

	public static BaseRequestBodyRedis buildBaseRequestBodyForRedis(String parameters) {

		BaseRequestBodyRedis baseRequestBody = new BaseRequestBodyRedis();

		JSONObject parametersJson = JSON.parseObject(parameters);
		String configuration = parametersJson.getString("configuration");
		JSONObject configurationJson = JSONObject.parseObject(configuration);

		// 0. 解析参数
		String serviceName = configurationJson.getString("serviceName");
		String version = configurationJson.getString("version");
		String password = configurationJson.getString("password");
		int replicas = configurationJson.getInteger("replicas");
		String type = configurationJson.getString("type");

		String cpu = configurationJson.getString("cpu");
		String memory = configurationJson.getString("memory");
		String capacity = configurationJson.getString("capacity");

		String sentinalCpu = configurationJson.getString("sentinelCPU");
		String sentinalMemory = configurationJson.getString("sentinelMemory");
		String sentinalNum = configurationJson.getString("sentinelNum");

		String tenantId = parametersJson.getString("tenant_id");
		String projectId = parametersJson.getString("project_id");
		String userId = parametersJson.getString("user_id");

		baseRequestBody.setServiceName(serviceName);
		baseRequestBody.setVersion(version);
		baseRequestBody.setPassword(password);
		baseRequestBody.setReplicas(replicas);

		baseRequestBody.setCpu(cpu);
		baseRequestBody.setMemory(memory);
		baseRequestBody.setCapacity(capacity);

		baseRequestBody.setType(type);

		if (null != sentinalCpu) {
			baseRequestBody.setSentinelCpu(sentinalCpu);
		}

		if (null != sentinalMemory) {
			baseRequestBody.setSentinelMemory(sentinalMemory);
		}

		if (null != sentinalNum) {
			baseRequestBody.setSentinelReplicas(sentinalNum);
		}

		baseRequestBody.setTenantId(tenantId);
		baseRequestBody.setProjectId(projectId);
		baseRequestBody.setUserId(userId);

		return baseRequestBody;
	}

	public static Resources getResources(String cpu, String memory, String memoryUnit) {
		Resources resources = new Resources();

		MemoryCPU requests = new MemoryCPU();
		MemoryCPU limits = new MemoryCPU();

		requests.setCpu(String.valueOf(Math.floor(Float.parseFloat(cpu) / 4)));
		requests.setMemory(Math.floor(Float.parseFloat(memory) / 2) + memoryUnit);

		limits.setCpu(cpu);
		limits.setMemory(memory + memoryUnit);

		resources.setRequests(requests);
		resources.setLimits(limits);
		return resources;
	}

	public static Namespace buildNamespace(String tenantId) throws BrokerException {
		Namespace ns;
		try {
			ns = k8sClient.namespaces().withName(tenantId).get();
			if (null == ns) {
				ns = k8sClient.namespaces().createNew().withNewMetadata().withName(tenantId).endMetadata()
						.done();

			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new BrokerException(e.getMessage());
		}

		return ns;
	}

	public static String getFullBeanPath(String appType, String optWorker) {
		return String.format("com.bonc.broker.service.%s.%s", appType, optWorker);
	}

	public static String getFullBeanPathBase(String appType, String optWorker) {
		return String.format("com.bonc.broker.service.%s.base.%s", appType, optWorker);
	}

	public static String buildDashBoardUrl(String appType, String serviceName) {
		return String.format("http://%s/component/service?serviceName=%s&appType=%s", PropertyUtil.getProperty("bcm.nginx.url"), serviceName, appType);
	}

	/**
	 * 构建DashboardUrl
	 * @param dashboardUrl
	 * @param desc
	 * @return
	 */
	public static List<DashboardUrl> buildDashBoardUrlObject(String dashboardUrl, String desc) {
		DashboardUrl dashboard = new DashboardUrl();

		List<DashboardUrl> dashboardList = new ArrayList(16);

		dashboard.setUrl(dashboardUrl);
		dashboard.setDesc(desc);

		dashboardList.add(dashboard);

		return dashboardList;
	}

}
