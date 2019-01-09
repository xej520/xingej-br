package com.bonc.broker.service.mysql.base;

import com.alibaba.fastjson.JSONObject;
import com.bonc.broker.common.Global;
import com.bonc.broker.common.GlobalHelp;
import com.bonc.broker.common.K8sClient;
import com.bonc.broker.controller.mode.BindingMysqlInsideK8S;
import com.bonc.broker.controller.mode.BindingMysqlOutsideK8S;
import com.bonc.broker.controller.mode.DashboardUrl;
import com.bonc.broker.entity.ServiceInstance;
import com.bonc.broker.service.DaoService;
import com.bonc.broker.service.IBindingWorker;
import com.bonc.broker.service.model.crd.mysql.DoneableMysql;
import com.bonc.broker.service.model.crd.mysql.MysqlList;
import com.bonc.broker.service.model.mysql.MysqlCluster;
import com.bonc.broker.service.model.mysql.MysqlConfig;
import com.bonc.broker.service.model.mysql.MysqlServer;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

	private MixedOperation<MysqlCluster, MysqlList, DoneableMysql, Resource<MysqlCluster, DoneableMysql>> k8sClientForMysql = K8sClient
			.getK8sClientForMysql();
	@Autowired
	private DaoService daoService;

	@Override
	public JSONObject buildBindingInfo(String instanceId) {
// 1. 获取instance_id对应的mysqlCluster对象
		ServiceInstance serviceInstance = daoService.getServiceInstance(instanceId);
		if (null == serviceInstance) {
			logger.error("[buildBindingInfoForMysql]--->get serviceInstance failed" + "\tinstanceId:\t" + instanceId);
			return null;
		}
		logger.info("[buildBindingInfoForMysql]--instanceID:\t" + instanceId + "\t" + JSONObject.toJSONString(serviceInstance));

		MysqlCluster mysqlCluster = null;
		try {
			mysqlCluster = k8sClientForMysql.inNamespace(serviceInstance.getTenantId())
					.withName(serviceInstance.getServiceName()).get();
		} catch (Exception e) {
			logger.error("[buildBindingInfoForMysql]--->error:\n" + e.getMessage());
		}

		logger.info("[buildBindingInfoForMysql]---get mysqlCluster from k8s ok!---instanceID:\t" + instanceId + "\tserviceName:\t" + serviceInstance.getServiceName());
		// 2. 转换成json格式
		JSONObject credentials = buildCredentials(mysqlCluster);

		// 3. 返回
		logger.info("[buildBindingInfoForMysql]---build credentials info:\n" + credentials.toJSONString());
		return credentials;
	}

	private JSONObject buildCredentials(MysqlCluster mysqlCluster) {

		JSONObject bindingObject = new JSONObject();
		JSONObject credentialsObject = new JSONObject();

		MysqlConfig config = mysqlCluster.getSpec().getConfig();

		Map<String, MysqlServer> serverNodes = mysqlCluster.getStatus().getServerNodes();
		for (Map.Entry<String, MysqlServer> entry : serverNodes.entrySet()) {
			JSONObject nodeObject = new JSONObject();
			BindingMysqlInsideK8S insideK8s = new BindingMysqlInsideK8S();
			insideK8s.setUsername("root");
			insideK8s.setPassword(config.getPassword());
			insideK8s.setSvcname(entry.getValue().getSvcname());
			insideK8s.setPort("3306");
			insideK8s.setRole(entry.getValue().getRole());

			BindingMysqlOutsideK8S outsideK8s = new BindingMysqlOutsideK8S();
			outsideK8s.setUsername("root");
			outsideK8s.setPassword(config.getPassword());
			outsideK8s.setHost(entry.getValue().getNodeIP());
			outsideK8s.setPort(String.valueOf(entry.getValue().getNodeport()));
			outsideK8s.setRole(entry.getValue().getRole());

			nodeObject.put("inside_k8s", insideK8s);
			nodeObject.put("outside_k8s", outsideK8s);
			credentialsObject.put(entry.getKey(), nodeObject);
		}

		List<DashboardUrl> mysqlDashboardUrl = GlobalHelp.buildDashBoardUrlObject(GlobalHelp.buildDashBoardUrl(Global.MYSQL, mysqlCluster.getMetadata().getName()), "mysql dashboard url");
		credentialsObject.put("dashboard_urls", mysqlDashboardUrl);

		bindingObject.put("credentials", credentialsObject);
		bindingObject.put("syslog_drain_url", null);
		bindingObject.put("route_service_url", null);
		bindingObject.put("volume_mounts", null);

		return bindingObject;
	}

}




