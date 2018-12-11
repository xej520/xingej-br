package com.bonc.broker.service.model.base;

import io.fabric8.kubernetes.api.model.ListMeta;

/**
 * @author Yx Liu
 * @date 2018年6月7日
 *
 */
public abstract class BaseClusterList {
    private String apiVersion;
    private String kind;
    private ListMeta metadata;

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
     * @return the metadata
     */
    public ListMeta getMetadata() {
        return metadata;
    }

    /**
     * @param metadata
     *            the metadata to set
     */
    public void setMetadata(ListMeta metadata) {
        this.metadata = metadata;
    }

}
