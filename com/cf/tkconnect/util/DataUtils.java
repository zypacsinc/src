package com.cf.tkconnect.util;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cf.tkconnect.data.ReadExcel;
import com.cf.tkconnect.data.process.SaveData;
import com.cf.tkconnect.database.SqlUtils;
import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import static org.apache.commons.lang3.math.NumberUtils.toLong;
import static org.apache.commons.lang3.math.NumberUtils.toInt;

public class DataUtils {

	static Log logger = LogSource.getInstance(DataUtils.class);
	
	public static long createSaveRecord(Map<String,String> map) throws Exception{
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String insert_service = "insert into  saved_files (external_name,save_name,file_path,file_name,isupload,prefix) "
		+" values(?,?,?,?,?,?)";
		long save_id =0;
		try {
			logger.debug(" Create upload service ::"+map);
			 conn = SqlUtils.getConnection();
			 ps  = conn.prepareStatement( insert_service,Statement.RETURN_GENERATED_KEYS );
	         ps.setString(1,    FileUtils.getFileName( map.get("external_name")));
	         ps.setString(2,     map.get("save_name"));
	         ps.setString(3,      map.get("file_path"));
	         ps.setString(4,   FileUtils.getFileName(  map.get("file_name")));
	         ps.setInt(5, toInt(map.get("uploadtype")));//
	         ps.setString(6,  map.get("prefix"));
	     	 int insertid = ps.executeUpdate();
	     	 rs = ps.getGeneratedKeys();
	         if(rs != null && rs.next())
	        	 save_id = rs.getLong(1);
	     	if(logger.isDebugEnabled())
	         logger.debug(" The createSaveRecord inserted::::::::::::::: " +save_id);
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return save_id;
	}
	
	public static Map<String,String> getSaveRecord(long fileid) throws Exception{
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select * from  saved_files where service_id = ?";
		Map<String,String> map = new HashMap<String,String>();
		try {
			logger.debug(" Create upload service");
			 conn = SqlUtils.getConnection();
			 ps  = conn.prepareStatement( sql );
	         ps.setLong(1, fileid);
	     	 rs = ps.executeQuery() ;
	         if(rs != null && rs.next()){
	        	map.put("file_path", rs.getString("file_path"));
	        	map.put("file_name", rs.getString("file_name"));
	        	map.put("save_name", rs.getString("save_name"));
	        	map.put("external_name", rs.getString("external_name"));
	        	
	        	map.put("uploadtype", rs.getString("isupload"));
	        	map.put("prefix", rs.getString("prefix"));
	         }	 
	     	if(logger.isDebugEnabled())
	         logger.debug(" The getSaveRecord get::::::::::::::: " +map+ " service_id "+fileid);
		}catch(Exception e){
			logger.error(e,e); 	
		} finally {
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return map;
	}
	
	
	public static Map<String,String> processSaveData(String[] fileinfo, String save_name,String external_name,String prefix, int uploadtype) throws Exception{
		 
		Map<String,String> map = new HashMap<String,String>();
		map.put("company_id", "1");
		map.put("file_name",fileinfo[0]);
		map.put("file_path",fileinfo[1]);
		map.put("save_name", save_name);
		map.put("external_name", external_name);
		map.put("prefix", prefix);
		map.put("uploadtype", uploadtype+"");
		long fileid = createSaveRecord(map);
		//  save this record
		map.put("fileid", ""+fileid);
		if(logger.isDebugEnabled())
			logger.debug("processSaveData map :"+map);
		return map;
	}
	
	public static Map<String,Object> getServiceAuditFile(long id,String filetype){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select * from services_audit_log where id = ?"; 
		String file = "output_file_name";
		Map<String,Object> map = new HashMap<String,Object>();
	//	String filetype = this.fileaction =req.getParameter("fileaction2");
		logger.debug("service file filetype : "+filetype);	
		if("zipfile".equalsIgnoreCase(filetype))
			file = "output_zip_file";
		try{
			conn = SqlUtils.getConnection();
			ps = conn.prepareStatement(sql);
			ps.setLong(1, id); 
			rs = ps.executeQuery();
			if(rs.next()){
				map.put("file_path", rs.getString("file_path"));
	        	map.put("input_file_name", rs.getString("input_file_name"));
	        	map.put("output_file_name", rs.getString("output_file_name"));
	        	map.put("save_file_id", rs.getLong("saved_file_id") );
	        	map.put("external_file_name", rs.getString("external_file_name"));
	        	map.put("time", rs.getTimestamp("start_time"));
	        	map.put("response_code", rs.getInt("response_code"));
				
				String name =  rs.getString(file);
				
			}
		logger.debug("getServiceAuditFile file name : "+map);	
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return map;
	}
	
	public static int updateServiceRecord(long service_id, int statuscode, String outputfilename,  String output_zip_file, String errors) throws Exception{
		Connection conn = null;
		PreparedStatement ps = null;
		String update_service = "update services_audit_log set end_time =?,response_code=?,output_file_name=? , errors = ?, output_zip_file=? where id = ? ";
		if(errors != null && errors.length() > 245)
			errors = errors.substring(0, 240);
		int upid = 0;
		try {
			logger.debug(" update service");
			 conn = SqlUtils.getConnection();
			 ps  = conn.prepareStatement( update_service);
	         ps.setTimestamp(1, (new Timestamp(System.currentTimeMillis())));
	         ps.setInt(2  ,  statuscode);
	         ps.setString(3,     FileUtils.getFileName(outputfilename));
	         ps.setString(4,     errors);
	         ps.setString(5,     output_zip_file);
	         ps.setLong(6, service_id);
	     	 upid = ps.executeUpdate();
	         logger.debug(" The updateServiceRecord ::::::::::::::: " +upid+"  :"+service_id);
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return upid;
	}
	
	public static long createAuditServiceRecord(Map<String,String> map) throws Exception{
		
		long service_id = 0; 
		String insert_service = "insert into  services_audit_log (saved_file_id,company_url,external_file_name,method_name,file_path,input_file_name,output_file_name) "
		+" values(?,?,?,?,?,?,?)";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			logger.debug(" Create service ::"+insert_service);
			 conn = SqlUtils.getConnection();
			 ps  = conn.prepareStatement( insert_service,Statement.RETURN_GENERATED_KEYS );
	         ps.setLong(1,    toLong(map.get("saved_file_id")));
	         ps.setString(2,     InitialSetUp.company.get("company_url"));
	         ps.setString(3,    FileUtils.getFileName(map.get("external_file_name")));
	         ps.setString(4,    map.get("method_name"));
	         ps.setString(5,   FileUtils.getFilePath(map.get("file_path")));
	         ps.setString(6,    FileUtils.getFileName(map.get("input_file_name")));
	         ps.setString(7,    FileUtils.getFileName(map.get("output_file_name")) );
	         
	     	 int insertid = ps.executeUpdate();
	         rs =ps.getGeneratedKeys();
	         if(rs != null && rs.next())
	        	 service_id = rs.getLong(1) ;
	         logger.debug(" The object inserted::::::::::::::: " +insertid+"  :"+service_id);
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return service_id;
	}
	
public static long createServiceRecord(String method_name, String request_type,int response_code ) throws Exception{
		
		long service_id = 0; 
		String insert_service = "insert into  services (method_name,request_type,response_code) "
		+" values(?,?,?)";
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			logger.debug(" Create internal service ::"+insert_service);
			 conn = SqlUtils.getConnection();
			 ps  = conn.prepareStatement( insert_service,Statement.RETURN_GENERATED_KEYS );
	         ps.setString(1,    method_name);
	         ps.setString(2,   request_type);
	         ps.setInt(3,    response_code);
	       	         
	     	 int insertid = ps.executeUpdate();
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return service_id;
	}
	
public static String getServiceDetails(String request_type){
	Connection conn = null;
	PreparedStatement ps = null;
	ResultSet rs = null;
	String sql = "select * from services where response_code = 200 and  request_type = ?"; 
	StringBuilder buf = new StringBuilder("{");
	try{
		SimpleDateFormat sdf = new SimpleDateFormat(WSConstants.TIME_FORMAT);
		conn = SqlUtils.getConnection();
		ps = conn.prepareStatement(sql);
		ps.setString(1, request_type); 
		rs = ps.executeQuery();
		if(rs.next()){
			buf.append("\"method_name\":\""+rs.getString("method_name")+"\",");
			String date = sdf.format(rs.getTimestamp("lastmodified"));
			buf.append("\"date\":\""+date+"\"");
			
		}
		buf.append("}");
	logger.debug("getServiceAuditFile file name : "+buf);	
	}catch(Exception e){
		logger.error(e,e);
	} finally {
		SqlUtils.closeResultSet(rs);
		SqlUtils.closeStatement(ps);
		SqlUtils.closeConnection(conn);
	}
	return buf.toString();
}
}
