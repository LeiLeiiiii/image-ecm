package com.sunyard.ecm.util;

import com.sunyard.ecm.dto.EcmDownloadByFileIdDTO;
import com.sunyard.ecm.dto.EcmDownloadFileDTO;
import lombok.extern.slf4j.Slf4j;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author: lw
 * @Date: 2024/1/28
 * @Description: 文件压缩工具类
 */
@Slf4j
public class FileZipUtils {


    /**
     * @param inputFileName 你要压缩的文件夹(整个完整路径)
     * @param zipFileName   压缩后的文件(整个完整路径)
     * @throws Exception
     */
    public static Boolean zip(String inputFileName, String zipFileName) throws Exception {
        zip(zipFileName, new File(inputFileName));
        return true;
    }

    private static void zip(String zipFileName, File inputFile) throws Exception {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
        zip(out, inputFile, "");
        out.flush();
        out.close();
    }

    private static void zip(ZipOutputStream out, File f, String base) throws Exception {
        if (f.isDirectory()) {
            File[] fl = f.listFiles();
            out.putNextEntry(new ZipEntry(base + "/"));
            base = base.length() == 0 ? "" : base + "/";
            for (int i = 0; i < fl.length; i++) {
                zip(out, fl[i], base + fl[i].getName());
            }
        } else {
            try {
                out.putNextEntry(new ZipEntry(base));
                FileInputStream in = new FileInputStream(f);
                int b;
                while ((b = in.read()) != -1) {
                    out.write(b);
                }
                in.close();
            } catch (IOException e) {
                log.error("io异常",e);
            }
        }
    }

//    public static void main(String [] temp){
//        try {
//            zip("E:\\ftl","E:\\test.zip");//你要压缩的文件夹      和  压缩后的文件
//        }catch (Exception ex) {
//            log.error("错误信息为:{}",ex);
//        }
//    }

    /**
     * @param sourceFolder
     * @param folderName
     * @param zipOutputStream
     * @throws IOException
     */
    public static void compressFolder(String sourceFolder, String folderName, ZipOutputStream zipOutputStream) throws IOException {
        File folder = new File(sourceFolder);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 压缩子文件夹
                    compressFolder(file.getAbsolutePath(), folderName + "/" + file.getName(), zipOutputStream);
                } else {
                    // 压缩文件
                    addToZipFile(folderName + "/" + file.getName(), file.getAbsolutePath(), zipOutputStream);
                }
            }
        }
    }

    private static void addToZipFile(String fileName, String fileAbsolutePath, ZipOutputStream zipOutputStream) throws IOException {
        // 创建ZipEntry对象并设置文件名
        ZipEntry entry = new ZipEntry(fileName);
        zipOutputStream.putNextEntry(entry);

        // 读取文件内容并写入Zip文件
        try (FileInputStream fileInputStream = new FileInputStream(fileAbsolutePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                zipOutputStream.write(buffer, 0, bytesRead);
            }
        }

        // 完成当前文件的压缩
        zipOutputStream.closeEntry();
    }

    public static void printXmlByDownload(EcmDownloadFileDTO ecmDownloadFileDTO,
                                          List<EcmDownloadByFileIdDTO> filesByBusiOrDoc,
                                          String pathxml,
                                          String appname) {
        try (FileOutputStream fos = new FileOutputStream(pathxml)) {
            XMLStreamWriter writer = null;
            try {
                // 创建XMLStreamWriter
                writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fos, "UTF-8");

                // 写入XML内容（和之前相同）
                writer.writeStartDocument("UTF-8", "1.0");
                writer.writeStartElement("root");

                // 写入DocInfo子元素
                writer.writeStartElement("DocInfo");
                writer.writeStartElement("APP_CODE");
                writer.writeCharacters(ecmDownloadFileDTO.getAppCode());
                writer.writeEndElement(); // 结束APP_CODE

                writer.writeStartElement("APP_NAME");
                writer.writeCharacters(appname);
                writer.writeEndElement(); // 结束APP_NAME

                writer.writeStartElement("BUSI_NO");
                writer.writeCharacters(ecmDownloadFileDTO.getBusiNo());
                writer.writeEndElement(); // 结束BUSI_NO
                writer.writeEndElement(); // 结束DocInfo

                // 写入PAGES子元素
                writer.writeStartElement("PAGES");
                for (EcmDownloadByFileIdDTO d : filesByBusiOrDoc) {
                    writer.writeStartElement("PAGE");
                    writer.writeAttribute("DOC_CODE", d.getDocCode());
                    writer.writeAttribute("DOC_NAME", d.getDocName());
                    writer.writeAttribute("FILE_ID", d.getFileId().toString());
                    writer.writeAttribute("FILE_NAME", d.getNewFileName());
                    writer.writeAttribute("PAGE_URL", d.getNewFileId() + "." + d.getFormat());
                    writer.writeAttribute("CREATE_USER", d.getCreateUser());
                    writer.writeEndElement(); // 结束PAGE
                }
                writer.writeEndElement(); // 结束PAGES

                writer.writeEndElement(); // 结束root
                writer.writeEndDocument();

            } finally {
                // 手动关闭XMLStreamWriter（确保资源释放）
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (XMLStreamException e) {
                        log.error("xml流处理异常",e);
                    }
                }
            }

        } catch (Exception e) {
            log.error("xml下载处理异常",e);
        }
    }


}
