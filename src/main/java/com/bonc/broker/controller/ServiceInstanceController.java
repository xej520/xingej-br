package com.bonc.broker.controller;

import com.alibaba.fastjson.JSONObject;
import com.bonc.broker.common.*;
import com.bonc.broker.entity.BrokerOptLog;
import com.bonc.broker.entity.ServiceInstance;
import com.bonc.broker.entity.ServiceInstanceBinding;
import com.bonc.broker.exception.BrokerException;
import com.bonc.broker.repository.ServiceInstanceBindingRepo;
import com.bonc.broker.service.DaoService;
import com.bonc.broker.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xingej
 */

@RestController
@RequestMapping(value = "/v2/service_instances/{instance_id}")
public class ServiceInstanceController {
	/**
	 * 日志记录
	 */
	private static Logger logger = LoggerFactory.getLogger(ServiceInstanceController.class);

	@Autowired
	private DaoService daoService;

	@Autowired
	private ServiceInstanceBindingRepo serviceInstanceBindingRepo;

	@PutMapping
	public ResponseEntity<?> provisioning(@PathVariable("instance_id") String instance_id,
										  @RequestParam(value = "accepts_incomplete", required = false) Boolean accepts_incomplete,
										  @RequestBody JSONObject requestBody) {
		// 1. 参数校验
		try {
			checkProvisioning(instance_id, accepts_incomplete, requestBody);
		} catch (BrokerException e) {
			return new ResponseEntity<>(e.getMessage(), e.getCode());
		}

		// 2. 更新操作记录表
		String id;
		try {
			id = daoService.saveBrokerLog(instance_id, Global.OPT_MYSQL_PROVISIONING);
		} catch (BrokerException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// 3. 异步(创建实例)
		Map<String, String> data = buildData(id, instance_id, requestBody);

		String catalog = GlobalHelp.getCatalogType(data.get("plan_id"));

		if (Global.MYSQL.equals(catalog)) {
			ExecuteHelper.addPool(AppTypeConst.APPTYPE_MYSQL, MysqlClusterConst.MYSQL_CLUSTER_CREATE, data);
		} else {
			ExecuteHelper.addPool(AppTypeConst.APPTYPE_REDIS, RedisClusterConst.REDIS_CLUSTER_CREATE, data);
		}
		logger.info("--create---mysql--id-----\t" + id);
		// 4. 返回
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseEntityHelp.setOperation(id));
	}


	@PatchMapping
	public ResponseEntity<?> updateInstance(@PathVariable("instance_id") String instanceId,
											@RequestParam(value = "accepts_incomplete", required = false) Boolean acceptsIncomplete,
											@RequestBody JSONObject requestBody) {
		// 1. 参数校验
		try {
			checkUpdateInstance(instanceId, acceptsIncomplete, requestBody);
		} catch (BrokerException e) {
			return new ResponseEntity<>(e.getMessage(), e.getCode());
		}

		// 2. 更新操作记录表

		String id;
		try {
			id = daoService.saveBrokerLog(instanceId, Global.OPT_MYSQL_UPDATE);
		}catch (BrokerException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// 3. 异步 更新
		Map<String, String> data = buildData(id, instanceId, requestBody);

		String serviceId = data.get("service_id");
		logger.info("----update---serviceId:\t" + serviceId);
		String catalog = GlobalHelp.getCatalogTypeByServiceId(serviceId);

		if (Global.MYSQL.equals(catalog)) {
			ExecuteHelper.addPool(AppTypeConst.APPTYPE_MYSQL, MysqlClusterConst.MYSQL_CLUSTER_UPDATE, data);
		} else {
			ExecuteHelper.addPool(AppTypeConst.APPTYPE_REDIS, RedisClusterConst.REDIS_CLUSTER_UPDATE, data);
		}

		// 4. 返回
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseEntityHelp.setOperation(id));
	}

	@DeleteMapping
	public ResponseEntity<?> deleteInstance(@PathVariable("instance_id") String instanceId,
											@RequestParam("service_id") String serviceId,
											@RequestParam("plan_id") String planId,
											@RequestParam(value = "accepts_incomplete", required = false) Boolean acceptsIncomplete) {
		// 1. 参数校验
		try {
			checkDeleteInstance(instanceId, serviceId, planId, acceptsIncomplete);
		} catch (BrokerException e) {
			return new ResponseEntity<>(e.getMessage(), e.getCode());
		}

		// 2. 更新操作记录表
		String id;
		try {
			 id = daoService.saveBrokerLog(instanceId, Global.OPT_MYSQL_DELETE);
		}catch (BrokerException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// 3. 异步 删除
		Map<String, String> data = buildData(id, instanceId, null);

		String catalog = GlobalHelp.getCatalogTypeByServiceId(serviceId);

		if (Global.MYSQL.equals(catalog)) {
			ExecuteHelper.addPool(AppTypeConst.APPTYPE_MYSQL, MysqlClusterConst.MYSQL_CLUSTER_DELETE, data);
		} else {
			ExecuteHelper.addPool(AppTypeConst.APPTYPE_REDIS, RedisClusterConst.REDIS_CLUSTER_DELETE, data);
		}

		// 4. 返回
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseEntityHelp.setOperation(id));
	}

	/**
	 * 获取实例
	 *
	 * @param instanceId
	 * @return
	 */
	@GetMapping
	public ResponseEntity<?> getServiceInstance(@PathVariable("instance_id") String instanceId){
		// 1. 获取实例
		ServiceInstance serviceInstance  = daoService.getServiceInstance(instanceId);

		if (null == serviceInstance) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST);
		}

		// 2. 返回(同步)
		return ResponseEntity.status(HttpStatus.OK).body(ResponseEntityHelp.setServiceInstance(serviceInstance.getServiceId(), serviceInstance.getPlanId(), serviceInstance.getParametersObject()));
	}

	@ResponseBody
	@RequestMapping(value = {"/last_operation"}, method = RequestMethod.GET)
	public ResponseEntity<?> getLastOperation(@RequestParam("operation") String operation) {
		// 1. 查询操作记录表
		BrokerOptLog brokerOptLog  = daoService.getBrokerLogRepo(operation);

		if (null == brokerOptLog) {
			logger.error("[Get last operation]:\tnot exists! operation:\t" + operation);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST);
		}

		return ResponseEntity.status(HttpStatus.OK).body(ResponseEntityHelp.setLastOperation(brokerOptLog.getState()));
	}

	/**
	 * @param id          操作记录表的主键
	 * @param instance_id 实例ID
	 * @param requestBody 创建实例时，传递过来的请求体
	 * @return
	 */
	private Map<String, String> buildData(String id, String instance_id, JSONObject requestBody) {

		Map<String, String> data = new HashMap<>(16);

		data.put("id", id);
		data.put("instance_id", instance_id);

		// 针对的是创建实例，更新实例操作
		if (null != requestBody) {

			JSONObject parameters = requestBody.getJSONObject("parameters");
			String tenantId = parameters.getString("tenant_id");
			if (null != tenantId) {
				data.put("tenant_id", tenantId);
			}

			String projectId = parameters.getString("project_id");
			if (null != projectId) {
				data.put("project_id", projectId);
			}

			String userId = parameters.getString("user_id");
			if (null != userId) {
				data.put("user_id", userId);
			}

			String configuration = parameters.getString("configuration");
			if (null != configuration) {
				data.put("configuration", configuration);
			}

			data.put("service_id", requestBody.getString("service_id"));
			data.put("parameters", requestBody.getString("parameters"));

			String planId = requestBody.getString("plan_id");
			// planID，并非必传参数
			if (null != planId) {
				data.put("plan_id", planId);
			}
		}
		return data;
	}

	/**
	 * 校验创建实例
	 *
	 * @param instanceId
	 * @param acceptsIncomplete
	 * @param requestBody
	 * @throws BrokerException
	 */
	private void checkProvisioning(String instanceId, Boolean acceptsIncomplete, JSONObject requestBody) throws BrokerException {
		// 校验公共参数mysql, redis共有的参数

		// 1. 校验instance_id格式 是否符合要求
		if (!ParameterCheckingHelp.checkInstanceId(instanceId)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 2. 校验instance_id 是否已经存在了
		ServiceInstance instanceIsExists = daoService.getServiceInstance(instanceId);
		if (null != instanceIsExists) {
			throw new BrokerException(HttpStatus.CONFLICT, HttpStatus.CONFLICT.getReasonPhrase());
		}

		// 3. 校验accepts_incomplete
		if (null != acceptsIncomplete && false == acceptsIncomplete.booleanValue()) {
			throw new BrokerException(HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase());
		}

		// 下面4-6校验请求体
		// 4. 校验serviceId
		String serviceId = requestBody.getString("service_id");
		if (StringUtils.isBlank(serviceId)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkServiceId(serviceId)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 5. 校验planId
		String planId = requestBody.getString("plan_id");
		if (StringUtils.isBlank(planId)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		logger.info("-----planId:\t" + planId);
		if (!ParameterCheckingHelp.checkPlanId(planId)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 6. 校验请求参数，如cpu，memory，version, password等
		try {
			// 注意：这里开始区分是mysql，还是redis
			checkProvisionParameters(planId, requestBody);
		} catch (BrokerException e) {
			throw new BrokerException(e.getCode(), e.getMessage());
		}
	}

	private void checkProvisionParameters(String planId, JSONObject requestBody) throws BrokerException {
		JSONObject parameters = requestBody.getJSONObject("parameters");
		if (null == parameters) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		JSONObject configuration = parameters.getJSONObject("configuration");
		if (null == configuration) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		String catalogType = GlobalHelp.getCatalogType(planId);

		switch (catalogType) {
			case "mysql":
				checkCreateParametersForMysql(planId, configuration);
				break;
			case "redis":
				checkCreateParametersForRedis(planId, configuration);
				break;
			default:
				throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
	}

	private void checkUpdateInstance(String instance_id, Boolean accepts_incomplete, JSONObject requestBody) throws BrokerException {
		// 1. 校验instanceID
		// 1.1 校验instance_id格式 是否符合要求
		if (!ParameterCheckingHelp.checkInstanceId(instance_id)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 1.2 校验instance_id 是否存在
		ServiceInstance instanceIsExists = daoService.getServiceInstance(instance_id);
		if (null == instanceIsExists) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 2. 校验accepts_incomplete
		if (null != accepts_incomplete && false == accepts_incomplete.booleanValue()) {
			throw new BrokerException(HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase());
		}

		// 3. 校验service_id
		String serviceId = requestBody.getString("service_id");
		if (StringUtils.isBlank(serviceId)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkServiceId(serviceId)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 4. 校验cpu，memory，capacity
		checkUpdateInstanceParameters(serviceId, requestBody);

	}

	private void checkDeleteInstance(String instanceId, String serviceId, String planId, Boolean acceptsIncomplete) throws BrokerException {
		// 1. 校验instanceID
		// 1.1 校验instance_id格式 是否符合要求
		if (!ParameterCheckingHelp.checkInstanceId(instanceId)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 1.2 校验instance_id 是否存在
		ServiceInstance instanceIsExists = daoService.getServiceInstance(instanceId);
		if (null == instanceIsExists) {
			throw new BrokerException(HttpStatus.GONE, HttpStatus.GONE.getReasonPhrase());
		}

		// 2. 校验accepts_incomplete
		if (null != acceptsIncomplete && false == acceptsIncomplete.booleanValue()) {
			throw new BrokerException(HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase());
		}

		// 这里包括了mysql，redis的serviceId 校验
		// 3. 校验service_id
		if (!Global.SERVICE_ID.contains(serviceId)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 4. 校验plan_id
		if (!Global.PLAN_ID.contains(planId)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 5. 校验此instance实例是否还存在binding对象
		ServiceInstanceBinding serviceInstanceBinding = serviceInstanceBindingRepo.findByInstanceId(instanceId);
		if (null != serviceInstanceBinding) {
			logger.warn("delete instanceId: %d  failed; This instance ID also has a binding object", instanceId);
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
	}

	private void checkCreateParametersForMysql(String planId, JSONObject configuration) throws BrokerException {

		// 1. 校验集群名称serviceName
		String serviceName = configuration.getString("serviceName");
		if (StringUtils.isBlank(serviceName)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkServiceName(serviceName)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		//todo
		//serviceName 是否要添加唯一性校验，同一租户，同mysql集群下，servicename 不能一样

		// 2. 校验副本数
		// mysql的ms模式，需要校验replicas参数
		String type = Global.PLAN_ID_SERVICE_MODE.get(planId);
		if (MysqlClusterConst.TYPE_MS.equals(type)) {
			Integer replicas = configuration.getInteger("replicas");
			if (null == replicas) {
				throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
			}
			if (!ParameterCheckingHelp.checkReplicasForMysqlMs(replicas.intValue())) {
				throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
			}
		}

		// 3. 校验version
		String version = configuration.getString("version");
		if (null == version) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkVersionForMysql(version)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 4. 校验密码password
		String password = configuration.getString("password");
		if (null == password) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkPassword(password)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 5.校验cpu
		Float cpu = configuration.getFloat("cpu");
		if (null == cpu) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkCpuForMysql(cpu.intValue())) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 6. 校验memory
		Float memory = configuration.getFloat("memory");
		if (null == memory) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkMemoryForMysql(memory.intValue())) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 7. 校验capacity
		Float capacity = configuration.getFloat("capacity");
		if (null == capacity) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkCapacityForMysql(capacity.intValue())) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
	}

	private void checkCreateParametersForRedis(String planId, JSONObject configuration) throws BrokerException {
		// Single模式下的参数，相当于基础参数，其他模式也会用到
		checkCreateParametersForRedisSingle(configuration);

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

	private void checkCreateParametersForRedisSingle(JSONObject configuration) throws BrokerException {
		// 1. 校验集群名称serviceName
		String serviceName = configuration.getString("serviceName");
		if (StringUtils.isBlank(serviceName)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkServiceName(serviceName)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		//todo
		// serviceName 唯一性校验

		// 2. 校验version
		String version = configuration.getString("version");
		if (null == version) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkVersionForRedis(version)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 3. 校验密码password
		String password = configuration.getString("password");
		if (null == password) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkPassword(password)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 4.校验cpu
		Float cpu = configuration.getFloat("cpu");
		if (null == cpu) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkCpuForRedis(cpu.intValue())) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 5. 校验memory
		Float memory = configuration.getFloat("memory");
		if (null == memory) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkMemoryForRedis(memory.intValue())) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 6. 校验capacity
		Float capacity = configuration.getFloat("capacity");
		if (null == capacity) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkCapacityForRedis(capacity.intValue())) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
	}

	private void checkReplicas(JSONObject configuration) throws BrokerException {
		Integer replicas = configuration.getInteger("replicas");
		if (null == replicas) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkReplicasForMysqlMs(replicas.intValue())) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
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

	private void checkUpdateInstanceParameters(String serviceId, JSONObject requestBody) throws BrokerException {
		JSONObject parameters = requestBody.getJSONObject("parameters");
		if (null == parameters) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		if (Global.MYSQL.equalsIgnoreCase(GlobalHelp.getCatalogTypeByServiceId(serviceId))) {
			checkUpdateInstanceParametersForMysql(parameters);
		} else {
			checkUpdateInstanceParametersForRedis(parameters);
		}
	}

	private void checkUpdateInstanceParametersForMysql(JSONObject parameters) throws BrokerException {
		// 3.1校验cpu
		Float cpu = parameters.getFloat("cpu");
		if (null == cpu) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkCpuForMysql(cpu.intValue())) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 3.2 校验memory
		Float memory = parameters.getFloat("memory");
		if (null == memory) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkMemoryForMysql(memory.intValue())) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 3.3 校验capacity
		Float capacity = parameters.getFloat("capacity");
		if (null == capacity) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkCapacityForMysql(capacity.intValue())) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
	}

	private void checkUpdateInstanceParametersForRedis(JSONObject parameters) throws BrokerException {
		// 1 校验cpu
		Float cpu = parameters.getFloat("cpu");
		if (null == cpu) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkCpuForRedis(cpu.intValue())) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 2 校验memory
		Float memory = parameters.getFloat("memory");
		if (null == memory) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkMemoryForRedis(memory.intValue())) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 3 校验capacity
		Float capacity = parameters.getFloat("capacity");
		if (null == capacity) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!ParameterCheckingHelp.checkCapacityForRedis(capacity.intValue())) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
	}

}
