package com.stanko.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EMailHelper {
	
	public static boolean isValidEmail(String email) {
	    String expression = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
	    
	    Pattern p = Pattern.compile(expression);
	    Matcher m = p.matcher(email);
	    
	    return m.matches();
	}
}
