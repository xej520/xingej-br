package com.bonc.broker.repository;

import com.bonc.broker.entity.UnitVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author xingej
 */
@Repository
public interface UnitVersionRepo extends JpaRepository<UnitVersionRepo, String> {
    public UnitVersion findByTypeAndVersion(String type, String version);
}
