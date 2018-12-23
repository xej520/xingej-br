package com.bonc.broker.controller.mode;

public class BindingRedisInsideK8s extends BaseBinding {

	private String svcname;

	public String getSvcname() {
		return svcname;
	}

	public void setSvcname(String svcname) {
		this.svcname = svcname;
	}

}
