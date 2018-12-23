package com.bonc.broker.service.redis;

import com.alibaba.fastjson.JSONObject;
import com.bonc.broker.common.AppTypeConst;
import com.bonc.broker.common.Global;
import com.bonc.broker.common.GlobalHelp;
import com.bonc.broker.common.RedisClusterConst;
import com.bonc.broker.exception.BrokerException;
import com.bonc.broker.service.BaseRequestBodyRedis;
import com.bonc.broker.service.DaoService;
import com.bonc.broker.service.model.redis.RedisCluster;
import com.bonc.broker.service.model.redis.RedisSpec;
import com.bonc.broker.service.model.redis.Sentinel;
import com.bonc.broker.service.redis.base.BaseWorkerThread;
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
		RedisCluster redisCluster;
		try {
			redisCluster = createInstance();
		} catch (BrokerException e) {
			updateTableForF();
			logger.error("[redis create instance:\t]" + e.getMessage());
			return;
		}

		// 2. 开始校验集群状态
		boolean checkStatusFlag = checkStatus(redisCluster);

		// 3. 处理数据库,LVM等基本业务
		if (checkStatusFlag) {
			processAfterCheckStatusSucceed(redisCluster);
		}else {
			processAfterCheckStatusFail(redisCluster);
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
		}
	}

	@Override
	protected void updateTableForF() {
		try {
			daoService.updateBrokerLog(data.get("id"), Global.STATE_F);
		} catch (BrokerException e) {
			logger.error(e.getMessage());
		}
	}

	private RedisCluster createInstance() throws BrokerException {
		String parameters = data.get("parameters");
		BaseRequestBodyRedis baseRequestBody = GlobalHelp.buildBaseRequestBodyForRedis(parameters);

		RedisCluster redisCluster;
		String tenantId = baseRequestBody.getTenantId();
		RedisCluster redisCluster2;
		try {
			// 1. 拼接YMAL
			redisCluster = buildRedisCluster(baseRequestBody);
			// 2. 命名空间创建
			GlobalHelp.buildNamespace(tenantId);
			// 3. 创建redisCluster
			redisCluster2 = k8sClientForRedis.inNamespace(tenantId).create(redisCluster);
		} catch (Exception e) {
			logger.info("----create---redis--->\t" + e.getMessage());
			throw new BrokerException(e.getMessage());
		}
		logger.info("--构建redis对象-2-->\t" + JSONObject.toJSONString(redisCluster));

		//2. 调用k8s接口
		return redisCluster2;
	}

	private RedisCluster buildRedisCluster(BaseRequestBodyRedis baseRequestBodyRedis) throws BrokerException {

		logger.info("===============开始构建redisCluster==================");
		RedisCluster redisCluster = new RedisCluster();
		ObjectMeta metaData = new ObjectMeta();
		redisCluster.setKind(RedisClusterConst.KIND_REDIS);
		redisCluster.setApiVersion(RedisClusterConst.API_VERSION);
		metaData.setName(baseRequestBodyRedis.getServiceName());
		metaData.setNamespace(baseRequestBodyRedis.getTenantId());

		Map<String, String> labels = new HashMap(16);
		labels.put("tenant_id", data.get("tenant_id"));
		labels.put("project_id", data.get("project_id"));
		labels.put("user_id", data.get("user_id"));
		metaData.setLabels(labels);

		redisCluster.setMetadata(metaData);

		RedisSpec redisSpec = new RedisSpec();
		logger.info("===============开始构建redisCluster===========1=======");
		String version = baseRequestBodyRedis.getVersion();
		redisSpec.setStopped(false);
		redisSpec.setVersion(version);
		logger.info("===============开始构建redisCluster===========1=======verison:\t" + version);

		String imageUrl = daoService.getRepoPath(AppTypeConst.APPTYPE_REDIS, "default", version);
		if (null == imageUrl) {
			throw new BrokerException("[ ]");
		}
		redisSpec.setImage(imageUrl);
		String exporterImageUrl = daoService.getRepoPath(AppTypeConst.APPTYPE_REDIS, "exporter", version);
		if (null == exporterImageUrl) {
			throw new BrokerException("[ ]");
		}
		redisSpec.setExporterImage(exporterImageUrl);

		logger.info("===============开始构建redisCluster==========2========");
		redisSpec.setLogDir(RedisClusterConst.REDIS_EXPORTER_LOGDIR);
		redisSpec.setReplicas(baseRequestBodyRedis.getReplicas());
		redisSpec.setCapacity(baseRequestBodyRedis.getCapacity() + AppTypeConst.UNIT_GI);
		redisSpec.setStorageClass("lvm");
		logger.info("===============开始构建redisCluster========3==========");
		redisSpec.setVolume(vgName);
		String planId = data.get("plan_id");
		redisSpec.setType(Global.PLAN_ID_SERVICE_MODE.get(planId));
		redisSpec.setPassword(baseRequestBodyRedis.getPassword());

		Map<String, String> nodeSelector = new HashMap<>(16);
		if (Global.TRUE.equals(componentNodeSelector)) {
			logger.info("===============开始构建redisCluster=========4=========");
			nodeSelector.put(AppTypeConst.APPTYPE_REDIS, componentNodeSelector);
		}
		redisSpec.setNodeSelector(nodeSelector);
		logger.info("===============开始构建redisCluster==========5========");
		redisSpec.setResources(GlobalHelp.getResources(baseRequestBodyRedis.getCpu(), baseRequestBodyRedis.getMemory(), AppTypeConst.UNIT_GI));
		if (null != baseRequestBodyRedis.getSentinelCpu() && null != baseRequestBodyRedis.getSentinelMemory() && null != baseRequestBodyRedis.getSentinelReplicas()) {
			Sentinel sentinel = new Sentinel();
			sentinel.setResources(GlobalHelp.getResources(baseRequestBodyRedis.getSentinelCpu(), baseRequestBodyRedis.getSentinelMemory(), AppTypeConst.UNIT_MI));
			sentinel.setReplicas(Integer.parseInt(baseRequestBodyRedis.getSentinelReplicas()));
			redisSpec.setSentinel(sentinel);
			logger.info("sentinel的参数sentinelCpu：" + baseRequestBodyRedis.getSentinelCpu() + "   " + "sentinelMemory" + baseRequestBodyRedis.getSentinelMemory());
		}
		redisCluster.setSpec(redisSpec);
		logger.info("===============开始构建redisCluster==========7========");
		return redisCluster;
	}

	@Override
	protected boolean checkStatus(RedisCluster redisCluster) {
		String namespace = redisCluster.getMetadata().getNamespace();
		String serviceName = redisCluster.getMetadata().getName();

		logger.info("-----namespace--1---:\t" + namespace);
		logger.info("-----namespace--2---:\t" + serviceName);

		int time = 0;
		String status = "";
		while (true) {
			time++;
			if (600 <= time) {
				return false;
			}

			//1. 获取yaml对象
			try {
				status = k8sClientForRedis.inNamespace(namespace).withName(serviceName).get().getStatus().getPhase();
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			logger.info("----checkStatus----status----:\t" + status);

			if ("running".equalsIgnoreCase(status)) {
				return true;
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
		}
	}

	@Override
	protected void processAfterCheckStatusSucceed(RedisCluster redisCluster) {
		String namespace = redisCluster.getMetadata().getNamespace();
		String serviceName = redisCluster.getMetadata().getName();

		// 1. 创建LVM
		try {
			// 重新获取的
			RedisCluster newRedisCluster = k8sClientForRedis.inNamespace(namespace).withName(serviceName).get();
			lvm.registerLvm(newRedisCluster);
		} catch (Exception e) {
			updateTableForF();
			return;
		}

		// 2. 更新数据库
		updateTableForS();

	}

	@Override
	protected void processAfterCheckStatusFail(RedisCluster redisCluster) {
		//创建实例失败，不需要创建实例表
		updateTableForF();
	}
}



