package com.sunyard.ecm.oldToNew.socket.constant;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class OldToNewConstant {


	public static final Logger bLog = LoggerFactory.getLogger("BusinessLog");

	public static final Logger eLog = LoggerFactory.getLogger("ExceptionLog");

	//总公司机构号
	public static String HEAD_ORGCODE = "";


	//如果是新项目则填写：base(base11 syd中增加指定缓存机构，base12 增加支持多人备注)，如果是picc版本的老项目升级则填写：picc，如果是国寿财DM版本的升级则填写：dm
	public static String SYS_FLAG="base12";

	// SunECM2.1 公司SunECM+SunECMConsole+SunDM
	// 是否使用内容管理平台
	public static String ECM_FLAG;
	// DM地址
	public static String ECM_IP;
	// DM端口
	public static String ECM_PORT;
	// 用户名
	public static String ECM_USERCODE;
	// 用户密码
	public static String ECM_PASSWORD;
	// 1 表示 HTTP 2 表示 SOCKET
	public static String ECM_PROTOCOL;

	// console端口
	public static String CONSOLE_PORT;
	// console地址
	public static String CONSOLE_IP;
	// 是否必须归类 ：0:无控制，1:弱控制，2:强控制
	public static String classifyLimit;
	// 默认图片压缩比
	public static String defResize;
	// 默认业务编号，必须同ProExt.js的defaultUnkowBizCode
	public static String DefaultUnkowBizCode;
	// 是否检查上传缓存服务器，默认不校验
	public static String checkComCode = "0";

	// mime类型
	public static Map<String, String> mimeMap = new HashMap<String,String>();

	// 记录最大查询条数
	public static int MAX_RESULTS = 1000;

	//tcp监听端口
	public static int TCPListenPort = 0;

	//tcp多线程，线程池最大数量
	public static int TCPPoolSize = 10;

	//输入缓冲区大小
	public static int RecvSocketBufferSize = 0;

	//输出缓冲区大小
	public static int SendSocketBufferSize = 0;

	//一次读写文件大小
	public static int TransBufferSize = 0;

	//设置socket等待超时时间
	public static int SocketSoTimeOut = 0;

	// 批次锁超时时间(分钟)
	public static int lockTimeOut = 30;

	// 同一用户是否开启解锁按钮     1:开启,0:关闭
	public static String isUnlock = "0";

	// 同一用户同一机器是否开启自动解锁     1:开启,0:关闭
	public static String MACH_UNLOCK = "1";

	// 第三方上传，SunECM批次接收路径,简单上传临时保存路径
	public static String receivePath;

	// 指定TRM批注请求地址
	public static String TRM_ANNOTATION_URL;
	// 影像回写接口
	public static String TRM_WRITEBACK_URL;
	// 影像回写接口
	public static String TRM_UNIFIEDACCESS_URL;
	// 影像传输缓存Webservice接口地址
	public static String TRM_WEBSERVICE_URL;
	// CE对应TRM缓存机构代码
	public static String TRM_CE_ORGCODE;
	// TRM是否使用新的传输协议,0使用老的传输协议，1使用新的传输协议
	public static String TRM_TRANSFER_PROTOCOL="0";
	// 流媒体SunVCT地址
	public static String VST_VCT_URL;

	// 下载批次保留周期
	public static int downCacheTime = 1;

	// 上传批次保留的周期
	public static int upCacheTime = 1;

	// 简单上传批次保留的周期
	public static int simpleUpCacheTime = 1;
	//获取全部的业务类型，用于批量扫描使用
	public static Set<String> allAppCodes = null;
	//ECM配置文件存放地址
	public static String configPath;

	//业务类型添加验证公开密钥
	public static String publicKey;

	// 影像查询分页大小
	public static int QUERY_SIZE = 10;

	// 是否记录资料日志
	public static String IS_RECORD_IMAGE = "0";

	//tif图片保存格式,0:tif格式不变，1：tif变成jpg
	public static String tifToJpg = "0";

	//支持文件拆分的文件类型以@符号分隔
	public static String SUPPORT_SPLIT_FILE = "TIF@PDF";

	//设置拆分的文件类型，以@符合分隔（如：TIF@PDF），为空则不拆分，如拆分，则拆分后文件转为JPG
	public static String SPLIT_FILE = "";

	//文件拆分后是否保留原件，1:保留，0:不保留
	public static String KEEP_FILE = "1";

	//PDF、TIF是否首页生成缩略图，1:生成，0:不生成
	public static String KEEP_THUM = "0";

	//初始化展示的时候：0最佳适应，1宽适应，2高适应，3原尺寸
	public static String POSITION_NORMAL = "1";
	//双击之后:0最佳适应，1宽适应，2高适应，3原尺寸
	public static String POSITION_LARGE = "3";
	//录单随动和反漂显示模式，0代表使用框，1代表采用高亮
	public static String POSITION_MODE = "0";
	//录单随动是否同步下载,0:异步下载,1:同步下载
	public static String POSITION_SYNC = "0";
	//录单随动高亮颜色设置,red:红框,其他值为对应的英文单词黄色对应yellow
	public static String POSITION_COLOR = "red";
	//反漂字体设置
	public static String POSITION_FONT = "微软雅黑";
	//反漂字体大小
	public static String POSITION_FONTSIZE = "12";
	//反漂时文字颜色，16进制转10进制，如00FFFF(黄色)计算结果为65535，则position_fontColor=65535,新版SunImageBroseView才可以设置颜色
	public static String POSITION_FONTCOLOR = "red";

	//待签名扣取资料类型
	public static String SIGNATURE_SRCDOCCODE;
	//待签名扣取资料类型，字段名称
	public static String SIGNATURE_FIELDID;
	//签名资料归类类型
	public static String SIGNATURE_DOCCODE;


	//第三方下载缩略图到word，缩略图宽、高
	public static String downThumSize = "175*224";
	//文件存储加密标识
	public static String pageEncrytMark = "0";
	//上传是否加密
	public static String transEncrypt = "0";
	//获取文件MD5模式
	public static String fileMd5Mode = "0";
	//主键查询为空是否显示空批次 0：不显示 1：显示 默认0
	public static String emptyShow = "0";
	//第三方上传是否支持断点续传(为了和以前程序做兼容 根据对接系统使用jar包决定该参数) 0：不支持 1：支持
	public static String uploadImageBreakType = "0";
	//接口失败枚举
	public static Map<String, String> interfaceFailMap = null;

	public static String isRedis = "0";

	public static String hideNorightNode = "0";

	public static String isShortConn = "0";
	public static String shortConnUrl = null;

	public static int uploadTaskNum = 3;
	public static int downloadTaskNum = 3;
	public static int download = 30;
	public static int upload = 30;


	public static int threadTimeOut = 60;

	public static ExecutorService downloadPool;
	public static ExecutorService uploadPool;

	//是否开启图像智能分类
	public static String IMAGE_SMART_CLASSIFY = "0";
	//是否开启图像智能分类引擎地址
	public static String IMAGE_SMART_URL = "";

	//是否开启图像字段权限控制，目前仅限嵌入式查询
	public static String FILEDS_RIGHT_FLAG = "0";
	public static int loginInterval = 0;
	public static int loginErrorNum = 0;

	public static long  loginTimeLimit = 60;

	//上传提交锁  0关闭  1打开
	public static String SUBMIT_LOCK = "0";

	//0:查重中 1:待查重 2:重复 3:不重复
	public static Integer CHECK_REPEAT_STATE_INIT = 0;
	public static Integer CHECK_REPEAT_STATE_WAIT = 1;
	public static Integer CHECK_REPEAT_STATE_REPEAT = 2;
	public static Integer CHECK_REPEAT_STATE_NOREPEAT = 3;

	//查重审核状态 0:不通过 1:通过 2:待审核
	public static Integer CHECK_REPEAT_EXAMINE_NOPASS = 0;
	public static Integer CHECK_REPEAT_EXAMINE_PASS = 1;
	public static Integer CHECK_REPEAT_EXAMINE_WAIT = 2;

	//系统名称
	public static String SOURCESYS = "SunICMS";
	//域名
	public static String DOMAINNAME = "";
	//查重接口地址-不直接返回查重结果
	public static String ANTIFRAUDDET = "";
	//查重接口地址-返回查重结果
	public static String ANTIFRAUDDETNOW = "";
	//查重结果接口地址
	public static String ANTIFRAUDDETRES = "";
	//手动查重接口直接返回查重结果地址
	public static String antiFraudDetNowBase64 = "";

	//自动刷新查重状态WebSocketUrl
	public static String WebSocketUrl = "";
	public static String shortHost = "";
	public static String trmSocketMapping = "";
	public static String noAutoClassifyAppCode = "";
	//查重状态更新端口：后端webSocket端口
	public static Integer WebSocketServerPort = 8018;
}
