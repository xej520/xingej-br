package com.bonc.broker.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author xingej serviceinstance binding表
 */
@Entity
@Table(name = "`SERVICE_INSTANCE_BINDING`")
public class ServiceInstanceBinding implements Serializable {

	@Id
	@Column(name = "`ID`") //binding_id
	private String bindingId;

	@Column(name = "`INSTANCE_ID`")  // instance_id
	private String instanceId;

	@Column(name = "`CREATED_TIME`")  // 创建时间
	private Date createdTime;

	@Column(name = "`CREDENTIALS`", length = 3000)  // credentials
	private String credentials;

	public String getBindingId() {
		return bindingId;
	}

	public void setBindingId(String bindingId) {
		this.bindingId = bindingId;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public String getCredentials() {
		return credentials;
	}

	public void setCredentials(String credentials) {
		this.credentials = credentials;
	}

	@Transient
	public JSONObject getCredentialsObject() {
		return JSON.parseObject(this.credentials);
	}

	public void setCredentialsObject(JSONObject credentials) {
		this.credentials = JSONObject.toJSONString(credentials, SerializerFeature.WriteMapNullValue);
	}
}
