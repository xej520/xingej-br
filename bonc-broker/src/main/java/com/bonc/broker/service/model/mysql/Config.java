/**
 * 
 */
package com.bonc.broker.service.model.mysql;

import java.util.Map;

/**
 * @author LYX
 *
 */
public class Config {
    private String password;
    private String mysqldb;
    private Map<String, String> mycnf;
    private boolean configmapapply;
    private int livenessDelayTimeout;
    private int readinessDelayTimeout;
    private int livenessFailureThreshold;
    private int readinessFailureThreshold;
    private String repluser;
    private String replpassword;

    /**
     * @return the livenessDelayTimeout
     */
    public int getLivenessDelayTimeout() {
        return livenessDelayTimeout;
    }

    /**
     * @param livenessDelayTimeout
     *            the livenessDelayTimeout to set
     */
    public void setLivenessDelayTimeout(int livenessDelayTimeout) {
        this.livenessDelayTimeout = livenessDelayTimeout;
    }

    /**
     * @return the readinessDelayTimeout
     */
    public int getReadinessDelayTimeout() {
        return readinessDelayTimeout;
    }

    /**
     * @param readinessDelayTimeout
     *            the readinessDelayTimeout to set
     */
    public void setReadinessDelayTimeout(int readinessDelayTimeout) {
        this.readinessDelayTimeout = readinessDelayTimeout;
    }

    /**
     * @return the livenessFailureThreshold
     */
    public int getLivenessFailureThreshold() {
        return livenessFailureThreshold;
    }

    /**
     * @param livenessFailureThreshold
     *            the livenessFailureThreshold to set
     */
    public void setLivenessFailureThreshold(int livenessFailureThreshold) {
        this.livenessFailureThreshold = livenessFailureThreshold;
    }

    /**
     * @return the readinessFailureThreshold
     */
    public int getReadinessFailureThreshold() {
        return readinessFailureThreshold;
    }

    /**
     * @param readinessFailureThreshold
     *            the readinessFailureThreshold to set
     */
    public void setReadinessFailureThreshold(int readinessFailureThreshold) {
        this.readinessFailureThreshold = readinessFailureThreshold;
    }

    /**
     * @return the repluser
     */
    public String getRepluser() {
        return repluser;
    }

    /**
     * @param repluser
     *            the repluser to set
     */
    public void setRepluser(String repluser) {
        this.repluser = repluser;
    }

    /**
     * @return the replpassword
     */
    public String getReplpassword() {
        return replpassword;
    }

    /**
     * @param replpassword
     *            the replpassword to set
     */
    public void setReplpassword(String replpassword) {
        this.replpassword = replpassword;
    }

    /**
     * @return the mysqldb
     */
    public String getMysqldb() {
        return mysqldb;
    }

    /**
     * @param mysqldb
     *            the mysqldb to set
     */
    public void setMysqldb(String mysqldb) {
        this.mysqldb = mysqldb;
    }

    /**
     * @return the configmapapply
     */
    public boolean isConfigmapapply() {
        return configmapapply;
    }

    /**
     * @param configmapapply
     *            the configmapapply to set
     */
    public void setConfigmapapply(boolean configmapapply) {
        this.configmapapply = configmapapply;
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
     * @return the mycnf
     */
    public Map<String, String> getMycnf() {
        return mycnf;
    }

    /**
     * @param mycnf
     *            the mycnf to set
     */
    public void setMycnf(Map<String, String> mycnf) {
        this.mycnf = mycnf;
    }

}
