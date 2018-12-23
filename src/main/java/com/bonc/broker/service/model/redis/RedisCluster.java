package com.bonc.broker.service.model.redis;

import com.bonc.broker.common.Global;
import io.fabric8.kubernetes.client.CustomResource;

public class RedisCluster extends CustomResource {

	private RedisSpec spec;
	private RedisStatus status;

	public RedisCluster() {
		super();
		super.setKind(Global.KIND_REDIS);
		super.setApiVersion(Global.API_VERSION);
	}

	public RedisSpec getSpec() {
		return spec;
	}

	public void setSpec(RedisSpec spec) {
		this.spec = spec;
	}

	public RedisStatus getStatus() {
		return status;
	}

	public void setStatus(RedisStatus status) {
		this.status = status;
	}

}
