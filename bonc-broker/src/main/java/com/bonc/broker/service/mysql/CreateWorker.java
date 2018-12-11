package com.bonc.broker.service.mysql;
/**
 * @author xingej
 */

import com.alibaba.fastjson.JSON;
import com.bonc.broker.common.*;
import com.bonc.broker.common.crd.DoneableLvm;
import com.bonc.broker.common.crd.LvmList;
import com.bonc.broker.entity.UnitVersion;
import com.bonc.broker.repository.UnitVersionRepo;
import com.bonc.broker.service.BaseExecuteThread;
import com.bonc.broker.service.BaseRequstBody;
import com.bonc.broker.service.DAOService;
import com.bonc.broker.service.model.base.MemoryCPU;
import com.bonc.broker.service.model.base.Resources;
import com.bonc.broker.service.model.base.Server;
import com.bonc.broker.service.model.base.Spec;
import com.bonc.broker.service.model.lvm.LVMSpec;
import com.bonc.broker.service.model.lvm.Lvm;
import com.bonc.broker.service.model.mysql.ClusterOp;
import com.bonc.broker.service.model.mysql.Config;
import com.bonc.broker.service.model.mysql.MysqlBackup;
import com.bonc.broker.service.model.mysql.MysqlCluster;
import com.bonc.broker.util.StringUtils;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

public class CreateWorker extends BaseExecuteThread<Map<String, String>> {
    /**
     *  日志记录
     */
    private static Logger logger = LoggerFactory.getLogger(CreateWorker.class);

    protected MixedOperation<Lvm, LvmList, DoneableLvm, Resource<Lvm, DoneableLvm>> k8sClientForLvm = K8sClient.getK8sClientForLvm();

    @Value("${ratio.limittorequestcpu}")
    private int RATIO_LIMITTOREQUESTCPU;

    @Value("${ratio.limittorequestmemory}")
    private int RATIO_LIMITTOREQUESTMEMORY;

    @Value("${lvm.vgname}")
    private String vgname;

    @Value("${nodeselector.component}")
    private String componentNodeSelector;

    @Autowired
    private DAOService daoService;

    @Autowired
    private UnitVersionRepo unitVersionRepo;

    @Override
    protected void execute() {
        // 1. 创建mysql实例
        MysqlCluster mysqlCluster = createInstance();

        // 2. 开始校验集群状态，更新数据库
        checkStatus(mysqlCluster);
    }

    /**
     *  针对mysql对象，创建LVM
     * @param mysqlCluster
     */
    @Override
    protected void registeLvm(MysqlCluster mysqlCluster) {
        logger.info("===========开始创建lvm===========");
        Lvm lvm = null;
        ObjectMeta metadata = null;
        LVMSpec spec = null;
        Map<String, Server> nodes = mysqlCluster.getStatus().getServerNodes();
        for (Map.Entry<String, Server> entry : nodes.entrySet()) {
            lvm = new Lvm();

            spec = new LVMSpec();
            metadata = new ObjectMeta();
            Server volume = entry.getValue();
            spec.setHost(volume.getNodeIp());
            spec.setLvName(volume.getVolumeid());
            spec.setMessage("");
            spec.setPath("");
            spec.setSize(StringUtils.unitExchange(mysqlCluster.getSpec().getCapacity()));
            spec.setVgName(mysqlCluster.getSpec().getVolume());
            metadata.setName(volume.getVolumeid());
            lvm.setMetadata(metadata);
            lvm.setSpec(spec);

            k8sClientForLvm.create(lvm);
            logger.info("创建lvm完成，lvm：" + JSON.toJSONString(lvm));
        }
    }

    @Override
    protected void updateTableForS() {
        daoService.updateBrokerLog(data.get("id"), Global.STATE_S);
        daoService.saveServiceInstance(data.get("instance_id"),data.get("parameters"), data.get("service_id"), data.get("plan_id"));
    }

    @Override
    protected void updateTableForF() {
        daoService.updateBrokerLog(data.get("id"), Global.STATE_F);
    }

    private MysqlCluster createInstance() {
        String parameters = data.get("parameters");
        BaseRequstBody baseRequstBody = GlobalHelp.buildBaseRequestBody(parameters);

        // 1. 拼接YMAL
        MysqlCluster mysqlCluster = buildMysqlCluster(baseRequstBody);

        //2. 调用k8s接口
        return k8sClientForMysql.create(mysqlCluster);
    }

    private MysqlCluster buildMysqlCluster(BaseRequstBody baseRequstBody) {
        MysqlCluster mysqlCluster = new MysqlCluster();

        ObjectMeta metaData = new ObjectMeta();
        mysqlCluster.setKind(MysqlClusterConst.KIND);
        mysqlCluster.setApiVersion(MysqlClusterConst.API_VERSION);
        metaData.setName(baseRequstBody.getServiceName());
        metaData.setNamespace(baseRequstBody.getTenant_id());
        mysqlCluster.setMetadata(metaData);

        Spec spec = new Spec();
        ClusterOp clusterOp = new ClusterOp();

        clusterOp.setOperator(AppTypeConst.OPT_CLUSTER_CREATE);
        spec.setClusterop(clusterOp);
        spec.setVersion(baseRequstBody.getVersion());
        spec.setType(baseRequstBody.getType());

        String imageUrl = getRepoPath(AppTypeConst.APPTYPE_MYSQL, baseRequstBody.getVersion());
        if (null == imageUrl) {
            return null;
        }
        spec.setImage(imageUrl);

        Resources backResources = getResources(MysqlClusterConst.MYSQL_BACKUP_CONTAINER_DEFAULT_CPU,
                MysqlClusterConst.MYSQL_BACKUP_CONTAINER_DEFAULT_MEMORY, AppTypeConst.UNIT_GI);

        logger.info("构建backup Resources");
        MysqlBackup mysqlBackup = new MysqlBackup();

        mysqlBackup.setResources(backResources);
        //------------------------------------注意这里-----请教刘月-----backup----
        mysqlBackup.setBackupimage(getRepoPath(AppTypeConst.APPTYPE_MYSQL, baseRequstBody.getVersion()));
        logger.info("mysqlBackup：" + JSON.toJSONString(mysqlBackup));
        spec.setMysqlbackup(mysqlBackup);
        logger.info("构建backup Resources完毕");
        spec.setHealthcheck(false);
        logger.info("spec:" + JSON.toJSONString(spec));

        Config config = new Config();
        config.setPassword(baseRequstBody.getPassword());
        config.setLivenessDelayTimeout(MysqlClusterConst.HEALTH_CHECK_LIVENESS_DELAY_TIMEOUT);
        config.setLivenessFailureThreshold(MysqlClusterConst.HEALTH_CHECK_LIVENESS_FAILURE_THRESHOLD);
        config.setReadinessDelayTimeout(MysqlClusterConst.HEALTH_CHECK_READINESS_DELAY_TIMEOUT);
        config.setReadinessFailureThreshold(MysqlClusterConst.HEALTH_CHECK_READINESS_FAILURE_THRESHOLD);
        logger.info("config:" + JSON.toJSONString(config));

        spec.setConfig(config);

        logger.info("buildConfig完成,spec：" + JSON.toJSONString(spec));

        String replicas = baseRequstBody.getReplicas();
        if (!StringUtils.isBlank(replicas) && ParameterCheckingHelp.isInteger(replicas)) {

            spec.setReplicas(Integer.parseInt(replicas));

        }

        spec.setResources(getResources(baseRequstBody.getCpu(), baseRequstBody.getMemeory(), AppTypeConst.UNIT_GI));

        spec.setCapacity(baseRequstBody.getCapacity() + AppTypeConst.UNIT_GI);
        logger.info("创建mysql配置文件获取到的vgname:" + vgname);
        spec.setVolume(vgname);
        logger.info("build的mysqlcluster spec:" + JSON.toJSONString(spec));
        Map<String, String> nodeSelector = new HashMap<>(16);
//        String performance = "";
//        if (null != performance) {
//            nodeSelector.put(AppTypeConst.NODESELECTOR_PERFORMANCE, performance);
//        }

        logger.info("创建mysql配置文件获取到的componentNodeSelector:" + componentNodeSelector);
        if ("true".equals(componentNodeSelector)) {
            nodeSelector.put(AppTypeConst.APPTYPE_MYSQL, componentNodeSelector);
        }
        spec.setNodeSelector(nodeSelector);
        mysqlCluster.setSpec(spec);

        logger.info("创建拼接的mysqlCluster：" + JSON.toJSONString(mysqlCluster));

        return mysqlCluster;
    }

    /**
     * @param version
     * @return
     */
    public String getRepoPath(String appType, String version) {

        UnitVersion unitVersion = unitVersionRepo.findByTypeAndVersion(appType, version);

        if (null == unitVersion) {
            return null;
        }

        return unitVersion.getImageUrl();

    }

    @Override
    protected void delLvm(MysqlCluster mysqlCluster) {

    }

    /**
     * @param cpu
     * @param memory
     * @return
     */
    public Resources getResources(String cpu, String memory, String memoryUnit) {
        Resources resources = new Resources();

        MemoryCPU requests = new MemoryCPU();
        MemoryCPU limits = new MemoryCPU();

        requests.setCpu(String.valueOf(Math.floor(Float.parseFloat(cpu) / RATIO_LIMITTOREQUESTCPU)));
        requests.setMemory(Math.floor(Float.parseFloat(memory) / RATIO_LIMITTOREQUESTMEMORY) + memoryUnit);

        limits.setCpu(cpu);
        limits.setMemory(memory + memoryUnit);

        resources.setRequests(requests);
        resources.setLimits(limits);

        return resources;
    }

    private static class MySqlWatcher implements Watcher<MysqlCluster> {
        @Override
        public void eventReceived(Action action, MysqlCluster mysqlCluster) {
            if ((action == Action.MODIFIED) && mysqlCluster.getStatus().getPhase() == "running") {
                //1. 更新数据库
            }
        }

        @Override
        public void onClose(KubernetesClientException e) {

        }
    }


}



