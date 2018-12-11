/**
 * 
 */
package com.bonc.broker.service.model.lvm;

/**
 * @author LYX
 *
 */
public class LVMSpec {

    private String host;
    private String lvName;
    private String size;
    private String vgName;
    private String path;
    private String message;

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host
     *            the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the lvName
     */
    public String getLvName() {
        return lvName;
    }

    /**
     * @param lvName
     *            the lvName to set
     */
    public void setLvName(String lvName) {
        this.lvName = lvName;
    }

    /**
     * @return the size
     */
    public String getSize() {
        return size;
    }

    /**
     * @param size
     *            the size to set
     */
    public void setSize(String size) {
        this.size = size;
    }

    /**
     * @return the vgName
     */
    public String getVgName() {
        return vgName;
    }

    /**
     * @param vgName
     *            the vgName to set
     */
    public void setVgName(String vgName) {
        this.vgName = vgName;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path
     *            the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message
     *            the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

}
