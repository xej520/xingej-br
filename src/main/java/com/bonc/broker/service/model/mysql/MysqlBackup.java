package com.bonc.broker.service.model.mysql;

import com.bonc.broker.service.model.base.Resources;

public class MysqlBackup {

    private String backupimage;
    private Resources resources;

    public String getBackupimage() {
        return backupimage;
    }

    public void setBackupimage(String backupimage) {
        this.backupimage = backupimage;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

}
