package com.bonc.broker.service.model.crd.redis;

import com.bonc.broker.service.model.redis.RedisCluster;
import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneableRedis extends CustomResourceDoneable<RedisCluster> {
    public DoneableRedis(RedisCluster resource, Function<RedisCluster, RedisCluster> function) {
        super(resource, function);
    }
}
