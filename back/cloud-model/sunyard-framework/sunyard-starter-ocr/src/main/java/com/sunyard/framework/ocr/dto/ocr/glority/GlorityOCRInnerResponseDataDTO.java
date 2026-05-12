package com.sunyard.framework.ocr.dto.ocr.glority;


import com.sunyard.framework.ocr.dto.ocr.OcrResultDTO;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 *
 * @author PJW*/
@Data
public class GlorityOCRInnerResponseDataDTO {
	private String version;
	private Integer result;
	private Date timestamp;
	private String message;
	/**
	 * 识别结果标识id,用于结果反馈
	 */
	private String id;
	/**
	 *  识别图片唯一标识
	 */
	private String sha1;
	/**
	 * 识别花费的时长，单位毫秒
	 */
    private Date timeCost;
    private List<OcrResultDTO> identifyResults;
}
