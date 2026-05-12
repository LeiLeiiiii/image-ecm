package com.sunyard.ecm.po;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author scm
 * @since 2023/8/1 11:38
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmOperateLog对象", description = "影像业务操作表")
public class EcmBusiLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @ExcelIgnore
    private Long id;

    @ApiModelProperty(value = "业务类型名称")
    @ExcelProperty(value = "业务类型",index = 0)
    @ColumnWidth(12)
    private String appName;

    @ApiModelProperty(value = "业务类型ID")
    @ExcelIgnore
    private String appCode;

    @ApiModelProperty(value = "业务主索引")
    @ExcelProperty(value = "业务主索引",index = 1)
    @ColumnWidth(18)
    private String busiNo;

    @ApiModelProperty(value = "操作内容")
    @ExcelProperty(value = "操作内容",index = 2)
    @ColumnWidth(27)
    private String operateContent;

    @ApiModelProperty(value = "操作人")
    @ExcelProperty(value = "操作人",index = 3)
    @ColumnWidth(10)
    private String operator;

    @ApiModelProperty(value = "操作人ID")
    @ExcelIgnore
    private String operatorId;

    @ApiModelProperty(value = "操作时间")
    @ExcelProperty(value = "操作时间",index = 4)
    @ColumnWidth(27)
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "机构号")
    @ExcelProperty(value = "机构号",index = 5)
    @ColumnWidth(16)
    private String orgCode;

    @ApiModelProperty(value = "异常信息")
    @ExcelProperty(value = "异常信息",index = 6)
    @ColumnWidth(16)
    private String errorInfo;

    @ApiModelProperty(value = "(0 业务新增 ;1 业务查看 ;2 业务采集 ;3 业务删除;4 文件新增;5 文件查看; 6 文件编辑; 7 文件删除)")
    @ExcelIgnore
    private Integer operatorType;
}
