package com.sunyard.framework.monitor.screw.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.smallbun.screw.core.Configuration;
import cn.smallbun.screw.core.engine.EngineConfig;
import cn.smallbun.screw.core.engine.EngineFileType;
import cn.smallbun.screw.core.engine.EngineTemplateType;
import cn.smallbun.screw.core.execute.DocumentationExecute;
import cn.smallbun.screw.core.process.ProcessConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 生成数据库文档
 *
 * @author huronghao
 */
@Slf4j
public class DatabaseDocUtils {

    //安全扫描问题解决
    private static final String FILE_OUTPUT_DIR = getSafeOutputDir();

    private static String getSafeOutputDir() {
        // 获取基础临时目录
        String tmpDir = System.getProperty("java.io.tmpdir");
        if (tmpDir == null || tmpDir.trim().isEmpty()) {
            throw new SecurityException("System temporary directory is not available (java.io.tmpdir is null or empty)");
        }

        // 构造并规范化基础路径（获取绝对路径，避免相对路径风险）
        Path basePath;
        try {
            // toRealPath：解析符号链接，获取真实绝对路径
            basePath = Paths.get(tmpDir).toRealPath().normalize();
        } catch (Exception e) {
            throw new SecurityException("Failed to resolve real path for system temporary directory: " + tmpDir, e);
        }

        // 构造目标输出路径并规范化
        Path outputPath;
        try {
            outputPath = basePath.resolve("db-doc").toRealPath().normalize();
        } catch (Exception e) {
            // 若目录不存在，先不解析真实路径，仅规范化
            outputPath = basePath.resolve("db-doc").normalize().toAbsolutePath();
        }

        // 安全校验：使用更严格的路径包含校验（替代startsWith，避免层级误判）
        // 验证outputPath是否是basePath的直接子目录（或自身），彻底防止跳出预期目录
        if (!outputPath.startsWith(basePath)) {
            throw new SecurityException("Invalid path attempted: Output directory is outside the system temporary directory. " +
                    "Expected base: " + basePath + ", Actual: " + outputPath);
        }

        // 创建目录（使用NIO2的Files类，更安全高效）
        try {
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            } else if (!Files.isDirectory(outputPath)) {
                // 额外校验：避免存在同名文件，导致目录创建失败
                throw new RuntimeException("Failed to create output directory: A file with the same name already exists - " + outputPath);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create or access output directory: " + outputPath, e);
        }

        return outputPath.toString();
    }
    private static String DOC_FILE_NAME = "数据库文档";
    private static String DOC_VERSION = "1.0.0";
    private static String DOC_DESCRIPTION = "数据库字段备注详解";
    private static List<String> ignoreTableNameList;

    private static List<String> tableNameList;
    private static List<String> ignorePrefixList;
    private static List<String> prefixList;
    private static List<String> ignoreSuffixList;
    private static List<String> suffixList;

    /**
     * 忽略的表名
     *
     * @param list 表名集合
     */
    public static void setIgnoreTableName(List<String> list) {
        ignoreTableNameList = list;
    }

    /**
     * 想要生成的表
     *
     * @param list 表名集合
     */
    public static void setTableName(List<String> list) {
        tableNameList = list;
    }

    /**
     * 忽略""开头的表
     *
     * @param list 表名集合
     */
    public static void setIgnorePrefix(List<String> list) {
        ignorePrefixList = list;
    }

    /**
     * 根据""开头的表生成
     *
     * @param list 表名集合
     */
    public static void setPrefix(List<String> list) {
        prefixList = list;
    }

    /**
     * 忽略""后缀的表
     *
     * @param list 表名集合
     */
    public static void setIgnoreSuffix(List<String> list) {
        ignoreSuffixList = list;
    }

    /**
     * 生成""后缀的表
     *
     * @param list 表名集合
     */
    public static void setSuffix(List<String> list) {
        suffixList = list;
    }

    /**
     * 设置版本 默认1.0.0
     *
     * @param version 版本号
     */
    public static void setDocVersion(String version) {
        DOC_VERSION = version;
    }

    /**
     * 设置文档描述
     *
     * @param description 描述
     */
    public static void setDocDescription(String description) {
        DOC_DESCRIPTION = description;
    }

    /**
     * @param url 数据库链接的url
     * @param userName 数据库用户名
     * @param password 数据库密码
     */
    public static void exportDatabaseDoc(HttpServletResponse response, EngineFileType fileOutputType, String url,
        String userName, String password) {
        String docFileName = DOC_FILE_NAME + "_" + IdUtil.fastSimpleUUID();
        doExportFile(response, fileOutputType, docFileName, url, userName, password);
    }

    /**
     * 导出文件
     * @param fileOutputType 文件类型
     * @param fileName 文件名
     * @param url url
     * @param userName 账号
     * @param password 密码
     */
    private static void doExportFile(HttpServletResponse response, EngineFileType fileOutputType, String fileName,
        String url, String userName, String password) {
        try (HikariDataSource dataSource = buildDataSource(url, userName, password)) {
            // 创建 screw 的配置
            Configuration config = Configuration.builder()
                // 版本
                .version(DOC_VERSION)
                // 描述
                .description(DOC_DESCRIPTION)
                // 数据源
                .dataSource(dataSource)
                // 引擎配置
                .engineConfig(buildEngineConfig(fileOutputType, fileName))
                // 处理配置
                .produceConfig(buildProcessConfig()).build();

            // 执行 screw，生成数据库文档
            new DocumentationExecute(config).execute();
//            File file = new File(FILE_OUTPUT_DIR + File.separator + fileName + fileOutputType.getFileSuffix());
            File file = getSafeOutputFile(fileName, fileOutputType.getFileSuffix());
            // 设置 header 和 contentType
            try {
                response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(file.getName(), "UTF-8"));
                response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                // 输出附件
                IoUtil.write(response.getOutputStream(), false, FileUtil.readBytes(file.getPath()));
            } catch (Exception e) {
                log.error("返回文件出错：",e);
                throw new RuntimeException(e);
            } finally {
                log.error("删除文件：" + file.getName());
                FileUtil.del(file);
            }
        }
    }

    /**
     * 数据源
     *
     * @param url url
     * @param userName 账号
     * @param password 密码
     * @return Result
     */
    private static HikariDataSource buildDataSource(String url, String userName, String password) {
        // 创建 HikariConfig 配置类
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(userName);
        hikariConfig.setPassword(password);
        // 设置可以获取 tables remarks 信息
        hikariConfig.addDataSourceProperty("useInformationSchema", "true");
        // 创建数据源
        return new HikariDataSource(hikariConfig);
    }

    /**
     * 创建 screw 的引擎配置
     *
     * @param fileOutputType 文件输出流
     * @param docFileName 文档名称
     * @return Result
     */
    private static EngineConfig buildEngineConfig(EngineFileType fileOutputType, String docFileName) {
        return EngineConfig.builder()
            // 生成文件路径
            .fileOutputDir(FILE_OUTPUT_DIR)
            // 打开目录
            .openOutputDir(false)
            // 文件类型
            .fileType(fileOutputType)
            // 文件类型
            .produceType(EngineTemplateType.freemarker)
            // 自定义文件名称
            .fileName(docFileName).build();
    }

    /**
     * 指定生成逻辑、当存在指定表、指定表前缀、指定表后缀时，将生成指定表，其余表不生成、并跳过忽略表配置
     *
     * @return Result
     */
    private static ProcessConfig buildProcessConfig() {
        return ProcessConfig.builder()
            // 忽略表前缀
            .ignoreTablePrefix(ignorePrefixList)
            // 忽略表后缀
            .ignoreTableSuffix(ignoreSuffixList)
            // 忽略表
            .ignoreTableName(ignoreTableNameList)
            // 根据表名称生成
            .designatedTableName(tableNameList)
            // 根据表前缀生成
            .designatedTablePrefix(prefixList)
            // 根据表后缀
            .designatedTableSuffix(suffixList).build();

    }

    static File getSafeOutputFile(String fileName, String fileSuffix) {
        Path safePath = getSafeOutputPath(fileName, fileSuffix);
        File file = safePath.toFile();
        return file;
    }

    // 安全生成目标文件路径
    public static Path getSafeOutputPath(String fileName, String fileSuffix) {
        // 1. 获取基础安全目录（确保已在安全范围内）
        Path baseDir = Paths.get(FILE_OUTPUT_DIR).normalize().toAbsolutePath();

        // 2. 拼接文件名并规范化（防止路径遍历）
        Path resolvedPath = baseDir.resolve(fileName + fileSuffix).normalize();

        // 3. 检查是否仍在安全目录内（防止跳出）
        if (!resolvedPath.startsWith(baseDir)) {
            throw new SecurityException("非法路径访问: " + fileName);
        }

        return resolvedPath;
    }
}
