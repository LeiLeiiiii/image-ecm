package com.sunyard.mytool.until;


import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.ss.usermodel.DateUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
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
     * 用poi抽取docx文件内容
     *
     * @param file 文件对象
     */
    public static String parseDocxAndDoc(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        XWPFDocument document = new XWPFDocument(fis);
        StringBuilder context= new StringBuilder();
        // 获取所有段落
        List<XWPFParagraph> paragraphs = document.getParagraphs();

        // 遍历段落并提取文本
        for (XWPFParagraph para : paragraphs) {
            context.append(para.getText());
        }

        fis.close();
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
        switch (cell.getCellType()) {
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
            case FORMULA:
                // 公式取结果，失败则留空（不显示公式）
                try {
                    return getCellValueAsString(cell);
                } catch (Exception e) {
                    return "";
                }
            default:
                return "";
        }
    }

}
