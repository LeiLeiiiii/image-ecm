package com.sunyard.framework.ocr.dto.invoicevalidation.glority;


import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 *
 * @author PJW*/
@Data
public class GlorityInvoiceValidationDataDTO {
	private String version;
	private Integer result;
	private Timestamp  timestamp;
	private String message;
    private String timeCost;
    private List<GlorityInvoiceValidationIdentifyResultsDTO> identifyResults;

}
