package com.bonc.broker.service.redis;
/**
 * @author xingej
 */

import com.bonc.broker.common.Global;
import com.bonc.broker.common.K8sClient;
import com.bonc.broker.entity.ServiceInstance;
import com.bonc.broker.exception.BrokerException;
import com.bonc.broker.service.DaoService;
import com.bonc.broker.service.model.crd.lvm.DoneableLvm;
import com.bonc.broker.service.model.crd.lvm.LvmList;
import com.bonc.broker.service.model.lvm.Lvm;
import com.bonc.broker.service.model.redis.RedisCluster;
import com.bonc.broker.service.redis.base.BaseWorkerThread;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeleteWorker extends BaseWorkerThread {

	/**
	 * 日志记录
	 */
	private static Logger logger = LoggerFactory.getLogger(DeleteWorker.class);

	protected MixedOperation<Lvm, LvmList, DoneableLvm, Resource<Lvm, DoneableLvm>> k8sClientForLvm = K8sClient.getK8sClientForLvm();

	@Autowired
	private DaoService daoService;

	@Override
	protected void execute() {
		// 1. 删除实例
		RedisCluster redisCluster;
		try {
			redisCluster = deleteServiceInstance();
		} catch (BrokerException e) {
			updateTableForF();
			logger.error("[redis delete instance:\t]" + e.getMessage());
			return;
		}

		// 2. 校验轮询，删除数据库，删除LVM
		boolean checkStatusFlag = checkStatus(redisCluster);

		// 3. 处理数据库，LVM等业务
		if (checkStatusFlag) {
			processAfterCheckStatusSucceed(redisCluster);
		} else {
			processAfterCheckStatusFail(redisCluster);
		}

	}

	@Override
	protected void updateTableForS() {
		// 1. 更新数据库
		String instance_id = data.get("instance_id");
		String id = data.get("id");
		try {
			daoService.updateBrokerLog(id, Global.STATE_S);
			daoService.deleteServiceInstance(instance_id);
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

	private RedisCluster deleteServiceInstance() throws BrokerException {
		// 1. 从k8s获取到对应的yaml对象RedisCluster
		String instanceId = data.get("instance_id");
		ServiceInstance serviceInstance = daoService.getServiceInstance(instanceId);
		if (null == serviceInstance) {
			throw new BrokerException("[ ]");
		}
		logger.info("---开始删除---serviceInstance:\t" + serviceInstance);

		// 2. 删除
		String serviceName = serviceInstance.getServiceName();
		logger.info("---开始删除---serviceInstance---2---serviceName:\t" + serviceName);
		String namespace = serviceInstance.getTenantId();
		RedisCluster redisCluster;
		try {
			redisCluster = k8sClientForRedis.inNamespace(namespace).withName(serviceName).get();

			k8sClientForRedis.inNamespace(namespace).delete(redisCluster);
		} catch (Exception e) {
			throw new BrokerException(e.getMessage());
		}

		logger.info("---开始删除---serviceInstance---4---掉完k8s 删除接口完成------");

		return redisCluster;
	}

	@Override
	protected boolean checkStatus(RedisCluster redisCluster){
		int time = 0;
		String namespace = redisCluster.getMetadata().getNamespace();
		String serviceName = redisCluster.getMetadata().getName();

		logger.info("---开始删除---进入轮询阶段-------");
		RedisCluster rc = null;
		while (true) {
			//1. 获取YAML对象
			try {
				rc = k8sClientForRedis.inNamespace(namespace).withName(serviceName).get();
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			logger.info("---开始删除---2---获取mysqlCluster对象---");
			if (null == rc) {
				return true;
			}

			time++;
			if (600 <= time) {
				return false;
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void processAfterCheckStatusSucceed(RedisCluster redisCluster) {
		// 2. 删除LVM
		lvm.delLvm(redisCluster);
		// 3. 更新数据库
		updateTableForS();
	}

	@Override
	protected void processAfterCheckStatusFail(RedisCluster redisCluster) {
		updateTableForF();
	}
}
