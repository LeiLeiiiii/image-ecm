package com.sunyard.framework.common.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
/**
 * @author 汪泽舟
 */
@Slf4j
public class RarUtils {

    /**
     * @param rarDir   rar解压文件路径包含文件名称
     * @param outDir   rar存储文件路径不含文件名称
     * @param passWord 密码
     * @return Result
     */
    public static boolean unRar(String rarDir, String outDir, String passWord) {
        RandomAccessFile randomAccessFile = null;
        IInArchive inArchive = null;
        try {
            // 第一个参数是需要解压的压缩包路径，第二个参数参考JdkAPI文档的RandomAccessFile
            randomAccessFile = new RandomAccessFile(rarDir, "r");
            if (StrUtil.isNotBlank(passWord)) {
                inArchive = SevenZip.openInArchive(ArchiveFormat.RAR5, new RandomAccessFileInStream(randomAccessFile), passWord);
            } else {
                inArchive = SevenZip.openInArchive(ArchiveFormat.RAR5, new RandomAccessFileInStream(randomAccessFile));
            }
            ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

            int fileCout = 0;
            int failCout = 0;
            int itemCout = simpleInArchive.getArchiveItems().length;
            String fail = "";
            for (final ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
                final int[] hash = new int[]{0};
                if (!item.isFolder()) {
                    ExtractOperationResult result;
                    final long[] sizeArray = new long[1];
                    String dest = outDir.substring(0, outDir.lastIndexOf("/")) + "/";
                    File outFile = new File(dest + item.getPath());
                    File parent = outFile.getParentFile();
                    if ((!parent.exists()) && (!parent.mkdirs())) {
                        continue;
                    }
                    if (StrUtil.isNotBlank(passWord)) {
                        result = item.extractSlow(data -> {
                            try {
                                IOUtils.write(data, new BufferedOutputStream(new FileOutputStream(outFile, true)));
                            } catch (Exception e) {
                                log.error("系统异常",e);
                                throw new RuntimeException(e);
                            }
                            hash[0] ^= Arrays.hashCode(data); // Consume data
                            sizeArray[0] += data.length;
                            return data.length; // Return amount of consumed
                        }, passWord);
                    } else { // 非加密
                        result = item.extractSlow(data -> {
                            try {
                                IOUtils.write(data, new BufferedOutputStream(new FileOutputStream(outFile, true)));
                            } catch (Exception e) {
                                log.error("系统异常",e);
                                throw new RuntimeException(e);
                            }
                            hash[0] ^= Arrays.hashCode(data); // Consume data
                            sizeArray[0] += data.length;
                            return data.length; // Return amount of consumed
                        });
                    }

                    // 解压成功
                    if (result == ExtractOperationResult.OK) {
                        fileCout++;
                    } else { // 解压失败
                        failCout++;
                        break;
                    }
                }
            }
            //去掉一个文件夹名称就正确
            return fileCout - 1 == itemCout;
        } catch (Exception e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (inArchive != null && randomAccessFile != null) {
                    inArchive.close();
                    randomAccessFile.close();
                }
            } catch (Exception e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
        }
    }

}
