package com.sunyard.framework.ofd.util;

import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.ofdrw.core.action.Actions;
import org.ofdrw.core.action.CT_Action;
import org.ofdrw.core.action.EventType;
import org.ofdrw.core.action.actionType.actionGoto.CT_Dest;
import org.ofdrw.core.action.actionType.actionGoto.DestType;
import org.ofdrw.core.action.actionType.actionGoto.Goto;
import org.ofdrw.core.basicStructure.doc.bookmark.Bookmark;
import org.ofdrw.core.basicStructure.doc.bookmark.Bookmarks;
import org.ofdrw.core.basicStructure.outlines.CT_OutlineElem;
import org.ofdrw.core.basicStructure.outlines.Outlines;
import org.ofdrw.core.basicStructure.pageTree.Page;
import org.ofdrw.core.basicStructure.pageTree.Pages;
import org.ofdrw.core.basicType.ST_ID;
import org.ofdrw.gm.cert.PKCS12Tools;
import org.ofdrw.gm.ses.v4.SESeal;
import org.ofdrw.layout.OFDDoc;
import org.ofdrw.layout.edit.Attachment;
import org.ofdrw.reader.OFDReader;
import org.ofdrw.sign.NumberFormatAtomicSignID;
import org.ofdrw.sign.OFDSigner;
import org.ofdrw.sign.SignCleaner;
import org.ofdrw.sign.SignMode;
import org.ofdrw.sign.signContainer.SESV4Container;
import org.ofdrw.sign.stamppos.NormalStampPos;
import org.ofdrw.sign.verify.OFDValidator;
import org.ofdrw.sign.verify.container.SESV4ValidateContainer;
import org.ofdrw.tool.merge.OFDMerger;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.List;

/**
 * @author HRH
 * @date 2023/7/14
 * @describe 处理ofd文件
 */
@Slf4j
public class OfdUtils {

    /**
     * 合并ofd文件
     *
     * @param list 文件路集
     * @param outPath 输出文件路径
     */
    public static void mergeOfd(List<String> list, String outPath) {
        // 1. 提供合并文件输出位置。
        Path dst = Paths.get(outPath);
        // 3. 创建合并对象
        OFDMerger ofdMerger = new OFDMerger(dst);
        try {
            for (String path : list) {
                Path d1Path = Paths.get(path);
                ofdMerger.add(d1Path);
            }
            ofdMerger.close();
        } catch (Exception e) {
            log.error("mergeOfd " + e);
            throw new SunyardException(ResultCode.SYSTEM_BUSY_ERROR, e.toString());
        }
    }

    /**
     * 添加附件
     *
     * @param ofdOutPath 输出文件路径
     * @param ofdPath 原odf文件流路径
     * @param attachPath 附件文件路径
     */
    public static void addAttachments(String ofdOutPath, String ofdPath, String attachPath) {
        File file = new File(attachPath);
        if (ObjectUtils.isEmpty(file)) {
            log.info("目标文件不存在");
            throw new SunyardException(ResultCode.SYSTEM_BUSY_ERROR, attachPath + "目标文件不存在");
        }
        try {
            OFDReader reader = new OFDReader(Paths.get(ofdPath));
            OFDDoc ofdDoc = new OFDDoc(reader, Paths.get(ofdOutPath));
            Attachment attachment = new Attachment(file.getName().split("\\.")[0], Paths.get(attachPath));
            ofdDoc.addAttachment(attachment);
            ofdDoc.close();
            reader.close();
        } catch (Exception e) {
            log.error("ofdRW addAttachments", e);
            throw new SunyardException(ResultCode.SYSTEM_BUSY_ERROR, e.toString());
        }
    }

    /**
     * 添加大纲 （父级大纲名如果重复，会在匹配到的第一个下加大纲，从上往下）
     *
     * @param ofdOutPath  ofd文件输出位置
     * @param ofdPath     ofd文件位置
     * @param name        大纲名
     * @param pageNum     跳转对应的页码
     * @param parentTitle 父级大纲名
     */
    public static void addOutlines(String ofdOutPath, String ofdPath, String name, Integer pageNum, String parentTitle) {
        try {
            OFDReader reader = new OFDReader(Paths.get(ofdPath));
            OFDDoc ofdDoc = new OFDDoc(reader, Paths.get(ofdOutPath));

            org.ofdrw.core.basicStructure.doc.Document ofdDocument = ofdDoc.getOfdDocument();
            Pages pages = ofdDocument.getPages();
            Page pageByIndex = pages.getPageByIndex(pageNum - 1);
            ST_ID id = pageByIndex.getID();

            CT_Action ctAction = new CT_Action().setEvent(EventType.CLICK).setAction(new Goto(new CT_Dest().setType(DestType.XYZ).setPageID(id.ref())));
            Actions actions = new Actions().addAction(ctAction);
            CT_OutlineElem ctOutlineElem = new CT_OutlineElem().setTitle(name).setActions(actions);
            // 判断是否已经存在大纲
            Outlines outlines = ofdDocument.getOutlines();
            if (outlines == null) {
                outlines = new Outlines();
                ofdDocument.setOutlines(outlines);
            }

            if (parentTitle != null) {
                // 找到指定的父节点
                CT_OutlineElem parentElem = findOutlineElemByTitle(outlines.getOutlineElems(), parentTitle);
                if (parentElem != null) {
                    parentElem.addOutlineElem(ctOutlineElem);
                } else {
                    log.info("未找到对应的大纲");
                    throw new SunyardException(ResultCode.SYSTEM_BUSY_ERROR, "未找到对应的大纲");
                }
            } else {
                outlines.addOutlineElem(ctOutlineElem);
            }
            ofdDoc.close();
            reader.close();
        } catch (Exception e) {
            log.error("ofdrw addOutlines出错", e);
            throw new SunyardException(ResultCode.SYSTEM_BUSY_ERROR, e.toString());
        }
    }

    /**
     * 添加书签
     *
     * @param ofdOutPath ofd输出位置
     * @param ofdPath    ofd文件位置
     * @param name       书签名
     * @param pageNum    对应跳转的页码数
     */
    public static void addBookmark(String ofdOutPath, String ofdPath, String name, Integer pageNum) {
        try {
            OFDReader reader = new OFDReader(Paths.get(ofdPath));
            OFDDoc ofdDoc = new OFDDoc(reader, Paths.get(ofdOutPath));

            org.ofdrw.core.basicStructure.doc.Document ofdDocument = ofdDoc.getOfdDocument();
            Pages pages = ofdDocument.getPages();
            Page pageByIndex = pages.getPageByIndex(pageNum - 1);
            ST_ID id = pageByIndex.getID();
            Bookmark bookmark = new Bookmark().setBookmarkName(name)
                    .setDest(new CT_Dest().setType(DestType.XYZ).setPageID(id.ref()));
            // 判断是否已经存在书签
            Bookmarks bookmarks = ofdDocument.getBookmarks();
            if (bookmarks == null) {
                bookmarks = new Bookmarks();
                ofdDocument.setBookmarks(bookmarks);
            }
            bookmarks.addBookmark(bookmark);
            ofdDoc.close();
            reader.close();
        } catch (Exception e) {
            log.error("ofdrw addBookmark出错", e);
            throw new SunyardException(ResultCode.SYSTEM_BUSY_ERROR, e.toString());
        }
    }

    /**
     * 清除电子签名
     * 不清楚的可以参考ofdrw
     *
     * @param ofdPath    ofd文件路径
     * @param ofdOutPath ofd文件输出路径
     */
    public static void cleanerSinger(String ofdPath, String ofdOutPath) {
        try {
            Path src = Paths.get(ofdPath);
            Path out = Paths.get(ofdOutPath);
            // 1. 创建 OFD解析器
            OFDReader reader = new OFDReader(src);
            // 2. 构造签名清理工具
            SignCleaner sCleaner = new SignCleaner(reader, out);
            // 3. 清空所有电子签名
            sCleaner.clean();
            // 4. 关闭 Reader，这里使用 try finally 语法自动 Close Reader
            log.info("生成文档位置: " + out.toAbsolutePath());
        } catch (Exception e) {
            log.error("cleanerSinger出错" ,e);
            throw new SunyardException(ResultCode.SYSTEM_BUSY_ERROR, e.toString());
        }
    }

    /**
     * 添加数字签名
     * 不清楚的可以参考ofdrw
     *
     * @param userP12Path p12文件 公钥
     * @param sealPath    esl 文件
     * @param ofdPath     ofd文件路径
     * @param ofdOutPath  ofd输出文件路径
     */
    public static void addSigner(String userP12Path, String sealPath, String ofdPath, String ofdOutPath) {
        try {
            Path userP12Path1 = Paths.get(userP12Path);
            Path sealPath1 = Paths.get(sealPath);

            //alias psw 根据实际情况修改
            PrivateKey prvKey = PKCS12Tools.ReadPrvKey(userP12Path1, "private", "密码");
            Certificate signCert = PKCS12Tools.ReadUserCert(userP12Path1, "private", "777777");
            SESeal seal = SESeal.getInstance(Files.readAllBytes(sealPath1));

            Path src = Paths.get(ofdPath);
            Path out = Paths.get(ofdOutPath);
            OFDReader readerSrc = new OFDReader(src);
            OFDSigner signer = new OFDSigner(readerSrc, out, new NumberFormatAtomicSignID());
            SESV4Container signContainer = new SESV4Container(prvKey, seal, signCert);
            // 2. 设置签名模式
            signer.setSignMode(SignMode.WholeProtected);
            // 3. 设置签名使用的扩展签名容器
            signer.setSignContainer(signContainer);
            // 4. 设置显示位置
            signer.addApPos(new NormalStampPos(1, 50, 50, 40, 40));
            // 5. 执行签名
            signer.exeSign();

            OFDReader readerOut = new OFDReader(out);
            OFDValidator validator = new OFDValidator(readerOut);
            validator.setValidator(new SESV4ValidateContainer());
            validator.exeValidate();
            log.info("addSigner>> 验证通过");
        } catch (Exception e) {
            log.error("addSigner出错" ,e);
            throw new SunyardException(ResultCode.SYSTEM_BUSY_ERROR, e.toString());
        }
    }


    /**
     * 根据节点title递归查找大纲元素  如果重复，就取第一个匹配到的
     *
     * @param outlineElems outlineElems
     * @param title title
     * @return Result
     */
    private static CT_OutlineElem findOutlineElemByTitle(List<CT_OutlineElem> outlineElems, String title) {
        for (CT_OutlineElem outlineElem : outlineElems) {
            if (outlineElem.getTitle().equals(title)) {
                return outlineElem;
            }
            List<CT_OutlineElem> childOutlineElems = outlineElem.getOutlineElems();
            if (childOutlineElems != null && !childOutlineElems.isEmpty()) {
                CT_OutlineElem foundElem = findOutlineElemByTitle(childOutlineElems, title);
                if (foundElem != null) {
                    return foundElem;
                }
            }
        }
        return null;
    }
}
