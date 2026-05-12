package com.sunyard.ecm.oldToNew.socket.util;


import com.sunyard.ecm.dto.FileAndSortDTO;
import com.sunyard.ecm.dto.UploadFileDTO;
import com.sunyard.ecm.exception.OldToNewException;
import com.sunyard.ecm.oldToNew.dto.FileNameWithNodeDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.FileItem;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author yzy
 * @desc
 * @since 2025/2/24
 */
@Slf4j
public class ZipFileProcessorUtils {
    // 读取 XML 文件的内容
    public static String readXmlContent(File zipFile) throws IOException {
        String xmlContent = null;

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().endsWith(".xml")) {
                    // 读取 XML 内容
                    xmlContent = readContent(zipInputStream);
                    break; // 假设只有一个 XML 文件，找到后直接退出
                }
                zipInputStream.closeEntry();
            }
        }
        return xmlContent;
    }

    // 读取内容方法
    private static String readContent(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }
        return content.toString();
    }

    public static List<UploadFileDTO> getUploadFileDtoList(String xml, String zipPath) throws Exception {
        List<UploadFileDTO> uploadFileDTOS = new ArrayList<>();

        // 创建 DocumentBuilderFactory 和 DocumentBuilder
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder db = dbf.newDocumentBuilder();

        // 转换字符串为 InputStream
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes("UTF-8"));

        // 读取并解析 XML 文件
        Document document = db.parse(inputStream);

        // 获取根节点 BATCH
        NodeList batchList = document.getElementsByTagName("BATCH");

        // 用来收集所有的文件名和对应的 NODE ID
        List<FileNameWithNodeDTO> fileNamesWithNodes = new ArrayList<>();

        // 遍历每个 BATCH 节点，收集文件名和 NODE ID
        for (int i = 0; i < batchList.getLength(); i++) {
            Node batchNode = batchList.item(i);
            if (batchNode.getNodeType() == Node.ELEMENT_NODE) {
                Element batchElement = (Element) batchNode;

                // 获取 PAGES 节点
                NodeList pagesList = batchElement.getElementsByTagName("PAGES");
                for (int j = 0; j < pagesList.getLength(); j++) {
                    Node pagesNode = pagesList.item(j);
                    if (pagesNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element pagesElement = (Element) pagesNode;

                        // 获取每个 NODE 节点
                        NodeList nodeList = pagesElement.getElementsByTagName("NODE");
                        for (int k = 0; k < nodeList.getLength(); k++) {
                            Node nodeNode = nodeList.item(k);
                            if (nodeNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element nodeElement = (Element) nodeNode;
                                String nodeId = nodeElement.getAttribute("ID");
                                String action=nodeElement.getAttribute("ACTION");
                                if("ADD".equals(action)) {
                                    // 获取该 NODE 下的每个 PAGE 节点中的文件名
                                    NodeList pageList = nodeElement.getElementsByTagName("PAGE");
                                    for (int l = 0; l < pageList.getLength(); l++) {
                                        Node pageNode = pageList.item(l);
                                        if (pageNode.getNodeType() == Node.ELEMENT_NODE) {
                                            Element pageElement = (Element) pageNode;
                                            String fileName = pageElement.getAttribute("FILE_NAME");

                                            // 将文件名和对应的 NODE ID 存储到文件名与 NODE 的集合中
                                            fileNamesWithNodes.add(new FileNameWithNodeDTO(fileName, nodeId));
                                        }
                                    }
                                }else{
                                    throw new OldToNewException("存在不支持的ACTION:"+action);
                                }
                            }
                        }
                    }
                }
            }
        }

        // 一次性通过所有文件名获取 multipartFiles
        List<Map<String, MultipartFile>> multipartFiles = getFilesFromZip(zipPath, fileNamesWithNodes.stream().map(FileNameWithNodeDTO::getFileName).collect(Collectors.toList()));

        // 遍历收集的文件名和 NODE ID，根据 NODE 分组文件
        Map<String, List<FileAndSortDTO>> nodeGroupedFiles = new HashMap<>();

        for (FileNameWithNodeDTO fileNameWithNode : fileNamesWithNodes) {
            String fileName = fileNameWithNode.getFileName();
            String nodeId = fileNameWithNode.getNodeId();

            // 查找对应的 MultipartFile
            for (Map<String, MultipartFile> fileMap : multipartFiles) {
                if (fileMap.containsKey(fileName)) {
                    MultipartFile file = fileMap.get(fileName);

                    // 将文件添加到对应 NODE 的分组中
                    FileAndSortDTO fileAndSortDTO=new FileAndSortDTO();
                    fileAndSortDTO.setMultipartFile(file);
                    nodeGroupedFiles.computeIfAbsent(nodeId, k -> new ArrayList<>())
                            .add(fileAndSortDTO);
                    break;  // 找到对应的文件后跳出循环
                }
            }
        }

        // 构建 UploadFileDTO 对象并返回
        for (Map.Entry<String, List<FileAndSortDTO>> entry : nodeGroupedFiles.entrySet()) {
            String nodeId = entry.getKey();
            List<FileAndSortDTO> fileAndSortDTOS = entry.getValue();

            UploadFileDTO uploadFileDTO = new UploadFileDTO();
            uploadFileDTO.setDocNo(nodeId);  // 使用 nodeId 作为 DocNo
            uploadFileDTO.setFileAndSortDTOS(fileAndSortDTOS);  // 设置该节点下所有的文件
            uploadFileDTOS.add(uploadFileDTO);
        }

        return uploadFileDTOS;
    }


    public static List<Map<String,MultipartFile>> getFilesFromZip(String zipFilePath, List<String> fileNames) throws Exception {

        List<Map<String,MultipartFile>> multipartFiles = new ArrayList<>();

        // 创建文件输入流
        FileInputStream fis = new FileInputStream(zipFilePath);
        ZipInputStream zipIn = new ZipInputStream(fis);
        ZipEntry entry;

        // 遍历压缩包中的每个文件
        while ((entry = zipIn.getNextEntry()) != null) {
            String entryName = entry.getName();

            // 检查该文件是否在给定的文件名列表中
            if (fileNames.contains(entryName)) {
                // 如果匹配，读取文件内容并转换为 MultipartFile
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = zipIn.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, length);
                }

                // 获取文件字节数据
                byte[] fileData = byteArrayOutputStream.toByteArray();

                // 将文件字节数据转换为 MultipartFile
                MultipartFile multipartFile = convertToMultipartFile(fileData, entryName);
                Map<String,MultipartFile> map=new Hashtable();
                map.put(entryName,multipartFile);
                multipartFiles.add(map);
            }

            zipIn.closeEntry();
        }

        zipIn.close();

        return multipartFiles;
    }

    private static MultipartFile convertToMultipartFile(byte[] fileData, String fileName) {
        FileItem fileItem = new DiskFileItem("file", "application/octet-stream", false, fileName, fileData.length, new File("/tmp"));

        try (OutputStream outputStream = fileItem.getOutputStream()) {
            outputStream.write(fileData);
        } catch (IOException e) {
            log.error("转换为MultipartFile失败", e);
            throw new RuntimeException("转换为MultipartFile失败", e);
        }

        return new CommonsMultipartFile(fileItem);
    }

}
