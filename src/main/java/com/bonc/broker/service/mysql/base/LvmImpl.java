package com.bonc.broker.service.mysql.base;

import com.alibaba.fastjson.JSON;
import com.bonc.broker.common.K8sClient;
import com.bonc.broker.exception.BrokerException;
import com.bonc.broker.service.ILvmWorker;
import com.bonc.broker.service.model.crd.lvm.DoneableLvm;
import com.bonc.broker.service.model.crd.lvm.LvmList;
import com.bonc.broker.service.model.lvm.LVMSpec;
import com.bonc.broker.service.model.lvm.Lvm;
import com.bonc.broker.service.model.mysql.MysqlCluster;
import com.bonc.broker.service.model.mysql.MysqlServer;
import com.bonc.broker.util.StringUtils;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author xingej
 */
@Service
public class LvmImpl implements ILvmWorker<MysqlCluster> {

    private static Logger logger = LoggerFactory.getLogger(LvmImpl.class);

    private MixedOperation<Lvm, LvmList, DoneableLvm, Resource<Lvm, DoneableLvm>> k8sClientForLvm = K8sClient.getK8sClientForLvm();

    @Override
    public void registerLvm(MysqlCluster mysqlCluster) throws BrokerException{
        logger.info("===========mysql start create lvm===========");
        String namespace = mysqlCluster.getMetadata().getNamespace();
        ObjectMeta metadata;
        LVMSpec spec;
        logger.info("===1===>创建LVM" );
        Map<String, MysqlServer> nodes = mysqlCluster.getStatus().getServerNodes();

        try {
            for (Map.Entry<String, MysqlServer> entry : nodes.entrySet()) {

                // 1. 先校验是否已经存在，若存在，则删除
                Resource<Lvm, DoneableLvm> lvmDoneableLvmResource = k8sClientForLvm.inNamespace(namespace).withName(entry.getValue().getVolumeid());
                if (null != lvmDoneableLvmResource.get()) {
                    logger.warn("--->mysql create instance ---->删除已经存在的LVM");
                    k8sClientForLvm.inNamespace(namespace).delete(lvmDoneableLvmResource.get());
                }

                // 2. 开始创建LVM
                Lvm lvm  = new Lvm();
                spec = new LVMSpec();
                metadata = new ObjectMeta();

                MysqlServer volume = entry.getValue();

                spec.setHost(volume.getNodeIP());
                spec.setLvName(volume.getVolumeid());
                spec.setMessage("");
                spec.setPath("");
                spec.setSize(StringUtils.unitExchange(mysqlCluster.getSpec().getCapacity()));
                spec.setVgName(mysqlCluster.getSpec().getVolume());

                metadata.setName(volume.getVolumeid());

                lvm.setMetadata(metadata);
                lvm.setSpec(spec);

                logger.info("---开始调用LVM的create接口-----");
                k8sClientForLvm.inNamespace(namespace).create(lvm);
                logger.info("mysql create lvm ok:\t" + JSON.toJSONString(lvm));
            }
        }catch (Exception e) {
            logger.error("==mysql==create lvm  error=>创建LVM\t" + e.getMessage());
            throw new BrokerException(HttpStatus.INTERNAL_SERVER_ERROR, "mysql create lvm failed!");
        }
    }

    @Override
    public void delLvm(MysqlCluster mysqlCluster) {
        logger.info("[delLvm]---start mysql lvm serviceName:\t" + mysqlCluster.getMetadata().getName());
        if (null != mysqlCluster) {
            String namespace = mysqlCluster.getMetadata().getNamespace();
            logger.info("----开始删除---mysql---lvm----");
            Map<String, MysqlServer> nodes = mysqlCluster.getStatus().getServerNodes();
            List<Lvm> delList = new ArrayList<>(16);

            for (Map.Entry<String, MysqlServer> entry : nodes.entrySet()) {
                logger.info("[delLvm]---start mysql lvm serviceName----->");
                Resource<Lvm, DoneableLvm> lvmResource = k8sClientForLvm.inNamespace(namespace).withName(entry.getValue().getVolumeid());
                delList.add(lvmResource.get());
            }

            logger.info("----调用---mysql---lvm--的删除接口--");
            k8sClientForLvm.inNamespace(namespace).delete(delList);

            logger.info("删除集群时删除lvm成功！");
        }
    }
}
