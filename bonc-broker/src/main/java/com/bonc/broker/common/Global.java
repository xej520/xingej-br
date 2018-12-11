package com.bonc.broker.common;

import java.util.HashSet;

public class Global {
    public static final String SERVICE_ID_MYSQL = "8780d398-a010-4696-bb0d-78ea6511fa95";
    public static final String PLAN_ID_MYSQL_MS = "aee3dab8-8cf9-4d3a-a2fd-f8155e56750e";
    public static final String PLAN_ID_MYSQL_SINGLE = "f0dfb49f-91cf-42af-871c-d06d8f7887dd";
    public static final String PLAN_ID_MYSQL_MM = "17f5165d-737e-4742-a6a2-f368f0e632d6";


    public static final String SERVICE_ID_redis = "764ad650-e6cf-4855-9ab5-51820afa3515";
    public static final String PLAN_ID_REDIS_SINGLE = "64002283-77a5-40f3-859d-d37a6d202bc3";
    public static final String PLAN_ID_REDIS_MS = "e44b275f-fe4b-4721-aa18-009c1ce7353e";
    public static final String PLAN_ID_REDIS_MS_SENTINEL = "b6ab12b3-c56b-480e-a09b-0e9df72f5bc0";
    public static final String PLAN_ID_REDIS_CODIS = "9ac370f7-9127-4334-b90a-fa6ceee73740";

    public static final HashSet<String> PLAN_ID_MYSQL = new HashSet<String>() {{
        add(PLAN_ID_MYSQL_MS);
        add(PLAN_ID_MYSQL_SINGLE);
        add(PLAN_ID_MYSQL_MM);
    }};

    public static final HashSet<String> PLAN_ID_REDIS = new HashSet<String>() {{
        add(PLAN_ID_REDIS_SINGLE);
        add(PLAN_ID_REDIS_MS);
        add(PLAN_ID_REDIS_MS_SENTINEL);
        add(PLAN_ID_REDIS_CODIS);
    }};


    //异步响应时，操作状态
    public static final String STATE_IN = "in progress";
    public static final String STATE_S = "succeeded";
    public static final String STATE_F = "failed";

    // Mysql 操作类型: 创建实例，更新实例，删除实例
    public static final String OPT_MYSQL_PROVISIONING = "provisioning";
    public static final String OPT_MYSQL_UPDATE = "update";
    public static final String OPT_MYSQL_DELETE = "delete";

    // Redis 操作类型: 创建实例，更新实例，删除实例
    public static final String OPT_REDIS_PROVISIONING = "provisioning";
    public static final String OPT_REDIS_UPDATE = "update";
    public static final String OPT_REDIS_DELETE = "delete";


}
