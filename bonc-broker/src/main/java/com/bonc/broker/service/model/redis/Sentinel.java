/**
 * 
 */
package com.bonc.broker.service.model.redis;

import com.bonc.broker.service.model.base.Resources;

/**
 * @author LYX
 *
 */
public class Sentinel {

    private int quorum;
    private int replicas;
    private Resources resources;

    /**
     * @return the quorum
     */
    public int getQuorum() {
        return quorum;
    }

    /**
     * @param quorum
     *            the quorum to set
     */
    public void setQuorum(int quorum) {
        this.quorum = quorum;
    }

    /**
     * @return the replicas
     */
    public int getReplicas() {
        return replicas;
    }

    /**
     * @param replicas
     *            the replicas to set
     */
    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    /**
     * @return the resources
     */
    public Resources getResources() {
        return resources;
    }

    /**
     * @param resources
     *            the resources to set
     */
    public void setResources(Resources resources) {
        this.resources = resources;
    }

}
