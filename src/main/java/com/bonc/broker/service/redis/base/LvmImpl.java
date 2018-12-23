package com.bonc.broker.service.redis.base;

import com.alibaba.fastjson.JSON;
import com.bonc.broker.common.K8sClient;
import com.bonc.broker.service.ILvmWorker;
import com.bonc.broker.service.model.crd.lvm.DoneableLvm;
import com.bonc.broker.service.model.crd.lvm.LvmList;
import com.bonc.broker.service.model.lvm.LVMSpec;
import com.bonc.broker.service.model.lvm.Lvm;
import com.bonc.broker.service.model.redis.BindingNode;
import com.bonc.broker.service.model.redis.RedisCluster;
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
public class LvmImpl implements ILvmWorker<RedisCluster> {

    private static Logger logger = LoggerFactory.getLogger(LvmImpl.class);

    private MixedOperation<Lvm, LvmList, DoneableLvm, Resource<Lvm, DoneableLvm>> k8sClientForLvm = K8sClient.getK8sClientForLvm();

    @Override
    public void registerLvm(RedisCluster redisCluster) {
        logger.info("===========开始创建lvm===========");
        String namespace = redisCluster.getMetadata().getNamespace();
        Lvm lvm = null;
        ObjectMeta metadata = null;
        LVMSpec spec = null;
        Map<String, BindingNode> nodes = redisCluster.getStatus().getBindings();
        for (Map.Entry<String, BindingNode> entry : nodes.entrySet()) {
            lvm = new Lvm();
            spec = new LVMSpec();
            metadata = new ObjectMeta();

            BindingNode volume = entry.getValue();

            spec.setHost(volume.getBindIp());
            spec.setLvName(volume.getName());
            spec.setMessage("");
            spec.setPath("");
            spec.setSize(StringUtils.unitExchange(redisCluster.getSpec().getCapacity()));
            spec.setVgName(redisCluster.getSpec().getVolume());

            metadata.setName(volume.getName());

            lvm.setMetadata(metadata);
            lvm.setSpec(spec);

            k8sClientForLvm.inNamespace(namespace).create(lvm);
            logger.info("创建lvm完成，lvm：" + JSON.toJSONString(lvm));
        }
    }

    @Override
    public void delLvm(RedisCluster redisCluster) {
        if (null != redisCluster) {
            String namespace = redisCluster.getMetadata().getNamespace();
            Map<String, BindingNode> nodes = redisCluster.getStatus().getBindings();
            List<Lvm> delList = new ArrayList<>(16);

            for (Map.Entry<String, BindingNode> entry : nodes.entrySet()) {
                Resource<Lvm, DoneableLvm> lvmResource = k8sClientForLvm.inNamespace(namespace).withName(entry.getValue().getName());
                delList.add(lvmResource.get());
            }

            k8sClientForLvm.inNamespace(namespace).delete(delList);

            logger.info("删除集群时删除lvm成功！");
        }
    }
}
