package com.bonc.broker.service.redis;
/**
 * @author xingej
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bonc.broker.common.AppTypeConst;
import com.bonc.broker.common.Global;
import com.bonc.broker.entity.ServiceInstance;
import com.bonc.broker.exception.BrokerException;
import com.bonc.broker.service.DaoService;
import com.bonc.broker.service.model.base.MemoryCPU;
import com.bonc.broker.service.model.base.Resources;
import com.bonc.broker.service.model.lvm.LVMSpec;
import com.bonc.broker.service.model.lvm.Lvm;
import com.bonc.broker.service.model.redis.BindingNode;
import com.bonc.broker.service.model.redis.RedisCluster;
import com.bonc.broker.service.model.redis.RedisSpec;
import com.bonc.broker.service.redis.base.BaseWorkerThread;
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
		// 1. 更新redis实例
		RedisCluster redisCluster;
		try {
			redisCluster = updateInstance();
		} catch (BrokerException e) {
			updateTableForF();
			logger.error("[redis update instance:\t]" + e.getMessage());
			return;
		}

		// 2. 开始校验集群状态，更新数据库
		boolean checkStatusFlag;
		try {
			checkStatusFlag = checkStatus(redisCluster);
		} catch (BrokerException e) {
			updateTableForF();
			return;
		}

		// 3.
		if (checkStatusFlag) {
			processAfterCheckStatusSucceed(redisCluster);
		} else {
			processAfterCheckStatusFail(redisCluster);
		}

	}

	/**
	 * @return
	 */
	private RedisCluster updateInstance() throws BrokerException {
		// 1. 从k8s获取到对应的yaml对象RedisCluster
		String instanceId = data.get("instance_id");
		ServiceInstance serviceInstance = daoService.getServiceInstance(instanceId);
		if (null == serviceInstance) {
			throw new BrokerException("[ ]");
		}

		// 2. 更新资源属性
		String serviceName = serviceInstance.getServiceName();
		String tenantId = serviceInstance.getTenantId();
		RedisCluster redisResource;
		RedisCluster orReplace;
		try {
			redisResource = k8sClientForRedis.inNamespace(tenantId).withName(serviceName).get();

			redisResource = updateResource(redisResource);
			// 3. 调用k8s接口，更新对象
			orReplace = k8sClientForRedis.inNamespace(tenantId).createOrReplace(redisResource);
		} catch (Exception e) {
			throw new BrokerException(e.getMessage());
		}

		logger.info("--更新业务---调用完k8s接口-----成功了!---");
		return orReplace;
	}

	@Override
	protected void updateTableForS() {
		// 2. 更新数据库
		try {
			daoService.updateBrokerLog(data.get("id"), Global.STATE_S);
		} catch (BrokerException e) {
			logger.error(e.getMessage());
			return;
		}

		String parameters = data.get("parameters");
		JSONObject parametersObject = JSONObject.parseObject(parameters);
		String newCpu = parametersObject.getString("cpu");
		String newMemory = parametersObject.getString("memory");
		String newCapacity = parametersObject.getString("capacity");
		ServiceInstance instanceId = daoService.getServiceInstance(data.get("instance_id"));

		JSONObject oldParametersObject = JSONObject.parseObject(instanceId.getParameters());
		String oldConfiguration = oldParametersObject.getString("configuration");
		JSONObject oldResourceObject = JSONObject.parseObject(oldConfiguration);

		oldResourceObject.put("cpu", newCpu);
		oldResourceObject.put("memory", newMemory);
		oldResourceObject.put("capacity", newCapacity);

		oldParametersObject.put("configuration", oldResourceObject);

		try {
			daoService.updateServiceInstance(data.get("instance_id"), oldParametersObject);
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
		}
	}

	private RedisCluster updateResource(RedisCluster redisCluster) {
		String parameters = data.get("parameters");
		JSONObject jsonObject = JSONObject.parseObject(parameters);
		String cpu = jsonObject.getString("cpu");
		String memory = jsonObject.getString("memory");
		String capacity = jsonObject.getString("capacity");

		RedisSpec spec = redisCluster.getSpec();
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
		logger.info("---update业务----3");
		redisCluster.setSpec(spec);

		logger.info("---update业务----spec:\t" + JSONObject.toJSONString(redisCluster.getSpec()));

		return redisCluster;
	}

	private boolean isStoppedOrStarted(String namespace, String serviceName, String expectStatus) {
		int time = 0;
		String status = "";
		logger.info("--redis---update work---check---status----:\t" + status + "\tinstanceId:\t" + data.get("instance_id"));
		logger.info("--redis---update work---check---status---namespace---:\t" + namespace + "\tinstanceId:\t" + data.get("instance_id"));
		logger.info("--redis---update work---check---status---serviceName-:\t" + serviceName + "\tinstanceId:\t" + data.get("instance_id"));

		while (true) {
			//1. 获取yaml对象
			try {
				status = k8sClientForRedis.inNamespace(namespace).withName(serviceName).get().getStatus().getPhase();
			} catch (Exception e) {
				logger.error(e.getMessage());
			}

			logger.info("--redis--update work--checkStatus--current-status:\t< " + status + ">\tinstanceId:\t" + data.get("instance_id"));
			if (expectStatus.equalsIgnoreCase(status)) {
				logger.info("--redis---停止/启动成功---");
				return true;
			}
			logger.info("--redis---update service instance--checkStatus----status--2---:time\t" + time + " < 600 " + "\tnamespace:\t" + namespace + "\tserviceName:\t" + serviceName + "\tinstanceId:\t" + data.get("instance_id"));
			time++;
			if (600 <= time) {
				logger.error("--redis--update work ----checkStatus----status--timeout----:\t< " + status + ">\tinstanceId:\t" + data.get("instance_id"));
				return false;
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
		}
	}

	private boolean doExpandCapacity(RedisCluster redisCluster){
		logger.info("----开始扩容-----");
		String namespace = redisCluster.getMetadata().getNamespace();
		Map<String, BindingNode> nodes = redisCluster.getStatus().getBindings();
		Lvm lvm;
		logger.info("----node--size--->\t" + nodes.size());

		try {

			for (Map.Entry<String, BindingNode> entry : nodes.entrySet()) {
				lvm = k8sClientForLvm.inNamespace(namespace).withName(entry.getKey()).get();

				LVMSpec spec = lvm.getSpec();
				spec.setSize(StringUtils.unitExchange(redisCluster.getSpec().getCapacity()));

				lvm.setSpec(spec);

				k8sClientForLvm.inNamespace(namespace).createOrReplace(lvm);
				logger.info("扩容lvm完成，lvm：" + JSON.toJSONString(lvm));
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	protected boolean checkStatus(RedisCluster redisCluster) throws BrokerException {
		String namespace = redisCluster.getMetadata().getNamespace();
		String serviceName = redisCluster.getMetadata().getName();

		// 0. 先判断更新资源操作完成
		Boolean isOkUpdateResourcesFlag;
		try {
			isOkUpdateResourcesFlag = isOkUpdateResources(namespace, serviceName);
		}catch (Exception e){
			throw new BrokerException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}

		if (!isOkUpdateResourcesFlag) {
			logger.warn("--->redis-broker---change-resources--needStart---failed");
			return false;
		}

		// 1. 先停止redis-operator
		RedisSpec redisSpec = redisCluster.getSpec();
		redisSpec.setStopped(true);
		redisCluster.setSpec(redisSpec);
		try {
			k8sClientForRedis.inNamespace(namespace).createOrReplace(redisCluster);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new BrokerException(e.getMessage());
		}
		boolean isStoppedFlag = isStoppedOrStarted(namespace, serviceName, "stopped");
		if (!isStoppedFlag) {
			return false;
		}
		logger.info("--->redis-broker--->stopped---OK!");

		// 2. 再启动redis-operator
		RedisCluster redisCluster1;
		try {
			redisCluster1 = k8sClientForRedis.inNamespace(namespace).withName(serviceName).get();
			redisCluster1.getSpec().setStopped(false);
			k8sClientForRedis.inNamespace(namespace).createOrReplace(redisCluster1);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new BrokerException(e.getMessage());
		}

		boolean isStartedFlag = isStoppedOrStarted(namespace, serviceName, "running");
		if (!isStartedFlag) {
			logger.info("--->redis-broker--->started---failed");
			return false;
		}
		logger.info("--->redis-broker--->started---OK!");

		return true;
	}

	@Override
	protected void processAfterCheckStatusSucceed(RedisCluster redisCluster) {
		// 1. 校验是否需要创建LVM
		Float oldCapacity = daoService.getOldCapacity(data.get("instance_id"));
		if (null == oldCapacity) {
			logger.info("--->redis-broker--->started---failed");
			updateTableForF();
			return;
		}

		String capacity = redisCluster.getSpec().getCapacity();
		String reCapacity = capacity.substring(0, capacity.length() - 2);
		float newCapacity = Float.valueOf(reCapacity);
		boolean isExpandOk = true;
		if (newCapacity > oldCapacity) {
			logger.info("---->redis---开始--存储扩容操作-----");
			isExpandOk = doExpandCapacity(redisCluster);
		}

		// 2. 更新数据库
		if (isExpandOk == true) {
			updateTableForS();
		} else {
			updateTableForF();
		}
	}

	@Override
	protected void processAfterCheckStatusFail(RedisCluster redisCluster) {
		updateTableForF();
	}

	private boolean isOkUpdateResources(String namespace, String serviceName) {
		int time = 0;
		String status = "";
		logger.info("--redis---update work---needRestart---serviceName-:\t" + serviceName + "\tinstanceId:\t" + data.get("instance_id"));
		while (true) {
			if (600 <= time) {
				logger.error("----redis更新资源时，redis-operator 在规定时间内未更新needRestart字段；更新资源失败!\tinstanceId:\t" + data.get("instance_id"));
				return false;
			}
			time++;
			logger.info("---redis---update resources--needRestart--2---:time\t" + time + " < 600 " + "\tnamespace:\t" + namespace + "\tserviceName:\t" + serviceName + "\tinstanceId:\t" + data.get("instance_id"));
			//1. 获取yaml对象
			try {
				RedisCluster redisCluster = k8sClientForRedis.inNamespace(namespace).withName(serviceName).get();
				boolean needRestart = redisCluster.getStatus().isNeedRestart();
				// 校验mysqlCluster 资源更新是否完成
				if (needRestart){
					logger.info("---redis--update work--needRestart--ok!");
					return true;
				}
				logger.info("---redis--update work--needRestart--current-status:\t<" + status + ">\tinstanceId:\t" + data.get("instance_id"));

			} catch (Exception e) {
				logger.error("[update service instance]:\t get redisCluster operator error:=======>\t" + e.getMessage());
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
		}
	}




}



