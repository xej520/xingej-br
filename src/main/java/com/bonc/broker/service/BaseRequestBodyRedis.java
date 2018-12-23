package com.bonc.broker.service;

import com.alibaba.fastjson.JSONObject;

public class BaseRequestBodyRedis {
    private String version;
    private String type;
    private String serviceName;
    private String password;
    private String cpu;
    private String memory;
    private String capacity;
    private int replicas;
    private String sentinelCpu;
    private String sentinelMemory;
    private String sentinelReplicas;
    private JSONObject configuration;
    private String performance;
    private String tenantId;
    private String projectId;
    private String userId;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public String getSentinelCpu() {
        return sentinelCpu;
    }

    public void setSentinelCpu(String sentinelCpu) {
        this.sentinelCpu = sentinelCpu;
    }

    public String getSentinelMemory() {
        return sentinelMemory;
    }

    public void setSentinelMemory(String sentinelMemory) {
        this.sentinelMemory = sentinelMemory;
    }

    public String getSentinelReplicas() {
        return sentinelReplicas;
    }

    public void setSentinelReplicas(String sentinelReplicas) {
        this.sentinelReplicas = sentinelReplicas;
    }

    public JSONObject getConfiguration() {
        return configuration;
    }

    public void setConfiguration(JSONObject configuration) {
        this.configuration = configuration;
    }

    public String getPerformance() {
        return performance;
    }

    public void setPerformance(String performance) {
        this.performance = performance;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
