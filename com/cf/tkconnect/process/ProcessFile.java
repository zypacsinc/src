/*
 * Created on Sep 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.cf.tkconnect.process;


import com.cf.tkconnect.connector.TKConnector;
import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.log.WebLinkLogLoader;
import com.cf.tkconnect.process.ProcessFile;
import com.cf.tkconnect.process.ProcessFileService;
import com.cf.tkconnect.process.ResponseObject;
import com.cf.tkconnect.util.FileUtils;
import com.cf.tkconnect.util.InitialSetUp;
import com.cf.tkconnect.util.MailUtils;
import com.cf.tkconnect.util.PropertyManager;
import com.cf.tkconnect.util.xml.FileObject;
import com.cf.tkconnect.util.xml.ParseXML;


import java.util.HashMap;
import java.util.List;
import java.util.ArrayList; 
import java.io.File;


import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.apache.commons.lang3.StringUtils;

import static com.cf.tkconnect.util.FileUtils.createDateDirs;
import static com.cf.tkconnect.util.InitialSetUp.attachzip;

import static com.cf.tkconnect.util.InitialSetUp.callback_poll_server;
import static com.cf.tkconnect.util.InitialSetUp.isCallbackService;
import static com.cf.tkconnect.util.InitialSetUp.nocallback;
import static com.cf.tkconnect.util.WSConstants.*;
import static com.cf.tkconnect.util.WSUtil.checkInputValue;
import static com.cf.tkconnect.util.WSUtil.getBPAttachments;
import static com.cf.tkconnect.util.WSUtil.getDateString;
import static com.cf.tkconnect.util.WSUtil.processResponse;

/**
 * @author Cyril Furtado
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ProcessFile  {
	/*
	 * This file collects from the parser and sends to the webservice
	 */
	static Log logger = LogSource.getInstance(ProcessFile.class);

	private HashMap<String,Object> inputMap = new HashMap<String,Object>();

	private StringBuffer inputXmlBuffer = new StringBuffer(200);

	private String serviceName = null;

	private String today = "";


	public void processFile(String documentPath) throws Exception {
		boolean error = false;
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
			if (!checkInputValue(SHORTNAME, inputMap)) {
				String shortName = (String) PropertyManager.getProperty(COMPANY_SHORTNAME);

				if (shortName == null || shortName.trim().length() == 0) {
					logger.error("Cannot process request as Company Short Name is not available.");
					WebLinkLogLoader.JobErrorLogger
							.error("Cannot process request as Company Short Name is not available");
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
				} else 
					inputMap.put(SERVICENAME, serviceName);
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
				
				MailUtils.sendMailMessage("smartlink could not proceed for file name:"+fileName, "Input XML file "+fileName+" could not be successfully parsed reasons:"+buf.toString(),"smartlink Status",fileName,500);
			}
		}
	}


	class RequestProcessor implements Runnable {

		private Log logger = LogSource.getInstance(RequestProcessor.class);
		private ResponseObject responseObj = null;
		HashMap inputMap = null;
		String documentPath = null;
		String fName_noext = null;
		String fileName_withExt = null;
		private	boolean incallback = false;	

		public RequestProcessor(HashMap inputMap, String documentPath) {
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
				processResponse( responseObj,  documentPath,  today,  incallback);
			} catch (Exception e) {
				logger.error(e, e);
				// no action taken
			}

		}

		public void sendRequest() throws Exception {
			String serverUrl = InitialSetUp.company.get("url");
			String shortName = InitialSetUp.company.get("shortname");
			String authCode = InitialSetUp.company.get("authcode");
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

			logger.debug("*************** REQUEST INFO BEGIN ************:::");
			logger.debug("SERVERURL: " + serverUrl);
			logger.debug("SHORTNAME: " + shortName);
			logger.debug("PROJECT NUMBER: " + projNumber);
			logger.debug("SERVICE NAME: " + serviceName+"   "+serviceName.equalsIgnoreCase(GET_BPRECORD));
			logger.debug("INPUTXML: " + inputMap.get(INPUTXML));
			logger.debug("*************** REQUEST INFO END ************");
			
			String inputxml = inputMap.get(INPUTXML) != null ? inputMap.get(
					INPUTXML).toString() : null;
			int lineitemcount = 0;		
			if(inputxml != null){
				inputxml = "<?xml version='1.0' encoding='ISO-8859-1' ?>\n"+inputxml;
				lineitemcount = StringUtils.countMatches(inputxml, LINE_ITEM_TAG);
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
			} else if (serviceName.equalsIgnoreCase(CREATE_COMPLETE_BPRECORD)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				logger.debug("CREATE_COMPLETE_BPRECORD Number of lineitems are :"+lineitemcount);
				if(!nocallback &&  !incallback && lineitemcount > MAX_LINEITEMS){
					// must set this to callback servrice only if nocallback has not been set
					incallback = true;
					logger.info("Number of lineitems are quite high, to avoid timeout this has been set for callback service.");
					logger.info("Check after "+(callback_poll_server/1000)+" seconds.");
				}

				String bpName = this.inputMap.get(BPNAME) != null ?
						        (String) this.inputMap.get(BPNAME) : null;
				 
				String zipfile = (String)this.inputMap.get(ZIPFILE);
				if (( zipfile != null ) && (zipfile.trim().length() > 0))
				{
					this.inputMap.put(ISZIPFILE, "yes");
				}
				else
				{
					this.inputMap.put(ISZIPFILE, "no");
				}
				
				logger.debug("CREATE_COMPLETE_BPRECORD sending data");
				List<String> files = new ArrayList<String>();
				files.add(zipfile);
				FileObject[] atts = getBPAttachments( documentPath, FileUtils.InputFileServiceAttDirectory, 
						(List)this.inputMap.get(FILELIST), today, isZip(), zipfile);
				logger.debug("Input XML: " + inputxml);
				responseObj = uc.createCompleteBPRecord(shortName, authCode, projNumber, bpName, inputxml, isZip(), atts );
				//delete temp zip file here if created
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
				logger.debug("in UPDATE_BPRECORDV2 ====> Number of options are :"+options);
				responseObj = uc.updateBPRecordV2(shortName, authCode,
						projNumber, bpName, inputxml, options);
			} else if (serviceName.equalsIgnoreCase(UPDATE_COMPLETE_BPRECORD)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				logger.debug("UPDATE_COMPLETE_BPRECORD Number of lineitems are :"+lineitemcount);
				if(!nocallback &&  !incallback && lineitemcount > MAX_LINEITEMS){
					// must set this to callback servrice only if nocallback has not been set
					incallback = true;
					logger.info("Number of lineitems are quite high, to avoid timeout this has been set for callback service.");
					logger.info("Check after "+(callback_poll_server/1000)+" seconds.");
				}

				String bpName = this.inputMap.get(BPNAME) != null ?
						        (String) this.inputMap.get(BPNAME) : null;
				String zipfile = (String)this.inputMap.get(ZIPFILE);
				if (( zipfile != null ) && (zipfile.trim().length() > 0))
				{
					this.inputMap.put(ISZIPFILE, "yes");
				}
				else
				{
					this.inputMap.put(ISZIPFILE, "no");
				}
				logger.debug("UPDATE_COMPLETE_BPRECORD sending data");
				FileObject[] atts = getBPAttachments( documentPath, FileUtils.InputFileServiceAttDirectory, 
						(List)this.inputMap.get(FILELIST), today, isZip(), zipfile);
				logger.debug("Input XML: " + inputxml);
				responseObj = uc.updateCompleteBPRecord(shortName, authCode, projNumber, bpName, inputxml, isZip(), atts );
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
			} else if (serviceName.equalsIgnoreCase(ADD_COMPLETE_BPLINEITEM)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				logger.debug("ADD_COMPLETE_BPLINEITEM Number of lineitems are :"+lineitemcount);
				if(!nocallback &&  !incallback && lineitemcount > MAX_LINEITEMS){
					// must set this to callback servrice only if nocallback has not been set
					incallback = true;
					logger.info("Number of lineitems are quite high, to avoid timeout this has been set for callback service.");
					logger.info("Check after "+(callback_poll_server/1000)+" seconds.");
				}

				String bpName = this.inputMap.get(BPNAME) != null ?
						        (String) this.inputMap.get(BPNAME) : null;
				String zipfile = (String)this.inputMap.get(ZIPFILE);
				if (( zipfile != null ) && (zipfile.trim().length() > 0))
				{
					this.inputMap.put(ISZIPFILE, "yes");
				}
				else
				{
					this.inputMap.put(ISZIPFILE, "no");
				}
				logger.debug("ADD_COMPLETE_BPLINEITEM sending data");
				FileObject[] atts = getBPAttachments( documentPath, FileUtils.InputFileServiceAttDirectory, 
						(List)this.inputMap.get(FILELIST), today, isZip(), zipfile);
				logger.debug("Input XML: " + inputxml);
				responseObj = uc.addCompleteBPLineItem(shortName, authCode, projNumber, bpName, inputxml, isZip(), atts );
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
				logger.debug(" The service name in the condition "+serviceName);
				String bpName = this.inputMap.get(BPNAME) != null ?
				        (String) this.inputMap.get(BPNAME) : null;
				String recordno = (String) this.inputMap.get(RECORDNUMBER);
				responseObj = uc.getBPRecord(shortName, authCode, projNumber,
						bpName, recordno);
			} else if (serviceName.equalsIgnoreCase(GET_PLANNINGITEM)) {
				String bpName = this.inputMap.get(BPNAME) != null ?
				        (String) this.inputMap.get(BPNAME) : null;
				String recordno = (String) this.inputMap.get(RECORDNUMBER);
				String planningitem = (String) this.inputMap.get(PLANNINGITEM);
				responseObj = uc.getPlanningItem(shortName, authCode, projNumber,
						bpName, recordno, planningitem);
			}else if (serviceName.equalsIgnoreCase(GET_WBSSTRUCTURE)) {
				responseObj = uc.getWBSStructure(shortName, authCode);
			} else if (serviceName.equalsIgnoreCase(GET_COMPLETE_BPRECORD)) {
				String bpName = this.inputMap.get(BPNAME) != null ?
				        (String) this.inputMap.get(BPNAME) : null;
				String recordno = (String) this.inputMap.get(RECORDNUMBER);
				responseObj = uc.getCompleteBPRecord(shortName, authCode,
						projNumber, bpName, recordno);
			} else if (serviceName.equalsIgnoreCase(GET_SOV)) {
				String bpName = this.inputMap.get(BPNAME) != null ?
				        (String) this.inputMap.get(BPNAME) : null;
				String recordno = (String) this.inputMap.get(RECORDNUMBER);
				responseObj = uc.getSOV(shortName, authCode,
						projNumber, bpName, recordno);
			} else if (serviceName.equalsIgnoreCase(GET_OBJECT_LIST)) {
				String objName = this.inputMap.get(OBJECTNAME) != null ?
				         (String)this.inputMap.get(OBJECTNAME) : null;
				responseObj = uc.getObjectList(shortName, authCode, objName,
						getFieldNames(), getFilterCondition(),
						getFilterValues());
			}  else if (serviceName.equalsIgnoreCase(GET_COLUMN_DATA)) {
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
				/*
				if(!nocallback &&  !incallback && lineitemcount > MAX_LINEITEMS){
					// must set this to callback servrice only if nocallback has not been set
					incallback = true;
					logger.info("Number of lineitems are quite high, to avoid timeout this has been set for callback service.");
					logger.info("Check after "+(callback_poll_server/1000)+" seconds.");
				}
				*/
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
				incallback = isCallbackService;
				logger.debug("Number of lineitems are :"+lineitemcount);
				String sheetname = this.inputMap.get(SHEETNAME) != null ?
								        (String) this.inputMap.get(SHEETNAME) : null;
				String options = this.inputMap.get(SCHEDULEOPTIONS) != null ?
										        (String) this.inputMap.get(SCHEDULEOPTIONS) : null;
										        
                logger.debug("Number of options are :"+options);
                
				responseObj = uc.createScheduleActivitiesV2(shortName, authCode,
						projNumber, sheetname, inputxml , options);
				logger.info("createScheduleActivities:  shhetname:"+sheetname);
			} else if (serviceName.equalsIgnoreCase(CREATE_FILE_ACTIVITIESV2)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				
				String sheetname = this.inputMap.get(SHEETNAME) != null ?
								        (String) this.inputMap.get(SHEETNAME) : null;
				String options = this.inputMap.get(SCHEDULEOPTIONS) != null ?
										        (String) this.inputMap.get(SCHEDULEOPTIONS) : null;
				logger.debug("Schedule of sheetname are :"+sheetname);						        
		        String zipfile = (String)this.inputMap.get(ZIPFILE);
				if (( zipfile != null ) && (zipfile.trim().length() > 0))
					this.inputMap.put(ISZIPFILE, "yes");
				else
					this.inputMap.put(ISZIPFILE, "no");
				
				logger.debug("CREATE_FILE_ACTIVITIESV2 sending data "+zipfile);
				List<String> filenames = new ArrayList<String>();
				filenames.add(zipfile);
				FileObject[] atts = getBPAttachments( documentPath, FileUtils.InputFileServiceAttDirectory, 
						filenames, today, isZip(), zipfile);						        
                FileObject fileobj = null;
                if(atts != null && atts.length > 0)
					fileobj = atts[0];
					
				logger.debug(" create fileobj are :"+fileobj);
				
				responseObj = uc.createScheduleActivitiesFromFileV2(shortName, authCode,
						projNumber, sheetname,options,isZip(),fileobj);
				logger.info("createScheduleActivitiesFromFileV2:  shhetname:"+sheetname);
			}  else if (serviceName.equalsIgnoreCase(UPDATE_ACTIVITIES)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				logger.debug("Number of lineitems are :"+lineitemcount);
				String sheetname = this.inputMap.get(SHEETNAME) != null ?
								        (String) this.inputMap.get(SHEETNAME) : null;
				responseObj = uc.updateScheduleActivities(shortName, authCode,
						projNumber, sheetname, inputxml);
				logger.info("updateScheduleActivitiesV2:  sheetname:"+sheetname);
			} else if (serviceName.equalsIgnoreCase(UPDATE_ACTIVITIESV2)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				logger.debug("Number of lineitems are :"+lineitemcount);
				String sheetname = this.inputMap.get(SHEETNAME) != null ?
								        (String) this.inputMap.get(SHEETNAME) : null;
				String options = this.inputMap.get(SCHEDULEOPTIONS) != null ?
										        (String) this.inputMap.get(SCHEDULEOPTIONS) : null;
				responseObj = uc.updateScheduleActivitiesV2(shortName, authCode,
						projNumber, sheetname, inputxml , options);
				logger.info("updateScheduleActivitiesV2:  sheetname:"+sheetname);
			} else if (serviceName.equalsIgnoreCase(UPDATE_FILE_ACTIVITIESV2)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				incallback = isCallbackService;
				logger.debug("Number of lineitems are :"+lineitemcount);
				String sheetname = this.inputMap.get(SHEETNAME) != null ?
								        (String) this.inputMap.get(SHEETNAME) : null;
				String options = this.inputMap.get(SCHEDULEOPTIONS) != null ?
		        (String) this.inputMap.get(SCHEDULEOPTIONS) : null;
		        String zipfile = (String)this.inputMap.get(ZIPFILE);
				if (( zipfile != null ) && (zipfile.trim().length() > 0))
				{
					this.inputMap.put(ISZIPFILE, "yes");
				}
				else
				{
					this.inputMap.put(ISZIPFILE, "no");
				}
				logger.debug("UPDATE_FILE_ACTIVITIESV2 sending data");
				List<String> filenames = new ArrayList<String>();
				filenames.add(zipfile);
				
				FileObject[] atts = getBPAttachments( documentPath, FileUtils.InputFileServiceAttDirectory, 
														filenames, today, isZip(), zipfile);			
				FileObject fileobj = null;
                if(atts != null || atts.length > 0)
					fileobj = atts[0];
				responseObj = uc.updateScheduleActivitiesFromFileV2(shortName, authCode,
						projNumber, sheetname, options,isZip(),fileobj);
				logger.info("updateScheduleActivitiesFromFileV2:  sheetname:"+sheetname);
			} else if (serviceName.equalsIgnoreCase(GET_ACTIVITIES)) {
				incallback = isCallbackService;
				logger.debug("Number of lineitems are :"+lineitemcount);
				String sheetname = this.inputMap.get(SHEETNAME) != null ?
								        (String) this.inputMap.get(SHEETNAME) : null;
				responseObj = uc.getScheduleActivities(shortName, authCode,
						projNumber, sheetname);
				logger.info("getScheduleActivities:  sheetname:"+sheetname);
			} else if (serviceName.equalsIgnoreCase(GET_SPACE_LIST)) {
				String spaceType = this.inputMap.get(SPACETYPE) != null ?
								        (String) this.inputMap.get(SPACETYPE) : null;
				String filterCondition = getFilterCondition();
				String fieldNames = this.inputMap.get(FIELDNAMES) != null ?
				        (String) this.inputMap.get(FIELDNAMES) : null;
				responseObj = uc.getSpaceList(shortName, authCode,
						projNumber, spaceType,fieldNames,filterCondition);
				logger.info("getSpaceList:  spaceType:"+spaceType);	
			} else if (serviceName.equalsIgnoreCase(GET_LEVEL_LIST)) {
				String filterCondition = getFilterCondition();
				String fieldNames = this.inputMap.get(FIELDNAMES) != null ?
				        (String) this.inputMap.get(FIELDNAMES) : null;
				responseObj = uc.getLevelList(shortName, authCode,
						projNumber, fieldNames,filterCondition);
				logger.info("getLevelList:  ");		
			}	else if (serviceName.equalsIgnoreCase(CREATE_LEVEL)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				responseObj = uc.createLevel(shortName, authCode,
						projNumber, inputxml);
				logger.info("createLevel:  ");		
			} else if (serviceName.equalsIgnoreCase(UPDATE_LEVEL)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				responseObj = uc.updateLevel(shortName, authCode,
						projNumber, inputxml);
				logger.info("updateLevel:  ");		
			}
			else if (serviceName.equalsIgnoreCase(CREATE_SPACE)) {
				logger.debug("createSpace:  ");	
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				String spaceType = this.inputMap.get(SPACETYPE) != null ?
				        (String) this.inputMap.get(SPACETYPE) : null;
				logger.debug("createSpace:  "+spaceType);	       
				responseObj = uc.createSpace(shortName, authCode,
						projNumber,spaceType, inputxml);
				logger.info("createSpace:  ");		
			} else if (serviceName.equalsIgnoreCase(UPDATE_SPACE)) {
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				String spaceType = this.inputMap.get(SPACETYPE) != null ?
				        (String) this.inputMap.get(SPACETYPE) : null;
				responseObj = uc.updateSpace(shortName, authCode,
						projNumber,spaceType, inputxml);
				logger.info("updateSpace: ");		
			}
			else if (serviceName.equalsIgnoreCase(CREATE_UPDATE_ROLE)) {
				logger.debug("createUpdateRole   ");	
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				logger.debug("createUpdateRole  ****  ");	       
				responseObj = uc.createUpdateRole(shortName, authCode, inputxml);
				logger.info("createUpdateRole:  ");	
			}
			else if (serviceName.equalsIgnoreCase(CREATE_UPDATE_RESOURCE)) {
				logger.debug("createUpdateResource   ");	
				if(inputxml == null) {
					logInputXmlError();
					return;
				}
				logger.debug("createUpdateResource  ****  ");	       
				responseObj = uc.createUpdateResource(shortName, authCode, inputxml);
				logger.info("createUpdateResource: ret  ");	

			}
			else if (serviceName.equalsIgnoreCase(GET_RESOURCE_LIST)) {
				incallback = isCallbackService;
				logger.debug("getResourceList in processfile *** ");
				responseObj = uc.getResourceList(shortName, authCode, getFilterCondition());
			}
			else if (serviceName.equalsIgnoreCase(GET_ROLE_LIST)) {
				incallback = isCallbackService;
				logger.debug("getRoleList in processfile *** ");
				responseObj = uc.getRoleList(shortName, authCode, getFilterCondition());
			}
			else if (serviceName.equalsIgnoreCase(CREATE_CONFIG_MODULE)) {
				incallback = isCallbackService;
				logger.debug("createConfigurableModuleRecord ");
				responseObj = uc.createConfigurableModuleRecord(
						shortName, authCode, projNumber,(String) this.inputMap.get(CMCODE), (String) this.inputMap.get(CLASSNAME),  (String) this.inputMap.get(COPYFROMRECORD), inputxml);
				logger.info("createConfigurableModuleRecord:  cmcode:"+(String) this.inputMap.get(CMCODE));
			} else if (serviceName.equalsIgnoreCase(UPDATE_CONFIG_MODULE)) {
				incallback = isCallbackService;
				logger.debug("updateConfigurableModuleRecord ");
				responseObj = uc.updateConfigurableModuleRecord(
						shortName, authCode, projNumber, (String) this.inputMap.get(CMCODE), (String) this.inputMap.get(CLASSNAME),   inputxml);
				logger.info("updateConfigurableModuleRecord:  cmcode:"+(String) this.inputMap.get(CMCODE));
			} else if (serviceName.equalsIgnoreCase(CREATE_SHELL)) {
				incallback = isCallbackService;
				logger.debug("createShell ");
				responseObj = uc.createShell(
						shortName, authCode,  (String) this.inputMap.get(COPYFROMSHELLTEMPLATE),   inputxml);
				logger.info("createShell:  copyfrom:"+(String) this.inputMap.get(COPYFROMSHELLTEMPLATE));
			} else if (serviceName.equalsIgnoreCase(UPDATE_SHELL)) {
				incallback = isCallbackService;
				logger.debug("updateShell ");
				responseObj = uc.updateShell(
						shortName, authCode, (String) this.inputMap.get(SHELLTYPE),    inputxml);
				logger.info("updateShell:  number:"+(String) this.inputMap.get(SHELLTYPE));
			} else if (serviceName.equalsIgnoreCase(GET_SHELL_LIST)) {
				incallback = isCallbackService;
				logger.debug("getShell list ");
				responseObj = uc.getShellList(
						shortName, authCode, (String) this.inputMap.get(SHELLTYPE), getFilterCondition());
				logger.info("getShellList:  type:"+(String) this.inputMap.get(SHELLTYPE));

			} else if (serviceName.equalsIgnoreCase(GET_EXCHANGE_RATES)) {
				responseObj = uc.getExchangeRates(shortName, authCode);
			} else if (serviceName.equalsIgnoreCase(UPDATE_EXCHANGE_RATES)) {
				if (inputxml == null) {
					logInputXmlError();
					return;
				}
				responseObj = uc.updateExchangeRates(shortName, authCode, inputxml);
			} else if (serviceName.equalsIgnoreCase(CREATE_USER)) {
				incallback = isCallbackService;
				logger.debug("createUser ");
				responseObj = uc.createUser(shortName, authCode, (String) this.inputMap.get(COPY_FROM_USER_PREFERENCE_TEMPLATE), inputxml);
			} else if (serviceName.equalsIgnoreCase(UPDATE_USER)) {
				incallback = isCallbackService;
				logger.debug("updateUser ");
				responseObj = uc.updateUser(shortName, authCode, inputxml);
			} else if (serviceName.equalsIgnoreCase(UPDATE_USERGROUPMEMBERSHIP)) {
				incallback = isCallbackService;
				logger.debug("updateUserGroupMembership ");
				responseObj = uc.updateUserGroupMembership(shortName, authCode, inputxml);
			} else if (serviceName.equalsIgnoreCase(UPDATE_USER_SHELL_MEMBERSHIP)) {
				incallback = isCallbackService;
				logger.debug("updateUserShellMembership ");
				responseObj = uc.updateUserShellMembership(shortName, authCode, projNumber, inputxml);
			} else if (serviceName.equalsIgnoreCase(GET_USER_LIST)) {
				incallback = isCallbackService;
				logger.debug("getUserList ");
				responseObj = uc.getUserList(shortName, authCode, getFilterCondition());
			} else {
				WebLinkLogLoader.JobErrorLogger
						.error("Unsupported/invalid service request: "	+ serviceName);
				logger.error("Unsupported/invalid service request: "
						+ serviceName);
				FileUtils.moveFiles(FileUtils.TempFileServiceBaseDirectory,
						FileUtils.ErrorFileServiceBaseDirectory	+ File.separator + today, fileName_withExt);
				return;
			}
		}

		private void logInputXmlError() throws Exception {
			WebLinkLogLoader.JobErrorLogger
			.error("Input XML is Null or Invalid");
			logger.error("Input XML is Null or Invalid");
			FileUtils.moveFiles(FileUtils.TempFileServiceBaseDirectory,
					FileUtils.ErrorFileServiceBaseDirectory
					+ File.separator + today, fileName_withExt);
		}
		
		
		private String[] getFilterValues() {
			if (this.inputMap.get(FILTERVALUELIST) == null)
				return null;
			List filterVallist = (ArrayList) this.inputMap.get(FILTERVALUELIST);
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
			List fnamelist = (ArrayList) this.inputMap.get(FIELDNAMELIST);
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

		private String isZip(){
			String zipfile = (String)this.inputMap.get(ZIPFILE);
			
			String iszipfile = "no"; 
			if (zipfile != null && zipfile.trim().length() > 0 )
				iszipfile = "yes";
			else if ( "no".equalsIgnoreCase(attachzip)) iszipfile = "no";
			else iszipfile = "yes";
			return iszipfile;
		}

	}// end class RequestProcessor

}
