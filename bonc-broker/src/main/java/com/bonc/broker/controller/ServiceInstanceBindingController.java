package com.bonc.broker.controller;

import com.alibaba.fastjson.JSONObject;
import com.bonc.broker.common.Global;
import com.bonc.broker.common.K8sClient;
import com.bonc.broker.common.ParameterCheckingHelp;
import com.bonc.broker.common.ResponseEntityHelp;
import com.bonc.broker.common.crd.DoneableMysql;
import com.bonc.broker.common.crd.MysqlList;
import com.bonc.broker.entity.ServiceInstance;
import com.bonc.broker.entity.ServiceInstanceBinding;
import com.bonc.broker.service.DAOService;
import com.bonc.broker.service.model.base.Server;
import com.bonc.broker.service.model.mysql.Config;
import com.bonc.broker.service.model.mysql.MysqlCluster;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/mysql/v2/service_instances/{instance_id}/service_bindings/{binding_id}")
public class ServiceInstanceBindingController {

    @Autowired
    private DAOService daoService;

    protected MixedOperation<MysqlCluster, MysqlList, DoneableMysql, Resource<MysqlCluster, DoneableMysql>> k8sClientForMysql = K8sClient.getK8sClientForMysql();

    @ResponseBody
    @RequestMapping(value = {"/last_operation"}, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getLastOperation(@PathVariable("instance_id") String instance_id,
                                                                @PathVariable("binding_id") String binding_id) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseEntityHelp.setError("Support only synchronous requests!"));
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> binding(@PathVariable("instance_id") String instance_id,
                                                       @PathVariable("binding_id") String binding_id,
                                                       @RequestParam(value = "accepts_incomplete", required = false) Boolean accepts_incomplete,
                                                       @RequestBody JSONObject requestBody) {
        // 1. 参数校验
        ResponseEntity responseEntity = ParameterCheckingHelp.bindingForMysql(instance_id, binding_id,accepts_incomplete, requestBody);
        if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return responseEntity;
        }

        // 2. 构建binding信息
        String credentials = buildBindingInfo(instance_id);
        if (null == credentials || StringUtils.isBlank(credentials)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseEntityHelp.setOperation(null));
        }

        // 3. 回写到数据库ServiceBinding表
        ServiceInstanceBinding serviceInstanceBinding = daoService.saveServiceInstanceBinding(binding_id, instance_id, credentials);
        if (null == serviceInstanceBinding) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseEntityHelp.setOperation(null));
        }

        // 4. 返回
        return ResponseEntity.status(HttpStatus.OK).body(ResponseEntityHelp.setBinding(credentials));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> unBinding(@PathVariable("instance_id") String instance_id,
                                                         @PathVariable("binding_id") String binding_id,
                                                         @RequestParam("service_id") String service_id,
                                                         @RequestParam("plan_id") String plan_id) {
        // 1. 参数校验
        ResponseEntity responseEntity = ParameterCheckingHelp.unBindingForMysql(instance_id, binding_id, service_id, plan_id);
        if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return responseEntity;
        }
        // 2. 更新数据库(ServiceBinding表)，删除记录
        daoService.deleteServiceInstanceBinding(binding_id);

        // 3. 返回
        return ResponseEntity.status(HttpStatus.OK).body(ResponseEntityHelp.setMessage(""));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getBinding(@PathVariable("instance_id") String instance_id,
                                                          @PathVariable("binding_id") String binding_id) {
        // 1. 参数校验
        ResponseEntity responseEntity = ParameterCheckingHelp.getBindingInfo(instance_id, binding_id);
        if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return responseEntity;
        }

        // 2. 查询数据库，获取绑定信息
        ServiceInstanceBinding serviceInstanceBinding = daoService.getServiceInstanceBinding(binding_id);
        if (null == serviceInstanceBinding) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseEntityHelp.setError("query serviceinstanceBinding table failed!"));
        }

        // 3. 返回
        return ResponseEntity.status(HttpStatus.OK).body(ResponseEntityHelp.setBinding(serviceInstanceBinding.getCredentials()));
    }

    /**
     * @param instanceId
     * @return
     */
    private String buildBindingInfo(String instanceId) {
        // 1. 获取instance_id对应的mysqlCluster对象
        ServiceInstance serviceInstance = daoService.getServiceInstance(instanceId);
        if (null == serviceInstance) {
            //更新操作记录表，
            daoService.updateBrokerLog(instanceId, Global.STATE_F);
            return null;
        }
        MysqlCluster mysqlCluster = k8sClientForMysql.inNamespace("----####----++++--").withName(serviceInstance.getServiceName()).get();

        // 2. 转换成json格式
        String credentials = buildCredentialsInfo(mysqlCluster);

        // 3. 返回
        return credentials;
    }

    private String buildCredentialsInfo(MysqlCluster mysqlCluster) {
        HashMap<String, String> credentialsAll = new HashMap<>(16);

        HashMap<String, String> credentials = new HashMap<>(16);
        Config config = mysqlCluster.getSpec().getConfig();

        Map<String, Server> serverNodes = mysqlCluster.getStatus().getServerNodes();
        for (Map.Entry<String, Server> entry : serverNodes.entrySet()) {
            String nodeName = entry.getValue().getName();

            HashMap<String, String> cre = new HashMap<>(16);
            cre.put("userName", "root");
            cre.put("password", config.getPassword());
            cre.put("host", entry.getValue().getNodeIp());
            cre.put("port", String.valueOf(entry.getValue().getNodeport()));
            cre.put("role", entry.getValue().getRole());
            cre.put("status", entry.getValue().getStatus());

            credentials.put(nodeName, JSONObject.toJSONString(cre));
        }

        return credentialsAll.put("credentials", JSONObject.toJSONString(credentials));

    }


}
