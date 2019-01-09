package com.bonc.broker.service.redis.base;

import com.alibaba.fastjson.JSONObject;
import com.bonc.broker.common.Global;
import com.bonc.broker.common.ParameterCheckingHelp;
import com.bonc.broker.common.RedisClusterConst;
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


	@Override
	public void checkCreateInstanceParameters(String appType, String tenantId, String planId, JSONObject configuration) throws BrokerException {
		// Single模式下的参数，相当于基础参数，其他模式也会用到
		checkCreateParametersForRedisSingle(tenantId, configuration);

		// 针对redis MS模式
		if (RedisClusterConst.REDIS_TYPE_MS.equals(planId)) {
			checkReplicas(configuration);
		}

		// 针对redis MS-sentinel模式
		if (RedisClusterConst.REDIS_TYPE_MS_SENTINEL.equals(planId)) {
			checkReplicas(configuration);

			//校验哨兵资源
			checkSentinelResource(configuration);
		}
	}

	@Override
	public void checkUpdateInstanceParameters(String instanceId, JSONObject parameters) throws BrokerException {
		// 1 校验cpu
		Float cpu = parameters.getFloat("cpu");
		if (null == cpu) {
			logger.error("--->cpu---1-->:\t" + ExceptionMsg.CPU_BADREQUEST_REDIS);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.CPU_BADREQUEST_REDIS);
		}
		if (!ParameterCheckingHelp.checkCpuForRedis(cpu.intValue())) {
			logger.error("--->cpu---2-->cpu:\t" + cpu.intValue() + "\n" + ExceptionMsg.CPU_BADREQUEST_REDIS);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.CPU_BADREQUEST_REDIS);
		}

		// 2 校验memory
		Float memory = parameters.getFloat("memory");
		if (null == memory) {
			logger.error("--->memory---1-->:\t" + ExceptionMsg.MEMORY_BADREQUEST_REDIS);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.MEMORY_BADREQUEST_REDIS);
		}
		if (!ParameterCheckingHelp.checkMemoryForRedis(memory.intValue())) {
			logger.error("--->memory---2-->memory:\t" + memory.intValue() + "\n" + ExceptionMsg.MEMORY_BADREQUEST_REDIS);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.MEMORY_BADREQUEST_REDIS);
		}

		// 3 校验capacity
		Float capacity = parameters.getFloat("capacity");
		if (null == capacity) {
			logger.error("--->capacity---1-->:\t" + ExceptionMsg.CAPACITY_BADREQUEST_REDIS);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.CAPACITY_BADREQUEST_REDIS);
		}
		if (!ParameterCheckingHelp.checkCapacityForRedis(capacity.intValue())) {
			logger.error("--->capacity---2-->capacity:\t" + capacity + "\n" + ExceptionMsg.CAPACITY_BADREQUEST_REDIS);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.CAPACITY_BADREQUEST_REDIS);
		}
		Float oldCapacity = daoService.getOldCapacity(instanceId);
		if (capacity.intValue() < oldCapacity.intValue()) {
			logger.error("--->capacity---3-->new value of capacity(redis):\t" + capacity.intValue() + "\toldCapacity\t" + oldCapacity.intValue() + "\n" + ExceptionMsg.CAPACITY_NOT_LESS);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.CAPACITY_NOT_LESS + "\toldCapacity:\t" + oldCapacity.intValue());
		}
	}

	private void checkCreateParametersForRedisSingle(String tenantId, JSONObject configuration) throws BrokerException {
		// 1. 校验集群名称serviceName
		String serviceName = configuration.getString("serviceName");
		if (StringUtils.isBlank(serviceName)) {
			logger.error("--->serviceName---1-->:\t" + ExceptionMsg.SERVICENAME_BADREQUEST);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.SERVICENAME_BADREQUEST);
		}
		if (!ParameterCheckingHelp.checkServiceName(serviceName)) {
			logger.error("--->serviceName---2-->serviceName:\t" + serviceName + "\n" + ExceptionMsg.SERVICENAME_BADREQUEST);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.SERVICENAME_BADREQUEST);
		}
		//校验serviceName 是否唯一？ 在同一个租户，同一个mysql/redis 下；
		ServiceInstance mysqlServiceInstance = daoService.getServiceInstanceByTenantIdAndCatalogAndServiceName(tenantId, "redis", serviceName);
		if (null != mysqlServiceInstance) {
			logger.error("--redis->serviceName---3-->serviceName:\t" + serviceName + "\t" + ExceptionMsg.SERVICE_NAME_CONFLICT + " in tenant_id:" + tenantId + " namespace!(redis-broker)");
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.SERVICE_NAME_CONFLICT + "\tserviceName:\t" + serviceName + " in tenant_id:" + tenantId + " namespace!");
		}

		// 2. 校验version
		String version = configuration.getString("version");
		if (null == version) {
			logger.error("--->version---1-->:\t" + ExceptionMsg.VERSION_BADREQUEST_REDIS);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.VERSION_BADREQUEST_REDIS);
		}
		if (!ParameterCheckingHelp.checkVersionForRedis(version)) {
			logger.error("--->version---2-->version:\t" + version + "\n" + ExceptionMsg.VERSION_BADREQUEST_REDIS);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.VERSION_BADREQUEST_REDIS);
		}

		// 3. 校验密码password
		String password = configuration.getString("password");
		if (null == password) {
			logger.error("--->password---1-->:\t" + ExceptionMsg.PASSWORD_BADREQUEST);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.PASSWORD_BADREQUEST);
		}
		if (!ParameterCheckingHelp.checkPassword(password)) {
			logger.error("--->password---2-->password:\t" + password + "\n" + ExceptionMsg.PASSWORD_BADREQUEST);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.PASSWORD_BADREQUEST);
		}

		// 4.校验cpu
		Float cpu = configuration.getFloat("cpu");
		if (null == cpu) {
			logger.error("--->cpu---1-->:\t" + ExceptionMsg.CPU_BADREQUEST_REDIS);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.CPU_BADREQUEST_REDIS);
		}
		if (!ParameterCheckingHelp.checkCpuForRedis(cpu.intValue())) {
			logger.error("--->cpu---2-->cpu:\t" + cpu.intValue() + "\n" + ExceptionMsg.CPU_BADREQUEST_REDIS);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.CPU_BADREQUEST_REDIS);
		}

		// 5. 校验memory
		Float memory = configuration.getFloat("memory");
		if (null == memory) {
			logger.error("--->memory---1-->:\t" + ExceptionMsg.MEMORY_BADREQUEST_REDIS);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.MEMORY_BADREQUEST_REDIS);
		}
		if (!ParameterCheckingHelp.checkMemoryForRedis(memory.intValue())) {
			logger.error("--->memory---2-->memory:\t" + memory + "\n" + ExceptionMsg.MEMORY_BADREQUEST_REDIS);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.MEMORY_BADREQUEST_REDIS);
		}

		// 6. 校验capacity
		Float capacity = configuration.getFloat("capacity");
		if (null == capacity) {
			logger.error("--->capacity---1-->:\t" + ExceptionMsg.CAPACITY_BADREQUEST_REDIS);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.CAPACITY_BADREQUEST_REDIS);
		}
		if (!ParameterCheckingHelp.checkCapacityForRedis(capacity.intValue())) {
			logger.error("--->capacity---2-->capacity:\t" + capacity + "\n" + ExceptionMsg.CAPACITY_BADREQUEST_REDIS);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.CAPACITY_BADREQUEST_REDIS);
		}
	}

	private void checkReplicas(JSONObject configuration) throws BrokerException {
		Integer replicas = configuration.getInteger("replicas");
		if (null == replicas) {
			logger.error("--->replicas---1-->:\t" + ExceptionMsg.REPLICAS_BADREQUEST_REDIS);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.REPLICAS_BADREQUEST_REDIS);
		}
		if (!ParameterCheckingHelp.checkReplicasForRedisMs(replicas.intValue())) {
			logger.error("--->replicas---2-->replicas:\t" + replicas.intValue() + "\n" + ExceptionMsg.REPLICAS_BADREQUEST_REDIS);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.REPLICAS_BADREQUEST_REDIS);
		}
	}

	/**
	 * 校验redis的哨兵资源
	 *
	 * @param configuration
	 * @throws BrokerException
	 */
	private void checkSentinelResource(JSONObject configuration) throws BrokerException {
		// 1. 校验哨兵实例个数
		Integer sentinelNum = configuration.getInteger("sentinelNum");
		if (null == sentinelNum) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!Global.SENTINEL_NUM.contains(sentinelNum.intValue())) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 2. 校验哨兵CPU
		Float sentinelCpu = configuration.getFloat("sentinelCPU");
		if (null == sentinelCpu) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkSentinelCpuForRedis(sentinelCpu.intValue())) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 3. 校验哨兵内存
		Float sentinelMemory = configuration.getFloat("sentinelMemory");
		if (null == sentinelNum) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkSentinelMemoryForRedis(sentinelMemory.intValue())) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
	}

}
