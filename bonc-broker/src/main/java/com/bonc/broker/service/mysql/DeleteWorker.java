package com.bonc.broker.service.mysql;
/**
 * @author xingej
 */

import com.bonc.broker.common.Global;
import com.bonc.broker.common.K8sClient;
import com.bonc.broker.common.crd.DoneableLvm;
import com.bonc.broker.common.crd.DoneableMysql;
import com.bonc.broker.common.crd.LvmList;
import com.bonc.broker.entity.ServiceInstance;
import com.bonc.broker.service.BaseExecuteThread;
import com.bonc.broker.service.DAOService;
import com.bonc.broker.service.model.base.Server;
import com.bonc.broker.service.model.lvm.Lvm;
import com.bonc.broker.service.model.mysql.MysqlCluster;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeleteWorker extends BaseExecuteThread<Map<String, String>> {

    /**
     * 日志记录
     */
    private static Logger logger = LoggerFactory.getLogger(DeleteWorker.class);

    protected MixedOperation<Lvm, LvmList, DoneableLvm, Resource<Lvm, DoneableLvm>> k8sClientForLvm = K8sClient.getK8sClientForLvm();

    @Autowired
    private DAOService daoService;

    @Override
    protected void execute() {
        // 1. 删除实例
        MysqlCluster mysqlCluster = deleteServiceInstance();

        // 2. 校验轮询，删除数据库，删除LVM
        checkStatus(mysqlCluster);
    }

    @Override
    protected void updateTableForS() {
        // 1. 更新数据库
        String id = data.get("id");
        daoService.updateBrokerLog(id, Global.STATE_S);
        daoService.deleteServiceInstance(id);
    }

    @Override
    protected void delLvm(MysqlCluster mysqlCluster) {
        if (null != mysqlCluster) {
            Map<String, Server> nodes = mysqlCluster.getStatus().getServerNodes();
            List<Lvm> delList = new ArrayList<>(16);

            for (Map.Entry<String, Server> entry : nodes.entrySet()) {
                Resource<Lvm, DoneableLvm> lvmResource = k8sClientForLvm.inNamespace("default").withName(entry.getValue().getVolumeid());
                delList.add(lvmResource.get());
            }

            k8sClientForLvm.delete(delList);

            logger.info("删除集群时删除lvm成功！");
        }
    }

    @Override
    protected void registeLvm(MysqlCluster mysqlCluster) {

    }

    @Override
    protected void updateTableForF() {
        daoService.updateBrokerLog(data.get("id"), Global.STATE_F);
    }

    private MysqlCluster deleteServiceInstance() {
        // 1. 从k8s获取到对应的yaml对象MysqlCluster
        String instanceId = data.get("instance_id");
        ServiceInstance serviceInstance = daoService.getServiceInstance(instanceId);
        if (null == serviceInstance) {
            //更新操作记录表，
            daoService.updateBrokerLog(data.get("id"), Global.STATE_F);
            return null;
        }
        // 2. 删除
        String serviceName = serviceInstance.getServiceName();
        Resource<MysqlCluster, DoneableMysql> mysqlClusterResource = k8sClientForMysql.inNamespace("").withName(serviceName);

        k8sClientForMysql.inNamespace("").delete(mysqlClusterResource.get());

        return mysqlClusterResource.get();
    }


}
