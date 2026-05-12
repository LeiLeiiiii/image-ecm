package com.sunyard.ecm.oldToNew.socket.server;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.sunyard.ecm.dto.AddBusiDTO;
import com.sunyard.ecm.dto.EcmBaseInfoDTO;
import com.sunyard.ecm.dto.EcmBusExtendDTO;
import com.sunyard.ecm.dto.EcmBusiAttrDTO;
import com.sunyard.ecm.dto.EcmBusiFileInfoDTO;
import com.sunyard.ecm.dto.EcmRootDataDTO;
import com.sunyard.ecm.dto.FileAndSortDTO;
import com.sunyard.ecm.dto.FileDTO;
import com.sunyard.ecm.dto.UploadAllDTO;
import com.sunyard.ecm.dto.UploadFileDTO;
import com.sunyard.ecm.dto.split.SysFileApiDTO;
import com.sunyard.ecm.oldToNew.constant.ApiConstants;
import com.sunyard.ecm.oldToNew.service.ApiService;
import com.sunyard.ecm.oldToNew.socket.constant.OldToNewConstant;
import com.sunyard.ecm.oldToNew.socket.util.DateTimeUtils;
import com.sunyard.ecm.oldToNew.socket.util.DateUtils;
import com.sunyard.ecm.oldToNew.socket.util.MD5Util;
import com.sunyard.ecm.oldToNew.socket.util.StringUtil;
import com.sunyard.ecm.oldToNew.socket.util.ZipFileProcessorUtils;
import com.sunyard.ecm.util.AnalysisGrantToolUtils;
import com.sunyard.ecm.util.FunctionUtil;
import com.sunyard.framework.common.util.FileUtils;
import com.sunyard.insurance.base.util.crypto.Base64WithAccess;
import com.sunyard.insurance.ecm.bean.ResponseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SocketServerService implements Runnable, Serializable {
	// 1. 定义合法文件名的正则表达式（只允许字母、数字、下划线、横线、点和空格，限制长度）
	private static final Pattern VALID_FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\\.\\s]{1,255}$");

	private static final long serialVersionUID = -2809455986003376547L;
	private static final Logger log = LoggerFactory.getLogger(SocketServerService.class);
	private Socket socket = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	private DataOutputStream dos = null;
	private DataInputStream dis = null;
	private InputStreamReader inputStream = null;
	private BufferedWriter bufferWriter = null;
	private OutputStreamWriter outWriter = null;
	private int transBufferSize = OldToNewConstant.TransBufferSize;
	private ApiService apiService=null;
	public SocketServerService(Socket socket, ApiService apiService) {
		this.socket = socket;
		this.apiService = apiService;
	}

	public void run() {
		long startTime = System.currentTimeMillis();
		//记录第三方上传socket方式请求参数
		Map<String, Object> reMap = new HashMap<String, Object>();
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("code", "ECM0018");
		ResponseBean retBean = new ResponseBean("200", "上传成功！");
		try {
			this.dos = new DataOutputStream(this.socket.getOutputStream());
			this.dis = new DataInputStream(this.socket.getInputStream());
			this.inputStream = new InputStreamReader(this.socket.getInputStream());
			this.in = new BufferedReader(this.inputStream);
			this.outWriter = new OutputStreamWriter(this.socket.getOutputStream(),"UTF-8");
			this.bufferWriter = new BufferedWriter(this.outWriter);
			this.out = new PrintWriter(this.bufferWriter,
					true);
			this.socket.setSoTimeout(OldToNewConstant.SocketSoTimeOut);
			this.socket.setReceiveBufferSize(OldToNewConstant.RecvSocketBufferSize);
			this.socket.setSendBufferSize(OldToNewConstant.SendSocketBufferSize);

			InetAddress addr = socket.getInetAddress();
			String clientIp = addr.getHostAddress();
			boolean isAlive = true;
//			String uuidStr = java.util.UUID.randomUUID().toString();
			// SunECM缓存目录
			String receivePath = OldToNewConstant.receivePath;

			while (isAlive) {
				String str = this.in.readLine();
				log.info("客户端IP["+clientIp+"]输入:" + str);

				long beginTime = System.currentTimeMillis();
				if (null == str || "".equals(str) || "null".equals(str)) {
					isAlive = false;
					retBean.setRespCode("404");
					retBean.setRespMsg("客户端输入为空。");
					continue;
				}
				boolean breakType = false;
				boolean encryptType = false;
				if(str.indexOf("ECM0018") == -1 && str.indexOf("UNLOCK") == -1){
					str = Base64WithAccess.decode(str,"utf-8");
					breakType = true;
					encryptType = true;
				}
				paramMap.put("str", str);
				reMap.put("paramMap", paramMap);
				// 分解消息报文
				String[] content = str.split(":");
				String result = "";
				String position = "0";

				if (content[0].equals("0009")) {
					// 断开连接请求，返回成功
					isAlive = false;
					result = "0";
				} else if (content[0].equals("ECM0018")) {
					//第三方上传交易
					String ipMd5 = "";
					String md5 = "";
					String licenseStr = "";
					if (str.indexOf("@") != -1) {
						// 此段处理是为了处理服务端获取到客户端socket实际请求的地址
						licenseStr = content[2];
						ipMd5 = str.substring(str.lastIndexOf("@")+1);
						log.info("SunECM接收传入客户端IP["+ipMd5.split(":")[0]+"]");
						log.info("==授权校验=="+licenseStr);
						if(encryptType){
							md5 = ipMd5.split(":")[1];
							if("".equals(licenseStr)){
								result = "-1";
							}else{
								try {
									licenseStr = licenseStr.replace("SunECM", "SunICMS");
									retBean = AnalysisGrantToolUtils.verifyOther(content[0], licenseStr, md5 , str.substring(0, str.lastIndexOf(":")), reMap);
									if("200".equals(retBean.getRespCode())){
										result = "1";
									}else{
										log.error("第三方上传校验授权及参数不通过："+retBean.getRespMsg());
										result = AnalysisGrantToolUtils.getMsgWithoutErrorCode(retBean.getRespMsg());
										this.out.println(result);
										isAlive = false;
										continue;
									}
								} catch (Exception e) {
									retBean.setRespCode("400");
									retBean.setRespMsg("3@@"+e.getMessage());
									log.error("第三方上传校验授权异常："+e);
									result = e.getMessage();
									this.out.println(result);
									isAlive = false;
									continue;
								}
							}
						}else{
							if("".equals(licenseStr)){
								result = "-1";
							}else{
								result = "1";
							}
						}
						str = str.substring(0, str.lastIndexOf("@"));
					}else{
						retBean.setRespCode("400");
						retBean.setRespMsg("2@@参数校验未通过。");
						result = "-1";
					}
					String appCode = content[1];
					//根据文件名获取由文件名生成的随机UUID，去前面2个2位作为临时文件夹路径，以便之后断点续传使用
					if(!breakType){
						String uuidStr = UUID.randomUUID().toString();
						receivePath = receivePath + File.separator + "upload" + File.separator + DateUtils.getDateStrCompact()
								+ File.separator + appCode+ File.separator + DateUtils.getHour() + File.separator + uuidStr;
						log.info("向客户端输出:" + result);
						this.out.println(result);
					}else{
						try {
							String nameUUID = UUID.nameUUIDFromBytes(content[3].getBytes()).toString();
							String tempPath = receivePath + File.separator + "upload" + File.separator;
							String secondFold = nameUUID.substring(0, 2);
							String uploadZipMd5 = content[6].toLowerCase(Locale.ENGLISH);
							//判断文件是否存在
							boolean existsFlag = true;
							for(int i = 0; i < OldToNewConstant.upCacheTime; i++){
								String dateStr = DateUtils.getDateBefAft(i, "yyyyMMdd");
								receivePath =StringUtil.pathManipulation(tempPath + dateStr + File.separator + appCode + File.separator + secondFold + File.separator + nameUUID);
								File receiveZip = new File(StringUtil.pathManipulation(receivePath + File.separator + content[3]));
								if(receiveZip.exists()){
									//断点续传，先判断本次上传客户端传入md5码和服务器记录md5是否一致
									String recordMd5 = readMd5FromRecord(receivePath+"/md5Record.bak");
									if(recordMd5 != null && !uploadZipMd5.equals(recordMd5.toLowerCase(Locale.ENGLISH))){
//										retBean.setRespCode("400");
//										retBean.setRespMsg("2@@【断点续传】两次传入同名文件不一致。");
//										log.error("【断点续传】客户端本次传入MD5是"+uploadZipMd5+",读取记录文件MD5是"+recordMd5+"两次传入文件不一致，请检查，或重命名重新上传。");
//										this.out.println("【断点续传】客户端本次传入MD5是"+uploadZipMd5+",读取记录文件MD5是"+recordMd5+"两次传入文件不一致，请检查，或重命名重新上传。");
//										isAlive = false;
//										break;
										FileUtils.deleteFile(receivePath);
										existsFlag = false;
										break;
									}
									String revZipMD5 = MD5Util.getFileMD5(receivePath + File.separator + content[3]).toLowerCase(Locale.ENGLISH);
									//判断本地是否已经有完整的zip包文件
									if(!revZipMD5.equals(uploadZipMd5)){
										if(recordMd5 != null){
											position = String.valueOf(receiveZip.length());
										}else{
											position = "0";
											//第一次上传，记录客户端传入md5参数
											if(!recordUpMd5(receivePath, uploadZipMd5)){
												retBean.setRespCode("400");
												retBean.setRespMsg("4@@【第三方上传】记录客户端本次传入MD5失败。");
												log.error("【第三方上传】记录客户端本次传入MD5失败,请联系管理员。");
												this.out.println("【第三方上传】记录客户端本次传入MD5失败,请联系管理员。");
												isAlive = false;
												break;
											}
										}
									}else{
										position = "-1";
										result = "1";
										log.info("第三方上传相同MD5的zip文件已存在，已上传，不需要重复上传。");
									}
									break;
								}else{
									if(i == OldToNewConstant.upCacheTime -1 ){
										existsFlag = false;
									}
								}
							}
							if(!isAlive){
								continue;
							}
							if(!existsFlag){
								receivePath = StringUtil.pathManipulation(tempPath + DateUtils.getDateStrCompact() + File.separator + appCode + File.separator + secondFold + File.separator + nameUUID);
								//第一次上传，记录客户端传入md5参数
								if(!recordUpMd5(receivePath, uploadZipMd5)){
									retBean.setRespCode("400");
									retBean.setRespMsg("4@@【第三方上传】记录客户端本次传入MD5失败。");
									log.error("【第三方上传】记录客户端本次传入MD5失败,请联系管理员。");
									this.out.println("【第三方上传】记录客户端本次传入MD5失败,请联系管理员。");
									isAlive = false;
									continue;
								}
							}
						} catch (Exception e) {
							retBean.setRespCode("400");
							retBean.setRespMsg("4@@生成第三方上传文件上传目录出现异常。"+e.getMessage());
							log.error("生成第三方上传文件上传目录出现异常："+e);
							result = e.getMessage();
							this.out.println(result);
							isAlive = false;
							continue;
						}
						log.info("向客户端输出:" + result+":"+position);
						this.out.println(result+":"+position);
					}
				} else {
					//其他接口请求交易（解锁等）、暂不统一接入
					//避免非法输入
					Pattern pattern = Pattern.compile("^[a-zA-Z0-9][^[:]{1}[a-zA-Z0-9]]{1,}[a-zA-Z0-9]{1,}$");
					Matcher matcher = pattern.matcher(str);
					if(!matcher.matches()){
						retBean.setRespCode("400");
						retBean.setRespMsg("2@@socket其他接口交易请求，非法输入异常。");
						this.out.println("非法输入，请检查！");
						isAlive = false;
						continue;
					}
					// todo 控件调用批次解锁
					String batchId = str.split(":")[1];
					if(batchId.matches("^[a-zA-Z0-9]+$") && batchId.length()==32){
//						BatchService batchService = (BatchService) ECMContext.getBean("batchService");
//						BatchFilter batchFilter = new BatchFilter();
//						batchFilter.setBatchId(batchId);
//						batchService.unlockBatch(batchFilter);
//						result = "0";
					}else{
						retBean.setRespCode("400");
						retBean.setRespMsg("2@@socket批次解锁交易请求，批次号错误异常。");
						this.out.println("批次号错误，请检查！");
						isAlive = false;
						continue;
					}
					log.info("向客户端输出:" + result);
					this.out.println(result);
				}

				log.info("交易类型:" + content[0]+"耗时:"+(System.currentTimeMillis()-beginTime));

				//标识正在处理的是第三方上传交易，接收ZIP包
				if (content[0].equals("ECM0018") && result.equals("1")) {
					if ("-1".equals(position) || recieveFile(receivePath, str, Integer.parseInt(position))) {
						String format = "";
						if(str.split(":") != null && str.split(":").length>6){
							format = str.substring(str.lastIndexOf(":")+1).toLowerCase();
						}
						//上传批次
						String fileName = str.split(":")[3];
						log.info("===zip存放路径==="+ receivePath);
//						ImageUploadService imageUpload = null;
						String returnStr = "1";
						try {
							//获取提交上来的文件MD5码
							String revZipMD5 = MD5Util.getFileMD5(receivePath + File.separator + fileName).toLowerCase(Locale.ENGLISH);
							String uploadZipMd5 = str.split(":")[6].toLowerCase(Locale.ENGLISH);
							if(!revZipMD5.equals(uploadZipMd5)){
								retBean.setRespCode("400");
								retBean.setRespMsg("2@@第三方上传，MD5校验失败。");
								log.info("MD5码校验不通过， 客户端提交的ZIP文件MD5="+ uploadZipMd5+", 服务端接受到的文件MD5="+revZipMD5);
								returnStr = "-6";
							}else{
								// todo 上传
								String filePath=receivePath+File.separator+fileName;
								Path safeFilePath = createSafeFilePath(receivePath, fileName);
								String xml=ZipFileProcessorUtils.readXmlContent(safeFilePath.toFile());
								//封装参数
								UploadAllDTO uploadAllDTO=new UploadAllDTO();
								AddBusiDTO addBusiDTO=new AddBusiDTO();
								EcmRootDataDTO ecmRootDataDTO= FunctionUtil.getEcmRootDataDTO(xml);
								//清空扩展属性,目前会解析PAGES进去，所以删除
								EcmBusExtendDTO busExtendDTO=ecmRootDataDTO.getEcmBusExtendDTOS().get(0);
								// 过滤掉 attrCode 为 "PAGES" 的项
								List<EcmBusiAttrDTO> filteredList = busExtendDTO.getEcmBusiAttrDTOList().stream()
										.filter(attr -> !"PAGES".equals(attr.getAttrCode()))
										.collect(Collectors.toList());
								busExtendDTO.setEcmBusiAttrDTOList(filteredList);
								//封装业务属性列表
								apiService.setEcmBusiAttrDTOList(busExtendDTO);
								EcmBaseInfoDTO ecmBaseInfoDTO=ecmRootDataDTO.getEcmBaseInfoDTO();
								ecmBaseInfoDTO.setOneBatch("1");
								//判断树结构
								if(CollectionUtils.isEmpty(busExtendDTO.getEcmVTreeDataDTOS())){
									//静态树
									ecmBaseInfoDTO.setTypeTree("0");
								}else {
									ecmBaseInfoDTO.setTypeTree("1");
								}
								addBusiDTO.setEcmBaseInfoDTO(ecmBaseInfoDTO);
								addBusiDTO.setEcmBusExtendDTOS(busExtendDTO);
								uploadAllDTO.setEcmRootDataDTO(addBusiDTO);
								//封装文件信息
								List<UploadFileDTO> splitDTO= ZipFileProcessorUtils.getUploadFileDtoList(xml,filePath);
								uploadAllDTO.setSplitDTO(splitDTO);
								//若资料节点下无文件，则排除该资料节点
								List<UploadFileDTO> splitDTO1 = uploadAllDTO.getSplitDTO();
								List<UploadFileDTO> filteredData = new ArrayList<>();
								for (UploadFileDTO item : splitDTO1) {
									List<FileAndSortDTO> fileAndSortDTOS = item.getFileAndSortDTOS();
									boolean containsFile1 = false;
									for (FileAndSortDTO subitem : fileAndSortDTOS) {
										if (subitem.getMultipartFile() != null) {
											containsFile1 = true;
											break;
										}
									}
									if (containsFile1) {
										filteredData.add(item);
									}
								}
								uploadAllDTO.setSplitDTO(filteredData);

								EcmBusExtendDTO ecmBusExtendDTOS = uploadAllDTO.getEcmRootDataDTO().getEcmBusExtendDTOS();
								//判断该业务是否压缩
								String isCompress = uploadAllDTO.getEcmRootDataDTO().getEcmBusExtendDTOS().getIsCompress();
								if (StrUtil.isNotBlank(isCompress) && isCompress.equals(ApiConstants.COMPRESS.toString())) {
									//获取业务类型压缩配置
									ecmBusExtendDTOS = apiService.getCompressParams(uploadAllDTO.getEcmRootDataDTO().getEcmBusExtendDTOS());
								} else {
									ecmBusExtendDTOS.setIsCompress(ApiConstants.NOCOMPRESS.toString());
								}
								uploadAllDTO.getEcmRootDataDTO().setEcmBusExtendDTOS(ecmBusExtendDTOS);
								List<EcmBusiFileInfoDTO> uploadResult = Collections.synchronizedList(new ArrayList<>());
								for (UploadFileDTO dto : uploadAllDTO.getSplitDTO()) {
									EcmBusiFileInfoDTO ecmBusiFileInfoDTO = apiService.checkBusiAndFile(dto, uploadAllDTO);
									uploadResult.add(ecmBusiFileInfoDTO);
								}
								List<Map<String, Object>> resultList = new ArrayList<>();
								List<SysFileApiDTO> succ = new ArrayList<>();
								if (CollectionUtil.isNotEmpty(uploadResult)) {
									//重复文件信息返回
									for (EcmBusiFileInfoDTO ecmBusiFileInfoDTO : uploadResult) {
										if (CollectionUtil.isNotEmpty(ecmBusiFileInfoDTO.getRepeatFileMd5List())) {
											Map<String, Object> map = new HashMap<>();
											List<String> fileNameList = null;
											String docCode = ecmBusiFileInfoDTO.getEcmFileInfoDTO().getDocCode();
											List<FileDTO> repeatFileList = ecmBusiFileInfoDTO.getRepeatFileMd5List();
											fileNameList = repeatFileList.stream().map(FileDTO::getFile).map(MultipartFile::getOriginalFilename)
													.collect(Collectors.toList());
											map.put("docCode", docCode);
											map.put("repeatFile", fileNameList);
											resultList.add(map);
										}
										succ.addAll(ecmBusiFileInfoDTO.getSaveFileSucc());
									}
								}
								Map<String, Object> mapRet = new HashMap<>();
								mapRet.put("succFile", succ);
								mapRet.put("errorFile", resultList);
							}
						} catch (Exception e) {
							retBean.setRespCode("400");
							retBean.setRespMsg("4@@"+e.getMessage());
							if("处理批次文件失败：该批次无影像信息".equals(e.getMessage())){
								returnStr = "-7";
							}else{
								returnStr = e.getMessage();
							}
						} finally {
//							if (imageUpload != null) {
//								imageUpload.clear();
//							}
						}
						log.info("第三方上传结束，返回状态：" + returnStr);
						this.out.println(returnStr);
						isAlive = false;
						continue;
					} else {
						isAlive = false;
						continue;
					}
				}

			}
		} catch (IOException e) {
			retBean.setRespCode("400");
			retBean.setRespMsg("4@@"+e.getMessage());
			log.error("|Socket|IOException异常!",e);
		} finally {
			System.out.println("-------------Socket传输线程销毁-------------");
			int timeConsume = DateTimeUtils.timeConsume(startTime);
			reMap.put("timeConsume", timeConsume);
			try {
				if(null!=this.out) {
					this.out.close();
				}
				if(null!=this.bufferWriter) {
					this.bufferWriter.close();
				}
				if(null!=this.outWriter) {
					this.outWriter.close();
				}
				if(null != this.in) {
					this.in.close();
				}
				if(null != this.inputStream) {
					this.inputStream.close();
				}
				if(null != this.dos) {
					this.dos.close();
				}
				if(null != this.dis) {
					this.dis.close();
				}
				if(null != this.socket) {
					this.socket.close();
				}
				if("400".equals(retBean.getRespCode())){
					int id = AnalysisGrantToolUtils.recordFailRequest(reMap, retBean.getRespMsg(), "", "");
					OldToNewConstant.eLog.error("请求ID为【"+id+"】的socket请求失败：" + retBean.getRespMsg());
				}else if("200".equals(retBean.getRespCode())){
					AnalysisGrantToolUtils.recordSuccessRequest(reMap);
				}else if("404".equals(retBean.getRespCode())){
				}
			} catch (IOException e) {
				log.error("|Socket|资源关闭异常!",e);
			}
		}
	}

	private boolean recieveFile(String rootPath, String str, int position) {
		File recieveFile = null;
		RandomAccessFile fileOutPutStream = null;
		try {
			String[] content = str.split(":");
			String fileName = content[3];
			long fileSize = Long.parseLong(content[4]);
			this.transBufferSize = Integer.parseInt(content[5]);
			boolean transFlag = true;
			File recieveFolder = new File(StringUtil.pathManipulation(rootPath));
			if (!recieveFolder.exists()) {
				recieveFolder.mkdirs();
			}
			recieveFile = new File(StringUtil.pathManipulation(rootPath + File.separator + fileName));

			fileOutPutStream = new RandomAccessFile(recieveFile, "rw");
			fileOutPutStream.seek(position);//移动文件指定的位置开始写入数据
			long availSize = fileSize - position;
			if(availSize < 0){
				log.error("【断点续传】相同文件名zip包，服务器保存文件和本次上传文件不是同一文件，请检查。");
				return false;
			}
			while (transFlag) {
				int bufSize = this.transBufferSize;
				if (availSize < this.transBufferSize) {
					bufSize = Integer.parseInt(Long.toString(availSize));
					transFlag = false;
				}
				byte[] buf = new byte[bufSize];
				this.dis.readFully(buf);
				fileOutPutStream.write(buf);
				availSize -= this.transBufferSize;
			}
			log.info("|SOCKET|接收文件|" + recieveFile.getAbsolutePath() + "成功");
			return true;
		} catch (IOException e) {
			log.error("|SOCKET|接收文件|" + recieveFile.getAbsolutePath() + "失败了!",e);
			return false;
		} finally {
			try {
				if(fileOutPutStream!=null){
					fileOutPutStream.close();
				}
			} catch (IOException e) {
				log.error("|SOCKET|接收文件|流fileOutPutStream关闭异常!",e);
			}
		}
	}

	//  安全的路径构建方法
	private Path createSafeFilePath(String baseDir, String fileName) throws SecurityException {
		// 验证文件名合法性
		if (fileName == null || !VALID_FILENAME_PATTERN.matcher(fileName).matches()) {
			throw new SecurityException("非法文件名: " + fileName + "，包含不允许的字符");
		}

		// 解析基础目录为绝对路径（规范化）
		Path basePath;
		try {
			basePath = Paths.get(baseDir).toRealPath(); // 自动处理相对路径和符号链接
		} catch (Exception e) {
			throw new SecurityException("基础目录无效: " + baseDir, e);
		}

		// 解析完整路径（自动处理路径分隔符）
		Path fullPath;
		try {
			fullPath = Paths.get(baseDir, fileName).toRealPath(); // 规范化完整路径
		} catch (InvalidPathException e) {
			throw new SecurityException("无效的文件路径: " + fileName, e);
		} catch (Exception e) {
			throw new SecurityException("无法解析文件路径: " + fileName, e);
		}

		// 检查完整路径是否在基础目录之内（防止目录遍历）
		if (!fullPath.startsWith(basePath)) {
			throw new SecurityException("禁止访问基础目录外的文件: " + fullPath);
		}

		return fullPath;
	}

	/**
	 * 首次上传记录本次上传参数中的md5
	 * @param md5RecordPath
	 * @param md5
	 * @return
	 */
	private boolean recordUpMd5(String md5RecordPath, String md5){
		FileWriter fw = null;
		boolean flag = false;
		try {
			long begin = System.currentTimeMillis();
			File recieveFolder = new File(md5RecordPath);
			if (!recieveFolder.exists()) {
				recieveFolder.mkdirs();
			}
			fw = new FileWriter(md5RecordPath+"/md5Record.bak");
			fw.write(md5);
			long end = System.currentTimeMillis();
			log.info("记录客户端上传md5执行耗时:" + (end - begin) + " 毫秒");
			flag = true;
		} catch (Exception e) {
			// todo: handle exception
			log.error("记录客户端上传md5失败了!",e);
			flag = false;
		}finally{
			try {
				if(fw != null){
					fw.close();
				}
			} catch (IOException e) {
				// todo Auto-generated catch block
				log.error("记录客户端上传md5关闭流失败了!",e);
				flag = false;
			}
		}
		return flag;
	}

	/**
	 * 断点续传读取记录中的MD5
	 * @param fileFullPath
	 * @return
	 */
	private String readMd5FromRecord(String fileFullPath){
		String Md5 = null;
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		boolean flag = false;
		try {
			fis = new FileInputStream(fileFullPath);
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
			Md5 = br.readLine();
			flag = true;
		} catch (Exception e) {
			log.error("【第三方上传】读取记录md5失败。",e);
			flag = false;
		}finally{
			try {
				if(br != null){
					br.close();
				}
				if(isr != null){
					isr.close();
				}
				if(fis != null){
					fis.close();
				}
			} catch (IOException e) {
				log.error("【第三方上传】读取记录md5关闭流失败。",e);
				flag = false;
			}
		}
		if(flag){
			return Md5;
		}else{
			return null;
		}
	}

//	// 过渡解决方案，必须改掉
//	private ServiceBean getServiceBean(String entry, String code) {
//		ServiceBean bean = null;
//		if ("doGet".equals(entry)) {
//			bean = OldToNewConstant.servicesConfig.getDoGetServices().get(code);
//		} else if ("doPost".equals(entry)) {
//			bean = OldToNewConstant.servicesConfig.getDoPostServices().get(code);
//		} else if ("socket".equals(entry)) {
//			bean = OldToNewConstant.servicesConfig.getSocketServices().get(code);
//		} else {
//			throw new ServiceException(StringUtil.getMessage(
//					"无法匹配的Servlet方法【{x}】", "{x}", entry));
//		}
//		if (bean == null) {
//			throw new ServiceException(StringUtil.getMessage(
//					"服务列表【{x}】中没有对应服务【{x}】", "{x}", entry, code));
//		}
//		return bean;
//	}
//
//	private BaseService getService(ServiceBean bean) throws SecurityException,
//			IllegalArgumentException, ClassNotFoundException,
//			NoSuchMethodException, InstantiationException,
//			IllegalAccessException, InvocationTargetException {
//
//		Object service = null;
//		if (ServiceBean.SPRING.equals(bean.getObjectFactory())) {
//			service = ECMContext.getBean(bean.getServiceName());
//		} else if (ServiceBean.CLASS.equals(bean.getObjectFactory())) {
//			service = ClassUtil.getInstance(bean.getClassName());
//		}
//		if (service == null) {
//			throw new ServiceException("无法获取到对应服务！");
//		}
//		return (BaseService) service;
//	}


}
