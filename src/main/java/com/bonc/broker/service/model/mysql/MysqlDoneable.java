package com.bonc.broker.service.model.mysql;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class MysqlDoneable extends CustomResourceDoneable<MysqlCluster>{

	public MysqlDoneable(MysqlCluster resource, Function<MysqlCluster, MysqlCluster> function) {
		super(resource, function);
	}

}
