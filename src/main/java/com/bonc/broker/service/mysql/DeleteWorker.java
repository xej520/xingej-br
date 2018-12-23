package com.bonc.broker.service.mysql;
/**
 * @author xingej
 */

import com.bonc.broker.common.Global;
import com.bonc.broker.entity.ServiceInstance;
import com.bonc.broker.exception.BrokerException;
import com.bonc.broker.service.DaoService;
import com.bonc.broker.service.model.mysql.MysqlCluster;
import com.bonc.broker.service.mysql.base.BaseWorkerThread;
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

	@Autowired
	private DaoService daoService;

	@Override
	protected void execute() {
		// 1. 删除实例
		MysqlCluster mysqlCluster;
		try {
			mysqlCluster = deleteServiceInstance();
		} catch (BrokerException e) {
			updateTableForF();
			logger.error("[mysql delete instance:\t]" + e.getMessage());
			return;
		}

		// 2. 校验轮询
		boolean checkStatusFlag = checkStatus(mysqlCluster);

		// 3. 校验后，处理数据库，LVM等业务
		if (checkStatusFlag) {
			processAfterCheckStatusSucceed(mysqlCluster);
		}else {
			processAfterCheckStatusFail(mysqlCluster);
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
		logger.info("------deleteWork----updateTableForS---2:\t" + instance_id);

	}

	@Override
	protected void updateTableForF() {
		try {
			daoService.updateBrokerLog(data.get("id"), Global.STATE_F);
		} catch (BrokerException e) {
			logger.error(e.getMessage());
		}
	}

	private MysqlCluster deleteServiceInstance() throws BrokerException {
		// 1. 从k8s获取到对应的yaml对象MysqlCluster
		String instanceId = data.get("instance_id");
		logger.info("---开始删除---获取instanceID:\t" + instanceId);
		ServiceInstance serviceInstance = daoService.getServiceInstance(instanceId);
		if (null == serviceInstance) {
			throw new BrokerException("[ ]");
		}

		logger.info("---开始删除---serviceInstance:\t" + serviceInstance);

		// 2. 删除
		String serviceName = serviceInstance.getServiceName();
		String namespace = serviceInstance.getTenantId();
		logger.info("---开始删除---serviceInstance---3---namespace:\t" + serviceInstance.getTenantId());
		MysqlCluster mysqlCluster;
		try {
			mysqlCluster = k8sClientForMysql.inNamespace(namespace).withName(serviceName).get();

			k8sClientForMysql.inNamespace(namespace).delete(mysqlCluster);

			logger.info("---开始删除---serviceInstance---4---掉完k8s 删除接口完成------");
		} catch (Exception e) {
			logger.error("[ mysql-broker: delete instance ] serviceId: " + instanceId + "\n" + e.getMessage());
			throw new BrokerException(e.getMessage());
		}

		return mysqlCluster;
	}

	@Override
	protected boolean checkStatus(MysqlCluster mysqlCluster) {

		String namespace = mysqlCluster.getMetadata().getNamespace();
		String serviceName = mysqlCluster.getMetadata().getName();

		int time = 0;
		logger.info("---开始删除---进入轮询阶段-------");
		while (true) {
			//1. 获取YAML对象
			MysqlCluster ms =null;
			try {
				ms = k8sClientForMysql.inNamespace(namespace).withName(serviceName).get();
			}catch (Exception e) {
				logger.error(e.getMessage());
			}
			logger.info("---开始删除---2---获取mysqlCluster对象---");
			if (null == ms) {
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
		// 1. 删除LVM
		lvm.delLvm(mysqlCluster);
		// 2. 更新数据库
		updateTableForS();
	}

	@Override
	protected void processAfterCheckStatusFail(MysqlCluster mysqlCluster) {
		updateTableForF();
	}
}
