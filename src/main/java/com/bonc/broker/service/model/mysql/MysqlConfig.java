package com.bonc.broker.service.model.mysql;

import com.bonc.broker.service.model.base.HealthCheck;

import java.util.Map;

public class MysqlConfig extends HealthCheck {

	private String password;
	private Map<String, String> mycnf;
	private String mysqldb;
	private String repluser;
	private String replpassword;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMysqldb() {
		return mysqldb;
	}

	public void setMysqldb(String mysqldb) {
		this.mysqldb = mysqldb;
	}

	public Map<String, String> getMycnf() {
		return mycnf;
	}

	public void setMycnf(Map<String, String> mycnf) {
		this.mycnf = mycnf;
	}

	public String getRepluser() {
		return repluser;
	}

	public void setRepluser(String repluser) {
		this.repluser = repluser;
	}

	public String getReplpassword() {
		return replpassword;
	}

	public void setReplpassword(String replpassword) {
		this.replpassword = replpassword;
	}

}
