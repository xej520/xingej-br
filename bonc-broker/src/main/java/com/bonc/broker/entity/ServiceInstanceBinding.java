package com.bonc.broker.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author xingej serviceinstance binding表
 */
@Entity
@Table(name = "`SERVICE_INSTANCE_BINDING`")
public class ServiceInstanceBinding implements Serializable {

    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    @Column(name = "`ID`") //binding_id
    private String bindingId;

    @Column(name = "`INSTANCE_ID`")  // instance_id
    private String instanceId;

    @Column(name = "`CREATED_TIME`")  // 创建时间
    private Date createdTime;

    @Column(name = "`CREDENTIALS`")  // credentials
    private String credentials;

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }
}
