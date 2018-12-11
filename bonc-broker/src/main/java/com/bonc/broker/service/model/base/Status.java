/**
 *
 */
package com.bonc.broker.service.model.base;

import com.bonc.broker.service.model.mysql.Condition;
import com.bonc.broker.service.model.redis.Services;

import java.util.List;
import java.util.Map;


/**
 * @author LYX
 */
public class Status {
    private List<Condition> conditions;
    private boolean configmapneedapply;
    private Map<String, String> currentmycnf;
    private boolean resourceupdateneedrestart;
    private boolean parameterupdateneedrestart;
    private String phase;
    private boolean paused;
    private String reason;
    private boolean stopped;
    private Map<String, Server> serverNodes;
    private int mmreplwait;

    private Map<String, Server> bindings;
    private boolean needRestart;
    private Services Services;

    /**
     * @return the services
     */
    public Services getServices() {
        return Services;
    }

    /**
     * @param services the services to set
     */
    public void setServices(Services services) {
        Services = services;
    }

    /**
     * @return the needRestart
     */
    public boolean isNeedRestart() {
        return needRestart;
    }

    /**
     * @param needRestart the needRestart to set
     */
    public void setNeedRestart(boolean needRestart) {
        this.needRestart = needRestart;
    }

    /**
     * @return the mmreplwait
     */
    public int getMmreplwait() {
        return mmreplwait;
    }

    /**
     * @param mmreplwait the mmreplwait to set
     */
    public void setMmreplwait(int mmreplwait) {
        this.mmreplwait = mmreplwait;
    }

    /**
     * @return the bindings
     */
    public Map<String, Server> getBindings() {
        return bindings;
    }

    /**
     * @param bindings the bindings to set
     */
    public void setBindings(Map<String, Server> bindings) {
        this.bindings = bindings;
    }

    /**
     * @return the conditions
     */
    public List<Condition> getConditions() {
        return conditions;
    }

    /**
     * @param conditions the conditions to set
     */
    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    /**
     * @return the configmapneedapply
     */
    public boolean isConfigmapneedapply() {
        return configmapneedapply;
    }

    /**
     * @param configmapneedapply the configmapneedapply to set
     */
    public void setConfigmapneedapply(boolean configmapneedapply) {
        this.configmapneedapply = configmapneedapply;
    }

    /**
     * @return the currentmycnf
     */
    public Map<String, String> getCurrentmycnf() {
        return currentmycnf;
    }

    /**
     * @param currentmycnf the currentmycnf to set
     */
    public void setCurrentmycnf(Map<String, String> currentmycnf) {
        this.currentmycnf = currentmycnf;
    }

    /**
     * @return the resourceupdateneedrestart
     */
    public boolean isResourceupdateneedrestart() {
        return resourceupdateneedrestart;
    }

    /**
     * @param resourceupdateneedrestart the resourceupdateneedrestart to set
     */
    public void setResourceupdateneedrestart(boolean resourceupdateneedrestart) {
        this.resourceupdateneedrestart = resourceupdateneedrestart;
    }

    /**
     * @return the parameterupdateneedrestart
     */
    public boolean isParameterupdateneedrestart() {
        return parameterupdateneedrestart;
    }

    /**
     * @param parameterupdateneedrestart the parameterupdateneedrestart to set
     */
    public void setParameterupdateneedrestart(boolean parameterupdateneedrestart) {
        this.parameterupdateneedrestart = parameterupdateneedrestart;
    }

    /**
     * @return the phase
     */
    public String getPhase() {
        return phase;
    }

    /**
     * @param phase the phase to set
     */
    public void setPhase(String phase) {
        this.phase = phase;
    }

    /**
     * @return the paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * @param paused the paused to set
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    /**
     * @return the reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * @param reason the reason to set
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * @return the stopped
     */
    public boolean isStopped() {
        return stopped;
    }

    /**
     * @param stopped the stopped to set
     */
    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    /**
     * @return the serverNodes
     */
    public Map<String, Server> getServerNodes() {
        return serverNodes;
    }

    /**
     * @param serverNodes the serverNodes to set
     */
    public void setServerNodes(Map<String, Server> serverNodes) {
        this.serverNodes = serverNodes;
    }

}
