package com.bonc.broker.service.model.base;

public class Resources {
    private MemoryCPU requests;
    private MemoryCPU limits;
 
    public MemoryCPU getRequests() {
        return requests;
    }
    
    public void setRequests(MemoryCPU requests) {
        this.requests = requests;
    }

    public MemoryCPU getLimits() {
        return limits;
    }

    public void setLimits(MemoryCPU limits) {
        this.limits = limits;
    }

}
