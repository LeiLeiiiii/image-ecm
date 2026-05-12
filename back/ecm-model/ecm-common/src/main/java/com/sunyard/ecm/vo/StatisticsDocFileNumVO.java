package com.sunyard.ecm.vo;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StatisticsDocFileNumVO implements Serializable {

    private static final long serialVersionUID = 1L;


    @JacksonXmlProperty(localName = "SUM_IMGS")
    private Integer count;

    @JacksonXmlElementWrapper(localName = "NODES")
    @JacksonXmlProperty(localName = "NODE")
    private List<DocFileNumVO> docFileNumList;

}
