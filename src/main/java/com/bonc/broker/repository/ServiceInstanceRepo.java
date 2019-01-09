package com.bonc.broker.repository;

import com.bonc.broker.entity.ServiceInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * @author xingej
 */
@Repository
public interface ServiceInstanceRepo extends JpaRepository <ServiceInstance, String>{
    ServiceInstance findByServiceName(String serviceName);

    /**
     * 同一个命名空间下，同一个catalog类型里的serviceName是唯一的
     * @param tenantId 命名空间
     * @param catalog   mysql, redis,
     * @param serviceName
     * @return
     */
    ServiceInstance findByTenantIdAndCatalogAndServiceName(String tenantId, String catalog, String serviceName);
}

