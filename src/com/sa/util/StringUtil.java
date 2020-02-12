package com.sa.util;

import java.util.Map;
import java.util.regex.Pattern;

public class StringUtil {

	public static boolean isEmpty(String content){
		return content == null || content.length() == 0;
	}

	public static boolean isEmpty(Map map){
		return map == null || map.size() == 0;
	}

	public static String subStringIp(String str) {
		return str.substring(str.indexOf("/")+1,str.indexOf(":"));
	}
	
	public static String subStringAddress(String str) {
		return str.substring(str.indexOf("/")+1,str.length());
	}

	public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
  }
}
