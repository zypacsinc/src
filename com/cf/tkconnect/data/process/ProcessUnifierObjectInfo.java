package com.cf.tkconnect.data.process;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.lang3.StringEscapeUtils;
import static org.apache.commons.lang3.math.NumberUtils.toDouble;
import static org.apache.commons.lang3.math.NumberUtils.toInt;

import com.cf.tkconnect.csv.ImportDataCSV;
import com.cf.tkconnect.data.form.DataObject;
import com.cf.tkconnect.data.process.ReadXMLData;
import com.cf.tkconnect.data.process.ProcessUnifierObjectInfo;
import com.cf.tkconnect.database.SqlUtils;
import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.util.WSUtil;


public class ProcessUnifierObjectInfo {
	
	static Log logger = LogSource.getInstance(ProcessUnifierObjectInfo.class);
	String xmldata; // complete xml with list_wrapper
//	int company_id;
	String filename;
	boolean isfile = false;

	Map<String,String> bplistmap = new HashMap<String,String>();
	Map<String,String> ddmap = new HashMap<String,String>();
	Map<String,String> demap = new HashMap<String,String>();
	List<Map<String,String>> ddlist;
	List<Map<String,String>> delist;
	private final String  DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
	
	public ProcessUnifierObjectInfo(  String xmldata ){
		this.xmldata = xmldata;
		isfile = false;
	}

	public ProcessUnifierObjectInfo(  String filename, boolean isfile){
		this.filename = filename;
		isfile = true;
	}
	public void setXMLData(String xmldata){
		this.xmldata = xmldata;
		
	}

	private void processDEandDDFromDBandXML() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SqlUtils.getConnection();
			System.out.println("Connection ------");
			ps = conn.prepareStatement("select * from data_definitions order by data_name");
			rs = ps.executeQuery();
			ddmap = DataObject.getKeyMap(rs, "data_name","data_name");
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			ps = conn.prepareStatement("select * from data_elements  order by de_name");
			rs = ps.executeQuery();
			demap = DataObject.getKeyMap(rs, "de_name","de_name");
			
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		// now check if values have changed
	}
	
	public String[] processDDDEXMLData() throws Exception{
		if(WSUtil.isBlankOrNull(xmldata))
			return null;
		processDEandDDFromDBandXML();
		ReadXMLData dr;
		if(isfile)
			dr = new ReadXMLData(filename,true);
		else
			dr = new ReadXMLData(xmldata);
		dr.parse();
		String ddstr = dr.getDD();
		String destr = dr.getDE();
		if(logger.isDebugEnabled())
			logger.debug("processDDDEXMLData  dd  "+ddstr);
		if(logger.isDebugEnabled())
			logger.debug("processDDDEXMLData  de  "+destr);
		ImportDataCSV imp = new ImportDataCSV();
		ddlist = imp.processDDImport(StringEscapeUtils.unescapeXml(ddstr),ddmap);
		delist = imp.processDEImport(StringEscapeUtils.unescapeXml(destr),demap);
		// insert this
		if(logger.isDebugEnabled())
			logger.debug("processDDDEXMLData  dedd  "+ddlist);
		String[] str = {"",""}; 
		str[0] = insertDDList(ddlist);
		str[1] = insertDEList(delist);
		return str;
	}

	public	List<Map<String,String>> getDDList(){
		return ddlist;
	}
	
	public	List<Map<String,String>> getDEList(){
		return delist;
	}

	private void processBPListfromDBAndXML() throws Exception {
		bplistmap = getDataList("select * from bp_log  order by bp_name","bp_name");
		
	}
	
	private Map<String,String> getDataList(String sql, String key) throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Map<String,String> datamap = new HashMap<String,String>();
		try {
			conn = SqlUtils.getConnection();
			logger.debug("Connection ------getDataList");
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			
			datamap = DataObject.getKeyMap(rs, key,key);
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return datamap;
	}

	private void setStudioListfromDBAndXML() throws Exception {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SqlUtils.getConnection();
//			System.out.println("Connection ------");
			ps = conn.prepareStatement("select studio_prefix,studio_version from studio_log order by studio_name");
			rs = ps.executeQuery();
			bplistmap = DataObject.getKeyMap(rs, "studio_prefix","studio_version");
			
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		// now check if values have changed
		
	}
	
	public String processProjectXMLData() throws Exception{
		if(WSUtil.isBlankOrNull(xmldata))
			return "";
		 Map<String,String> datamap =getDataList("select * from project_log  order by projectnumber","projectnumber");
		ReadXMLData dr;
		if(isfile)
			dr = new ReadXMLData(filename,true);
		else
			dr = new ReadXMLData(xmldata);
		dr.setCheckMap(datamap, "projectnumber");
		dr.parse();
		List<Map<String,String>> newlist = dr.getNewList() ;
		logger.debug("this is a new list for projects "+newlist);
		// insert this
		String newstr = insertProjectList(newlist);
		return newstr;
	}

	public String[] processStudioXMLData() throws Exception{
		if(WSUtil.isBlankOrNull(xmldata))
			return null;
		setStudioListfromDBAndXML();
		ReadXMLData dr;
		if(isfile)
			dr = new ReadXMLData(filename,true);
		else
			dr = new ReadXMLData(xmldata);
		dr.setCheckMap(bplistmap, "studio_prefix","studio_version");
		dr.parse();
		List<Map<String,String>> newlist = dr.getNewList() ;
		logger.debug("this is a new list for studio "+newlist);
		List<Map<String,String>> updatelist = dr.getUpdateList() ;
		// insert this
		String[] str = {"",""}; 
		str[0] = insertStudioList(newlist);
		str[1] = updateStudioList(updatelist);
		return str;
	}

	public String processBPXMLData() throws Exception{
		if(WSUtil.isBlankOrNull(xmldata))
			return "";
		processBPListfromDBAndXML();
		ReadXMLData dr;
		if(isfile)
			dr = new ReadXMLData(filename,true);
		else
			dr = new ReadXMLData(xmldata);
		dr.setCheckMap(bplistmap, "bp_name");
		dr.parse();
		List<Map<String,String>> newlist = dr.getNewList() ;
		logger.debug("this is a new list for bp "+newlist);
		// insert this
		String newstr = insertBPList(newlist);
		return newstr;
	}
	public String processWBSXMLData(String projectnumber) throws Exception{
		if(WSUtil.isBlankOrNull(xmldata))
			return "";
		processBPListfromDBAndXML();
		ReadXMLData dr;
		if(isfile)
			dr = new ReadXMLData(filename,true);
		else
			dr = new ReadXMLData(xmldata);
		//dr.setCheckMap(bplistmap, "bp_name");
		dr.parse();
		List<Map<String,String>> newlist = dr.getNewList() ;
		logger.debug("this is a new list for WBS "+newlist);
		// insert this
		String newstr = insertWBSList(newlist,projectnumber);
		return newstr;
	}

	private String insertWBSList(List<Map<String,String>> newlist, String projectnumber) throws Exception {
		StringBuilder buf = new StringBuilder();
		if(newlist == null || newlist.size() == 0)
			return buf.toString();
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SqlUtils.getConnection();
			ps = conn.prepareStatement("insert into wbs_codes  (status,wbscode,wbsitem,costtype,wbscodeid,parentid,orderid,indentlevel,projectnumber) " +
					"values (?,?,?,?,?,?,?,?,?) ");
			for(Map<String,String> map : newlist){
				
				ps.setString(1, map.get("status"));
				ps.setString(2, map.get("wbscode"));
				ps.setString(3, map.get("wbsitem"));
				ps.setString(4, map.get("costtype"));
				ps.setInt(5, toInt(map.get("wbscodeid")));
				ps.setInt(6, toInt(map.get("parentid")));
				ps.setInt(7, toInt(map.get("orderid")));
				ps.setInt(8, toInt(map.get("indentlevel")));
				ps.setString(9,projectnumber);
				ps.addBatch();
				
			}
			int[] ret = ps.executeBatch();
			for(int r =0; r <  ret.length; r++)
				logger.debug("insert insertWBSList "+r+"  "+ret[r]);
			
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return buf.toString();
	}
	private String insertStudioList(List<Map<String,String>> newlist) throws Exception {
		StringBuilder buf = new StringBuilder();
		if(newlist == null || newlist.size() == 0)
			return buf.toString();
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SqlUtils.getConnection();
			ps = conn.prepareStatement("insert into studio_log  (studio_name,studio_guid,studio_prefix,studio_source," +
					"studio_type,studio_level,studio_version,studio_status,published_date,lastmodified) " +
					"values (?,?,?,?,?,?,?,?,?,?) ");
			for(Map<String,String> map : newlist){
				Date dt = WSUtil.parseDate( map.get("published_date"), DATE_FORMAT);
				Timestamp ts = (dt==null?null: new Timestamp(dt.getTime()));
				ps.setString(1, map.get("studio_name"));
				ps.setString(2, map.get("studio_guid"));
				ps.setString(3, map.get("studio_prefix"));
				ps.setString(4, map.get("studio_source"));
				ps.setString(5, map.get("studio_type"));
				ps.setInt(6, toInt(map.get("studio_level")));
				ps.setInt(7, toInt(map.get("studio_version")));
				ps.setInt(8,toInt( map.get("studio_status")));
				ps.setTimestamp(9, ts) ;
				ps.setTimestamp(10, new Timestamp(System.currentTimeMillis()));
				ps.addBatch();
				if(buf.length() > 0)
					buf.append(",");
				buf.append(map.get("studio_prefix"));
			}
			int[] ret = ps.executeBatch();
			for(int r =0; r <  ret.length; r++)
				logger.debug("insert studio "+r+"  "+ret[r]);
			
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return buf.toString();
	}

	private String insertBPList(List<Map<String,String>> newlist) throws Exception {
		StringBuilder buf = new StringBuilder();
		if(newlist == null || newlist.size() == 0)
			return buf.toString();
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SqlUtils.getConnection();
			System.out.println("Connection ------");
			ps = conn.prepareStatement("insert into bp_log  (bp_name,bp_type,studio_source," +
					"studio_type,company_bp,no_workflow,single_record,seq_format,lastmodified) " +
					"values (?,?,?,?,?,?,?,?,?) ");
			for(Map<String,String> map : newlist){
				ps.setString(1, map.get("bp_name"));
				ps.setString(2, map.get("bp_type"));
				ps.setString(3, map.get("studio_source"));
				ps.setString(4, map.get("studio_type"));
				ps.setInt(5, toInt(map.get("company_bp")));
				ps.setInt(6, toInt(map.get("no_workflow")));
				ps.setInt(7,toInt( map.get("single_record")));
				ps.setString(8, map.get("seq_format"));
				ps.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
				ps.addBatch();
				if(buf.length() > 0)
					buf.append(",");
				buf.append(map.get("studio_prefix"));
			}
			int[] ret = ps.executeBatch();
			for(int r =0; r <  ret.length; r++)
				logger.debug("insert bp log "+r+"  "+ret[r]);
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return buf.toString();
	}
	
	private String insertProjectList(List<Map<String,String>> newlist) throws Exception {
		if(newlist == null || newlist.size() == 0)
			return "[]";
		Connection conn = null;
		PreparedStatement ps = null;
		StringBuilder buf = new StringBuilder("[");
		try {
			conn = SqlUtils.getConnection();
			//System.out.println("Connection ------");
			ps = conn.prepareStatement("insert into project_log  (projectnumber,projectname,projectphase," +
					"status,projecttype,projectemail,lastmodified) " +
					"values (?,?,?,?,?,?,?) ");
			for(Map<String,String> map : newlist){
				ps.setString(1, map.get("projectnumber"));
				ps.setString(2, map.get("projectname"));
				ps.setString(3, map.get("projectphase"));
				ps.setInt(4, toInt(map.get("status")));
				ps.setInt(5, toInt(map.get("projecttype")));
				ps.setString(6, map.get("project_email"));
				ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
				ps.addBatch();
				if(buf.length() > 0)
					buf.append(",");
				buf.append("{\n");
				buf.append("\"projectnumber\":\"").append(map.get("projectnumber")).append("\",");
				buf.append("\"projectname\":\"").append(WSUtil.jsfilter2(map.get("projectname"))).append("\",");
				buf.append("\"projectphase\":\"").append(map.get("projectphase")).append("\",");
				buf.append("\"uid\":\"").append(map.get("uid")).append("\",");
				buf.append("\"projecttype\":\"").append(map.get("projecttype")).append("\"");
				buf.append("}\n");
				
			}
			int[] ret = ps.executeBatch();
			for(int r =0; r <  ret.length; r++)
				logger.debug("insert projectnumber log "+r+"  "+ret[r]);
		}catch(Exception e){
			logger.error(e,e);
			return "[]";
		} finally {
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		buf.append("]");
		return buf.toString();
	}

	private String updateStudioList(List<Map<String,String>> updatelist) throws Exception {
		StringBuilder buf = new StringBuilder();
		if(updatelist == null || updatelist.size() == 0)
			return buf.toString();
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SqlUtils.getConnection();
			System.out.println("Connection ------");
			ps = conn.prepareStatement("update studio_log set studio_version = ?,studio_status =?, published_date= ? lastmodifieddate= ? where  studio_prefix = ? " +
					"values (?,?,?,?,?,?,?,?,?,?,?) ");
			for(Map<String,String> map : updatelist){
				ps.setInt(1, toInt(map.get("studio_version")));
				ps.setInt(2,toInt( map.get("studio_status")));
				ps.setTimestamp(3, (Timestamp)WSUtil.parseDate( map.get("published_date"), DATE_FORMAT)) ;
				ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
				ps.setString(5, map.get("studio_prefix"));
				ps.addBatch();
				if(buf.length() > 0)
					buf.append(",");
				buf.append(map.get("studio_prefix"));
			}
			int[] ret = ps.executeBatch();
			for(int r =0; r <  ret.length; r++)
				logger.debug("update studio "+r+"  "+ret[r]);
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return buf.toString();
	}

	private String insertDDList(List<Map<String,String>> newlist) throws Exception {
		StringBuilder buf = new StringBuilder();
		if(newlist == null || newlist.size() == 0)
			return buf.toString();
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SqlUtils.getConnection();
			logger.debug("Connection ------ data_definitions");
			ps = conn.prepareStatement("insert into data_definitions  (data_name,data_type,input_type," +
					"data_size,category,used,lastmodified) " +
					"values (?,?,?,?,?,?,?) ");
			for(Map<String,String> map : newlist){
				logger.debug("insert data_definitions   "+map);
				ps.setString(1, map.get("Name"));
				ps.setString(2, map.get("Data Type"));
				ps.setString(3, map.get("Input Type"));
				ps.setInt(4, toInt(map.get("Data Size")));
				ps.setString(5, map.get("Category"));
				ps.setString(6,map.get("Ysed"));
				ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
				ps.addBatch();
				if(buf.length() > 0)
					buf.append(",");
				buf.append(map.get("data_name"));
			}
			
					
			int[] ret = ps.executeBatch();
			for(int r =0; r <  ret.length; r++)
				logger.debug("insert data_definitions "+r+"  "+ret[r]);
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return buf.toString();
	}

	private String insertDEList(List<Map<String,String>> newlist) throws Exception {
		StringBuilder buf = new StringBuilder();
		if(newlist == null || newlist.size() == 0)
			return buf.toString();
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = SqlUtils.getConnection();
			logger.debug("Connection ------ data_elements "+newlist.size());
			ps = conn.prepareStatement("insert into data_elements  (de_name,de_label,de_data_def,lastmodified)" +
					"values (?,?,?,?) ");
			for(Map<String,String> map : newlist){
				ps.setString(1, map.get("Data Element"));
				ps.setString(2, map.get("Form Label"));
				ps.setString(3, map.get("Data Definition"));
				ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
				ps.addBatch();
				if(buf.length() > 0)
					buf.append(",");
				buf.append(map.get("Data Element"));
			}
			int[] ret = ps.executeBatch();
			for(int r =0; r <  ret.length; r++)
				logger.debug("insert Data Element "+r+"  "+ret[r]);
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return buf.toString();
	}



}
