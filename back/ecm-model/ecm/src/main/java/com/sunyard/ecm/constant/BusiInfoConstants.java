package com.sunyard.ecm.constant;

/**
 * @author XQZ
 * @date 2023/4/25
 * @describe 业务常量配置
 */
public class BusiInfoConstants {

    // 输入框
    public final static Integer INPUT_TYP = 1;
    // 日期框
    public final static Integer DATE_TYPE = 2;
    // 选择框
    public final static Integer SELECT_TYPE = 3;

    /**
     * 业务量统计导出文件名
     */
    public static final String BUSI_STATISTICS = "业务量统计.xlsx";

    /**
     * 业务量统计导出文件名
     */
    public static final String WORK_STATISTICS = "工作量统计.xlsx";

    /**
     * 业务状态(0待提交 1 已提交  2已受理（处理中）3 已作废 4 处理失败 5 已完结)
     */
    public final static Integer BUSI_STATUS_ZERO = 0;
    public final static Integer BUSI_STATUS_ONE = 1;
    public final static Integer BUSI_STATUS_TWO = 2;
    public final static Integer BUSI_STATUS_THREE = 3;
    public final static Integer BUSI_STATUS_FOUR = 4;
    public final static Integer BUSI_STATUS_FIVE = 5;
    public final static String BUSI_STATUS_ZERO_STR = "待提交";
    public final static String BUSI_STATUS_ONE_STR = "已提交";
    public final static String BUSI_STATUS_TWO_STR = "处理中";
    public final static String BUSI_STATUS_THREE_STR = "已作废";
    public final static String BUSI_STATUS_FOUR_STR = "处理失败";
    public final static String BUSI_STATUS_FIVE_STR = "已完结";

    /**
     * 影像扫描列表导出文件名
     */
    public static final String BUSI_IMAGES_SCAN_EXCEL_NAME = "影像扫描列表.xlsx";
    /**
     * 影像扫描列表导出sheet列名称
     */
    public static final String BUSI_IMAGES_SCAN_SHEET = "业务信息";

    /**
     * 默认excel 表头
     */
    public static final String[] DEFAULT_EXCEL_HEADER = {"序号", "业务类型", "业务主索引", "业务状态","机构号","模板","创建人", "创建时间", "最新修改人", "最新修改时间"};
}
