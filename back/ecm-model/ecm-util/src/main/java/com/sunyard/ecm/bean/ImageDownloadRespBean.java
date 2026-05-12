package com.sunyard.ecm.bean;

import com.sunyard.insurance.ecm.bean.ResponseBean;
import lombok.Data;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

@Data
public class ImageDownloadRespBean extends ResponseBean {
	
	private String image_zip_url;
	
	public String getImage_zip_url() {
		return image_zip_url;
	}

	public void setImage_zip_url(String image_zip_url) {
		this.image_zip_url = image_zip_url;
	}

	public String toXml() {
		Document document = DocumentHelper.createDocument();
		Element rootElement = document.addElement("root");
		rootElement.addElement("RESPONSE_CODE").setText(this.getRespCode());
		rootElement.addElement("IMAGE_ZIP_URL").setText(this.getImage_zip_url());
		rootElement.addElement("RESPONSE_MSG").setText(this.getRespMsg());
		return document.asXML();
	}
}
