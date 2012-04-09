package org.bclove.common.utils;

import java.net.URI;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLUtil {
	public static String getParameter(String url,String parameter){
		String reg = "(^|&|\\?)"+parameter+"=([^&]*)(&|$)";
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(url);
		if(matcher.find()){
			try {
				String src = URLDecoder.decode(matcher.group(2),"UTF-8");
				return src;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	public static boolean isValid(String link){
		if(link==null||link.trim().length()==0){
			return false;
		}
		try {
			URI u = new URI(link);
			String host = u.getHost();
			if(host==null){
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

}
