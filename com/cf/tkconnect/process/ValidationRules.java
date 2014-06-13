package com.cf.tkconnect.process;

import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.process.ValidationRules;


public class ValidationRules {

      static Log logger =
    	LogSource.getInstance(ValidationRules.class);
	
	public static final int ALPHA = 1;
	public static final int ALPHA_DIGIT = 2;
	public static final int ALPHA_SPACE = 3;
	public static final int ALPHA_DIGIT_SPACE = 4;
	public static final int ALPHA_DIGIT_DASH = 5;
	
	public static final int UNDERSCORE = 95;
	public static final int DOLLAR = 36;
	public static final int PERCENT = 37;
	public static final int AMPERSAND = 38;
	public static final int SINGLE_QUOTE = 39;
	public static final int OPEN_BRACKET = 40;// )
	public static final int CLOSE_BRACKET = 41;// (
	public static final int STAR = 42;
	public static final int PLUS = 43;
	public static final int DASH = 45;
	public static final int AT = 64;
	public static final int COMMA = 46;
	public static final int OPEN_BOX_BRACKET = 91;// ]
	public static final int CLOSE_BOX_BRACKET = 93;// [
		
	public static final String SPECIAL_CHARS ="!@#$%^&*()-_=+[]\\{}|;':\",./<>?";
	public static final String NON_PERMITTED_CHARS ="!@#$%^&*()=+[]\\{}|;':\",./<>?";
	
	public static final int MAX_LENGTH_16 = 16;
	public static final int MAX_LENGTH_32 = 32;
	public static final int MAX_LENGTH_64 = 64;
	
	
	
	public static boolean checkString(String inptstr, int type ){
		
		if(inptstr == null || inptstr.length() == 0) return true;
		final char[] chars = inptstr.toCharArray();
		if(chars == null) return true;
		for(int i = 0; i < chars.length; i++){
			switch(type){
				case ALPHA:	if(!Character.isLetter(chars[i]))
							   return false;
							 break;
				case ALPHA_DIGIT:	if(!Character.isLetterOrDigit(chars[i]))
							   return false;
							 break;
				case ALPHA_SPACE:	if(!Character.isSpaceChar(chars[i]) && !Character.isLetter(chars[i])  )
							   return false;
							 break;
				case ALPHA_DIGIT_SPACE:	if(!Character.isSpaceChar(chars[i]) && !Character.isLetterOrDigit(chars[i])  )
							   return false;
							 break;
				case ALPHA_DIGIT_DASH:	if(!isDash(chars[i]) && !Character.isLetterOrDigit(chars[i])  )
							   return false;
							 break;
			}//switch
		}
		
		return true;
		
	}
	
	public static boolean isDash(char c){
		if(c == DASH) return true;
		return false;
	}

	public static boolean isValidChar(String inptstr){
		if(inptstr == null || inptstr.length() == 0) return true;
		final char[] chars = inptstr.toCharArray();
		if(chars == null) return true;
		for(int i = 0; i < chars.length; i++){
			/*switch(type){
				case ALPHA:	if(!Character.isLetter(chars[i]))
							   return false;
								 break;
			}*/
		}//for
		return true;
	}

	public static String stripComma(String inptstr ){
		
		if(inptstr == null || inptstr.length() == 0) return inptstr;
		final char[] chars = inptstr.toCharArray();
		if(chars == null) return inptstr;
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < chars.length; i++){
			if(chars[i] != COMMA)
				buf.append(chars[i]);
		}
		return buf.toString();
	}	
	
	public static boolean isValidAmt(String inputstr, boolean strict) {
		// note we are permitting commas in value currently blocked
	    boolean ret = false;
		if(inputstr == null || inputstr.length() == 0) return (!strict);
		//String val = stripComma(inputstr);
		String val = inputstr;
		try{
		
			Double dbx = new Double(val);
			if (dbx != null)  {
				 dbx.doubleValue();
				//if(dt <= CostImport.MAX_AMOUNT)
					ret = true;
			}
		}catch(Exception de){
			 logger.debug("Error in creating line item, amount value:"+val+" is not correct.");		 	
		}
		return ret;
	
	}

	public static boolean isValidAmt(String val) {
		return isValidAmt( val, true);
	}


	public static double getDoubleValue(String inputstr) {
	
		String val = stripComma(inputstr);
		if(val == null || val.length() == 0) return (0.0);
		Double dbx = null;
		try{
			dbx =  new Double(val);
		}catch(Exception de){
			return (0.0);		 	
		}
		if(dbx == null) return (0.0);		 	
		return dbx.doubleValue();
	
	}

}
