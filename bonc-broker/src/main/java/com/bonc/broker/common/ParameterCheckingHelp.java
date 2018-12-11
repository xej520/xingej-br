package com.bonc.broker.common;

import com.alibaba.fastjson.JSONObject;
import com.bonc.broker.entity.ServiceInstance;
import com.bonc.broker.entity.ServiceInstanceBinding;
import com.bonc.broker.repository.ServiceInstanceBindingRepo;
import com.bonc.broker.repository.ServiceInstanceRepo;
import com.bonc.broker.service.DAOService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * @author xingej
 */
public class ParameterCheckingHelp {

    @Autowired
    private static ServiceInstanceRepo serviceInstanceRepo;

    @Autowired
    private static ServiceInstanceBindingRepo serviceInstanceBindingRepo;

    @Autowired
    private static DAOService daoService;

    /**
     * 校验字符串是否是数字
     */
    private static Pattern isIntegerPattern = Pattern.compile("^[-\\+]?[\\d]*$");

    /**
     * 针对的是 mysql的创建实例
     *
     * @param instance_id
     * @param accepts_incomplete
     * @return
     */
    public static ResponseEntity provisioningForMysql(String instance_id, Boolean accepts_incomplete) {
        Map<String, Object> body = new HashMap<>();

        // 1. 校验instance_id格式 是否符合要求
        if (StringUtils.isBlank(instance_id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseEntityHelp.setMessage("instance_id does not meet the requirements"));
        }

        // 2. 校验instance_id 是否已经存在了
        Optional<ServiceInstance> instanceIsExists = serviceInstanceRepo.findById(instance_id);
        if (instanceIsExists.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseEntityHelp.setMessage("instance_id already exists"));
        }

        // 3. 支持请求方式:异步，同步; AsyncRequired
        if (null != accepts_incomplete && false == accepts_incomplete.booleanValue()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResponseEntityHelp.setMessage("accepts_incomplete: AsyncRequired"));
        }

        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    /**
     * 针对mysql: 获取实例的校验
     *
     * @param instance_id
     * @return
     */
    public static ResponseEntity getServiceInstanceForMysql(String instance_id) {
        Map<String, Object> body = new HashMap<>(16);

        // 1. 校验instance_id格式 是否符合要求
        if (StringUtils.isBlank(instance_id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseEntityHelp.setMessage("instance_id does not meet the requirements"));
        }

        // 2. 校验instance_id 是否存在？
        Optional<ServiceInstance> instanceIsExists = serviceInstanceRepo.findById(instance_id);
        if (null == instanceIsExists) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseEntityHelp.setMessage("error: mysql-broker query table failed!"));
        }

        if (!instanceIsExists.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseEntityHelp.setError("error: instance_id not exists"));
        }

        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    /**
     * 针对mysql: 获取上次操作
     *
     * @param instance_id
     * @return
     */
    public static ResponseEntity getLastOperationForMysql(String instance_id) {
        Map<String, Object> body = new HashMap<>(16);

        // 1. 校验instance_id格式 是否符合要求
        if (StringUtils.isBlank(instance_id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseEntityHelp.setMessage("instance_id does not meet the requirements"));
        }

        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    public static ResponseEntity updateInstanceForMysql(String instance_id, Boolean accepts_incomplete, JSONObject requestBody) {
        Map<String, Object> body = new HashMap<>();

        // 1. 校验instance_id格式 是否符合要求
        if (StringUtils.isBlank(instance_id)) {
            body.put("message", "instance_id does not meet the requirements");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseEntityHelp.setMessage("instance_id does not meet the requirements"));
        }

        // 2. 校验instance_id 是否存在?
        Optional<ServiceInstance> instanceIsExists = serviceInstanceRepo.findById(instance_id);
        if (!instanceIsExists.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseEntityHelp.setError("instance_id does not exist"));
        }

        // 3. 支持请求方式:异步，同步; 有效值:不写，或者true
        if (null != accepts_incomplete && false == accepts_incomplete.booleanValue()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResponseEntityHelp.setMessage("accepts_incomplete: AsyncRequired"));
        }

        // 4. 对cpu，memory，capacity进行校验
        JSONObject parameters = requestBody.getJSONObject("parameters");
        ResponseEntity responseEntity = checkResource(parameters);

        if (!responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return responseEntity;
        }

        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    /**
     * 针对mysql的删除实例，参数校验
     *
     * @param instance_id
     * @param service_id
     * @param plan_id
     * @param accepts_incomplete
     * @return
     */
    public static ResponseEntity deleteInstanceForMysql(String instance_id, String service_id, String plan_id, Boolean accepts_incomplete) {
        Map<String, Object> body = new HashMap<>(16);

        // 1. 校验instance_id格式 是否符合要求
        if (StringUtils.isBlank(instance_id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseEntityHelp.setMessage("instance_id does not meet the requirements"));
        }

        // 2. 校验instance_id 是否存在?
        Optional<ServiceInstance> instanceIsExists = serviceInstanceRepo.findById(instance_id);
        if (!instanceIsExists.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseEntityHelp.setError("instance_id does not exist"));
        }

        // 3. 支持请求方式:异步，同步; 有效值:不写，或者true
        if (null != accepts_incomplete && false == accepts_incomplete.booleanValue()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResponseEntityHelp.setMessage("accepts_incomplete: AsyncRequired"));
        }

        // 4. 校验service_id
        if (!Global.SERVICE_ID_MYSQL.equals(service_id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseEntityHelp.setMessage("service_id not exist"));
        }

        // 5. 校验plan_id
        if (!Global.PLAN_ID_MYSQL.contains(plan_id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseEntityHelp.setMessage("plan_id not exist"));
        }

        // 6. 校验此instance实例是否还存在binding对象
        ServiceInstanceBinding serviceInstanceBinding = serviceInstanceBindingRepo.findByInstanceId(instance_id);
        if (null != serviceInstanceBinding) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseEntityHelp.setMessage("There is also a binding object; instanceId: " + instance_id));
        }

        return ResponseEntity.status(HttpStatus.OK).body(body);
    }


    /**
     * 校验字符串是否是数字
     *
     * @param str
     * @return
     */
    public static boolean isInteger(String str) {
        return isIntegerPattern.matcher(str).matches();
    }

    /**
     * 针对cpu，memory，capacity的校验
     *
     * @param parameters
     * @return
     */
    private static ResponseEntity checkResource(JSONObject parameters) {
        Map<String, Object> body = new HashMap<>(16);

        String cpu = parameters.getString("cpu");
        String memory = parameters.getString("memory");
        String capacity = parameters.getString("capacity");

        boolean cpuFlag = null == cpu || StringUtils.isBlank(cpu) || !isInteger(cpu) || isInteger(cpu) && Integer.parseInt(cpu) <= 0;
        if (cpuFlag) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseEntityHelp.setMessage("cpu does not meet the requirements"));
        }

        boolean memoryFlag = null == memory || StringUtils.isBlank(memory) || !isInteger(memory) || isInteger(memory) && Integer.parseInt(memory) <= 0;
        if (memoryFlag) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseEntityHelp.setMessage("memory does not meet the requirements"));
        }

        boolean capacityFlag = null == capacity || StringUtils.isBlank(capacity) || !isInteger(capacity) || isInteger(capacity) && Integer.parseInt(capacity) <= 0;
        if (capacityFlag) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseEntityHelp.setMessage("capacity does not meet the requirements"));
        }

        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    /**
     * mysql：binding 参数校验
     *
     * @param instance_id
     * @param binding_id
     * @param requestBody
     * @return
     */
    public static ResponseEntity bindingForMysql(String instance_id, String binding_id,Boolean accepts_incomplete, JSONObject requestBody) {
        Map<String, Object> body = new HashMap<>(16);

        // 1. 校验instance_id格式 是否符合要求
        if (StringUtils.isBlank(instance_id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseEntityHelp.setMessage("instance_id does not meet the requirements"));
        }

        // 2. 校验instance_id 是否存在?
        Optional<ServiceInstance> instanceIsExists = serviceInstanceRepo.findById(instance_id);
        if (!instanceIsExists.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseEntityHelp.setError("instance_id does not exist"));
        }

        String service_id = requestBody.getString("service_id");
        // 3. 校验service_id
        if (!Global.SERVICE_ID_MYSQL.equals(service_id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseEntityHelp.setMessage("service_id not exist"));
        }

        String plan_id = requestBody.getString("plan_id");
        // 4. 校验plan_id
        if (!Global.PLAN_ID_MYSQL.contains(plan_id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseEntityHelp.setMessage("plan_id not exist"));
        }

        // 5. 校验binding格式 是否符合要求
        if (StringUtils.isBlank(binding_id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseEntityHelp.setMessage("binding_id does not meet the requirements"));
        }

        // 5.1 校验bindingID 是否已经binding过了
        ServiceInstanceBinding serviceInstanceBinding = daoService.getServiceInstanceBinding(binding_id);
        if (null != serviceInstanceBinding) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseEntityHelp.setError("binding_id: " + binding_id + " has already exists."));
        }

        // 3. 仅支持同步，binding绑定；true表示: 支持异步请求
        if (null != accepts_incomplete && true == accepts_incomplete.booleanValue()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ResponseEntityHelp.setMessage("accepts_incomplete: SyncRequired"));
        }

        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    public static ResponseEntity unBindingForMysql(String instance_id, String binding_id, String service_id, String plan_id) {
        Map<String, Object> body = new HashMap<>(16);

        // 1. 校验instance_id格式 是否符合要求
        if (StringUtils.isBlank(instance_id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseEntityHelp.setMessage("instance_id does not meet the requirements"));
        }

        // 2. 校验instance_id 是否存在?
        Optional<ServiceInstance> instanceIsExists = serviceInstanceRepo.findById(instance_id);
        boolean instanceExistsFlag = null == instanceIsExists || null != instanceIsExists && !instanceIsExists.isPresent();
        if (instanceExistsFlag) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseEntityHelp.setError("instance_id does not exist"));
        }

        // 3. 校验service_id
        if (!Global.SERVICE_ID_MYSQL.equals(service_id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseEntityHelp.setMessage("service_id not exist"));
        }

        // 4. 校验plan_id
        if (!Global.PLAN_ID_MYSQL.contains(plan_id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseEntityHelp.setMessage("plan_id not exist"));
        }

        // 5. 校验binding格式 是否符合要求
        if (StringUtils.isBlank(binding_id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseEntityHelp.setMessage("binding_id does not meet the requirements"));
        }

        // 5.1 校验bindingID 是否bindingId 是否存在？
        ServiceInstanceBinding serviceInstanceBinding = daoService.getServiceInstanceBinding(binding_id);
        if (null == serviceInstanceBinding) {
            return ResponseEntity.status(HttpStatus.GONE).body(ResponseEntityHelp.setError("binding_id: " + binding_id + " does not exists."));
        }

        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    public static ResponseEntity getBindingInfo(String instance_id, String binding_id){
        Map<String, Object> body = new HashMap<>(16);

        // 1. 校验instance_id格式 是否符合要求
        if (StringUtils.isBlank(instance_id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseEntityHelp.setMessage("instance_id does not meet the requirements"));
        }

        // 2. 校验instance_id 是否存在?
        Optional<ServiceInstance> instanceIsExists = serviceInstanceRepo.findById(instance_id);
        boolean instanceExistsFlag = null == instanceIsExists || null != instanceIsExists && !instanceIsExists.isPresent();
        if (instanceExistsFlag) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseEntityHelp.setError("instance_id does not exist"));
        }

        // 3. 校验binding格式 是否符合要求
        if (StringUtils.isBlank(binding_id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseEntityHelp.setMessage("binding_id does not meet the requirements"));
        }

        // 4 校验bindingID 是否bindingId 是否存在？
        ServiceInstanceBinding serviceInstanceBinding = daoService.getServiceInstanceBinding(binding_id);
        if (null == serviceInstanceBinding) {
            return ResponseEntity.status(HttpStatus.GONE).body(ResponseEntityHelp.setError("binding_id: " + binding_id + " does not exists."));
        }

        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

}
