package com.sunyard.mytool.until;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunyard.mytool.constant.MigrateConstant;

import java.util.HashMap;
import java.util.List;


/**
 * EXIF信息转换工具类
 */
public class ExifConverterUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将file_exif字符串转换为目标格式的Map
     * 
     * @param fileExifString 数据库中存储的file_exif字段值，如：[{"ID":"Create SoftWare","NAME":"创建软件","VALUE":"www.meitu.com"},{"ID":"Camera model","NAME":"相机型号","VALUE":"500ps"}]
     * @return 转换后的Map，如：{"make"："www.meitu.com","model":"500ps"}
     */
    public static HashMap<String, String> convertExifStringToMap(String fileExifString) {
        if (fileExifString == null || fileExifString.trim().isEmpty()) {
            return new HashMap<>();
        }

        try {
            // 将JSON字符串解析为对象列表
            List<ExifItem> exifItems = objectMapper.readValue(fileExifString, new TypeReference<List<ExifItem>>() {});
            return convertExifItemsToMap(exifItems);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("解析fileExif字段失败: " + e.getMessage());
        }
    }

    /**
     * 将EXIF对象列表转换为目标格式的Map
     * 
     * @param exifItems EXIF对象列表
     * @return 转换后的Map
     */
    public static HashMap<String, String> convertExifItemsToMap(List<ExifItem> exifItems) {
        HashMap<String, String> resultMap = new HashMap<>();
        
        if (exifItems == null || exifItems.isEmpty()) {
            return resultMap;
        }

        for (ExifItem item : exifItems) {
            // 根据ID在EXIF_MAP中查找对应的值
            String mappedKey = MigrateConstant.EXIF_MAP.get(item.getID());
            if (mappedKey != null) {
                // 如果找到映射关系，则使用映射后的key和VALUE值放入结果Map
                resultMap.put(mappedKey, item.getVALUE());
            } else {
                // 如果没有找到映射关系，则使用原始的ID和VALUE值放入结果Map
                resultMap.put(item.getID(), item.getVALUE());
            }
        }

        return resultMap;
    }

    /**
     * EXIF条目内部类
     */
    public static class ExifItem {
        @JsonProperty("ID")
        private String ID;
        @JsonProperty("NAME")
        private String NAME;
        @JsonProperty("VALUE")
        private String VALUE;

        // Getters and Setters
        public String getID() {
            return ID;
        }

        public void setID(String ID) {
            this.ID = ID;
        }

        public String getNAME() {
            return NAME;
        }

        public void setNAME(String NAME) {
            this.NAME = NAME;
        }

        public String getVALUE() {
            return VALUE;
        }

        public void setVALUE(String VALUE) {
            this.VALUE = VALUE;
        }
    }
    
    /**
     * 示例用法：
     * String fileExif = "[{\"ID\":\"Create SoftWare\",\"NAME\":\"创建软件\",\"VALUE\":\"www.meitu.com\"},{\"ID\":\"Camera model\",\"NAME\":\"相机型号\",\"VALUE\":\"500ps\"}]";
     * Map<String, String> result = ExifConverterUtil.convertExifStringToMap(fileExif);
     * // 结果: {"make":"www.meitu.com","model":"500ps"}
     */
}