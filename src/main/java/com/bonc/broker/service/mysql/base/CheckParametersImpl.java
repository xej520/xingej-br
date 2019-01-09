package com.bonc.broker.service.mysql.base;

import com.alibaba.fastjson.JSONObject;
import com.bonc.broker.common.Global;
import com.bonc.broker.common.MysqlClusterConst;
import com.bonc.broker.common.ParameterCheckingHelp;
import com.bonc.broker.entity.ServiceInstance;
import com.bonc.broker.exception.BrokerException;
import com.bonc.broker.exception.ExceptionMsg;
import com.bonc.broker.service.DaoService;
import com.bonc.broker.service.ICheckParameters;
import com.bonc.broker.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * @author xingej
 */
@Service
public class CheckParametersImpl implements ICheckParameters {
	/**
	 * 日志记录
	 */
	private static Logger logger = LoggerFactory.getLogger(CheckParametersImpl.class);

	@Autowired
	private DaoService daoService;

	/**
	 *
	 * @param appType
	 * @param tenantId
	 * @param planId
	 * @param configuration
	 * @throws BrokerException
	 */
	@Override
	public void checkCreateInstanceParameters(String appType, String tenantId, String planId, JSONObject configuration) throws BrokerException {
		// 1. 校验集群名称serviceName
		String serviceName = configuration.getString("serviceName");
		if (StringUtils.isBlank(serviceName)) {
			logger.error("----create service instance---->serviceName---1-->serviceName:\t" + serviceName + "\t" + ExceptionMsg.SERVICENAME_BADREQUEST);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.SERVICENAME_BADREQUEST);
		}
		if (!ParameterCheckingHelp.checkServiceName(serviceName)) {
			logger.error("-----create service instance--->serviceName---2-->serviceName:\t" + serviceName + "\t" + ExceptionMsg.SERVICENAME_BADREQUEST);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.SERVICENAME_BADREQUEST);
		}
		//校验serviceName 是否唯一？ 在同一个租户，同一个mysql/redis 下；
		ServiceInstance mysqlServiceInstance = daoService.getServiceInstanceByTenantIdAndCatalogAndServiceName(tenantId, "mysql", serviceName);
		if (null != mysqlServiceInstance) {
			logger.error("---mysql---create service instance-->serviceName---3-->serviceName:\t" + serviceName + "\t" + ExceptionMsg.SERVICE_NAME_CONFLICT + " in tenant_id:" + tenantId + " namespace!(mysql-broker)");
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.SERVICE_NAME_CONFLICT + "\tserviceName:\t" + serviceName + " in tenant_id:" + tenantId + " namespace!");
		}
		// 2. 校验副本数   mysql的ms模式，需要校验replicas参数
		String type = Global.PLAN_ID_SERVICE_MODE.get(planId);
		if (MysqlClusterConst.TYPE_MS.equals(type)) {
			Integer replicas = configuration.getInteger("replicas");
			if (null == replicas) {
				logger.error("--->replicas---1-->:\t" + ExceptionMsg.REPLICAS_BADREQUEST_MYSQL);
				throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.REPLICAS_BADREQUEST_MYSQL);
			}
			if (!ParameterCheckingHelp.checkReplicasForMysqlMs(replicas.intValue())) {
				logger.error("--->replicas---2-->replicas:\t" + replicas.intValue() + "\n" + ExceptionMsg.REPLICAS_BADREQUEST_MYSQL);
				throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.REPLICAS_BADREQUEST_MYSQL);
			}
		}
		// 3. 校验version
		String version = configuration.getString("version");
		if (null == version) {
			logger.error("--->version---1-->:\t" + ExceptionMsg.VERSION_BADREQUEST_MYSQL);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.VERSION_BADREQUEST_MYSQL);
		}
		if (!ParameterCheckingHelp.checkVersionForMysql(version)) {
			logger.error("--->version---2-->version:\t" + version + "\n" + ExceptionMsg.VERSION_BADREQUEST_MYSQL);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.VERSION_BADREQUEST_MYSQL);
		}
		// 4. 校验密码password
		String password = configuration.getString("password");
		if (null == password) {
			logger.error("--->password---1-->:\t" + ExceptionMsg.PASSWORD_BADREQUEST);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.PASSWORD_BADREQUEST);
		}
		if (!ParameterCheckingHelp.checkPassword(password)) {
			logger.error("--->password---2-->password:\t" + password + "\n" + ExceptionMsg.PASSWORD_BADREQUEST);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.PASSWORD_BADREQUEST);
		}
		Float cpu = configuration.getFloat("cpu");
		if (null == cpu) {
			logger.error("--->cpu---1-->:\t" + ExceptionMsg.CPU_BADREQUEST_MYSQL);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.CPU_BADREQUEST_MYSQL);
		}
		if (!ParameterCheckingHelp.checkCpuForMysql(cpu.intValue())) {
			logger.error("--->cpu---2-->cpu:\t" + cpu + "\n" + ExceptionMsg.CPU_BADREQUEST_MYSQL);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.CPU_BADREQUEST_MYSQL);
		}
		Float memory = configuration.getFloat("memory");
		if (null == memory) {
			logger.error("--->memory---1-->:\t" + ExceptionMsg.MEMORY_BADREQUEST_MYSQL);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.MEMORY_BADREQUEST_MYSQL);
		}
		if (!ParameterCheckingHelp.checkMemoryForMysql(memory.intValue())) {
			logger.error("--->memory---2-->memory:\t" + memory + "\n" + ExceptionMsg.MEMORY_BADREQUEST_MYSQL);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.MEMORY_BADREQUEST_MYSQL);
		}
		Float capacity = configuration.getFloat("capacity");
		if (null == capacity) {
			logger.error("--->capacity---1-->:\t" + ExceptionMsg.CAPACITY_BADREQUEST_MYSQL);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.CAPACITY_BADREQUEST_MYSQL);
		}
		if (!ParameterCheckingHelp.checkCapacityForMysql(capacity.intValue())) {
			logger.error("--->capacity---2-->capacity:\t" + capacity.intValue() + "\n" + ExceptionMsg.CAPACITY_BADREQUEST_MYSQL);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.CAPACITY_BADREQUEST_MYSQL);
		}
	}

	@Override
	public void checkUpdateInstanceParameters(String instanceId, JSONObject parameters) throws BrokerException {
		// 1校验cpu
		Float cpu = parameters.getFloat("cpu");
		if (null == cpu) {
			logger.error("--->cpu--1--->:\t" + ExceptionMsg.CPU_BADREQUEST_MYSQL);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.CPU_BADREQUEST_MYSQL);
		}
		if (!ParameterCheckingHelp.checkCpuForMysql(cpu.intValue())) {
			logger.error("--->cpu---2-->cpu:\t" + cpu.intValue() + "\n" + ExceptionMsg.CPU_BADREQUEST_MYSQL);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.CPU_BADREQUEST_MYSQL);
		}

		// 2 校验memory
		Float memory = parameters.getFloat("memory");
		if (null == memory) {
			logger.error("--->memory---1-->:\t" + ExceptionMsg.MEMORY_BADREQUEST_MYSQL);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.MEMORY_BADREQUEST_MYSQL);
		}
		if (!ParameterCheckingHelp.checkMemoryForMysql(memory.intValue())) {
			logger.error("--->memory---2-->memory:\t" + memory.intValue() + "\n" + ExceptionMsg.MEMORY_BADREQUEST_MYSQL);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.MEMORY_BADREQUEST_MYSQL);
		}

		// 3 校验capacity
		Float capacity = parameters.getFloat("capacity");
		if (null == capacity) {
			logger.error("--->capacity---1-->:\t" + ExceptionMsg.CAPACITY_BADREQUEST_MYSQL);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.CAPACITY_BADREQUEST_MYSQL);
		}
		if (!ParameterCheckingHelp.checkCapacityForMysql(capacity.intValue())) {
			logger.error("--->capacity---2-->capacity:\t" + capacity.intValue() + "\n" + ExceptionMsg.CAPACITY_BADREQUEST_MYSQL);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.CAPACITY_BADREQUEST_MYSQL);
		}
		Float oldCapacity = daoService.getOldCapacity(instanceId);
		if (capacity.intValue() < oldCapacity.intValue()) {
			logger.error("--->capacity---3-->new value of capacity(mysql):\t" + capacity.intValue() + "\toldCapacity\t" + oldCapacity.intValue() + "\n" + ExceptionMsg.CAPACITY_NOT_LESS);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.CAPACITY_NOT_LESS + "\toldCapacity:\t" + oldCapacity.intValue());
		}

	}

}
