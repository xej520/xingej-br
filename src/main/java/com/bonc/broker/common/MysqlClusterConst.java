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
public class MysqlClusterConst {

    public static final String KIND = "MysqlCluster";
    public static final String API_VERSION = "mysql.bonc.com/v1beta1";
    public static final String MYSQL_VERSION_SEVEN = "5.7";

    public static final String TYPE_MS = "ms";
    public static final String TYPE_MM = "mm";
    public static final String TYPE_SINGLE = "single";

    public final static String MYSQL_CLUSTER_CREATE = "CreateWorker";
    public final static String MYSQL_CLUSTER_DELETE = "DeleteWorker";
    public final static String MYSQL_CLUSTER_UPDATE = "UpdateWorker";
    public final static String MYSQL_CLUSTER_CHANGE_RESOURCE = "ChangeResourceWorker";

    // 定义操作类型:创建实例，更新实例，删除实例
    public static final String MYSQL_CLUSTER_OPT_PROVISIONING = "provisioning";
    public static final String MYSQL_CLUSTER_OPT_UPDATE_INSTANCE = "ChangeResource";
    public static final String MYSQL_CLUSTER_OPT_DELETE_INSTANCE= "DELETE";

    public static final String MYSQL_API_RESPONSE_STATUS = "Success";

    public static final List<String> MYSQL_TYPE_LIST = Arrays.asList(TYPE_MS, TYPE_MM, TYPE_SINGLE);

    public static final int HEALTH_CHECK_LIVENESS_DELAY_TIMEOUT = 100;
    public static final int HEALTH_CHECK_LIVENESS_FAILURE_THRESHOLD = 100;
    public static final int HEALTH_CHECK_READINESS_DELAY_TIMEOUT = 100;
    public static final int HEALTH_CHECK_READINESS_FAILURE_THRESHOLD = 100;

    public static final String MYSQL_BACKUP_CONTAINER_DEFAULT_CPU = "1";
    public static final String MYSQL_BACKUP_CONTAINER_DEFAULT_MEMORY = "1";

    public static final int MYSQL_CLUSTER_CREATE_TIMEOUT = 300000;

    public static final String DEFAULT_NAMESPACE = "default";
    public static final int MYSQL_MS_DEFAULT_REPLICAS = 2;

    public static List<String> getMysqlTypeList() {
        return MYSQL_TYPE_LIST;
    }
}
