package com.bonc.broker.service.redis.base;

import com.bonc.broker.common.K8sClient;
import com.bonc.broker.exception.BrokerException;
import com.bonc.broker.service.BaseExecuteThread;
import com.bonc.broker.service.model.crd.lvm.DoneableLvm;
import com.bonc.broker.service.model.crd.lvm.LvmList;
import com.bonc.broker.service.model.crd.redis.DoneableRedis;
import com.bonc.broker.service.model.crd.redis.RedisList;
import com.bonc.broker.service.model.lvm.Lvm;
import com.bonc.broker.service.model.redis.RedisCluster;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author xingej
 */
public abstract class BaseWorkerThread extends BaseExecuteThread<Map<String, String>> {

    private static Logger logger = LoggerFactory.getLogger(BaseWorkerThread.class);

    protected MixedOperation<RedisCluster, RedisList, DoneableRedis, Resource<RedisCluster, DoneableRedis>> k8sClientForRedis = K8sClient.getK8sClientForRedis();

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
     * 校验状态
     * @param redisCluster
     * @return
     * @throws BrokerException
     */
    protected abstract boolean checkStatus(RedisCluster redisCluster) throws BrokerException;

    /**
     * 校验成功后，进行的业务逻辑处理
     * @param redisCluster
     */
    protected abstract void processAfterCheckStatusSucceed(RedisCluster redisCluster);

    /**
     * 校验失败后，进行的业务逻辑处理
     * @param redisCluster
     */
    protected abstract void processAfterCheckStatusFail(RedisCluster redisCluster);



}
