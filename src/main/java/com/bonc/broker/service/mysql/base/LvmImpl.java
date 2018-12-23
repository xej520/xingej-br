package com.bonc.broker.service.mysql.base;

import com.alibaba.fastjson.JSON;
import com.bonc.broker.common.K8sClient;
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
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author xingej
 */
@Component
public class LvmImpl implements ILvmWorker<MysqlCluster> {

    private static Logger logger = LoggerFactory.getLogger(LvmImpl.class);

    private MixedOperation<Lvm, LvmList, DoneableLvm, Resource<Lvm, DoneableLvm>> k8sClientForLvm = K8sClient.getK8sClientForLvm();

    @Override
    public void registerLvm(MysqlCluster mysqlCluster) {
        logger.info("===========开始创建lvm===========");

        String namespace = mysqlCluster.getMetadata().getNamespace();
        ObjectMeta metadata;
        LVMSpec spec;
        logger.info("===1===>创建LVM" );
        Map<String, MysqlServer> nodes = mysqlCluster.getStatus().getServerNodes();

        try {
            for (Map.Entry<String, MysqlServer> entry : nodes.entrySet()) {
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

                k8sClientForLvm.inNamespace(namespace).create(lvm);
                logger.info("创建lvm完成，lvm：" + JSON.toJSONString(lvm));
            }
        }catch (Exception e) {
            logger.info("===2===>创建LVM\t" + e.getMessage());
        }
    }

    @Override
    public void delLvm(MysqlCluster mysqlCluster) {
        if (null != mysqlCluster) {
            String namespace = mysqlCluster.getMetadata().getNamespace();
            logger.info("----开始删除---mysql---lvm----");
            Map<String, MysqlServer> nodes = mysqlCluster.getStatus().getServerNodes();
            List<Lvm> delList = new ArrayList<>(16);

            for (Map.Entry<String, MysqlServer> entry : nodes.entrySet()) {
                Resource<Lvm, DoneableLvm> lvmResource = k8sClientForLvm.inNamespace("default").withName(entry.getValue().getVolumeid());
                delList.add(lvmResource.get());
            }

            k8sClientForLvm.inNamespace(namespace).delete(delList);

            logger.info("删除集群时删除lvm成功！");
        }
    }
}
