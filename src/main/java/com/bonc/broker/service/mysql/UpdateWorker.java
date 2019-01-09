package com.bonc.broker.service.mysql;
/**
 * @author xingej
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bonc.broker.common.AppTypeConst;
import com.bonc.broker.common.Global;
import com.bonc.broker.common.MysqlClusterConst;
import com.bonc.broker.entity.ServiceInstance;
import com.bonc.broker.exception.BrokerException;
import com.bonc.broker.service.DaoService;
import com.bonc.broker.service.model.base.MemoryCPU;
import com.bonc.broker.service.model.base.Resources;
import com.bonc.broker.service.model.lvm.LVMSpec;
import com.bonc.broker.service.model.lvm.Lvm;
import com.bonc.broker.service.model.mysql.MysqlCluster;
import com.bonc.broker.service.model.mysql.MysqlServer;
import com.bonc.broker.service.model.mysql.MysqlSpec;
import com.bonc.broker.service.mysql.base.BaseWorkerThread;
import com.bonc.broker.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UpdateWorker extends BaseWorkerThread {
	/**
	 * 日志记录
	 */
	private static Logger logger = LoggerFactory.getLogger(UpdateWorker.class);

	@Value("${nodeselector.component}")
	private String componentNodeSelector;
	@Autowired
	private DaoService daoService;

	@Override
	protected void execute() {
		// 1. 更新mysql实例
		MysqlCluster mysqlCluster;
		try {
			mysqlCluster = updateInstance();
		} catch (BrokerException e) {
			updateTableForF();
			logger.error("[mysql update instance:\t]" + e.getMessage());
			return;
		}

		// 2. 开始校验集群状态，更新数据库
		boolean checkStatusFlag;
		try {
			checkStatusFlag = checkStatus(mysqlCluster);
		} catch (BrokerException e) {
			updateTableForF();
			return;
		}

		// 3. 校验后，处理数据库，LVM等业务
		if (checkStatusFlag) {
			processAfterCheckStatusSucceed(mysqlCluster);
		} else {
			processAfterCheckStatusFail(mysqlCluster);
		}

	}

	/**
	 * @return
	 */
	private MysqlCluster updateInstance() throws BrokerException {
		// 1. 从k8s获取到对应的yaml对象MysqlCluster
		String instanceId = data.get("instance_id");
		logger.info("---update---instanceId--->\t" + instanceId);
		ServiceInstance serviceInstance = daoService.getServiceInstance(instanceId);
		if (null == serviceInstance) {
			logger.error("[update work] query serviceInstance table failed; reason: by instanceId:\t" + instanceId);
			throw new BrokerException("[update work] query serviceInstance table failed; reason: by instanceId:\t" + instanceId);
		}

		logger.info("---update---serviceInstance----->\t" + JSONObject.toJSONString(serviceInstance));
		String serviceName = serviceInstance.getServiceName();
		String tenantId = serviceInstance.getTenantId();
		MysqlCluster orReplace;
		MysqlCluster mysqlCluster;
		try {

			// 2. 更新资源属性
			mysqlCluster = k8sClientForMysql.inNamespace(tenantId).withName(serviceName).get();
			MysqlCluster mysqlResourceNew = updateResource(mysqlCluster);
			logger.info("--更新业务---开始调用k8s的更新接口---");
			// 3. 调用k8s接口，更新对象
			orReplace = k8sClientForMysql.inNamespace(tenantId).createOrReplace(mysqlResourceNew);

		} catch (Exception e) {
			logger.error("[update work] query k8s failed; reason: by instanceId:\t" + instanceId);
			throw new BrokerException(e.getMessage());
		}

		logger.info("--更新业务---调用完k8s接口-----成功了!---");
		return orReplace;
	}

	@Override
	protected void updateTableForS() {
		// 2. 更新数据库
		logger.info("---update work----updateTableForS----" + "\tinstanceId:\t" + data.get("instance_id"));
		logger.info("----update---serviceInstance----parameters--->\t" + JSONObject.toJSONString(data.get("parameters")));

		String parameters = data.get("parameters");
		JSONObject parametersObject = JSONObject.parseObject(parameters);
		String newCpu = parametersObject.getString("cpu");
		String newMemory = parametersObject.getString("memory");
		String newCapacity = parametersObject.getString("capacity");

		ServiceInstance instanceId = daoService.getServiceInstance(data.get("instance_id"));

		if (null == instanceId) {
			try {
				logger.error("---update work---+++--query service instance--object--failed---" + "\tinstanceId:\t" + data.get("instance_id"));
				daoService.updateBrokerLog(data.get("id"), Global.STATE_F);
			} catch (BrokerException e) {
				logger.error(e.getMessage());
				return;
			}
		}
		JSONObject oldParametersObject = JSONObject.parseObject(instanceId.getParameters());
		String oldConfiguration = oldParametersObject.getString("configuration");
		JSONObject oldResourceObject = JSONObject.parseObject(oldConfiguration);
		oldResourceObject.put("cpu", newCpu);
		oldResourceObject.put("memory", newMemory);
		oldResourceObject.put("capacity", newCapacity);

		oldParametersObject.put("configuration", oldResourceObject);

		try {
			logger.error("---update work---update service instance---id:\t" + data.get("id") + "\tinstanceId:\t" + data.get("instance_id"));
			daoService.updateServiceInstance(data.get("instance_id"), oldParametersObject);
			daoService.updateBrokerLog(data.get("id"), Global.STATE_S);
			logger.info("---update work---update broker log---id:\t" + data.get("id") + "\tinstanceId:\t" + data.get("instance_id"));
		} catch (BrokerException e) {
			logger.error(e.getMessage());
			return;
		}
	}

	@Override
	protected void updateTableForF() {
		try {
			logger.info("---update work---update broker log---failed---id:\t" + data.get("id") + "\tinstanceId:\t" + data.get("instance_id"));
			daoService.updateBrokerLog(data.get("id"), Global.STATE_F);
		} catch (BrokerException e) {
			logger.error(e.getMessage());
		}
	}

	private MysqlCluster updateResource(MysqlCluster mysqlCluster) {

		String parameters = data.get("parameters");

		logger.info("====parameters===>\t" + parameters);

		JSONObject jsonObject = JSONObject.parseObject(parameters);
		logger.info("====parameters===>\t" + jsonObject);
		String cpu = jsonObject.getString("cpu");
		String memory = jsonObject.getString("memory");
		String capacity = jsonObject.getString("capacity");

		MysqlSpec spec = mysqlCluster.getSpec();
		Resources resources = spec.getResources();
		MemoryCPU limits = resources.getLimits();
		MemoryCPU requests = resources.getRequests();
		String json = JSON.toJSONString(data);

		logger.info("json" + JSON.toJSONString(json));

		requests.setCpu(String.valueOf(Math.floor(Float.parseFloat(cpu) / 4)));
		limits.setCpu(cpu);
		requests.setMemory(Math.floor(Float.parseFloat(memory) / 2) + AppTypeConst.UNIT_GI);
		limits.setMemory(memory + AppTypeConst.UNIT_GI);
		spec.setCapacity(capacity + AppTypeConst.UNIT_GI);

		resources.setRequests(requests);
		resources.setLimits(limits);
		spec.setResources(resources);
		spec.getClusterop().setOperator(MysqlClusterConst.MYSQL_CLUSTER_OPT_UPDATE_INSTANCE);
		mysqlCluster.setSpec(spec);

		logger.info("---mysql---spec:\t[%s]", JSONObject.toJSONString(mysqlCluster.getSpec()));

		return mysqlCluster;
	}

	@Override
	protected boolean checkStatus(MysqlCluster mysqlCluster) throws BrokerException {
		String namespace = mysqlCluster.getMetadata().getNamespace();
		String serviceName = mysqlCluster.getMetadata().getName();

		// 0. 先判断更新资源操作完成
		Boolean isOkUpdateResourcesFlag;
		try {
			isOkUpdateResourcesFlag = isOkUpdateResources(namespace, serviceName);
		}catch (Exception e){
			throw new BrokerException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}

		if (!isOkUpdateResourcesFlag) {
			logger.warn("--->mysql-broker---change-resources--operator---failed");
			return false;
		}

		//1. 先停止mysql-operator
		MysqlSpec mysqlSpec = mysqlCluster.getSpec();
		mysqlSpec.getClusterop().setOperator(AppTypeConst.OPT_CLUSTER_STOP);
		logger.info("----update--mysqlCluster---stop--mysqlClusters---0-->\t" + JSONObject.toJSONString(mysqlCluster));
		try {
			k8sClientForMysql.inNamespace(namespace).createOrReplace(mysqlCluster);
		} catch (Exception e) {
			throw new BrokerException("[ ]");
		}
		boolean isStoppedFlag = isStoppedOrStarted(namespace, serviceName, "stopped");
		if (!isStoppedFlag) {
			logger.warn("--->mysql-broker---change-resources--stopped---failed");
			return false;
		}
		logger.info("--->mysql-broker---change-resources--stopped---ok");

		// 2. 再启动mysql-operator
		try {
			MysqlCluster mysqlCluster1 = k8sClientForMysql.inNamespace(namespace).withName(serviceName).get();
			mysqlCluster1.getSpec().getClusterop().setOperator(AppTypeConst.OPT_CLUSTER_START);
			logger.info("----update--mysqlCluster---start---1-->\t" + JSONObject.toJSONString(mysqlCluster1));
			logger.info("----update--mysqlCluster---开始调用k8s接口---update-->");
			k8sClientForMysql.inNamespace(namespace).createOrReplace(mysqlCluster1);
		} catch (Exception e) {
			logger.error("----update work ----start optType---error---\tinstanceId:\t" + data.get("instance_id"));
			throw new BrokerException("[ ]");
		}
		boolean isStartedFlag = isStoppedOrStarted(namespace, serviceName, "running");
		if (!isStartedFlag) {
			logger.warn("--->mysql-broker---change-resources--stopped---failed");
			return false;
		}
		logger.info("--->mysql-broker---change-resources--started---ok");

		return true;
	}

	@Override
	protected void processAfterCheckStatusSucceed(MysqlCluster mysqlCluster) {
		logger.info("---update work----processAfterCheckStatusSucceed----" + "\tinstanceId:\t" + data.get("instance_id"));
		// 1. 判断是否进行存储扩容
		boolean isExpandOk = true;
		String instanceId = data.get("instance_id");
		String capacity = mysqlCluster.getSpec().getCapacity();
		String reCapacity = capacity.substring(0, capacity.length() - 2);
		float newCapacity = Float.valueOf(reCapacity);
		try {
			float oldCapacity = daoService.getOldCapacity(instanceId);

			logger.info("---oldCapacity\t" + oldCapacity);
			logger.info("---newCapacity\t" + newCapacity);
			if (oldCapacity < newCapacity) {
				logger.info("---->oldCapacity: %s;\tnewCapacity: %s", oldCapacity, newCapacity);
				logger.info("---->mysql-broker---开始扩容操作!");
				isExpandOk = doExpandCapacity(mysqlCluster);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			updateTableForF();
			return;
		}

		// 2. 更新数据库
		if (isExpandOk == true) {
			updateTableForS();
		} else {
			updateTableForF();
		}

	}

	@Override
	protected void processAfterCheckStatusFail(MysqlCluster mysqlCluster) {
		updateTableForF();
	}

	private boolean doExpandCapacity(MysqlCluster mysqlCluster) throws BrokerException {
		Lvm lvm;
		String namespace = mysqlCluster.getMetadata().getNamespace();
		Map<String, MysqlServer> nodes = mysqlCluster.getStatus().getServerNodes();
		try {
			for (Map.Entry<String, MysqlServer> entry : nodes.entrySet()) {
				MysqlServer volume = entry.getValue();

				logger.warn("===更新==lvmID===1=>\t" + volume.getVolumeid());
				lvm = k8sClientForLvm.inNamespace(namespace).withName(volume.getVolumeid()).get();

				LVMSpec spec = lvm.getSpec();
				spec.setSize(StringUtils.unitExchange(mysqlCluster.getSpec().getCapacity()));

				lvm.setSpec(spec);

				k8sClientForLvm.inNamespace(mysqlCluster.getMetadata().getNamespace()).createOrReplace(lvm);
				logger.info("创建lvm完成，lvm：" + JSON.toJSONString(lvm));
			}
		} catch (Exception e) {
			logger.error("----lvm--error---\t" + e.getMessage());
			throw new BrokerException(e.getMessage());
		}

		return true;
	}

	private boolean isStoppedOrStarted(String namespace, String serviceName, String expectStatus) {
		int time = 0;
		String status = "";

		logger.info("----checkStatus----expectStatus:\t" + expectStatus);
		logger.info("--mysql---update work---check---status----:\t" + status + "\tinstanceId:\t" + data.get("instance_id"));
		logger.info("--mysql---update work---check---status---namespace---:\t" + namespace + "\tinstanceId:\t" + data.get("instance_id"));
		logger.info("--mysql---update work---check---status---serviceName-:\t" + serviceName + "\tinstanceId:\t" + data.get("instance_id"));
		while (true) {
			if (600 <= time) {
				logger.error("----update work ----checkStatus----status--timeout----:\t< " + status + ">\tinstanceId:\t" + data.get("instance_id"));
				return false;
			}
			time++;
			logger.info("---mysql---update service instance--checkStatus----status--2---:time\t" + time + " < 600 " + "\tnamespace:\t" + namespace + "\tserviceName:\t" + serviceName + "\tinstanceId:\t" + data.get("instance_id"));
			//1. 获取yaml对象
			try {
				status = k8sClientForMysql.inNamespace(namespace).withName(serviceName).get().getStatus().getPhase();
				if (expectStatus.equalsIgnoreCase(status)) {
					logger.info("---mysql--update work---checkStatus--current-status:\t" + status + "  ok!");
					return true;
				}
				logger.info("---mysql--update work--checkStatus--current-status:\t\t<" + status + ">\tinstanceId:\t" + data.get("instance_id"));

			} catch (Exception e) {
				logger.error("[update service instance]:\t get mysqlCluster yaml status: error:=======>\t" + e.getMessage());
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
		}
	}

	private boolean isOkUpdateResources(String namespace, String serviceName) {
		int time = 0;
		String status = "";
		logger.info("--mysql---update work---operator---serviceName-:\t" + serviceName + "\tinstanceId:\t" + data.get("instance_id"));
		while (true) {
			if (600 <= time) {
				logger.error("----mysql更新资源时，mysql-operator 在规定时间内未清空operator字段；更新资源失败!\tinstanceId:\t" + data.get("instance_id"));
				return false;
			}
			time++;
			logger.info("---mysql---update resources--operator--2---:time\t" + time + " < 600 " + "\tnamespace:\t" + namespace + "\tserviceName:\t" + serviceName + "\tinstanceId:\t" + data.get("instance_id"));
			//1. 获取yaml对象
			try {
				MysqlCluster mysqlCluster = k8sClientForMysql.inNamespace(namespace).withName(serviceName).get();
				String operator = mysqlCluster.getSpec().getClusterop().getOperator();
				// 校验mysqlCluster 资源更新是否完成
				if (StringUtils.isBlank(operator)){
					logger.info("---mysql--update work--operator--ok!");
					return true;
				}
				logger.info("---mysql--update work--checkStatus--current-status:\t<" + status + ">\tinstanceId:\t" + data.get("instance_id"));

			} catch (Exception e) {
				logger.error("[update service instance]:\t get mysqlCluster operator error:=======>\t" + e.getMessage());
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
		}
	}


}



