package com.sunyard.framework.ocr.dto.ocr.glority;

import lombok.Data;

/**
 *
 * @author PJW*/
@Data
public class GlorityOCRResponseDTO {
	private Integer result;
	private GlorityOCRInnerResponseDTO response;

	public boolean isSuccess(){
		return new Integer("1").equals(result);
	}

}
