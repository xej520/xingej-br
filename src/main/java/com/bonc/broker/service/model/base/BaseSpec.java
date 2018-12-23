package com.bonc.broker.service.model.base;

public class BaseSpec {

	private Resources resources;
	private int replicas;
	private String volume;
	private String volumeMount;
	private String capacity;

	public Resources getResources() {
		return resources;
	}

	public void setResources(Resources resources) {
		this.resources = resources;
	}

	public int getReplicas() {
		return replicas;
	}

	public void setReplicas(int replicas) {
		this.replicas = replicas;
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

	public String getCapacity() {
		return capacity;
	}

	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}

}
