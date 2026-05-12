package com.sunyard.ecm.util;

import com.sunyard.ecm.dto.GrantAccessDTO;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.Key;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @Title: RSA_Encrypt.java
 * @Description: 权限管理相关方法，ras解码，mac地址获取，ip地址获取
 * @Package com.sunyard.insurance.ecm.common
 * @author sbc
 * @create 2015-5-13 上午11:23:20
 * @modUser TODO
 * @modTime 2015-5-13 上午11:23:20
 *          <p>
 *          modify：TODO
 *          </p>
 * @version 1.0
 */
@Slf4j
public class EncryptFuncUtils {
	/** 指定加密算法为DESede */
	private static String ALGORITHM = "RSA";

	/**
	 * 解密算法 cryptograph:密文
	 */
	public static String decrypt(InputStream FilePath, String cryptograph) throws Exception {
		/** 将文件中的私钥对象读出 */
		//new ObjectInputStream(FilePath);
		ObjectInputStream ois = new AntObjectInputStream(FilePath);
		Key key = (Key) ois.readObject();
		/** 得到Cipher对象对已用公钥加密的数据进行RSA解密 */
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] b1 = Base64.getMimeDecoder().decode(cryptograph);
		/** 执行解密操作 */
		byte[] byteAll = new byte[] {};
		for (int i = 0; i < b1.length; i += 128) {
			byte[] doFinal = cipher.doFinal(Arrays.copyOfRange(b1, i, i + 128));
			byteAll = Arrays.copyOf(byteAll, byteAll.length + doFinal.length);
			System.arraycopy(doFinal, 0, byteAll, byteAll.length - doFinal.length, doFinal.length);
		}

		return new String(byteAll,"utf-8");
	}

	/**
	 * @throws SocketException
	 * @Title: getIpAddress
	 * @Description: 获取当前ip地址
	 * @param @return
	 * @param @throws UnknownHostException
	 * @return String
	 * @throws
	 */
	public static InetAddress getIpAddress() throws Exception {
		InetAddress ip = null;
		boolean isWindowsOS = false;
		String osName = System.getProperty("os.name");
		if (osName.toLowerCase().indexOf("windows") > -1) {
			isWindowsOS = true;
		}
		// 如果是Windows操作系統
		if (isWindowsOS) {
			ip = InetAddress.getLocalHost();
		}
		// 如果是Linux操作系統
		else {
			boolean bFindIP = false;
			Enumeration<NetworkInterface> netInterfaces = (Enumeration<NetworkInterface>) NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				if (bFindIP) {
					break;
				}
				NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
				// ----------特定情況，可以考慮用ni.getName判斷
				// 遍歷所有ip
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while (ips.hasMoreElements()) {
					ip = (InetAddress) ips.nextElement();
					if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress() // 127.開頭的都是lookback地址
							&& ip.getHostAddress().indexOf(":") == -1) {
						bFindIP = true;
						break;
					}
				}
			}
		}
		return ip;
	}

	/**
	 * 获取当前操作系统名称. return 操作系统名称 例如:windows xp,linux 等.
	 */
	public static String getOSName() {
		return System.getProperty("os.name").toLowerCase();
	}

	/**
	 * 获取unix网卡的mac地址. 非windows的系统默认调用本方法获取. 如果有特殊系统请继续扩充新的取mac地址方法.
	 *
	 * @return mac地址
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	public static String getUnixMACAddress() throws Exception {
		String mac = null;
		BufferedReader bufferedReader = null;
		InputStreamReader inputReader = null;
		Process process = null;
		try {
			// linux下的命令，一般取eth0作为本地主网卡,如果没有，则根据ip地址重新获取
			process = Runtime.getRuntime().exec("ifconfig eth0");
			// 显示信息中包含有mac地址信息
			inputReader = new InputStreamReader(process.getInputStream());
			bufferedReader = new BufferedReader(inputReader);
			String line = null;
			int index = -1;
			while ((line = bufferedReader.readLine()) != null) {
				// 寻找标示字符串[hwaddr]
				index = line.toLowerCase().indexOf("hwaddr");
				if (index >= 0) {// 找到了
					// 取出mac地址并去除2边空格
					mac = line.substring(index + "hwaddr".length() + 1).trim();
					break;
				}
			}
		} catch (IOException e) {
			log.error("获取unix网卡的mac地址:IOException",e);
			throw new Exception(e);
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException e1) {
				log.error("获取unix网卡的mac地址，关闭流:IOException",e1);
				throw new Exception(e1);
			}
			try {
				if (inputReader != null) {
					inputReader.close();
				}
			} catch (IOException e2) {
				log.error("获取unix网卡的mac地址，关闭流:IOException",e2);
				throw new Exception(e2);
			}
			bufferedReader = null;
			process = null;
		}
		//根据ip获取mac地址
		if(mac==null||"".equals(mac)){
			mac = getMacAddressByIp(getIpAddress());
		}
		return mac;
	}

	/**
	 * 获取widnows网卡的mac地址.
	 *
	 * @return mac地址
	 */
	public static String getWindowsMACAddress() throws Exception{
		String mac = null;
		BufferedReader bufferedReader = null;
		InputStreamReader inputReader = null;
		Process process = null;
		try {
			// windows下的命令，显示信息中包含有mac地址信息
			process = Runtime.getRuntime().exec("ipconfig /all");
			inputReader = new InputStreamReader(process.getInputStream());
			bufferedReader = new BufferedReader(inputReader);
			String line = null;
			int index = -1;
			while ((line = bufferedReader.readLine()) != null) {
				System.out.println(line);
				// 寻找标示字符串[physical
				index = line.toLowerCase().indexOf("physical address");

				if (index >= 0) {// 找到了
					index = line.indexOf(":");// 寻找":"的位置
					if (index >= 0) {
						// 取出mac地址并去除2边空格
						mac = line.substring(index + 1).trim();
					}
					break;
				}
			}
		} catch (IOException e) {
			log.error("获取widnows网卡的mac地址:IOException",e);
			throw new Exception(e);
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException e1) {
				log.error("获取unix网卡的mac地址，关闭流:IOException",e1);
				throw new Exception(e1);
			}
			try {
				if (inputReader != null) {
					inputReader.close();
				}
			} catch (IOException e2) {
				log.error("获取unix网卡的mac地址，关闭流:IOException",e2);
				throw new Exception(e2);
			}
			bufferedReader = null;
			process = null;
		}

		return mac;
	}

	/**
	 * windows 7 专用 获取MAC地址
	 *
	 * @return
	 * @throws Exception
	 */
	public static String getWin7MACAddress() throws Exception {

		// 获取本地IP对象
		InetAddress ia = InetAddress.getLocalHost();
		// 获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。
		byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();

		// 下面代码是把mac地址拼装成String
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < mac.length; i++) {
			if (i != 0) {
				sb.append("-");
			}
			// mac[i] & 0xFF 是为了把byte转化为正整数
			String s = Integer.toHexString(mac[i] & 0xFF);
			sb.append(s.length() == 1 ? 0 + s : s);
		}

		// 把字符串所有小写字母改为大写成为正规的mac地址并返回
		return sb.toString().toUpperCase();
	}

	/**
	 * @throws SocketException
	 * @Title: getMacAddressByIp
	 * @Description: 根据ip获取mac地址
	 * @param @param ip
	 * @param @return
	 * @return String
	 * @throws
	 */
	public static String getMacAddressByIp(InetAddress ip) throws Exception{
		StringBuilder sb = new StringBuilder();
		NetworkInterface network = NetworkInterface.getByInetAddress(ip);
		if (network != null) {
			byte[] mac = network.getHardwareAddress();
			if (mac != null) {
				for (int i = 0; i < mac.length; i++) {
					sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
				}
			}
		}
//		return sb.toString();
		return "18-26-49-BB-32-9E";
	}

	public static String getMacAddress() throws Exception {
		String os = getOSName();
		String mac = "";
		if (os.toLowerCase(Locale.ENGLISH).equals("windows 7".toLowerCase(Locale.ENGLISH))) {
			mac = getWin7MACAddress();
		} else if (os.toLowerCase(Locale.ENGLISH).startsWith("windows".toLowerCase(Locale.ENGLISH))) {
			if (os.toLowerCase(Locale.ENGLISH).indexOf("vista".toLowerCase(Locale.ENGLISH)) != -1) {
				String version = System.getProperty("os.version");
				if (version.equals("6.1")) {
					mac = getWin7MACAddress();
				} else {
					mac = getWindowsMACAddress();
				}
			} else {
				// 本地是windows
				mac = getWindowsMACAddress();
			}
		} else {
			// 本地是非windows系统 一般就是unix
			mac = getUnixMACAddress();
		}
		return mac;
	}

	public static String getReferer(HttpServletRequest request){
		Enumeration headers = request.getHeaderNames();
		String referer = "";
		while (headers.hasMoreElements()) {
			String header = (String) headers.nextElement();
			if (header.equalsIgnoreCase("referer")) {
				referer = request.getHeader(header);
				break;
			}
		}
		if("".equals(referer)){
			referer = "SunICMS";
		}
		return referer;
	}

	public static GrantAccessDTO checkReferer(HttpServletRequest request)throws Exception{
		String referer = getReferer(request);
        log.info("referer值为：{}", referer);
		//判断referer是否已经授权
		if("".equals(referer)){
			referer = "SunICMS";
		}
		if(referer.indexOf("?") != -1){
			referer = referer.substring(0, referer.indexOf("?"));
		}
        log.info("截取后referer值为：{}", referer);
		//判断referer是否在授权文件中
		GrantAccessDTO accessMacth = null;
		for(int i = 0; i < AnalysisGrantToolUtils.grantAccessList.size(); i++){
			GrantAccessDTO access = AnalysisGrantToolUtils.grantAccessList.get(i);
			List<String> recordReferer = access.getReferer();
			if(recordReferer == null || recordReferer.size() == 0){
				throw new Exception("3@@没有获取到接入系统"+access.getAccessName()+"的配置referer，请联系管理员检查授权文件！");
			}
			for (int j = 0; j < recordReferer.size(); j++) {
				int index = referer.indexOf(recordReferer.get(j));
				if(index != -1){
					accessMacth = access;
					break;
				}
			}
			if(accessMacth != null){
				break;
			}
		}
		return accessMacth;
	}

	public static Map<String,String> analysisParam(String data) throws ServletException{
		try {
			Map<String,String> map = new HashMap<String, String>();
			String[] requestArr = data.split("&");
			for(String str : requestArr) {
				String code = str.substring(0, str.indexOf("="));
				String requestData = str.substring(str.indexOf("=")+1);
				map.put(code, requestData);
			}
			return map;
		} catch (Exception e) {
			throw new ServletException("参数解析异常，请检查！参数："+data, e);
		}
	}

}
