/**
 * 
 */
package com.bonc.broker.service.model.mysql;


import com.bonc.broker.service.model.base.Spec;
import com.bonc.broker.service.model.base.Status;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;

/**
 * @author LYX
 *
 */
public class MysqlCluster extends CustomResource {

    private String kind;
    private String apiVersion;
    private ObjectMeta metadata;
    private Spec spec;
    private Status status;

    /**
     * @return the metadata
     */
    @Override
    public ObjectMeta getMetadata() {
        return metadata;
    }

    /**
     * @param metadata
     *            the metadata to set
     */
    @Override
    public void setMetadata(ObjectMeta metadata) {
        this.metadata = metadata;
    }

    /**
     * @return the apiVersion
     */
    @Override
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * @param apiVersion
     *            the apiVersion to set
     */
    @Override
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    /**
     * @return the kind
     */
    @Override
    public String getKind() {
        return kind;
    }

    /**
     * @param kind
     *            the kind to set
     */
    @Override
    public void setKind(String kind) {
        this.kind = kind;
    }

    /**
     * @return the spec
     */
    public Spec getSpec() {
        return spec;
    }

    /**
     * @param spec
     *            the spec to set
     */
    public void setSpec(Spec spec) {
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
