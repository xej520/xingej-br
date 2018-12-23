package com.bonc.broker.service.model.lvm;

import io.fabric8.kubernetes.client.CustomResource;

public class Lvm extends CustomResource{

    private LVMSpec spec;

    public Lvm() {
        super();
        super.setApiVersion("bonc.com/v1");
        super.setKind("LVM");
    }

    public LVMSpec getSpec() {
        return spec;
    }

    public void setSpec(LVMSpec spec) {
        this.spec = spec;
    }

}
