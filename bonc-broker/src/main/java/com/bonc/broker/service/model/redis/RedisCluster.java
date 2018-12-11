/**
 * 
 */
package com.bonc.broker.service.model.redis;

import com.bonc.broker.service.model.base.Status;
import io.fabric8.kubernetes.api.model.ObjectMeta;

/**
 * @author Yx Liu
 * @date 2018年5月30日
 *
 */
public class RedisCluster {

    private String kind;
    private String apiVersion;
    private ObjectMeta metadata;
    private RedisSpec spec;
    private Status status;

    /**
     * @return the kind
     */
    public String getKind() {
        return kind;
    }

    /**
     * @param kind
     *            the kind to set
     */
    public void setKind(String kind) {
        this.kind = kind;
    }

    /**
     * @return the apiVersion
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * @param apiVersion
     *            the apiVersion to set
     */
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    /**
     * @return the metadata
     */
    public ObjectMeta getMetadata() {
        return metadata;
    }

    /**
     * @param metadata
     *            the metadata to set
     */
    public void setMetadata(ObjectMeta metadata) {
        this.metadata = metadata;
    }

    /**
     * @return the spec
     */
    public RedisSpec getSpec() {
        return spec;
    }

    /**
     * @param spec
     *            the spec to set
     */
    public void setSpec(RedisSpec spec) {
        this.spec = spec;
    }

    /**
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

}
