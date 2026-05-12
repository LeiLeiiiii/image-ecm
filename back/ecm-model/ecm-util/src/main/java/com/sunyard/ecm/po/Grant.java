/**
 * 
 */
package com.sunyard.ecm.po;

import com.sunyard.insurance.ecm.bean.SunBean;

import java.sql.Blob;

/**
 * @author LJJ
 * 
 */
public class Grant extends SunBean {
	
	private String Id;
	private Blob grantService;
	private Blob grantAccess;

	public Grant() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Grant(String grantId, Blob grantService, Blob grantAccess) {
		super();
		this.Id = grantId;
		this.grantService = grantService;
		this.grantAccess = grantAccess;
	}

	public String getId() {
		return Id;
	}

	public void setId(String grantId) {
		this.Id = grantId;
	}

	public Blob getGrantService() {
		return grantService;
	}

	public void setGrantService(Blob grantService) {
		this.grantService = grantService;
	}

	public Blob getGrantAccess() {
		return grantAccess;
	}

	public void setGrantAccess(Blob grantAccess) {
		this.grantAccess = grantAccess;
	}

}
