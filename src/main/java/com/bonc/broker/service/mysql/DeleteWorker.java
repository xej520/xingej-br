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
		} else {
			processAfterCheckStatusFail(mysqlCluster);
		}
	}

	@Override
	protected void updateTableForS() {
		// 1. 更新数据库
		String instanceId = data.get("instance_id");
		String id = data.get("id");
		try {
			daoService.deleteServiceInstance(instanceId);
			logger.info("------deleteWork----delete---service--instance---table---instanceId:\t" + instanceId);
			daoService.updateBrokerLog(id, Global.STATE_S);
			logger.info("------deleteWork----delete---update--broker---log---table---instanceId:\t" + instanceId);
		} catch (BrokerException e) {
			logger.error(e.getMessage());
		}
		logger.info("------deleteWork----updateTableForS---2---instanceId:\t" + instanceId);
	}

	@Override
	protected void updateTableForF() {
		try {
			daoService.updateBrokerLog(data.get("id"), Global.STATE_F);
			logger.info("------deleteWork----update---broker---log---5---id:\t" + data.get("id") + "\tinstanceId:\t" + data.get("instance_id"));
		} catch (BrokerException e) {
			logger.error(e.getMessage());
		}
	}

	private MysqlCluster deleteServiceInstance() throws BrokerException {
		// 1. 从k8s获取到对应的yaml对象MysqlCluster
		String instanceId = data.get("instance_id");
		logger.info("[deleteServiceInstance]---delete mysql instanceID:\t" + instanceId);
		ServiceInstance serviceInstance = daoService.getServiceInstance(instanceId);
		if (null == serviceInstance) {
			logger.error("[deleteServiceInstance]---query serviceInstance " + "\tinstanceId:\t" + data.get("instance_id"));
			throw new BrokerException("[ ]");
		}

		logger.info("---开始删除---serviceInstance:\t" + serviceInstance + "\tinstanceId:\t" + data.get("instance_id"));

		// 2. 删除
		String serviceName = serviceInstance.getServiceName();
		String namespace = serviceInstance.getTenantId();
		logger.info("---开始删除---serviceInstance---3---namespace:\t" + serviceInstance.getTenantId() + "\tinstanceId:\t" + data.get("instance_id"));
		MysqlCluster mysqlCluster;
		try {
			mysqlCluster = k8sClientForMysql.inNamespace(namespace).withName(serviceName).get();

			k8sClientForMysql.inNamespace(namespace).delete(mysqlCluster);

			logger.info("---开始删除---serviceInstance---4---掉完k8s 删除接口完成------" + "\tinstanceId:\t" + data.get("instance_id"));
		} catch (Exception e) {
			logger.error("[ mysql-broker: delete instance ] instanceId: " + instanceId + "\n" + e.getMessage());
			throw new BrokerException(e.getMessage());
		}

		return mysqlCluster;
	}

	@Override
	protected boolean checkStatus(MysqlCluster mysqlCluster) {

		String namespace = mysqlCluster.getMetadata().getNamespace();
		String serviceName = mysqlCluster.getMetadata().getName();

		int time = 0;
		logger.info("---开始删除---进入轮询阶段-------" + "\tinstanceId:\t" + data.get("instance_id"));
		logger.info("--mysql---delete work---check---status----:" + "\tinstanceId:\t" + data.get("instance_id"));
		logger.info("--mysql---delete work---check---status---namespace---:\t" + namespace + "\tinstanceId:\t" + data.get("instance_id"));
		logger.info("--mysql---delete work---check---status---serviceName-:\t" + serviceName + "\tinstanceId:\t" + data.get("instance_id"));
		while (true) {
			//1. 获取YAML对象
			MysqlCluster ms = null;
			try {
				ms = k8sClientForMysql.inNamespace(namespace).withName(serviceName).get();
			} catch (Exception e) {
				logger.error("[delete work] get mysqlCluster from k8s failed: reason\t"+e.getMessage());
			}
			logger.info("---删除---2---获取mysqlCluster对象---" + "\tinstanceId:\t" + data.get("instance_id"));
			if (null == ms) {
				logger.info("---删除---3---删除mysqlCluster对象---成功!" + "\tinstanceId:\t" + data.get("instance_id"));
				return true;
			}

			time++;
			if (600 <= time) {
				logger.error("---删除---4---删除mysqlCluster对象---timeout" + "\tinstanceId:\t" + data.get("instance_id"));
				return false;
			}
			logger.info("--mysql--delete service instance--checkStatus----status--2---:time\t" + time + " < 600 " + "\tnamespace:\t" + namespace + "\tserviceName:\t" + serviceName + "\tinstanceId:\t" + data.get("instance_id"));
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
