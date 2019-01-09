package com.bonc.broker.controller;

import com.alibaba.fastjson.JSONObject;
import com.bonc.broker.SpringApplicationContext;
import com.bonc.broker.common.Global;
import com.bonc.broker.common.GlobalHelp;
import com.bonc.broker.entity.ServiceInstance;
import com.bonc.broker.entity.ServiceInstanceBinding;
import com.bonc.broker.exception.BrokerException;
import com.bonc.broker.exception.ExceptionMsg;
import com.bonc.broker.repository.ServiceInstanceRepo;
import com.bonc.broker.service.DaoService;
import com.bonc.broker.service.IBindingWorker;
import com.bonc.broker.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * @author xingej
 */
@RestController
@RequestMapping(value = "/v2/service_instances/{instance_id}/service_bindings/{binding_id}")
public class ServiceInstanceBindingController {
	/**
	 * 日志记录
	 */
	private static Logger logger = LoggerFactory.getLogger(ServiceInstanceBindingController.class);

	@Autowired
	private ServiceInstanceRepo serviceInstanceRepo;
	@Autowired
	private DaoService daoService;

	@ResponseBody
	@RequestMapping(value = {"/last_operation"}, method = RequestMethod.GET)
	public ResponseEntity<?> getLastOperation(
			@PathVariable("instance_id") String instanceId, @PathVariable("binding_id") String bindingId) {
		logger.warn("[binding getLastOperation]--->getLastOperation---not support:\t" + "instanceID:\t" + instanceId + ";\tbindingID:\t" + bindingId);
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase());
	}

	@PutMapping
	public ResponseEntity<?> binding(@PathVariable("instance_id") String instanceId,
									 @PathVariable("binding_id") String bindingId,
									 @RequestParam(value = "accepts_incomplete", required = false) Boolean acceptsIncomplete,
									 @RequestBody JSONObject requestBody) {
		// 1. 参数校验
		try {
			logger.info("[binding ]bindingId:\t" + bindingId + "\tinstanceID:\t" + instanceId + "\nrequestBody:\t" + requestBody.toJSONString());
			checkBinding(instanceId, bindingId, acceptsIncomplete, requestBody);
		} catch (BrokerException e) {
			logger.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), e.getCode());
		}

		logger.info("[binding ]\tstart binding info:\n" + "instanceId:\t" + instanceId + "\tbindingId:\t" + bindingId);
		// 2. 构建binding信息
		String serviceId = requestBody.getString("service_id");
		JSONObject credentials;
		try {
			credentials = buildBindingInfo(GlobalHelp.getCatalogTypeByServiceId(serviceId), instanceId);
		} catch (BrokerException e) {
			logger.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), e.getCode());
		}

		logger.info("[binding ]\tsave binding info into serviceInstance table:\n" + "instanceId:\t" + instanceId + "\tbindingId:\t" + bindingId);
		// 3. 回写到数据库ServiceBinding表
		ServiceInstanceBinding serviceInstanceBinding;
		try {
			serviceInstanceBinding = daoService.saveServiceInstanceBinding(bindingId, instanceId, credentials);
		} catch (BrokerException e) {
			logger.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), e.getCode());
		}

		// 4. 返回
		logger.info("[binding ]---return binding info-----:\n" + "instanceId:\t" + instanceId + "\tbindingId:\t" + bindingId + "\n" + JSONObject.toJSONString(serviceInstanceBinding));
		return ResponseEntity.status(HttpStatus.OK).body(credentials);
	}

	@DeleteMapping
	public ResponseEntity unBinding(@PathVariable("instance_id") String instanceId,
									@PathVariable("binding_id") String bindingId, @RequestParam("service_id") String serviceId,
									@RequestParam("plan_id") String planId,
									@RequestParam(value = "accepts_incomplete", required = false) Boolean acceptsIncomplete) {
		try {
			// 1. 参数校验
			logger.info("[unbinding ]\tstart unbinding info:bindingID:\t" + bindingId + "\tinstanceID:\t" + instanceId);
			checkUnBinding(instanceId, bindingId, serviceId, planId, acceptsIncomplete);

			// 2. 更新数据库(ServiceBinding表)，删除记录
			logger.info("[unbinding ]\tstart delete binding info in serviceInstance table :bindingID:\t" + bindingId + "\tinstanceID:\t" + instanceId);
			daoService.deleteServiceInstanceBinding(bindingId);
		} catch (BrokerException e) {
			logger.error("[unbinding ]\tdelete binding info:bindingID:\t" + bindingId + "\tinstanceID:\t" + instanceId + "\tfailed!");
			return new ResponseEntity(e.getMessage(), e.getCode());
		}

		// 3. 返回
		logger.info("[unbinding ]\treturn unbinding info:bindingID:\t" + bindingId + "\tinstanceID:\t" + instanceId + "\tOK");
		return ResponseEntity.status(HttpStatus.OK).body(new JSONObject());
	}

	@GetMapping
	public ResponseEntity<?> getBinding(@PathVariable("binding_id") String bindingId) {
		// 1. 查询数据库，获取绑定信息
		logger.info("[getBinding]\tstart get binding info:\tbindingId:\t" + bindingId);
		ServiceInstanceBinding serviceInstanceBinding = daoService.getServiceInstanceBinding(bindingId);
		if (null == serviceInstanceBinding) {
			logger.error("[getBinding]\tget binding info: bindingId:\t" + bindingId + "\tfailed!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionMsg.BINDINGID_NOTFOUND + "\tbinding_id:\t" + bindingId);
		}

		// 2. 返回
		logger.info("[getBinding]\treturn binding info:\t" + serviceInstanceBinding.getCredentialsObject() + "\tOK!");
		return ResponseEntity.status(HttpStatus.OK).body(serviceInstanceBinding.getCredentialsObject());
	}

	private void checkBinding(String instanceId, String bindingId, Boolean acceptsIncomplete, JSONObject requestBody) throws BrokerException {
		// 1. 校验instance_id格式 是否符合要求
		if (StringUtils.isBlank(instanceId)) {
			logger.error("[checkBinding]--->instanceId:\t" + ExceptionMsg.INSTANCEID_BADREQUEST);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.INSTANCEID_BADREQUEST);
		}

		// 2. 校验instance_id 是否存在? 查看是否存在对应的实例ServiceInstance对象，就是查看binding的对象是否存在
		Optional<ServiceInstance> instanceIsExists = serviceInstanceRepo.findById(instanceId);
		if (!instanceIsExists.isPresent()) {
			logger.error("[checkBinding]--->instanceIsExists:\t" + ExceptionMsg.INSTANCEID_NOTFOUND);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.INSTANCEID_NOTFOUND);
		}

		// 3. 校验service_id
		String serviceId = requestBody.getString("service_id");
		if (null == serviceId) {
			logger.error("[checkBinding]--->service_id:\t" + ExceptionMsg.SERVICEID_BADREQUEST);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.SERVICEID_BADREQUEST);
		}

		if (!Global.SERVICE_ID.contains(serviceId)) {
			logger.error("[checkBinding]--->service_id:\t" + ExceptionMsg.SERVICEID_BADREQUEST);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.SERVICEID_BADREQUEST);
		}

		// 4. 校验plan_id
		String planId = requestBody.getString("plan_id");
		if (null == planId) {
			logger.error("[binding interface]--1->plan_id:\t" + ExceptionMsg.PLANID_BADREQUEST);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.PLANID_BADREQUEST);
		}
		if (!Global.PLAN_ID.contains(planId)) {
			logger.error("[binding interface]-2-->plan_id:\t" + ExceptionMsg.PLANID_BADREQUEST);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.PLANID_BADREQUEST);
		}

		// 5. 校验binding格式 是否符合要求
		if (StringUtils.isBlank(bindingId)) {
			logger.error("[binding interface]--->bindingId:\t" + ExceptionMsg.BINDING_BADREQUEST);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.BINDING_BADREQUEST);
		}

		// 5.1 校验bindingID 是否已经binding过了
		ServiceInstanceBinding serviceInstanceBinding = daoService.getServiceInstanceBinding(bindingId);
		if (null != serviceInstanceBinding) {
			logger.error("[binding interface]--->serviceInstanceBinding:\t" + ExceptionMsg.BINDING_INSTANCE_CONFLICT);
			throw new BrokerException(HttpStatus.CONFLICT, ExceptionMsg.BINDING_INSTANCE_CONFLICT);
		}

		// 3. 仅支持同步，binding绑定；true表示: 支持异步请求
		if (null != acceptsIncomplete && true == acceptsIncomplete.booleanValue()) {
			logger.error("[binding interface]--->acceptsIncomplete:\t" + HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase());
			throw new BrokerException(HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase());
		}
	}

	private void checkUnBinding(String instanceId, String bindingId, String serviceId, String planId, Boolean acceptsIncomplete)
			throws BrokerException {
		// 1. 校验instance_id格式 是否符合要求
		if (StringUtils.isBlank(instanceId)) {
			logger.error("[binding interface]--->instanceId:\t" + ExceptionMsg.INSTANCEID_BADREQUEST);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.INSTANCEID_BADREQUEST);
		}

		// 2. 校验instance_id 是否存在?
		Optional<ServiceInstance> instanceIsExists = serviceInstanceRepo.findById(instanceId);
		if (!instanceIsExists.isPresent()) {
			logger.error("[binding interface]--->instanceIsExists:\t" + ExceptionMsg.INSTANCEID_NOTFOUND);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.INSTANCEID_NOTFOUND);
		}

		// 3. 校验service_id
		if (!Global.SERVICE_ID.contains(serviceId)) {
			logger.error("[binding interface]--->serviceId:\t" + ExceptionMsg.SERVICEID_BADREQUEST);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.SERVICEID_BADREQUEST);
		}

		// 4. 校验plan_id
		if (!Global.PLAN_ID.contains(planId)) {
			logger.error("[binding interface]--->planId:\t" + ExceptionMsg.PLANID_BADREQUEST);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.PLANID_BADREQUEST);
		}

		// 5. 校验binding格式 是否符合要求
		if (StringUtils.isBlank(bindingId)) {
			logger.error("[binding interface]--->bindingId:\t" + ExceptionMsg.BINDING_BADREQUEST);
			throw new BrokerException(HttpStatus.BAD_REQUEST, ExceptionMsg.BINDING_BADREQUEST);
		}

		// 5.1 校验bindingID 是否bindingId 是否存在？根据bindingID 查询是否存在对应的实例ID
		ServiceInstanceBinding serviceInstanceBinding = daoService.getServiceInstanceBinding(bindingId);
		if (null == serviceInstanceBinding) {
			logger.error("[checkUnBinding]---binding_id:\t" + bindingId + " not exists!");
			throw new BrokerException(HttpStatus.GONE, ExceptionMsg.BINDINGID_NOTFOUND);
		}

		// 6. 仅支持同步，binding绑定；true表示: 支持异步请求
		if (null != acceptsIncomplete && true == acceptsIncomplete.booleanValue()) {
			logger.error("[binding interface]--->acceptsIncomplete:\t" + HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase());
			throw new BrokerException(HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase());
		}
	}

	private JSONObject buildBindingInfo(String appType, String instanceId) throws BrokerException {
		String bean = GlobalHelp.getFullBeanPathBase(appType, "BindingWorkerImpl");
		IBindingWorker bindingWorker;
		try {
			bindingWorker = (IBindingWorker) SpringApplicationContext.getBean(Class.forName(bean));
		} catch (Exception e) {
			logger.error("--->build binding info failed:\t" + e.getMessage());
			throw new BrokerException("--->build binding info failed:\t" + e.getMessage());
		}
		return bindingWorker.buildBindingInfo(instanceId);
	}

}
