/**
 * 
 */
package com.sunyard.ecm.dto;

import com.sunyard.insurance.ecm.bean.SunBean;

import java.util.List;

/**
 * @author LJJ
 * 
 */
public class GrantAccessDTO extends SunBean {
	private static final long serialVersionUID = 1L;

	// 授权码
	private String accessKey;
	//授权系统ID
	private String accessId;
	// 接入系统名称
	private String accessName;
	// 该授权码对应接入系统referer
	private List<String> referer;
	// 接入系统对应ID
	private String serviceCodes;
	public GrantAccessDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public GrantAccessDTO(String accessKey, String accessId,
                          String accessName, List<String> referer, String serviceCodes) {
		super();
		this.accessKey = accessKey;
		this.accessId = accessId;
		this.accessName = accessName;
		this.referer = referer;
		this.serviceCodes = serviceCodes;
	}
	public String getAccessKey() {
		return accessKey;
	}
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}
	public String getAccessName() {
		return accessName;
	}
	public void setAccessName(String accessName) {
		this.accessName = accessName;
	}
	public List<String> getReferer() {
		return referer;
	}
	public void setReferer(List<String> referer) {
		this.referer = referer;
	}
	public String getServiceCodes() {
		return serviceCodes;
	}
	public void setServiceCodes(String serviceCodes) {
		this.serviceCodes = serviceCodes;
	}
	public String getAccessId() {
		return accessId;
	}
	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}
	
}
