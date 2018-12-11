package com.bonc.broker.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bonc.broker.common.Global;
import com.bonc.broker.common.GlobalHelp;
import com.bonc.broker.entity.BrokerOptLog;
import com.bonc.broker.entity.ServiceInstance;
import com.bonc.broker.entity.ServiceInstanceBinding;
import com.bonc.broker.repository.BrokerLogRepo;
import com.bonc.broker.repository.ServiceInstanceBindingRepo;
import com.bonc.broker.repository.ServiceInstanceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Component
public class DAOService {

    @Autowired
    private BrokerLogRepo brokerLogRepo;

    @Autowired
    private ServiceInstanceRepo serviceInstanceRepo;

    @Autowired
    private ServiceInstanceBindingRepo serviceInstanceBindingRepo;


    //操作记录表:创建，更新，删除
    public String saveBrokerLog(String instanceId, String optType) {
        BrokerOptLog brokerOptLog = new BrokerOptLog();

        brokerOptLog.setInstanceId(instanceId);
        brokerOptLog.setOptType(optType);

        brokerOptLog.setCreatedTime(new Date());
        brokerOptLog.setState(Global.STATE_IN);

        brokerOptLog = brokerLogRepo.save(brokerOptLog);

        return brokerOptLog.getId();
    }

    /**
     *
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

    public void updateBrokerLog(String id, String state) {
        Optional<BrokerOptLog> brokerOptLog = brokerLogRepo.findById(id);

        if (brokerOptLog.isPresent()) {
            brokerOptLog.get().setState(state);
            brokerOptLog.get().setUpdatedTime(new Date());
        }

        // 如果不存在时，如何处理？

    }

    public void saveServiceInstance(String instanceId, String parametes, String serviceId, String planId) {

        JSONObject jsonObject = JSON.parseObject(parametes);

        ServiceInstance serviceInstance = new ServiceInstance();

        serviceInstance.setInstanceId(instanceId);

        //有效值:mysql, redis
        serviceInstance.setCatalog(GlobalHelp.getServiceType(planId));

        serviceInstance.setDashboardUrl(null);
        serviceInstance.setPlanId(planId);
        serviceInstance.setServiceId(serviceId);
        serviceInstance.setProjectId(jsonObject.getString("project_id"));
        serviceInstance.setTenantId(jsonObject.getString("tenant_id"));
        serviceInstance.setServiceName(jsonObject.getString("service_name"));

        serviceInstance.setParameters(parametes);

        serviceInstanceRepo.save(serviceInstance);
    }

    public void updateServiceInstance(String instanceId, String parameters) {
        Optional<ServiceInstance> byId = serviceInstanceRepo.findById(instanceId);

        if (null != byId && byId.isPresent() && null != parameters) {
            ServiceInstance serviceInstance = byId.get();
            serviceInstance.setParameters(parameters);

            serviceInstanceRepo.save(serviceInstance);
        }

    }

    public void deleteServiceInstance(String instanceId) {
        Optional<ServiceInstance> byId = serviceInstanceRepo.findById(instanceId);

        if (null != byId && byId.isPresent()) {
            serviceInstanceRepo.deleteById(instanceId);
        }

    }

    public void deleteServiceInstanceBinding(String bindingId) {
        Optional<ServiceInstanceBinding> byId = serviceInstanceBindingRepo.findById(bindingId);
        if (null != byId && byId.isPresent()) {
            serviceInstanceBindingRepo.deleteById(bindingId);
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
     * @param binding_id
     * @return
     */
    public ServiceInstanceBinding getServiceInstanceBinding(String binding_id) {
        Optional<ServiceInstanceBinding> byId = serviceInstanceBindingRepo.findById(binding_id);

        if (null != byId && byId.isPresent()) {
            return byId.get();
        }

        return null;
    }

    /**
     * binding表
     * @param binding_id
     * @param instance_id
     * @param credentials
     * @return
     */
    public ServiceInstanceBinding saveServiceInstanceBinding(String binding_id, String instance_id, String credentials) {
        ServiceInstanceBinding serviceInstanceBinding = new ServiceInstanceBinding();

        serviceInstanceBinding.setBindingId(binding_id);
        serviceInstanceBinding.setCredentials(credentials);
        serviceInstanceBinding.setInstanceId(instance_id);

        serviceInstanceBinding.setCreatedTime(new Date());

        ServiceInstanceBinding save = serviceInstanceBindingRepo.save(serviceInstanceBinding);

        return save;
    }
}
