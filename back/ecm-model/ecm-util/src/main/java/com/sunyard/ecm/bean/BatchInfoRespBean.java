package com.sunyard.ecm.bean;

import com.sunyard.insurance.ecm.bean.ResponseBean;
import lombok.Data;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;


@Data
public class BatchInfoRespBean extends ResponseBean {
	
	// 批次版本
	private String batchVer = null;
	
	public String getBatchVer() {
		return batchVer;
	}

	public void setBatchVer(String batchVer) {
		this.batchVer = batchVer;
	}
	
	public BatchInfoRespBean() {
		super();
	}

	public BatchInfoRespBean(String batchVer) {
		super();
		this.batchVer = batchVer;
	}

	public String toXml() {
		Document document = DocumentHelper.createDocument();
		Element rootElement = document.addElement("root");
		rootElement.addElement("RESPONSE_CODE").setText(this.getRespCode());
		rootElement.addElement("BATCH_VER").setText(this.getBatchVer());
		rootElement.addElement("RESPONSE_MSG").setText(this.getRespMsg());
		return document.asXML();
	}
}
