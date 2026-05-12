/**
 * 
 */
package com.sunyard.ecm.dto;

import com.sunyard.insurance.ecm.bean.SunBean;

/**
 * @author LJJ
 * 
 */
public class GrantServiceDTO extends SunBean {
	private static final long serialVersionUID = 1L;

	private String companyName;
	private String macAddress;
	private String ipAddress;
	private String startDateStr;
	private String endDateStr;
	private String serviceCodes;
	private String serviceConfig;//services-config配置文件

	public GrantServiceDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public GrantServiceDTO(String companyName, String macAddress,
                           String ipAddress, String startDateStr, String endDateStr,
                           String serviceCodes, String serviceConfig) {
		super();
		this.companyName = companyName;
		this.macAddress = macAddress;
		this.ipAddress = ipAddress;
		this.startDateStr = startDateStr;
		this.endDateStr = endDateStr;
		this.serviceCodes = serviceCodes;
		this.serviceConfig = serviceConfig;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getStartDateStr() {
		return startDateStr;
	}

	public void setStartDateStr(String startDateStr) {
		this.startDateStr = startDateStr;
	}

	public String getEndDateStr() {
		return endDateStr;
	}

	public void setEndDateStr(String endDateStr) {
		this.endDateStr = endDateStr;
	}

	public String getServiceCodes() {
		return serviceCodes;
	}

	public void setServiceCodes(String serviceCodes) {
		this.serviceCodes = serviceCodes;
	}

	public String getServiceConfig() {
		return serviceConfig;
	}

	public void setServiceConfig(String serviceConfig) {
		this.serviceConfig = serviceConfig;
	}

}
