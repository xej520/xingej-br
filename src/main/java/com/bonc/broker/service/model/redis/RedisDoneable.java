package com.bonc.broker.service.model.redis;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class RedisDoneable extends CustomResourceDoneable<RedisCluster>{

	public RedisDoneable(RedisCluster resource, Function<RedisCluster, RedisCluster> function) {
		super(resource, function);
	}

}
