package com.bonc.broker.service.mysql.base;

import com.bonc.broker.common.K8sClient;
import com.bonc.broker.exception.BrokerException;
import com.bonc.broker.service.BaseExecuteThread;
import com.bonc.broker.service.model.crd.lvm.DoneableLvm;
import com.bonc.broker.service.model.crd.lvm.LvmList;
import com.bonc.broker.service.model.crd.mysql.DoneableMysql;
import com.bonc.broker.service.model.crd.mysql.MysqlList;
import com.bonc.broker.service.model.lvm.Lvm;
import com.bonc.broker.service.model.mysql.MysqlCluster;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author xingej
 */
public abstract class BaseWorkerThread extends BaseExecuteThread<Map<String, String>> {

    protected MixedOperation<MysqlCluster, MysqlList, DoneableMysql, Resource<MysqlCluster, DoneableMysql>> k8sClientForMysql = K8sClient.getK8sClientForMysql();

    protected MixedOperation<Lvm, LvmList, DoneableLvm, Resource<Lvm, DoneableLvm>> k8sClientForLvm = K8sClient.getK8sClientForLvm();

    @Autowired
    protected LvmImpl lvm;

    /**
     * 调用operator成功后，更新数据库
     */
    protected abstract void updateTableForS();

    /**
     * 调用operator失败后，更新数据库
     */
    protected abstract void updateTableForF();

    /**
     * 校验
     * @param mysqlCluster
     * @return
     * @throws BrokerException
     */
    protected abstract boolean checkStatus(MysqlCluster mysqlCluster) throws BrokerException;

    /**
     * 校验成功后，进行的业务逻辑处理
     * @param mysqlCluster
     */
    protected abstract void processAfterCheckStatusSucceed(MysqlCluster mysqlCluster);

    /**
     * 校验失败后，进行的业务逻辑处理
     * @param mysqlCluster
     */
    protected abstract void processAfterCheckStatusFail(MysqlCluster mysqlCluster);

}
