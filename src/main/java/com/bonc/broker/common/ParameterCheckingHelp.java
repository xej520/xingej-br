package com.bonc.broker.common;

import com.bonc.broker.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * @author xingej
 */
@Component
public class ParameterCheckingHelp {

	/**
	 * 校验字符串是否是数字
	 */
	private static Pattern isIntegerPattern = Pattern.compile("^[-\\+]?[\\d]*$");
	private static Pattern isPasswordPattern = Pattern.compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$");
	private static Pattern isServiceNamePattern = Pattern.compile("^[a-z][a-z0-9]{4,14}[a-z0-9]$");


	public static Boolean checkInstanceId(String instanceId) {
		return !StringUtils.isBlank(instanceId);
	}

	public static Boolean checkServiceId(String serviceId) {
		return Global.SERVICE_ID_MYSQL.contains(serviceId) || Global.SERVICE_ID_REDIS.contains(serviceId);
	}

	public static Boolean checkPlanId(String planId) {
		return Global.PLAN_ID_MYSQL.contains(planId) || Global.PLAN_ID_REDIS.contains(planId);
	}

	public static Boolean checkCpuForMysql(float cpu) {
		return cpu >= 1 && cpu <= 16;
	}

	public static Boolean checkCpuForRedis(float cpu) {
		return cpu >= 1 && cpu <= 2;
	}


	public static Boolean checkMemoryForMysql(float memory) {
		return memory >= 1 && memory <= 1024;
	}

	public static Boolean checkMemoryForRedis(float memory) {
		return memory >= 1 && memory <= 64;
	}

	public static Boolean checkSentinelCpuForRedis(float cpu) {
		return cpu >= 0.5 && cpu <= 1;
	}

	public static Boolean checkSentinelMemoryForRedis(float memory) {
		return memory >= 128 && memory <= 256;
	}

	public static Boolean checkCapacityForMysql(float capacity) {
		return capacity >= 1 && capacity <= 2048;
	}

	public static Boolean checkCapacityForRedis(float capacity) {
		return capacity >= 1 && capacity <= 64;
	}

	public static Boolean checkVersionForMysql(String version) {
		return Global.MYSQL_VERSION.contains(version);
	}

	public static Boolean checkVersionForRedis(String version) {
		return Global.REDIS_VERSION.contains(version);
	}

	public static Boolean checkPassword(String password) {
		return isPasswordPattern.matcher(password).matches();
	}

	public static Boolean checkReplicasForMysqlMs(int replicas) {
		return Global.MYSQL_MS_REPLICAS.contains(replicas);
	}

	public static Boolean checkReplicasForRedisMs(int replicas) {
		if (replicas >= 2) {
			return true;
		}
		return false;
	}

	public static Boolean checkServiceName(String serviceName) {
		return isServiceNamePattern.matcher(serviceName).matches();
	}

	/**
	 * 校验字符串是否是数字
	 *
	 * @param str
	 * @return
	 */
	public boolean isInteger(String str) {
		return isIntegerPattern.matcher(str).matches();
	}

}
