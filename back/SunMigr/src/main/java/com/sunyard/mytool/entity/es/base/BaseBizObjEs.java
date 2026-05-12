
package com.sunyard.mytool.entity.es.base;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class BaseBizObjEs {
    private String baseBizSource;
    private Long baseBizSourceId;
    private Long  baseCreateTime;
    private Long baseCreateUser;

    public String getBaseBizSource() {
        return this.baseBizSource;
    }

    public Long getBaseBizSourceId() {
        return this.baseBizSourceId;
    }

    public Long getBaseCreateTime() {
        return baseCreateTime;
    }

    public void setBaseCreateTime(Long baseCreateTime) {
        this.baseCreateTime = baseCreateTime;
    }

    public Long getBaseCreateUser() {
        return this.baseCreateUser;
    }

    public void setBaseBizSource(String baseBizSource) {
        this.baseBizSource = baseBizSource;
    }

    public void setBaseBizSourceId(Long baseBizSourceId) {
        this.baseBizSourceId = baseBizSourceId;
    }



    public void setBaseCreateUser(Long baseCreateUser) {
        this.baseCreateUser = baseCreateUser;
    }

    public String toString() {
        return "BaseBizObjEs(baseBizSource=" + this.getBaseBizSource() + ", baseBizSourceId=" + this.getBaseBizSourceId() + ", baseCreateTime=" + this.getBaseCreateTime() + ", baseCreateUser=" + this.getBaseCreateUser() + ")";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof BaseBizObjEs)) {
            return false;
        } else {
            BaseBizObjEs other = (BaseBizObjEs)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$baseBizSourceId = this.getBaseBizSourceId();
                Object other$baseBizSourceId = other.getBaseBizSourceId();
                if (this$baseBizSourceId == null) {
                    if (other$baseBizSourceId != null) {
                        return false;
                    }
                } else if (!this$baseBizSourceId.equals(other$baseBizSourceId)) {
                    return false;
                }

                Object this$baseCreateUser = this.getBaseCreateUser();
                Object other$baseCreateUser = other.getBaseCreateUser();
                if (this$baseCreateUser == null) {
                    if (other$baseCreateUser != null) {
                        return false;
                    }
                } else if (!this$baseCreateUser.equals(other$baseCreateUser)) {
                    return false;
                }

                Object this$baseBizSource = this.getBaseBizSource();
                Object other$baseBizSource = other.getBaseBizSource();
                if (this$baseBizSource == null) {
                    if (other$baseBizSource != null) {
                        return false;
                    }
                } else if (!this$baseBizSource.equals(other$baseBizSource)) {
                    return false;
                }

                Object this$baseCreateTime = this.getBaseCreateTime();
                Object other$baseCreateTime = other.getBaseCreateTime();
                if (this$baseCreateTime == null) {
                    if (other$baseCreateTime != null) {
                        return false;
                    }
                } else if (!this$baseCreateTime.equals(other$baseCreateTime)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof BaseBizObjEs;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $baseBizSourceId = this.getBaseBizSourceId();
        result = result * 59 + ($baseBizSourceId == null ? 43 : $baseBizSourceId.hashCode());
        Object $baseCreateUser = this.getBaseCreateUser();
        result = result * 59 + ($baseCreateUser == null ? 43 : $baseCreateUser.hashCode());
        Object $baseBizSource = this.getBaseBizSource();
        result = result * 59 + ($baseBizSource == null ? 43 : $baseBizSource.hashCode());
        Object $baseCreateTime = this.getBaseCreateTime();
        result = result * 59 + ($baseCreateTime == null ? 43 : $baseCreateTime.hashCode());
        return result;
    }
}
