package com.cf.tkconnect.util;

import com.cf.tkconnect.process.ResponseObject;
import com.cf.tkconnect.util.ReturnUtil;
import com.cf.tkconnect.util.xml.XMLObject;

import static com.cf.tkconnect.util.WSConstants.*;

public class ReturnUtil {
  
      
	static com.cf.tkconnect.log.Log logger =
    	  com.cf.tkconnect.log.LogSource.getInstance(ReturnUtil.class);

	 public static ResponseObject authError(){
	 	logger.error(INVALID_AUTH_VAL);
	  	return returnXml(INVALID_AUTH_CODE, INVALID_AUTH_VAL);
	  }

	  public static ResponseObject OK(){
	  	logger.error(OK_VAL);
	  	return returnXml(OK_CODE, EMPTY_STR);
	  }

  	  public static ResponseObject invalidUrl(){
  	  	logger.error(INVALID_SERVER_URL_VAL);
	  	return returnXml(INVALID_SERVER_URL_CODE, INVALID_SERVER_URL_VAL);
	  }
	  
  	  public static ResponseObject forbiddenUrl(){
    	  	logger.error(FORBIDDEN_URL_VAL);
  	  	return returnXml(FORBIDDEN_URL_CODE, FORBIDDEN_URL_VAL);
  	  }

  	  public static ResponseObject invalidDirectory(){
  	  	logger.error(SERVER_DIRECTORY_ERROR_VAL);
	  	return returnXml(SERVER_DIRECTORY_ERROR_CODE, SERVER_DIRECTORY_ERROR_VAL);
	  }
	  
	  
  	  public static ResponseObject invalidProjectNumber(){
  	  	logger.error(INVALID_PROJECT_NUMBER_VAL);
	  	return returnXml(INVALID_PROJECT_NUMBER_CODE, INVALID_PROJECT_NUMBER_VAL);
	  }

  	  public static ResponseObject invalidBPName(){
  	  	logger.error(INVALID_BP_NAME_VAL);
	  	return returnXml(INVALID_BP_NAME_CODE, INVALID_BP_NAME_VAL);
	  }


  	  public static ResponseObject incorrectBPName(String BPName){
  	  	
  	  	logger.error(INCORRECT_BP_NAME_VAL+BPName);
	  	return returnXml(INCORRECT_BP_NAME_CODE, INCORRECT_BP_NAME_VAL+BPName+" does not exist.");
	  }



  	  public static ResponseObject invalidBudget(){
  	  	logger.error(INVALID_XML_VAL);
	  	return returnXml(INVALID_XML_CODE, INVALID_XML_VAL);
	  }

  	  public static ResponseObject invalidXML(){
  	  	logger.error(INVALID_XML_VAL);
	  	return returnXml(INVALID_XML_CODE, INVALID_XML_VAL);
	  }

  	  public static ResponseObject notImplemented(){
  	  	logger.error(NOT_IMPLEMENTED_VAL);
	  	return returnXml(NOT_IMPLEMENTED_CODE, NOT_IMPLEMENTED_VAL);
	  }

  	  public static ResponseObject invalidRecordNo(){
  	  	logger.error(INV_RECORDNO_VAL);
	  	return returnXml(INV_RECORDNO_CODE, INV_RECORDNO_VAL);
	  }


	  public static ResponseObject returnXml(int code, String status){
	  	logger.error("Invalid status:"+status);
	  	 return returnXml( code,  status, EMPTY_STR);
	  }
	  
	  public static ResponseObject returnXml(int code, String[] statusarray){
	  	
	  	 return returnXml( code,  statusarray, EMPTY_STR);
	  }

  	  public static ResponseObject returnXml(int code, String status, String xml){
		 	String[] str = new String[1];
			str[0] = new String(status);
			return returnXml( code, str,  xml);
	  }
	  	
	  public static ResponseObject returnXml(int code, String[] status, String xml){
		  ResponseObject xobj = new ResponseObject();
		xobj.setStatusCode(code) ;
		xobj.setErrorStatus(status) ;
		xobj.setXmlcontents(xml) ;
		return xobj;
	  }
	  
	  public static ResponseObject toFileXml(XMLObject xobj){
	  	if(xobj == null) return null;
	  	ResponseObject xfileobj = new ResponseObject();
		xfileobj.setStatusCode(xobj.getStatusCode()) ;
		xfileobj.setErrorStatus(xobj.getErrorStatus()) ;
		xfileobj.setXmlcontents(xobj.getXmlcontents()) ;
		return xfileobj;
	  }
	  
	  
}// end class
	  
