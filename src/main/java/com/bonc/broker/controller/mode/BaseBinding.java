package com.bonc.broker.controller.mode;

/**
 * @author xingej
 */
public class BaseBinding {
	private String password;
	private String port;
	private String role;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
