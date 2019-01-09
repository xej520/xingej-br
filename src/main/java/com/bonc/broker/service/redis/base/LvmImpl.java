package com.bonc.broker.service.redis.base;

import com.alibaba.fastjson.JSON;
import com.bonc.broker.common.K8sClient;
import com.bonc.broker.exception.BrokerException;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author xingej
 */
@Service
public class LvmImpl implements ILvmWorker<RedisCluster> {

	private static Logger logger = LoggerFactory.getLogger(LvmImpl.class);

	private MixedOperation<Lvm, LvmList, DoneableLvm, Resource<Lvm, DoneableLvm>> k8sClientForLvm = K8sClient.getK8sClientForLvm();

	@Override
	public void registerLvm(RedisCluster redisCluster) throws BrokerException {
		logger.info("===========redis start create lvm===========");
		String namespace = redisCluster.getMetadata().getNamespace();
		Lvm lvm;
		ObjectMeta metadata;
		LVMSpec spec;
		Map<String, BindingNode> nodes = redisCluster.getStatus().getBindings();
		try {
			for (Map.Entry<String, BindingNode> entry : nodes.entrySet()) {

				// 1. 先校验是否已经存在，若存在，则删除
				Resource<Lvm, DoneableLvm> lvmDoneableLvmResource = k8sClientForLvm.inNamespace(namespace).withName(entry.getValue().getName());
				if (null != lvmDoneableLvmResource.get()) {
					logger.warn("--->redis create instance ---->删除已经存在的LVM");
					k8sClientForLvm.inNamespace(namespace).delete(lvmDoneableLvmResource.get());
				}

				// 2. 开始创建
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
				logger.info("redis create lvm ok:\t" + JSON.toJSONString(lvm));
			}
		}catch (Exception e) {
			logger.error("==redis==create lvm  error=>创建LVM\t" + e.getMessage());
			throw new BrokerException(HttpStatus.INTERNAL_SERVER_ERROR, "redis: create lvm failed!");
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
