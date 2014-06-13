package com.cf.tkconnect.process;

import static com.cf.tkconnect.util.FileUtils.createDateDirs;
import static com.cf.tkconnect.util.InitialSetUp.callback_poll_server;
import static com.cf.tkconnect.util.InitialSetUp.isCallbackService;
import static com.cf.tkconnect.util.InitialSetUp.nocallback;
import static com.cf.tkconnect.util.WSConstants.ADD_BPLINEITEM;
import static com.cf.tkconnect.util.WSConstants.ADD_COMPLETE_BPLINEITEM;
import static com.cf.tkconnect.util.WSConstants.ASSETCLASSNAME;
import static com.cf.tkconnect.util.WSConstants.BPNAME;
import static com.cf.tkconnect.util.WSConstants.CLASSNAME;
import static com.cf.tkconnect.util.WSConstants.CLONEPROJECTNUMBER;
import static com.cf.tkconnect.util.WSConstants.CMCODE;
import static com.cf.tkconnect.util.WSConstants.COLUMNNAME;
import static com.cf.tkconnect.util.WSConstants.COMPANY_SHORTNAME;
import static com.cf.tkconnect.util.WSConstants.COPYFROMASSET;
import static com.cf.tkconnect.util.WSConstants.COPYFROMRECORD;
import static com.cf.tkconnect.util.WSConstants.COPYFROMSHELLTEMPLATE;
import static com.cf.tkconnect.util.WSConstants.COPY_FROM_USER_PREFERENCE_TEMPLATE;
import static com.cf.tkconnect.util.WSConstants.CREATE_ACTIVITIES;
import static com.cf.tkconnect.util.WSConstants.CREATE_ACTIVITIESV2;
import static com.cf.tkconnect.util.WSConstants.CREATE_ASSET;
import static com.cf.tkconnect.util.WSConstants.CREATE_BPRECORD;
import static com.cf.tkconnect.util.WSConstants.CREATE_COMPLETE_BPRECORD;
import static com.cf.tkconnect.util.WSConstants.CREATE_CONFIG_MODULE;
import static com.cf.tkconnect.util.WSConstants.CREATE_FILE_ACTIVITIESV2;
import static com.cf.tkconnect.util.WSConstants.CREATE_LEVEL;
import static com.cf.tkconnect.util.WSConstants.CREATE_OBJECT;
import static com.cf.tkconnect.util.WSConstants.CREATE_PROJECT;
import static com.cf.tkconnect.util.WSConstants.CREATE_SHELL;
import static com.cf.tkconnect.util.WSConstants.CREATE_SPACE;
import static com.cf.tkconnect.util.WSConstants.CREATE_UPDATE_RESOURCE;
import static com.cf.tkconnect.util.WSConstants.CREATE_UPDATE_ROLE;
import static com.cf.tkconnect.util.WSConstants.CREATE_USER;
import static com.cf.tkconnect.util.WSConstants.CREATE_WBS;
import static com.cf.tkconnect.util.WSConstants.FIELDNAMES;
import static com.cf.tkconnect.util.WSConstants.FILELIST;
import static com.cf.tkconnect.util.WSConstants.FILTEROPTIONS;
import static com.cf.tkconnect.util.WSConstants.GET_ACTIVITIES;
import static com.cf.tkconnect.util.WSConstants.GET_BPLIST;
import static com.cf.tkconnect.util.WSConstants.GET_BPRECORD;
import static com.cf.tkconnect.util.WSConstants.GET_COLUMN_DATA;
import static com.cf.tkconnect.util.WSConstants.GET_COMPLETE_BPRECORD;
import static com.cf.tkconnect.util.WSConstants.GET_EXCHANGE_RATES;
import static com.cf.tkconnect.util.WSConstants.GET_LEVEL_LIST;
import static com.cf.tkconnect.util.WSConstants.GET_OBJECT_LIST;
import static com.cf.tkconnect.util.WSConstants.GET_PLANNINGITEM;
import static com.cf.tkconnect.util.WSConstants.GET_RESOURCE_LIST;
import static com.cf.tkconnect.util.WSConstants.GET_ROLE_LIST;
import static com.cf.tkconnect.util.WSConstants.GET_SHELL_LIST;
import static com.cf.tkconnect.util.WSConstants.GET_SOV;
import static com.cf.tkconnect.util.WSConstants.GET_SPACE_LIST;
import static com.cf.tkconnect.util.WSConstants.GET_UDR_DATA;
import static com.cf.tkconnect.util.WSConstants.GET_USER_LIST;
import static com.cf.tkconnect.util.WSConstants.GET_WBSSTRUCTURE;
import static com.cf.tkconnect.util.WSConstants.FILTERVALUELIST;
import static com.cf.tkconnect.util.WSConstants.FILTERCONDITION;
import static com.cf.tkconnect.util.WSConstants.FIELDNAMELIST;
import static com.cf.tkconnect.util.WSConstants.INPUTXML;
import static com.cf.tkconnect.util.WSConstants.ISZIPFILE;
import static com.cf.tkconnect.util.WSConstants.LINE_ITEM_TAG;
import static com.cf.tkconnect.util.WSConstants.MAX_LINEITEMS;
import static com.cf.tkconnect.util.WSConstants.OBJECTNAME;
import static com.cf.tkconnect.util.WSConstants.PLANNINGITEM;
import static com.cf.tkconnect.util.WSConstants.PROJECTNUMBER;
import static com.cf.tkconnect.util.WSConstants.PROPERTY_SERVICE_NAME;
import static com.cf.tkconnect.util.WSConstants.RECORDNUMBER;
import static com.cf.tkconnect.util.WSConstants.REPORTNAME;
import static com.cf.tkconnect.util.WSConstants.SCHEDULEOPTIONS;
import static com.cf.tkconnect.util.WSConstants.SEND_EMAIL_ONERROR;
import static com.cf.tkconnect.util.WSConstants.SERVICENAME;
import static com.cf.tkconnect.util.WSConstants.SHEETNAME;
import static com.cf.tkconnect.util.WSConstants.SHELLTYPE;
import static com.cf.tkconnect.util.WSConstants.SHORTNAME;
import static com.cf.tkconnect.util.WSConstants.SPACETYPE;
import static com.cf.tkconnect.util.WSConstants.UPDATE_ACTIVITIES;
import static com.cf.tkconnect.util.WSConstants.UPDATE_ACTIVITIESV2;
import static com.cf.tkconnect.util.WSConstants.UPDATE_BPRECORD;
import static com.cf.tkconnect.util.WSConstants.UPDATE_BPRECORDV2;
import static com.cf.tkconnect.util.WSConstants.UPDATE_BPRECORDV2_OPTIONS;
import static com.cf.tkconnect.util.WSConstants.UPDATE_COLUMN_DATA;
import static com.cf.tkconnect.util.WSConstants.UPDATE_COMPLETE_BPRECORD;
import static com.cf.tkconnect.util.WSConstants.UPDATE_CONFIG_MODULE;
import static com.cf.tkconnect.util.WSConstants.UPDATE_EXCHANGE_RATES;
import static com.cf.tkconnect.util.WSConstants.UPDATE_FILE_ACTIVITIESV2;
import static com.cf.tkconnect.util.WSConstants.UPDATE_LEVEL;
import static com.cf.tkconnect.util.WSConstants.UPDATE_OBJECT;
import static com.cf.tkconnect.util.WSConstants.UPDATE_SHELL;
import static com.cf.tkconnect.util.WSConstants.UPDATE_SPACE;
import static com.cf.tkconnect.util.WSConstants.UPDATE_USER;
import static com.cf.tkconnect.util.WSConstants.UPDATE_USERGROUPMEMBERSHIP;
import static com.cf.tkconnect.util.WSConstants.UPDATE_USER_SHELL_MEMBERSHIP;
import static com.cf.tkconnect.util.WSConstants.ZIPFILE;
import static com.cf.tkconnect.util.WSUtil.checkInputValue;
import static com.cf.tkconnect.util.WSUtil.getBPAttachments;
import static com.cf.tkconnect.util.WSUtil.getDateString;
import static com.cf.tkconnect.util.WSUtil.processResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.cf.tkconnect.connector.TKConnector;
import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.log.WebLinkLogLoader;
import com.cf.tkconnect.process.ProcessFile.RequestProcessor;
import com.cf.tkconnect.unifierservices.UnifierServices;
import com.cf.tkconnect.util.FileUtils;
import com.cf.tkconnect.util.InitialSetUp;
import com.cf.tkconnect.util.MailUtils;
import com.cf.tkconnect.util.PropertyManager;
import com.cf.tkconnect.util.WSUtil;
import com.cf.tkconnect.util.xml.FileData;
import com.cf.tkconnect.util.xml.FileObject;
import com.cf.tkconnect.util.xml.ParseXML;
import com.cf.tkconnect.wsdl.WSDLServices;

public class ProcessXMLFile {
	static Log logger = LogSource.getInstance(ProcessXMLFile.class);

	private Map<String,Object> inputMap = new HashMap<String,Object>();
	private StringBuffer inputXmlBuffer = new StringBuffer(200);
	private String documentPath = null;
	private String today = "";
	private String zip_file_name_path = "";

	public void processFile(String documentPath) throws Exception {
		boolean error = false;
		this.documentPath = documentPath;
		List<String> errList = new ArrayList<String>();
		String fileName = null;
		try {
			today = getDateString();
			createDateDirs(today);
			ParseXML parser = new ParseXML();
			// this method returns parsed data in the inputMap which can be
			// used later on and the BPXML(list_wrapper) information is returned
			// as string in the inputXmlBuffer object.
			errList = parser.validateSchemaAndProcessXML(null,
					documentPath, inputMap, inputXmlBuffer);
			/*
			 * after the file is processed, move the file to output directory to
			 * prevent duplicate processing.
			 */
			fileName = documentPath.substring(documentPath
					.lastIndexOf(File.separator) + 1);
			// move the file to temp directory
			FileUtils.copyFiles(FileUtils.InputFileServiceBaseDirectory,
					FileUtils.TempFileServiceBaseDirectory, fileName, fileName,
					true);

			// check the basic criteria for the input read from the file.

			if (errList != null && errList.size() > 0) {
				logger.error("Cannot process request due to following invalid input data -- ");
				WebLinkLogLoader.JobErrorLogger
						.error("Processing of request failed due to following invalid input data -- ");
				for (int i = 0; i < errList.size(); i++) {
					logger.error(errList.get(i));
					WebLinkLogLoader.JobErrorLogger.error(errList.get(i));
				}
				error = true;
			}
			
			if (error) {
				// do not proceed if there is an error.
				// move the request file to error directory
				WebLinkLogLoader.JobErrorLogger.error("*****Invalid input file: "
						+ fileName);
				FileUtils.moveFiles(FileUtils.TempFileServiceBaseDirectory,
						FileUtils.ErrorFileServiceBaseDirectory
								+ File.separator + today, fileName);

				return;
			}

			RequestProcessor rp = new RequestProcessor(inputMap, documentPath);
			logger.info("sending for processing ");
			ProcessFileService.serve(rp);

		} catch (SAXParseException e) {
			logger.error("SAXParseException - " + e.getMessage());
			error = true;
		} catch (SAXException e) {
			logger.error("SAXException - " + e.getMessage());
			error = true;
		}finally{
	        String inputerror = PropertyManager.getProperty(SEND_EMAIL_ONERROR,"no"); 
			if(error && "yes".equals(inputerror)){
				StringBuilder buf = new StringBuilder();
				for (int i = 0; i < errList.size(); i++) 
					buf.append(errList.get(i));
				
			//	MailUtils.sendMailMessage("smartlink could not proceed for file name:"+fileName, "Input XML file "+fileName+" could not be successfully parsed reasons:"+buf.toString(),"smartlink Status",fileName,500);
			}
		}
	}
	
	class RequestProcessor implements Runnable {

		private Log logger = LogSource.getInstance(RequestProcessor.class);
		private ResponseObject responseObj = null;
		Map<String,Object> inputMap = null;
		String documentPath = null;
		String fName_noext = null;
		String fileName_withExt = null;

		public RequestProcessor(Map<String,Object> inputMap, String documentPath) {
			this.inputMap = inputMap;
			this.documentPath = documentPath;
			int ind = documentPath.lastIndexOf(File.separator);
			int extInd = documentPath.lastIndexOf(".");
			fName_noext = documentPath.substring(ind + 1, extInd);
			fileName_withExt = this.documentPath.substring(ind + 1);
		}

		public void run() {
			try {
				logger.info("Calling smartlinkConnector to send request to the server");
				sendRequest();
				processResponse( responseObj,  documentPath,  today,  false);
			} catch (Exception e) {
				logger.error(e, e);
				// no action taken
			}

		}

		public void sendRequest() throws Exception {
			String shortname = InitialSetUp.company.get("shortname");
			String authcode = InitialSetUp.company.get("authcode");
			
			
			String methodName = this.inputMap.get(SERVICENAME) != null ? ((String) this.inputMap
					.get(SERVICENAME)).trim(): null;
			logger.debug("*************** REQUEST INFO BEGIN ************:::"+ this.inputMap);
			
			RunWebServices run = new RunWebServices(methodName, zip_file_name_path);
			
			Map<String,String>  map = convertInputMaptoMap(methodName);
			responseObj = run.runServiceFromFile(map);
		}
	}
	private String getListToString(List<String> list){
		if(list== null || list.size() ==0)
			return "";
		StringBuilder buf = new StringBuilder();
		for(String s : list)
			buf.append(s+" ");
		return buf.toString();
	}
	
	private Map<String,String>  convertInputMaptoMap(String methodName) throws Exception{
		List<Map<String,String>> params = WSDLServices.getMethods().get(methodName);
		
		Map<String,String> map = new HashMap<String,String>();
		for(Map<String,String> param : params){
			String param_name = param.get("param_name");
			if(param_name.equalsIgnoreCase( "shortname") || param_name.equalsIgnoreCase("authcode"))
				continue;// don't save these values
			//
			if(param_name.toLowerCase().indexOf("xml") > 0){
				String inputxml = (String)inputMap.get(INPUTXML) ;
				if(inputxml == null || inputxml.trim().length()== 0)
					throw new Exception ("There is no XML data");
				map.put(param_name, (String)inputMap.get(INPUTXML));//
			}else if(param_name.toLowerCase().indexOf("cloneprojectnumber") >=0)
				map.put(param_name, (String)inputMap.get(CLONEPROJECTNUMBER));
			else if(param_name.toLowerCase().indexOf("projectnumber") >=0)
				map.put(param_name, (String)inputMap.get(PROJECTNUMBER));
			else if(param_name.toLowerCase().indexOf("bpname") >=0)
				map.put(param_name, (String)inputMap.get(BPNAME));
			else if(param_name.toLowerCase().indexOf("recordnumber") >=0)
				map.put(param_name, (String)inputMap.get(RECORDNUMBER));
			else if(param_name.toLowerCase().indexOf("bpname") >=0)
				map.put(param_name, (String)inputMap.get(BPNAME));
			else if(param_name.toLowerCase().indexOf("sheetname") >=0)
				map.put(param_name, (String)inputMap.get(SHEETNAME));
			else if(param_name.toLowerCase().indexOf("assetclassname") >=0)
				map.put(param_name, (String)inputMap.get(ASSETCLASSNAME));
			else if(param_name.toLowerCase().indexOf("classname") >=0)
				map.put(param_name, (String)inputMap.get(CLASSNAME));
			else if(param_name.toLowerCase().indexOf("filterlist") >=0)
				map.put(param_name, getListToString((List) inputMap.get(FILTERVALUELIST)));
			else if(param_name.toLowerCase().indexOf("fieldnames") >=0)
				map.put(param_name, getListToString((List) inputMap.get(FIELDNAMELIST)));
			else if(param_name.toLowerCase().indexOf("filtercondition") >=0)
				map.put(param_name, (String) inputMap.get(FILTERCONDITION));
			else if(param_name.toLowerCase().indexOf("planningitem") >=0)
				map.put(param_name, (String)inputMap.get(PLANNINGITEM));
			else if(param_name.toLowerCase().indexOf("options") >=0){
				if(inputMap.containsKey(SCHEDULEOPTIONS) )
					map.put(param_name, (String)inputMap.get(SCHEDULEOPTIONS));
				else  if(inputMap.containsKey(FILTEROPTIONS) )
					map.put(param_name, (String)inputMap.get(FILTEROPTIONS));
				else  if(inputMap.containsKey(UPDATE_BPRECORDV2_OPTIONS) )
					map.put(param_name, (String)inputMap.get(UPDATE_BPRECORDV2_OPTIONS));

			}else if(param_name.toLowerCase().indexOf("spacetype") >=0)
				map.put(param_name, (String)inputMap.get(SPACETYPE));
			else if(param_name.toLowerCase().indexOf("shellType") >=0)
				map.put(param_name, (String)inputMap.get(SHELLTYPE));
			else if(param_name.toLowerCase().indexOf("copyfromuser") >=0)
				map.put(param_name, (String)inputMap.get(COPY_FROM_USER_PREFERENCE_TEMPLATE));
			else if(param_name.toLowerCase().indexOf("cmcode") >=0)
				map.put(param_name, (String)inputMap.get(CMCODE));
			else if(param_name.toLowerCase().indexOf("copyfromasset") >=0)
				map.put(param_name, (String)inputMap.get(COPYFROMASSET));
			else if(param_name.toLowerCase().indexOf("copyfromshelltemplate") >=0)
				map.put(param_name, (String)inputMap.get(COPYFROMSHELLTEMPLATE));
			else if(param_name.toLowerCase().indexOf("copyfromrecord") >=0)
				map.put(param_name, (String)inputMap.get(COPYFROMRECORD));
			else if(param_name.toLowerCase().indexOf("iszipfile") >=0)
				map.put(param_name, "yes");
			else if (param_name.toLowerCase().indexOf("files") >=0){
				String zipfile = (String)this.inputMap.get(ZIPFILE);
				zip_file_name_path = WSUtil.getFileAttachments( documentPath, FileUtils.InputFileServiceAttDirectory, 
						(List<String>)this.inputMap.get(FILELIST), today,  zipfile);
			}
		//,,,iszipfile,,,,files
		}//refBpName,sapPoNo
		return map;
	}
	

}
