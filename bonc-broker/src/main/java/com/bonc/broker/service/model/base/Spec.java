package com.bonc.broker.service.model.base;

import com.bonc.broker.service.model.mysql.ClusterOp;
import com.bonc.broker.service.model.mysql.Config;
import com.bonc.broker.service.model.mysql.MysqlBackup;
import com.bonc.broker.service.model.mysql.NodeOp;

import java.util.Map;


/**
 * @author LYX
 *
 */
public class Spec {

    private String type;
    private String capacity;
    private boolean healthcheck;
    private Resources resources;
    private Config config;
    private ClusterOp clusterop;
    private NodeOp nodeop;
    private String image;
    private int replicas;
    private String version;
    private String volume;
    private String exporterimage;
    private MysqlBackup mysqlbackup;
    private Map<String, String> nodeSelector;

    /**
     * @return the nodeSelector
     */
    public Map<String, String> getNodeSelector() {
        return nodeSelector;
    }

    /**
     * @param nodeSelector
     *            the nodeSelector to set
     */
    public void setNodeSelector(Map<String, String> nodeSelector) {
        this.nodeSelector = nodeSelector;
    }

    /**
     * @return the mysqlbackup
     */
    public MysqlBackup getMysqlbackup() {
        return mysqlbackup;
    }

    /**
     * @param mysqlbackup
     *            the mysqlbackup to set
     */
    public void setMysqlbackup(MysqlBackup mysqlbackup) {
        this.mysqlbackup = mysqlbackup;
    }

    /**
     * @return the exporterimage
     */
    public String getExporterimage() {
        return exporterimage;
    }

    /**
     * @param exporterimage
     *            the exporterimage to set
     */
    public void setExporterimage(String exporterimage) {
        this.exporterimage = exporterimage;
    }

    /**
     * @return the volume
     */
    public String getVolume() {
        return volume;
    }

    /**
     * @param volume
     *            the volume to set
     */
    public void setVolume(String volume) {
        this.volume = volume;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the capacity
     */
    public String getCapacity() {
        return capacity;
    }

    /**
     * @param capacity
     *            the capacity to set
     */
    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    /**
     * @return the healthcheck
     */
    public boolean isHealthcheck() {
        return healthcheck;
    }

    /**
     * @param healthcheck
     *            the healthcheck to set
     */
    public void setHealthcheck(boolean healthcheck) {
        this.healthcheck = healthcheck;
    }

    /**
     * @return the resources
     */
    public Resources getResources() {
        return resources;
    }

    /**
     * @param resources
     *            the resources to set
     */
    public void setResources(Resources resources) {
        this.resources = resources;
    }

    /**
     * @return the config
     */
    public Config getConfig() {
        return config;
    }

    /**
     * @param config
     *            the config to set
     */
    public void setConfig(Config config) {
        this.config = config;
    }

    /**
     * @return the clusterop
     */
    public ClusterOp getClusterop() {
        return clusterop;
    }

    /**
     * @param clusterop
     *            the clusterop to set
     */
    public void setClusterop(ClusterOp clusterop) {
        this.clusterop = clusterop;
    }

    /**
     * @return the nodeop
     */
    public NodeOp getNodeop() {
        return nodeop;
    }

    /**
     * @param nodeop
     *            the nodeop to set
     */
    public void setNodeop(NodeOp nodeop) {
        this.nodeop = nodeop;
    }

    /**
     * @return the image
     */
    public String getImage() {
        return image;
    }

    /**
     * @param image
     *            the image to set
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * @return the replicas
     */
    public int getReplicas() {
        return replicas;
    }

    /**
     * @param replicas
     *            the replicas to set
     */
    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

}
