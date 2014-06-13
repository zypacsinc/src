package com.cf.tkconnect.data;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.cf.tkconnect.data.form.WBSCodes;
import com.cf.tkconnect.data.process.ProcessUnifierObjectInfo;
import com.cf.tkconnect.data.process.UnifierXMLServiceData;
import com.cf.tkconnect.database.SqlUtils;
import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.process.ProcessSync;
import com.cf.tkconnect.process.ResponseObject;
import com.cf.tkconnect.process.RunWebServices;
import com.cf.tkconnect.util.DataUtils;
import com.cf.tkconnect.util.FileUtils;
import com.cf.tkconnect.util.WSConstants;
import com.cf.tkconnect.util.WSUtil;
import com.cf.tkconnect.wsdl.ParseWSDL;


public class TKUnifierMetaData {
	static Log logger = LogSource.getInstance(TKUnifierMetaData.class);
	
	
	int user_id =1;
	int loopcount = 0;
	
	public TKUnifierMetaData( int user_id){
		this.user_id = user_id;
	}
	
	public TKUnifierMetaData(){
	}
	
	public String getProjectList() throws Exception{
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuilder buf = new StringBuilder("[");
		try {
			conn = SqlUtils.getConnection();
			String sql = "select p.*,COALESCE( u.user_id , 999 ) uid from project_log p  left outer join user_fav_projects u on (u.projectnumber = p.projectnumber and u.user_id = ?) ";
			sql +="where p.status = 1 and p.projecttype != 500 order by uid";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, user_id);
			rs = ps.executeQuery();
			int count = 0;
			while(rs.next()){
				if(count > 0)
					buf.append(",");
				buf.append("{");
				String pp = rs.getString("projectphase");
				if(pp == null || pp.equalsIgnoreCase("NULL"))
					pp = "";
				buf.append("\"projectnumber\":\"").append(rs.getString("projectnumber")).append("\",");
				buf.append("\"projectname\":\"").append(WSUtil.jsfilter2(rs.getString("projectname"))).append("\",");
				buf.append("\"projectphase\":\"").append(pp).append("\",");
				buf.append("\"uid\":\"").append(rs.getString("uid")).append("\",");
				buf.append("\"projecttype\":\"").append(rs.getInt("projecttype")).append("\"");
				buf.append("}\n");
				count++;
			}
			if(count == 0){
				return syncProjectData();
			}
			
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		buf.append("]");
		return buf.toString();
		
	}

	
public List<String> getProjectNumberList() throws Exception{
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<String> list = new ArrayList<String>();
		try {
			conn = SqlUtils.getConnection();
			String sql = "select projectnumber from project_log where status = 1 and projecttype != 500 order by projectnumber";
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while(rs.next()){
				list.add(rs.getString("projectnumber"));
			}
			
			
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return list;
		
	}
	
	public String getBPList() throws Exception{
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuilder buf = new StringBuilder("[");
		try {
			conn = SqlUtils.getConnection();
//			SELECT s.studio_prefix, a.file_name
			String sql = "select COALESCE(u.studio_prefix,'zz') fav, s.*,a.file_name,a.file_path from studio_bp_view s LEFT OUTER JOIN attribute_templates a ON ( s.studio_prefix = a.attr_prefix   " +
					" ) left outer join user_fav_bps u on ( u.studio_prefix = s.studio_prefix and u.user_id= ?)"+
					" where (s.studio_source ='cost' or s.studio_source ='database' or s.studio_source ='simple' or s.studio_source ='lineitem' or s.studio_source ='document' or s.studio_source ='space') order by fav,studio_name";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, user_id);
			rs = ps.executeQuery();
			int count = 0;
			while(rs.next()){
				SimpleDateFormat sdf = new SimpleDateFormat(WSConstants.TIME_FORMAT);
				if(count > 0)
					buf.append(",");
				String file_name = rs.getString("file_name");
				if(file_name == null || file_name.equalsIgnoreCase("NULL"))
					file_name = "";
				buf.append("{");
				buf.append("\"fav\":\"").append(rs.getString("fav")).append("\",");
				buf.append("\"studio_prefix\":\"").append(rs.getString("studio_prefix")).append("\",");
				buf.append("\"studio_name\":\"").append(WSUtil.jsfilter2(rs.getString("studio_name"))).append("\",");
				buf.append("\"studio_source\":\"").append(rs.getString("studio_source")).append("\",");
				buf.append("\"studio_type\":\"").append(rs.getString("studio_type")).append("\",");
				buf.append("\"studio_version\":\"").append(rs.getInt("studio_version")).append("\",");
				buf.append("\"published_date\":\"").append(sdf.format(rs.getTimestamp("published_date"))).append("\",");
				buf.append("\"company_bp\":\"").append(rs.getInt("company_bp")).append("\",");
				buf.append("\"no_workflow\":\"").append(rs.getInt("no_workflow")).append("\",");
				buf.append("\"lastmodified\":\"").append(sdf.format(rs.getTimestamp("lastmodified"))).append("\",");
				buf.append("\"file_name\":\"").append(WSUtil.jsfilter2(file_name)).append("\"");
				buf.append("}\n");
				count++;
			}
			if(count == 0)
				return syncBPData();
			
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			buf.append("]");
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return buf.toString();
		
	}

	public String getShellTypes() throws Exception{
		//TODO need to configure this
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuilder buf = new StringBuilder("[");
		try {
			conn = SqlUtils.getConnection();
//			SELECT s.studio_prefix, a.file_name
			ps = conn.prepareStatement("select COALESCE (u.studio_prefix, 'zz')   fav, s.*,a.file_name,a.file_path from studio_bp_view s LEFT OUTER JOIN attribute_templates a ON ( s.studio_prefix = a.attr_prefix   " +
					" ) left outer join user_fav_bps u on ( u.studio_prefix = s.studio_prefix and u.user_id= ?)"+
					" where  (s.studio_source ='shell' ) order by fav,studio_name");
			ps.setInt(1, user_id);
			rs = ps.executeQuery();
			int count = 0;
			while(rs.next()){
				SimpleDateFormat sdf = new SimpleDateFormat(WSConstants.TIME_FORMAT);
				if(count > 0)
					buf.append(",");
				String file_name = rs.getString("file_name");
				if(file_name == null || file_name.equalsIgnoreCase("NULL"))
					file_name = "";
				buf.append("{");
				buf.append("\"fav\":\"").append(rs.getString("fav")).append("\",");
				buf.append("\"studio_prefix\":\"").append(rs.getString("studio_prefix")).append("\",");
				buf.append("\"studio_name\":\"").append(WSUtil.jsfilter2(rs.getString("studio_name"))).append("\",");
				buf.append("\"studio_source\":\"").append(rs.getString("studio_source")).append("\",");
				buf.append("\"studio_type\":\"").append(rs.getString("studio_type")).append("\",");
				buf.append("\"studio_version\":\"").append(rs.getInt("studio_version")).append("\",");
				buf.append("\"published_date\":\"").append(sdf.format(rs.getTimestamp("published_date"))).append("\",");
				buf.append("\"company_bp\":\"").append(rs.getInt("company_bp")).append("\",");
				buf.append("\"no_workflow\":\"").append(rs.getInt("no_workflow")).append("\",");
				buf.append("\"lastmodified\":\"").append(sdf.format(rs.getTimestamp("lastmodified"))).append("\",");
				buf.append("\"file_name\":\"").append(WSUtil.jsfilter2(file_name)).append("\"");
				buf.append("}\n");
				count++;
			}
			if(count == 0)
				syncBPData();
			
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			buf.append("]");
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return buf.toString();
		
	}

	

	public synchronized String getWebServicesList(Map<String,List<Map<String,String>>> unifierMethodMap, boolean skiplist) throws Exception{
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuilder buf = new StringBuilder("");
		try {
			conn = SqlUtils.getConnection();
//			SELECT s.studio_prefix, a.file_name
			ps = conn.prepareStatement("select * from services where Lower(service_name) = 'unifierwebservices' or Lower(service_name) = 'mainservice' ");
			rs = ps.executeQuery();
			int count = 0;
			if(!skiplist)
				buf.append("[");
			while(rs.next()){
				String file_name = rs.getString("file_name");
				if(file_name == null || file_name.equalsIgnoreCase("NULL")){
					continue;
				}	
				String methodname = rs.getString("method_name");
				ParseWSDL pw = new ParseWSDL(file_name,methodname);
				pw.parse();
				String result = pw.getParamsInJson();
				if(result== null || result.trim().length() == 0)
					continue;
				if(count > 0)
					buf.append(",");
				
				logger.debug(" The getWebServicesList  "+methodname+" "+file_name+" re** "+result);
				buf.append(result);
				if(unifierMethodMap != null && !unifierMethodMap.containsKey(methodname)){
					unifierMethodMap.put(methodname, pw.getParamsInList());
				}
				count++;
			}
			
			if(!skiplist)
				buf.append("]\n");
			logger.debug(" The getWebServicesList final ** "+buf);
			
		}catch(Exception e){
			logger.error(e,e);
			return "[]";
		} finally {
			
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return buf.toString();
		
	}

	
	public String getDDList() throws Exception{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuilder buf = new StringBuilder("[");
		try {
			conn = SqlUtils.getConnection();
			ps = conn.prepareStatement("select * from data_definitions order by data_name ");
			rs = ps.executeQuery();
			int count = 0;
			while(rs.next()){
				if(count > 0)
					buf.append(",");
				buf.append("{");
				buf.append("\"data_type\":\"").append(rs.getString("data_type")).append("\",");
				buf.append("\"data_name\":\"").append(WSUtil.jsfilter2(rs.getString("data_name"))).append("\",");
				buf.append("\"input_type\":\"").append(rs.getString("input_type")).append("\",");
				buf.append("\"data_size\":\"").append(rs.getInt("data_size")).append("\"");
				buf.append("}\n");
				count++;
			}
			if(count == 0)
				return syncDDData();
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			buf.append("]");
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return buf.toString();
	}

	public String getDEList() throws Exception{
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuilder buf = new StringBuilder("[");
		try {
			conn = SqlUtils.getConnection();
//			System.out.println("Connection ------");
			ps = conn.prepareStatement("select * from data_elements order by de_name ");
			rs = ps.executeQuery();
			int count = 0;
			while(rs.next()){
				if(count > 0)
					buf.append(",");
				buf.append("{");
				buf.append("\"de_name\":\"").append(rs.getString("de_name")).append("\",");
				buf.append("\"de_label\":\"").append(WSUtil.jsfilter2(rs.getString("de_label"))).append("\",");
				buf.append("\"de_data_def\":\"").append(rs.getString("de_data_def")).append("\"");
			
				buf.append("}\n");
				count++;
			}
			if(count == 0)
				return syncDEData();
			
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			buf.append("]");
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return buf.toString();
		
	}

	public String getBatchServiceList() throws Exception{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuilder buf = new StringBuilder("[");
		try {
			conn = SqlUtils.getConnection();
			ps = conn.prepareStatement("select * from batch_services_log where user_id = ? order by start_time desc");
			ps.setInt(1, user_id);
			rs = ps.executeQuery();
			int count = 0;
			while(rs.next()){
				if(count > 0)
					buf.append(",");
				SimpleDateFormat sdf = new SimpleDateFormat(WSConstants.TIME_FORMAT);
				
				buf.append("{");
				buf.append("\"batch_id\":\"").append(rs.getLong("batch_id")).append("\",");
				buf.append("\"batch_name\":\"").append(WSUtil.jsfilter2(rs.getString("batch_name"))).append("\",");
				buf.append("\"start_time\":\"").append(sdf.format(rs.getTimestamp("start_time"))).append("\",");
				buf.append("\"end_time\":\"").append(sdf.format(rs.getTimestamp("end_time"))).append("\"");
				buf.append("\"thread_count\":\"").append(rs.getInt("thread_count")).append("\",");
				buf.append("\"service_count\":\"").append(rs.getInt("service_count")).append("\"");
				buf.append("}\n");
				count++;
			}
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			buf.append("]");
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return buf.toString();
	}

	public String getRunServiceList(String query) throws Exception{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		if(logger.isDebugEnabled())
			logger.debug("getRunServiceList   query  :: "+query );
		StringBuilder qbuf = new StringBuilder("");
		if(query != null && query.trim().length() > 0){
			if(!checkQuery(query) )
				qbuf.append("");
			else if(query.startsWith("method_name") || query.startsWith("service_name") ||query.startsWith("response_code")  ||query.indexOf("time") >= 0)
				qbuf.append(" and ").append(query.trim());
				
		}
		String sql = "select s.*,f.save_name from services_audit_log  s left outer join saved_files f on s.saved_file_id = f.file_id where 1 =1  "+qbuf.toString()+" order by time desc "	;
		logger.debug("getRunServiceList    :: "+sql );
		StringBuilder buf = new StringBuilder("[");
		try {
			conn = SqlUtils.getConnection();
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			int count = 0;
			while(rs.next()){
				if(count > 0)
					buf.append(",");
				SimpleDateFormat sdf = new SimpleDateFormat(WSConstants.TIME_FORMAT);
				
				buf.append("{");
				buf.append("\"saved_file_id\":\"").append(rs.getLong("saved_file_id")).append("\",");
				buf.append("\"prefix\":\"").append(rs.getString("prefix")).append("\",");
				buf.append("\"save_name\":\"").append(WSUtil.jsfilter2(rs.getString("save_name"))).append("\",");
				buf.append("\"external_file_name\":\"").append(WSUtil.jsfilter2(WSUtil.getExcelFileName(rs.getString("external_file_name")))).append("\",");
				buf.append("\"errors\":\"").append(WSUtil.jsfilter2(rs.getString("errors"))).append("\",");
				buf.append("\"time\":\"").append(sdf.format(rs.getTimestamp("time"))).append("\",");
				buf.append("\"start_time\":\"").append(rs.getTimestamp("start_time") != null?sdf.format(rs.getTimestamp("start_time")):"").append("\",");
				buf.append("\"input_file_name\":\"").append(WSUtil.jsfilter2(rs.getString("input_file_name"))).append("\",");
				buf.append("\"output_file_name\":\"").append(WSUtil.jsfilter2(rs.getString("output_file_name"))).append("\",");
				buf.append("\"response_code\":\"").append(rs.getInt("response_code")).append("\",");
				buf.append("\"id\":\"").append(rs.getLong("id")).append("\"");
				buf.append("}\n");
				count++;
			}
			buf.append("]");
		}catch(Exception e){
			logger.error(e,e);
			return "[]";
		} finally {
			
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return buf.toString();
	}
	
	

	public String getRunServiceDetails(long id) throws Exception{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		String sql = "select * from services_audit_log where  id = ?"	;
		logger.debug("getRunServiceDetails    :: "+sql );
		StringBuilder buf = new StringBuilder("");
		try {
			conn = SqlUtils.getConnection();
			ps = conn.prepareStatement(sql);
		
			ps.setLong(1, id);
			rs = ps.executeQuery();
			buf.append("{");
			if(rs.next()){
				SimpleDateFormat sdf = new SimpleDateFormat(WSConstants.TIME_FORMAT);
				
				buf.append("\"saved_service_id\":\"").append(rs.getLong("saved_service_id")).append("\",");
				buf.append("\"method_name\":\"").append(rs.getString("method_name")).append("\",");
				buf.append("\"service_name\":\"").append(rs.getString("service_name")).append("\",");
				buf.append("\"errors\":\"").append(WSUtil.jsfilter2(rs.getString("errors"))).append("\",");
				buf.append("\"time\":\"").append(sdf.format(rs.getTimestamp("time"))).append("\",");
				buf.append("\"start_time\":\"").append(rs.getTimestamp("start_time") != null?sdf.format(rs.getTimestamp("start_time")):"").append("\",");
				buf.append("\"end_time\":\"").append(rs.getTimestamp("end_time") != null?sdf.format(rs.getTimestamp("end_time")):"").append("\",");
				buf.append("\"response_code\":\"").append(rs.getInt("response_code")).append("\",");
				buf.append("\"id\":\"").append(rs.getLong("id")).append("\",");
				// get the file
				String filename = rs.getString("file_path")+File.separator+rs.getString("input_file_name");
				String outfilename = rs.getString("file_path")+File.separator+rs.getString("output_file_name");
				buf.append("\"service_details\":").append(getServiceFileDetails(filename));
				int rc = rs.getInt("response_code");
				if(rc == 200)
					buf.append(",\"output_details\":").append(FileUtils.getFileContents(outfilename));
			}
			buf.append("}\n");
			logger.debug("getRunServiceDetails data  --------"+buf);
		}catch(Exception e){
			logger.error(e,e);
			return "{\"errors\":\"errors occured\"}";
		} finally {
			
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return buf.toString();
	}

	public String getSysInfo( String data_type) throws Exception{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuilder buf = new StringBuilder("");
		try {
			conn = SqlUtils.getConnection();
			ps = conn.prepareStatement("select max(lastmodified) lastmodified from sys_info  where  data_type=?");
			ps.setString(1, data_type);
			rs = ps.executeQuery();
			while(rs.next()){
				if( rs.getTimestamp("lastmodified") == null){
					buf.append("{}");
					break;
				}	
				SimpleDateFormat sdf = new SimpleDateFormat(WSConstants.TIME_FORMAT);
				buf.append("{");
				buf.append("\"lastmodified\":\"").append(sdf.format( rs.getTimestamp("lastmodified") )).append("\" ");
				buf.append("}\n");
			}
		}catch(Exception e){
			logger.error(e,e);
			buf.append("{}");
		} finally {
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return buf.toString();
	}
	protected synchronized String syncBPData() throws Exception{
		logger.debug("Syncing data again -------- syncBPData :");
		ProcessSync ps = new ProcessSync();
		ps.processStudioInfo();	
		loopcount++;
		if(loopcount >1)
			return "[]";
		return getBPList();
		
		
	}
	protected synchronized String syncDEData() throws Exception{
		logger.debug("Syncing DE data again -------- ");
		ProcessSync ps = new ProcessSync();
		ps.proceeStudioDEInfo() ;
		loopcount++;
		if(loopcount >1)
			return "[]";
		return getDEList();
		
	}
	protected synchronized String syncDDData() throws Exception{
		logger.debug("Syncing DD data again -------- ");
		ProcessSync ps = new ProcessSync();
		ps.proceeStudioDEInfo() ;
		loopcount++;
		if(loopcount >1)
			return "[]";
		return getDDList();
		
	}
	protected synchronized String syncProjectData() throws Exception{
		logger.debug("Syncing project data again -------- for :");
		ProcessSync psy = new ProcessSync();
		psy.synchProjects();
		loopcount++;
		if(loopcount >1)
			return "[]";
		return getProjectList();
		
	}
	public static String getServiceFileDetails(String filename) throws Exception{
		
		File f = new File(filename);
		if(f.exists()){
			UnifierXMLServiceData sx = new UnifierXMLServiceData(filename, true);
			return sx.parse();
		}
		return "{}";
	}
	private boolean checkQuery(String query){
		if(query == null || query.trim().length() == 0)
			return false;
		if(query.toLowerCase().indexOf("select")>= 0 || query.toLowerCase().indexOf("union")>= 0 || query.toLowerCase().indexOf("group") >=0 )
			return false;
		String str = query.substring(0,2);
		return StringUtils.isAlpha(str);

	}
	
	public String getSavedFiles(String query) throws Exception{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuilder qbuf = new StringBuilder("");
		if(query != null && query.trim().length() > 0){
			if(!checkQuery(query) )
				qbuf.append("");
			else if(query.startsWith("file_name") || query.startsWith("name")   ||query.indexOf("saved_date") >= 0)
				qbuf.append(" and ").append(query.trim());
				
		}
		String sql = "select * from saved_files where 1=1 "+qbuf.toString()+" order by save_name,lastmodified desc "	;
		logger.debug("getSavedFiles    :: "+sql );
		StringBuilder buf = new StringBuilder("[");
		try {
			conn = SqlUtils.getConnection();
			logger.debug("getSavedFiles  ");
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			int count = 0;
			while(rs.next()){
				if(count > 0)
					buf.append(",");
				String external_name = rs.getString("external_name");
				if(external_name == null || external_name.trim().isEmpty())
					external_name = "#";
				else{
					int ind = external_name.indexOf("unifier_");
					if(ind > 0)
						external_name = external_name.substring(ind);
				}
					
				SimpleDateFormat sdf = new SimpleDateFormat(WSConstants.TIME_FORMAT);
				buf.append("{\"file_id\":\"").append(rs.getString("service_id")).append("\",");
				buf.append("\"external_name\":\"").append(WSUtil.jsfilter2(external_name)).append("\",");
				buf.append("\"save_name\":\"").append(WSUtil.jsfilter2(rs.getString("save_name"))).append("\",");
				buf.append("\"lastmodified\":\"").append(sdf.format(rs.getTimestamp("lastmodified"))).append("\",");
				buf.append("\"prefix\":\"").append(rs.getString("prefix")).append("\",");
				buf.append("\"file_name\":\"").append(WSUtil.jsfilter2(rs.getString("file_name"))).append("\",");
				buf.append("\"file_path\":\"").append(WSUtil.jsfilter2(rs.getString("file_path"))).append("\"");
				buf.append("}\n");
				count++;
			}
			
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			buf.append("]");
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		logger.debug("getSavedFiles ********** "+buf);
		return buf.toString();
		
	}
	
	public String getBatchFiles(String query) throws Exception{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuilder qbuf = new StringBuilder("");
		if(query != null && query.trim().length() > 0){
			if(!checkQuery(query) )
				qbuf.append("");
			else if(query.startsWith("method_name") || query.startsWith("name")   ||query.indexOf("saved_date") >= 0)
				qbuf.append(" and ").append(query.trim());
				
		}
		String sql = "select * from batch_services_log where  user_id = ? "+qbuf.toString()+" order by batch_name  limit 200"	;
		logger.debug("getBatchFiles    :: "+sql );
		StringBuilder buf = new StringBuilder("[");
		try {
			conn = SqlUtils.getConnection();
			logger.debug("getBatchFiles  ");
			ps = conn.prepareStatement(sql);
			ps.setInt(1, user_id);
			rs = ps.executeQuery();
			int count = 0;
			while(rs.next()){
				if(count > 0)
					buf.append(",");
				SimpleDateFormat sdf = new SimpleDateFormat(WSConstants.TIME_FORMAT);
				buf.append("{\"batch_id\":\"").append(rs.getLong("batch_id")).append("\",");
				buf.append("\"last_run_id\":\"").append(rs.getLong("last_run_id")).append("\",");
				buf.append("\"batch_name\":\"").append(rs.getString("batch_name")).append("\",");
				buf.append("\"thread_count\":\"").append(rs.getInt("thread_count")).append("\",");
				buf.append("\"service_count\":\"").append(rs.getInt("service_count")).append("\",");
				buf.append("\"created_date\":\"").append(sdf.format(rs.getTimestamp("created_date"))).append("\",");
				String lastrun = "";
				if(rs.getTimestamp("last_run_time") != null)
					lastrun = sdf.format(rs.getTimestamp("last_run_time"));
				buf.append("\"last_run_time\":\"").append(lastrun).append("\"");
				buf.append("}\n");
				count++;
			}
			
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			buf.append("]");
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		logger.debug("getSavedFiles ********** "+buf);
		return buf.toString();
		
	}
public Map<String,Object> getStudioInfo(String prefix) throws Exception{
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Map<String,Object> map = new HashMap<String,Object>();
		try {
			conn = SqlUtils.getConnection();
			String sql = "select * from studio_log where studio_prefix = ? ";
			ps = conn.prepareStatement(sql);
			ps.setString(1, prefix);
			rs = ps.executeQuery();
			
			if(rs.next()){
				map.put("studio_name", rs.getString("studio_name"));
				map.put("studio_prefix", rs.getString("studio_prefix"));
				map.put("studio_source", rs.getString("studio_source"));
				map.put("studio_type", rs.getString("studio_type"));
				map.put("studio_level", rs.getInt("studio_level"));
				map.put("studio_version", rs.getInt("studio_version"));
				map.put("studio_status", rs.getInt("studio_status"));
				map.put("published_date", rs.getTimestamp ("published_date"));
			}
			
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return map;
		
	}
	public String getWBSCodes(String projectnumber) throws Exception{
		WBSCodes wbs = new WBSCodes(projectnumber);
		return wbs.getWBSCodes();
		
	}


	

}
