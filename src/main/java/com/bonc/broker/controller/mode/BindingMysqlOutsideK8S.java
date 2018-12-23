package com.bonc.broker.controller.mode;

/**
 * @author xingej
 */
public class BindingMysqlOutsideK8S extends BaseBinding {
	private String host;
	private String username;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
