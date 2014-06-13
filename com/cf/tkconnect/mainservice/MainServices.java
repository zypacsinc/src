/**
 * MainServices.java
 *
 * Author Cyril Furtado
 * smartlink for Integration 
 *
 */

package com.cf.tkconnect.mainservice;
import com.cf.tkconnect.util.xml.FileObject;
import com.cf.tkconnect.util.xml.XMLFileObject;
import com.cf.tkconnect.util.xml.XMLObject;

public interface MainServices extends java.rmi.Remote {
    public  XMLObject createObject(String shortname, String authcode, String objectName, String objectXML, String sendresponse) throws java.rmi.RemoteException;
    public  XMLObject createObject(String shortname, String authcode, String objectName, String objectXML) throws java.rmi.RemoteException;
    public  XMLObject updateObject(String shortname, String authcode, String objectName, String objectXML, String sendresponse) throws java.rmi.RemoteException;
    public  XMLObject updateObject(String shortname, String authcode, String objectName, String objectXML) throws java.rmi.RemoteException;
    public  XMLObject createWBS(String shortname, String authcode, String projectNumber, String WBSXML) throws java.rmi.RemoteException;
    public  XMLObject createWBS(String shortname, String authcode, String projectNumber, String WBSXML, String sendresponse) throws java.rmi.RemoteException;
    public  XMLObject createBPRecord(String shortname, String authcode, String projectNumber, String BPName, String BPXML) throws java.rmi.RemoteException;
    public  XMLObject createBPRecord(String shortname, String authcode, String projectNumber, String BPName, String BPXML, String sendresponse) throws java.rmi.RemoteException;
    public  XMLObject addBPLineItem(String shortname, String authcode, String projectNumber, String BPName, String BPXML) throws java.rmi.RemoteException;
    public  XMLObject addBPLineItem(String shortname, String authcode, String projectNumber, String BPName, String itemXML, String sendresponse) throws java.rmi.RemoteException;
    public  XMLObject getBPRecord(String shortname, String authcode, String projectNumber, String BPName, String record_no) throws java.rmi.RemoteException;
    public  XMLObject getPlanningItem(String shortname, String authcode, String projectNumber, String BPName, String record_no, String planningitem) throws java.rmi.RemoteException;
    public  XMLObject getWBSStructure(String shortname, String authcode) throws java.rmi.RemoteException;
    public  XMLObject getBPList(String shortname, String authcode, String projectNumber, String BPName, String[] fieldnames, String filterCondition, String[] filterValues) throws java.rmi.RemoteException;
    public  XMLFileObject getCompleteBPRecord(String shortname, String authcode, String projectNumber, String BPName, String record_no) throws java.rmi.RemoteException;
	public  XMLObject getSOV(String shortname, String authcode, String projectNumber, String BPName, String record_no) throws java.rmi.RemoteException;
    public  XMLObject createProject(String shortname, String authcode, String cloneProjectNumber, String projectXML) throws java.rmi.RemoteException;
    public  XMLObject createProject(String shortname, String authcode, String cloneProjectNumber, String projectXML, String sendresponse) throws java.rmi.RemoteException;
    public  XMLObject updateBPRecord(String shortname, String authcode, String projectNumber, String BPName, String BPXML) throws java.rmi.RemoteException;
    public  XMLObject updateBPRecord(String shortname, String authcode, String projectNumber, String BPName, String BPXML, String sendresponse) throws java.rmi.RemoteException;
	public  XMLObject updateBPRecordV2(String shortname, String authcode, String projectNumber, String BPName, String BPXML, String options) throws java.rmi.RemoteException;
    public  XMLObject updateBPRecordV2(String shortname, String authcode, String projectNumber, String BPName, String BPXML, String options, String sendresponse) throws java.rmi.RemoteException;
    public  XMLObject getObjectList(String shortname, String authcode, String objectName, String[] fieldnames, String filterCondition, String[] filterValues) throws java.rmi.RemoteException;
    public  XMLObject getPendingResponse(String shortname, String authcode) throws java.rmi.RemoteException;
    public  XMLObject getIDResponse(String shortname, String authcode, String id) throws java.rmi.RemoteException;
    public  XMLObject getID(String shortname, String authcode) throws java.rmi.RemoteException;
    public  XMLObject updateColumnData(String shortname, String authcode, String projectNumber, String columnName, String columnXML) throws java.rmi.RemoteException;
    public  XMLObject getColumnData(String shortname, String authcode, String projectNumber, String columnName) throws java.rmi.RemoteException;
    public  XMLObject getUDRData(String shortname, String authcode, String projectNumber, String reportName ) throws java.rmi.RemoteException;
    public  XMLObject createAsset(String shortname, String authcode, String assetClassName, String copyFromAsset, String assetXML) throws java.rmi.RemoteException;
    public  XMLObject createAsset(String shortname, String authcode, String assetClassName, String copyFromAsset, String assetXML, String sendresponse) throws java.rmi.RemoteException;
    public  XMLObject getScheduleActivities(String shortname, String authcode, String projectNumber, String sheetName) throws java.rmi.RemoteException;
    public  XMLObject getScheduleActivities(String shortname, String authcode, String projectNumber, String sheetName, String sendresponse) throws java.rmi.RemoteException;
    public  XMLObject createScheduleActivities(String shortname, String authcode, String projectNumber, String sheetName, String sheetXML) throws java.rmi.RemoteException;
    public  XMLObject updateScheduleActivities(String shortname, String authcode, String projectNumber, String sheetName, String sheetXML) throws java.rmi.RemoteException;
    public XMLObject createCompleteBPRecord(String shortname, String authcode, String projectNumber, String BPName,  String BPXML, String iszipfile, FileObject[] files )throws java.rmi.RemoteException;
    public XMLObject addCompleteBPLineItem(String shortname, String authcode, String projectNumber, String BPName,  String BPXML, String iszipfile, FileObject[] files )throws java.rmi.RemoteException;
    public XMLObject updateCompleteBPRecord(String shortname, String authcode, String projectNumber, String BPName,  String BPXML, String iszipfile, FileObject[] files )throws java.rmi.RemoteException;
    public XMLObject createConfigurableModuleRecord(String shortname, String authcode,String projectNumber,  String CMCode, String ClassName, String copyFromRecord, String recordXML)throws java.rmi.RemoteException;
    public XMLObject updateConfigurableModuleRecord(String shortname, String authcode, String projectNumber, String CMCode, String ClassName, String recordXML)throws java.rmi.RemoteException;
    public XMLObject createShell(String shortname, String authcode, String copyFromShellTemplate, String shellXML)throws java.rmi.RemoteException;
    public XMLObject updateShell(String shortname, String authcode, String shellType, String shellXML)throws java.rmi.RemoteException;
    public XMLObject getShellList(String shortname, String authcode, String shellType, String filterCondition)throws java.rmi.RemoteException;
    public  XMLObject createScheduleActivitiesV2(String shortname, String authcode, String projectNumber, String sheetName, String sheetXML,String options) throws java.rmi.RemoteException;
    public  XMLObject updateScheduleActivitiesV2(String shortname, String authcode, String projectNumber, String sheetName, String sheetXML,String options) throws java.rmi.RemoteException;
    public XMLObject getExchangeRates(String shortname, String authcode) throws java.rmi.RemoteException;
    public XMLObject updateExchangeRates(String shortname, String authcode, String ratesXML) throws java.rmi.RemoteException;
    public  XMLObject getSpaceList(String shortname, String authcode, String projectNumber, String spaceType, String fieldnames, String filterCondition) throws java.rmi.RemoteException;
    public  XMLObject getLevelList(String shortname, String authcode, String projectNumber, String fieldnames, String filterCondition) throws java.rmi.RemoteException;
    public  XMLObject createSpace(String shortname, String authcode, String projectNumber, String spaceType, String SPACEXML) throws java.rmi.RemoteException;
    public  XMLObject createLevel(String shortname, String authcode, String projectNumber, String LEVELXML) throws java.rmi.RemoteException;
    public  XMLObject updateSpace(String shortname, String authcode, String projectNumber, String spaceType, String SPACEXML) throws java.rmi.RemoteException;
    public  XMLObject updateLevel(String shortname, String authcode, String projectNumber, String LEVELXML) throws java.rmi.RemoteException;
    public XMLObject createUser(String shortname, String authcode, String copyFromUserPreferenceTemplate, String userXML)throws java.rmi.RemoteException;
    public XMLObject updateUser(String shortname, String authcode, String userXML)throws java.rmi.RemoteException;
    public XMLObject getUserList(String shortname, String authcode, String filterCondition)throws java.rmi.RemoteException;
    public  XMLObject createUpdateRole(String shortname, String authcode, String rolexml) throws java.rmi.RemoteException;
    public  XMLObject createUpdateResource(String shortname, String authcode, String resourcexml) throws java.rmi.RemoteException;
    public  XMLObject getRoleList(String shortname, String authcode, String fileteroptions) throws java.rmi.RemoteException;
    public  XMLObject getResourceList(String shortname, String authcode, String fileteroptions) throws java.rmi.RemoteException;
    public  XMLObject createScheduleActivitiesFromFileV2(String shortname, String authcode, String projectNumber, String sheetName, String options, String iszipfile, FileObject files) throws java.rmi.RemoteException;
    public  XMLObject updateScheduleActivitiesFromFileV2(String shortname, String authcode, String projectNumber, String sheetName, String options, String iszipfile, FileObject files) throws java.rmi.RemoteException;
	public XMLObject updateUserGroupMembership(String shortname, String authcode, String membershipXML)throws java.rmi.RemoteException;
	public XMLObject updateUserShellMembership(String shortname, String authcode, String projectNumber, String membershipXML)throws java.rmi.RemoteException;
	public  XMLObject  getProjectShellList ( String shortname , String authcode , String options) throws java.rmi.RemoteException;
	public  XMLObject  getScheduleSheetDataMaps ( String shortname , String authcode , String projectnumber, String sheetName, String options) throws java.rmi.RemoteException;
	public  XMLObject  getScheduleSheetList ( String shortname , String authcode ,  String projectnumber, String options) throws java.rmi.RemoteException;
	public  XMLObject  getWBSCodes ( String shortname , String authcode ,  String projectnumber, String options) throws java.rmi.RemoteException;
	public  XMLObject  getScheduleDataMapsDetails ( String shortname , String authcode , String projectnumber, String sheetName, String datamap, String options) throws java.rmi.RemoteException;
	public  XMLObject  updateScheduleDataMapsDetails ( String shortname , String authcode , String projectnumber, String sheetName, String datamap, String dataXML, String options) throws java.rmi.RemoteException;
	 

}
