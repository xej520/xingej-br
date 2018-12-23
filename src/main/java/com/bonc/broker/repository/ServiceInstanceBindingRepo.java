package com.bonc.broker.repository;

import com.bonc.broker.entity.ServiceInstanceBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * @author xingej
 */

@Repository
public interface ServiceInstanceBindingRepo extends JpaRepository<ServiceInstanceBinding, String> {
    /**
     *  根据instanceID，来查询binding信息
     * @param instanceId
     * @return
     */
    public ServiceInstanceBinding findByInstanceId(String instanceId);

}
