package com.bonc.broker.service.mysql;

import com.alibaba.fastjson.JSON;
import com.bonc.broker.common.AppTypeConst;
import com.bonc.broker.common.Global;
import com.bonc.broker.common.GlobalHelp;
import com.bonc.broker.common.MysqlClusterConst;
import com.bonc.broker.exception.BrokerException;
import com.bonc.broker.service.BaseRequestBody;
import com.bonc.broker.service.DaoService;
import com.bonc.broker.service.model.mysql.MysqlCluster;
import com.bonc.broker.service.model.mysql.MysqlClusterOp;
import com.bonc.broker.service.model.mysql.MysqlConfig;
import com.bonc.broker.service.model.mysql.MysqlSpec;
import com.bonc.broker.service.mysql.base.BaseWorkerThread;
import com.bonc.broker.util.StringUtils;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xingej
 */

@Service
public class CreateWorker extends BaseWorkerThread {
	/**
	 * 日志记录
	 */
	private static Logger logger = LoggerFactory.getLogger(CreateWorker.class);

	@Value("${lvm.vgname}")
	private String vgName;
	@Value("${nodeselector.component}")
	private String componentNodeSelector;
	@Autowired
	private DaoService daoService;

	@Override
	protected void execute() {
		// 1. 创建mysql实例
		MysqlCluster mysqlCluster = null;
		try {
			mysqlCluster = createInstance();
		} catch (BrokerException e) {
			updateTableForF();
			deleteMysqlClusterForFail(mysqlCluster);
			logger.error("[mysql create instance:\t]" + e.getMessage() + "\tinstanceId:\t" + data.get("instance_id"));
			return;
		}

		// 2. 开始校验集群状态，更新数据库
		boolean checkStatusFlag = checkStatus(mysqlCluster);

		// 3. 处理数据库，LVM等基本业务
		if (checkStatusFlag) {
			logger.info("-----k8s---调用成功了-----开始更新数据了:\t" + data.get("id") + "\tinstanceId:\t" + data.get("instance_id"));
			processAfterCheckStatusSucceed(mysqlCluster);
		} else {
			logger.info("-----k8s---失败-----开始更新数据了:\t" + data.get("id") + "\tinstanceId:\t" + data.get("instance_id"));
			deleteMysqlClusterForFail(mysqlCluster);
			processAfterCheckStatusFail(mysqlCluster);
		}
	}

	@Override
	protected void updateTableForS() {
		logger.info("----checkStatus----updateBrokerLog---id:\t" + data.get("id") + "\tinstanceId:\t" + data.get("instance_id"));
		try {
			logger.info("----checkStatus----save---service instance--table---id:\t" + data.get("id") + "\tinstanceId:\t" + data.get("instance_id"));
			daoService.saveServiceInstance(data.get("instance_id"), data.get("parameters"), data.get("service_id"), data.get("plan_id"));
			logger.info("----checkStatus----updateBrokerLog---id:\t" + data.get("id") + "\tinstanceId:\t" + data.get("instance_id"));
			daoService.updateBrokerLog(data.get("id"), Global.STATE_S);
		} catch (BrokerException e) {
			logger.error(e.getMessage());
			return;
		}
	}

	@Override
	protected void updateTableForF() {
		try {
			logger.info(String.format("--update broker log---status: failed! id:\t%s;\tinstanceId:\t%s;\tplanId:\t%s", data.get("id"), data.get("instance_id"), data.get("plan_id")));
			daoService.updateBrokerLog(data.get("id"), Global.STATE_F);
		} catch (BrokerException e) {
			logger.error("--mysql-----create service instance---update broker log error---\tinstanceId:\t" + data.get("instance_id"));
			return;
		}
	}

	private MysqlCluster createInstance() throws BrokerException {
		String parameters = data.get("parameters");
		logger.info("[mysql-create-thread]:\tparameters:\t" + parameters);
		BaseRequestBody baseRequestBody = GlobalHelp.buildBaseRequestBody(parameters);
		// 1. 拼接YMAL
		MysqlCluster mysqlCluster;
		try {
			logger.info("[mysql-create-thread]:\tstart build mysqlCluster yaml." + "\tinstanceId:\t" + data.get("instance_id"));
			mysqlCluster = buildMysqlCluster(baseRequestBody);
		} catch (BrokerException e) {
			logger.error("[mysql-create-thread]:\tstart build mysqlCluster yaml failed!" + "\tinstanceId:\t" + data.get("instance_id"));
			throw new BrokerException(e.getMessage());
		}

		logger.info("[mysql-create-thread]:\t build mysqlCluster yaml ok!" + "\tinstanceId:\t" + data.get("instance_id"));
		String tenantId = baseRequestBody.getTenantId();

		MysqlCluster mysqlCluster2;
		try {
			GlobalHelp.buildNamespace(tenantId);
			//2. 调用k8s接口
			mysqlCluster2 = k8sClientForMysql.inNamespace(tenantId).create(mysqlCluster);
		} catch (Exception e) {
			logger.error("[ mysql-broker: create instance ] instanceId:\t" + data.get("instance_id") + "\tRequest parameters:\t" + parameters + "\n" + e.getMessage());
			throw new BrokerException(e.getMessage());
		}

		logger.info("[mysql-create-thread]:\t call k8s create interface ok! tenantId:\t" + tenantId + "\tinstanceId:\t" + data.get("instance_id"));
		return mysqlCluster2;
	}

	/**
	 * @param baseRequestBody
	 * @return
	 * @throws BrokerException
	 */
	private MysqlCluster buildMysqlCluster(BaseRequestBody baseRequestBody) throws BrokerException {
		MysqlCluster mysqlCluster = new MysqlCluster();

		try {
			mysqlCluster = buildMysqlClusterMetadata(mysqlCluster, baseRequestBody);

			mysqlCluster = buildMysqlClusterSpec(mysqlCluster, baseRequestBody);

		}catch (BrokerException e) {
			logger.error("--->build mysqlcluster yaml error:\t" + e.getMessage());
			throw e;
		}

		return mysqlCluster;
	}

	@Override
	protected boolean checkStatus(MysqlCluster mysqlCluster) {
		String namespace = mysqlCluster.getMetadata().getNamespace();
		String serviceName = mysqlCluster.getMetadata().getName();

		int time = 0;
		String status = "";
		logger.info("--mysql-----create service instance---check---status----:\t" + status + "\tinstanceId:\t" + data.get("instance_id"));
		logger.info("--mysql-----create service instance---check---status---namespace---:\t" + namespace + "\tinstanceId:\t" + data.get("instance_id"));
		logger.info("--mysql-----create service instance---check---status---serviceName-:\t" + serviceName + "\tinstanceId:\t" + data.get("instance_id"));
		while (true) {
			//1. 获取yaml对象
			try {
				status = k8sClientForMysql.inNamespace(namespace).withName(serviceName).get().getStatus().getPhase();
			} catch (Exception e) {
				logger.error("[create service instance]:\t get mysqlCluster yaml status: error:=======>\t" + e.getMessage());
			}
			logger.info("--create service instance--checkStatus----status--1============>:\t<" + status + ">\tinstanceId:\t" + data.get("instance_id"));
			if ("running".equalsIgnoreCase(status)) {
				logger.info("-create service instance---checkStatus----status--ok--:\t<" + status + ">\tinstanceId:\t" + data.get("instance_id"));
				return true;
			}

			time++;
			logger.info("--create service instance--checkStatus----status--2---:time\t" + time + " < 600 " + "\tnamespace:\t" + namespace + "\tserviceName:\t" + serviceName + "\tinstanceId:\t" + data.get("instance_id"));
			if (600 <= time) {
				logger.error("----create service instance---checkStatus----status--timeout----:\t<" + status + ">\tinstanceId:\t" + data.get("instance_id"));
				return false;
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
		}
	}

	@Override
	protected void processAfterCheckStatusSucceed(MysqlCluster mysqlCluster) {
		String namespace = mysqlCluster.getMetadata().getNamespace();
		String serviceName = mysqlCluster.getMetadata().getName();
		// 1. 创建LVM
		try {
			logger.info("----k8s创建对象成功了----开始创建LVM----:\t" + "\tinstanceId:\t" + data.get("instance_id"));
			// 重新获取对象
			MysqlCluster newMysqlCluster = k8sClientForMysql.inNamespace(namespace).withName(serviceName).get();
			lvm.registerLvm(newMysqlCluster);
		} catch (Exception e) {
			updateTableForF();
			return;
		}

		// 2. 更新数据库
		logger.info("----k8s创建对象成功了----创建完成LVM--开始更新----数据库--" + "\tinstanceId:\t" + data.get("instance_id"));
		updateTableForS();
	}

	@Override
	protected void processAfterCheckStatusFail(MysqlCluster mysqlCluster) {
		updateTableForF();
	}

	private void deleteMysqlClusterForFail(MysqlCluster mysqlCluster) {

		String namespace = mysqlCluster.getMetadata().getNamespace();
		String serviceName = mysqlCluster.getMetadata().getName();

		try {
			logger.info("-----k8s---开始删除---新创建的对象-----开始更新数据了:\t" + data.get("id") + "\tinstanceId:\t" + data.get("instance_id"));
			MysqlCluster mysqlCluster1 = k8sClientForMysql.inNamespace(namespace).withName(serviceName).get();
			k8sClientForMysql.inNamespace(namespace).delete(mysqlCluster1);
		} catch (Exception e) {
			logger.error("[ ==mysql===删除k8s创建的对象失败=======\t]" + e.getMessage());
		}
	}

	private MysqlCluster buildMysqlClusterMetadata(MysqlCluster mysqlCluster, BaseRequestBody baseRequestBody) {
		ObjectMeta metaData = new ObjectMeta();
		mysqlCluster.setKind(MysqlClusterConst.KIND);
		mysqlCluster.setApiVersion(MysqlClusterConst.API_VERSION);
		metaData.setName(baseRequestBody.getServiceName());
		metaData.setNamespace(baseRequestBody.getTenantId());
		Map<String, String> labels = new HashMap(16);

		labels.put("tenant_id", data.get("tenant_id"));
		labels.put("project_id", data.get("project_id"));
		labels.put("user_id", data.get("user_id"));
		metaData.setLabels(labels);

		mysqlCluster.setMetadata(metaData);

		return mysqlCluster;
	}

	private MysqlCluster buildMysqlClusterSpec(MysqlCluster mysqlCluster, BaseRequestBody baseRequestBody) throws BrokerException {
		MysqlSpec spec = new MysqlSpec();
		MysqlClusterOp clusterOp = new MysqlClusterOp();

		clusterOp.setOperator(AppTypeConst.OPT_CLUSTER_CREATE);
		spec.setClusterop(clusterOp);
		spec.setVersion(baseRequestBody.getVersion());
		String serviceType = Global.PLAN_ID_SERVICE_MODE.get(data.get("plan_id"));
		spec.setType(serviceType);
		logger.info("==============================version===\t" + baseRequestBody.getVersion());

		String imageUrl = daoService.getRepoPath(AppTypeConst.APPTYPE_MYSQL, "default", baseRequestBody.getVersion());
		if (null == imageUrl) {
			logger.error("[buildMysqlCluster]:\t query mysql image url failed!");
			throw new BrokerException("[ ]");
		}
		spec.setImage(imageUrl);

		String exporterImageUrl = daoService.getRepoPath(AppTypeConst.APPTYPE_MYSQL, "exporter", baseRequestBody.getVersion());
		if (null == exporterImageUrl) {
			logger.error("[buildMysqlCluster]:\t query mysql exporterImage url failed!");
			throw new BrokerException("[ ]");
		}
		spec.setExporterimage(exporterImageUrl);

		MysqlConfig config = new MysqlConfig();
		config.setPassword(baseRequestBody.getPassword());
		config.setLivenessDelayTimeout(MysqlClusterConst.HEALTH_CHECK_LIVENESS_DELAY_TIMEOUT);
		config.setLivenessFailureThreshold(MysqlClusterConst.HEALTH_CHECK_LIVENESS_FAILURE_THRESHOLD);
		config.setReadinessDelayTimeout(MysqlClusterConst.HEALTH_CHECK_READINESS_DELAY_TIMEOUT);
		config.setReadinessFailureThreshold(MysqlClusterConst.HEALTH_CHECK_READINESS_FAILURE_THRESHOLD);
		logger.info("config:" + JSON.toJSONString(config));

		spec.setConfig(config);
		logger.info("buildConfig完成,spec：" + JSON.toJSONString(spec));

		String replicas = baseRequestBody.getReplicas();
		//针对的是ms模式
		if (!StringUtils.isBlank(replicas) && parameterCheckingHelp.isInteger(replicas)) {
			logger.info("----replicas value:\t" + Integer.parseInt(replicas));
			spec.setReplicas(Integer.parseInt(replicas));
		}
		if (MysqlClusterConst.TYPE_MM.equalsIgnoreCase(serviceType)) {
			logger.info("--mm mode--replicas value: 2");
			spec.setReplicas(2);
		}
		if (MysqlClusterConst.TYPE_SINGLE.equalsIgnoreCase(serviceType)) {
			logger.info("--single mode--single value: 1");
			spec.setReplicas(1);
		}

		spec.setResources(GlobalHelp.getResources(baseRequestBody.getCpu(), baseRequestBody.getMemory(), AppTypeConst.UNIT_GI));
		spec.setCapacity(baseRequestBody.getCapacity() + AppTypeConst.UNIT_GI);
		logger.info("创建mysql配置文件获取到的vgname:" + vgName + "\tinstanceId:\t" + data.get("instance_id"));
		spec.setVolume(vgName);
		logger.info("build的mysqlCluster spec:" + JSON.toJSONString(spec));
		Map<String, String> nodeSelector = new HashMap<>(16);

		logger.info("创建mysql配置文件获取到的componentNodeSelector:" + componentNodeSelector);
		if (Global.TRUE.equals(componentNodeSelector)) {
			nodeSelector.put(AppTypeConst.APPTYPE_MYSQL, componentNodeSelector);
		}
		spec.setNodeSelector(nodeSelector);
		mysqlCluster.setSpec(spec);

		logger.info("创建拼接的mysqlCluster：" + JSON.toJSONString(mysqlCluster));

		return mysqlCluster;
	}


}



