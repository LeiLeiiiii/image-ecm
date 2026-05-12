package com.sunyard.ecm.dto.split;

import java.io.Serializable;
import java.util.Date;

/**
 * @author: lw
 * @Date: 2024/1/28
 * @Description: 文件分片DTO
 */
public class PartSummaryDTO implements Serializable {
    private int partNumber;
    private Date lastModified;
    private String eTag;
    private long size;

    public PartSummaryDTO() {
    }

    public int getPartNumber() {
        return this.partNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    public Date getLastModified() {
        return this.lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getETag() {
        return this.eTag;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
