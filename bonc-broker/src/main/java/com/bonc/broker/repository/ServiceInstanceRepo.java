package com.bonc.broker.repository;

import com.bonc.broker.entity.ServiceInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * @author xingej
 */
@Repository
public interface ServiceInstanceRepo extends JpaRepository <ServiceInstance, String>{
}
