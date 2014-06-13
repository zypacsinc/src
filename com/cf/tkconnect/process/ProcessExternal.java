package com.cf.tkconnect.process;

import static com.cf.tkconnect.util.FileUtils.createDateDirs;

import static com.cf.tkconnect.util.InitialSetUp.callback_poll_server;
import static com.cf.tkconnect.util.InitialSetUp.isCallbackService;
import static com.cf.tkconnect.util.InitialSetUp.nocallback;
import static com.cf.tkconnect.util.WSConstants.ADD_BPLINEITEM;
import static com.cf.tkconnect.util.WSConstants.ASSETCLASSNAME;
import static com.cf.tkconnect.util.WSConstants.AUTHENTICATIONKEY;
import static com.cf.tkconnect.util.WSConstants.BPNAME;
import static com.cf.tkconnect.util.WSConstants.CLONEPROJECTNUMBER;
import static com.cf.tkconnect.util.WSConstants.COLUMNNAME;
import static com.cf.tkconnect.util.WSConstants.COPYFROMASSET;
import static com.cf.tkconnect.util.WSConstants.COPY_FROM_USER_PREFERENCE_TEMPLATE;
import static com.cf.tkconnect.util.WSConstants.CREATE_ACTIVITIES;
import static com.cf.tkconnect.util.WSConstants.CREATE_ACTIVITIESV2;
import static com.cf.tkconnect.util.WSConstants.CREATE_ASSET;
import static com.cf.tkconnect.util.WSConstants.CREATE_BPRECORD;
import static com.cf.tkconnect.util.WSConstants.CREATE_OBJECT;
import static com.cf.tkconnect.util.WSConstants.CREATE_PROJECT;
import static com.cf.tkconnect.util.WSConstants.CREATE_USER;
import static com.cf.tkconnect.util.WSConstants.CREATE_WBS;
import static com.cf.tkconnect.util.WSConstants.FIELDNAMELIST;
import static com.cf.tkconnect.util.WSConstants.FILTERCONDITION;
import static com.cf.tkconnect.util.WSConstants.FILTERVALUELIST;
import static com.cf.tkconnect.util.WSConstants.GET_ACTIVITIES;
import static com.cf.tkconnect.util.WSConstants.GET_BPLIST;
import static com.cf.tkconnect.util.WSConstants.GET_BPRECORD;
import static com.cf.tkconnect.util.WSConstants.GET_COLUMN_DATA;
import static com.cf.tkconnect.util.WSConstants.GET_COMPLETE_BPRECORD;
import static com.cf.tkconnect.util.WSConstants.GET_EXCHANGE_RATES;
import static com.cf.tkconnect.util.WSConstants.GET_OBJECT_LIST;
import static com.cf.tkconnect.util.WSConstants.GET_PLANNINGITEM;
import static com.cf.tkconnect.util.WSConstants.GET_RESOURCE_LIST;
import static com.cf.tkconnect.util.WSConstants.GET_UDR_DATA;
import static com.cf.tkconnect.util.WSConstants.GET_USER_LIST;
import static com.cf.tkconnect.util.WSConstants.GET_WBSSTRUCTURE;
import static com.cf.tkconnect.util.WSConstants.INPUTXML;
import static com.cf.tkconnect.util.WSConstants.MAX_LINEITEMS;
import static com.cf.tkconnect.util.WSConstants.OBJECTNAME;
import static com.cf.tkconnect.util.WSConstants.PLANNINGITEM;
import static com.cf.tkconnect.util.WSConstants.PROJECTNUMBER;
import static com.cf.tkconnect.util.WSConstants.SEND_EMAIL_ONERROR;
import static com.cf.tkconnect.util.WSConstants.COMPANY_URL;
import static com.cf.tkconnect.util.WSConstants.PROPERTY_SERVICE_NAME;
import static com.cf.tkconnect.util.WSConstants.COMPANY_SHORTNAME;
import static com.cf.tkconnect.util.WSConstants.RECORDNUMBER;
import static com.cf.tkconnect.util.WSConstants.REPORTNAME;
import static com.cf.tkconnect.util.WSConstants.SCHEDULEOPTIONS;
import static com.cf.tkconnect.util.WSConstants.SERVICENAME;
import static com.cf.tkconnect.util.WSConstants.SHEETNAME;
import static com.cf.tkconnect.util.WSConstants.SHORTNAME;
import static com.cf.tkconnect.util.WSConstants.UPDATE_ACTIVITIES;
import static com.cf.tkconnect.util.WSConstants.UPDATE_ACTIVITIESV2;
import static com.cf.tkconnect.util.WSConstants.UPDATE_BPRECORD;
import static com.cf.tkconnect.util.WSConstants.UPDATE_BPRECORDV2;
import static com.cf.tkconnect.util.WSConstants.UPDATE_BPRECORDV2_OPTIONS;
import static com.cf.tkconnect.util.WSConstants.UPDATE_COLUMN_DATA;
import static com.cf.tkconnect.util.WSConstants.UPDATE_EXCHANGE_RATES;
import static com.cf.tkconnect.util.WSConstants.UPDATE_OBJECT;
import static com.cf.tkconnect.util.WSConstants.UPDATE_USER;
import static com.cf.tkconnect.util.WSConstants.UPDATE_USERGROUPMEMBERSHIP;
import static com.cf.tkconnect.util.WSConstants.UPDATE_USER_SHELL_MEMBERSHIP;
import static com.cf.tkconnect.util.WSUtil.checkInputValue;
import static com.cf.tkconnect.util.WSUtil.getDateString;
import static com.cf.tkconnect.util.WSUtil.processExtResponse;
import static com.cf.tkconnect.util.WSUtil.processObjectResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;



import com.cf.tkconnect.connector.TKConnector;

import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.log.WebLinkLogLoader;
import com.cf.tkconnect.process.ProcessExternal;
import com.cf.tkconnect.process.ProcessFileService;
import com.cf.tkconnect.process.ResponseObject;
import com.cf.tkconnect.util.FileUtils;
import com.cf.tkconnect.util.MailUtils;
import com.cf.tkconnect.util.PropertyManager;
import com.cf.tkconnect.util.InitialSetUp;
import com.cf.tkconnect.util.WSConstants;
import com.cf.tkconnect.util.WSUtil;
import com.cf.tkconnect.util.xml.ParseAndSplit;
import com.cf.tkconnect.util.xml.ParseXML;


public class ProcessExternal {

	static Log logger = LogSource.getInstance(ProcessExternal.class);
	
	
	private String serviceName = null;

	private String today = "";

	private static  String inprogress = "no";
	private static  int 	processcount = 0;
	private static  int 	processcompletecount = 0;
	private static  boolean processSet = false;
	private static  boolean processComplete = false;
	public static boolean nspresent = false;
	public static String nsvalue="";

	public ProcessExternal(){
		
	}
	private void resetVariables(){
		inprogress = "no";
		processcount = 0;
		processcompletecount = 0;
		processSet = false;
		processComplete = false;
	}
	
	public boolean isAvailable(){
		if(inprogress.equals("no")) return true;
		return false;
	}
	
	public List<String> processFileContents( String documentPath, List<String> failureList, List<String> sucessList, List responselist) throws Exception {
		List<String> errList = new ArrayList<String>();
		logger.debug("processFileContents start with checking failureList size::"+failureList.size());
		synchronized(this){
			if(inprogress.equals("yes")){
				errList.add("cannot start another process for file:"+documentPath);
				return errList;
			}else inprogress = "yes";
		}
		logger.debug("start with processing:"+documentPath);
		WebLinkLogLoader.JobErrorLogger.error("checking purpose");
		
		boolean error = false;
		String fileName = null;
		try {
			File file  = new File(documentPath);
			if(!file.exists()){
				logger.error("Cannot process request due to following invalid filename:"+documentPath);
				errList.add("Cannot process request due to following invalid filename:"+documentPath);
				return errList;
			}
			today = getDateString();
			logger.debug("start with creating dirs ");
			createDateDirs(today);
			// first parse the big file

			fileName = documentPath.substring(documentPath.lastIndexOf(File.separator) + 1);
			// move the file to temp directory
			logger.debug("start with processing file:"+fileName);
			// check the basic criteria for the input read from the file.
			List<String> inputList = new ArrayList<String>();
			ParseAndSplit spiltparser = new ParseAndSplit();
			StringBuffer  inputXmlBuffer = new StringBuffer();
			spiltparser.ProcessXML( documentPath, inputList );
			logger.debug("after split and parse with processing file:"+fileName);
			// make a copy and keep. but dont delete original file
			logger.debug("make a copy of original file in success directory:"+fileName+" inputList::"+inputList);
			FileUtils.copyFiles(FileUtils.getFilePath(documentPath),
					FileUtils.SuccessFileServiceBaseDirectory+ File.separator+ today, fileName, fileName,
					false);

			ParseXML parser = new ParseXML();
			// this method returns parsed data in the inputMap which can be
			// used later on and the BPXML(list_wrapper) information is returned
			// as string in the inputXmlBuffer object.
			processcount = 0;
			logger.debug("after split and parse with processing size:"+inputList.size());
			for(String XmlDocumentContents : inputList){
				Map<String,Object> inputMap = new HashMap<String,Object>();
				//logger.debug("after split and parse with processing XmlDocumentContents:"+XmlDocumentContents);
				List<String> temperrList = parser.processXMLContents(null, XmlDocumentContents, inputMap, inputXmlBuffer) ;
				inputMap.put(SHORTNAME, InitialSetUp.company.get("shortname"));
				/*
				 * after the file is processed, move the file to output directory to
				 * prevent duplicate processing.
				 */
	
				if (temperrList != null && temperrList.size() > 0) {
					logger.error("Cannot process request due to following invalid input data -- ");
					WebLinkLogLoader.JobErrorLogger.error("Processing of request failed due to following invalid input data -- ");
					for (int i = 0; i < temperrList.size(); i++) {
						logger.error(temperrList.get(i));
						WebLinkLogLoader.JobErrorLogger.error(temperrList.get(i));
					}
					errList.addAll(temperrList);
					error = true;
				}
				// Set keySet = inputMap.keySet();
	
				if (!checkInputValue(SHORTNAME, inputMap)) {
					String shortName = (String) PropertyManager.getProperty(COMPANY_SHORTNAME);
	
					if (shortName == null || shortName.trim().length() == 0) {
						logger.error("Cannot process request as Company Short Name is not available.");
						WebLinkLogLoader.JobErrorLogger
								.error("Cannot process request as Company Short Name is not available");
						errList.add("Cannot process request as Company Short Name is not available");
						error = true;
					} else 
						inputMap.put(SHORTNAME, shortName);
				}
				if (!checkInputValue(SERVICENAME, inputMap)) {
					serviceName = (String) PropertyManager.getProperty(PROPERTY_SERVICE_NAME);
					if (serviceName == null || serviceName.trim().length() == 0) {
						logger.error("Cannot process request as Service Name:"
								+ SERVICENAME + " is not available.");
						WebLinkLogLoader.JobErrorLogger
								.error("Cannot process request as Service Name:"
										+ SERVICENAME + " is not available");
						error = true;
						errList.add("Cannot process request as Service Name:"
								+ SERVICENAME + " is not available");
					} else 
						inputMap.put(SERVICENAME, serviceName);
				}
				if(error){
					failureList.add(XmlDocumentContents);
					logger.info("NOT sending for processing..... failureList size::"+failureList.size());
				}else{
					synchronized(inprogress){
						processcount++;
					}
					RequestExtProcessor rp = new RequestExtProcessor(inputMap, documentPath, XmlDocumentContents, failureList , sucessList, responselist );
					logger.info("sending for processing..... ");
					ProcessFileService.serve(rp);
				}
			}// for
			synchronized(inprogress){
				processSet = true;
			}
			
			
		} catch (SAXParseException e) {
			logger.error("SAXParseException - " + e.getMessage());
			errList.add(e.getMessage());
			error = true;
		} catch (SAXException e) {
			logger.error("SAXException - " + e.getMessage());
			errList.add(e.getMessage());
			error = true;
		}
		
        String inputerror = PropertyManager.getProperty(SEND_EMAIL_ONERROR,"yes"); 
		if(error){ 
			if("yes".equals(inputerror)){
				StringBuilder buf = new StringBuilder();
				for (int i = 0; i < errList.size(); i++) 
					buf.append(errList.get(i));
				MailUtils.sendMailMessage("smartlink could not proceed for file name:"+fileName, "Input XML file "+fileName+" could not be successfully parsed reasons:"+buf.toString(),"smartlink Status",fileName,500);
			}
		}else{
			 logger.debug("Thread is checking for completion::"+processComplete+" inprogress:"+inprogress);		
			// now keep waiting untill complete
			boolean check = true;
			while(check){
				synchronized(inprogress){
					if(processComplete  ){
						inprogress = "no";
						check = false;
						break;
					}
				}
				try{
					Thread.sleep(5000);
				}catch(InterruptedException ie){
					   logger.debug("Thread is awaiting completion");	
				}
			}// its done now reset
			resetVariables();
			logger.debug("Thread has finished work.");	
		}
		return errList;
	}

	public List<String> processContents( String XmlDocumentContents, List<String> failureList, List<String> sucessList, List responselist) throws Exception {
		List<String> errList = new ArrayList<String>();
		logger.debug("processFileContents start with checking failureList size::"+failureList.size());
		
		boolean error = false;
		String fileName = null;
		try {

			ParseXML parser = new ParseXML();
			// this method returns parsed data in the inputMap which can be
			// used later on and the BPXML(list_wrapper) information is returned
			// as string in the inputXmlBuffer object.
			StringBuffer  inputXmlBuffer = new StringBuffer();
			processcount = 0;
				Map<String,Object> inputMap = new HashMap<String,Object>();
				//logger.debug("after split and parse with processing XmlDocumentContents:"+XmlDocumentContents);
				List<String> temperrList = parser.processXMLContents(null, XmlDocumentContents, inputMap, inputXmlBuffer) ;
				/*
				 * after the file is processed, move the file to output directory to
				 * prevent duplicate processing.
				 */
	
				if (temperrList != null && temperrList.size() > 0) {
					logger.error("Cannot process request due to following invalid input data -- ");
					WebLinkLogLoader.JobErrorLogger.error("Processing of request failed due to following invalid input data -- ");
					for (int i = 0; i < temperrList.size(); i++) {
						logger.error(temperrList.get(i));
						WebLinkLogLoader.JobErrorLogger.error(temperrList.get(i));
					}
					errList.addAll(temperrList);
					error = true;
				}
				// Set keySet = inputMap.keySet();
	
				if (!checkInputValue(SHORTNAME, inputMap)) {
					String shortName = (String) PropertyManager.getProperty(COMPANY_SHORTNAME);
	
					if (shortName == null || shortName.trim().length() == 0) {
						logger.error("Cannot process request as Company Short Name is not available.");
						WebLinkLogLoader.JobErrorLogger
								.error("Cannot process request as Company Short Name is not available");
						errList.add("Cannot process request as Company Short Name is not available");
						error = true;
					} else 
						inputMap.put(SHORTNAME, shortName);
				}
				if (!checkInputValue(SERVICENAME, inputMap)) {
					serviceName = (String) PropertyManager.getProperty(PROPERTY_SERVICE_NAME);
					if (serviceName == null || serviceName.trim().length() == 0) {
						logger.error("Cannot process request as Service Name:"
								+ SERVICENAME + " is not available.");
						WebLinkLogLoader.JobErrorLogger
								.error("Cannot process request as Service Name:"
										+ SERVICENAME + " is not available");
						error = true;
						errList.add("Cannot process request as Service Name:"
								+ SERVICENAME + " is not available");
					} else 
						inputMap.put(SERVICENAME, serviceName);
				}
				if(error){
					failureList.add(XmlDocumentContents);
					logger.info("NOT sending for processing..... failureList size::"+failureList.size());
				}else{
					RequestExtProcessor rp = new RequestExtProcessor(inputMap, "", XmlDocumentContents, failureList , sucessList, responselist );
					logger.info("sending for processing..... ");
					ProcessFileService.serve(rp);
				}
				
			
			
		} catch (SAXParseException e) {
			logger.error("SAXParseException - " + e.getMessage());
			errList.add(e.getMessage());
			error = true;
		} catch (SAXException e) {
			logger.error("SAXException - " + e.getMessage());
			errList.add(e.getMessage());
			error = true;
		}
		
        String inputerror = PropertyManager.getProperty(SEND_EMAIL_ONERROR,"yes"); 
		if(error){ 
			if("yes".equals(inputerror)){
				StringBuilder buf = new StringBuilder();
				for (int i = 0; i < errList.size(); i++) 
					buf.append(errList.get(i));
				MailUtils.sendMailMessage("smartlink could not proceed for file name:"+fileName, "Input XML file "+fileName+" could not be successfully parsed reasons:"+buf.toString(),"smartlink Status",fileName,500);
			}
		}else{
			 logger.debug("Thread is checking for completion::"+processComplete+" inprogress:"+inprogress);		
			// now keep waiting until complete
			logger.debug("Thread has finished work.");	
		}
		return errList;
	}


	class RequestExtProcessor implements Runnable {
		

		private Log logger = LogSource.getInstance(RequestExtProcessor.class);
		private ResponseObject responseObj = null;
		Map inputMap = null;
		String documentPath = null;
		String fName_noext = null;
		String fileName_withExt = null;
		private	boolean incallback = false;
		private  List<String> failureList;
		private List<String> sucessList;
		private List<String> responseList;
		private String inputrecdxml;
		private boolean getObjectData = false;

		public RequestExtProcessor(Map inputMap, String documentPath, String inputxml, List<String> failureList,
				List<String> sucessList , List<String> responseList) {
			this.inputMap = inputMap;
			this.documentPath = documentPath;
			int ind = documentPath.lastIndexOf(File.separator);
			int extInd = documentPath.lastIndexOf(".");
			fName_noext = documentPath.substring(ind + 1, extInd);
			fileName_withExt = this.documentPath.substring(ind + 1);
			this.failureList = failureList;
			this.sucessList = sucessList;
			this.responseList = responseList;
			this.inputrecdxml = inputxml;
		}

		public RequestExtProcessor(Map inputMap, String documentPath, String inputxml, String failure,
				String sucess , String response) {
			this.inputMap = inputMap;
			this.documentPath = documentPath;
			int ind = documentPath.lastIndexOf(File.separator);
			int extInd = documentPath.lastIndexOf(".");
			fName_noext = documentPath.substring(ind + 1, extInd);
			fileName_withExt = this.documentPath.substring(ind + 1);
			this.failureList = failureList;
			this.sucessList = sucessList;
			this.responseList = responseList;
			this.inputrecdxml = inputxml;
			this.getObjectData = true;
		}
		public void run() {
			try {
				logger.info("Calling smartlinkConnector to send request to the server");
				sendRequest();
				if(getObjectData){// get objectlist
					processObjectResponse( responseObj,inputrecdxml,failureList, sucessList , inputMap );
					
				}else{ // standard cases for all webservices
				
					processExtResponse( responseObj,inputrecdxml,failureList, sucessList , responseList );
					checkCompletion();
				}
			} catch (Exception e) {
				logger.error(e, e);
			}

		}

		private void checkCompletion(){
			synchronized(inprogress){
				processcompletecount++;
				if(processSet && processcompletecount == processcount)
					processComplete = true;
				logger.info("Calling the processSet:"+processSet+" processcompletecount:"+processcompletecount+" processcount:"+processcount);
			}
		}
		public void sendRequest() throws Exception {
			String serverUrl = InitialSetUp.company.get("url");
			String shortName = this.inputMap.get(SHORTNAME) != null ? ((String) this.inputMap
					.get(SHORTNAME)).trim()	: null;
			String authCode = InitialSetUp.company.get("authcode");
			if(this.inputMap.containsKey(AUTHENTICATIONKEY)) authCode = (String)this.inputMap.get(AUTHENTICATIONKEY);
			logger.debug("in sendrequest authCode::"+authCode);
			String serviceName = this.inputMap.get(SERVICENAME) != null ? ((String) this.inputMap
					.get(SERVICENAME)).trim()
					: null;
			if (serverUrl == null || serverUrl.length() == 0) {
				logger.error("No Server URL is specified");
				return;
			}
			String projNumber = this.inputMap.get(PROJECTNUMBER) != null ? ((String) this.inputMap
					.get(PROJECTNUMBER)).trim()
					: null;
			String cloneProjNumber = this.inputMap.get(CLONEPROJECTNUMBER) != null ? ((String) this.inputMap
					.get(CLONEPROJECTNUMBER)).trim()
					: null;

			logger.debug("*************** REQUEST INFO BEGIN ************");
			logger.debug("SERVERURL: " + serverUrl);
			logger.debug("SHORTNAME: " + shortName);
			logger.debug("PROJECT NUMBER: " + projNumber);
			logger.debug("SERVICE NAME: " + serviceName);
			logger.debug("INPUTXML: " + inputMap.get(INPUTXML));
			logger.debug("*************** REQUEST INFO END ************");
			
			String inputxml = inputMap.get(INPUTXML) != null ? inputMap.get(
					INPUTXML).toString() : null;
			int lineitemcount = 0;		
			if(inputxml != null){
				inputxml = "<?xml version='1.0' encoding='ISO-8859-1' ?>\n"+inputxml;
				lineitemcount = StringUtils.countMatches(inputxml, "<_bp_lineitem");
			}
			TKConnector uc = new TKConnector(serverUrl);

			if (serviceName.equalsIgnoreCase(CREATE_OBJECT)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				String objName = this.inputMap.get(OBJECTNAME) != null ?
				         (String)this.inputMap.get(OBJECTNAME) : null;
					responseObj = uc.createObject(shortName, authCode, objName,
						inputxml);
			} else if (serviceName.equalsIgnoreCase(UPDATE_OBJECT)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				String objName = this.inputMap.get(OBJECTNAME) != null ?
						         (String)this.inputMap.get(OBJECTNAME) : null;
				responseObj = uc.updateObject(shortName, authCode, objName,
						inputxml);
			} else if (serviceName.equalsIgnoreCase(CREATE_USER)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				String copyFromUserPreferenceTemplate = this.inputMap.get(COPY_FROM_USER_PREFERENCE_TEMPLATE) != null ? (String) this.inputMap.get(COPY_FROM_USER_PREFERENCE_TEMPLATE) : null;
				responseObj = uc.createUser(shortName, authCode, copyFromUserPreferenceTemplate, inputxml);
			} else if (serviceName.equalsIgnoreCase(UPDATE_USER)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				responseObj = uc.updateUser(shortName, authCode, inputxml);
			} else if (serviceName.equalsIgnoreCase(UPDATE_USERGROUPMEMBERSHIP)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				responseObj = uc.updateUserGroupMembership(shortName, authCode, inputxml);
			} else if (serviceName.equalsIgnoreCase(UPDATE_USER_SHELL_MEMBERSHIP)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				responseObj = uc.updateUserShellMembership(shortName, authCode, projNumber, inputxml);
			} else if (serviceName.equalsIgnoreCase(GET_USER_LIST)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				responseObj = uc.getUserList(shortName, authCode, getFilterCondition());
			} else if (serviceName.equalsIgnoreCase(CREATE_BPRECORD)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				logger.debug("Number of lineitems are :"+lineitemcount);
				if(!nocallback &&  !incallback && lineitemcount > MAX_LINEITEMS){
					// must set this to callback servrice only if nocallback has not been set
					incallback = true;
					logger.info("Number of lineitems are quite high, to avoid timeout this has been set for callback service.");
					logger.info("Check after "+(callback_poll_server/1000)+" seconds.");
				}

				String bpName = this.inputMap.get(BPNAME) != null ?
						        (String) this.inputMap.get(BPNAME) : null;
				responseObj = uc.createBPRecord(shortName, authCode,
						projNumber, bpName, inputxml);
			} else if (serviceName.equalsIgnoreCase(UPDATE_BPRECORD)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				String bpName = this.inputMap.get(BPNAME) != null ?
				        (String) this.inputMap.get(BPNAME) : null;
				responseObj = uc.updateBPRecord(shortName, authCode,
						projNumber, bpName, inputxml);
			} else if (serviceName.equalsIgnoreCase(UPDATE_BPRECORDV2)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				String bpName = this.inputMap.get(BPNAME) != null ?
				        (String) this.inputMap.get(BPNAME) : null;
				String options = this.inputMap.get(UPDATE_BPRECORDV2_OPTIONS) != null ?
										        (String) this.inputMap.get(UPDATE_BPRECORDV2_OPTIONS) : null;
				responseObj = uc.updateBPRecordV2(shortName, authCode,
						projNumber, bpName, inputxml, options);
			} else if (serviceName.equalsIgnoreCase(ADD_BPLINEITEM)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				if(!nocallback &&  !incallback && lineitemcount > MAX_LINEITEMS){
					// must set this to callbackservicestr servrice only if nocallback has not been set
					incallback = true;
					logger.info("Number of lineitems are quite high, to avoid timeout this has been set for callback service.");
					logger.info("Check after "+(callback_poll_server/1000)+" seconds.");
				}
				String bpName = this.inputMap.get(BPNAME) != null ?
				        (String) this.inputMap.get(BPNAME) : null;
				responseObj = uc.addBPLineItem(shortName, authCode, projNumber,
						bpName, inputxml);
			} else if (serviceName.equalsIgnoreCase(CREATE_PROJECT)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				//logger.error("clone project number: "+ cloneProjNumber);
				incallback = isCallbackService;
				responseObj = uc.createProject(shortName, authCode, cloneProjNumber,
						inputxml);
			} else if (serviceName.equalsIgnoreCase(CREATE_WBS)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				responseObj = uc.createWBS(shortName, authCode, projNumber,
						inputxml);
			} else if (serviceName.equalsIgnoreCase(GET_BPLIST)) {
				String bpName = this.inputMap.get(BPNAME) != null ?
				        (String) this.inputMap.get(BPNAME) : null;
				responseObj = uc.getBPList(shortName, authCode, projNumber,
						bpName, getFieldNames(), getFilterCondition(),
						getFilterValues());
				
				
			} else if (serviceName.equalsIgnoreCase(GET_BPRECORD)) {
				String bpName = this.inputMap.get(BPNAME) != null ?
				        (String) this.inputMap.get(BPNAME) : null;
				String recordno = (String) this.inputMap.get(RECORDNUMBER);
				responseObj = uc.getBPRecord(shortName, authCode, projNumber,
						bpName, recordno);
				// now here we need to see if record no is null
			} else if (serviceName.equalsIgnoreCase(GET_PLANNINGITEM)) {
				String bpName = this.inputMap.get(BPNAME) != null ?
				        (String) this.inputMap.get(BPNAME) : null;
				String recordno = (String) this.inputMap.get(RECORDNUMBER);
				String planningitem = (String) this.inputMap.get(PLANNINGITEM);
				logger.info(this.inputMap);
				logger.info(this.inputMap.get("_planningitem"));
				logger.info(this.inputMap.keySet().toString() +" -- PLANNINGITEM "+ PLANNINGITEM);
				responseObj = uc.getPlanningItem(shortName, authCode, projNumber,
						bpName, recordno, planningitem);
			} else if (serviceName.equalsIgnoreCase(GET_WBSSTRUCTURE)) {
				responseObj = uc.getWBSStructure(shortName, authCode);
			} else if (serviceName.equalsIgnoreCase(GET_COMPLETE_BPRECORD)) {
				String bpName = this.inputMap.get(BPNAME) != null ?
				        (String) this.inputMap.get(BPNAME) : null;
				String recordno = (String) this.inputMap.get(RECORDNUMBER);
				responseObj = uc.getCompleteBPRecord(shortName, authCode,
						projNumber, bpName, recordno);
			}else if (serviceName.equalsIgnoreCase("getSOV")) {
				String bpName = this.inputMap.get(BPNAME) != null ?
				        (String) this.inputMap.get(BPNAME) : null;
				String recordno = (String) this.inputMap.get(RECORDNUMBER);
				responseObj = uc.getSOV(shortName, authCode,
						projNumber, bpName, recordno);

			}else if (serviceName.equalsIgnoreCase(GET_OBJECT_LIST)) {
				String objName = this.inputMap.get(OBJECTNAME) != null ?
				         (String)this.inputMap.get(OBJECTNAME) : null;
				responseObj = uc.getObjectList(shortName, authCode, objName,
						getFieldNames(), getFilterCondition(),	getFilterValues());
				if(responseObj.getStatusCode() == WSConstants.OK_CODE){
					// now we need to process the 
					
				}
				// now we can process the file & update the data base
				
			} else if (serviceName.equalsIgnoreCase(GET_COLUMN_DATA)) {
				String colName = this.inputMap.get(COLUMNNAME) != null ?
				         (String)this.inputMap.get(COLUMNNAME) : null;
				responseObj = uc.getColumnData(shortName, authCode, projNumber, colName);

			} else if (serviceName.equalsIgnoreCase(UPDATE_COLUMN_DATA)) {
				String colName = this.inputMap.get(COLUMNNAME) != null ?
				         (String)this.inputMap.get(COLUMNNAME) : null;
				responseObj = uc.updateColumnData(shortName, authCode, projNumber, colName, inputxml);
				
			} else if (serviceName.equalsIgnoreCase(GET_UDR_DATA)) {
				String repName = this.inputMap.get(REPORTNAME) != null ?
				         (String)this.inputMap.get(REPORTNAME) : null;
				responseObj = uc.getUDRData(shortName, authCode, projNumber, repName);
			} else if (serviceName.equalsIgnoreCase(CREATE_ASSET)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				logger.debug("Number of lineitems are :"+lineitemcount);
				String assetclassname = this.inputMap.get(ASSETCLASSNAME) != null ?
						        (String) this.inputMap.get(ASSETCLASSNAME) : null;
				String copyfromasset = this.inputMap.get(COPYFROMASSET) != null ?
								        (String) this.inputMap.get(COPYFROMASSET) : null;
				responseObj = uc.createAsset(shortName, authCode,
						assetclassname, copyfromasset, inputxml);
				logger.info("Asset assetclassname:"+assetclassname+"  copyfromasset:"+copyfromasset);
				
			} else if (serviceName.equalsIgnoreCase(CREATE_ACTIVITIES)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				logger.debug("Number of lineitems are :"+lineitemcount);
				String sheetname = this.inputMap.get(SHEETNAME) != null ?
								        (String) this.inputMap.get(SHEETNAME) : null;
				responseObj = uc.createScheduleActivities(shortName, authCode,
						projNumber, sheetname, inputxml);
				logger.info("createScheduleActivities:  shhetname:"+sheetname);
			} else if (serviceName.equalsIgnoreCase(CREATE_ACTIVITIESV2)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				logger.debug("Number of lineitems are :"+lineitemcount);
				String sheetname = this.inputMap.get(SHEETNAME) != null ?
								        (String) this.inputMap.get(SHEETNAME) : null;
				String options = this.inputMap.get(SCHEDULEOPTIONS) != null ?
										        (String) this.inputMap.get(SCHEDULEOPTIONS) : null;
				responseObj = uc.createScheduleActivitiesV2(shortName, authCode,
						projNumber, sheetname, inputxml , options);
				logger.info("createScheduleActivities:  shhetname:"+sheetname);
			
			} else if (serviceName.equalsIgnoreCase(UPDATE_ACTIVITIES)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				logger.debug("Number of lineitems are :"+lineitemcount);
				String sheetname = this.inputMap.get(SHEETNAME) != null ?
								        (String) this.inputMap.get(SHEETNAME) : null;
				responseObj = uc.updateScheduleActivities(shortName, authCode,
						projNumber, sheetname, inputxml);
				logger.info("updateScheduleActivities:  sheetname:"+sheetname);
			
			} else if (serviceName.equalsIgnoreCase(UPDATE_ACTIVITIESV2)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				logger.debug("Number of lineitems are :"+lineitemcount);
				String sheetname = this.inputMap.get(SHEETNAME) != null ?
								        (String) this.inputMap.get(SHEETNAME) : null;
				String options = this.inputMap.get(SCHEDULEOPTIONS) != null ?
										        (String) this.inputMap.get(SCHEDULEOPTIONS) : null;
				responseObj = uc.updateScheduleActivitiesV2(shortName, authCode,
						projNumber, sheetname, inputxml , options);
				logger.info("updateScheduleActivitiesV2:  sheetname:"+sheetname);
			
			} else if (serviceName.equalsIgnoreCase(GET_ACTIVITIES)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				logger.debug("Number of lineitems are :"+lineitemcount);
				String sheetname = this.inputMap.get(SHEETNAME) != null ?
								        (String) this.inputMap.get(SHEETNAME) : null;
				responseObj = uc.getScheduleActivities(shortName, authCode,
						projNumber, sheetname);
				logger.info("getScheduleActivities:  sheetname:"+sheetname);

			} else if (serviceName.equalsIgnoreCase(GET_EXCHANGE_RATES)) {
				responseObj = uc.getExchangeRates(shortName, authCode);
			} else if (serviceName.equalsIgnoreCase(UPDATE_EXCHANGE_RATES)) {
				if (inputxml == null) {
					logInputXmlError();
					return;
				}
				responseObj = uc.updateExchangeRates(shortName, authCode, inputxml);
			} else if (serviceName.equalsIgnoreCase(GET_RESOURCE_LIST)) {
				logger.debug("getResourceList in ProcessExternal  ");
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				responseObj = uc.getResourceList(shortName, authCode, getFilterCondition());
			} else {
				WebLinkLogLoader.JobErrorLogger
						.error("Unsupported/invalid service request: "
								+ serviceName);
				logger.error("Unsupported/invalid service request: "
						+ serviceName);
				failureList.add(inputrecdxml);
				return;
			}

		}

		private void logInputXmlError() throws Exception {
			WebLinkLogLoader.JobErrorLogger
			.error("Input XML is Null or Invalid");
			logger.error("Input XML is Null or Invalid");
		}
		
		
		private String[] getFilterValues() {
			if (this.inputMap.get(FILTERVALUELIST) == null)
				return null;
			ArrayList filterVallist = (ArrayList) this.inputMap
					.get(FILTERVALUELIST);
			String[] fvalues = null;
			if (filterVallist != null && filterVallist.size() > 0) {
				fvalues = new String[filterVallist.size()];
				for (int i = 0; i < filterVallist.size(); i++) {
					fvalues[i] = filterVallist.get(i).toString();
				}
			}
			if (fvalues != null) {
				for (int j = 0; j < fvalues.length; j++) {
					logger.debug("filter value(" + (j + 1) + ") - "
							+ fvalues[j]);
				}
			}
			return fvalues;
		}

		private String getFilterCondition() {
			String fcondition = null;
			if (this.inputMap.get(FILTERCONDITION) == null)
				return null;
			else {
				fcondition = (String) this.inputMap.get(FILTERCONDITION);
				logger.debug("filtercondition: " + fcondition);
				return fcondition.trim();
			}
		}

		private String[] getFieldNames() {
			if (this.inputMap.get(FIELDNAMELIST) == null)
				return null;
			ArrayList fnamelist = (ArrayList) this.inputMap.get(FIELDNAMELIST);
			String[] fnames = null;
			if (fnamelist != null && fnamelist.size() > 0) {
				fnames = new String[fnamelist.size()];
				for (int i = 0; i < fnamelist.size(); i++) {
					fnames[i] = fnamelist.get(i).toString();
				}
			}
			if (fnames != null) {
				for (int j = 0; j < fnames.length; j++) {
					logger.debug("field name(" + (j + 1) + ") - " + fnames[j]);
				}
			}
			return fnames;
		}



	}// end class RequestExtProcessor

}
