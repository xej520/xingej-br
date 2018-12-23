package com.bonc.broker.repository;

import com.bonc.broker.entity.UnitVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author xingej
 */
@Repository
public interface UnitVersionRepo extends JpaRepository<UnitVersion, String> {
    /**
     * 根据type和verson来获取UnitVersion对象
     * @param type
     * @param version
     * @return
     */
    UnitVersion findByAppTypeAndVersion(String type, String version);

    /**
     * 根据type、extended和verson来获取UnitVersion对象
     * @param type
     * @param extended
     * @param version
     * @return
     */
    UnitVersion findByAppTypeAndExtendedFieldAndVersion(String type, String extended, String version);
}
