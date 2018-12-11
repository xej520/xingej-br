/**
 * 
 */
package com.bonc.broker.service.model.redis;

/**
 * @author Yx Liu
 * @date 2018年6月6日
 *
 */
public class RedisServiceInfo {

    private int nodePort;
    private String role;
    private String svcIp;
    private String svcName;
    private String status;

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
     * @return the svcName
     */
    public String getSvcName() {
        return svcName;
    }

    /**
     * @param svcName
     *            the svcName to set
     */
    public void setSvcName(String svcName) {
        this.svcName = svcName;
    }

    /**
     * @return the nodePort
     */
    public int getNodePort() {
        return nodePort;
    }

    /**
     * @param nodePort
     *            the nodePort to set
     */
    public void setNodePort(int nodePort) {
        this.nodePort = nodePort;
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
     * @return the svcIp
     */
    public String getSvcIp() {
        return svcIp;
    }

    /**
     * @param svcIp
     *            the svcIp to set
     */
    public void setSvcIp(String svcIp) {
        this.svcIp = svcIp;
    }

}
