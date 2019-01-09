package com.bonc.broker.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bonc.broker.common.Global;
import com.bonc.broker.common.GlobalHelp;
import com.bonc.broker.entity.BrokerOptLog;
import com.bonc.broker.entity.ServiceInstance;
import com.bonc.broker.entity.ServiceInstanceBinding;
import com.bonc.broker.entity.UnitVersion;
import com.bonc.broker.exception.BrokerException;
import com.bonc.broker.repository.BrokerLogRepo;
import com.bonc.broker.repository.ServiceInstanceBindingRepo;
import com.bonc.broker.repository.ServiceInstanceRepo;
import com.bonc.broker.repository.UnitVersionRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

/**
 * @author xingej
 */

@Component
public class DaoService {

	private static Logger logger = LoggerFactory.getLogger(DaoService.class);

	@Autowired
	private BrokerLogRepo brokerLogRepo;
	@Autowired
	private ServiceInstanceRepo serviceInstanceRepo;
	@Autowired
	private ServiceInstanceBindingRepo serviceInstanceBindingRepo;
	@Autowired
	private UnitVersionRepo unitVersionRepo;

	/**
	 * 操作记录表:创建，更新，删除
	 *
	 * @param instanceId
	 * @param optType
	 * @return
	 */
	public String saveBrokerLog(String planId, String instanceId, String optType) throws BrokerException {
		BrokerOptLog brokerOptLog = new BrokerOptLog();

		brokerOptLog.setInstanceId(instanceId);
		brokerOptLog.setPlanId(planId);
		brokerOptLog.setOptType(optType);

		brokerOptLog.setCreatedTime(new Date());
		brokerOptLog.setState(Global.STATE_IN);

		try {
			brokerOptLog = brokerLogRepo.save(brokerOptLog);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new BrokerException(e.getMessage());
		}

		return brokerOptLog.getId();
	}

	/**
	 * @param operation
	 * @return
	 */
	public BrokerOptLog getBrokerLogRepo(String operation) {
		Optional<BrokerOptLog> byId = brokerLogRepo.findById(operation);

		if (null != byId && byId.isPresent()) {
			return byId.get();
		}

		return null;
	}

	public void updateBrokerLog(String id, String state) throws BrokerException {

		try {
			Optional<BrokerOptLog> brokerOptLog = brokerLogRepo.findById(id);

			if (brokerOptLog.isPresent()) {
				BrokerOptLog newBrokerLog = brokerOptLog.get();

				newBrokerLog.setState(state);
				newBrokerLog.setUpdatedTime(new Date());

				brokerLogRepo.save(newBrokerLog);
				logger.info("-----更新---操作日志表---成功了");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new BrokerException(e.getMessage());
		}
	}

	public void saveServiceInstance(String instanceId, String parameters, String serviceId, String planId) throws BrokerException {
		JSONObject jsonObject = JSON.parseObject(parameters);

		String configuration = jsonObject.getString("configuration");
		JSONObject confObject = JSONObject.parseObject(configuration);

		ServiceInstance serviceInstance = new ServiceInstance();

		serviceInstance.setInstanceId(instanceId);
		serviceInstance.setPlanId(planId);
		serviceInstance.setCatalog(GlobalHelp.getCatalogType(planId));

		serviceInstance.setServiceId(serviceId);
		serviceInstance.setServiceName(confObject.getString("serviceName"));
		serviceInstance.setDashboardUrl(null);

		serviceInstance.setProjectId(jsonObject.getString("project_id"));
		serviceInstance.setTenantId(jsonObject.getString("tenant_id"));
		serviceInstance.setUserId(jsonObject.getString("user_id"));

		serviceInstance.setParametersObject(jsonObject);

		try {
			serviceInstanceRepo.save(serviceInstance);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new BrokerException(e.getMessage());
		}
	}

	public void updateServiceInstance(String instanceId, JSONObject parameters) throws BrokerException {
		try {
			Optional<ServiceInstance> byId = serviceInstanceRepo.findById(instanceId);

			ServiceInstance serviceInstance = byId.get();
			serviceInstance.setParametersObject(parameters);

			serviceInstanceRepo.save(serviceInstance);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new BrokerException(e.getMessage());
		}
	}

	public void deleteServiceInstance(String instanceId) throws BrokerException {
		try {
			serviceInstanceRepo.deleteById(instanceId);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new BrokerException(e.getMessage());
		}
	}

	public void deleteServiceInstanceBinding(String bindingId) throws BrokerException {
		try {
			serviceInstanceBindingRepo.deleteById(bindingId);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new BrokerException(e.getMessage());
		}
	}

	/**
	 * 根据instance_id来获得对应的实例对象ServiceInstance
	 *
	 * @param instanceId
	 * @return
	 */
	public ServiceInstance getServiceInstance(String instanceId) {
		Optional<ServiceInstance> byId = serviceInstanceRepo.findById(instanceId);

		if (null != byId && byId.isPresent()) {
			return byId.get();
		}

		return null;
	}

	/**
	 * 根据binding Id 来获取binding对象
	 *
	 * @param bindingId
	 * @return
	 */
	public ServiceInstanceBinding getServiceInstanceBinding(String bindingId) {
		Optional<ServiceInstanceBinding> byId = serviceInstanceBindingRepo.findById(bindingId);

		if (null != byId && byId.isPresent()) {
			return byId.get();
		}

		return null;
	}

	/**
	 * binding表
	 *
	 * @param bindingId
	 * @param instanceId
	 * @param credentials
	 * @return
	 */
	public ServiceInstanceBinding saveServiceInstanceBinding(String bindingId, String instanceId, JSONObject credentials) throws BrokerException {
		ServiceInstanceBinding serviceInstanceBinding = new ServiceInstanceBinding();

		serviceInstanceBinding.setBindingId(bindingId);
		serviceInstanceBinding.setCredentialsObject(credentials);
		serviceInstanceBinding.setInstanceId(instanceId);

		serviceInstanceBinding.setCreatedTime(new Date());
		ServiceInstanceBinding newServiceInstanceBinding;
		try {
			newServiceInstanceBinding = serviceInstanceBindingRepo.save(serviceInstanceBinding);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new BrokerException(e.getMessage());
		}

		return newServiceInstanceBinding;
	}

	public String getRepoPath(String appType, String extended, String version) {
		UnitVersion unitVersion = unitVersionRepo.findByAppTypeAndExtendedFieldAndVersion(appType, extended, version);
		if (null != unitVersion) {
			return unitVersion.getImageUrl();
		}

		return null;
	}

	public Float getOldCapacity(String instanceId) {
		ServiceInstance serviceInstance = getServiceInstance(instanceId);

		if (null == serviceInstance) {
			return null;
		}

		String parameters = serviceInstance.getParameters();

		JSONObject jsonObject = JSONObject.parseObject(parameters);
		String configuration = jsonObject.getString("configuration");

		JSONObject configurationObject = JSONObject.parseObject(configuration);

		String capacity = configurationObject.getString("capacity");

		return Float.valueOf(capacity);
	}

	/**
	 * @param tenantId    租户ID，对应命名空间
	 * @param catalog     有效值:mysql, redis
	 * @param serviceName
	 * @return
	 */
	public ServiceInstance getServiceInstanceByTenantIdAndCatalogAndServiceName(String tenantId, String catalog, String serviceName) {
		ServiceInstance serviceInstance  = serviceInstanceRepo.findByTenantIdAndCatalogAndServiceName(tenantId, catalog, serviceName);

		if (null == serviceInstance) {
			logger.error("---query service instance table failed by tenantId:\t" + tenantId + "\tcatalog:\t" + catalog + "\tserviceName:\t" + serviceName);
			return null;
		}

		logger.info("---query service instance table by tenantId:\t" + tenantId + "\tcatalog:\t" + catalog + "\tserviceName:\t" + serviceName);
		return serviceInstance;
	}
}
