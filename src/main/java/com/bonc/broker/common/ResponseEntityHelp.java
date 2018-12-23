package com.bonc.broker.common;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xingej
 */
public class ResponseEntityHelp {

    public static HashMap<String, Object> setOperation(String id) {
        HashMap<String, Object> newBody = new HashMap(16);
        newBody.put("dashboard_url", null);
        newBody.put("operation", id);

        return newBody;
    }

    public static HashMap<String, Object> setMessage(String message) {
        HashMap<String, Object> newBody = new HashMap(16);
        newBody.put("message", message);

        return newBody;
    }

    /**
     * @param message
     * @return
     */
    public static HashMap<String, Object> setError(String message) {
        HashMap<String, Object> newBody = new HashMap(16);
        newBody.put("error", message);

        return newBody;
    }


    /**
     * 针对mysql，获取实例
     *
     * @param serviceId
     * @param planID
     * @param parameters
     * @return
     */
    public static HashMap<String, Object> setServiceInstance(String serviceId, String planID, JSONObject parameters) {
        HashMap<String, Object> newBody = new HashMap(16);

        newBody.put("service_id", serviceId);
        newBody.put("plan_id", planID);
        newBody.put("dashboard_url", null);
        newBody.put("parameters", parameters);

        return newBody;
    }

    /**
     *
     * @param state
     * @return
     */
    public static HashMap<String, Object> setLastOperation(String state) {
        HashMap<String, Object> newBody = new HashMap<String, Object>(16) {
            {
                put("description", null);
            }
        };

        newBody.put("state", state);

        return newBody;
    }

    /**
     * binding信息
     * @param credentials
     * @return
     */
    public static HashMap<String, Object> setBinding(Map<String, Object> credentials) {
        HashMap<String, Object> newBody = new HashMap<String, Object>(16) {
            {
                put("syslog_drain_url", null);
                put("route_service_url", null);
                put("volume_mounts", null);
                put("parameters", null);
            }
        };

        newBody.put("credentials", credentials);

        return newBody;
    }

}
