package com.bonc.broker.service.model.mysql;

import com.bonc.broker.service.model.base.StatusCondition;

import java.util.List;
import java.util.Map;

public class MysqlStatus {

	private String phase;
	private boolean stopped;
	private boolean resourceupdateneedrestart;
	private boolean parameterupdateneedrestart;
	private boolean configmapneedapply;
	private Map<String, String> currentmycnf;
	private Map<String, MysqlServer> serverNodes;
	private List<StatusCondition> conditions;
	private String reason;
	private int mmreplwait;

	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public boolean isStopped() {
		return stopped;
	}

	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}

	public boolean isResourceupdateneedrestart() {
		return resourceupdateneedrestart;
	}

	public void setResourceupdateneedrestart(boolean resourceupdateneedrestart) {
		this.resourceupdateneedrestart = resourceupdateneedrestart;
	}

	public boolean isParameterupdateneedrestart() {
		return parameterupdateneedrestart;
	}

	public void setParameterupdateneedrestart(boolean parameterupdateneedrestart) {
		this.parameterupdateneedrestart = parameterupdateneedrestart;
	}

	public boolean isConfigmapneedapply() {
		return configmapneedapply;
	}

	public void setConfigmapneedapply(boolean configmapneedapply) {
		this.configmapneedapply = configmapneedapply;
	}

	public Map<String, String> getCurrentmycnf() {
		return currentmycnf;
	}

	public void setCurrentmycnf(Map<String, String> currentmycnf) {
		this.currentmycnf = currentmycnf;
	}

	public Map<String, MysqlServer> getServerNodes() {
		return serverNodes;
	}

	public void setServerNodes(Map<String, MysqlServer> serverNodes) {
		this.serverNodes = serverNodes;
	}

	public List<StatusCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<StatusCondition> conditions) {
		this.conditions = conditions;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public int getMmreplwait() {
		return mmreplwait;
	}

	public void setMmreplwait(int mmreplwait) {
		this.mmreplwait = mmreplwait;
	}

}
