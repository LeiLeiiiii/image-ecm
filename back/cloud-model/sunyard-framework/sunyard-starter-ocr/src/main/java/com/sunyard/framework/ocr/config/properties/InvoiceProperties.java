package com.sunyard.framework.ocr.config.properties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化信息
 * @author PJW
 */
@Configuration
public class InvoiceProperties {


    /**
     * 初始化发票类型代码:发票名称静态map
     *
     * @return Result
     */
    @Bean(name = "invoiceTypeCodeNameMap")
    @ConfigurationProperties(prefix = "invoice-type-code-name")
    public Map<String, String> getInvoiceTypeCodeNameMap() {
        Map<String, String> map = new HashMap<>(6);
        return map;
    }

    /**
     * 初始化可验真发票类型列表
     *
     * @return Result
     */
    @Bean(name = "canBeValidatedInvoiceTypeCodeSet")
    @ConfigurationProperties(prefix = "can-be-validated-invoice-type-code")
    public Set<String> getCanBeValidatedInvoiceTypeCodeSet() {
        Set<String> set = new HashSet<>();
        return set;
    }

    /**
     * 初始化可用于[校验公司信息]的发票类型代码列表
     *
     * @return Result
     */
    @Bean(name = "canBeValidatedCompanyInfoInvoiceTypeCodeSet")
    @ConfigurationProperties(prefix = "can-be-validated-company-info-invoice-type-code")
    public Set<String> getCanBeValidatedCompanyInfoInvoiceTypeCodeSet() {
        Set<String> set = new HashSet<>();
        return set;
    }

    /**
     * 初始化可用于[校验大小发票号]的发票类型代码列表
     *
     * @return Result
     */
    @Bean(name = "canBeConfirmedInvoiceTypeCodeSet")
    @ConfigurationProperties(prefix = "can-be-confirmed-invoice-type-code")
    public Set<String> getCanBeConfirmedInvoiceTypeCodeSet() {
        Set<String> set = new HashSet<>();
        return set;
    }


    /**
     * 初始化验真入参pretaxAmount取值时用到的 发票类型:OCR识别结果字段名称map
     *
     * @return Result
     */
    @Bean(name = "typeCodePretaxAmountKeyNameMap")
    @ConfigurationProperties(prefix = "type-code-pretax-amount-key-name")
    public Map<String, String> getTypeCodePretaxAmountKeyNameMap() {
        Map<String, String> map = new HashMap<>(6);
        return map;
    }

    /**
     * 初始化验真结果代码与结果描述map
     *
     * @return Result
     */
    @Bean(name = "validateResultCodeDesMap")
    @ConfigurationProperties(prefix = "validate-result-code-des")
    public Map<Integer, String> getValidateResultCodeDesMap() {
        Map<Integer, String> map = new HashMap<>(6);
        return map;
    }


    /**
     * 业务代码与业务名称映射Map
     *
     * @return Result
     */
    @Bean(name = "busiTypeCodeNameMap")
    @ConfigurationProperties(prefix = "busi-type-code-name-map")
    public Map<String, String> busiTypeCodeNameMap() {
        Map<String, String> map = new HashMap<>(6);
        return map;
    }
}
