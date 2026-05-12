package com.sunyard.module.storage.ecmbank.util;


import com.sunyard.client.SunEcmClientApi;
import com.sunyard.client.bean.ClientBatchBean;
import com.sunyard.client.bean.ClientBatchFileBean;
import com.sunyard.client.bean.ClientBatchIndexBean;
import com.sunyard.client.bean.ClientFileBean;
import com.sunyard.client.bean.ClientHeightQuery;
import com.sunyard.client.impl.SunEcmClientSocketApiImpl;
import com.sunyard.framework.common.util.FileUtils;
import com.sunyard.framework.common.util.conversion.XmlUtils;
import com.sunyard.framework.common.util.encryption.Md5Utils;
import com.sunyard.module.storage.constant.StateConstants;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author PJW
 */
@Slf4j
public class EcmUtils {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private String tableName = "ECM_LOG";

    private String ecmIp;
    private int ecmPort;
    private String username;
    private String cipher;

    private String groupName;
    /**
     * 分表字段
     */
    private String indexName;

    /**
     * 内容模型代码
     */
    private String modelCode;
    /**
     * 文档部件模型代码
     */
    private String filePartName;


    public EcmUtils(String ecmIp, int ecmPort, String username, String password) {
        this.ecmIp = ecmIp;
        this.ecmPort = ecmPort;
        this.username = username;
        this.cipher = password;
    }

    /**
     * set对象
     *
     * @param groupName    组名
     * @param modelCode    模块code
     * @param filePartName 文件分片名
     * @param indexName    索引明
     */

    public void set(String groupName, String modelCode, String filePartName, String indexName) {
        this.groupName = groupName;
        this.modelCode = modelCode;
        this.filePartName = filePartName;
        this.indexName = indexName;
    }

    /**
     * 登录
     *
     * @return Result
     * @throws Exception 异常
     */
    public boolean login() throws Exception {
        SunEcmClientApi clientApi = new SunEcmClientSocketApiImpl(ecmIp, ecmPort);
        String resultMsg = clientApi.login(username, cipher);
        logger.info("#######登陆返回的信息[" + resultMsg + "]#######", tableName);
        if (StateConstants.SUCCESS.equals(resultMsg)) {
            return true;
        }
        return false;
    }

    /**
     * 退出
     *
     * @throws Exception 异常
     */
    public void logout() throws Exception {
        SunEcmClientApi clientApi = new SunEcmClientSocketApiImpl(ecmIp, ecmPort);
        String resultMsg = clientApi.logout(username);
        logger.info("#######登出返回的信息[" + resultMsg + "]#######", tableName);
    }


    /**
     * 上传影像
     *
     * @param batchId    批量id
     * @param filePath   文件路径
     * @param indexValue 索引值
     * @return Result
     */
    public String upload(String batchId, String filePath, String indexValue) {
        String contentId = "";
        File imagefile = new File(filePath);
        FileUtils.deleteXml(imagefile);
        List<File> imagefiles = Arrays.asList(imagefile.listFiles());
        if (imagefile == null || imagefiles == null || imagefiles.size() == 0) {
            logger.info("路径下文件不存在");
            return "FAIL";
        }
        Collections.sort(imagefiles, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile()) {
                    return -1;
                }
                if (o1.isFile() && o2.isDirectory()) {
                    return 1;
                }
                return o1.getName().compareTo(o2.getName());
            }
        });
        ClientBatchBean clientBatchBean = new ClientBatchBean();
        clientBatchBean.setModelCode(modelCode);
        clientBatchBean.setUser(username);
        clientBatchBean.setPassWord(cipher);
        // 是否作为断点续传上传
        clientBatchBean.setBreakPoint(false);
        // 是否为批次下的文件添加MD5码
        clientBatchBean.setOwnMD5(false);
        // =========================设置索引对象信息开始=========================
        ClientBatchIndexBean clientBatchIndexBean = new ClientBatchIndexBean();
        clientBatchIndexBean.setAmount("");
        // 索引自定义属性
        clientBatchIndexBean.addCustomMap("BUSI_SERIAL_NO", batchId);
        clientBatchIndexBean.addCustomMap("CREATEDATE", indexValue);
        // =========================设置索引对象信息结束=========================
        // =========================设置文档部件信息开始=========================
        ClientBatchFileBean clientBatchFileBean = new ClientBatchFileBean();
        clientBatchFileBean.setFilePartName(filePartName);
        // =========================设置文档部件信息结束=========================
        //拼装xml
        Document sortdoc = DocumentHelper.createDocument();
        Element sortElem = sortdoc.addElement("root");
        Element sortnodeElem = sortElem.addElement("node");
        sortnodeElem.addAttribute("name", "BUSI_SERIAL_NO");
        // =========================添加文件=========================
        int n = 0;
        for (int i = 0; i < imagefiles.size(); i++) {
            if (imagefiles.get(i).isFile()) {
                File imageFile = imagefiles.get(i);
                String imagepath = imageFile.getPath();
                String fileName = imageFile.getName();
                logger.info(batchId + "<><><>文件部件添加:" + fileName);
                // 添加FileBean
                ClientFileBean fileBean = new ClientFileBean();
                fileBean.setFileName(imageFile.getPath());
                fileBean.setFileFormat(fileName.substring(fileName.lastIndexOf(".") + 1));
                fileBean.addOtherAtt("SHOWNAME", batchId + "-" + n);
                fileBean.addOtherAtt("FILEFORM", "BUSI_SERIAL_NO");
                fileBean.addOtherAtt("TRUENAME", fileName);
                fileBean.addOtherAtt("FILEATTR", "1");
                fileBean.addOtherAtt("FILEMD5", Md5Utils.getHash(imagepath, "MD5"));
                clientBatchFileBean.addFile(fileBean);
                Element sortitemElem = sortnodeElem.addElement("item");
                sortitemElem.addAttribute("filename", fileName);
                n++;
            } else if (imagefiles.get(i).isDirectory()) {
                Element sortnodeElemOther = sortElem.addElement("node");
                //文件名即为目录树名
                sortnodeElemOther.addAttribute("name", imagefiles.get(i).getName());
                File otherFiles = new File(String.valueOf(imagefiles.get(i)));
                List<File> otherImages = Arrays.asList(otherFiles.listFiles());
                if (otherFiles == null || otherImages == null || otherImages.size() == 0) {
                    logger.info("路径下文件不存在");
                    return "FAIL";
                }
                for (int j = 0; j < otherImages.size(); j++) {
                    File imageFileOther = otherImages.get(j);
                    String imagepathOther = imageFileOther.getPath();
                    String fileNameOther = imageFileOther.getName();
                    logger.info(batchId + "<><><>文件部件添加:" + fileNameOther);
                    // 添加FileBean
                    ClientFileBean fileBean = new ClientFileBean();
                    fileBean.setFileName(imageFileOther.getPath());
                    fileBean.setFileFormat(fileNameOther.substring(fileNameOther.lastIndexOf(".") + 1));
                    fileBean.addOtherAtt("SHOWNAME", batchId + "-" + n);
                    fileBean.addOtherAtt("FILEFORM", imagefiles.get(i).getName());
                    fileBean.addOtherAtt("TRUENAME", fileNameOther);
                    fileBean.addOtherAtt("FILEATTR", "1");
                    fileBean.addOtherAtt("FILEMD5", Md5Utils.getHash(imagepathOther, "MD5"));
                    clientBatchFileBean.addFile(fileBean);
                    Element sortitemElemOther = sortnodeElemOther.addElement("item");
                    sortitemElemOther.addAttribute("filename", fileNameOther);
                    n++;

                }
            }
        }
        // =======================添加排序文档============================
        // 生产排序报文
        String sortfileName = "sort_" + System.currentTimeMillis() + ".xml";
        XmlUtils.createXml(sortdoc, filePath + File.separator + sortfileName);
        ClientFileBean sortFileBean = new ClientFileBean();
        sortFileBean.setFileName(filePath + File.separator + sortfileName);
        sortFileBean.setFileFormat("xml");
        sortFileBean.addOtherAtt("FILEMD5", Md5Utils.getHash(filePath
                + File.separator + sortfileName, "MD5"));
        sortFileBean.addOtherAtt("FILEATTR", "0");
        sortFileBean.addOtherAtt("FILEFORM", "SORT_");
        sortFileBean.addOtherAtt("SHOWNAME", sortfileName);
        clientBatchFileBean.addFile(sortFileBean);
        // =========================添加文件=========================
        clientBatchBean.setIndex_Object(clientBatchIndexBean);
        clientBatchBean.addDocument_Object(clientBatchFileBean);
        SunEcmClientApi clientApi = new SunEcmClientSocketApiImpl(ecmIp, ecmPort);
        try {
            String resultMsg = clientApi.upload(clientBatchBean, groupName);
            logger.info("#######上传批次返回的信息[" + resultMsg + "]#######");
            if (resultMsg.contains(StateConstants.SUCCESS)) {
                logger.info("上传成功");
                contentId = resultMsg.replace("SUCCESS<<::>>", "");
            } else if (resultMsg.contains(StateConstants.FAIL)) {
                logger.info("上传失败");
                return "FAIL";
            }
        } catch (Exception e) {
            log.error("系统异常",e);
            return "FAIL";
        }
        return contentId;
    }

    /**
     * 删除
     *
     * @param contentId  内容id
     * @param indexValue 索引值
     * @return Result
     */
    public String delete(String contentId, String indexValue) {
        ClientBatchBean clientBatchBean = new ClientBatchBean();
        // 必要信息,设置内容对象英文名
        clientBatchBean.setModelCode(modelCode);
        // 必要信息,设置用户名
        clientBatchBean.setUser(username);

        clientBatchBean.setPassWord(cipher);
        // 必要信息,设置内容ID
        clientBatchBean.getIndex_Object().setContentID(contentId);
        // 若分表则为必要信息,设置8位日期字符串的自定义属性(配置内容模型时的开始时间字段)
        clientBatchBean.getIndex_Object().addCustomMap(indexName, indexValue);
        String resultMsg = "";
        try {
            SunEcmClientApi clientApi = new SunEcmClientSocketApiImpl(ecmIp, ecmPort);
            resultMsg = clientApi.delete(clientBatchBean, groupName);
            logger.info("#######删除批次返回的信息[" + resultMsg + "]#######");
        } catch (Exception e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        }
        return resultMsg;
    }

    /**
     * 批量查询
     *
     * @param contentId  内容id
     * @param indexValue 索引值
     * @return Result
     */
    public String queryBatch(String contentId, String indexValue) {
        String resultMsg = null;
        ClientBatchBean clientBatchBean = new ClientBatchBean();
        clientBatchBean.setModelCode(modelCode);
        clientBatchBean.setUser(username);
        clientBatchBean.setPassWord(cipher);
        clientBatchBean.setDownLoad(false);
        clientBatchBean.getIndex_Object().setVersion("0");
        clientBatchBean.getIndex_Object().setContentID(contentId);
        clientBatchBean.getIndex_Object().addCustomMap(indexName, indexValue);

        try {
            SunEcmClientApi clientApi = new SunEcmClientSocketApiImpl(ecmIp, ecmPort);
            resultMsg = clientApi.queryBatch(clientBatchBean, groupName);
            logger.info("#######查询批次返回的信息[" + resultMsg + "]#######");
        } catch (Exception e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        }
        if (resultMsg != null) {
            resultMsg = resultMsg.replace("0001<<::>>", "");
        }
        return resultMsg;
    }

    /**
     * 高级查询（后督）
     *
     * @param id        id
     * @param batchId   批量id
     * @param inputDate 日期
     * @return Result
     */
    public String heightQuery(String id, String batchId, String inputDate) {
        String resultMsg = "FAIL";
        ClientHeightQuery heightQuery = new ClientHeightQuery();
        heightQuery.setUserName(username);
        heightQuery.setPassWord(cipher);
        heightQuery.setLimit(10);
        heightQuery.setPage(1);
        heightQuery.setModelCode(modelCode);
        heightQuery.addCustomAtt("CREATEDATE", inputDate);
        heightQuery.addCustomAtt("BUSI_SERIAL_NO", batchId);

        SunEcmClientApi clientApi = new SunEcmClientSocketApiImpl(ecmIp, ecmPort);
        try {
            resultMsg = clientApi.heightQuery(heightQuery, groupName);
            logger.info("#######调用高级搜索返回的信息[" + resultMsg + "]#######");
        } catch (Exception e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        }
        resultMsg = resultMsg.replace("0001<<::>>", "");
        return resultMsg;
    }

    /**
     * 解析高级查询xml
     *
     * @param xml xml
     * @return Result
     * @throws DocumentException 异常
     */
    public String parseHeightQueryXml(String xml) throws DocumentException {
        String contentId = "";
        Document document = XmlUtils.stringConvertDoc(xml);
        logger.info(xml);
        Element root = document.getRootElement();
        Element heightQuery = root.element("HeightQuery");
        Element indexBeans = heightQuery.element("indexBeans");
        if (indexBeans == null) {
            return contentId;
        }
        Element batchIndexBean = indexBeans.element("BatchIndexBean");

        contentId = batchIndexBean.attributeValue("CONTENT_ID");
        return contentId;
    }


}
