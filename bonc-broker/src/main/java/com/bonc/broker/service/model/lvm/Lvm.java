/**
 * 
 */
package com.bonc.broker.service.model.lvm;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;

/**
 * @author LYX
 *
 */
public class Lvm extends CustomResource {

    private String kind;
    private String apiVersion;

    private ObjectMeta metadata;

    private LVMSpec spec;

    /**
     * 
     */
    public Lvm() {
        super();
        this.kind = "LVM";
        this.apiVersion = "bonc.com/v1";
    }

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
    public LVMSpec getSpec() {
        return spec;
    }

    /**
     * @param spec
     *            the spec to set
     */
    public void setSpec(LVMSpec spec) {
        this.spec = spec;
    }

}
