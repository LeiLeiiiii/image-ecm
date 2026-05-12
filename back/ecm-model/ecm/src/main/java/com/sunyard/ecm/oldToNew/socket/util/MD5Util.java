package com.sunyard.ecm.oldToNew.socket.util;

import com.sunyard.ecm.dto.EcmBusExtendDTO;
import com.sunyard.ecm.dto.FileAndSortDTO;
import com.sunyard.ecm.oldToNew.constant.ApiConstants;
import com.sunyard.ecm.oldToNew.socket.constant.OldToNewConstant;
import com.sunyard.ecm.util.Md5Utils;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class MD5Util {

	/**
	 * 获取文件MD5码
	 *
	 * @return
	 * @throws Exception
	 */
	public static String getFileMD5(String filePath) throws Exception {
		MessageDigest messageDigest = null;
		FileInputStream in = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			in = new FileInputStream(filePath);
			byte[] buffer = new byte[1024 * 1024];
			int len = 0;
			while ((len = in.read(buffer)) > 0) {
				messageDigest.update(buffer, 0, len);
			}
		} catch (Exception e) {
			throw new Exception("获取文件[" + filePath + "+]MD5码异常!",e);
		} finally {
            if (in != null) {
                try {
                    in.close();
                }catch (IOException e){
                    throw new Exception("关闭文件流异常!",e);
                } finally {
                    in = null;
                }
            }
		}
		return toHexString(messageDigest.digest());
	}

	public static String getMd5(MultipartFile file) {
		try (InputStream inputStream = file.getInputStream()) {
			return Md5Utils.calculateMD5(inputStream);
		} catch (IOException | NoSuchAlgorithmException e) {
			log.error("获取md5值异常",e);
			return null;
		}
	}

	/**
	 * 获取文件md5并且压缩
	 * @param dto
	 * @param ecmBusExtendDTOS
	 * @return
	 */
	public static Map<String, FileAndSortDTO> getMd5(List<FileAndSortDTO> dto, EcmBusExtendDTO ecmBusExtendDTOS) {
		ArrayList<FileAndSortDTO> objects = new ArrayList<>();

		for(FileAndSortDTO f:dto) {
			MultipartFile file = null;
			try {
				if (f.getMultipartFile() != null) {
					file = f.getMultipartFile();
				}
				if (file != null) {
					String sourceFileMd5 = getMd5(file);
					String fileMd5 = sourceFileMd5;
					if (ecmBusExtendDTOS.getIsCompress().equals(ApiConstants.COMPRESS.toString())) {
						compress(file, ecmBusExtendDTOS);
						fileMd5 = getMd5(file);
					}
					f.setMultipartFile(file);
					f.setFileMd5(fileMd5);
					f.setSourceFileMd5(sourceFileMd5);
					objects.add(f);
				}
			} catch (Exception e) {
				log.error("获取md5值异常",e);
			}
		}
		Map<String, List<FileAndSortDTO>> collect = objects.stream().collect(Collectors.groupingBy(FileAndSortDTO::getSourceFileMd5));
		Map<String, FileAndSortDTO> ret = new HashMap<>();
		for(String md5:collect.keySet()){
			List<FileAndSortDTO> fileAndSortDTOS = collect.get(md5);
			ret.put(md5,fileAndSortDTOS.get(0));
		}
		return ret;
	}

	/**
	 * 文件按照压缩比和压缩质量进行压缩
	 */
	public static MultipartFile compress(MultipartFile file, EcmBusExtendDTO ecmBusExtendDTOS) throws IOException {
		Integer compressSize = Integer.valueOf(ecmBusExtendDTOS.getCompressSize());
		Double compressValue = Double.valueOf(ecmBusExtendDTOS.getCompressValue());

		// 首次读取输入流（用于获取图像尺寸）
		BufferedImage startOriginalImage;
		try (InputStream inputStream = file.getInputStream()) {
			startOriginalImage = ImageIO.read(inputStream);
		}

		// 特殊文件格式originalImage为null,所以不走压缩
		if (startOriginalImage != null) {
			int width = startOriginalImage.getWidth();
			int height = startOriginalImage.getHeight();

			// 计算宽高比例
			double ratio = (double) width / height;
			DecimalFormat decimalFormat = new DecimalFormat("#.00");
			String formattedResult = decimalFormat.format(ratio);
			Double newRatio = Double.valueOf(formattedResult);

			// 如果宽度大于压缩阈值，则按比例缩小
			if (width > compressSize && height > compressSize) {
				if (height >= width) {
					width = compressSize;
					height = (int) (height * newRatio);
				} else {
					height = compressSize;
					width = (int) (height * newRatio);
				}
			}

			// 第二次读取输入流（用于压缩处理）
			try (InputStream inputStream = file.getInputStream();
				 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

				Thumbnails.of(inputStream)
						.size(width, height)
						.outputQuality(compressValue)
						.toOutputStream(outputStream);

				byte[] thumbnailBytes = outputStream.toByteArray();

				return new ByteArrayMultipartFile(
						"file",
						file.getOriginalFilename(),
						"image/jpeg",
						thumbnailBytes);
			}
		}
		return file;
	}

	public static String toHexString(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
			sb.append(hexChar[b[i] & 0x0f]);
		}
		return sb.toString();
	}

	public static char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };


    public static String encrypt(String inStr) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		if (inStr == null || "".equals(inStr)) {
			throw new IllegalArgumentException("Parameter[inStr] can't be null.");
		}
		MessageDigest md5 = MessageDigest.getInstance("MD5");
//		char[] charArray = inStr.toCharArray();
//		byte[] byteArray = new byte[charArray.length];
//		for (int i = 0; i < charArray.length; i++) {
//			byteArray[i] = (byte) charArray[i];
//		}

		byte[] byteArray = inStr.getBytes("UTF-8");

		byte[] md5Bytes = md5.digest(byteArray);
		return byte2Hex(md5Bytes);
	}

	public static String byte2Hex(byte[] bytes) {
		StringBuilder hexSb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			int val = bytes[i] & 0XFF;
			if (val < 16) {
				hexSb.append('0');
			}
			hexSb.append(Integer.toHexString(val));
		}
		return hexSb.toString();
	}

    public static String encryptHmacMd5Str(String key, String inStr) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException{
    	if (inStr == null || "".equals(inStr)) {
			throw new IllegalArgumentException("Parameter[inStr] can't be null.");
		}
    	if (key == null || "".equals(key)) {
    		throw new IllegalArgumentException("Parameter[key] can't be null.");
    	}

//    	char[] charArray = inStr.toCharArray();
//		byte[] byteArray = new byte[charArray.length];
//		for (int i = 0; i < charArray.length; i++) {
//			byteArray[i] = (byte) charArray[i];
//		}

		byte[] byteArray = inStr.getBytes("UTF-8");

		SecretKeySpec sk = new SecretKeySpec(key.getBytes(), "HmacMD5");
		Mac mac = Mac.getInstance("HmacMD5");
		mac.init(sk);

		byte[] md5Bytes = mac.doFinal(byteArray) ;
		return byte2Hex(md5Bytes);
    }


	public static String getFileMD5s(String filePath) throws Exception{
		if("0".equals(OldToNewConstant.fileMd5Mode)){
			return getMD5ByFileContent(filePath);
		}else{
			return getMD5ByFileLength(filePath);
		}
	}

	/**
	 * 根据文件内容获取文件MD5码
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public static String getMD5ByFileContent(String filePath) throws Exception {
		MessageDigest messageDigest = null;
		FileInputStream in = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			in = new FileInputStream(StringUtil.pathManipulation(filePath));
			byte[] buffer = new byte[1024 * 1024];
			int len = 0;
			while ((len = in.read(buffer)) > 0) {
				messageDigest.update(buffer, 0, len);
			}
		} catch (Exception e) {
			throw new Exception("根据文件内容获取MD5：获取文件[" + filePath + "+]MD5码异常!",e);
		} finally{
			IOUtils.closeInputStream(in);
		}
		return toHexString(messageDigest.digest());
	}

	/**
	 * 根据文件长度获取文件MD5
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public static String getMD5ByFileLength(String filePath) throws Exception{
		try {
			File file = new File(StringUtil.pathManipulation(filePath));
			long length = file.length();
			return encrypt(String.valueOf(length));
		} catch (Exception e) {
			throw new Exception("根据文件长度获取MD5：获取文件[" + filePath + "+]MD5码异常!",e);
		}
	}
}
