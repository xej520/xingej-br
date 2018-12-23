package com.bonc.broker.service.model.crd.mysql;

import com.bonc.broker.service.model.mysql.MysqlCluster;
import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneableMysql extends CustomResourceDoneable<MysqlCluster> {
    public DoneableMysql(MysqlCluster resource, Function<MysqlCluster, MysqlCluster> function) {
        super(resource, function);
    }
}
