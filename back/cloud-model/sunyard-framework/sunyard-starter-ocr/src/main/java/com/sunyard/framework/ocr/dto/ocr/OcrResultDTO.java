package com.sunyard.framework.ocr.dto.ocr;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 *
 * @author PJW*/
@Data
public class OcrResultDTO {
    private String number;
    private String identifyCode;
    private String total;
    private String typeName;
    private String name;
    private String description;
    private String type;
    private String orientation;
    private String page;
    private List<Integer> region;
    private Map<String, Object> details;
    private Map<String, Object> extra;
    private Map<String, Object> regions;
}
