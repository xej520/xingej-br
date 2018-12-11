/**
 * 
 */
package com.bonc.broker.service.model.redis;


import com.bonc.broker.service.model.base.Spec;

import java.util.Map;

/**
 * @author LYX
 *
 */
public class RedisSpec extends Spec {

    private Map<String, String> configMap;
    private Sentinel sentinel;
    private boolean stopped;
    private String storageClass;
    private String password;
    private String exporterImage;
    private String logDir;

    /**
     * @return the exporterImage
     */
    public String getExporterImage() {
        return exporterImage;
    }

    /**
     * @param exporterImage
     *            the exporterImage to set
     */
    public void setExporterImage(String exporterImage) {
        this.exporterImage = exporterImage;
    }

    /**
     * @return the logDir
     */
    public String getLogDir() {
        return logDir;
    }

    /**
     * @param logDir
     *            the logDir to set
     */
    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the configMap
     */
    public Map<String, String> getConfigMap() {
        return configMap;
    }

    /**
     * @param configMap
     *            the configMap to set
     */
    public void setConfigMap(Map<String, String> configMap) {
        this.configMap = configMap;
    }

    /**
     * @return the sentinel
     */
    public Sentinel getSentinel() {
        return sentinel;
    }

    /**
     * @param sentinel
     *            the sentinel to set
     */
    public void setSentinel(Sentinel sentinel) {
        this.sentinel = sentinel;
    }

    /**
     * @return the stopped
     */
    public boolean getStopped() {
        return stopped;
    }

    /**
     * @param stopped
     *            the stopped to set
     */
    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    /**
     * @return the storageClass
     */
    public String getStorageClass() {
        return storageClass;
    }

    /**
     * @param storageClass
     *            the storageClass to set
     */
    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

}
