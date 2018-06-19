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

}
