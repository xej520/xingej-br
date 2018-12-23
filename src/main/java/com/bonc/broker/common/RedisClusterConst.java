/**
 * 
 */
package com.bonc.broker.common;

import java.util.Arrays;
import java.util.List;

/**
 * @author LYX
 *
 */
public class RedisClusterConst {

    public static final String KIND_REDIS = "Redis";
    public static final String KIND_CODIS = "CodisCluster";
    public static final String API_VERSION = "redis.bonc.com/v1beta1";

    public static final String REDIS_TYPE_MS = "ms";
    public static final String REDIS_TYPE_MS_SENTINEL = "ms-sentinel";
    public static final String REDIS_TYPE_CODIS = "codis";
    public static final String REDIS_TYPE_SINGLE = "single";

    public static final String REDIS_COMPONENT_DASHBOARD = "dashboard";
    public static final String REDIS_COMPONENT_SENTINEL = "sentinel";
    public static final String REDIS_COMPONENT_PROXY = "proxy";

    public final static String REDIS_CLUSTER_CREATE = "CreateWorker";
    public final static String REDIS_CLUSTER_DELETE = "DeleteWorker";
    public final static String REDIS_CLUSTER_UPDATE = "UpdateWorker";
    public final static String REDIS_CLUSTER_CHANGE_RESOURCE = "RedisClusterChangeResourceWorker";

    public final static String CODIS_CLUSTER_CREATE = "CodisClusterCreateWorker";
    public final static String CODIS_CLUSTER_DELETE = "CodisClusterDeleteWorker";
    public final static String CODIS_CLUSTER_UPDATE = "CodisClusterUpdateWorker";
    public final static String CODIS_CLUSTER_CHANGE_RESOURCE = "CodisClusterChangeResourceWorker";

    public static final String CHECK_CLUSTER_NAME = "^[a-z][a-z0-9-]{4,62}[a-z0-9]$";
    public static final String CHECK_USER_PASSSWORD = "[a-zA-Z0-9]{6,16}";
    public static final String CHECK_CLUSTER_REPLICAS = "^[0-9]*[1-9][0-9]*$";

    public static final String CHECK_RESOURCE_CPU = "^[1-9]\\d*(\\.\\d+)?$";
    public static final String CHECK_RESOURCE_MEMORY = "^[1-9]\\d*(\\.\\d+)?$";
    public static final String CHECK_RESOURCE_CAPACITY = "^[1-9]\\d*(\\.\\d+)?$";

    public static final String REDIS_CONF_NAME = "redis.conf";
    public static final String PROXY_CONF_NAME = "proxy.toml";
    public static final String DASHBOARD_CONF_NAME = "dashboard.toml";

    public static final String REDIS_ROLE_MASTER = "Master";
    public static final String REDIS_ROLE_SLAVE = "Slave";
    public static final String REDIS_ROLE_SERVER = "Server";

    public static final String REDIS_EXPORTER_LOGDIR = "/temp/component";

    public static final String CODIS_SENTINEL_DEFAULT_CPU = "0.5";
    public static final String CODIS_SENTINEL_DEFAULT_MEMORY = "128";

    public static final List<String> REDIS_TYPE_LIST = Arrays.asList(REDIS_TYPE_MS, REDIS_TYPE_MS_SENTINEL,
            REDIS_TYPE_CODIS, REDIS_TYPE_SINGLE);

    public static final int REDIS_CLUSTER_CREATE_TIMEOUT = 300000;

    public static List<String> getRedisTypeList() {
        return REDIS_TYPE_LIST;
    }

}
