package com.bonc.broker.common;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author xingej
 */
public class Global {
	public static final String MYSQL = "mysql";
	public static final String REDIS = "redis";
	public static final String BCM_NGINX_URL = "/component/service?serviceName=";

	public static final String TRUE = "true";

	public static final String CREATE_WORKER="CreateWorker";
	public static final String UPDATE_WORKER="UpdateWorker";
	public static final String DELETE_WORKER="DeleteWorker";

	public static final String SERVICE_ID_MYSQL = "8780d398-a010-4696-bb0d-78ea6511fa95";
	public static final String PLAN_ID_MYSQL_MS = "aee3dab8-8cf9-4d3a-a2fd-f8155e56750e";
	public static final String PLAN_ID_MYSQL_SINGLE = "f0dfb49f-91cf-42af-871c-d06d8f7887dd";
	public static final String PLAN_ID_MYSQL_MM = "17f5165d-737e-4742-a6a2-f368f0e632d6";


	public static final String SERVICE_ID_REDIS = "764ad650-e6cf-4855-9ab5-51820afa3515";
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
	}};


	//异步响应时，操作状态
	public static final String STATE_IN = "in progress";
	public static final String STATE_S = "succeeded";
	public static final String STATE_F = "failed";

	// Mysql 操作类型: 创建实例，更新实例，删除实例
	public static final String OPT_MYSQL_PROVISIONING = "Create";
	public static final String OPT_MYSQL_UPDATE = "ChangeResource";
	public static final String OPT_MYSQL_DELETE = "Delete";

	// Redis 操作类型: 创建实例，更新实例，删除实例
	public static final String OPT_REDIS_PROVISIONING = "provisioning";
	public static final String OPT_REDIS_UPDATE = "update";
	public static final String OPT_REDIS_DELETE = "delete";

	public static final String KIND_REDIS = "Redis";
	public static final String KIND_CODIS = "CodisCluster";
	public static final String API_VERSION = "redis.bonc.com/v1beta1";

	/**
	 * 针对的是mysql的MS模式的副本数
	 */
	public static final HashSet<Integer> MYSQL_MS_REPLICAS = new HashSet<Integer>(16) {
		{
			// 副本数是2
			add(2);
			// 副本数是3
			add(3);
			add(4);
		}
	};

	/**
	 * mysql支持的版本
	 */
	public static final HashSet<String> MYSQL_VERSION = new HashSet<String>(16) {
		{
			// 版本5.6
			add("5.6");
			// 版本5.7
			add("5.7");
			add("8.0");
		}
	};

	/**
	 * redis支持的版本
	 */
	public static final HashSet<String> REDIS_VERSION = new HashSet<String>(16) {
		{
			// 版本3.2.11
			add("3.2.11");
		}
	};

	// redis 哨兵实例个数
	public static final HashSet<Integer> SENTINEL_NUM = new HashSet<Integer>(16) {
		{
			add(3);
			add(5);
		}
	};

	public static final HashSet<String> SERVICE_ID = new HashSet<String>(16) {
		{
			add(SERVICE_ID_MYSQL);
			add(SERVICE_ID_REDIS);
		}
	};

	public static final HashSet<String> PLAN_ID = new HashSet<String>(16) {
		{
			add(PLAN_ID_REDIS_SINGLE);
			add(PLAN_ID_REDIS_MS);

			add(PLAN_ID_MYSQL_MM);
			add(PLAN_ID_MYSQL_MS);
			add(PLAN_ID_MYSQL_SINGLE);
		}
	};

	/**
	 * planID 与 服务模式的对应关系
	 */
	public static final HashMap<String, String> PLAN_ID_SERVICE_MODE = new HashMap<String, String>() {
		{
			put(Global.PLAN_ID_MYSQL_MM, MysqlClusterConst.TYPE_MM);
			put(Global.PLAN_ID_MYSQL_MS, MysqlClusterConst.TYPE_MS);
			put(Global.PLAN_ID_MYSQL_SINGLE, MysqlClusterConst.TYPE_SINGLE);

			put(Global.PLAN_ID_REDIS_SINGLE, RedisClusterConst.REDIS_TYPE_SINGLE);
			put(Global.PLAN_ID_REDIS_MS, RedisClusterConst.REDIS_TYPE_MS);
		}
	};

	/**
	 *  serviceId 与 跟catalog的对应关系
	 */
	public static final HashMap<String, String> SERVICE_ID_CATALOG = new HashMap<String, String>() {
		{
			put(Global.SERVICE_ID_MYSQL, AppTypeConst.APPTYPE_MYSQL);
			put(Global.SERVICE_ID_REDIS, AppTypeConst.APPTYPE_REDIS);
		}
	};

}
