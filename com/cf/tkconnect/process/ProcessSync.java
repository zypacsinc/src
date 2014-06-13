package com.cf.tkconnect.process;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;


import com.cf.tkconnect.data.TKUnifierMetaData;
import com.cf.tkconnect.data.form.BPAttributeData;
import com.cf.tkconnect.data.form.DataObject;
import com.cf.tkconnect.data.process.ProcessBPXMLTemplate;
import com.cf.tkconnect.data.process.ProcessUnifierObjectInfo;
import com.cf.tkconnect.database.SqlUtils;
import com.cf.tkconnect.process.ProcessSync;
import com.cf.tkconnect.process.ResponseObject;
import com.cf.tkconnect.unifierservices.UnifierServices;
import com.cf.tkconnect.util.DataUtils;
import com.cf.tkconnect.util.FileUtils;
import com.cf.tkconnect.util.InitialSetUp;
import com.cf.tkconnect.util.WSConstants;
import com.cf.tkconnect.util.WSUtil;
import com.cf.tkconnect.wsdl.WSDLServices;

import  org.apache.commons.lang3.StringUtils;

public class ProcessSync {
	private static final Logger logger = Logger.getLogger(ProcessSync.class);
	// this will sync up with unifier
	  int company_id=1;
/*	  String shortName;
	  String companyUrl;
	  String authCode;
	  */
	  String companyDir;
	  int data_set =0;
	  
	  String serviceName="UnifierWebServices";// default value
	  static boolean sync = false;
	  
	  Map<String,String> parameterMap = new HashMap<String,String>();
	  
	  public ProcessSync(){
		 
		  logger.debug(" The company id is being set  : ");
	  }
	
	  public ProcessSync( String serviceName){
		
		  this.serviceName = serviceName;
	  }
	
	  
	  public String getAllStudioInfo() {
		  try{
			  logger.debug(" The getAllStudioInfo  is being set ");
		 // now get studio list
//		     ResponseObject rc = ping(InitialSetUp.company.get("shortname"),InitialSetUp.company.get("authcode"),InitialSetUp.company.get("company_url"));
		   
			 processStudioInfo();
		  }catch(Exception e){
			  logger.error(e,e);
			   return "{ \"errors\": \"Errors occurred\" }";
		  }
		  Iterator<String> it = parameterMap.keySet().iterator();
		  StringBuilder buf = new StringBuilder("{");
		  int count = 0;
		  while(it.hasNext()){
			  if(count > 0)
				  buf.append(",");
			  String key = it.next();
			  buf.append("\"").append(key).append("\":\"").append(parameterMap.get(key)).append("\"");
			  count++;
		  }
		  buf.append("}");
		  return buf.toString();
	  }
	  
	 
	   public  synchronized void processStudioInfo() throws Exception{
		   logger.debug(" The processStudioInfo "+sync);
		  // if(sync)			   return;
		   sync = true;
			ResponseObject  responseObjStudio = getObjectList( "studio_list",  "");
			logger.debug(" The processStudioInfo  response :"+responseObjStudio.getXmlcontents());
			 DataUtils.createServiceRecord("data", "studio_list",responseObjStudio.getStatusCode() );
			checkError(responseObjStudio,"studio_list");
			// all is fine 
			// save this file 
			FileUtils.writeXMLContents("sync","studio", company_id, responseObjStudio.getXmlcontents());
			ProcessUnifierObjectInfo pod = new ProcessUnifierObjectInfo(   responseObjStudio.getXmlcontents() );
			String[] str = pod.processStudioXMLData();
			logger.debug(" The processStudioInfo  str :"+str[0]+"   :: "+str[1]);
	// blocking for connect dont need bp_list		
			if(str != null && (str[0].trim().length() > 0 || str[1].trim().length() > 0 )){// there is a new list 
				// make a getBPlist call
				ResponseObject  responseObjData = getObjectList( "bp_list", "");
				 DataUtils.createServiceRecord("data", "bp_list",responseObjData.getStatusCode() );
				logger.debug(" The processStudioInfo bp "+responseObjData.getXmlcontents());
				checkError(responseObjStudio,"bp_list");
				pod.setXMLData( responseObjData.getXmlcontents());
				String bpstr = pod.processBPXMLData();
				parameterMap.put("new_studio", str[0]);
				parameterMap.put("update_studio", str[1]);
				parameterMap.put("new_bp", bpstr);
				// for each of the BP's you can sync up the BP's
			}
			sync = false;// release it
	   }
	   
	   public void proceeStudioDEInfo() throws Exception {
			
			// sync up data def & DE
			ResponseObject  responseObjData = getObjectList( "data_definitions", "");
			 DataUtils.createServiceRecord("data", "data_definitions",responseObjData.getStatusCode() );
			checkError(responseObjData,"data_definitions");
			ProcessUnifierObjectInfo pod = new ProcessUnifierObjectInfo(   responseObjData.getXmlcontents() );
			//pod.setXMLData( responseObjData.getXmlcontents());
			String[] str = pod.processDDDEXMLData();
			FileUtils.writeXMLContents("sync","dd_de", company_id, responseObjData.getXmlcontents());

			logger.debug(" The processStudioInfo end of DE ***************");
			if(str != null && (str[0].trim().length() > 0 || str[1].trim().length() > 0 )){// there is a new list
				parameterMap.put("new_dd", str[0]);
				parameterMap.put("new_de", str[1]);
			}
			
	   }
	   
	   private  synchronized void setStudioInfo() throws Exception{
		   logger.debug(" The processStudioInfo "+sync);
		  // if(sync)			   return;
		   sync = true;
			ResponseObject  responseObjStudio = getObjectList( "studio_list",  "");
			logger.debug(" The processStudioInfo  response :"+responseObjStudio.getXmlcontents());
			DataUtils.createServiceRecord("data", "studio_list",responseObjStudio.getStatusCode() );
			checkError(responseObjStudio,"studio_list");
			// all is fine 
			// save this file 
			FileUtils.writeXMLContents("sync","studio", company_id, responseObjStudio.getXmlcontents());
			ProcessUnifierObjectInfo pod = new ProcessUnifierObjectInfo(   responseObjStudio.getXmlcontents() );
			String[] str = pod.processStudioXMLData();
			logger.debug(" The processStudioInfo  str :"+str[0]+"   :: "+str[1]);
			if(str != null && (str[0].trim().length() > 0 || str[1].trim().length() > 0 )){// there is a new list 
				// make a getBPlist call
				parameterMap.put("new_studio", str[0]);
				parameterMap.put("update_studio", str[1]);
				// for each of the BP's you can sync up the BP's
			}
			// sync up data def & DE
			ResponseObject  responseObjData = getObjectList( "data_definitions", "");
			DataUtils.createServiceRecord("data", "data_definitions",responseObjStudio.getStatusCode() );
			checkError(responseObjStudio,"data_definitions");
			pod.setXMLData( responseObjData.getXmlcontents());
			str = pod.processDDDEXMLData();
			FileUtils.writeXMLContents("sync","dd_de", company_id, responseObjData.getXmlcontents());
			logger.debug(" The processStudioInfo end of DE ***************");
			if(str != null && (str[0].trim().length() > 0 || str[1].trim().length() > 0 )){// there is a new list
				parameterMap.put("new_dd", str[0]);
				parameterMap.put("new_de", str[1]);
			}
			sync = false;// release it
	   }
	   
	   public String synchBP(String prefixes ) throws Exception{
		   // input comma separated bp prefixes
		   if(prefixes == null || prefixes.trim().length() == 0)
			   return "[]";
		   List<String> bpPrefix = new ArrayList<String>();
		   String[] str =StringUtils.splitPreserveAllTokens(prefixes);
		   for(String s : str)
			   bpPrefix.add(s);
		   return synchBP(true,bpPrefix);
	   }
	   
	   public String synchBP(boolean setCompany, List<String> bpPrefix) {
		   // returns JSON of 
		   try{
			   Map<String,String> responseMap = new HashMap<String,String>();
			   List<String> errbps = new ArrayList<String>();
			   Map<String, String> bpnames = DataObject.getBPNames(bpPrefix); 
			   Iterator<String> it = bpnames.keySet().iterator();
			   while(it.hasNext()){
				   String prefix = it.next();
				   String bpname = bpnames.get(prefix);
				   ResponseObject  responseObjData = getObjectList( "bp_info", bpname);
				   DataUtils.createServiceRecord("bp_info", prefix,responseObjData.getStatusCode() );
				  if( responseObjData.getStatusCode() != 200){
					  errbps.add(bpname);
					  continue;
				  }
				  // save the file & update database
				  responseMap.put(prefix, responseObjData.getXmlcontents());
				  FileUtils.writeXMLContents("bp_info",prefix, company_id, responseObjData.getXmlcontents());
			   
			   }//for
			   it = responseMap.keySet().iterator();
			   StringBuilder buf = new StringBuilder("[");
			   int count = 0;
			   while(it.hasNext()){
				   if(count > 0)
					   buf.append(",");
				   buf.append("{");
				   String prefix = it.next();
				   String bpname = bpnames.get(prefix);
				   String   xmldata = responseMap.get(prefix);
				   BPAttributeData ad = new BPAttributeData(this.company_id,xmldata, prefix, bpname);
				   buf.append("\"bp_prefix\":\"").append(prefix).append("\",");
				   buf.append("\"bp_name\":\"").append(WSUtil.filter(bpname)).append("\",");
				   String json= ad.updateAndCompare();
				   buf.append("\"bp_info\":").append(json).append("}\n");
				   count++;
			   }
			   buf.append("]");
			   return buf.toString(); 
		   }catch(Exception e){
			   logger.error(e,e);
			   return "{ \"errors\": \"Errors occurred\" }";
		   }
	   }
	   
	   public String getBPAttributes( String bpPrefix, boolean get_latest) {
		   // returns JSON of one BP
		   try{
		   logger.debug("getBPAttributes **** pre: "+bpPrefix);
		   Map<String,String> map = DataObject.getBPNameAttr( bpPrefix);
		   if(get_latest)
			   map.put("notfound","true");
		   String bpname = map.get("studio_name");
		   logger.debug("getBPAttributes : "+bpname+" :: "+map);
		   if(bpname == null || bpname.trim().length()==0)
			   return "{\"errors\":\"design not found\"}";
		   StringBuilder buf = new StringBuilder("{");
		   BPAttributeData ad = null;
		   String json= "";
		   buf.append("\"bp_prefix\":\"").append(bpPrefix).append("\",");
		   buf.append("\"bp_name\":\"").append(WSUtil.filter(bpname)).append("\",");
		   if(map.containsKey("notfound") && map.get("notfound").equalsIgnoreCase("true")){
			   ResponseObject  responseObjData = getObjectList( "bp_info", bpname);
			   DataUtils.createServiceRecord("bp_info", bpPrefix,responseObjData.getStatusCode() );
			   logger.debug("getBPAttributes ****-- "+responseObjData);
			   logger.debug("getBPAttributes ^^^^^^^-- "+responseObjData.getStatusCode());
 			  if( responseObjData.getStatusCode() == 200){
				   String   xmldata = responseObjData.getXmlcontents();
				   logger.debug("getBPAttributes **** "+xmldata);
				   ad = new BPAttributeData(this.company_id,xmldata, bpPrefix, bpname);
			  }
 			}else// found we can extract from 
				   ad = new BPAttributeData(this.company_id, bpPrefix, bpname);
		   
		   json= ad.getAttributeDetails();

		   buf.append("\"bp_info\":").append(json).append("}\n");	
		  logger.debug("getBPAttributes ****ccccc "+buf);
		   return buf.toString(); 
		   }catch(Exception e){
			   logger.error(e,e);
			   return "{ \"errors\": \"Errors occurred\" }";
		   }
	   }

	   public ProcessBPXMLTemplate getBPAttributeInfo( String bpPrefix, boolean get_latest) {
		   // returns JSON of one BP
		   try{
		   logger.debug("getBPAttributeDetail **** pre: "+bpPrefix);
		   Map<String,String> map = DataObject.getBPNameAttr( bpPrefix);
		   if(get_latest)
			   map.put("notfound","true");
		   String bpname = map.get("studio_name");
		   logger.debug("getBPAttributes : "+bpname+" :: "+map);
		   if(bpname == null || bpname.trim().length()==0)
			   return null; 
		   StringBuilder buf = new StringBuilder("{");
		   BPAttributeData ad = null;
		
		   if(map.containsKey("notfound") && map.get("notfound").equalsIgnoreCase("true")){
			   ResponseObject  responseObjData = getObjectList( "bp_info", bpname);
			   DataUtils.createServiceRecord("bp_info", bpPrefix,responseObjData.getStatusCode() );
			   logger.debug("getBPAttributes ****-- "+responseObjData);
			   logger.debug("getBPAttributes ^^^^^^^-- "+responseObjData.getStatusCode());
 			  if( responseObjData.getStatusCode() == 200){
				   String   xmldata = responseObjData.getXmlcontents();
				   logger.debug("getBPAttributes **** "+xmldata);
				   ad = new BPAttributeData(this.company_id,xmldata, bpPrefix, bpname);
			  }
 			}else// found we can extract from 
				   ad = new BPAttributeData(this.company_id, bpPrefix, bpname);
		   ProcessBPXMLTemplate bpifo = ad.getBPDesignDetails();

		
		  logger.debug("getBPAttributeInfo ****ccccc "+buf);
		   return bpifo; 
		   }catch(Exception e){
			   logger.error(e,e);
			   return null;
		   }
	   }

	   public String synchProjects() {
		   // returns JSON of 
		   try{
			   ResponseObject  responseObjData = getObjectList( "shell_info", "");
			    if( responseObjData.getStatusCode() != 200){
			    	String err = WSUtil.filter(responseObjData.getErrors());
					  return "[{\"errors\";\""+err+"\",\"statuscode\":"+responseObjData.getStatusCode()+" }]";
			    }
			    DataUtils.createServiceRecord("ObjectList", "shell_info",responseObjData.getStatusCode() );
				  // save the file & update database
				ProcessUnifierObjectInfo pod = new ProcessUnifierObjectInfo(   responseObjData.getXmlcontents() );
				//FileUtils.writeXMLContents("sync","projects", company_id, responseObjData.getXmlcontents());
				String projstr = pod.processProjectXMLData()   ;
				//String str ="{\"new_projects\":\""+  projstr+"\"}"; 
			   
			   return projstr; 
		   }catch(Exception e){
			   logger.error(e,e);
			   return "[{ \"errors\": \"Errors occurred :"+e.getMessage()+"\" }]";
		   }
	   }

	  
	  
		private ResponseObject getObjectList(String objectName, String objectCondition) throws Exception{
			  logger.debug("getObjectList ****xxxxx "+InitialSetUp.company.get("company_url") );
			UnifierServices us = new UnifierServices(InitialSetUp.company.get("company_url"), "getObjectList");  
			Object[] objarr = new Object[6];
			objarr[0] = InitialSetUp.company.get("shortname");
			objarr[1] = InitialSetUp.company.get("authcode");
			objarr[2] = objectName;
			objarr[3] = null;
			objarr[4] = objectCondition;
			objarr[5] = null;
			List<Map<String,String>> params = WSDLServices.getMethods().get("getObjectList");
			ResponseObject rc =us.callService(params, objarr);
//			WebLinkConnector uc = new WebLinkConnector(companyUrl,  this.serviceName);
//			ResponseObject rc =  uc.getObjectList(shortName, authCode, objectName,null, objectCondition,	null);
			// create entry in sys_info
			return rc;
			
		}
		
		public String testPing(String sn, String ac, String url){
			try{
				ResponseObject rc = ping(sn,ac,url);
				int code = rc.getStatusCode() ;
				return "{\"statuscode\":"+code+",\"errors\":\""+rc.getErrors() +"\" }";
			}catch(Exception e){
				logger.error(e,e);
				return "{\"errors\":\""+e.getMessage()+"\"}";
			}
		}
		
		public ResponseObject ping(String sn, String ac, String url) throws Exception{
			if(logger.isDebugEnabled() )
			  logger.debug("test ****xxxxx ping for url: "+url);
			UnifierServices us = new UnifierServices(url, "Ping");  
			Object[] objarr = new Object[6];
			objarr[0] = sn;
			objarr[1] = ac;
			objarr[2] = ac;
			objarr[3] = null;
			
			List<Map<String,String>> params = WSDLServices.getMethods().get("Ping");
			ResponseObject rc =us.callService(params, objarr);
			if(logger.isDebugEnabled() )
				  logger.debug("test result ****xxxxx ping for url: "+rc.getErrors()+"  code :"+rc.getStatusCode());

			// create entry in sys_info
			
			return rc;
			
		}

		private void checkError(ResponseObject  responseObj, String name) throws Exception{
			if(responseObj.getStatusCode() != WSConstants.OK_CODE){
				// now we need to process the 
				String error =  WSUtil.filter(responseObj.getErrors());
				
				throw new Exception("Error processing  "+name+" "+responseObj.getStatusCode()+" "+error);
			}
		}
		  
		
		public String createSysRecord(int resp_code, String data_type)  {		
			Connection conn = null;
			PreparedStatement ps  = null;
			try {
			
					 conn = SqlUtils.getConnection();
					 ps  = conn.prepareStatement("insert into sys_info (company_id,data_type,response_code) values (?,?,?)" );
			         ps.setInt(1,    company_id);
			         ps.setString(2,     data_type);
			         ps.setInt(3,     resp_code);
			       
			         int insertid = ps.executeUpdate();
			     	logger.debug(" Create Sys Info record "+insertid);			    	
			} catch (Exception e){
				logger.error(e,e);
				return "{ \"data_type\": \""+data_type+"\", \"errors\": \"Errors occurred\" }"  ;
			} finally {
				
		    	  SqlUtils.closeStatement(ps);
		    	  SqlUtils.closeConnection(conn);
			}
			return "{ \"company_id\": \""+this.company_id+"\"}"  ;
		}
	
}
