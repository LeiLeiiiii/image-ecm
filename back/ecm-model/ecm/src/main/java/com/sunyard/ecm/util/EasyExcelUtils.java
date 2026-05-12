package com.sunyard.ecm.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelAnalysisStopException;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mysql.cj.util.TimeUtil.DATE_FORMATTER;

/**
 * @author 饶昌妹
 * @since 2024-12-23
 * @desc easyexcel工具类 -- Excel解析
 */
public class EasyExcelUtils {

    /**
     *
     * @param response
     * @param data
     * @param tableName
     * @param <T>
     * @throws IOException
     */
    public static <T> void writeListTo(HttpServletResponse response, List<T> data, String tableName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(tableName, "UTF-8"));

        try (OutputStream os = response.getOutputStream()) {
            writeListTo(os, data);
            os.flush(); // 确保数据全部写入
        }
    }

    /**
     *
     * @param os
     * @param data
     * @param <T>
     */
    public static <T> void writeListTo(OutputStream os, List<T> data) {
        Class<?> clazz = null;
        if (!data.isEmpty()) {
            clazz = data.get(0).getClass();
        }
        writeListTo(os, data, clazz);
    }

    /**
     *
     * @param os
     * @param data
     * @param clz
     * @param <T>
     */
    public static <T> void writeListTo(OutputStream os, List<T> data, Class<?> clz) {
        WriteSheet writeSheet = new WriteSheet();
        writeSheet.setClazz(clz);
        writeSheet.setNeedHead(true);
        ExcelWriter write = EasyExcel.write(os).build();
        write.write(data==null ? new ArrayList():data, writeSheet);
        write.finish();
    }

    /**
     * 写入excel
     *
     * @param response
     * @param head
     * @param data
     * @param fileName
     * @throws IOException
     */
    public static void writeToExcel(HttpServletResponse response, List<List<String>> head, List<List<String>> data, String fileName, String sheetName) throws IOException {
        // 安全编码文件名
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");

        try (OutputStream os = response.getOutputStream()) {
            EasyExcel.write(os)
                    .head(head)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .autoCloseStream(true)
                    .sheet(sheetName)
                    .doWrite(data);
        }
    }

    /**
     * 批量新建树-获取下载模板
     */
    public static void writeTreesToExcel(HttpServletResponse response, HashMap<String, List<String>> hashMap) throws IOException {
        // 创建工作簿
        SXSSFWorkbook workbook = new SXSSFWorkbook(100); // 内存优化，保留100行在内存

        for (Map.Entry<String, List<String>> entry : hashMap.entrySet()) {
            // 获取业务类型名称（即sheet名）
            String businessTypeName = entry.getKey();
            if (businessTypeName == null || businessTypeName.trim().isEmpty()) {
                continue; // 跳过无效名称
            }
            // 创建 sheet
            Sheet sheet = workbook.createSheet(businessTypeName);

            // 获取拓展属性列表
            List<String> extensionAttributes = entry.getValue();
            if (extensionAttributes == null) {
                extensionAttributes = new ArrayList<>();
            }

            // 设置列头
            Row headerRow = sheet.createRow(0);
//            headerRow.createCell(0).setCellValue("业务主索引");

            int colIndex = 0;
            for (String attr : extensionAttributes) {
                headerRow.createCell(colIndex++).setCellValue(attr);
            }

            // 可选：设置列宽（可调整）
            sheet.setColumnWidth(0, 20 * 256); // 业务主索引列宽
            for (int i = 1; i < colIndex; i++) {
                sheet.setColumnWidth(i, 15 * 256);
            }
        }

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=业务统计导出.xlsx");

        // 写入输出流
        workbook.write(response.getOutputStream());
        workbook.close();
    }


    /**
     * 解析Excel，返回 List<{appCode, data: List<List<String>>}>
     */
    public static List<Map<String, Object>> parseExcel(InputStream inputStream) throws java.io.IOException {
        List<Map<String, Object>> result = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();

                // 提取 appCode: (hz)xxx → hz
                String appCode = extractAppCode(sheetName);
                String appName = extractAppName(sheetName);
                if (appCode == null || appCode.trim().isEmpty()) {
                    continue;
                }

                List<List<String>> dataRows = new ArrayList<>();

                // 遍历所有行
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) {
                        continue; // 跳过第一行（表头）
                    }

                    List<String> rowData = new ArrayList<>();
                    boolean hasNonEmptyCell = false;

                    // 获取最大列数（避免因空单元格导致列数不一致）
                    int lastCellNum = row.getLastCellNum();
                    if (lastCellNum <= 0) continue;

                    for (int j = 0; j < lastCellNum; j++) {
                        Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        String value = getCellValue(cell);
                        rowData.add(value);
                        if (!value.isEmpty()) {
                            hasNonEmptyCell = true;
                        }
                    }

                    // 只有当该行至少有一个非空单元格时才保留
                    if (hasNonEmptyCell) {
                        dataRows.add(rowData);
                    }
                }

                if (!dataRows.isEmpty()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("appCode", appCode);
                    item.put("appName", appName);
                    item.put("data", dataRows);
                    result.add(item);
                }
            }
        }

        return result;
    }

    /**
     * 获取appCode
     * @param sheetName
     * @return
     */
    private static String extractAppCode(String sheetName) {
        Pattern pattern = Pattern.compile("^\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(sheetName);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    /**]
     * 获取appName
     * @param sheetName
     * @return
     */
    private static String extractAppName(String sheetName) {
        if (sheetName == null) {
            return null;
        }
        // 正则：匹配以 (xxx) 开头，后面跟任意字符（即 appName）
        Pattern pattern = Pattern.compile("^\\([^)]+\\)(.*)");
        Matcher matcher = pattern.matcher(sheetName.trim());
        if (matcher.matches()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    return Instant.ofEpochMilli(date.getTime())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .format(DATE_FORMATTER); // 输出如 "2026-01-14"
                } else {
                    // 防止数字被转成科学计数法（如保单号）
                    cell.setCellType(CellType.STRING);
                    return cell.getStringCellValue().trim();
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
            default:
                return "";
        }
    }

    /**
     * 判断文件内容是否为空
     *
     * @param file
     * @return
     */
    public static boolean isExcelCompletelyEmpty(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            // 先用 POI 获取 sheet 数量（只解析结构，不读数据，很快）
            Workbook workbook = WorkbookFactory.create(is);
            int numberOfSheets = workbook.getNumberOfSheets();
            workbook.close(); // 注意：Workbook 必须 close

            // 重新读取内容（因为流已消费）
            byte[] excelBytes = file.getBytes();

            for (int i = 0; i < numberOfSheets; i++) {
                if (hasNonEmptyRowInSheet(excelBytes, i)) {
                    return false;
                }
            }
            return true;

        } catch (Exception e) {
            throw new RuntimeException("判断 Excel 是否为空失败", e);
        }
    }

    // 检查指定编号的 sheet 是否包含非空单元格
    public static boolean hasNonEmptyRowInSheet(byte[] excelBytes, int sheetNo) {
        AtomicBoolean foundData = new AtomicBoolean(false);

        try {
            EasyExcel.read(new ByteArrayInputStream(excelBytes), new AnalysisEventListener<Map<Integer, String>>() {
                        @Override
                        public void invoke(Map<Integer, String> row, AnalysisContext context) {
                            boolean hasNonEmpty = row.values().stream()
                                    .anyMatch(cell -> cell != null && !cell.trim().isEmpty());
                            if (hasNonEmpty) {
                                foundData.set(true);
                                throw new ExcelAnalysisStopException(); // 立即停止
                            }
                        }

                        @Override
                        public void doAfterAllAnalysed(AnalysisContext context) {
                            // 不处理
                        }
                    })
                    .sheet(sheetNo) // 指定读取第 N 个 sheet（从 0 开始）
                    .doRead();

        } catch (ExcelAnalysisStopException e) {
            // 正常中断，说明有数据
        } catch (Exception e) {
            // 如果 sheet 不存在（如索引越界），EasyExcel 通常会抛 IllegalArgumentException 或类似异常
            // 我们在上层捕获并 break
            throw e;
        }

        return foundData.get();
    }
}
