package com.bonc.broker.repository;

import com.bonc.broker.entity.BrokerOptLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrokerLogRepo extends JpaRepository <BrokerOptLog, String>{
}
