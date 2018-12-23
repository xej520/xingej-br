package com.bonc.broker.common;

import com.bonc.broker.service.model.crd.lvm.DoneableLvm;
import com.bonc.broker.service.model.crd.lvm.LvmList;
import com.bonc.broker.service.model.crd.mysql.DoneableMysql;
import com.bonc.broker.service.model.crd.mysql.MysqlList;
import com.bonc.broker.service.model.crd.redis.DoneableRedis;
import com.bonc.broker.service.model.crd.redis.RedisList;
import com.bonc.broker.service.model.lvm.Lvm;
import com.bonc.broker.service.model.mysql.MysqlCluster;
import com.bonc.broker.service.model.redis.RedisCluster;
import io.fabric8.kubernetes.api.model.Doneable;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.*;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.internal.CustomResourceOperationsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xingej
 */
public class K8sClient {

    private static Logger logger = LoggerFactory.getLogger(K8sClient.class);

    private static BoncKubernetesClient kubernetesClient;
    private final static String CRD_MYSQL = "mysqlclusters.mysql.bonc.com";
    private final static String CRD_REDIS = "redises.redis.bonc.com";
    private final static String CRD_LVM = "lvms.bonc.com";

    public static KubernetesClient getK8sClient() {

        if (null == kubernetesClient) {
            logger.info("---从配置文件中读取到的masterURL:\t" + BrokerCfg.MASTER);
            Config config;
            try {
                config = new ConfigBuilder().withMasterUrl("http://172.16.3.30:23333").build();
            }catch (Exception e){
                logger.error("[ Get k8s config ]\t" + e.getMessage());
                return null;
            }

            // 使用自定义的客户端，可以实现级联删除
            kubernetesClient = new BoncKubernetesClient(config);
        }

        //使用默认的就足够了
        return kubernetesClient;
    }

    public static MixedOperation getK8sClientForMysql() {
        KubernetesClient k8sClient = getK8sClient();

        if (null == k8sClient) {
            return null;
        }

        CustomResourceDefinition crd = k8sClient.customResourceDefinitions().withName(CRD_MYSQL).get();

        MixedOperation<MysqlCluster, MysqlList, DoneableMysql, Resource<MysqlCluster, DoneableMysql>> mysqlK8sClient =
                k8sClient.customResources(crd, MysqlCluster.class, MysqlList.class, DoneableMysql.class);
        logger.info("=======k8s--client: mysql");
        return mysqlK8sClient;
    }

    public static MixedOperation getK8sClientForRedis() {
        KubernetesClient k8sClient = getK8sClient();

        if (null == k8sClient) {
            return null;
        }

        CustomResourceDefinition crd = k8sClient.customResourceDefinitions().withName(CRD_REDIS).get();

        MixedOperation<RedisCluster, RedisList, DoneableRedis, Resource<RedisCluster, DoneableRedis>> redisK8sClient = k8sClient.customResources(crd, RedisCluster.class, RedisList.class, DoneableRedis.class);
        logger.info("=======k8s--client: redis");
        return redisK8sClient;
    }

    public static MixedOperation getK8sClientForLvm() {
        KubernetesClient k8sClient = getK8sClient();
        if (null == k8sClient) {
            return null;
        }

        CustomResourceDefinition crd = k8sClient.customResourceDefinitions().withName(CRD_LVM).get();

        if (null == crd) {
            logger.info("----------------------------crd---------------------------------------------------");
        }

        MixedOperation<Lvm, LvmList, DoneableLvm, Resource<Lvm, DoneableLvm>> lvmK8sClient = k8sClient.customResources(crd, Lvm.class, LvmList.class, DoneableLvm.class);
        logger.info("=======k8s--client: lvm");
        return lvmK8sClient;
    }


    public static class BoncKubernetesClient extends DefaultKubernetesClient {

        /**
         * cascading delete default is false
         */
        @SuppressWarnings("rawtypes")
        @Override
        public <T extends HasMetadata, L extends KubernetesResourceList, D extends Doneable<T>> MixedOperation<T, L, D, Resource<T, D>> customResources(
                CustomResourceDefinition crd, Class<T> resourceType, Class<L> listClass, Class<D> doneClass) {
            return new CustomResourceOperationsImpl<T, L, D>(httpClient, getConfiguration(), crd.getSpec().getGroup(),
                    crd.getSpec().getVersion(), crd.getSpec().getNames().getPlural(), null, null, true, null, null,
                    false, resourceType, listClass, doneClass);
        }

        public BoncKubernetesClient(Config config) throws KubernetesClientException {
            super(config);
        }

    }

}


