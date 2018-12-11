package com.bonc.broker.service.mysql;
/**
 * @author xingej
 */
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bonc.broker.common.AppTypeConst;
import com.bonc.broker.common.Global;
import com.bonc.broker.common.MysqlClusterConst;
import com.bonc.broker.common.crd.DoneableMysql;
import com.bonc.broker.entity.ServiceInstance;
import com.bonc.broker.service.BaseExecuteThread;
import com.bonc.broker.service.DAOService;
import com.bonc.broker.service.model.base.MemoryCPU;
import com.bonc.broker.service.model.base.Resources;
import com.bonc.broker.service.model.base.Spec;
import com.bonc.broker.service.model.mysql.MysqlCluster;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

public class UpdateWorker extends BaseExecuteThread<Map<String, String>> {
    /**
     *  日志记录
     */
    private static Logger logger = LoggerFactory.getLogger(UpdateWorker.class);

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

    @Override
    protected void execute() {
        // 1. 更新mysql实例
        MysqlCluster mysqlCluster = updateInstance();

        // 2. 开始校验集群状态，更新数据库
        checkStatus(mysqlCluster);
    }

    /**
     * 1. 从k8s获取到对应的yaml对象MysqlCluster
     * 2. 更新资源属性
     * 3. 调用k8s接口，更新对象
     * 4. 开始轮询校验，
     * 5. 更新数据库
     *
     * @return
     */
    private MysqlCluster updateInstance() {
        // 1. 从k8s获取到对应的yaml对象MysqlCluster
        String instanceId = data.get("instance_id");
        ServiceInstance serviceInstance = daoService.getServiceInstance(instanceId);
        if (null == serviceInstance) {
            //更新操作记录表，
            daoService.updateBrokerLog(data.get("id"), Global.STATE_F);
            return null;
        }

        // 2. 更新资源属性
        String serviceName = serviceInstance.getServiceName();
        String tenantId = serviceInstance.getTenantId();
        Resource<MysqlCluster, DoneableMysql> mysqlResource = k8sClientForMysql.inNamespace(tenantId).withName(serviceName);
        Resource<MysqlCluster, DoneableMysql> mysqlResourceNew = updateResource(mysqlResource);

        // 3. 调用k8s接口，更新对象
        MysqlCluster orReplace = k8sClientForMysql.createOrReplace(mysqlResourceNew.get());
        return orReplace;
    }

    @Override
    protected void updateTableForS() {
        // 2. 更新数据库
        daoService.updateBrokerLog(data.get("id"), Global.STATE_S);
        daoService.updateServiceInstance(data.get("instance_id"), data.get("parameters"));
    }

    @Override
    protected void updateTableForF() {
        daoService.updateBrokerLog(data.get("id"), Global.STATE_F);
    }

    private Resource<MysqlCluster, DoneableMysql> updateResource(Resource<MysqlCluster, DoneableMysql> mysqlResource) {
        MysqlCluster mysqlCluster = mysqlResource.get();

        String parameters = data.get("parameters");
        JSONObject jsonObject = JSONObject.parseObject(parameters);
        String cpu = jsonObject.getString("cpu");
        String memory = jsonObject.getString("memory");
        String capacity = jsonObject.getString("capacity");

        Spec spec = mysqlCluster.getSpec();
        Resources resources = spec.getResources();
        MemoryCPU limits = resources.getLimits();
        MemoryCPU requests = resources.getRequests();
        String json = JSON.toJSONString(data);

        logger.info("json" + JSON.toJSONString(json));

        requests.setCpu(String.valueOf(Math.floor(Float.parseFloat(cpu) / RATIO_LIMITTOREQUESTCPU)));
        limits.setCpu(cpu);

        requests.setMemory(
                Math.floor(Float.parseFloat(memory) / RATIO_LIMITTOREQUESTMEMORY) + AppTypeConst.UNIT_GI);
        limits.setMemory(memory + AppTypeConst.UNIT_GI);

        spec.setCapacity(capacity + AppTypeConst.UNIT_GI);

        resources.setRequests(requests);
        resources.setLimits(limits);
        spec.setResources(resources);
        spec.getClusterop().setOperator(MysqlClusterConst.MYSQL_CLUSTER_OPT_UPDATE_INSTANCE);

        mysqlCluster.setSpec(spec);

        return mysqlResource;
    }

    @Override
    protected void delLvm(MysqlCluster mysqlCluster) {

    }

    @Override
    protected void registeLvm(MysqlCluster mysqlCluster) {

    }
}



