package com.bonc.broker.service.model.crd.lvm;

import com.bonc.broker.service.model.lvm.Lvm;
import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneableLvm extends CustomResourceDoneable<Lvm> {

    public DoneableLvm(Lvm resource, Function<Lvm, Lvm> function) {
        super(resource, function);
    }
}
