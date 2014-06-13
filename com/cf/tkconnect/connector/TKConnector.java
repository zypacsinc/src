/*
 * 
 * 
 * 
 * Author Cyril Furtado
 * smartlinkConnector for Integration 
 * 
 *      
 * 
 */

package com.cf.tkconnect.connector;




import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.mainservice.MainServiceClient;
import com.cf.tkconnect.mainservice.MainServices;
import com.cf.tkconnect.mainservice.MainServicesServiceLocator;
import com.cf.tkconnect.mainservice.MainserviceSoapBindingStub;
import com.cf.tkconnect.process.ResponseObject;

import com.cf.tkconnect.util.InitialSetUp;
import com.cf.tkconnect.util.WSUtil;
import com.cf.tkconnect.util.xml.FileData;
import com.cf.tkconnect.util.xml.FileObject;
import com.cf.tkconnect.util.xml.XMLObject;


import static com.cf.tkconnect.util.InitialSetUp.isCallbackService;
import static com.cf.tkconnect.util.InitialSetUp.servicestyle;

import static com.cf.tkconnect.util.WSConstants.MAIN_SERVICES;
import static com.cf.tkconnect.util.WSUtil.convertXMLErrorObject;
import static com.cf.tkconnect.util.WSUtil.convertXMLObject;


/**
 * This class shows how to use the Call object's ability to to call the web
 * service server
 * 
 * 
 */
public final class TKConnector {
	static Log logger = LogSource.getInstance(TKConnector.class);

	MainServicesServiceLocator mss;
	MainServices binding;
	String url = "";
	String uServicestyle = servicestyle;
	
	public TKConnector(String inpurl, String uServicestyle) throws Exception{
		this.uServicestyle = uServicestyle;
		this.init(inpurl);
	}

	public TKConnector(String inpurl) throws Exception{
		// default is 
		this.init(inpurl);
	}
	
	private void init(String inpurl) throws Exception{
		this.url = inpurl;
		
		logger.debug(" The service style value is "+uServicestyle);
		if(uServicestyle.equalsIgnoreCase(MAIN_SERVICES)){
			this.url = WSUtil.getMainServiceUrl(inpurl);
			 mss = new MainServicesServiceLocator();
			 mss.setmainserviceAddress(url);
			 binding = mss.getmainservice();
		    ((MainserviceSoapBindingStub) binding).setMaintainSession(true);

		
		}else{
			throw new Exception("Service name : "+uServicestyle+" not found.");
		}
	}

	public ResponseObject getScheduleSheetDataMaps ( String shortname , String authcode , String projectnumber, 
			String sheetName, String options)throws Exception {
		// here we need to know where this target is called
		logger.debug("getScheduleSheetDataMaps start:" + projectnumber);
		try {
			
			return convertXMLObject( ((MainserviceSoapBindingStub) binding).getScheduleSheetDataMaps(shortname,
						authcode, projectnumber, sheetName, options));
		}
		catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error("Error in smartlink  getScheduleSheetDataMaps:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end getScheduleSheetDataMaps
	
	public ResponseObject getScheduleSheetList ( String shortname , String authcode ,
			String projectnumber, String options) throws Exception {
		// here we need to know where this target is called
		logger.debug("getScheduleSheetList start:" + projectnumber);
		try {
			return convertXMLObject( ((MainserviceSoapBindingStub) binding).getScheduleSheetList(shortname,
						authcode, projectnumber, options));
		}
		catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error("Error in smartlink  getScheduleSheetList:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end getScheduleSheetList

	
	public ResponseObject getWBSCodes ( String shortname , String authcode ,  String projectnumber, String options) throws Exception {
		// here we need to know where this target is called
		logger.debug("getWBSCodes start:" + projectnumber);
		try {
			
				return convertXMLObject( ((MainserviceSoapBindingStub) binding).getWBSCodes(shortname,
						authcode, projectnumber,  options));
		}
		catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error("Error in smartlink  getWBSCodes:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// getWBSCodes
	
	public ResponseObject getScheduleDataMapsDetails ( String shortname , String authcode , String projectnumber,
			String sheetName, String datamap, String options) throws Exception {
		// here we need to know where this target is called
		logger.debug("getScheduleSheetDataMapsDetails start:" + projectnumber);
		try {
			
				return convertXMLObject( ((MainserviceSoapBindingStub) binding).getScheduleDataMapsDetails(shortname,
						authcode, projectnumber, sheetName,datamap, options));
		}
		catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error("Error in smartlink  getScheduleSheetDataMapsDetails:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// getScheduleSheetDataMapsDetails
	
	public ResponseObject updateScheduleDataMapsDetails ( String shortname , String authcode , String projectnumber, String sheetName,
			String datamap, String dataXML, String options) throws Exception {
				// here we need to know where this target is called
				logger.debug("updateScheduleSheetDataMaps start:" + projectnumber);
				try {
					
						return convertXMLObject( ((MainserviceSoapBindingStub) binding).updateScheduleDataMapsDetails(shortname,
								authcode, projectnumber, sheetName,datamap,dataXML, options));
				}
				catch (java.rmi.RemoteException re) {
					String err = re.getMessage();
					int errCode = 500;
					logger.error("Error in smartlink  updateScheduleSheetDataMaps:" + err);
					return convertXMLErrorObject(errCode, err);
				}
	}// updateScheduleSheetDataMaps
			
	
	public ResponseObject getProjectShellList ( String shortname , String authcode , String options) throws Exception {
		// here we need to know where this target is called
		logger.debug("getProjectShellList start:" + options);
		try {
			
				return convertXMLObject( ((MainserviceSoapBindingStub) binding).getProjectShellList(shortname,
						authcode, options));
		}
		catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error("Error in smartlink  getProjectShellList:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end getProjectShellList

	
	public ResponseObject createWBS(String shortname, String authcode,
			String projectNumber, String WBSXML) throws Exception {
		// here we need to know where this target is called
		logger.debug("createWBS start:" + projectNumber);
		try {
			
				return convertXMLObject( ((MainserviceSoapBindingStub) binding).createWBS(shortname,
						authcode, projectNumber, WBSXML));
		}
		catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error("Error in smartlink  createWBS:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end createWBS

	public ResponseObject createBPRecord(String shortname, String authcode,
			String projectNumber, String BPName, String BPXML) throws Exception {
		logger.debug("createBPRecord start:" + BPName);
		try {
			
				XMLObject xobj = null;
				xobj = ((MainserviceSoapBindingStub) binding).createBPRecord(
						shortname, authcode, projectNumber, BPName, BPXML);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  createBPRecord:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end createBPRecord

	public ResponseObject createCompleteBPRecord(String shortname, String authcode,
			String projectNumber, String BPName, String BPXML, String iszipfile, FileObject[] files) throws Exception {
		logger.debug("createCompleteBPRecord start:" + BPName);
		try {
			XMLObject xobj = null;
				xobj = ((MainserviceSoapBindingStub) binding).createCompleteBPRecord(
						shortname, authcode, projectNumber, BPName, BPXML, iszipfile, files);
			return WSUtil.convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  createCompleteBPRecord:" + err);
			return WSUtil.convertXMLErrorObject(errCode, err);
		}
	}// end createCompleteBPRecord
	public ResponseObject createCompleteBPRecord(String shortname, String authcode,
			String projectNumber, String BPName, String BPXML, String iszipfile, FileData[] files) throws Exception {
		logger.debug("createCompleteBPRecord start:" + BPName);
		return null;
	}// end createCompleteBPRecord


	public ResponseObject updateCompleteBPRecord(String shortname, String authcode,
			String projectNumber, String BPName, String BPXML, String iszipfile, FileObject[] files) throws Exception {
		logger.debug("updateCompleteBPRecord start:" + BPName);
		try {
			XMLObject xobj = null;
				xobj = ((MainserviceSoapBindingStub) binding).updateCompleteBPRecord(
						shortname, authcode, projectNumber, BPName, BPXML, iszipfile, files);
			return WSUtil.convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  updateCompleteBPRecord:" + err);
			return WSUtil.convertXMLErrorObject(errCode, err);
		}
	}// end updateCompleteBPRecord

	public ResponseObject updateCompleteBPRecord(String shortname, String authcode,
			String projectNumber, String BPName, String BPXML, String iszipfile, FileData[] files) throws Exception {
		logger.debug("updateCompleteBPRecord start:" + BPName);
		return null;
	}// end updateCompleteBPRecord
	
	
	public ResponseObject updateBPRecord(String shortname, String authcode,
			String projectNumber, String BPName, String BPXML) throws Exception {
		logger.debug("updateBPRecord start:" + BPName);
		try {
			
				XMLObject xobj = null;
					xobj = ((MainserviceSoapBindingStub) binding).updateBPRecord(
							shortname, authcode, projectNumber, BPName, BPXML);
				return convertXMLObject(xobj);
			
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  updateBPRecord:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end updateBPRecord

	public ResponseObject updateBPRecordV2(String shortname, String authcode,
			String projectNumber, String BPName, String BPXML, String options) throws Exception {
		logger.debug("updateBPRecordV2 start:" + BPName);
		logger.debug("updateBPRecordV2 _options:" + options);
		try {
			
				XMLObject xobj = null;
					xobj = ((MainserviceSoapBindingStub) binding).updateBPRecordV2(
							shortname, authcode, projectNumber, BPName, BPXML, options);
				return convertXMLObject(xobj);
			
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  updateBPRecordV2:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end updateBPRecordV2

	public ResponseObject getWBSStructure(String shortname, String authcode	)
			throws Exception {
		logger.debug("getWBSStructure start:");
		try {
			
			return convertXMLObject(((MainserviceSoapBindingStub) binding).getWBSStructure(
					shortname, authcode));
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  getWBSStructure:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end getWBSStructure

	public ResponseObject createProject(String shortname, String authcode,
			String cloneProjectNumber,  String projectxml)
			throws Exception {
		logger.debug("createProject start:" + cloneProjectNumber);
		try {
			
			XMLObject xobj = null;
				xobj = convertXMLObject(((MainserviceSoapBindingStub) binding).createProject(
					shortname, authcode, cloneProjectNumber,  projectxml));
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  createProject:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end 
	
	public ResponseObject getCompleteBPRecord(String shortname, String authcode,
			String projectNumber, String BPName, String record_no) throws Exception {
		logger.debug("getCompleteBPRecord start:" + BPName);
		try {
			logger.debug("sending values: "+ projectNumber + " "+ BPName + " "+ record_no);
			return convertXMLObject(((MainserviceSoapBindingStub) binding).getCompleteBPRecord(
					shortname, authcode, projectNumber, BPName, record_no));
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  getCompleteBPRecord:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end getCompleteBPRecord
	
		
	public ResponseObject getSOV(String shortname, String authcode,
			String projectNumber, String BPName, String record_no) throws Exception {
		logger.debug("getSOV start:" + BPName);
		try {
			logger.debug("sending values: "+ projectNumber + " "+ BPName + " "+ record_no);
			return convertXMLObject(((MainserviceSoapBindingStub) binding).getSOV(
					shortname, authcode, projectNumber, BPName, record_no));
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  getCompleteBPRecord:" + err);
			return convertXMLErrorObject(errCode, err);
		} catch (Exception ex){
			logger.debug("Get SOV Exception::" + ex.toString());
			throw ex;
		}
	}// end getSOV


	public ResponseObject addBPLineItem(String shortname, String authcode,
			String projectNumber, String BPName, String BPXML) throws Exception {
		logger.debug("addBPLineItem start:" + BPName);
		try {
			
			XMLObject xobj = null;
				xobj = ((MainserviceSoapBindingStub) binding).addBPLineItem(
						shortname, authcode, projectNumber, BPName, BPXML);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  addBPLineItem:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end addBPLineItem

	public ResponseObject addCompleteBPLineItem(String shortname, String authcode,
			String projectNumber, String BPName, String BPXML, String iszipfile, FileObject[] files) throws Exception {
		logger.debug("addCompleteBPLineItem start:" + BPName);
		try {
			XMLObject xobj = null;
				xobj = ((MainserviceSoapBindingStub) binding).addCompleteBPLineItem(
						shortname, authcode, projectNumber, BPName, BPXML, iszipfile, files);
			return WSUtil.convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  addCompleteBPLineItem:" + err);
			return WSUtil.convertXMLErrorObject(errCode, err);
		}
	}// end addCompleteBPLineItem

	public ResponseObject addCompleteBPLineItem(String shortname, String authcode,
			String projectNumber, String BPName, String BPXML, String iszipfile, FileData[] files) throws Exception {
		logger.debug("addCompleteBPLineItem start:" + BPName);
		return null;
	}// end addCompleteBPLineItem
	
	

	public ResponseObject createObject(String shortname, String authcode,
			String objectName, String ObjectXML) throws Exception {
		// here we need to know where this target is called
		// logger.debug("service start
		// createobject..........shortname:"+shortname+" authcode:"+authcode);
		logger.debug("createObject start:-----------" + objectName);
		try {
			
		XMLObject xobj = null;
				xobj = ((MainserviceSoapBindingStub) binding).createObject(
						shortname, authcode, objectName, ObjectXML);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  createObject:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end createObject

	public ResponseObject updateObject(String shortname, String authcode,
			String objectName, String objectXML) throws Exception {
		logger.debug("updateObject start:" + objectName);
		try {
			
			XMLObject xobj = null;
				xobj =((MainserviceSoapBindingStub) binding).updateObject(
						shortname, authcode, objectName, objectXML);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  updateObject:" + err);
			return convertXMLErrorObject(errCode, err);
		}

	}// end UpdateObject

	public ResponseObject getObjectList(String shortname, String authcode,
			String objectName, String[] fieldnames, String filterCondition,
			String[] filterValues) throws Exception {
		logger.error("******  getObjectList  :" + uServicestyle);
		try {
			
			return convertXMLObject( ((MainserviceSoapBindingStub) binding).getObjectList(
					shortname, authcode, objectName, fieldnames,
					filterCondition, filterValues));
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  getObjectList:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}
	
	public ResponseObject createUser(String shortname, String authcode,	String copyFromUserPreferenceTemplate, String userXML) throws Exception {
		try {
			
			XMLObject xobj = null;
			xobj = ((MainserviceSoapBindingStub) binding).createUser(shortname, authcode, copyFromUserPreferenceTemplate, userXML);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re, re);
			logger.error("Error in smartlink  createUser:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}
	
	public ResponseObject updateUser(String shortname, String authcode, String userXML) throws Exception {
		try {
			
			XMLObject xobj = null;
			xobj = ((MainserviceSoapBindingStub) binding).updateUser(shortname, authcode, userXML);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re, re);
			logger.error("Error in smartlink  updateUser:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}
	
	public ResponseObject getUserList(String shortname, String authcode, String filterCondition) throws Exception {
		try {
			
			XMLObject xobj = null;
			xobj = ((MainserviceSoapBindingStub) binding).getUserList(shortname, authcode, filterCondition);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re, re);
			logger.error("Error in smartlink  getUserList:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}

	public ResponseObject getBPList(String shortname, String authcode,
			String projectNumber, String bpName, String[] fieldnames,
			String filterCondition, String[] filterValues) throws Exception {

		try {
		
			return convertXMLObject(((MainserviceSoapBindingStub) binding).getBPList(shortname,
					authcode, projectNumber, bpName, fieldnames,	filterCondition, filterValues));
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  getBPList:" + err);
			return convertXMLErrorObject(errCode, err);
		}
		
	}// getBPList
	public ResponseObject getBPRecord(String shortname, String authcode,
			String projectNumber, String BPName, String record_no)
			throws Exception {
		logger.debug("getBPRecord mc start:" + BPName);
		try {
			
			return convertXMLObject(((MainserviceSoapBindingStub) binding).getBPRecord(
					shortname, authcode, projectNumber, BPName, record_no));
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  getBPRecord:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end getBPRecord

	public ResponseObject getPlanningItem(String shortname, String authcode,
			String projectNumber, String BPName, String record_no, String planningitem)
			throws Exception {
		logger.debug("getPlanningItem mc start:" + BPName);
		try {
			return convertXMLObject(((MainserviceSoapBindingStub) binding).getPlanningItem(
					shortname, authcode, projectNumber, BPName, record_no, planningitem));
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			return convertXMLErrorObject(errCode, err);
		}
	}// end getBPRecord

	public ResponseObject getIDResponse(String shortname, String authcode , String id	)
		throws Exception {
		logger.debug("getId start:");
		try {
			return convertXMLObject(((MainserviceSoapBindingStub) binding).getIDResponse(
					shortname, authcode, id));
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  getId:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end getIDResponse
	
	public ResponseObject updateColumnData(String shortname, String authcode,
			String projectNumber, String columnName, String columnXML) throws Exception {
		logger.debug("updateColumnData start:" + columnName);
		try {
			XMLObject xobj = null;
			
				xobj = ((MainserviceSoapBindingStub) binding).updateColumnData(
						shortname, authcode, projectNumber, columnName, columnXML);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  updateColumnData:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end updateColumnData

	public ResponseObject getColumnData(String shortname, String authcode,
			String projectNumber, String columnName) throws Exception {
		logger.debug("getColumnData start:" + columnName);
		try {
			
			XMLObject xobj = null;
			
				xobj = ((MainserviceSoapBindingStub) binding).getColumnData(
						shortname, authcode, projectNumber, columnName);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  getColumnData:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end getColumnData


	public ResponseObject getUDRData(String shortname, String authcode,
			String projectNumber, String reportName) throws Exception {
		logger.debug("getUDRData start:" + reportName);
		try {
			XMLObject xobj = null;
			
			MainServiceClient cl = new MainServiceClient(InitialSetUp.company.get("url"));
			return WSUtil.convertXMLObject(cl.getUDRData(shortname, authcode, projectNumber, reportName));

				//xobj = ((MainserviceSoapBindingStub) binding).getUDRData(
						//shortname, authcode, projectNumber, reportName );
			//return WSUtil.convertXMLObject(xobj);
		} catch (Exception re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in connect  getUDRData:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end getUDRData

	public ResponseObject createAsset(String shortname, String authcode,
			String assetClassName, String copyFromAsset, String assetXML) throws Exception {
		logger.debug("createAsset start:" + assetClassName);
		try {
			XMLObject xobj = null;
			
				xobj = ((MainserviceSoapBindingStub) binding).createAsset(
						shortname, authcode, assetClassName, copyFromAsset, assetXML);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  createAsset:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end createAsset

	public ResponseObject createScheduleActivities(String shortname, String authcode,
			String projectnumber, String sheetName, String sheetXML) throws Exception {
		logger.debug("createScheduleActivities start:" + sheetName);
		try {
			XMLObject xobj = null;
			
				xobj = ((MainserviceSoapBindingStub) binding).createScheduleActivities(
						shortname, authcode, projectnumber, sheetName, sheetXML);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  createScheduleActivities:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end createScheduleActivities
	public ResponseObject updateScheduleActivities(String shortname, String authcode,
			String projectnumber, String sheetName, String sheetXML) throws Exception {
		logger.debug("updateScheduleActivities start:" + sheetName);
		try {
			XMLObject xobj = null;
			
			xobj = ((MainserviceSoapBindingStub) binding).updateScheduleActivities(
						shortname, authcode, projectnumber, sheetName, sheetXML);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  updateScheduleActivities:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end updateScheduleActivities

	public ResponseObject createScheduleActivitiesFromFileV2(String shortname, String authcode,
			String projectnumber, String sheetName, String options,String iszipfile, FileObject files) throws Exception {
		logger.debug("createScheduleActivitiesV2 start:" + sheetName+" files:"+files);
		logger.debug("createScheduleActivitiesV2 shortname:" + shortname+" projectnumber:"+projectnumber+" iszipfile "+iszipfile);
		try {
			XMLObject xobj = null;
			
				xobj = ((MainserviceSoapBindingStub) binding).createScheduleActivitiesFromFileV2(
						shortname, authcode, projectnumber, sheetName, options, iszipfile, files);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  createScheduleActivitiesFromFileV2:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end createScheduleActivitiesFromFileV2
	
	public ResponseObject createScheduleActivitiesFromFileV2(String shortname, String authcode,
			String projectnumber, String sheetName, String options,String iszipfile, FileData files) throws Exception {
		logger.debug("createScheduleActivitiesV2 start:" + sheetName+" files:"+files);
		logger.debug("createScheduleActivitiesV2 shortname:" + shortname+" projectnumber:"+projectnumber+" iszipfile "+iszipfile);
		return null;
	}// end createScheduleActivitiesFromFileV2
	public ResponseObject updateScheduleActivitiesFromFileV2(String shortname, String authcode,
			String projectnumber, String sheetName, String options,String iszipfile, FileData files) throws Exception {
		logger.debug("createScheduleActivitiesV2 start:" + sheetName+" files:"+files);
		logger.debug("createScheduleActivitiesV2 shortname:" + shortname+" projectnumber:"+projectnumber+" iszipfile "+iszipfile);
		return null;
	}// end updatecheduleActivitiesFromFileV2
	
	public ResponseObject createScheduleActivitiesV2(String shortname, String authcode,
			String projectnumber, String sheetName, String sheetXML,String options) throws Exception {
		logger.debug("createScheduleActivitiesV2 start:" + sheetName+" options:"+options);
		try {
			XMLObject xobj = null;
			
				xobj = ((MainserviceSoapBindingStub) binding).createScheduleActivitiesV2(
						shortname, authcode, projectnumber, sheetName, sheetXML, options);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  createScheduleActivitiesV2:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end createScheduleActivities
	public ResponseObject updateScheduleActivitiesV2(String shortname, String authcode,
			String projectnumber, String sheetName, String sheetXML ,String options) throws Exception {
		logger.debug("updateScheduleActivitiesV2 start:" + sheetName);
		try {
			XMLObject xobj = null;
							xobj = ((MainserviceSoapBindingStub) binding).updateScheduleActivitiesV2(
						shortname, authcode, projectnumber, sheetName, sheetXML, options);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  updateScheduleActivitiesV2:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end updateScheduleActivities
	
	public ResponseObject updateScheduleActivitiesFromFileV2(String shortname, String authcode,
			String projectnumber, String sheetName, String options,String iszipfile, FileObject files) throws Exception {
		logger.debug("updateScheduleActivitiesV2 start:" + sheetName);
		try {
			XMLObject xobj = null;
				xobj = ((MainserviceSoapBindingStub) binding).updateScheduleActivitiesFromFileV2(
						shortname, authcode, projectnumber, sheetName,  options, iszipfile, files);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  updateScheduleActivitiesFromFileV2:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end updateScheduleActivities

	public ResponseObject getScheduleActivities(String shortname, String authcode,
			String projectnumber, String sheetName) throws Exception {
		logger.debug("getScheduleActivities start:" + sheetName);
		try {
			XMLObject xobj = null;
			
				xobj = ((MainserviceSoapBindingStub) binding).getScheduleActivities(
						shortname, authcode, projectnumber, sheetName);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  getScheduleActivities:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}// end getScheduleActivities
	
	public ResponseObject createConfigurableModuleRecord(String shortname, String authcode, String projectnumber, String CMCode, String ClassName, String copyFromRecord, String recordXML) throws Exception{
		logger.debug("createConfigurableModuleRecord start:" + ClassName);
		try {
			XMLObject xobj = null;
			
				xobj = ((MainserviceSoapBindingStub) binding).createConfigurableModuleRecord(
						shortname, authcode,projectnumber, CMCode, ClassName, copyFromRecord, recordXML);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  createConfigurableModuleRecord:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}
	
	public ResponseObject updateConfigurableModuleRecord(String shortname, String authcode,String projectnumber, String CMCode, String ClassName, String recordXML)throws Exception{
		try {
			XMLObject xobj = null;
			
				xobj = ((MainserviceSoapBindingStub) binding).updateConfigurableModuleRecord(
						shortname, authcode, projectnumber,CMCode, ClassName,  recordXML);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500; 
			logger.error(re,re);
			logger.error("Error in smartlink  updateConfigurableModuleRecord:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}

	public ResponseObject createShell(String shortname, String authcode,  String copyFromShellTemplate, String shellXML)throws Exception{
		try {
			
			XMLObject xobj = null;
				xobj = ((MainserviceSoapBindingStub) binding).createShell(
						shortname, authcode,  copyFromShellTemplate,  shellXML);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  createShell:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}

	public ResponseObject updateShell(String shortname, String authcode, String shellType, String shellXML)throws Exception{
		try {
			
			XMLObject xobj = null;
				xobj = ((MainserviceSoapBindingStub) binding).updateShell(
						shortname, authcode, shellType,   shellXML);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  updateShell:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}
	public ResponseObject getShellList(String shortname, String authcode, String shellType, String filterCondition)throws Exception{
		try {
			
			XMLObject xobj = null;
				xobj = ((MainserviceSoapBindingStub) binding).getShellList(
						shortname, authcode, shellType, filterCondition);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  getShellList:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}
	
	public ResponseObject getExchangeRates(String shortname, String authcode)
			throws Exception {
		logger.debug("getExchangeRates start:");
		try {
			return convertXMLObject(((MainserviceSoapBindingStub) binding).getExchangeRates(
					shortname, authcode));
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  getExchangeRates:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	} // end getExchangeRates

	public ResponseObject updateExchangeRates(String shortname, String authcode, String ratesXML)
	throws Exception {
		logger.debug("updateExchangeRates start:");
		try {
			return convertXMLObject(((MainserviceSoapBindingStub) binding).updateExchangeRates(
					shortname, authcode, ratesXML));
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  updateExchangeRates:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	} // end getExchangeRates
	
	public ResponseObject getSpaceList(String shortname, String authcode,
			String projectNumber, String objectName, String fieldnames,
			String filterCondition) throws Exception {

		try {
			return convertXMLObject(((MainserviceSoapBindingStub) binding).getSpaceList(shortname,
					authcode, projectNumber, objectName, fieldnames,
					filterCondition));
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  getSpaceList:" + err);
			return convertXMLErrorObject(errCode, err);
		}
		
	}// getSpaceList
	
	public ResponseObject getLevelList(String shortname, String authcode,
			String projectNumber, String fieldnames,
			String filterCondition) throws Exception {

		try {
			return convertXMLObject(((MainserviceSoapBindingStub) binding).getLevelList(shortname,
					authcode, projectNumber,  fieldnames,
					filterCondition ));
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  getLevelList:" + err);
			return convertXMLErrorObject(errCode, err);
		}
		
	}// getLevelList
	public ResponseObject createLevel(String shortname, String authcode,
			String projectNumber,String levelXML) throws Exception {

		try {
			return convertXMLObject(((MainserviceSoapBindingStub) binding).createLevel(shortname,
					authcode, projectNumber,  levelXML
					 ));
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  createLevel:" + err);
			return convertXMLErrorObject(errCode, err);
		}
		
	}// createLevel
	
	public ResponseObject updateLevel(String shortname, String authcode,
			String projectNumber,String levelXML) throws Exception {
		try {
			return convertXMLObject(((MainserviceSoapBindingStub) binding).updateLevel(shortname,
					authcode, projectNumber,  levelXML
					 ));
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  updateLevel:" + err);
			return convertXMLErrorObject(errCode, err);
		}
		
	}// updateLevel
	
	public ResponseObject createSpace(String shortname, String authcode,
			String projectNumber, String spaceType, String spaceXML) throws Exception {

		try {
			return convertXMLObject(((MainserviceSoapBindingStub) binding).createSpace(shortname,
					authcode, projectNumber, spaceType,  spaceXML
					 ));
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  createSpace:" + err);
			return convertXMLErrorObject(errCode, err);
		}
		
	}// createSpace
	
	public ResponseObject updateSpace(String shortname, String authcode,
			String projectNumber, String spaceType, String spaceXML) throws Exception {
		try {
			return convertXMLObject(((MainserviceSoapBindingStub) binding).updateSpace(shortname,
					authcode, projectNumber, spaceType,  spaceXML
					 ));
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  updateSpace:" + err);
			return convertXMLErrorObject(errCode, err);
		}
		
	}// updateSpace
	
	
	public ResponseObject createUpdateRole(String shortname, String authcode,
			 String roleXML) throws Exception {
		try {
			return convertXMLObject(((MainserviceSoapBindingStub) binding).createUpdateRole(shortname,
					authcode,  roleXML
					 ));
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  createUpdateRole:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	
	}// createUpdateRole

	public ResponseObject createUpdateResource(String shortname, String authcode,
			 String resourceXML) throws Exception {
		try {
			return convertXMLObject(((MainserviceSoapBindingStub) binding).createUpdateResource(shortname,
					authcode,  resourceXML
					 ));
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  createUpdateResource:" + err);
			return convertXMLErrorObject(errCode, err);
}
		
	}// createUpdateResource
	
	public ResponseObject getRoleList(String shortname, String authcode,
			 String options) throws Exception {
		try {
			return convertXMLObject(((MainserviceSoapBindingStub) binding).getRoleList(shortname,
					authcode,  options
					 ));
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  getRoleList:" + err);
			return convertXMLErrorObject(errCode, err);
		}
		
	}// getRoleList

	public ResponseObject getResourceList(String shortname, String authcode,
			 String options) throws Exception {
		try {
			return convertXMLObject(((MainserviceSoapBindingStub) binding).getResourceList(shortname,
					authcode,  options
					 ));
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re,re);
			logger.error("Error in smartlink  getResourceList:" + err);
			return convertXMLErrorObject(errCode, err);
		}
		
	}// getRoleList
	
	public ResponseObject updateUserGroupMembership(String shortname, String authcode, String membershipXML) throws Exception {
		try {
			XMLObject xobj = null;
			xobj = ((MainserviceSoapBindingStub) binding).updateUserGroupMembership(shortname, authcode, membershipXML);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re, re);
			logger.error("Error in smartlink  updateUserGroupMembership:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}
	
	public ResponseObject updateUserShellMembership(String shortname, String authcode, String projectNumber, String membershipXML) throws Exception {
		try {
			XMLObject xobj = null;
			xobj = ((MainserviceSoapBindingStub) binding).updateUserShellMembership(shortname, authcode, projectNumber, membershipXML);
			return convertXMLObject(xobj);
		} catch (java.rmi.RemoteException re) {
			String err = re.getMessage();
			int errCode = 500;
			logger.error(re, re);
			logger.error("Error in smartlink  updateUserShellMembership:" + err);
			return convertXMLErrorObject(errCode, err);
		}
	}
	
}
