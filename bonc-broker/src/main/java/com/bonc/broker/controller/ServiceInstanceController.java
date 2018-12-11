package com.bonc.broker.controller;

import com.alibaba.fastjson.JSONObject;
import com.bonc.broker.common.*;
import com.bonc.broker.entity.BrokerOptLog;
import com.bonc.broker.entity.ServiceInstance;
import com.bonc.broker.service.DAOService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xingej
 */

@RestController
@RequestMapping(value = "/mysql/v2/service_instances/{instance_id}")
public class ServiceInstanceController {

    @Autowired
    private DAOService daoService;

    @PutMapping
    public ResponseEntity<Map<String, Object>> provisioning(@PathVariable("instance_id") String instance_id,
                                                            @RequestParam(value = "accepts_incomplete", required = false) Boolean accepts_incomplete,
                                                            @RequestBody JSONObject provisionRequestBody) {

        // 1. 参数校验
        ResponseEntity responseEntity = ParameterCheckingHelp.provisioningForMysql(instance_id, accepts_incomplete);
        if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return responseEntity;
        }

        // 2. 更新操作记录表
        String id = daoService.saveBrokerLog(instance_id, Global.OPT_MYSQL_PROVISIONING);
        if (StringUtils.isBlank(id)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseEntityHelp.setOperation(null));
        }

        // 3. 异步(创建实例)
        Map<String, String> data = buildData(id, instance_id, provisionRequestBody);
        ExecuteHelper.addPool(AppTypeConst.APPTYPE_MYSQL, MysqlClusterConst.MYSQL_CLUSTER_CREATE, data);

        // 4. 返回
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseEntityHelp.setOperation(id));
    }


    @PatchMapping
    public ResponseEntity<Map<String, Object>> updateInstance(@PathVariable("instance_id") String instance_id,
                                                              @RequestParam(value = "accepts_incomplete", required = false) Boolean accepts_incomplete,
                                                              @RequestBody JSONObject requestBody) {
        // 1. 参数校验
        ResponseEntity responseEntity = ParameterCheckingHelp.updateInstanceForMysql(instance_id, accepts_incomplete, requestBody);
        if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return responseEntity;
        }

        // 2. 更新操作记录表
        String id = daoService.saveBrokerLog(instance_id, Global.OPT_MYSQL_UPDATE);
        if (StringUtils.isBlank(id)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseEntityHelp.setOperation(null));
        }

        // 3. 异步 更新
        Map<String, String> data = buildData(id, instance_id, requestBody);
        ExecuteHelper.addPool(AppTypeConst.APPTYPE_MYSQL, MysqlClusterConst.MYSQL_CLUSTER_UPDATE, data);

        // 4. 返回

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseEntityHelp.setOperation(id));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteInstance(@PathVariable("instance_id") String instance_id,
                                                              @RequestParam("service_id") String service_id,
                                                              @RequestParam("plan_id") String plan_id,
                                                              @RequestParam(value = "accepts_incomplete", required = false) Boolean accepts_incomplete) {
        // 1. 参数校验
        ResponseEntity responseEntity = ParameterCheckingHelp.deleteInstanceForMysql(instance_id, service_id, plan_id, accepts_incomplete);
        if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return responseEntity;
        }

        // 2. 更新操作记录表
        String id = daoService.saveBrokerLog(instance_id, Global.OPT_MYSQL_DELETE);
        if (StringUtils.isBlank(id)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseEntityHelp.setOperation(null));
        }

        // 3. 异步 删除
        Map<String, String> data = buildData(id, instance_id, null);
        ExecuteHelper.addPool(AppTypeConst.APPTYPE_MYSQL, MysqlClusterConst.MYSQL_CLUSTER_DELETE, data);

        // 4. 返回

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ResponseEntityHelp.setOperation(id));
    }

    /**
     *  获取mysql实例
     * @param instance_id
     * @return
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getServiceInstance(@PathVariable("instance_id") String instance_id) {
        // 1. 参数校验
        ResponseEntity responseEntityForMysql = ParameterCheckingHelp.getServiceInstanceForMysql(instance_id);
        if (!responseEntityForMysql.getStatusCode().equals(HttpStatus.OK)) {
            return responseEntityForMysql;
        }

        // 2. 获取实例
        ServiceInstance serviceInstance = daoService.getServiceInstance(instance_id);
        if (null == serviceInstance) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseEntityHelp.setMessage("error: mysql-broker query table failed!"));
        }

        // 3. 返回(同步)
        return ResponseEntity.status(HttpStatus.OK).body(ResponseEntityHelp.setServiceInstance(serviceInstance.getServiceId(), serviceInstance.getPlanId(), serviceInstance.getParameters()));
    }

    @ResponseBody
    @RequestMapping(value = {"/last_operation"}, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getLastOperationForMysql(@PathVariable("instance_id") String instance_id,
                                                                @RequestParam("operation") String operation) {
        // 1. 参数校验
        ResponseEntity responseEntity = ParameterCheckingHelp.getLastOperationForMysql(instance_id);
        if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return  responseEntity;
        }

        // 2. 查询操作记录表
        BrokerOptLog brokerOptLog = daoService.getBrokerLogRepo(operation);
        if (null == brokerOptLog) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseEntityHelp.setMessage("error: mysql-broker query table failed!"));
        }

        return ResponseEntity.status(HttpStatus.OK).body(ResponseEntityHelp.setLastOperation(brokerOptLog.getState()));
    }

    /**
     * @param id          操作记录表的主键
     * @param instance_id 实例ID
     * @param requestBody 创建实例时，传递过来的请求体
     * @return
     */
    private Map<String, String> buildData(String id, String instance_id, JSONObject requestBody) {
        JSONObject jsonObject = requestBody.getJSONObject("parameters");

        Map<String, String> data = new HashMap<>(16);

        data.put("id", id);
        data.put("instance_id", instance_id);

        // 针对的是创建实例，更新实例操作
        if (null != requestBody) {

            data.put("parameters", jsonObject.toJSONString());
            data.put("service_id", requestBody.getJSONObject("service_id").toString());

            JSONObject planId = requestBody.getJSONObject("plan_id");

            // planID，并非必传参数
            if (null != planId) {
                data.put("plan_id", planId.toString());
            }
        }

        return data;
    }

}
