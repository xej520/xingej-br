package com.bonc.broker.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class JsonUtil {
    private static Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    /**
     * 将接收的收据转为json串
     * 
     * @param object
     * @return JSONObject
     */
    public static JSONObject toJsonObject(Object object) {

        if (null == object) {
            return null;
        }

        String json;
        if (object instanceof String) {
            json = object.toString();
        } else {
            json = JSON.toJSONString(object);
        }
        JSONObject jsonObj = new JSONObject();

        try {
            jsonObj = JSON.parseObject(json);
        } catch (Exception e) {
            logger.error("json转换失败，不是正确的json格式，json ： " + json, e);
        }

        return jsonObj;
    }

}