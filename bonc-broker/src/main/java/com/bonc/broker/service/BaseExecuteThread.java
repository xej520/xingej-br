package com.bonc.broker.service;

import com.bonc.broker.common.ExecuteHelper;
import com.bonc.broker.common.Global;
import com.bonc.broker.common.K8sClient;
import com.bonc.broker.common.crd.DoneableMysql;
import com.bonc.broker.common.crd.MysqlList;
import com.bonc.broker.service.model.mysql.MysqlCluster;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xingej
 */

public abstract class BaseExecuteThread<Map> implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(BaseExecuteThread.class);

    protected MixedOperation<MysqlCluster, MysqlList, DoneableMysql, Resource<MysqlCluster, DoneableMysql>> k8sClientForMysql = K8sClient.getK8sClientForMysql();

    @Autowired
    public ExecuteHelper executeHelper;

    protected Map data = null;

    public Map getData() {
        return data;
    }

    public void setData(Map data) {
        this.data = data;
    }

    @Override
    public void run() {
        execute();
    }

    /**
     * 具体业务流程
     */
    protected abstract void execute();

    /**
     * 调用operator成功后，更新数据库
     */
    protected abstract void updateTableForS();

    /**
     * 调用operator成功后，注册LVM
     */
    protected abstract void registeLvm(MysqlCluster mysqlCluster);

    /**
     *
     * @param mysqlCluster
     */
    protected abstract void delLvm(MysqlCluster mysqlCluster);


    /**
     * 调用operator失败后，更新数据库
     */
    protected abstract void updateTableForF();

    protected void checkStatus(MysqlCluster mysqlCluster) {

        String namespace = mysqlCluster.getMetadata().getNamespace();
        String serviceName = mysqlCluster.getMetadata().getName();

        String optType = mysqlCluster.getSpec().getClusterop().getOperator();

        switch (optType) {
            case Global.OPT_MYSQL_DELETE:
                delCheckStatus(namespace, serviceName);
                break;
            case Global.OPT_MYSQL_PROVISIONING:
                createCheckStatus(mysqlCluster);
                break;

            case Global.OPT_MYSQL_UPDATE:
                updateCheckStatus(namespace, serviceName);
                break;

            default:
                logger.warn("%s does not meet the requirements; namespace: %s;\t serviceName: %s", optType, namespace, serviceName);;
        }

    }

    private void delCheckStatus(String namespace, String serviceName) {
        int time = 0;
        while (true) {
            //1. 获取YAML对象
            MysqlCluster mysqlCluster = k8sClientForMysql.inNamespace(namespace).withName(serviceName).get();

            if (null == mysqlCluster) {
                // 2. 更新数据库
                updateTableForS();
                // 3. 删除LVM
                delLvm(mysqlCluster);
            }

            time++;
            if (300 <= time) {
                //创建实例失败，不需要创建实例表
                updateTableForF();
                break;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void createCheckStatus(MysqlCluster mysqlCluster) {
        String namespace = mysqlCluster.getMetadata().getNamespace();
        String serviceName = mysqlCluster.getMetadata().getName();

        int time = 0;
        String status;
        while (true) {
            //1. 获取yaml对象
            status = k8sClientForMysql.inNamespace(namespace).withName(serviceName).get().getStatus().getPhase();

            if ("running".equalsIgnoreCase(status)) {
                // 2. 更新数据库
                updateTableForS();
                // 3. 创建LVM
                registeLvm(mysqlCluster);
            }

            time++;
            if (300 <= time) {
                //创建实例失败，不需要创建实例表
                updateTableForF();
                break;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void updateCheckStatus(String namespace, String serviceName) {
        int time = 0;
        String status;
        while (true) {
            //1. 获取yaml对象
            status = k8sClientForMysql.inNamespace(namespace).withName(serviceName).get().getStatus().getPhase();

            if ("running".equalsIgnoreCase(status)) {
                // 2. 更新数据库
                updateTableForS();

            }

            time++;
            if (300 <= time) {
                //创建实例失败，不需要创建实例表
                updateTableForF();
                break;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}


