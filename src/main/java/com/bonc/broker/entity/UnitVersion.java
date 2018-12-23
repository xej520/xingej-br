package com.bonc.broker.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author xingej mysql UnitVersion 版本跟镜像映射表
 */

@Entity
@Table(name = "`UNIT_VERSION`")
public class UnitVersion implements Serializable {
    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    @Column(name = "`app_type`") //服务类型mysql, redis
    private String appType;

    @Column(name = "`version`")  // 版本号
    private String version;

    @Column(name = "`image_url`")  // 镜像地址
    private String imageUrl;

    @Column(name = "`extended_field`")  //
    private String extendedField;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getExtendedField() {
        return extendedField;
    }

    public void setExtendedField(String extendedField) {
        this.extendedField = extendedField;
    }
}
