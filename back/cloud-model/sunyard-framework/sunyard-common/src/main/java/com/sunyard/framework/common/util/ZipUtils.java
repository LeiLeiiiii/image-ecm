package com.sunyard.framework.common.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author liugang
 * @Type com.sunyard.sunam.utils
 * @Desc
 * @date 9:51 2021/11/17
 */
@Slf4j
public class ZipUtils {

    private static final int BUFFER_SIZE = 2 * 1024;

    /**
     * 压缩成ZIP 方法1
     *
     * @param srcDir 压缩文件夹路径
     * @param out 压缩文件输出流
     * @param keepDirStructure 是否保留原来的目录结构,true:保留目录结构; false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws RuntimeException 压缩失败会抛出运行时异常
     */
    public static void toZip(String srcDir, OutputStream out, boolean keepDirStructure) throws RuntimeException {

        long start = System.currentTimeMillis();
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(out);
            File sourceFile = new File(srcDir);
            compress(sourceFile, zos, sourceFile.getName(), keepDirStructure);
            long end = System.currentTimeMillis();
            log.debug("压缩完成，耗时：" + (end - start) + " ms");
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils", e);
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
            }
        }

    }

    /**
     * 压缩成ZIP 方法2
     *
     * @param srcFiles 需要压缩的文件列表
     * @param out 压缩文件输出流
     * @throws RuntimeException 压缩失败会抛出运行时异常
     */
    public static void toZip(List<File> srcFiles, OutputStream out) {
        /*long start = System.currentTimeMillis();*/
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(out);
            for (File srcFile : srcFiles) {
                byte[] buf = new byte[BUFFER_SIZE];
                zos.putNextEntry(new ZipEntry(srcFile.getName()));
                int len;
                FileInputStream in = new FileInputStream(srcFile);
                while ((len = in.read(buf)) != -1) {
                    zos.write(buf, 0, len);
                }
                zos.closeEntry();
                in.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils", e);
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 递归压缩方法
     *
     * @param sourceFile 源文件
     * @param zos zip输出流
     * @param name 压缩后的名称
     * @param keepDirStructure 是否保留原来的目录结构,true:保留目录结构; false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws Exception
     */
    private static void compress(File sourceFile, ZipOutputStream zos, String name, boolean keepDirStructure)
        throws Exception {
        byte[] buf = new byte[BUFFER_SIZE];
        if (sourceFile.isFile()) {
            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
            zos.putNextEntry(new ZipEntry(name));
            // copy文件到zip输出流中
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buf)) != -1) {
                zos.write(buf, 0, len);
            }
            // Complete the entry
            zos.closeEntry();
            in.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                // 需要保留原来的文件结构时,需要对空文件夹进行处理
                if (keepDirStructure) {
                    // 空文件夹的处理
                    zos.putNextEntry(new ZipEntry(name + "/"));
                    // 没有文件，不需要文件的copy
                    zos.closeEntry();
                }

            } else {
                for (File file : listFiles) {
                    // 判断是否需要保留原来的文件结构
                    if (keepDirStructure) {
                        // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
                        // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
                        compress(file, zos, name + "/" + file.getName(), keepDirStructure);
                    } else {
                        compress(file, zos, file.getName(), keepDirStructure);
                    }

                }
            }
        }
    }

    /**
     * 压缩文件
     *
     * @param filePath 待压缩的文件路径
     * @param zipPath 压缩后的文件路径
     * @return Result 压缩后的文件
     */
    public static File zip(String filePath, String zipPath) {
        File target = null;
        File source = new File(filePath);
        if (source.exists()) {
            // 压缩文件名=源文件名.zip
            target = new File(zipPath);
            if (target.exists()) {
                target.delete(); // 删除旧的文件
            }
            FileOutputStream fos = null;
            ZipOutputStream zos = null;
            try {
                target.createNewFile();
                fos = new FileOutputStream(target);
                zos = new ZipOutputStream(new BufferedOutputStream(fos));
                // 添加对应的文件Entry
                addEntry("/", source, zos);
            } catch (IOException e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            } finally {
                try {
                    if (zos != null) {
                        zos.close();
                    }
                } catch (IOException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
            }
        }
        return target;
    }

    /**
     * 扫描添加文件Entry
     *
     * @param base 基路径
     *
     * @param source 源文件
     * @param zos Zip文件输出流
     * @throws IOException 异常
     */
    private static void addEntry(String base, File source, ZipOutputStream zos) throws IOException {
        // 按目录分级，形如：/aaa/bbb.txt
        String entry = base + source.getName();
        if (source.isDirectory()) {
            for (File file : source.listFiles()) {
                // 递归列出目录下的所有文件，添加文件Entry
                addEntry(entry + "/", file, zos);
            }
        } else {
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                byte[] buffer = new byte[1024 * 10];
                fis = new FileInputStream(source);
                bis = new BufferedInputStream(fis, buffer.length);
                int read = 0;
                zos.putNextEntry(new ZipEntry(entry));
                while ((read = bis.read(buffer, 0, buffer.length)) != -1) {
                    zos.write(buffer, 0, read);
                }
                zos.closeEntry();
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
                try {
                    if (bis != null) {
                        bis.close();
                    }
                } catch (IOException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 压缩文件
     * @param sourceFilePath 源文件路径
     * @param zipFilePath 压缩文件路径
     * @throws IOException 异常
     */
    public static void toZip(String sourceFilePath, String zipFilePath) throws IOException {
        long startTime = System.currentTimeMillis();
        FileOutputStream fos = new FileOutputStream(zipFilePath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(sourceFilePath);

        if (fileToZip.isDirectory()) {
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, "", zipOut);
            }
        } else {
            zipFile(fileToZip, "", zipOut);
        }
        log.info("压缩文件：" + sourceFilePath + "到指定路径：" + zipFilePath + "用时："+ (System.currentTimeMillis() - startTime) + "毫秒");
        zipOut.close();
        fos.close();
    }

    /**
     * 压缩文件
     * @param fileToZip 文件
     * @param parentName 压缩包名称
     * @param zipOut 压缩包输出流
     * @throws IOException 异常
     */
    private static void zipFile(File fileToZip, String parentName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, parentName + fileToZip.getName() + "/", zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(parentName + fileToZip.getName());
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    /**
     * apache压缩
     * @param filePath 文件地址
     * @param zipFilePath 压缩包地址
     * @throws Exception 异常
     */
    public static void apacheZip(String filePath, String zipFilePath) throws Exception{
        long startTime = System.currentTimeMillis();
        byte[] buf = new byte[1024];
        File file = new File(filePath);
        File zipFile = new File(zipFilePath);
        File[] files = file.listFiles();
        if (!zipFile.exists()) {
            zipFile.getParentFile().mkdirs();
        }
        FileOutputStream fileOut = new FileOutputStream(zipFilePath);
        CheckedOutputStream ch = new CheckedOutputStream(fileOut, new CRC32());
        BufferedOutputStream bufferedOut = new BufferedOutputStream(ch);
        org.apache.tools.zip.ZipOutputStream out = new org.apache.tools.zip.ZipOutputStream(bufferedOut);
        FileInputStream in = null;

        try {
            for (int i = 0; i < files.length; i++) {
                in = new FileInputStream(files[i]);
                out.putNextEntry(new org.apache.tools.zip.ZipEntry(files[i].getName()));
                out.setEncoding("gbk");
                int len = 0;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                in = null;
                out.closeEntry();
            }
            log.info("压缩文件：" + filePath + "到指定路径：" + zipFilePath + "用时："+ (System.currentTimeMillis() - startTime) + "毫秒");
        } catch (Exception e) {
            log.error("系统异常",e);
            throw new Exception("压缩文件：" + filePath + "到指定路径：" + zipFilePath + "失败！");
        }finally{
            if(out != null){
                out.flush();
                out.close();
                out = null;
            }
            if(bufferedOut != null){
                bufferedOut.flush();
                bufferedOut.close();
                bufferedOut = null;
            }
            if(ch != null){
                ch.flush();
                ch.close();
                ch = null;
            }
            if(fileOut != null){
                fileOut.flush();
                fileOut.close();
                fileOut = null;
            }
            if(in != null){
                in.close();
                in = null;
            }
        }
    }

    /**
     * apache压缩（支持文件夹/文件，递归压缩子目录）
     * @param sourcePath 源路径（可以是单个文件或文件夹路径）
     * @param zipFilePath 压缩包输出路径
     * @throws Exception 压缩异常
     */
    public static void apacheZipCompress(String sourcePath, String zipFilePath) throws Exception {
        long startTime = System.currentTimeMillis();
        File sourceFile = new File(sourcePath);
        File zipFile = new File(zipFilePath);

        // 校验源文件/文件夹是否存在
        if (!sourceFile.exists()) {
            throw new FileNotFoundException("源路径不存在：" + sourcePath);
        }

        // 创建压缩包父目录（如果不存在）
        if (!zipFile.getParentFile().exists()) {
            boolean mkdirsSuccess = zipFile.getParentFile().mkdirs();
            if (!mkdirsSuccess) {
                throw new IOException("创建压缩包父目录失败：" + zipFile.getParentFile().getAbsolutePath());
            }
        }

        // 初始化压缩流（使用try-with-resources自动关闭流，无需手动finally关闭）
        try (FileOutputStream fileOut = new FileOutputStream(zipFile);
             CheckedOutputStream checkedOut = new CheckedOutputStream(fileOut, new CRC32());
             BufferedOutputStream bufferedOut = new BufferedOutputStream(checkedOut);
             ZipOutputStream zipOut = new ZipOutputStream(bufferedOut)) {

            // 递归压缩源文件/文件夹
            compress(sourceFile, sourceFile, zipOut);

            log.info("压缩完成！源路径：{} → 压缩包路径：{}，用时：{}毫秒，压缩包大小：{}KB",
                    sourcePath, zipFilePath,
                    (System.currentTimeMillis() - startTime),
                    zipFile.length() / 1024);
        } catch (Exception e) {
            log.error("压缩失败！源路径：{} → 压缩包路径：{}", sourcePath, zipFilePath, e);
            // 压缩失败时删除生成的空压缩包
            if (zipFile.exists() && zipFile.length() == 0) {
                zipFile.delete();
            }
            throw new Exception("压缩文件失败",e);
        }
    }

    /**
     * 递归压缩文件/文件夹核心方法
     * @param rootFile 根目录（用于计算文件相对路径）
     * @param currentFile 当前要压缩的文件/文件夹
     * @param zipOut Zip输出流
     * @throws IOException IO异常
     */
    private static void compress(File rootFile, File currentFile, ZipOutputStream zipOut) throws IOException {
        // 如果是文件夹，递归处理子文件/子文件夹
        if (currentFile.isDirectory()) {
            // 获取当前文件夹下的所有文件/子文件夹
            File[] subFiles = currentFile.listFiles();
            if (subFiles == null) {
                log.warn("无法访问文件夹：{}（可能是权限不足）", currentFile.getAbsolutePath());
                return;
            }

            // 遍历子文件/子文件夹
            for (File subFile : subFiles) {
                compress(rootFile, subFile, zipOut);
            }
        } else {
            // 如果是文件，直接压缩（保留相对路径）
            // 计算相对路径：例如 rootFile=D:/test，currentFile=D:/test/a/b.txt → 相对路径=a/b.txt
            String relativePath = getRelativePath(rootFile, currentFile);
            // 创建ZipEntry（相对路径作为压缩包内的文件路径）
            ZipEntry zipEntry = new ZipEntry(relativePath);
            // 设置文件修改时间（可选，保留原文件时间戳）
            zipEntry.setTime(currentFile.lastModified());

            try (FileInputStream fileIn = new FileInputStream(currentFile);
                 BufferedInputStream bufferedIn = new BufferedInputStream(fileIn, BUFFER_SIZE)) {

                // 写入ZipEntry并开始写入文件内容
                zipOut.putNextEntry(zipEntry);
                byte[] buf = new byte[BUFFER_SIZE];
                int len;
                while ((len = bufferedIn.read(buf)) != -1) {
                    zipOut.write(buf, 0, len);
                }
                // 关闭当前Entry（必须调用，否则后续Entry会出错）
                zipOut.closeEntry();
                log.debug("已压缩文件：{} → 压缩包内路径：{}", currentFile.getAbsolutePath(), relativePath);
            } catch (IOException e) {
                log.error("压缩单个文件失败：{}", currentFile.getAbsolutePath(), e);
                throw e; // 抛出异常，终止整个压缩流程
            }
        }
    }

    /**
     * 计算currentFile相对于rootFile的相对路径
     * @param rootFile 根目录（必须是文件或文件夹）
     * @param currentFile 当前文件（必须是文件，且在rootFile之下）
     * @return 相对路径（例如：root=test，current=test/a/b.txt → a/b.txt）
     * @throws IOException 路径转换异常
     */
    private static String getRelativePath(File rootFile, File currentFile) throws IOException {
        String rootPath = rootFile.getCanonicalPath();
        String currentPath = currentFile.getCanonicalPath();

        // 如果根目录是文件（即源路径是单个文件），则相对路径就是文件名
        if (rootFile.isFile()) {
            return currentFile.getName();
        }

        // 确保currentFile在rootFile目录下（避免跨目录压缩）
        if (!currentPath.startsWith(rootPath)) {
            throw new IOException("当前文件不在根目录之下：" + currentFile.getAbsolutePath());
        }

        // 截取相对路径（去掉rootPath前缀，再去掉开头的路径分隔符）
        String relativePath = currentPath.substring(rootPath.length());
        if (relativePath.startsWith(File.separator)) {
            relativePath = relativePath.substring(1);
        }

        return relativePath;
    }

    /**
     * 安全的ZIP解压方法，修复路径遍历和文件覆盖漏洞
     * @param filePath 待解压的ZIP文件路径
     */
    public static void unzip(String filePath) {
        File source = new File(filePath);
        if (!source.exists()) {
            log.warn("ZIP文件不存在: {}", filePath);
            return;
        }

        // 确定预期的解压目录（源文件所在目录）
        File destDir = source.getParentFile();
        if (destDir == null) {
            throw new RuntimeException("无法确定解压目标目录");
        }

        ZipInputStream zis = null;
        try {
            // 获取目标目录的规范路径（绝对路径，消除相对路径符号）
            Path destDirPath = destDir.toPath().toRealPath();
            zis = new ZipInputStream(new FileInputStream(source));
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                // 跳过目录条目
                if (entry.isDirectory()) {
                    zis.closeEntry();
                    continue;
                }

                // 1. 校验ZIP条目名称，禁止包含路径遍历字符
                String entryName = entry.getName();
                if (isInvalidEntryName(entryName)) {
                    log.error("发现恶意ZIP条目，包含非法路径字符: {}", entryName);
                    throw new SecurityException("ZIP文件包含非法条目，解压终止");
                }

                // 2. 构建目标文件路径
                Path targetPath = destDirPath.resolve(entryName);

                // 3. 关键校验：确保目标文件在预期解压目录内（防止跳出目录）
                if (!targetPath.toRealPath().startsWith(destDirPath)) {
                    log.error("ZIP条目路径超出允许范围: {}", entryName);
                    throw new SecurityException("ZIP文件包含越权路径条目，解压终止");
                }

                // 4. 检查文件是否已存在，禁止覆盖（根据业务可调整为允许覆盖）
                if (Files.exists(targetPath)) {
                    log.error("解压失败，文件已存在: {}", targetPath);
                    throw new IOException("文件已存在，避免覆盖: " + targetPath.getFileName());
                }

                // 5. 创建父目录（确保目录结构正确）
                Files.createDirectories(targetPath.getParent());
                try( // 6. 写入文件（使用安全路径）
                     OutputStream outputStream = Files.newOutputStream(targetPath);
                     BufferedOutputStream bos = new BufferedOutputStream(outputStream);) {

                    byte[] buffer = new byte[1024 * 10];
                    int read;
                    while ((read = zis.read(buffer, 0, buffer.length)) != -1) {
                        bos.write(buffer, 0, read);
                    }
                    bos.flush();
                    zis.closeEntry();
                }
            }

        } catch (IOException e) {
            log.error("ZIP解压失败", e);
            throw new RuntimeException("解压文件异常:" , e);
        } finally {
            // 安全关闭流
            if (zis != null) {
                try {
                    zis.close();
                } catch (IOException e) {
                    log.error("关闭ZipInputStream失败", e);
                }
            }

        }
    }

    /**
     * 检查ZIP条目名称是否包含非法字符（防止路径遍历）
     */
    private static boolean isInvalidEntryName(String entryName) {
        // 禁止包含..、绝对路径符号/或\
        return entryName.contains("..")
                || entryName.startsWith("/")
                || entryName.startsWith("\\")
                // 禁止包含系统敏感目录（可选增强）
                || entryName.contains("etc/passwd")
                || entryName.contains("windows/system32");
    }

}
