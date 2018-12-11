package com.bonc.broker.common;

import com.bonc.broker.common.crd.DoneableLvm;
import com.bonc.broker.common.crd.DoneableMysql;
import com.bonc.broker.common.crd.LvmList;
import com.bonc.broker.common.crd.MysqlList;
import com.bonc.broker.service.model.lvm.Lvm;
import com.bonc.broker.service.model.mysql.MysqlCluster;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.*;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

public class K8sClient {

    private final static String CRD_MYSQL = "mysqlclusters.mysql.bonc.com";
    private final static String CRD_REDIS = "mysqlclusters.redis.bonc.com";
    private final static String CRD_LVM = "lvms.lvm.bonc.com";

    public static KubernetesClient getK8sClient() {
        Config config = new ConfigBuilder().withMasterUrl("http://172.16.3.30:23333").build();

        //使用默认的就足够了
        return new DefaultKubernetesClient(config);
    }

    public static MixedOperation getK8sClientForMysql() {
        KubernetesClient k8sClient = getK8sClient();

        CustomResourceDefinition crd = k8sClient.customResourceDefinitions().withName(CRD_MYSQL).get();

        MixedOperation<MysqlCluster, MysqlList, DoneableMysql, Resource<MysqlCluster, DoneableMysql>> mysqlK8sClient = k8sClient.customResources(crd, MysqlCluster.class, MysqlList.class, DoneableMysql.class);

        return mysqlK8sClient;
    }


    public static MixedOperation getK8sClientForRedis() {
        KubernetesClient k8sClient = getK8sClient();

        CustomResourceDefinition crd = k8sClient.customResourceDefinitions().withName(CRD_REDIS).get();

        MixedOperation<MysqlCluster, MysqlList, DoneableMysql, Resource<MysqlCluster, DoneableMysql>> mysqlK8sClient = k8sClient.customResources(crd, MysqlCluster.class, MysqlList.class, DoneableMysql.class);

        return mysqlK8sClient;
    }

    public static MixedOperation getK8sClientForLvm() {
        KubernetesClient k8sClient = getK8sClient();

        CustomResourceDefinition crd = k8sClient.customResourceDefinitions().withName(CRD_LVM).get();

        MixedOperation<Lvm, LvmList, DoneableLvm, Resource<Lvm, DoneableLvm>> lvmK8sClient = k8sClient.customResources(crd, Lvm.class, LvmList.class, DoneableLvm.class);

        return lvmK8sClient;
    }

}


