package com.bonc.broker.controller.mode;

/**
 * @author xingej
 */
public class BindingMysqlInsideK8S extends BaseBinding {
	private String svcname;
	private String username;

	public String getSvcname() {
		return svcname;
	}

	public void setSvcname(String svcname) {
		this.svcname = svcname;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
