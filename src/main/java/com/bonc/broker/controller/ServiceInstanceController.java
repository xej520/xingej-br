package com.bonc.broker.controller;

import com.alibaba.fastjson.JSONObject;
import com.bonc.broker.SpringApplicationContext;
import com.bonc.broker.common.*;
import com.bonc.broker.entity.BrokerOptLog;
import com.bonc.broker.entity.ServiceInstance;
import com.bonc.broker.entity.ServiceInstanceBinding;
import com.bonc.broker.exception.BrokerException;
import com.bonc.broker.exception.ExceptionMsg;
import com.bonc.broker.repository.ServiceInstanceBindingRepo;
import com.bonc.broker.service.DaoService;
import com.bonc.broker.service.ICheckParameters;
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
	public ResponseEntity<?> provisioning(@PathVariable("instance_id") String instanceId,
										  @RequestParam(value = "accepts_incomplete", required = false) Boolean acceptsIncomplete,
										  @RequestBody JSONObject requestBody) {
		// 1. 参数校验
		try {
			logger.info("[provisioning]\tinstanceID:\t" + instanceId + "\nrequestBody:\t" + requestBody.toJSONString());
			checkProvisioning(instanceId, acceptsIncomplete, requestBody);
		} catch (BrokerException e) {
			logger.error("--->provisioning opt:\t" + e.getMessage());
			return new ResponseEntity<>(e.getMessage(), e.getCode());
		}

		// 2. 更新操作记录表
		logger.info("[provisioning]\tstart save broker log into BrokerLog table!" + "\tinstanceId:\t" + instanceId);
		String id;
		try {
			String planId = requestBody.getString("plan_id");
			id = daoService.saveBrokerLog(planId, instanceId, Global.OPT_MYSQL_PROVISIONING);
		} catch (BrokerException e) {
			logger.error("[provisioning]\tstart save broker log into BrokerLog table!---->failed!" + "\tinstanceId:\t" + instanceId);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// 3. 异步(创建实例)
		logger.info("[provisioning]\tstart build data!" + "\tinstanceId:\t" + instanceId);
		Map<String, String> data = buildData(id, instanceId, requestBody);
		String catalog = GlobalHelp.getCatalogType(data.get("plan_id"));
		logger.info("[createInstance]\tcreate " + catalog + " thread![create worker]" + "\tinstanceId:\t" + instanceId);
		ExecuteHelper.addPool(catalog, Global.CREATE_WORKER, data);

		// 4. 返回
		logger.info("[provisioning]---return--operationId:\t" + id + "\tinstanceId:\t" + instanceId);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseEntityHelp.setOperation(id));
	}

	@PatchMapping
	public ResponseEntity<?> updateInstance(@PathVariable("instance_id") String instanceId,
											@RequestParam(value = "accepts_incomplete", required = false) Boolean acceptsIncomplete,
											@RequestBody JSONObject requestBody) {
		// 1. 参数校验
		try {
			logger.info("[updateInstance]\tinstanceID:\t" + instanceId + "\nrequestBody:\t" + requestBody.toJSONString());
			checkUpdateInstance(instanceId, acceptsIncomplete, requestBody);
		} catch (BrokerException e) {
			return new ResponseEntity<>(e.getMessage(), e.getCode());
		}

		// 2. 更新操作记录表
		logger.info("[updateInstance]\tstart save broker log into BrokerLog table!" + "\tinstanceId:\t" + instanceId);
		String id;
		try {
			String planId = requestBody.getString("plan_id");
			id = daoService.saveBrokerLog(planId, instanceId, Global.OPT_MYSQL_UPDATE);
		} catch (BrokerException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// 3. 异步 更新
		logger.info("[updateInstance]\tstart build data!" + "\tinstanceId:\t" + instanceId);
		Map<String, String> data = buildData(id, instanceId, requestBody);
		String catalog = Global.SERVICE_ID_CATALOG.get(data.get("service_id"));

		logger.info("[updateInstance]\tcreate " + catalog + " thread![update worker]" + "\tinstanceId:\t" + instanceId);
		ExecuteHelper.addPool(catalog, Global.UPDATE_WORKER, data);

		// 4. 返回
		logger.info("[provisioning]---return--operationId:\t" + id + "\tinstanceId:\t" + instanceId);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseEntityHelp.setOperation(id));
	}

	@DeleteMapping
	public ResponseEntity<?> deleteInstance(@PathVariable("instance_id") String instanceId,
											@RequestParam("service_id") String serviceId,
											@RequestParam("plan_id") String planId,
											@RequestParam(value = "accepts_incomplete", required = false) Boolean acceptsIncomplete) {
		// 1. 参数校验
		try {
			logger.info("[deleteInstance]\tinstanceID:\t" + instanceId + "\tserviceID:\t" + serviceId + "\tplanID:\t" + planId);
			checkDeleteInstance(instanceId, serviceId, planId, acceptsIncomplete);
		} catch (BrokerException e) {
			return new ResponseEntity<>(e.getMessage(), e.getCode());
		}

		// 2. 更新操作记录表
		logger.info("[deleteInstance]\tstart save broker log into BrokerLog table!" + "\tinstanceId:\t" + instanceId);
		String id;
		try {
			id = daoService.saveBrokerLog(planId, instanceId, Global.OPT_MYSQL_DELETE);
		} catch (BrokerException e) {
			logger.error("[deleteInstance]\tstart save broker log into BrokerLog table failed!" + "\tinstanceId:\t" + instanceId);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// 3. 异步 删除
		logger.info("[deleteInstance]\tstart build data!" + "\tinstanceId:\t" + instanceId);
		Map<String, String> data = buildData(id, instanceId, null);
		String catalog = Global.SERVICE_ID_CATALOG.get(serviceId);

		logger.info("[deleteInstance]\tcreate " + catalog + " thread![delete worker]" + "\tinstanceId:\t" + instanceId);
		ExecuteHelper.addPool(catalog, Global.DELETE_WORKER, data);

		// 4. 返回
		logger.info("[deleteInstance]---return--operationId:\t" + id + "\tinstanceId:\t" + instanceId);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseEntityHelp.setOperation(id));
	}

	/**
	 * 获取实例
	 *
	 * @param instanceId
	 * @return
	 */
	@GetMapping
	public ResponseEntity<?> getServiceInstance(@PathVariable("instance_id") String instanceId) {
		// 1. 获取实例
		logger.info("[getServiceInstance]:\tinstance_id:\t" + instanceId);
		ServiceInstance serviceInstance = daoService.getServiceInstance(instanceId);
		if (null == serviceInstance) {
			logger.error("[getServiceInstance]:\t---query--serviceInstance table--failed---by instanceId:\t" + instanceId);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST);
		}

		// 2. 返回(同步)
		logger.info("[getServiceInstance]:\treturn: serviceInstance\t" + JSONObject.toJSONString(serviceInstance) + "\tinstanceId:\t" + instanceId);
		return ResponseEntity.status(HttpStatus.OK).body(ResponseEntityHelp.setServiceInstance(serviceInstance.getServiceId(), serviceInstance.getPlanId(), serviceInstance.getParametersObject()));
	}

	@ResponseBody
	@RequestMapping(value = {"/last_operation"}, method = RequestMethod.GET)
	public ResponseEntity<?> getLastOperation(@RequestParam("operation") String operation) {
		// 1. 查询操作记录表
		logger.info("[getLastOperation]:\toperationId:\t" + operation);
		BrokerOptLog brokerOptLog = daoService.getBrokerLogRepo(operation);

		if (null == brokerOptLog) {
			logger.error("[Get last operation]:\tquery brokerOptLog table failed!\toperation:\t" + operation);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST);
		}

		logger.info("[getLastOperation]:\treturn:\t" + operation);
		return ResponseEntity.status(HttpStatus.OK).body(ResponseEntityHelp.setLastOperation(brokerOptLog.getState()));
	}

	/**
	 * @param id          操作记录表的主键
	 * @param instanceId  实例ID
	 * @param requestBody 创建实例时，传递过来的请求体
	 * @return
	 */
	private Map<String, String> buildData(String id, String instanceId, JSONObject requestBody) {

		Map<String, String> data = new HashMap<>(16);

		data.put("id", id);
		data.put("instance_id", instanceId);

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
			logger.error("--->instanceID:\t" + ExceptionMsg.INSTANCEID_BADREQUEST + "\tinstanceId:\t" + instanceId);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.INSTANCEID_BADREQUEST);
		}

		// 2. 校验instance_id 是否已经存在了
		ServiceInstance instanceIsExists = daoService.getServiceInstance(instanceId);
		if (null != instanceIsExists) {
			logger.error("--->instanceIsExists:\t" + ExceptionMsg.INSTANCEID_CONFLICT + "\tinstanceId:\t" + instanceId);
			throw new BrokerException(HttpStatus.CONFLICT, ExceptionMsg.INSTANCEID_CONFLICT);
		}

		// 3. 校验accepts_incomplete
		if (null != acceptsIncomplete && false == acceptsIncomplete.booleanValue()) {
			logger.error("--->acceptsIncomplete:\t" + HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase() + "\tinstanceId:\t" + instanceId);
			throw new BrokerException(HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase());
		}

		// 下面4-6校验请求体
		// 4. 校验serviceId
		String serviceId = requestBody.getString("service_id");
		if (StringUtils.isBlank(serviceId)) {
			logger.error("--->serviceId--1-->:\t" + ExceptionMsg.SERVICEID_BADREQUEST + "\tinstanceId:\t" + instanceId);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.SERVICEID_BADREQUEST);
		}
		if (!ParameterCheckingHelp.checkServiceId(serviceId)) {
			logger.error("--->serviceId--2-->:\t" + ExceptionMsg.SERVICEID_BADREQUEST + "\tinstanceId:\t" + instanceId + "\tserviceId:\t" + serviceId);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.SERVICEID_BADREQUEST);
		}

		// 5. 校验planId
		String planId = requestBody.getString("plan_id");
		if (StringUtils.isBlank(planId)) {
			logger.error("--->plan_id--1-->:\t" + ExceptionMsg.PLANID_BADREQUEST + "\tinstanceId:\t" + instanceId);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.PLANID_BADREQUEST);
		}
		logger.info("-----planId:\t" + planId);
		if (!ParameterCheckingHelp.checkPlanId(planId)) {
			logger.error("--->plan_id--2-->:\t" + ExceptionMsg.PLANID_BADREQUEST + "\tinstanceId:\t" + instanceId + "\tplanID:\t" + planId);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.PLANID_BADREQUEST);
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
			logger.error("--->parameters--->:\t" + ExceptionMsg.PARAMETERS_NOTFOUND);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.PARAMETERS_NOTFOUND);
		}

		JSONObject configuration = parameters.getJSONObject("configuration");
		if (null == configuration) {
			logger.error("--->configuration--->:\t" + ExceptionMsg.PARAMETERS_NOTFOUND);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.CONFIGURATION_NOTFOUND);
		}

		String tenantId = parameters.getString("tenant_id");
		if (null == tenantId) {
			logger.error("--->parameters---tenant_id>:\t" + ExceptionMsg.TENANT_ID_NOTFOUND);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.TENANT_ID_NOTFOUND);
		}

		String catalogType = GlobalHelp.getCatalogType(planId);
		try {
			checkProvisionParameters(catalogType, tenantId, planId, configuration);
		}catch (BrokerException e) {
			throw e;
		}

	}

	private void checkUpdateInstance(String instanceId, Boolean acceptsIncomplete, JSONObject requestBody) throws BrokerException {
		// 1. 校验instanceID
		// 1.1 校验instance_id格式 是否符合要求
		if (!ParameterCheckingHelp.checkInstanceId(instanceId)) {
			logger.error("--->instanceId---1-->:\t" + ExceptionMsg.INSTANCEID_BADREQUEST + "\tinstanceId:\t" + instanceId);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.INSTANCEID_BADREQUEST);
		}

		// 1.2 校验instance_id 是否存在
		ServiceInstance instanceIsExists = daoService.getServiceInstance(instanceId);
		if (null == instanceIsExists) {
			logger.error("--->instanceIsExists---2-->:\t" + ExceptionMsg.INSTANCEID_NOTFOUND + "\tinstanceId:\t" + instanceId);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.INSTANCEID_NOTFOUND);
		}

		// 2. 校验accepts_incomplete
		if (null != acceptsIncomplete && false == acceptsIncomplete.booleanValue()) {
			logger.error("--->accepts_incomplete----->:\t" + HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase() + "\tinstanceId:\t" + instanceId);
			throw new BrokerException(HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase());
		}

		// 3. 校验service_id
		String serviceId = requestBody.getString("service_id");
		if (StringUtils.isBlank(serviceId)) {
			logger.error("--->service_id----->:\t" + ExceptionMsg.SERVICEID_BADREQUEST + "\tinstanceId:\t" + instanceId);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.SERVICEID_BADREQUEST);
		}
		if (!ParameterCheckingHelp.checkServiceId(serviceId)) {
			logger.error("--->serviceId----->:\t" + ExceptionMsg.SERVICEID_BADREQUEST + "\tinstanceId:\t" + instanceId + "\tserviceId:\t" + serviceId);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.SERVICEID_BADREQUEST);
		}

		// 4. 校验cpu，memory，capacity
		try {
			checkUpdateInstanceParameters(instanceId, serviceId, requestBody);
		} catch (BrokerException e) {
			throw e;
		}

	}

	private void checkDeleteInstance(String instanceId, String serviceId, String planId, Boolean acceptsIncomplete) throws BrokerException {
		// 1. 校验instanceID
		// 1.1 校验instance_id格式 是否符合要求
		if (!ParameterCheckingHelp.checkInstanceId(instanceId)) {
			logger.error("--->instanceId---1-->:\t" + ExceptionMsg.INSTANCEID_BADREQUEST + "\tinstanceId:\t" + instanceId);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.INSTANCEID_BADREQUEST);
		}

		// 1.2 校验instance_id 是否存在
		ServiceInstance instanceIsExists = daoService.getServiceInstance(instanceId);
		if (null == instanceIsExists) {
			logger.error("--->instanceIsExists---2-->:\t" + ExceptionMsg.INSTANCEID_NOTFOUND + "\tinstanceId:\t" + instanceId);
			throw new BrokerException(HttpStatus.GONE, ExceptionMsg.INSTANCEID_NOTFOUND);
		}

		// 2. 校验accepts_incomplete
		if (null != acceptsIncomplete && false == acceptsIncomplete.booleanValue()) {
			logger.error("--->acceptsIncomplete----->:\t" + ExceptionMsg.UNPROCESSABLE_ENTITY + "\t" + HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase() + "\tinstanceId:\t" + instanceId);
			throw new BrokerException(HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase());
		}

		// 这里包括了mysql，redis的serviceId 校验
		// 3. 校验service_id
		if (!Global.SERVICE_ID.contains(serviceId)) {
			logger.error("--->SERVICE_ID----->:\t" + ExceptionMsg.SERVICEID_BADREQUEST + "\tinstanceId:\t" + instanceId + "\tserviceId:\t" + serviceId);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.SERVICEID_BADREQUEST);
		}

		// 4. 校验plan_id
		if (!Global.PLAN_ID.contains(planId)) {
			logger.error("--->planId----->:\t" + ExceptionMsg.PLANID_NOTFOUND + "\tinstanceId:\t" + instanceId + "\tplanID:\t" + planId);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.PLANID_NOTFOUND);
		}

		// 5. 校验此instance实例是否还存在binding对象
		ServiceInstanceBinding serviceInstanceBinding = serviceInstanceBindingRepo.findByInstanceId(instanceId);
		if (null != serviceInstanceBinding) {
			logger.warn("delete instanceId: %d  failed; This instance ID also has a binding object", instanceId);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.BINDING_INSTANCE_FORBID + "\tbindingId:\t" + serviceInstanceBinding.getBindingId());
		}
	}

	private void checkUpdateInstanceParameters(String instanceId, String serviceId, JSONObject requestBody) throws BrokerException {
		JSONObject parameters = requestBody.getJSONObject("parameters");
		if (null == parameters) {
			logger.error("[checkUpdateInstanceParameters]--->parameters----->:\t" + ExceptionMsg.PARAMETERS_NOTFOUND);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.PARAMETERS_NOTFOUND);
		}
		String catalog = GlobalHelp.getCatalogTypeByServiceId(serviceId);

		checkUpdateParameters(catalog, instanceId, parameters);
	}

	private void checkProvisionParameters(String appType, String tenantId, String planId, JSONObject configuration) throws BrokerException {
		logger.info("-->check create parameters start:\t" + appType);
		String bean = GlobalHelp.getFullBeanPathBase(appType, "CheckParametersImpl");
		ICheckParameters iCheckParameters;
		try {
			iCheckParameters = (ICheckParameters) SpringApplicationContext.getBean(Class.forName(bean));
		} catch (Exception e) {
			logger.error("--<T>-->check create parameters failed:\t" + e.getMessage());
			throw new BrokerException(HttpStatus.INTERNAL_SERVER_ERROR,  e.getMessage());
		}

		try {
			iCheckParameters.checkCreateInstanceParameters(appType, tenantId, planId, configuration);
		}catch (BrokerException e) {
			throw e;
		}

	}

	/**
	 * 泛型和接口的使用
	 *
	 * @param appType
	 * @param instanceId
	 * @param checkParameters
	 * @throws BrokerException
	 */
	private void checkUpdateParameters(String appType, String instanceId, JSONObject checkParameters) throws BrokerException {
		String bean = GlobalHelp.getFullBeanPathBase(appType, "CheckParametersImpl");
		ICheckParameters iCheckParameters;
		try {
			iCheckParameters = (ICheckParameters) SpringApplicationContext.getBean(Class.forName(bean));
		} catch (Exception e) {
			logger.error("-<T>-->check update parameters failed:\t" + e.getMessage());
			throw new BrokerException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}

		try {
			iCheckParameters.checkUpdateInstanceParameters(instanceId, checkParameters);
		}catch (BrokerException e) {
			throw e;
		}

	}

}
