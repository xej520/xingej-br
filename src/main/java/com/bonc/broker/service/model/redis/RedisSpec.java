package com.bonc.broker.service.model.redis;

import com.bonc.broker.service.model.base.BaseSpec;
import io.fabric8.kubernetes.api.model.Affinity;

import java.util.Map;

public class RedisSpec extends BaseSpec {

	private String version;
	private String type;
	private boolean stopped;
	private boolean paused;
	private String password;
	private String image;
	private String exporterImage;
	private String logDir;

	private Map<String, String> configMap;
	private Map<String, String> nodeSelector;

	private Sentinel sentinel;
	private String storageClass;

	private Affinity affinity;

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isStopped() {
		return stopped;
	}

	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}

	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getExporterImage() {
		return exporterImage;
	}

	public void setExporterImage(String exporterImage) {
		this.exporterImage = exporterImage;
	}

	public String getLogDir() {
		return logDir;
	}

	public void setLogDir(String logDir) {
		this.logDir = logDir;
	}

	public Map<String, String> getConfigMap() {
		return configMap;
	}

	public void setConfigMap(Map<String, String> configMap) {
		this.configMap = configMap;
	}

	public Map<String, String> getNodeSelector() {
		return nodeSelector;
	}

	public void setNodeSelector(Map<String, String> nodeSelector) {
		this.nodeSelector = nodeSelector;
	}

	public Sentinel getSentinel() {
		return sentinel;
	}

	public void setSentinel(Sentinel sentinel) {
		this.sentinel = sentinel;
	}

	public String getStorageClass() {
		return storageClass;
	}

	public void setStorageClass(String storageClass) {
		this.storageClass = storageClass;
	}

	public Affinity getAffinity() {
		return affinity;
	}

	public void setAffinity(Affinity affinity) {
		this.affinity = affinity;
	}

}
