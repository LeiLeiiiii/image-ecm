package com.sunyard.framework.ocr.operator;

import com.sunyard.framework.ocr.dto.invoicevalidation.ValidateRequestParamDTO;
import com.sunyard.framework.ocr.dto.ocr.OCRRequestParamDTO;
import com.sunyard.framework.ocr.dto.ocr.OcrResultDTO;
import com.sunyard.framework.ocr.entity.InvoiceValidation;

import java.util.List;

/**
 * OCR识别、验真接口
 *
 * @author PJW
 */
public interface OcrOperator {
    /**
     * ocr并直接返回接口返回字符串
     *
     * @param imageUrl  图片url
     * @param imageData 图片数据
     * @param appKey    appKey
     * @param ocrHost   ocrHost
     * @param appSecret appSecret
     * @return Result
     */
    String ocrResultAsString(String imageUrl, String imageData, String appKey, String ocrHost, String appSecret);

    /**
     * ocr并以List<OcrResult>形式返回结果
     *
     * @param imageUrl  图片url
     * @param imageData 图片数据
     * @param appKey    appKey
     * @param ocrHost   ocrHost
     * @param appSecret appSecret
     * @return Result
     */
    List<OcrResultDTO> ocrResultAsDtoList(String imageUrl, String imageData, String appKey, String ocrHost,
                                          String appSecret);

    /**
     * 验真并直接返回接口返回的字符串
     *
     * @param param 参数
     * @return Result
     */
    String validate(ValidateRequestParamDTO param);

    /**
     * 解析校验结果并填充InvoiceValidation对应参数
     *
     * @param invoiceValidation 验证obj
     */
    void analyseAndFillInvoiceValidation(InvoiceValidation invoiceValidation);

    /**
     * 以验真参数构建查询用的md5字符串
     *
     * @param code         code
     * @param number       number
     * @param checkCode    checkCode
     * @param pretaxAmount pretaxAmount
     * @param date         date
     * @param type         type
     * @return Result
     */
    String md5FullString(String code, String number, String checkCode, String pretaxAmount, String date, String type);

    /**
     * 验真并以InvoiceValidation形式返回结果
     *
     * @param param 参数
     * @return Result
     */
    InvoiceValidation validateAndGetInvoiceValidation(ValidateRequestParamDTO param);

    /**
     * 根据ocr结果构建票据唯一识别码
     *
     * @param ocrResultDTO ocr对象
     * @return Result
     */
    String buildIdentifyCode(OcrResultDTO ocrResultDTO);

    /**
     * 判断此ocr结果是否有效
     *
     * @param ocrResultDTO ocr对象
     * @return Result
     */
    String checkOcrResult(OcrResultDTO ocrResultDTO);

    /**
     * 以ocr结果构建验真参数
     *
     * @param ocrResultDTO ocr对象
     * @return Result
     */
    ValidateRequestParamDTO buildValidateRequestParamByOcrResult(OcrResultDTO ocrResultDTO);

    /**
     * ocr并验真
     *
     * @param param 参数
     * @return Result
     */
    String ocrAndValidate(OCRRequestParamDTO param);
}
