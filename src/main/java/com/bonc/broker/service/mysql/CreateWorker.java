package com.bonc.broker.service.mysql;

import com.alibaba.fastjson.JSON;
import com.bonc.broker.common.AppTypeConst;
import com.bonc.broker.common.Global;
import com.bonc.broker.common.GlobalHelp;
import com.bonc.broker.common.MysqlClusterConst;
import com.bonc.broker.exception.BrokerException;
import com.bonc.broker.service.BaseRequestBody;
import com.bonc.broker.service.DaoService;
import com.bonc.broker.service.model.base.Resources;
import com.bonc.broker.service.model.mysql.*;
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
		MysqlCluster mysqlCluster;
		try {
			mysqlCluster = createInstance();
		} catch (BrokerException e) {
			updateTableForF();
			logger.error("[mysql create instance:\t]" + e.getMessage());
			return;
		}

		// 2. 开始校验集群状态，更新数据库
		boolean checkStatusFlag = checkStatus(mysqlCluster);

		// 3. 处理数据库，LVM等基本业务
		if (checkStatusFlag) {
			processAfterCheckStatusSucceed(mysqlCluster);
		}else {
			processAfterCheckStatusFail(mysqlCluster);
		}
	}

	@Override
	protected void updateTableForS() {
		logger.info("----checkStatus----updateBrokerLog---id:\t" + data.get("id"));
		try {
			daoService.updateBrokerLog(data.get("id"), Global.STATE_S);
			daoService.saveServiceInstance(data.get("instance_id"), data.get("parameters"), data.get("service_id"), data.get("plan_id"));
		} catch (BrokerException e) {
			logger.error(e.getMessage());
			return;
		}
	}

	@Override
	protected void updateTableForF() {
		try {
			daoService.updateBrokerLog(data.get("id"), Global.STATE_F);
		} catch (BrokerException e) {
			logger.error(e.getMessage());
			return;
		}
	}

	private MysqlCluster createInstance() throws BrokerException {
		String parameters = data.get("parameters");
		BaseRequestBody baseRequestBody = GlobalHelp.buildBaseRequestBody(parameters);
		// 1. 拼接YMAL
		MysqlCluster mysqlCluster;
		try {
			mysqlCluster = buildMysqlCluster(baseRequestBody);
		} catch (BrokerException e) {
			throw new BrokerException(e.getMessage());
		}
		logger.info("--->拼接mysql yaml完成; serviceName:\t" + mysqlCluster.getMetadata().getName());
		String tenantId = baseRequestBody.getTenant_id();

		MysqlCluster mysqlCluster2;
		try {
			GlobalHelp.buildNamespace(tenantId);
			mysqlCluster2 = k8sClientForMysql.inNamespace(tenantId).create(mysqlCluster);
		} catch (Exception e) {
			logger.error("[ mysql-broker: create instance ] serviceId: " + data.get("instance_id") + "\tRequest parameters:\t" + parameters +"\n" + e.getMessage());
			throw new BrokerException(e.getMessage());
		}

		//2. 调用k8s接口
		return mysqlCluster2;
	}

	/**
	 * @param baseRequestBody
	 * @return
	 * @throws BrokerException
	 */
	private MysqlCluster buildMysqlCluster(BaseRequestBody baseRequestBody) throws BrokerException {
		MysqlCluster mysqlCluster = new MysqlCluster();

		ObjectMeta metaData = new ObjectMeta();
		mysqlCluster.setKind(MysqlClusterConst.KIND);
		mysqlCluster.setApiVersion(MysqlClusterConst.API_VERSION);
		metaData.setName(baseRequestBody.getServiceName());
		metaData.setNamespace(baseRequestBody.getTenant_id());
		Map<String, String> labels = new HashMap(16);

		labels.put("tenant_id", data.get("tenant_id"));
		labels.put("project_id", data.get("project_id"));
		labels.put("user_id", data.get("user_id"));
		metaData.setLabels(labels);

		mysqlCluster.setMetadata(metaData);

		MysqlSpec spec = new MysqlSpec();
		MysqlClusterOp clusterOp = new MysqlClusterOp();

		clusterOp.setOperator(AppTypeConst.OPT_CLUSTER_CREATE);
		spec.setClusterop(clusterOp);
		spec.setVersion(baseRequestBody.getVersion());
		spec.setType(Global.PLAN_ID_SERVICE_MODE.get(data.get("plan_id")));
		logger.info("==============================version===\t" + baseRequestBody.getVersion());

		String imageUrl = daoService.getRepoPath(AppTypeConst.APPTYPE_MYSQL, "default", baseRequestBody.getVersion());
		if (null == imageUrl) {
			throw new BrokerException("[ ]");
		}
		spec.setImage(imageUrl);

		String exporterImageUrl = daoService.getRepoPath(AppTypeConst.APPTYPE_MYSQL, "exporter", baseRequestBody.getVersion());
		if (null == exporterImageUrl) {
			throw new BrokerException("[ ]");
		}
		spec.setExporterimage(exporterImageUrl);


		Resources backResources = GlobalHelp.getResources(MysqlClusterConst.MYSQL_BACKUP_CONTAINER_DEFAULT_CPU,
				MysqlClusterConst.MYSQL_BACKUP_CONTAINER_DEFAULT_MEMORY, AppTypeConst.UNIT_GI);
		logger.info("构建backup Resources");
		MysqlBackup mysqlBackup = new MysqlBackup();
		mysqlBackup.setResources(backResources);

		String backupImageUrl = daoService.getRepoPath(AppTypeConst.APPTYPE_MYSQL, "backup", baseRequestBody.getVersion());
		if (null == backupImageUrl) {
			throw new BrokerException("[ ]");
		}

		logger.info("mysqlBackup：" + JSON.toJSONString(mysqlBackup));
		spec.setMysqlbackup(mysqlBackup);
		logger.info("构建backup Resources完毕");

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
		if (!StringUtils.isBlank(replicas) && parameterCheckingHelp.isInteger(replicas)) {
			spec.setReplicas(Integer.parseInt(replicas));
		}
		spec.setResources(GlobalHelp.getResources(baseRequestBody.getCpu(), baseRequestBody.getMemeory(), AppTypeConst.UNIT_GI));
		spec.setCapacity(baseRequestBody.getCapacity() + AppTypeConst.UNIT_GI);
		logger.info("创建mysql配置文件获取到的vgname:" + vgName);
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

	@Override
	protected boolean checkStatus(MysqlCluster mysqlCluster) {
		String namespace = mysqlCluster.getMetadata().getNamespace();
		String serviceName = mysqlCluster.getMetadata().getName();

		int time = 0;
		String status = "";
		while (true) {
			//1. 获取yaml对象
			try {
				status = k8sClientForMysql.inNamespace(namespace).withName(serviceName).get().getStatus().getPhase();
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			logger.info("----checkStatus----status----:\t" + status);
			if ("running".equalsIgnoreCase(status)) {
				return true;
			}

			time++;
			if (600 <= time) {
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
			// 重新获取对象
			MysqlCluster newMysqlCluster = k8sClientForMysql.inNamespace(namespace).withName(serviceName).get();
			lvm.registerLvm(newMysqlCluster);
		}catch (Exception e){
			updateTableForF();
			return;
		}

		// 2. 更新数据库
		updateTableForS();
	}

	@Override
	protected void processAfterCheckStatusFail(MysqlCluster mysqlCluster) {
		updateTableForF();
	}
}



