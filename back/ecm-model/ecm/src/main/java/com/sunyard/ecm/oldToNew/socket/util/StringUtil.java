package com.sunyard.ecm.oldToNew.socket.util;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

	public static final String MSG_GET_MESSAGE_FAILDED = "Get message failded, message={x}, i={x}, pos={x}, replace={x}.";
	
	public static String getMessage(String message, String mark, Object... args) {
		StringBuilder msg = new StringBuilder();
		msg.append(message);
		if (args != null && args.length > 0) {
			for (int i = 0, pos = 0; i < args.length; i++) {
				String str = String.valueOf(args[i]);
				int start = msg.indexOf(mark, pos);
				int end = start + mark.length();
				if (start < 0 || end > msg.length()) {
					throw new RuntimeException(getMessage(
							MSG_GET_MESSAGE_FAILDED, "{x}", message, i, pos, str));
				} else {
					msg.replace(start, end, str);
					pos = start + str.length();
				}
			}
		}
		return msg.toString();
	}
	
	public static String inputStream2String(InputStream input,String encode) throws IOException{
		return IOUtils.toString(input,encode) ;  
	}
	
	/**
	 * XSS校验是否含有特殊字符
	 * @param temp
	 * @return
	 */
	public static String check(String temp){
		
		//[`~!#$%^&*()\\+\\=\\{}|\"?><【】\\/r\\/n]
		//[`~!@#$%^&*()\\+\\=\\{}|:\"?><【】\\/r\\/n]
		//String regex = "[`~!$%^*()\\+\\=\\{}\"?><]";
		//String regex = "[`~!@#$%^&()\\+\\【】\\/\r/\\/\n/]";
		String regex = "[`~!#$%^&+【】\r\n]";
		Pattern pa = Pattern.compile(regex);

		if(temp!=null){
			Matcher ma = pa.matcher(temp);

			if(ma.find()){

			 temp = ma.replaceAll("");

			}
		}

		return temp;
	}
	
	
//	public static String check2(String temp){
//		if(temp == null){
//			return null;
//		}
//		String regex = "[`~#$^&*\\+]";
//
//		Pattern pa = Pattern.compile(regex);
//		
//		Matcher ma = pa.matcher(temp);
//
//		if(ma.find()){
//
//		 temp = ma.replaceAll("");
//
//		}
//		return temp;
//	}
	
	/**
	 * path白名单校验，避免路径攻击等问题
	 * @param temp
	 * @return
	 */
	public static String pathManipulation(String path) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("a", "a");
        map.put("b", "b");
        map.put("c", "c");
        map.put("d", "d");
        map.put("e", "e");
        map.put("f", "f");
        map.put("g", "g");
        map.put("h", "h");
        map.put("i", "i");
        map.put("j", "j");
        map.put("k", "k");
        map.put("l", "l");
        map.put("m", "m");
        map.put("n", "n");
        map.put("o", "o");
        map.put("p", "p");
        map.put("q", "q");
        map.put("r", "r");
        map.put("s", "s");
        map.put("t", "t");
        map.put("u", "u");
        map.put("v", "v");
        map.put("w", "w");
        map.put("x", "x");
        map.put("y", "y");
        map.put("z", "z");

        map.put("A", "A");
        map.put("B", "B");
        map.put("C", "C");
        map.put("D", "D");
        map.put("E", "E");
        map.put("F", "F");
        map.put("G", "G");
        map.put("H", "H");
        map.put("I", "I");
        map.put("J", "J");
        map.put("K", "K");
        map.put("L", "L");
        map.put("M", "M");
        map.put("N", "N");
        map.put("O", "O");
        map.put("P", "P");
        map.put("Q", "Q");
        map.put("R", "R");
        map.put("S", "S");
        map.put("T", "T");
        map.put("U", "U");
        map.put("V", "V");
        map.put("W", "W");
        map.put("X", "X");
        map.put("Y", "Y");
        map.put("Z", "Z");

        map.put(":", ":");
        map.put("/", File.separator);
        map.put("\\", File.separator);
        map.put(".", ".");
        map.put("-", "-");
        map.put("_", "_");
       // map.put("&", "&");

        map.put("0", "0");
        map.put("1", "1");
        map.put("2", "2");
        map.put("3", "3");
        map.put("4", "4");
        map.put("5", "5");
        map.put("6", "6");
        map.put("7", "7");
        map.put("8", "8");
        map.put("9", "9");

        String temp = "";
        for (int i = 0; i < path.length(); i++) {
            if (map.get(path.charAt(i) + "") != null) {
                temp += map.get(path.charAt(i) + "");
            }
        }
        return temp;
    }
	
	public static boolean checkFile(String temp) {

		// [`~!#$%^&*()\\+\\=\\{}|\"?><【】\\/r\\/n]
		// [`~!@#$%^&*()\\+\\=\\{}|:\"?><【】\\/r\\/n]
		String regex = "[:*?\"<>|]";

		Pattern pa = Pattern.compile(regex);

		if (temp != null) {
			Matcher ma = pa.matcher(temp);
			if (ma.find()) {
				return false;
			}
		}
		return true;
	}
}
