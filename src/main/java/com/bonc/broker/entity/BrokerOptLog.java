package com.bonc.broker.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author xingej mysql broker的操作记录表
 */
@Entity
@Table(name = "`BROKER_OPT_LOG`")
public class BrokerOptLog implements Serializable {
    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    private String id;

    @Column(name = "`SERVICE_ID`")  // service_id，
    private String serviceId;

    @Column(name = "`INSTANCE_ID`") //instance ID
    private String instanceId;

    @Column(name = "`OPT_TYPE`")  // 操作类型
    private String optType;

    @Column(name = "`STATE`")  // 操作状态
    private String state;

    @Column(name = "`CREATED_TIME`")  // 创建时间
    private Date createdTime;

    @Column(name = "`UPDATED_TIME`")  // 更新时间
    private Date updatedTime;

    @Column(name = "`ERROR_MSG`", length = 3000)  // 操作失败原因
    private String errorMsg;

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getOptType() {
        return optType;
    }

    public void setOptType(String optType) {
        this.optType = optType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }
}
