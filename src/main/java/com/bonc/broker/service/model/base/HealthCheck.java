package com.bonc.broker.service.model.base;

public class HealthCheck {

	private int livenessDelayTimeout;
	private int readinessDelayTimeout;
	private int livenessFailureThreshold;
	private int readinessFailureThreshold;

	public int getLivenessDelayTimeout() {
		return livenessDelayTimeout;
	}

	public void setLivenessDelayTimeout(int livenessDelayTimeout) {
		this.livenessDelayTimeout = livenessDelayTimeout;
	}

	public int getReadinessDelayTimeout() {
		return readinessDelayTimeout;
	}

	public void setReadinessDelayTimeout(int readinessDelayTimeout) {
		this.readinessDelayTimeout = readinessDelayTimeout;
	}

	public int getLivenessFailureThreshold() {
		return livenessFailureThreshold;
	}

	public void setLivenessFailureThreshold(int livenessFailureThreshold) {
		this.livenessFailureThreshold = livenessFailureThreshold;
	}

	public int getReadinessFailureThreshold() {
		return readinessFailureThreshold;
	}

	public void setReadinessFailureThreshold(int readinessFailureThreshold) {
		this.readinessFailureThreshold = readinessFailureThreshold;
	}

}
