package com.sunyard.mytool.until;

import com.sunyard.mytool.entity.KsXmlInfo;
import com.sunyard.mytool.entity.VersionInfo;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class XmlUtil {

	public static KsXmlInfo parseXmlFile(String xmlFilePath){
		File xmlFile = new File(xmlFilePath);
		if (!xmlFile.exists()) {
			log.error("xml描述文件不存在: {}", xmlFilePath);
			throw new RuntimeException("xml描述文件不存在");
		}
		KsXmlInfo info = null;
        try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 读取XML
			SAXReader reader = new SAXReader();
			// 读取XML文件，获取Document对象
			Document document = reader.read(xmlFile);
			// 获取根元素
			Element root = document.getRootElement();
			// 获取当前版本号
			Element versionElement = root.element("File").element("Version");
			String currentVersion = versionElement.getText();
			// 解析File节点信息
			Element fileElement = root.element("File");
			if (fileElement == null){
				log.error("xml描述文件缺少File节点: {}", xmlFilePath);
				throw new RuntimeException("xml描述文件缺少File节点");
			}
			info = new KsXmlInfo();
			handleFileInfo(fileElement, info);

			// 获取Versions元素
			Element versionsElement = root.element("Versions");
			List<VersionInfo> versionList = new ArrayList<>();

			// 遍历所有版本元素（V_1, V_2, V_3...）
			List<Element> versionElements = versionsElement.elements();
			if (versionElements == null || versionElements.isEmpty()) {
				log.error("xml描述文件缺少Versions节点: {}", xmlFilePath);
				throw new RuntimeException("xml描述文件缺少Versions节点");
			}
			for (Element versionElem : versionElements) {
				String versionTag = versionElem.getName(); // V_1, V_2, V_3等
				String versionNumber = versionTag.substring(2); // 提取版本号数字

				// 获取版本详细信息
				String createTime = versionElem.elementText("CreateTime").replace(',', '.');
				String createUser = versionElem.element("CreateUser").getText();
				String md5 = versionElem.element("MD5").getText();
				String modifyTime = createTime;
				String modifyUser = createUser;
				Element modifyTimeEl = versionElem.element("ModifyTime");
				if (modifyTimeEl != null){
					modifyTime = modifyTimeEl.getText().replace(',', '.');;
				}
				Element ModifyUserEl = versionElem.element("ModifyUser");
				if (ModifyUserEl != null){
					modifyUser = ModifyUserEl.getText();
				}
				// 判断是否为当前版本
				boolean isCurrentVersion = versionNumber.equals(currentVersion);
				// 创建版本信息对象
				VersionInfo versionInfo = new VersionInfo();
				versionInfo.setVersionTag(versionTag);
				versionInfo.setVersionNumber(versionNumber);
				versionInfo.setVersionCreateTime(sdf.parse(createTime));
				versionInfo.setVersionCreateUser(createUser);
				versionInfo.setMD5(md5);
				versionInfo.setVersionModifyTime(sdf.parse(modifyTime));
				versionInfo.setVersionModifyUser(modifyUser);
				versionInfo.setCurrent(isCurrentVersion);
				versionList.add(versionInfo);
			}
			info.setVersionInfos(versionList);
        } catch (Exception e) {
			log.error("解析xml描述文件发生异常: {}", e.getMessage());
            throw new RuntimeException("解析xml描述文件发生异常",e);
        }
		return info;
    }

	/**
	 * 解析File节点信息
	 */
	private static void handleFileInfo(Element fileElement, KsXmlInfo info){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            //xml中时间有,号  给其替换成.
            String fileCreateTime = fileElement.elementText("CreateTime").replace(',', '.');
            String fileModifyTime = fileCreateTime;
            Element filemodifyTimeEL = fileElement.element("ModifyTime");
            if (filemodifyTimeEL!= null){
                fileModifyTime = filemodifyTimeEL.getText().replace(',', '.');
            }
            info.setFileCreateTime(sdf.parse(fileCreateTime));
            info.setFileModifyTime(sdf.parse(fileModifyTime));

			String createUser = fileElement.elementText("CreateUser");
			String modifyUser = createUser;
			Element ModifyUserEl = fileElement.element("ModifyUser");
			if (ModifyUserEl != null){
				modifyUser = ModifyUserEl.getText();
			}
            info.setFileCreateUser(createUser);
            info.setFileModifyUser(modifyUser);
        } catch (Exception e) {
			log.error("xml描述文件File节点解析错误: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

	;


	private String getElementText(Element parent, String elementName) {
		Element element = parent.element(elementName);
		return element != null ? element.getText() : "";
	}


	public static Document readXmlDocByInStream(InputStream in)
			throws DocumentException, IOException {
		Document document = null;
		try {
			SAXReader  reader = new SAXReader();
			//不做xml的dtd解析验证
			reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			document = reader.read(in);
		} catch(Exception e){
			// 避免日志打印不清晰
			throw new DocumentException(e.getMessage());
		}finally {
			if (in != null) {
				in.close();
				in = null;
			}
		}
		return document;
	}

}
