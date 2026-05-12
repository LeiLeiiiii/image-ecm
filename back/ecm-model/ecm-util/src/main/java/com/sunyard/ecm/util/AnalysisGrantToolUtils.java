package com.sunyard.ecm.util;


import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sunyard.ecm.dto.GrantAccessDTO;
import com.sunyard.ecm.dto.GrantServiceDTO;
import com.sunyard.ecm.exception.OldToNewException;
import com.sunyard.ecm.po.Grant;
import com.sunyard.insurance.base.util.crypto.Base64WithAccess;
import com.sunyard.insurance.ecm.bean.ResponseBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author LJJ
 *
 */
@Slf4j
@Component
public class AnalysisGrantToolUtils {

	public static String serviceGrantStr = null;
	
	public static GrantServiceDTO grantService = new GrantServiceDTO();

	public static List<GrantAccessDTO> grantAccessList = new ArrayList<GrantAccessDTO>();

	public static Map<String, String> localGrantMap = new HashMap<String, String>();

	@Resource
	private Grant grant;
	/**
	 * 初始化鉴权信息 存到内存中
	 * @throws Exception
	 */
	@PostConstruct
	public  void init() throws Exception{
		// TODO 从数据库中查权限信息
		if(grant == null){
			log.error("没有获取到授权信息，请检查数据库！");
			throw new OldToNewException("没有获取到授权信息，请检查数据库！");
		}
		try {
//			String serviceGrantEnc  = IOUtils.toString(grant.getGrantService().getBinaryStream(), "utf-8");
//			//new String(grant.getGrantService().getBytes(1, (int) grant.getGrantService().length()),"utf-8");
//			grantService = analysisOwnServiceGrant(serviceGrantEnc);
//
//			String accessGrantEnc  = IOUtils.toString(grant.getGrantAccess().getBinaryStream(), "utf-8");
//			grantAccessList = analysisGrantAccess(accessGrantEnc);
//			if(grantAccessList.size() == 0){
//				throw new OldToNewException("没有获取到接入系统授权信息，请检查授权文件！");
//			}
		} catch (Exception e) {
			grantAccessList = null;
			log.error("解析授权文件出错！",e);
			throw new OldToNewException("解析授权文件出错！");
		}
//		verifyService();
	}

	/**
	 * 鉴权参数校验
	 * @param request
	 * @return
	 * @throws ServletException
	 */
	public static void checkParam(HttpServletRequest request) throws ServletException{
		long startTime = System.currentTimeMillis();
		request.setAttribute("startTime", startTime);
		HttpSession session = request.getSession(true);
		Map<String, String[]> requestParams = request.getParameterMap();
		String data = "";
		if(requestParams.get("data") ==  null){
			data = (String) requestParams.keySet().iterator().next();
		}else{
			data = requestParams.get("data")[0];
		}

		Map<String, String> map = null;
		Map<String, Object> reMap = null;
		try {
			reMap = check(request, data);
			map = (Map<String, String>) reMap.get("paramMap");
		} catch (Exception e) {
			int timeConsume = timeConsume(startTime);
			if(reMap != null){
				reMap.put("timeConsume", timeConsume);
			}
			throwServletException(request, e, reMap, data);
		}
		if(map!=null){
			try {
				verifyParam(reMap);
				String xml = "";
				xml = map.get("xml");
				//TODO 角色校验
//				RoleService roleService = (RoleService) ECMContext.getBean("roleService");
				if (xml != null && !xml.equals("")) {
					log.info("获取客户端请求报文：" + xml);
					// 判断传入报文是否合法
					String checkXmlStr = checkXml(xml);
					if (!checkXmlStr.equals("")) {
						throw new OldToNewException("2@@"+checkXmlStr);
					}
					try {
						Document document = DocumentHelper.parseText(xml);
						String userCode = document.selectSingleNode("/root/BASE_DATA/USER_CODE").getText();
						String roleCodes = document.selectSingleNode("/root/BASE_DATA/ROLE_CODE").getText();
						String[] roleArray = roleCodes.split("\\|");
						for (String roleCode : roleArray) {
							//TODO 角色校验
//							if (roleService.getRole(roleCode) == null) {
//								session.removeAttribute("userCode");
//								session.removeAttribute("roleCodes");
//								throw new ServletException("角色不存在！");
//							}else{
//								session.setAttribute("interUserCode", userCode);
//								session.setAttribute("interRoleCode", roleCode);
//							}
						}
					} catch (Exception e) {
						log.error("解析XML出错！", e);
						throw new OldToNewException("2@@"+e.getMessage());
					}
				}else{
					String roleCode = request.getParameter("roleCode");
					String userCode = checkUserCode(request.getParameter("userCode"));
					if(roleCode != null && !roleCode.equals("")){
						//TODO 角色校验
//						if (roleService.getRole(roleCode) != null) {
//							session.setAttribute("roleCodes", roleCode);
//						} else {
//							session.removeAttribute("roleCodes");
//							throw new ServletException("2@@角色不存在！");
//						}
					}else{
						session.removeAttribute("roleCodes");
					}
					if(userCode != null && !userCode.equals("")){
						session.setAttribute("userCode", userCode);
					}
				}
			} catch (Exception e) {
				int timeConsume = timeConsume(startTime);
				if(reMap != null){
					reMap.put("timeConsume", timeConsume);
				}
				throwServletException(request, e, reMap, null);
			}
		}
	}


	public static ResponseBean verifyOther(String serviceCode, String license, String md5, String data, Map<String, Object> reMap) throws Exception {
		boolean flag = false;
		ResponseBean respBean = new ResponseBean("200", "授权校验成功！");
		String accessId = "";
		try {
			accessId = license.split("#")[0];
		} catch (Exception e) {
			respBean = new ResponseBean("400", "2@@获取授权系统ID异常，请检查！");
            log.error("获取授权系统ID异常！{}", license, e);
			return respBean;
		}
		Date nowDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date endDate = null;
		try {
			endDate = sdf.parse(grantService.getEndDateStr() + " 23:59:59");
		} catch (ParseException e) {
			respBean = new ResponseBean("400", "3@@获取授权终止时间异常！");
			log.error("获取授权终止时间异常！",e);
			return respBean;
		}
		if (nowDate.getTime() >= endDate.getTime()) {
			respBean = new ResponseBean("400", "3@@授权时间已过期，请联系管理员，重新获取授权！");
			log.error("授权时间已过期！");
			return respBean;
		}
		for (int i = 0; i < grantAccessList.size(); i++) {
			GrantAccessDTO access = grantAccessList.get(i);
			if(accessId.equals(access.getAccessId())){
				flag = true;
				int indexCode = access.getServiceCodes().indexOf(serviceCode);
				String accessName = access.getAccessName();
				if(indexCode == -1){
					respBean = new ResponseBean("400", "3@@该接入系统没有授权该服务，请联系管理员！服务代码【"+serviceCode+"】接入系统ID【"+accessId+"】");
					log.error("该接入系统没有授权该服务，请联系管理员！服务代码【"+serviceCode+"】接入系统ID【"+accessId+"】");
				}else{

//					if(localGrantMap.containsKey(accessId)){
//						String localCode = localGrantMap.get(accessId);
//						int localIdx = localCode.indexOf(serviceCode);
//						if(localIdx == -1){
//							respBean = new ResponseBean("400", "3@@该接入系统没有授权该服务，请联系管理员！服务代码【"+serviceCode+"】接入系统【"+accessName+"("+accessId+")】");
//							log.error("错误码003-该接入系统没有授权该服务，请联系管理员！服务代码【"+serviceCode+"】接入系统【"+accessName+"("+accessId+")】");
//						}
//					}else {
//						respBean = new ResponseBean("400", "3@@该接入系统没有授权该服务，请联系管理员！服务代码【"+serviceCode+"】接入系统【"+accessName+"("+accessId+")】");
//						log.error("错误码003-该接入系统没有授权该服务，请联系管理员！服务代码【"+serviceCode+"】接入系统【"+accessName+"("+accessId+")】");
//					}
				}
//				String md5Str = Md5Utils.encryptHmacMd5Str(access.getAccessKey(), data);
//				if(!md5Str.equals(md5)){
//					respBean = new ResponseBean("400", "2@@md5码校验不通过，请检查！data="+data+",md5="+md5);
//					log.error("md5码校验不通过，请检查！data="+data+",md5="+md5+",accessKey="+access.getAccessKey());
//				}
				reMap.put("access", access);
				break;
			}
		}
		if(!flag){
			respBean = new ResponseBean("400", "1@@该接入系统未授权，请联系管理员！接入系统ID【"+accessId+"】");
			log.error("该接入系统未授权，请联系管理员！接入系统ID【"+accessId+"】");
		}
		return respBean;
	}


	/**
	 *
	 * @param request
	 * @param data
	 * @return
	 * @throws ServletException
	 */
	private static Map<String, Object> check(HttpServletRequest request, String data) throws ServletException{
		Map<String, String> map = null;
		//1、获取referer 并校验referer及服务代码
		GrantAccessDTO accessMacth = null;
		try {
			accessMacth = EncryptFuncUtils.checkReferer(request);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new OldToNewException(e.getMessage());
		}
		if(accessMacth == null){
			log.error("接入系统【"+EncryptFuncUtils.getReferer(request)+"】没有授权！");
			throw new OldToNewException("1@@接入系统未授权，请检查！");
		}
		//2、把data进行base64解密
		if(data == null || "".equals(data)){
			throw new OldToNewException("2@@传入参数为空，请检查！");
		}
		//3、获取md5
		try {
			data = Base64WithAccess.decode(data,"utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new OldToNewException("2@@解密传入参数失败，请检查！");
		}
        log.info("本次服务传入参数为：{}", data);
		String tokenMd5 = "";
		try {
			String md5Data = data.substring(0, data.lastIndexOf("&"));
			Pattern p = Pattern.compile("\\s*|\t|\r|\n");
			Matcher m = p.matcher(md5Data);
			md5Data = m.replaceAll("");
			if("SunICMS".equals(accessMacth.getAccessId())){
				accessMacth.setAccessKey("uroahf63n59ch6s8bn5m58sg");
			}
			tokenMd5 = Md5Utils.encryptHmacMd5Str(accessMacth.getAccessKey(), md5Data);
		} catch (Exception e) {
			throw new OldToNewException("4@@根据授权密钥生成签名失败失败，请检查");
		}
		//4、获取各参数的值
		map = EncryptFuncUtils.analysisParam(data);

		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap.put("paramMap", map);
		reMap.put("tokenMd5", tokenMd5);
		reMap.put("access", accessMacth);

		return reMap;
	}
	private static int timeConsume(long startTime){
		long endTime = System.currentTimeMillis();
		return (int)(endTime - startTime);
	}

	private static void throwServletException(HttpServletRequest request, Exception e, Map<String, Object> reMap, String param) throws ServletException{
		Map<String, String> mapEx = analysisExceptionMsg(e);
//		AccessInfoService accessInfoService = (AccessInfoService) ECMContext.getBean("accessInfoService");
		int id = 0;
		try {
			int timeConsume =  Integer.parseInt(reMap.get("timeConsume").toString());
			if(reMap != null){
				Map<String, String> paramMap = (Map<String, String>) reMap.get("paramMap");
				GrantAccessDTO access = (GrantAccessDTO) reMap.get("access");
				//TODO 数据库记录接口访问信息记录
//				id = accessInfoService.addAccessInfo(access == null ? "" : access.getAccessName(), paramMap.get("code"), "2", mapEx.get("errorCode"), mapEx.get("msg"), param, timeConsume);
			}else{
				//TODO 数据库记录接口访问信息记录
//				id = accessInfoService.addAccessInfo("", "", "2", mapEx.get("errorCode"), mapEx.get("msg"),param,timeConsume);
			}
		} catch (Exception e2) {
			log.error("记录接口访问信息失败。", e2);
		}
		throw new OldToNewException("请求ID为【"+id+"】的请求出现异常，异常信息："+e.getMessage().substring(e.getMessage().indexOf("@@")+2, e.getMessage().length()));
	}

	public static Map<String, String> analysisExceptionMsg(Exception ex){
		Map<String, String> map = new HashMap<String, String>();
		String msg = ex.getMessage();
		if(msg.indexOf("@@") != -1){
			String code = msg.substring(0, 1);
			msg = msg.substring(msg.indexOf("@@")+2, msg.length());
			map.put("errorCode", code);
			map.put("msg", msg);
		}else{
			String code = "5";
			msg = ex.getMessage();
			map.put("errorCode", code);
			map.put("msg", msg);
		}
		return map;
	}

	private static void verifyParam(Map<String, Object> reMap) throws ServletException{
		Map<String, String> map = (Map<String, String>) reMap.get("paramMap");
		String tokenMd5 = (String) reMap.get("tokenMd5");
		String hmacMd5 = map.get("hmac").toLowerCase(Locale.ENGLISH);
		long deadline = Long.parseLong(map.get("deadline"));
		//5、判断md5是否一致及连接是否超时
//		if(!tokenMd5.toLowerCase(Locale.ENGLISH).equals(hmacMd5)){
//			throw new ServletException("2@@参数MD5校验不通过，请检查！");
//		}
		long now = new Date().getTime();
		if(deadline < now){
			throw new OldToNewException("2@@您的连接已超时，请检查！");
		}
	}

	private static String checkXml(String xml) {
		String msg = "";
		SAXReader reader = new SAXReader();
		if (xml != null && !xml.equals("")) {
			try {
				reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
				reader.read(new ByteArrayInputStream(new String(xml
						.getBytes("UTF-8"), "UTF-8").getBytes("UTF-8")));
			} catch (Exception e) {
				log.error("",e);
				msg = "传入XML报文格式有误！";
			}
		} else {
			msg = "传入XML报文为空！";
		}
		return msg;
	}

	public static String checkUserCode(String temp) {

		//[`~!#$%^&*()\\+\\=\\{}|\"?><【】\\/r\\/n]
		//[`~!@#$%^&*()\\+\\=\\{}|:\"?><【】\\/r\\/n]
		//String regex = "[`~!$%^*()\\+\\=\\{}\"?><]";
		//String regex = "[`~!@#$%^&()\\+\\【】\\/\r/\\/\n/]";
		String regex = "[`~!#$%^&+【】\r\n]";
		Pattern pa = Pattern.compile(regex);

		if (temp != null) {
			Matcher ma = pa.matcher(temp);

			if (ma.find()) {

				temp = ma.replaceAll("");

			}
		}
		return temp;
	}

	public static GrantServiceDTO analysisOwnServiceGrant(String serviceGrantEnc) throws Exception {
		GrantServiceDTO bean = new GrantServiceDTO();
		serviceGrantStr = null;
		InputStream privateKey = AnalysisGrantToolUtils.class.getClassLoader().getResourceAsStream("ecm.key");
		try {
			serviceGrantStr = EncryptFuncUtils.decrypt(privateKey, serviceGrantEnc.toString());
			JSONArray serviceGrantJsArr = JSONUtil.parseArray(serviceGrantStr);
			//匹配完所有的mac地址判断是否能匹配其中一个,都不匹配 flag =false 匹配其中一个flag =true
			boolean flag = false;
			for (int i = 0; i < serviceGrantJsArr.size(); i++) {
				JSONObject js = (JSONObject) serviceGrantJsArr.get(i);
				// 根据权限文件中的ip获取mac地址
				String macLocalAdd = "";
				try {
					macLocalAdd = EncryptFuncUtils.getMacAddressByIp(InetAddress.getByName(js.getStr("ipAddress")));
					if("".equals(macLocalAdd)){
						if(i == serviceGrantJsArr.size()-1){
							log.error("没有获取到服务器MAC地址，请检查！");
							throw new OldToNewException("没有获取到服务器MAC地址，请检查！");
						}
						continue;
					}else if(matchMacAdd(macLocalAdd, js.getStr("macAddress"))){
                        log.info("服务器mac地址:{}", macLocalAdd);
						bean.setCompanyName(js.getStr("companyName"));
						bean.setIpAddress(js.getStr("ipAddress"));
						bean.setMacAddress(js.getStr("macAddress"));
						bean.setStartDateStr(js.getStr("startDate"));
						bean.setEndDateStr(js.getStr("endDate"));
						bean.setServiceCodes(js.getStr("serviceCode"));
						bean.setServiceConfig(js.getStr("serviceConfig"));
						flag = true;
						break;
					}else{
						log.error("服务器mac地址和授权文件mac地址不匹配，请确认！服务器mac地址:" + macLocalAdd);
//						throw new OldToNewException("服务器mac地址和授权文件mac地址不匹配，请确认！");
					}
				} catch (Exception e) {
					log.error("获取服务器MAC地址出错。",e);
					throw new OldToNewException("获取服务器MAC地址出错，请联系负责人解决。");
				}
			}
			if (!flag){
				throw new OldToNewException("服务器mac地址和授权文件mac地址不匹配，请确认！");
			}
			return bean;
		} catch (Exception e) {
			log.error("授权文件解析出错！",e);
			throw new OldToNewException("授权文件解析出错！");
		}finally{
			try {
				privateKey.close();
			} catch (IOException e) {
				log.error("关闭密钥文件流异常！",e);
				throw new OldToNewException("关闭密钥文件流异常！");
			}
		}
	}

	public static List<GrantAccessDTO> analysisGrantAccess(String accessGrantEnc) throws Exception{
		List<GrantAccessDTO> list = new ArrayList<GrantAccessDTO>();
		InputStream privateKey = AnalysisGrantToolUtils.class.getClassLoader().getResourceAsStream("ecm.key");
		try {
			String accessGrantStr = EncryptFuncUtils.decrypt(privateKey, accessGrantEnc.toString());
			JSONArray accessGrantJsArr = JSONUtil.parseArray(accessGrantStr);
			for (int i = 0; i < accessGrantJsArr.size(); i++) {
				JSONObject js = accessGrantJsArr.getJSONObject(i);
				GrantAccessDTO access = new GrantAccessDTO();
				access.setAccessKey(js.getStr("accessKey"));
				access.setAccessId(js.getStr("accessId"));
				access.setAccessName(js.getStr("accessName"));
				access.setServiceCodes(js.getStr("serviceCode"));
				String[] referers = js.getStr("accessReferer").split("@");
				access.setReferer(Arrays.asList(referers));
				list.add(access);
			}
			return list;
		} catch (Exception e) {
			log.error("接入系统授权文件解析出错！",e);
			throw new OldToNewException("接入系统授权文件解析出错！");
		}finally{
			try {
				privateKey.close();
			} catch (IOException e) {
				log.error("关闭密钥文件流异常！",e);
				throw new OldToNewException("关闭密钥文件流异常！");
			}
		}
	}

	private static boolean matchMacAdd(String localMac, String encryptMac){
		boolean result = false;
		if(localMac.toUpperCase(Locale.ENGLISH).equals(encryptMac.toUpperCase(Locale.ENGLISH))){
			result = true;
		}else{
			localMac = localMac.replaceAll("-|:", "");
			encryptMac = encryptMac.replaceAll("-|:", "");
			if(localMac.toUpperCase(Locale.ENGLISH).equals(encryptMac.toUpperCase(Locale.ENGLISH))){
				result = true;
			}
		}
		return result;
	}

	/**
	 * 检验mac地址
	 * @throws Exception
	 */
	private static void verifyService() throws Exception {
		// 根据权限文件中的ip获取mac地址
		String macLocalAdd = "";
		try {
			macLocalAdd = EncryptFuncUtils.getMacAddressByIp(InetAddress.getByName(grantService.getIpAddress()));
		} catch (Exception e) {
			log.error("获取服务器MAC地址出错。",e);
			throw new OldToNewException("获取服务器MAC地址出错，请联系负责人解决。");
		}
		log.info("服务器mac地址:" + macLocalAdd);
		Date nowDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date endDate = null;
		Date startDate = null;
		try {
			endDate = sdf.parse(grantService.getEndDateStr() + " 23:59:59");
			startDate = sdf.parse(grantService.getStartDateStr() + " 00:00:00");
		} catch (ParseException e) {
			log.error("授权文件结束时间获取出错。",e);
			throw new OldToNewException("授权文件结束时间获取出错，请联系负责人解决。");
		}
		if (grantService.getMacAddress() != null && grantService.getMacAddress().toUpperCase(Locale.ENGLISH).equals(macLocalAdd.toUpperCase(Locale.ENGLISH))) {
			if (nowDate.getTime() >= startDate.getTime() && nowDate.getTime() < endDate.getTime()) {
				log.info("验证授权文件成功！");
			} else {
				if(nowDate.getTime() >= endDate.getTime()){
					log.error("该授权文件已过期，请重新联系获取授权文件。");
					throw new OldToNewException("该授权文件已过期，请重新联系获取授权文件。");
				}
				if(nowDate.getTime() < startDate.getTime()){
					log.error("还未到授权时间服务暂不可用！");
					throw new OldToNewException("还未到授权时间服务暂不可用！");
				}
			}
		} else {
			log.error("服务器mac地址和授权文件mac地址不匹配，请确认！服务器mac地址:" + macLocalAdd);
			throw new OldToNewException("服务器mac地址和授权文件mac地址不匹配，请确认！");
		}
	}

	public static String getMsgWithoutErrorCode(String msg){
		if(msg.indexOf("@@") != -1){
			msg = msg.substring(msg.indexOf("@@")+2, msg.length());
		}
		return msg;
	}

	public static int recordFailRequest(Map<String, Object> reMap, String errorMsg, String fromPath, String param){

		//TODO 数据库记录接口访问信息记录
//		AccessInfoService accessInfoService = (AccessInfoService) ECMContext.getBean("accessInfoService");
		Map<String, String> mapEx = analysisFailMsg(errorMsg);
		int id = 0;
		try {
			int timeConsume = Integer.parseInt(reMap.get("timeConsume").toString());
			if(reMap != null){
				Map<String, String> paramMap = (Map<String, String>) reMap.get("paramMap");
				GrantAccessDTO access = (GrantAccessDTO) reMap.get("access");
//				id = accessInfoService.addAccessInfo(access == null ? "" : access.getAccessName(), paramMap.get("code"), "2", mapEx.get("errorCode"), mapEx.get("msg"), param, timeConsume);
			}else{
//				id = accessInfoService.addAccessInfo("", "", "2", mapEx.get("errorCode"), mapEx.get("msg"), param, timeConsume);
			}
		} catch (Exception e) {
			log.error("记录接口访问信息失败。", e);
		}
		return id;
	}

	private static Map<String, String> analysisFailMsg(String errorMsg){
		Map<String, String> map = new HashMap<String, String>();
		if(errorMsg.indexOf("@@") != -1){
			String code = errorMsg.substring(0, 1);
			errorMsg = errorMsg.substring(errorMsg.indexOf("@@")+2, errorMsg.length())+"\n";
			map.put("errorCode", code);
			map.put("msg", errorMsg);
		}else{
			String code = "5";
			map.put("errorCode", code);
			map.put("msg", errorMsg);
		}
		return map;
	}

	public static void recordSuccessRequest(Map<String, Object> reMap){
		Map<String, String> paramMap = (Map<String, String>) reMap.get("paramMap");
		GrantAccessDTO access = (GrantAccessDTO) reMap.get("access");
		// TODO 数据库记录接口访问信息记录
//		AccessInfoService accessInfoService = (AccessInfoService) ECMContext.getBean("accessInfoService");
		try {
			String code = paramMap.get("code");
			String format = paramMap.get("format");
			int timeConsume =  Integer.parseInt(reMap.get("timeConsume").toString());
			//ECM0018第三方上传接口,成功时accessParam记录业务类型
			if(code.equals("ECM0018")){
//				accessInfoService.addAccessInfo(access == null ? "" : access.getAccessName(), paramMap.get("code"), "1", "", "访问成功", paramMap.get("str").split(":")[1],timeConsume);
			}else{
//				accessInfoService.addAccessInfo(access == null ? "" : access.getAccessName(), paramMap.get("code"), "1", "", "访问成功", paramMap.get(format),timeConsume);
			}
		} catch (Exception e) {
			log.error("记录接口访问信息失败。", e);
		}
	}

}
