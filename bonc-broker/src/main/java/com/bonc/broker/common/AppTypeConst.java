package com.bonc.broker.common;

import java.util.Arrays;
import java.util.List;

public class AppTypeConst {

    public static final String APPTYPE_MYSQL = "mysql";
    public static final String APPTYPE_STORM = "storm";
    public static final String APPTYPE_KAFKA = "kafka";
    public static final String APPTYPE_FTP = "ftp";
    public static final String APPTYPE_HDFSFTP = "hdfsftp";
    public static final String APPTYPE_XCLOUD = "xcloud";
    public static final String APPTYPE_REDIS = "redis";
    public static final String APPTYPE_HBASE = "hbase";
    public static final String APPTYPE_ZK = "zookeeper";
    public static final String APPTYPE_PROMETHEUS = "prometheus";
    public static final String APPTYPE_HADOOP = "hadoop";
    public static final String APPTYPE_HIVE = "hive";
    public static final String APPTYPE_SPARK = "spark";
    public static final String APPTYPE_YARN = "yarn";
    public static final String APPTYPE_HDFS = "hdfs";
    public static final String APPTYPE_HFTP = "hftp";
    public static final String APPTYPE_MEMCACHED = "memcached";
    public static final String APPTYPE_MPP = "mpp";
    public static final String APPTYPE_XCLOUDTENANT = "xcloudtenant";
    public static final String APPTYPE_XCLOUD_TENANT = "xcloudTenant";
    public static final String APPTYPE_ES = "es";
    public static final String APPTYPE_TERMINAL = "terminal";

    public final static String STATE_CLUSTER_RUNNING = "Running";
    public final static String STATE_CLUSTER_STOPPED = "Stopped";
    public final static String STATE_CLUSTER_WAITING = "Waiting";
    public final static String STATE_CLUSTER_FAILED = "Failed";
    public final static String STATE_CLUSTER_DELETED = "Deleted";
    public final static String STATE_CLUSTER_WARNING = "Warning";
    public final static String STATE_CLUSTER_PENDING = "Pending";
    public final static String STATE_CLUSTER_SCALING = "Scaling";
    public final static String STATE_CLUSTER_TERMINATING = "Terminating";
    public final static String STATE_CLUSTER_STARTING = "Starting";

    public final static String STATE_NODE_RUNNING = "Running";
    public final static String STATE_NODE_STOPPED = "Stopped";
    public final static String STATE_NODE_WAITING = "Waiting";
    public final static String STATE_NODE_FAILED = "Failed";
    public final static String STATE_NODE_DELETED = "Deleted";
    public final static String STATE_NODE_INITIATED = "Initiated";
    public final static String STATE_NODE_WARNING = "Warning";
    public final static String STATE_NODE_INIT = "Init";
    public final static String STATE_NODE_UNKNOWN = "Unknow";
    public final static String STATE_NODE_ONLINE = "Online";
    public final static String STATE_NODE_PENDING = "Pending";
    public final static String STATE_NODE_DELETEING = "Deleteing";
    public final static String STATE_NODE_STOPPING = "Stopping";

    public static final String OPT_CLUSTER_CREATE = "Create";
    public static final String OPT_CLUSTER_STOP = "Stop";
    public static final String OPT_CLUSTER_START = "Start";
    public static final String OPT_CLUSTER_EXPAND = "Expand";
    public static final String OPT_CLUSTER_DELETE = "Delete";
    public static final String OPT_CLUSTER_CHANGE_RESOURCE = "ChangeResource";
    public static final String OPT_NODE_STOP = "Stop";
    public static final String OPT_NODE_START = "Start";
    public static final String OPT_NODE_DELETE = "Delete";

    public static final String CHECK_CLUSTER_NAME = "^[a-z][a-z0-9]{4,14}[a-z0-9]$";
    public static final String CHECK_USER_PASSSWORD = "[a-zA-Z0-9]{6,16}";
    public static final String CHECK_CLUSTER_REPLICAS = "^[0-9]*[1-9][0-9]*$";

    public static final String CHECK_RESOURCE_CPU = "^[1-9]\\d*(\\.\\d+)?$";
    public static final String CHECK_RESOURCE_MEMORY = "^[1-9]\\d*(\\.\\d+)?$";
    public static final String CHECK_RESOURCE_CAPACITY = "^[1-9]\\d*(\\.\\d+)?$";

    public static final String CHECK_REDIS_SENTINEL_CPU = "^[0-9]\\d*(\\.\\d+)?$";
    public static final String CHECK_REDIS_SENTINEL_MEMORY = "[0-9]\\d*(\\.\\d+)?$";

    public static final String CPU = "cpu";
    public static final String MEMORY = "memory";

    public static final String UNIT_GI = "Gi";
    public static final String UNIT_MI = "Mi";

    public static final String ROLE_MASTER = "master";
    public static final String ROLE_SLAVE = "slave";

    public static final String NODESELECTOR_PERFORMANCE = "performance";

    public static final String EXPORTER = "exporter";

    public static final int SHOW_LEVEL_UNABLE = 0;
    public static final int SHOW_LEVEL_ORDINARY = 1;
    public static final int SHOW_LEVEL_SENIOR = 2;

    public static final int BCONSOLE_REFERENCED = 1;
    public static final int BCONSOLE_UNREFERENCED = 0;
    public static final String BCONSOLEREFERENCED = "bconsoleReferenced";

    public static final String LOG_OPERATION_CREATE = "创建";
    public static final String LOG_OPERATION_STOP = "停止";
    public static final String LOG_OPERATION_START = "启动";
    public static final String LOG_OPERATION_DELETE = "删除";
    public static final String LOG_OPERATION_ADD_NODE = "增加节点";
    public static final String LOG_OPERATION_CHANGE_RESOURCE = "修改资源";
    public static final String LOG_OPERATION_CHANGE_CONFIG = "修改配置";
    public static final String LOG_OPERATION_ADD_DEPENDENCY = "添加依赖";

    public static final String LOG_OPERATION_USER_CREATE = "创建用户";
    public static final String LOG_OPERATION_USER_UPDATE = "更新用户";
    public static final String LOG_OPERATION_USER_DELETE = "删除用户";

    private static final List<String> APPTYPES = Arrays.asList(APPTYPE_MYSQL, APPTYPE_STORM, APPTYPE_KAFKA, APPTYPE_FTP,
            APPTYPE_XCLOUD, APPTYPE_REDIS, APPTYPE_HBASE, APPTYPE_HDFSFTP, APPTYPE_ZK, APPTYPE_HADOOP, APPTYPE_HIVE,
            APPTYPE_SPARK, APPTYPE_YARN, APPTYPE_HDFS, APPTYPE_MEMCACHED, APPTYPE_ES, APPTYPE_HFTP);

    private static final String[] TYPE_CODES = { APPTYPE_MYSQL, APPTYPE_REDIS, APPTYPE_KAFKA, APPTYPE_STORM,
            APPTYPE_FTP, APPTYPE_XCLOUDTENANT };

    private AppTypeConst() {
        super();
    }

    public static List<String> getAPPTYPES() {
        return APPTYPES;
    }

    public static String[] getTypeCodes() {
        return TYPE_CODES;
    }
}
