package com.sunyard.ecm.oldToNew.socket.client;

import com.sunyard.ecm.oldToNew.socket.util.StringUtil;
import com.sunyard.insurance.base.util.crypto.Base64WithAccess;
import com.sunyard.insurance.ecm.socket.util.MD5Util;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Date;

public class SocketTransClient {

	private Socket socket;
	private String ip;
	private int port;
	private String format="";
	// 输出字符
	private PrintWriter printWriter = null;
	// 接受字符
	private BufferedReader inStream = null;
	private BufferedWriter bufferWriter = null;
	private OutputStreamWriter outWriter = null;
	private InputStreamReader inStreamReader = null;
	// 输出流
	private DataOutputStream outputStream = null;
	// 缓冲区大小默认65536
	private int transBufferSize = 65536;

	// 客户端超时时间，默认120000
	private int socketTimeOut = 120000;

	public void setSocketTimeOut(int socketTimeOut) {
		this.socketTimeOut = socketTimeOut;
	}

	public SocketTransClient(String ip, int port) throws Exception {
		this.ip = ip;
		this.port = port;
	}

	public SocketTransClient(String ip, int port,String format) throws Exception {
		this.ip = ip;
		this.port = port;
		this.format = format;
	}
	public String uploadBatch(String appCode, String zipPath, String licenseStr) throws Exception {
		String respCode = "1";
		String respMsg = "上传成功";
		File zipFile = new File(StringUtil.pathManipulation(zipPath));
		if (!zipFile.exists()) {
			respCode = "ERROR001";
			respMsg = "文件不存在";
			if(format.equals("xml")){
				Document document = DocumentHelper.createDocument();
				Element rootElement = document.addElement("root");
				rootElement.addElement("RESPONSE_CODE").setText("ERROR001");
				rootElement.addElement("RESPONSE_MSG").setText("文件不存在");
				return document.asXML().toString().replaceAll("\\n","");
			}else{
				return "ERROR001@" + zipPath + "文件不存在";
			}
		}
		long fileSize = zipFile.length();

		try {
			this.socket = new Socket(this.ip, this.port);
			// 高可靠性，低延迟
			socket.setTrafficClass(0x04 | 0x10);
			// 客户端超时时间，默认120000
			this.socket.setSoTimeout(socketTimeOut);

		} catch (Exception e) {
			throw new Exception("连接服务器" + ip + "端口" + port + "失败" + e);
		}

		// 返回代码
		String resultStr = "";
		String[] resultContent = null;
		try {
			outWriter = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
			bufferWriter =new BufferedWriter(outWriter);
			printWriter = new PrintWriter(bufferWriter,true);

			outputStream = new DataOutputStream(socket.getOutputStream());
			inStreamReader = new InputStreamReader(socket.getInputStream(),"UTF-8");
			inStream = new BufferedReader(inStreamReader);

			System.out.println("开始自动上传交易" + new Date());
			String zipMD5 = MD5Util.getFileMD5(zipPath).toLowerCase();
//			printWriter.println("ECM0018:" + appCode + ":" + licenseStr+":"+zipFile.getName()+":"+fileSize+":"+transBufferSize+":"+zipMD5+"@"+this.ip);
			String sendStr = "ECM0018:" + appCode + ":" + licenseStr+":"+zipFile.getName()+":"+fileSize+":"+transBufferSize+":"+zipMD5+":"+format+"@"+this.ip;
			String licenseKey = licenseStr.split("#")[1];
			String md5 = MD5Util.encryptHmacMd5Str(licenseKey, sendStr);
			sendStr += ":"+md5;
			printWriter.println(Base64WithAccess.encode(sendStr,"utf-8"));
			resultStr = inStream.readLine();
			if(resultStr == null){
				respCode = "ERROR002";
				respMsg = "发送上传请求后没有收到服务端的响应";
				return "ERROR002@发送上传请求后没有收到服务端的响应。";
			}
			resultContent = resultStr.split(":");
			if (resultContent[0].equals("1")) {
				if("-1".equals(resultContent[1])){
					System.out.println("第三方上传相同MD5的zip文件已存在，本次不需要发送zip，第三方上传开始...");
				}
				// 收到响应，发送文件流
				if("-1".equals(resultContent[1]) || this.sendFile(zipPath, Long.parseLong(resultContent[1]))) {
					//等待处理结果
					String resultString = inStream.readLine();
					String[] retContent = resultString.split(":");
					resultStr = retContent[0];
					if("1".equals(resultStr)) {
						if(!format.equals("") && format.equals("xml")){
							System.out.println("批次文件: " + zipFile.getName() + "上传成功");
							return resultString.substring(resultString.indexOf(":")+1);
						}else{
							System.out.println("批次文件: " + zipFile.getName() + "上传成功");
							return "1@上传成功";
						}
					}if("-6".equals(resultStr)) {
						System.out.println("批次文件: " + zipFile.getName() + "上传失败，文件MD5码校验未通过");
						respCode = "ERROR006";
						respMsg = "上传失败，文件MD5码校验未通过";
					}if("-7".equals(resultStr)){
						System.out.println("批次文件: " + zipFile.getName() + "上传失败，服务器返回，" + "该批次无影像信息");
						respCode = "ERROR007";
						respMsg = "该批次无影像信息";
					}else {
						System.out.println("批次文件: " + zipFile.getName() + "上传失败，服务器返回，" + resultStr);
						respCode = "ERROR004";
						respMsg = "上传失败," + resultStr;
					}
				} else {
					respCode = "ERROR003";
					respMsg = "发送ZIP压缩文件失败";
				}
			} else if(resultStr.equals("-1")){
				respCode = "ERROR005";
				respMsg = "接入系统校验未通过";
			}else if(!"".equals(resultStr)) {
				respCode = "ERROR004";
				respMsg = "上传请求，服务器处理失败,"+resultStr;
			}else{
				respCode = "ERROR002";
				respMsg = "发送上传请求后没有收到服务端的响应";
			}

		} catch (UnsupportedEncodingException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}catch (Exception e) {
			throw e;
		} finally {
			try {
				if (null != printWriter) {
					printWriter.println("0009:true");
					printWriter.close();
				}
				if (null != bufferWriter) {
					bufferWriter.close();
				}
				if (null != outWriter) {
					outWriter.close();
				}
				if (null != inStream) {
					inStream.close();
				}
				if (null != inStreamReader ) {
					inStreamReader .close();
				}
				if (null != outputStream) {
					outputStream.flush();
					outputStream.close();
				}
				if (null != socket) {
					socket.close();
				}
			} catch (IOException e) {
				System.out.println("socket客户端资源关闭异常" + e);
			}
		}

		Document document = DocumentHelper.createDocument();
		Element rootElement = document.addElement("root");
		rootElement.addElement("RESPONSE_CODE").setText(respCode);
		rootElement.addElement("RESPONSE_MSG").setText(respMsg);
		if(format.equals("xml")){
			return document.asXML().toString().replaceAll("\\n","");
		}else{
			return respCode+"@"+respMsg;
		}
	}

	// 发送文件流
	public boolean sendFile(String filePath, Long position) throws Exception {
		long beginTime = System.currentTimeMillis();
		RandomAccessFile fileInputStream = null;
		System.out.println("发送文件,起始位置："+position);
		try {
			boolean transFlag = true;
			long fileSize = new File(StringUtil.pathManipulation(filePath)).length();
			long availSize = fileSize - position;
			if(availSize < 0){
				System.out.println("【断点续传】相同文件名zip包，服务器保存文件和本次上传文件不是同一文件，请检查。");
				return false;
			}
			fileInputStream = new RandomAccessFile(filePath, "r");
			fileInputStream.seek(position);
			while (transFlag) {
				int bufSize = transBufferSize;
				if (availSize < transBufferSize) {
					bufSize = Integer.parseInt(Long.toString(availSize));
					transFlag = false;
				}

				byte[] buf = new byte[bufSize];
				fileInputStream.read(buf);
				outputStream.write(buf);
				availSize -= transBufferSize;
			}

			System.out.println("传输文件[" + filePath + "]耗时:"
					+ (System.currentTimeMillis() - beginTime));
			return true;
		} catch (NumberFormatException e) {
			throw e;
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (null != fileInputStream) {
					fileInputStream.close();
				}
			} catch (IOException e) {
				System.out.println("sockeClient关闭资源异常" + e);
			}
		}
	}

//	public String getSeverIp() throws Exception{
//		InetAddress netAddress = InetAddress.getLocalHost();
//		return netAddress.getHostAddress();
//	}
//
//	public String getServerMac() throws Exception{
//		//获取网卡，获取地址
//		byte[] mac = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
//		StringBuffer sb = new StringBuffer("");
//		for(int i=0; i<mac.length; i++) {
//			if(i!=0) {
//				sb.append("-");
//			}
//			//字节转换为整数
//			int temp = mac[i]&0xff;
//			String str = Integer.toHexString(temp);
//			if(str.length()==1) {
//				sb.append("0"+str);
//			}else {
//				sb.append(str);
//			}
//		}
//		return sb.toString().toUpperCase();
//	}
}
