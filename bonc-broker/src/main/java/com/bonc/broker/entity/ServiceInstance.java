package com.bonc.broker.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author xingej serviceinstance表
 */
@Entity
@Table(name = "`SERVICE_INSTANCE`")
public class ServiceInstance implements Serializable {

    @Id
    @GenericGenerator(name = "uuidGenerator", strategy = "uuid")
    @GeneratedValue(generator = "uuidGenerator")
    @Column(name = "`ID`") //instance ID
    private String instanceId;

    @Column(name = "`SERVICE_ID`")  // service_id
    private String serviceId;

    @Column(name = "`catalog`")  // catalog: mysql redis
    private String catalog;

    @Column(name = "`PLAN_ID`")  // plan_id
    private String planId;

    @Column(name = "`PARAMETERS`")  // parameters，json
    private String parameters;

    @Column(name = "`DASHBOARD_URL`")  // dashboard_url
    private String dashboardUrl;

    @Column(name = "`TENANT_ID`")  // tenant_id 租户ID
    private String tenantId;

    @Column(name = "`PROJECT_ID`")  // project_id
    private String projectId;

    @Column(name = "`SERVICE_NAME`")  // service_name
    private String serviceName;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getDashboardUrl() {
        return dashboardUrl;
    }

    public void setDashboardUrl(String dashboardUrl) {
        this.dashboardUrl = dashboardUrl;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
