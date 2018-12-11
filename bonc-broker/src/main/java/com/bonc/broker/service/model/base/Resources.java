/**
 * 
 */
package com.bonc.broker.service.model.base;

/**
 * @author LYX
 *
 */
public class Resources {
    private MemoryCPU requests;
    private MemoryCPU limits;

    /**
     * @return the requests
     */
    public MemoryCPU getRequests() {
        return requests;
    }

    /**
     * @param requests
     *            the requests to set
     */
    public void setRequests(MemoryCPU requests) {
        this.requests = requests;
    }

    /**
     * @return the limits
     */
    public MemoryCPU getLimits() {
        return limits;
    }

    /**
     * @param limits
     *            the limits to set
     */
    public void setLimits(MemoryCPU limits) {
        this.limits = limits;
    }

}
