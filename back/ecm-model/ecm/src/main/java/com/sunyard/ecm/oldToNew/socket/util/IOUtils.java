package com.sunyard.ecm.oldToNew.socket.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.FileChannel;

public class IOUtils {

	private static final Logger log =  LoggerFactory.getLogger(IOUtils.class);

	public static void closeInputStream(InputStream in){
		if (in != null) {
			try {
				in.close();
			}catch (IOException e){
				log.error("关闭文件流异常!",e);
			} finally {
				in = null;
			}
		}
	}

	public static void closeOutputStream(OutputStream out){
		if (out != null) {
			try {
				out.flush();
			}catch (IOException e){
			} finally {
				try {
					out.close();
				}catch (IOException e){
					log.error("关闭文件流异常!",e);
				} finally {
					out = null;
				}
			}
		}
	}

	public static void closeFileChannel(FileChannel channel){
		if (channel != null) {
			try {
				if (channel.isOpen()) {
					channel.close();
				}
			}catch (IOException e){
				log.error("关闭文件流异常!",e);
			} finally {
				channel = null;
			}
		}
	}

	public static void closeWriterStream(Writer writer){
		if (writer != null) {
			try {
				writer.flush();
			}catch (IOException e){
			} finally {
				try {
					writer.close();
				}catch (IOException e){
					log.error("关闭文件流异常!",e);
				} finally {
					writer = null;
				}
			}
		}
	}
	public static void closeReaderStream(Reader reader){
		if (reader != null) {
			try {
				reader.close();
			}catch (IOException e){
				log.error("关闭文件流异常!",e);
			}finally {
				reader = null;
			}
		}
	}


}
