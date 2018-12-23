package com.bonc.broker.service.model.redis;

import com.bonc.broker.service.model.base.Resources;

import java.util.Map;


public class Sentinel {

	private int quorum;
	private Map<String, String> configMap;

	private int replicas;
	private Resources resources;
	private String capacity;
	private String storageClassName;
	private String volume;
	private String volumeMount;

	public int getQuorum() {
		return quorum;
	}

	public void setQuorum(int quorum) {
		this.quorum = quorum;
	}

	public int getReplicas() {
		return replicas;
	}

	public void setReplicas(int replicas) {
		this.replicas = replicas;
	}

	public Resources getResources() {
		return resources;
	}

	public void setResources(Resources resources) {
		this.resources = resources;
	}

	public Map<String, String> getConfigMap() {
		return configMap;
	}

	public void setConfigMap(Map<String, String> configMap) {
		this.configMap = configMap;
	}

	public String getCapacity() {
		return capacity;
	}

	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}

	public String getStorageClassName() {
		return storageClassName;
	}

	public void setStorageClassName(String storageClassName) {
		this.storageClassName = storageClassName;
	}

	public String getVolume() {
		return volume;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	public String getVolumeMount() {
		return volumeMount;
	}

	public void setVolumeMount(String volumeMount) {
		this.volumeMount = volumeMount;
	}

}
