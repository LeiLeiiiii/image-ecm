package com.sunyard.framework.common.util.conversion;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.util.StringUtils;
import org.xml.sax.InputSource;

import lombok.extern.slf4j.Slf4j;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * XML格式化工具
 *
 * @author 刘港
 */
@Slf4j
public class XmlUtils {
    private static final ConcurrentMap<Class<?>, JAXBContext> JAXB_CONTEXTS =
        new ConcurrentHashMap<Class<?>, JAXBContext>(64);

    /**
     * 对象序列化为XML字符串
     *
     * @param object obj
     * @return Result
     */
    public static String marshal(Object object) {
        try {
            Marshaller marshaller = getJaxbContext(object.getClass()).createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            StringWriter writer = new StringWriter();
            marshaller.marshal(object, writer);
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("xml生成失败", e);
        }
    }

    /**
     * 对象序列化为XMLFile
     *
     * @param object obj
     * @return Result
     */
    public static String marshalToFile(Object object, String path) {
        StringWriter writer = new StringWriter();
        FileOutputStream fos = null;
        try {
            Marshaller marshaller = getJaxbContext(object.getClass()).createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            fos = new FileOutputStream(path);
            marshaller.marshal(object, new BufferedOutputStream(fos));
            marshaller.marshal(object, writer);
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("xml生成失败", e);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException("关闭StringWriter失败", e);
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("关闭FileOutputStream失败", e);
            }
        }
    }

    /**
     * XML字符串反序列化为对象
     *
     * @param xmlContent xml的String格式
     * @param objectClass 需要转换成的obj
     * @return Result
     */
    @SuppressWarnings("unchecked")
    public static <T> T unmarshal(String xmlContent, Class<T> objectClass) {
        try {
            JAXBContext jaxbContext = getJaxbContext(objectClass);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            // 创建安全的 XMLReader（禁用外部实体）
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            // 禁用 DOCTYPE 声明
            spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            // 禁用外部实体
            spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            // 禁用外部 DTD 加载
            spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            XMLReader xmlReader = spf.newSAXParser().getXMLReader();

            // 创建 UnmarshallerHandler
            UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();

            // 使用 XMLFilter 包装，确保安全解析
            XMLFilter filter = new XMLFilterImpl(xmlReader) {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) {
                    // 返回空 InputSource，阻止加载任何外部资源
                    return new InputSource(new StringReader(""));
                }
            };

            // 配置解析器
            filter.setContentHandler(unmarshallerHandler);

            // 解析 XML
            InputSource inputSource = new InputSource(new StringReader(xmlContent));
            filter.parse(inputSource);

            // 获取解析结果
            return (T) unmarshallerHandler.getResult();
        } catch (Exception e) {
            throw new RuntimeException("XML 解析失败", e);
        }
    }

    /**
     * @param clazz 需要转换成的obj
     * @return Result
     */
    private static final JAXBContext getJaxbContext(Class<?> clazz) {
        if (null == clazz) {
            throw new RuntimeException("clazz' must not be null");
        }
        JAXBContext context = JAXB_CONTEXTS.get(clazz);
        if (context == null) {
            try {
                context = JAXBContext.newInstance(clazz);
                JAXB_CONTEXTS.putIfAbsent(clazz, context);
            } catch (Exception e) {
                log.error("系统异常",e);
                throw new RuntimeException(e);
            }
        }
        return context;
    }

    /**
     * 获取压缩的xml
     * 
     * @param xml xml数组
     * @return 压缩后的
     */
    public static String getXml(String xml) {
        String[] xmlStr = xml.split("<");
        StringBuffer upperBuffer = new StringBuffer();

        for (String str : xmlStr) {
            if (StringUtils.hasText(str)) {
                upperBuffer.append("<").append(str.substring(0, 1).toLowerCase()).append(str.substring(1));
            }
        }
        String[] upperStr = upperBuffer.toString().split("/");
        StringBuffer resultXml = new StringBuffer();
        for (int i = 0; i < upperStr.length; i++) {
            if (i != 0) {
                resultXml.append("/").append(upperStr[i].substring(0, 1).toLowerCase())
                    .append(upperStr[i].substring(1));
            } else {
                resultXml.append(upperStr[i]);
            }
        }
        return resultXml.toString();
    }

    /**
     * String转换为XML对象
     * 
     * @param message string
     * @return Result
     */
    public static Document stringConvertDoc(String message) {
        SAXReader reader = new SAXReader();
        Document doc = null;
        try {
            doc = reader.read(new StringReader(message));
        } catch (DocumentException e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        }
        return doc;
    }

    /**
     * 将组装好的XML信息写入文件
     * 
     * @param doc 文档
     * @param filePath 文件路径
     */
    public static void createXml(Document doc, String filePath) {
        XMLWriter writer = null;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
            writer = new XMLWriter(out);
            writer.write(doc);

        } catch (IOException e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 将xmlStr写到文件里面
     * 
     * @param xml
     * @param file
     */
    public static void xmlStrConvertFile(String xml, File file) {
        try {
            // Document document = stringConvertDoc(xml);

            // 解析 XML 字符串
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document parse = builder.parse(new InputSource(new StringReader(xml)));

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            DOMSource source = new DOMSource(parse);
            StreamResult result = new StreamResult(file);

            transformer.transform(source, result);
        } catch (Exception e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        }
    }
}
