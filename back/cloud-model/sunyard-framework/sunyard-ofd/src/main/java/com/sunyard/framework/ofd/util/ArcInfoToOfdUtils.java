package com.sunyard.framework.ofd.util;

import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.ofd.dto.OfdDTO;
import com.sunyard.framework.spire.util.ConvertOfdUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author HRH
 * @date 2023/7/14
 * @describe 档案信息转ofd
 */
@Slf4j
public class ArcInfoToOfdUtils {

    /**
     * 生成档案信息ofd文件
     *
     * @param ofd odf对象
     */
    public static void getArcInfoOfd(OfdDTO ofd) {
        getWord(ofd.getDataModel(), ofd.getWordPath(), ofd.getOutWordPath(), ofd.getOfdPath());
    }

    /**
     * 获取模板变量{{xxx}} 给返回的map赋值后在当作入参传入
     *
     * @param templatePath 临时路径
     * @return Result
     */
    public static Map<String, Object> getDataModel(String templatePath) {
        Map<String, Object> dataModel = new HashMap<>(16);
        try {
            FileInputStream fis = new FileInputStream(templatePath);
            XWPFDocument document = new XWPFDocument(fis);
            dataModel = getDataModel(document.getTables(), dataModel);
        } catch (Exception e) {
            log.error("getDataModel获取模板变量出错", e);
            throw new SunyardException(ResultCode.SYSTEM_BUSY_ERROR, e.toString());
        }
        return dataModel;
    }

    /**
     * 根据模板生成word，并转成ofd
     *
     * @param dataModel   模板变量数据
     * @param wordPath    word模板路径
     * @param outWordPath 输出word位置
     * @param ofdPath     ofd输出位置
     */
    private static void getWord(Map<String, Object> dataModel, String wordPath, String outWordPath, String ofdPath) {
        // 模板文件路径
        try {
            FileInputStream fis = new FileInputStream(wordPath);
            XWPFDocument document = new XWPFDocument(fis);
            FileOutputStream fos = new FileOutputStream(outWordPath);
            //校验 判断入参key与模板的key是否相同
            Set<String> dataModelKey = dataModel.keySet();
            Map<String, Object> dataModel1 = getDataModel(document.getTables(), dataModel);
            Set<String> dataModel1Key = dataModel1.keySet();
            AssertUtils.isTrue(!setEquals(dataModel1Key,dataModelKey),"模板参数不正确，请根据方法getDataModel()返回的模板进行传参");
            // 创建数据模型，包含需要替换的变量及其值 基本信息
            replaceTables(document.getTables(), dataModel);
            document.write(new BufferedOutputStream(fos));
            ConvertOfdUtils.wordOrTxtToOfd(wordPath, ofdPath);
        } catch (Exception e) {
            log.error("getWord" + e);
            throw new SunyardException(ResultCode.SYSTEM_BUSY_ERROR, e.toString());
        }
    }

    /**
     * 替换段落中的变量
     *
     * @param paragraphs paragraphs
     * @param dataModel dataModel
     */
    private static void replaceParagraphs(List<XWPFParagraph> paragraphs, Map<String, Object> dataModel) {
        for (XWPFParagraph paragraph : paragraphs) {
            List<XWPFRun> runs = paragraph.getRuns();
            for (int i = 0; i < runs.size(); i++) {
                XWPFRun run = runs.get(i);
                String text = run.getText(0);
                if (text != null) {
                    for (Map.Entry<String, Object> entry : dataModel.entrySet()) {
                        String placeholder = "{{" + entry.getKey() + "}}";
                        if (text.contains(placeholder)) {
                            text = text.replace(placeholder, String.valueOf(entry.getValue()));
                            run.setText(text, 0);
                        }
                    }
                }
            }
        }
    }

    /**
     * 替换表格中的变量
     *
     * @param tables tables
     * @param dataModel dataModel
     */
    private static void replaceTables(List<XWPFTable> tables, Map<String, Object> dataModel) {
        for (XWPFTable table : tables) {
            List<XWPFTableRow> rows = table.getRows();
            for (XWPFTableRow row : rows) {
                List<XWPFTableCell> cells = row.getTableCells();
                for (XWPFTableCell cell : cells) {
                    List<XWPFParagraph> paragraphs = cell.getParagraphs();
                    replaceParagraphs(paragraphs, dataModel);
                }
            }
        }
    }

    /**
     * 获取模板变量
     *
     * @param tables tables
     * @param dataModel dataModel
     */
    private static Map<String, Object> getDataModel(List<XWPFTable> tables, Map<String, Object> dataModel) {
        for (XWPFTable table : tables) {
            List<XWPFTableRow> rows = table.getRows();
            for (XWPFTableRow row : rows) {
                List<XWPFTableCell> cells = row.getTableCells();
                for (XWPFTableCell cell : cells) {
                    List<XWPFParagraph> paragraphs = cell.getParagraphs();
                    getDataMap(paragraphs, dataModel);
                }
            }
        }
        return dataModel;
    }

    /**
     * 获取模板变量
     *
     * @param paragraphs paragraphs
     * @param dataModel dataModel
     * @return Result
     */
    private static Map<String, Object> getDataMap(List<XWPFParagraph> paragraphs, Map<String, Object> dataModel) {
        for (XWPFParagraph paragraph : paragraphs) {
            List<XWPFRun> runs = paragraph.getRuns();
            for (int i = 0; i < runs.size(); i++) {
                XWPFRun run = runs.get(i);
                String text = run.getText(0);
                int index1 = text.indexOf("{{");
                int index2 = text.lastIndexOf("}}");
                if (index1 != -1 && index2 != -1 && index2 > index1) {
                    dataModel.put(text.substring(index1 + 2, index2), "");
                }
            }
        }
        return dataModel;
    }

    /**
     * 判断两个set集合是否相等
     * @param set1 set1
     * @param set2 set2
     * @return Result
     */
    private static boolean setEquals(Set<?> set1, Set<?> set2) {
        if (set1 == null || set2 == null) {
            //null就直接不比了
            return false;
        }
        if (set1.size() != set2.size()) {
            //大小不同也不用比了
            return false;
        }
        //最后比containsAll
        return set1.containsAll(set2);
    }

}
