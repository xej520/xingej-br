package com.bonc.broker.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bonc.broker.service.BaseRequstBody;

public class GlobalHelp {

    /**
     * 返回的是ms,mm,single
     *
     * @param planId
     * @return
     */
    public static String getType(String planId) {

        String type = "";

        switch (planId) {
            case Global.PLAN_ID_MYSQL_MM:
                type = "mm";
                break;
            case Global.PLAN_ID_MYSQL_SINGLE:
                type = "single";
                break;
            case Global.PLAN_ID_MYSQL_MS:
                type = "ms";
                break;

        }

        return type;
    }

    /**
     * 根据planID，来判断是mysql broker，还是redis broker
     *
     * @param planId
     * @return
     */
    public static String getServiceType(String planId) {
        if (Global.PLAN_ID_MYSQL.contains(planId)) {
            return "mysql";
        }

        if (Global.PLAN_ID_REDIS.contains(planId)) {
            return "redis";
        }

        return "";
    }

    public static BaseRequstBody buildBaseRequestBody(String parameters) {
        BaseRequstBody baseRequstBody = new BaseRequstBody();

        JSONObject parametersJson = JSON.parseObject(parameters);

        // 0. 解析参数
        String version = parametersJson.getString("version");
        // type:MS,SINGLE,MM
        String type = parametersJson.getString("type");
        String serviceName = parametersJson.getString("serviceName");
        String password = parametersJson.getString("password");
        String cpu = parametersJson.getString("cpu");
        String memeory = parametersJson.getString("memeory");
        String capacity = parametersJson.getString("capacity");

        String replicas = parametersJson.getString("replicas");
        String tenantId = parametersJson.getString("tenant_id");

        baseRequstBody.setCapacity(capacity);
        baseRequstBody.setCpu(cpu);
        baseRequstBody.setMemeory(memeory);
        baseRequstBody.setPassword(password);
        baseRequstBody.setReplicas(replicas);
        baseRequstBody.setServiceName(serviceName);
        baseRequstBody.setTenant_id(tenantId);
        baseRequstBody.setType(type);
        baseRequstBody.setVersion(version);

        return baseRequstBody;

    }

}
