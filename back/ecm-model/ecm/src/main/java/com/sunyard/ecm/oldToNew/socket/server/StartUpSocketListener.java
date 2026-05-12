package com.sunyard.ecm.oldToNew.socket.server;

import com.sunyard.ecm.oldToNew.service.ApiService;
import com.sunyard.ecm.oldToNew.socket.constant.OldToNewConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class StartUpSocketListener {

	public static SocketListenService socketListenService = null;

	@Value("${TCPListenPort:8881}")
	private Integer TCPListenPort;

	@Value("${TCPPoolSize:50}")
	private Integer TCPPoolSize;

    //一次读写文件大小
	@Value("${TransBufferSize:65536}")
	private Integer TransBufferSize;

	//设置socket等待超时时间2分钟
	@Value("${SocketSoTimeOut:120000}")
	private Integer SocketSoTimeOut;

	//输入缓冲区大小
	@Value("${RecvSocketBufferSize:32768}")
	private Integer RecvSocketBufferSize;

	//输出缓冲区大小
	@Value("${SendSocketBufferSize:32768}")
	private Integer SendSocketBufferSize;

	//输出缓冲区大小
	@Value("${receivePath:/app/deploy/imagefiles/ecmtemp/outher}")
	private String receivePath;

	//清理上传文件的周期
	@Value("${upCacheTime:3}")
	private Integer upCacheTime;

	@Resource
	private ApiService apiService;


	@Bean
	public void initListenerPort() {
		socketListenService = new SocketListenService(TCPListenPort,TCPPoolSize,apiService);
		socketListenService.start();
	}

	@Bean
	public void constant() {
		OldToNewConstant.TransBufferSize = TransBufferSize;
		OldToNewConstant.SocketSoTimeOut = SocketSoTimeOut;
		OldToNewConstant.RecvSocketBufferSize = RecvSocketBufferSize;
		OldToNewConstant.SendSocketBufferSize = SendSocketBufferSize;
		OldToNewConstant.receivePath = receivePath;
		OldToNewConstant.upCacheTime = upCacheTime;
	}

}
