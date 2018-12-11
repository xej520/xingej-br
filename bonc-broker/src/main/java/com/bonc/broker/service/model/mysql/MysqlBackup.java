/**
 * 
 */
package com.bonc.broker.service.model.mysql;

import com.bonc.broker.service.model.base.Resources;

/**
 * @author Yx Liu
 * @date 2018年7月3日
 *
 */
public class MysqlBackup {

    private String backupimage;
    private Resources resources;

    /**
     * @return the backupimage
     */
    public String getBackupimage() {
        return backupimage;
    }

    /**
     * @param backupimage
     *            the backupimage to set
     */
    public void setBackupimage(String backupimage) {
        this.backupimage = backupimage;
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

}
