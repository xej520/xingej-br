package com.bonc.broker.controller;

import com.alibaba.fastjson.JSONObject;
import com.bonc.broker.common.Global;
import com.bonc.broker.common.GlobalHelp;
import com.bonc.broker.common.K8sClient;
import com.bonc.broker.controller.mode.BindingMysqlInsideK8S;
import com.bonc.broker.controller.mode.BindingMysqlOutsideK8S;
import com.bonc.broker.controller.mode.BindingRedisOutK8s;
import com.bonc.broker.entity.ServiceInstance;
import com.bonc.broker.entity.ServiceInstanceBinding;
import com.bonc.broker.exception.BrokerException;
import com.bonc.broker.repository.ServiceInstanceRepo;
import com.bonc.broker.service.DaoService;
import com.bonc.broker.service.model.crd.mysql.DoneableMysql;
import com.bonc.broker.service.model.crd.mysql.MysqlList;
import com.bonc.broker.service.model.crd.redis.DoneableRedis;
import com.bonc.broker.service.model.crd.redis.RedisList;
import com.bonc.broker.service.model.mysql.MysqlCluster;
import com.bonc.broker.service.model.mysql.MysqlConfig;
import com.bonc.broker.service.model.mysql.MysqlServer;
import com.bonc.broker.service.model.redis.BindingNode;
import com.bonc.broker.service.model.redis.RedisCluster;
import com.bonc.broker.service.model.redis.ServiceStatus;
import com.bonc.broker.util.StringUtils;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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

	private MixedOperation<MysqlCluster, MysqlList, DoneableMysql, Resource<MysqlCluster, DoneableMysql>> k8sClientForMysql = K8sClient
			.getK8sClientForMysql();

	private MixedOperation<RedisCluster, RedisList, DoneableRedis, Resource<RedisCluster, DoneableRedis>> k8sClientForRedis = K8sClient
			.getK8sClientForRedis();


	@ResponseBody
	@RequestMapping(value = {"/last_operation"}, method = RequestMethod.GET)
	public ResponseEntity<?> getLastOperation(
			@PathVariable("instance_id") String instance_id, @PathVariable("binding_id") String binding_id) {

		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase());
	}

	@PutMapping
	public ResponseEntity<?> binding(@PathVariable("instance_id") String instanceId,
									 @PathVariable("binding_id") String bindingId,
									 @RequestParam(value = "accepts_incomplete", required = false) Boolean acceptsIncomplete,
									 @RequestBody JSONObject requestBody) {
		// 1. 参数校验
		try {
			checkBinding(instanceId, bindingId, acceptsIncomplete, requestBody);
		} catch (BrokerException e) {
			logger.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), e.getCode());
		}

		// 2. 构建binding信息
		String serviceId = requestBody.getString("service_id");
		JSONObject credentials;
		try {
			if (Global.MYSQL.equalsIgnoreCase(GlobalHelp.getCatalogTypeByServiceId(serviceId))) {
				credentials = buildBindingInfoForMysql(instanceId);
			} else {
				credentials = buildBindingInfoForRedis(instanceId);
			}
		} catch (BrokerException e) {
			logger.error(e.getMessage());
			return new ResponseEntity<>(e.getMessage(), e.getCode());
		}

		// 3. 回写到数据库ServiceBinding表
		ServiceInstanceBinding serviceInstanceBinding;
		try {
			serviceInstanceBinding = daoService.saveServiceInstanceBinding(bindingId, instanceId, credentials);
		} catch (BrokerException e) {
			return new ResponseEntity<>(e.getMessage(), e.getCode());
		}

		logger.info("---回写到数据库ServiceBinding表-----:\t" + JSONObject.toJSONString(serviceInstanceBinding));
		// 4. 返回
		return ResponseEntity.status(HttpStatus.OK).body(credentials);
	}

	@DeleteMapping
	public ResponseEntity unBinding(@PathVariable("instance_id") String instanceId,
									@PathVariable("binding_id") String bindingId, @RequestParam("service_id") String serviceId,
									@RequestParam("plan_id") String planId,
									@RequestParam(value = "accepts_incomplete", required = false) Boolean acceptsIncomplete) {
		try {
			// 1. 参数校验
			checkUnBinding(instanceId, bindingId, serviceId, planId, acceptsIncomplete);

			// 2. 更新数据库(ServiceBinding表)，删除记录
			daoService.deleteServiceInstanceBinding(bindingId);
		} catch (BrokerException e) {
			return new ResponseEntity(e.getMessage(), e.getCode());
		}

		// 3. 返回
		return ResponseEntity.status(HttpStatus.OK).body(new JSONObject());
	}

	@GetMapping
	public ResponseEntity<?> getBinding(@PathVariable("binding_id") String bindingId) {
		// 1. 查询数据库，获取绑定信息
		ServiceInstanceBinding serviceInstanceBinding = daoService.getServiceInstanceBinding(bindingId);
		if (null == serviceInstanceBinding) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		logger.info("----获取绑定信息---->\t" + serviceInstanceBinding.getCredentials());

		// 2. 返回
		return ResponseEntity.status(HttpStatus.OK).body(serviceInstanceBinding.getCredentialsObject());
	}

	/**
	 * @param instanceId
	 * @return
	 */
	private JSONObject buildBindingInfoForMysql(String instanceId) throws BrokerException {
		// 1. 获取instance_id对应的mysqlCluster对象
		ServiceInstance serviceInstance = daoService.getServiceInstance(instanceId);
		if (null == serviceInstance) {
			return null;
		}
		logger.info("---构建绑定信息-----:\t" + JSONObject.toJSONString(serviceInstance));

		MysqlCluster mysqlCluster = null;

		try {
			mysqlCluster = k8sClientForMysql.inNamespace(serviceInstance.getTenantId())
					.withName(serviceInstance.getServiceName()).get();
		} catch (Exception e) {
			logger.error("===获取mysqlCluster---error:\t" + e.getMessage());
		}

		logger.info("---构建绑定信息----mysqlCluster-:\t" + JSONObject.toJSONString(mysqlCluster));
		// 2. 转换成json格式
		JSONObject credentials = buildCredentials(mysqlCluster);

		// 3. 返回
		return credentials;
	}

	private JSONObject buildBindingInfoForRedis(String instanceId) throws BrokerException {
		// 1. 获取instance_id对应的redisCluster对象
		ServiceInstance serviceInstance = daoService.getServiceInstance(instanceId);
		if (null == serviceInstance) {
			return null;
		}

		logger.info("---构建绑定信息-----:\t" + JSONObject.toJSONString(serviceInstance));

		RedisCluster redisCluster = null;

		try {
			redisCluster = k8sClientForRedis.inNamespace(serviceInstance.getTenantId())
					.withName(serviceInstance.getServiceName()).get();
		} catch (Exception e) {
			logger.error("===redisCluster---error:\t" + e.getMessage());
		}

		logger.info("---构建绑定信息----mysqlCluster-:\t" + JSONObject.toJSONString(redisCluster));
		// 2. 转换成json格式
		JSONObject credentials = buildCredentials(redisCluster);

		// 3. 返回
		return credentials;
	}

	private JSONObject buildCredentials(MysqlCluster mysqlCluster) {

		JSONObject bindingObject = new JSONObject();
		JSONObject credentialsObject = new JSONObject();

		MysqlConfig config = mysqlCluster.getSpec().getConfig();

		Map<String, MysqlServer> serverNodes = mysqlCluster.getStatus().getServerNodes();
		for (Map.Entry<String, MysqlServer> entry : serverNodes.entrySet()) {
			JSONObject nodeObject = new JSONObject();
			BindingMysqlInsideK8S insideK8s = new BindingMysqlInsideK8S();
			insideK8s.setUsername("root");
			insideK8s.setPassword(config.getPassword());
			insideK8s.setSvcname(entry.getValue().getSvcname());
			insideK8s.setPort("3306");
			insideK8s.setRole(entry.getValue().getRole());

			BindingMysqlOutsideK8S outsideK8s = new BindingMysqlOutsideK8S();
			outsideK8s.setUsername("root");
			outsideK8s.setPassword(config.getPassword());
			outsideK8s.setHost(entry.getValue().getNodeIP());
			outsideK8s.setPort(String.valueOf(entry.getValue().getNodeport()));
			outsideK8s.setRole(entry.getValue().getRole());

			nodeObject.put("inside_k8s", insideK8s);
			nodeObject.put("outside_k8s", outsideK8s);
			credentialsObject.put(entry.getKey(), nodeObject);
		}

		bindingObject.put("credentials", credentialsObject);
		bindingObject.put("syslog_drain_url", null);
		bindingObject.put("route_service_url", null);
		bindingObject.put("volume_mounts", null);

		return bindingObject;
	}

	private JSONObject buildCredentials(RedisCluster redisCluster) {

		JSONObject bindingObject = new JSONObject();
		JSONObject credentialsObject = new JSONObject();

		String password = redisCluster.getSpec().getPassword();

		Map<String, ServiceStatus> services = redisCluster.getStatus().getServices();
		Map<String, String> portAll = new HashMap<>(16);
		for (Map.Entry<String, ServiceStatus> entry : services.entrySet()) {
			portAll.put(entry.getValue().getRole(), String.valueOf(entry.getValue().getNodePort()));
		}

		Map<String, BindingNode> serverNodes = redisCluster.getStatus().getBindings();
		for (Map.Entry<String, BindingNode> entry : serverNodes.entrySet()) {
			JSONObject nodeObject = new JSONObject();
			BindingRedisOutK8s bindingRedisOutK8s = new BindingRedisOutK8s();

			bindingRedisOutK8s.setHost(entry.getValue().getBindIp());
			bindingRedisOutK8s.setPassword(password);
			String role = entry.getValue().getRole();
			for (Map.Entry<String, String> port : portAll.entrySet()) {
				if (port.getKey().equalsIgnoreCase(role)) {
					bindingRedisOutK8s.setPort(port.getValue());
					break;
				}
			}
			bindingRedisOutK8s.setRole(role);

			nodeObject.put("inside_k8s", bindingRedisOutK8s);
			credentialsObject.put(entry.getKey(), nodeObject);
		}

		bindingObject.put("credentials", credentialsObject);
		bindingObject.put("syslog_drain_url", null);
		bindingObject.put("route_service_url", null);
		bindingObject.put("volume_mounts", null);

		return bindingObject;
	}

	private void checkBinding(String instanceId, String bindingId, Boolean acceptsIncomplete,
							  JSONObject requestBody) throws BrokerException {
		// 1. 校验instance_id格式 是否符合要求
		if (StringUtils.isBlank(instanceId)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 2. 校验instance_id 是否存在?
		Optional<ServiceInstance> instanceIsExists = serviceInstanceRepo.findById(instanceId);
		if (!instanceIsExists.isPresent()) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 3. 校验service_id
		String serviceId = requestBody.getString("service_id");
		if (null == serviceId) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		if (!Global.SERVICE_ID.contains(serviceId)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 4. 校验plan_id
		String planId = requestBody.getString("plan_id");
		if (null == planId) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}
		if (!Global.PLAN_ID.contains(planId)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 5. 校验binding格式 是否符合要求
		if (StringUtils.isBlank(bindingId)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 5.1 校验bindingID 是否已经binding过了
		ServiceInstanceBinding serviceInstanceBinding = daoService.getServiceInstanceBinding(bindingId);
		if (null != serviceInstanceBinding) {
			throw new BrokerException(HttpStatus.CONFLICT, HttpStatus.CONFLICT.getReasonPhrase());
		}

		// 3. 仅支持同步，binding绑定；true表示: 支持异步请求
		if (null != acceptsIncomplete && true == acceptsIncomplete.booleanValue()) {
			throw new BrokerException(HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase());
		}
	}

	private void checkUnBinding(String instanceId, String bindingId, String serviceId, String planId, Boolean acceptsIncomplete)
			throws BrokerException {
		// 1. 校验instance_id格式 是否符合要求
		if (StringUtils.isBlank(instanceId)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 2. 校验instance_id 是否存在?
		Optional<ServiceInstance> instanceIsExists = serviceInstanceRepo.findById(instanceId);
		if (!instanceIsExists.isPresent()) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 3. 校验service_id
		if (!Global.SERVICE_ID.contains(serviceId)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 4. 校验plan_id
		if (!Global.PLAN_ID.contains(planId)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 5. 校验binding格式 是否符合要求
		if (StringUtils.isBlank(bindingId)) {
			throw new BrokerException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase());
		}

		// 5.1 校验bindingID 是否bindingId 是否存在？
		ServiceInstanceBinding serviceInstanceBinding = daoService.getServiceInstanceBinding(bindingId);
		if (null == serviceInstanceBinding) {
			throw new BrokerException(HttpStatus.GONE, HttpStatus.GONE.getReasonPhrase());
		}

		// 6. 仅支持同步，binding绑定；true表示: 支持异步请求
		if (null != acceptsIncomplete && true == acceptsIncomplete.booleanValue()) {
			throw new BrokerException(HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase());
		}
	}


}
