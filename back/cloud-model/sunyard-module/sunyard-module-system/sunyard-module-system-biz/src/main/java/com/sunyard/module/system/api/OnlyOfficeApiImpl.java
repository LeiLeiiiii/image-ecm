package com.sunyard.module.system.api;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Resource;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.onlyoffice.FwFileHandler;
import com.sunyard.framework.onlyoffice.FwFileHandlerConfigFactory;
import com.sunyard.framework.onlyoffice.constant.StateConstants;
import com.sunyard.framework.onlyoffice.core.Cache;
import com.sunyard.framework.onlyoffice.dto.FwAddressUrlConfig;
import com.sunyard.framework.onlyoffice.dto.FwFileConfig;
import com.sunyard.framework.onlyoffice.dto.FwFileMetaData;
import com.sunyard.framework.onlyoffice.dto.convert.FwConvertBody;
import com.sunyard.framework.onlyoffice.dto.edit.FwFileUser;
import com.sunyard.framework.onlyoffice.enums.ConvertErrorCode;
import com.sunyard.framework.onlyoffice.enums.ErrorCode;
import com.sunyard.framework.onlyoffice.impl.FwFileContextImpl;
import com.sunyard.framework.onlyoffice.tools.DocumentKey;
import com.sunyard.framework.onlyoffice.tools.JWTUtil;
import com.sunyard.module.system.api.dto.FileMetaDTO;

import lombok.extern.slf4j.Slf4j;


/**
 * only office 实现类
 * @author PJW
 */

@Slf4j
@RestController
public class OnlyOfficeApiImpl implements OnlyOfficeApi {

    @Resource
    private FwFileHandlerConfigFactory fileHandlerConfigFactory;
    @Resource
    private FwAddressUrlConfig addressUrlConfig;
    @Resource
    private FwFileContextImpl tempFileContext;
    @Resource
    private FwFileHandler fileHandler;
    @Resource
    private Cache cache;
    @Resource
    private FwConvertBody convertBody;

    private static final String SPLIT = "/";
    private static final String UNDER_SPLIT = "_";

    /**
     * @param fileId               文件唯一id
     * @param fileName             文件名称
     * @param fileType             文件后缀名
     * @param fileSize             文件大小
     * @param fileUrl              文件下载地址
     * @param userId               打开文件用户id
     * @param userName             打开文件用户名
     * @param mode                 edit/view 编辑模式/查看模式
     * @param collaborativeEditing collaborativeEditing
     * @return Result
     */
    @Override
    public Result<Map> openDocument(String fileId,
                                    String fileName,
                                    String fileType,
                                    String fileSize,
                                    String fileUrl,
                                    String userId,
                                    String userName,
                                    String mode,
                                    boolean collaborativeEditing) {
        Map<String, Object> map = new HashMap<>(6);
        map.put("fileId", fileId);
        map.put("fileName", fileName);
        map.put("fileType", fileType);
        map.put("fileSize", fileSize);
        map.put("fileUrl", fileUrl);
        map.put("userId", userId);
        map.put("userName", userName);
        if (Long.parseLong(fileSize) > addressUrlConfig.getMaxSize()) {
            // 添加操作日志
            throw new RuntimeException("文件超过【" + addressUrlConfig.getMaxSize() / 1024 / 1024 + "MB】无法打开");
        }
        if (StateConstants.EDIT.equals(mode)) {
            return Result.success(documentEdit(map, mode, collaborativeEditing));
        }
        if (StateConstants.VIEW.equals(mode)) {
            return Result.success(documentView(map));
        }
        return null;
    }

    private Map documentEdit(Map<String, Object> map, String edit, boolean collaborativeEditing) {
        FwFileConfig fileConfigDTO = openEditConfig(map, edit, collaborativeEditing);
        String json = JSON.toJSONString(fileConfigDTO);
        Map<String, Object> config = JSON.parseObject(json, Map.class);
        return config;
    }


    private Map documentView(Map<String, Object> map) {
        FwFileConfig fileConfigDTO = openEditConfig(map, StateConstants.VIEW, false);
        String json = JSON.toJSONString(fileConfigDTO);
        Map<String, Object> config = JSON.parseObject(json, Map.class);
        return config;
    }

    private FwFileConfig openEditConfig(Map<String, Object> map, String mode, boolean collaborativeEditing) {
        map.put("mode", mode);
        log.info("开始生成文件信息");
        //在编辑模式 生成临时文件，保存原文件信息
        FwFileMetaData tempFileInfo = null;
        try {
            tempFileInfo = fileHandler.handlerFile(map, collaborativeEditing);
        } catch (Exception e) {
            log.error("系统异常", e);
            throw new RuntimeException(e);
        }
        //生成配置文件
        log.info("开始生成编辑器配置信息");
        FwFileUser user = new FwFileUser();
        user.setId(map.get("userId").toString());
        user.setName(map.get("userName").toString());
        FwFileConfig fileConfigDTO = fileHandlerConfigFactory.buildInitConfig(user, tempFileInfo.getUrl(), mode, tempFileInfo.getKey(), tempFileInfo.getOldName());
        log.info("生成编辑器配置信息结束");
        return fileConfigDTO;
    }


    /**
     * 获取规定历史文件数量
     *
     * @return Result
     */
    @Override
    public Result<Integer> getHistNum() {
        return Result.success(addressUrlConfig.getHistNum());
    }


    /**
     * 保存文件地址
     *
     * @return Result
     */
    @Override
    public Result<String> getCommandServiceUrl() {
        return Result.success(addressUrlConfig.getDocService() + StateConstants.SAVE);
    }

    @Override
    public Result<Integer> getTimeout() {
        return Result.success(addressUrlConfig.getTimeout());
    }

    @Override
    public Result<String> downloadFileRequestPath() {
        if (addressUrlConfig.getDownloadFile().endsWith(SPLIT)) {
            return Result.success(addressUrlConfig.getDownloadFile());
        }
        return Result.success(addressUrlConfig.getDownloadFile() + SPLIT);
    }


    @Override
    public Result<String> handlerStatus(String jsonStr) {
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        log.info("开始下载编辑器文件");
        int status = jsonObject.getIntValue("status");
        log.info("status[{}]:{}", status, jsonObject);
        String key = (String) jsonObject.get("key");
        FwFileHandler tempFileHandler = tempFileContext.getHandlerByKey(key);
        String url = jsonObject.getString("url");
        String changesurl = jsonObject.getString("changesurl");
        log.info("编辑后的文档下载路径url:" + url);
        log.info("文件变动信息文件url:" + changesurl);
        if (Objects.nonNull(tempFileHandler)) {
            Optional<FwFileMetaData> tempFile = tempFileHandler.getTempFile(key);
            if (!tempFile.isPresent()) {
                throw new RuntimeException("文件元信息不存在");
            }
        }
        return Result.success(url);
    }

    @Override
    public void removeTempFile(String key) {
        FwFileHandler tempFileHandler = tempFileContext.getHandlerByKey(key);
        if (tempFileHandler != null) {
            log.info("删除临时文件信息缓存" + key);
            tempFileHandler.removeTempFile(key);
        }
    }

    /**
     * 临时文件信息
     *
     * @param key
     * @return Result
     */
    @Override
    public Result<FileMetaDTO> getTempFile(String key) {
        FwFileMetaData tempFileInfo = tempFileContext.getFileInfo(key);
        FileMetaDTO fileMetaDTO = new FileMetaDTO();
        BeanUtils.copyProperties(tempFileInfo, fileMetaDTO);
        if (!ObjectUtils.isEmpty(tempFileInfo)) {
            return Result.success(fileMetaDTO);
        }
        return null;
    }

    /**
     * 获取打开文档时的唯一key
     *
     * @param id 文件id
     * @return Result
     */
    @Override
    public Result<String> getKey(String id, Long userId) {
        String userIdStr = userId.toString();
        if (cache.hasKey(userIdStr + UNDER_SPLIT + id)) {
            return Result.success((String) cache.get(userIdStr + "_" + id));
        }
        return Result.success((String) cache.get("collaborativeEditing_" + id));
    }

    @Override
    public void close(String key) {

        Result keyResult = iskey(key, null);
        int i = (int) keyResult.getData();
        if (i <= 0) {
            removeTempFile(key);
            String id = (String) cache.get("getID_" + key);
            cache.remove("getID_" + id);
            cache.remove("collaborativeEditing_" + id);
            cache.remove(id);
        }
    }

    /**
     * redis中存放文件使用人数
     *
     * @param key
     * @param users
     */
    @Override
    public Result iskey(String key, Integer users) {
        int i = 0;
        if (users != null) {
            cache.set(key, users, 60 * 60 * 6);
            i = users;
        } else {
            i = ((int) cache.get(key)) - 1;
            cache.set(key, i, 60 * 60 * 6);
        }
        log.info("[" + key + "]文档使用人数：" + i);
        return Result.success(i);
    }

    @Override
    public Result getUserNum(String key) {
        return Result.success((int) (cache.get(key)));
    }

    @Override
    public Result<String> getFileId(String key) {
        return Result.success((String) cache.get("getID_" + key));
    }

    /**
     * https://documentserver/coauthoring/CommandService.ashx
     * {
     * "c": "forcesave",
     * "key": "Khirz6zTPdfd7",
     * "userdata": "sample userdata"
     * }
     */
    @Override
    public Result<String> save(String key, String userId) {
        Map<String, Object> map = new HashMap<>(6);
        map.put("c", "forcesave");
        map.put("key", key);
        map.put("userdata", userId);
        if (!ObjectUtils.isEmpty(addressUrlConfig.getSecret())) {
            String token = JWTUtil.createToken(map, addressUrlConfig.getSecret());
            map.put("token", token);
        }
        String bodyString = JSON.toJSONString(map);
        log.info("forcesave:" + bodyString);
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(addressUrlConfig.getDocService() + StateConstants.SAVE);
        // 设置请求头
        httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
        httpPost.setHeader("Accept", "application/json");
        // 设置请求体
        StringEntity stringEntity = new StringEntity(bodyString, StandardCharsets.UTF_8);
        httpPost.setEntity(stringEntity);
        // 发送请求并获取响应
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            log.error("系统异常", e);
            throw new RuntimeException(e);
        }
        // 处理响应
        if (response.getEntity() != null) {
            String jsonString = null;
            try {
                jsonString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("系统异常", e);
                throw new RuntimeException(e);
            }
            JSONObject jsonObj = JSONObject.parseObject(jsonString);
            log.debug("http-response:" + jsonObj.toJSONString());
            Object error = jsonObj.get("error");
            String msg = "";
            if (!ObjectUtils.isEmpty(error)) {
                ErrorCode byCode = ErrorCode.getByCode((Integer) error);
                if (ObjectUtils.isEmpty(byCode)) {
                    msg = "保存成功";
                } else {
                    msg = byCode.getMessage();
                }
            }
            return Result.success(msg);
        } else {
            log.warn("onlyoffice 文件保存请求失败!");
        }

        return Result.success("");
    }
    /**
     * 文件转化 #简单请求参数示例:
     * @param filetype   文件类型
     * @param key        文件key
     * @param outputtype 输出类型
     * @param fileUrl    文件路径
     * @param title      输出文件名称
     * @param password   加密文档的密码
     * @return Result 文件路径
     */
    /**
     * //#请求参数示例 https://www.songbin.top/post_27457.html
     * "title": "Example Document Title.pdf",
     * "url": "https://example.com/url-to-example-spreadsheet.xlsx"
     * }
     */
    @Override
    public Result<String> converted(String filetype, String downloadUrl, String outputtype, String title, String password) {
        log.debug("文件开始转化{}->{}", filetype, outputtype);
        FwConvertBody body = getConvertBody(filetype, DocumentKey.snowflakeId(), outputtype, downloadUrl, title, password);
        String bodyString = JSON.toJSONString(body);
        if (!ObjectUtils.isEmpty(addressUrlConfig.getSecret())) {
            String token = JWTUtil.createToken(JSON.parseObject(bodyString, Map.class), addressUrlConfig.getSecret());
            body.setToken(token);
            bodyString = JSON.toJSONString(body);
        }
        log.debug(bodyString);
        String jsonString = null;
        log.info("forcesave:" + bodyString);
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(addressUrlConfig.getDocService() + StateConstants.SAVE);
            // 设置请求头
            httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
            httpPost.setHeader("Accept", "application/json");
            // 设置请求体
            StringEntity stringEntity = new StringEntity(bodyString, StandardCharsets.UTF_8);
            httpPost.setEntity(stringEntity);
            // 发送请求并获取响应
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            log.error("系统异常", e);
            throw new RuntimeException(e);
        }
        // 处理响应
            if (response.getEntity() != null) {
                try {
                    jsonString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    log.error("系统异常", e);
                    throw new RuntimeException(e);
                }
            } else {
                log.warn("onlyoffice 文件转换请求失败!");
            }

        /**
         * {
         *     "endConvert":true，//转换是否完 成
         *     "fileUrl"：“ https：//documentserver/ResourceService.ashx?filename=output.doc”，//转换后的文件地址
         *     "percent"：100//转换完成百分比 仅参数设置为异步时
         *  }
         */
        JSONObject jsonObj = JSONObject.parseObject(jsonString);
        log.debug("http-response:" + jsonObj.toJSONString());
        Object error = jsonObj.get("error");
        if (error != null) {
            try {
                processConvertServiceResponceError((Integer) error);
            } catch (Exception e) {
                log.error("系统异常", e);
                throw new RuntimeException(e);
            }
        }
        /**检查转换是否完成，并将结果保存到一个变量中*/
        Boolean isEndConvert = (Boolean) jsonObj.get("endConvert");
        Long resultPercent = 0L;
        String responseUri = null;
        if (isEndConvert) {
            resultPercent = 100L;
            responseUri = (String) jsonObj.get("fileUrl");
            log.debug("文件转化完成{}->{}", filetype, outputtype);
        } else {
            resultPercent = (Long) jsonObj.get("percent");
            resultPercent = resultPercent >= 100L ? 99L : resultPercent;
        }
        return Result.success(resultPercent >= 100L ? responseUri : "");
    }

    public FwConvertBody getConvertBody(String filetype, String key, String outputtype, String url, String title, String password) {
        FwConvertBody newConvertBody = new FwConvertBody();
        BeanUtils.copyProperties(newConvertBody, convertBody);
        newConvertBody.setFiletype(filetype);
        if (StringUtils.hasText(password)) {
            newConvertBody.setPassword(password);
        }
        newConvertBody.setKey(key);
        newConvertBody.setOutputtype(outputtype);
        newConvertBody.setUrl(url);
        newConvertBody.setTitle(title);
        return newConvertBody;
    }

    /**
     * 错误代码	描述
     * -1	未知错误。
     * -2	转换超时错误。
     * -3	转换错误。
     * -4	下载要转换的文档文件时出错。
     * -5	密码不正确。
     * -6	访问转换结果数据库时出错。
     * -7	输入错误。
     * -8	令牌无效。
     */
    private void processConvertServiceResponceError(int errorCode) throws Exception {
        String errorMessage = "";
        String errorMessageTemplate = "Error occurred in the ConvertService: ";
        ConvertErrorCode byCode = ConvertErrorCode.getByCode(errorCode);
        if (!ObjectUtils.isEmpty(byCode)) {
            errorMessage = errorMessageTemplate + ConvertErrorCode.getByCode(errorCode).getMessage();
        } else {
            errorMessage = "ErrorCode = " + errorCode;
        }
        throw new Exception(errorMessage);
    }
}


