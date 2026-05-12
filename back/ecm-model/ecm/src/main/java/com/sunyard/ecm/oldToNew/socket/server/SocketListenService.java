package com.sunyard.ecm.oldToNew.socket.server;

import com.sunyard.ecm.oldToNew.service.ApiService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class SocketListenService extends Thread {

	private ExecutorService pool = null;
	private ServerSocket serverListenSocket = null;
	private int port;
	private int TCPPoolSize;
	private ApiService apiService=null;
	public SocketListenService(int port, int TCPPoolSize, ApiService apiService) {
		this.port = port;
		this.TCPPoolSize = TCPPoolSize;
		this.apiService = apiService;
	}

	public void run() {
		this.pool = Executors.newFixedThreadPool(TCPPoolSize);
		try {
			this.serverListenSocket = new ServerSocket(this.port);
			this.serverListenSocket.setReuseAddress(true);
			log.info("|Socket服务|监听端口|" + port + "|开始监听");
			while (true) {
				Socket socket = this.serverListenSocket.accept();
				this.pool.execute(new SocketServerService(socket,apiService));

				// 强制转换为ThreadPoolExecutor以便访问其内部状态
				ThreadPoolExecutor executor = (ThreadPoolExecutor) this.pool;
				log.info("socketThreadPool线程池状态：{}，已使用线程的数量为 : {} , 线程池线程的数量为 : {}, 核心线程数为 : {}，空闲线程数量为 : {}，已完成的任务数数量为 : {},线程池线程的数量为 : {},线程池曾经存在的最大线程数为 : {}"
						,executor,executor.getActiveCount(),executor.getPoolSize(),executor.getCorePoolSize()
						,executor.getMaximumPoolSize()-executor.getActiveCount(),executor.getCompletedTaskCount(),
						executor.getLargestPoolSize(),executor.getPoolSize());


			}
		} catch (IOException e) {
			log.error("Socket服务启动异常!\n",e);
		} finally {
			cleanup();
		}

	}

	public void cleanup() {
		if (this.serverListenSocket != null) {
			try {
				this.serverListenSocket.close();
			} catch (IOException e) {
				log.error("Socket服务停止资源关闭异常!\n",e);
			}
		}
		this.pool.shutdown();
	}

}
