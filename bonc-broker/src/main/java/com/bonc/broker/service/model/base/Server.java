/**
 *
 */
package com.bonc.broker.service.model.base;

/**
 * @author LYX
 *
 */
public class Server {
    private String id;
    private String role;
    private String name;
    private String clusterName;
    private int nodeport;
    private String svcname;
    private String address;
    private String nodeName;
    private String status;
    private long downTime;
    private String restartaction;
    private String deststatus;
    private String volumeid;
    private String configmapname;
    private String master;
    private int serverID;
    private boolean mmreplstatus;
    private int exporternodeport;
    private String nodeIp;

    private String bindNode;
    private String bindIp;

    /**
     * @return the bindNode
     */
    public String getBindNode() {
        return bindNode;
    }

    /**
     * @param bindNode
     *            the bindNode to set
     */
    public void setBindNode(String bindNode) {
        this.bindNode = bindNode;
    }

    /**
     * @return the exporternodeport
     */
    public int getExporternodeport() {
        return exporternodeport;
    }

    /**
     * @param exporternodeport
     *            the exporternodeport to set
     */
    public void setExporternodeport(int exporternodeport) {
        this.exporternodeport = exporternodeport;
    }

    /**
     * @return the serverID
     */
    public int getServerID() {
        return serverID;
    }

    /**
     * @param serverID
     *            the serverID to set
     */
    public void setServerID(int serverID) {
        this.serverID = serverID;
    }

    /**
     * @return the configmapname
     */
    public String getConfigmapname() {
        return configmapname;
    }

    /**
     * @param configmapname
     *            the configmapname to set
     */
    public void setConfigmapname(String configmapname) {
        this.configmapname = configmapname;
    }

    /**
     * @return the master
     */
    public String getMaster() {
        return master;
    }

    /**
     * @param master
     *            the master to set
     */
    public void setMaster(String master) {
        this.master = master;
    }

    /**
     * @return the mmreplstatus
     */
    public boolean isMmreplstatus() {
        return mmreplstatus;
    }

    /**
     * @param mmreplstatus
     *            the mmreplstatus to set
     */
    public void setMmreplstatus(boolean mmreplstatus) {
        this.mmreplstatus = mmreplstatus;
    }

    /**
     * @return the volumeid
     */
    public String getVolumeid() {
        return volumeid;
    }

    /**
     * @param volumeid
     *            the volumeid to set
     */
    public void setVolumeid(String volumeid) {
        this.volumeid = volumeid;
    }

    /**
     * @return the deststatus
     */
    public String getDeststatus() {
        return deststatus;
    }

    /**
     * @param deststatus
     *            the deststatus to set
     */
    public void setDeststatus(String deststatus) {
        this.deststatus = deststatus;
    }

    /**
     * @return the clusterName
     */
    public String getClusterName() {
        return clusterName;
    }

    /**
     * @param clusterName
     *            the clusterName to set
     */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role
     *            the role to set
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the nodeport
     */
    public int getNodeport() {
        return nodeport;
    }

    /**
     * @param nodeport
     *            the nodeport to set
     */
    public void setNodeport(int nodeport) {
        this.nodeport = nodeport;
    }

    /**
     * @return the svcname
     */
    public String getSvcname() {
        return svcname;
    }

    /**
     * @param svcname
     *            the svcname to set
     */
    public void setSvcname(String svcname) {
        this.svcname = svcname;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address
     *            the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the nodeName
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * @param nodeName
     *            the nodeName to set
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the downTime
     */
    public long getDownTime() {
        return downTime;
    }

    /**
     * @param downTime
     *            the downTime to set
     */
    public void setDownTime(long downTime) {
        this.downTime = downTime;
    }

    /**
     * @return the restartaction
     */
    public String getRestartaction() {
        return restartaction;
    }

    /**
     * @param restartaction
     *            the restartaction to set
     */
    public void setRestartaction(String restartaction) {
        this.restartaction = restartaction;
    }

    public String getBindIp() {
        return bindIp;
    }

    public void setBindIp(String bindIp) {
        this.bindIp = bindIp;
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

}
