package com.sunyard.ecm.oldToNew.socket.client;

import com.sunyard.insurance.ecm.socket.client.SocketTransClient;


public class AutoScanApi {

	private String ip;
	private int socketPort;
	private String licenseStr;// 授权码
	private SocketTransClient socketTransClient = null;
	private String format = "";
	private int socketTimeOut = 1200000;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getSocketPort() {
		return socketPort;
	}

	public void setSocketPort(int socketPort) {
		this.socketPort = socketPort;
	}

	public String getLicenseStr() {
		return licenseStr;
	}

	public void setLicenseStr(String licenseStr) {
		this.licenseStr = licenseStr;
	}

	public SocketTransClient getSocketTransClient() {
		return socketTransClient;
	}

	public void setSocketTransClient(SocketTransClient socketTransClient) {
		this.socketTransClient = socketTransClient;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		if(!format.equals("")){
			this.format = format.toLowerCase();
		}
	}

	public int getSocketTimeOut() {
		return this.socketTimeOut;
	}

	public void setSocketTimeOut(int socketTimeOut) {
		this.socketTimeOut = socketTimeOut;
	}

	public AutoScanApi(String ip, int port, String licenseStr) {
		this.ip = ip;
		this.socketPort = port;
		this.licenseStr = licenseStr;
	}

	public String ScanImageFile(String appCode,String zipPath) throws Exception {
		SocketTransClient socketClient = new SocketTransClient(this.ip, this.socketPort,this.format);
		socketClient.setSocketTimeOut(this.socketTimeOut);
		return socketClient.uploadBatch(appCode, zipPath, this.licenseStr);
	}

	/**
	 *@Description
	 *
	 *@param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String zipPath = "D:\\81101\\4.zip";
		try {
			AutoScanApi autoScanApi = new AutoScanApi("127.0.0.1",8081,"SunICMS#uroahf63n59ch6s8bn5m58sg");
			autoScanApi.setFormat("xml");
			String returnMsg = autoScanApi.ScanImageFile("jyy", zipPath);
			System.out.println("returnMsg == " + returnMsg);
		} catch (Exception e) {
			
		}
	}
}
