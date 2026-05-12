package com.sunyard.framework.elasticsearch.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.BodyElementType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author peiy.yang
 * @Desc
 * @date 2024/10/21 20:09
 */
public class PdfBoxAndPoiUtils {

    /**
     * 用pdfBox解析pdf格式文件内容
     *
     * @param file 文件对象
     */
    public static String parserPDF(File file) throws IOException {
        PDDocument document= Loader.loadPDF(file);
        PDFTextStripper pdfStripper = new PDFTextStripper();
        String text = pdfStripper.getText(document);
        document.close();
        return text;
    }

    /**
     * 抽取txt文件内容
     *
     * @param file 文件对象
     */
    public static String parserTxt(File file) throws IOException {
        String lineTxt = null;//行读字符串
        InputStreamReader read = new InputStreamReader(new FileInputStream(file));// 字符缓冲输入流
        BufferedReader bufferedReader = new BufferedReader(read);
        StringBuffer xpStr = new StringBuffer(); //文件文本字符串
        while((lineTxt = bufferedReader.readLine()) != null){
            //处理字符串lineTxt_cr
            xpStr.append(lineTxt);
        }
        // 释放资源
        bufferedReader.close();
        read.close();
        return xpStr.toString();
    }

    /**
     * 抽取csv格式文件内容
     *
     * @param file 文件对象
     */
    public static String parseCsv(File file) throws IOException {
        try(FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader isReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(isReader);) {
            String testLine;
            StringBuilder context = new StringBuilder();
            while (true) {
                testLine = in.readLine();
                if (testLine != null) {
                    String[] split = testLine.split(",");
                    for (String s : split
                    ) {
                        context.append(s).append("  ");
                    }

                } else {
                    break;
                }
            }
            return context.toString();
        }
    }

    /**
     * 按文档原生顺序提取doc/docx文件内容（段落+表格按出现顺序提取）
     * @param file 文件对象
     * @return 提取的全部文本（保持原文档顺序）
     * @throws IOException 流操作异常
     */
    public static String parseDocxAndDoc(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("文件不存在：" + file.getAbsolutePath());
        }

        StringBuilder context = new StringBuilder();
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);
            String fileName = file.getName().toLowerCase();

            // 区分doc和docx格式
            if (fileName.endsWith(".docx")) {
                // 处理docx文件：按节点顺序遍历（核心修改）
                XWPFDocument document = new XWPFDocument(fis);

                // 遍历文档的所有正文元素（按原生顺序）
                for (IBodyElement bodyElement : document.getBodyElements()) {
                    // 判断当前元素是段落还是表格，按顺序处理
                    if (bodyElement.getElementType() == BodyElementType.PARAGRAPH) {
                        // 处理段落：保持原顺序
                        XWPFParagraph para = (XWPFParagraph) bodyElement;
                        String paraText = para.getText().trim();
                        if (!paraText.isEmpty()) {
                            context.append(paraText).append("\n");
                        }
                    } else if (bodyElement.getElementType() == BodyElementType.TABLE) {
                        // 处理表格：保持原顺序
                        XWPFTable table = (XWPFTable) bodyElement;
                        // 遍历表格的每一行
                        for (XWPFTableRow row : table.getRows()) {
                            // 遍历行中的每个单元格
                            for (XWPFTableCell cell : row.getTableCells()) {
                                String cellText = cell.getText().trim();
                                if (!cellText.isEmpty()) {
                                    context.append(cellText).append("\t");
                                }
                            }
                            context.append("\n"); // 行结束换行
                        }
                        context.append("\n"); // 表格结束空一行，区分上下文
                    }
                }
                document.close();
            } else if (fileName.endsWith(".doc")) {
                // 老版doc文件：WordExtractor本身就是按原顺序提取的，无需修改
                HWPFDocument document = new HWPFDocument(fis);
                WordExtractor extractor = new WordExtractor(document);
                context.append(extractor.getText().trim());
                extractor.close();
                document.close();
            } else {
                throw new IOException("不支持的文件格式，仅支持doc/docx：" + fileName);
            }
        } finally {
            // 确保流最终关闭，避免资源泄漏
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    
                }
            }
        }

        return context.toString();
    }

    // 读取所有Sheet，不显示行号，不标记空值
    public static String parseXlsAndXlsx(File file) throws IOException {
        Workbook wb = null;
        FileInputStream fis = null;
        StringBuilder allContent = new StringBuilder();

        try {
            fis = new FileInputStream(file);
            wb = new XSSFWorkbook(fis);
            int sheetCount = wb.getNumberOfSheets();
//            allContent.append("=== Excel文件总Sheet数：").append(sheetCount).append(" ===\n\n");

            for (int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
                Sheet sheet = wb.getSheetAt(sheetIndex);
                String sheetName = sheet.getSheetName();
//                 仅保留Sheet名称标识，删除行号
                allContent.append(sheetName).append("\n");
                allContent.append("----------------------------------------\n");

                // 1. 获取合并区域左上角值映射
                Map<String, String> mergedTopLeftMap = getMergedTopLeftValues(sheet);
                // 2. 记录所有合并区域单元格位置
                Map<String, Boolean> mergedPosMap = getMergedPositions(sheet);

                int firstRow = sheet.getFirstRowNum();
                int lastRow = sheet.getLastRowNum();
                if (firstRow > lastRow) {
//                    allContent.append("（当前Sheet无数据）\n\n");
                    continue;
                }

                // 遍历每行（不打印行号）
                for (int rowIdx = firstRow; rowIdx <= lastRow; rowIdx++) {
                    Row row = sheet.getRow(rowIdx);
                    if (row == null) {
                        allContent.append("\n"); // 空行仅保留换行
                        continue;
                    }

                    int firstCell = row.getFirstCellNum();
                    int lastCell = row.getLastCellNum();
                    StringBuilder rowContent = new StringBuilder();

                    // 遍历每列（空值位置留空，不显示“空值”文字）
                    for (int colIdx = firstCell; colIdx < lastCell; colIdx++) {
                        String cellKey = rowIdx + "_" + colIdx;
                        String cellValue = ""; // 默认空字符串，不显示“空值”

                        // 优先级：合并区域左上角→普通单元格→合并区域其他位置（留空）
                        if (mergedTopLeftMap.containsKey(cellKey)) {
                            // 合并区域左上角：显示值
                            cellValue = mergedTopLeftMap.get(cellKey);
                        } else if (!mergedPosMap.containsKey(cellKey)) {
                            // 普通单元格：有值则显示，无值留空
                            Cell cell = row.getCell(colIdx);
                            if (cell != null) {
                                cellValue = getCellValueAsString(cell);
                            }
                        }

                        // 单元格内容间用制表符分隔，空值留空
                        rowContent.append(cellValue).append("\t");
                    }

                    // 去除行末尾多余制表符，添加换行
                    if (rowContent.length() > 0) {
                        String cleanRow = rowContent.toString().replaceAll("\t$", "");
                        allContent.append(cleanRow).append("\n");
                    }
                }
                allContent.append("\n"); // Sheet间空行分隔
            }

        } finally {
            // 关闭资源
            if (wb != null) wb.close();
            if (fis != null) fis.close();
        }
        return allContent.toString();
    }

    // 仅获取合并区域左上角单元格的“位置-值”映射
    private static Map<String, String> getMergedTopLeftValues(Sheet sheet) {
        Map<String, String> map = new HashMap<>();
        int mergedCount = sheet.getNumMergedRegions();

        for (int i = 0; i < mergedCount; i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            int topRow = region.getFirstRow();
            int topCol = region.getFirstColumn();
            String key = topRow + "_" + topCol;

            // 读取左上角单元格值
            Row row = sheet.getRow(topRow);
            if (row != null) {
                Cell cell = row.getCell(topCol);
                if (cell != null) {
                    map.put(key, getCellValueAsString(cell));
                }
            }
        }
        return map;
    }

    // 记录所有合并区域的单元格位置
    private static Map<String, Boolean> getMergedPositions(Sheet sheet) {
        Map<String, Boolean> map = new HashMap<>();
        int mergedCount = sheet.getNumMergedRegions();

        for (int i = 0; i < mergedCount; i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            int startRow = region.getFirstRow();
            int endRow = region.getLastRow();
            int startCol = region.getFirstColumn();
            int endCol = region.getLastColumn();

            // 遍历合并区域所有单元格，标记位置
            for (int r = startRow; r <= endRow; r++) {
                for (int c = startCol; c <= endCol; c++) {
                    map.put(r + "_" + c, true);
                }
            }
        }
        return map;
    }

    // 统一单元格值格式（无额外标记）
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        // 先处理公式类型：获取公式计算后的缓存结果类型
        CellType cellType = cell.getCellType();
        if (cellType == CellType.FORMULA) {
            cellType = cell.getCachedFormulaResultType(); // 获取公式计算结果的类型
        }
        switch (cellType) {
            case STRING:
                return cell.getStringCellValue().trim(); // 文本去空格
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString(); // 日期格式
                } else {
                    // 数字去末尾.0，避免科学计数法
                    return String.valueOf(cell.getNumericCellValue()).replaceAll("\\.0$", "");
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue()); // 布尔值
            default:
                return "";
        }
    }
}
