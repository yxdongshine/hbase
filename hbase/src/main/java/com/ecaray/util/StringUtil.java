package com.ecaray.util;

public class StringUtil {
	
	public static boolean strIsEmpty(String str){
		boolean isEmpty = false ;
		if(null == str
				|| 0 == str.length()
				|| "".equals(str.trim())){
			isEmpty = true;
		}
		return isEmpty;
	}

	public static boolean isNull(String paramString) {
		return ((paramString == null) || (paramString.equals(""))
				|| (paramString.equalsIgnoreCase("null")) || (paramString
					.equalsIgnoreCase("undefined")));
	}

	public static boolean isNotNull(String paramString) {
		return (!(isNull(paramString)));
	}
	
	public static boolean isInteger(String paramString) {
		if (isNotNull(paramString))
			return paramString.matches("^-?\\d+$");
		return false;
	}
}
