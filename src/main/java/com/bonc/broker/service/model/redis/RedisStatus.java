package com.bonc.broker.service.model.redis;

import com.bonc.broker.service.model.base.StatusCondition;

import java.util.List;
import java.util.Map;

public class RedisStatus {
	private String phase;
	private String reason;
	private boolean needRestart;

	private Map<String, ServiceStatus> services;
	private Map<String, BindingNode> bindings;

	private List<StatusCondition> conditions;

	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public boolean isNeedRestart() {
		return needRestart;
	}

	public void setNeedRestart(boolean needRestart) {
		this.needRestart = needRestart;
	}

	public Map<String, ServiceStatus> getServices() {
		return services;
	}

	public void setServices(Map<String, ServiceStatus> services) {
		this.services = services;
	}

	public Map<String, BindingNode> getBindings() {
		return bindings;
	}

	public void setBindings(Map<String, BindingNode> bindings) {
		this.bindings = bindings;
	}

	public List<StatusCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<StatusCondition> conditions) {
		this.conditions = conditions;
	}

}
