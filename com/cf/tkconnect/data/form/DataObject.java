package com.cf.tkconnect.data.form;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


import com.cf.tkconnect.data.form.DataObject;
import com.cf.tkconnect.database.SqlUtils;
import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;

public class DataObject {

	static Log logger = LogSource.getInstance(DataObject.class);
	
	public DataObject(){

	}

	
	public static Map<String,Map<String,Object>> getDDMap(ResultSet rs, String key) throws Exception {
		Map<String,Map<String,Object>> map = new HashMap<String,Map<String,Object>>();
		if(rs == null)
			return map;
//		 ResultSetMetaData md = rs.getMetaData() ;
//		 int colsize = md.getColumnCount();
		 while(rs.next()){
			 Map<String,Object> dmap = new HashMap<String,Object>();
			 String keyvalue = rs.getString(key);
			 dmap.put("data_name", rs.getString("data_name"));
			 dmap.put("data_type", rs.getString("data_type"));
			 dmap.put("data_size", rs.getInt("data_size"));
			 dmap.put("input_type", rs.getString("input_type"));
			 dmap.put("category", rs.getString("category"));
			 dmap.put("used", rs.getString("used"));
			 dmap.put("lastmodified", rs.getTimestamp("lastmodified"));
			 map.put(keyvalue, dmap);
		 }
		 return map;
	}
	
	public static List<Map<String,Object>> getDDList(ResultSet rs) throws Exception {
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		if(rs == null)
			return list;
		 while(rs.next()){
			 Map<String,Object> dmap = new HashMap<String,Object>();
			 dmap.put("data_name", rs.getString("data_name"));
			 dmap.put("data_type", rs.getString("data_type"));
			 dmap.put("data_size", rs.getInt("data_size"));
			 dmap.put("input_type", rs.getString("input_type"));
			 dmap.put("category", rs.getString("category"));
			 dmap.put("used", rs.getString("used"));
			 dmap.put("lastmodified", rs.getTimestamp("lastmodified"));
			 list.add( dmap);
		 }
		 return list;
	}

	public static Map<String,String> getKeyMap(ResultSet rs, String key, String key_value) throws Exception {
		Map<String,String> map = new HashMap<String,String>();
		if(rs == null)
			return map;
		 while(rs.next()){
			 String keyvalue = rs.getString(key);
			 String mapvalue = rs.getString(key_value);
			 map.put(keyvalue, mapvalue);
		 }
		 return map;
	}
	
	public static Map<String,Map<String,Object>> getDEMap(ResultSet rs, String key) throws Exception {
		Map<String,Map<String,Object>> map = new HashMap<String,Map<String,Object>>();
		if(rs == null)
			return map;
//		 ResultSetMetaData md = rs.getMetaData() ;
//		 int colsize = md.getColumnCount();
		 while(rs.next()){
			 Map<String,Object> dmap = new HashMap<String,Object>();
			 String keyvalue = rs.getString(key);
			 dmap.put("de_name", rs.getString("de_name"));
			 dmap.put("de_label", rs.getString("de_label"));
			 dmap.put("de_data_def", rs.getInt("de_data_def"));
			 dmap.put("lastmodified", rs.getTimestamp("lastmodified"));
			 map.put(keyvalue, dmap);
		 }
		 return map;
	}

	public static List<Map<String,Object>> getDEList(ResultSet rs) throws Exception {
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		if(rs == null)
			return list;
		 while(rs.next()){
			 Map<String,Object> dmap = new HashMap<String,Object>();
			 dmap.put("de_name", rs.getString("de_name"));
			 dmap.put("de_label", rs.getString("de_label"));
			 dmap.put("de_data_def", rs.getInt("de_data_def"));
			 dmap.put("lastmodified", rs.getTimestamp("lastmodified"));
			 list.add( dmap);
		 }
		 return list;
	}
	
	 public static Map<String, String> getBPNames(List<String> bpPrefix){
		 StringBuilder buf = new StringBuilder();
		  for(String pr : bpPrefix){
			  if(buf.length() > 0)
				  buf.append(",");
			  buf.append("'"+pr+"'");
		  }
		  return getBPNames(buf.toString());
	 }
	
	
	 public static Map<String, String> getBPNames(String bpPrefixes){
		   Connection conn = null;
			  PreparedStatement ps = null;
			  ResultSet rs = null;
			  Map<String, String> map = new HashMap<String, String>();
			  try{
				  //get company info
				  conn = SqlUtils.getConnection();
					ps = conn.prepareStatement("select * from studio_log  where  studio_prefix in (?) ");
					ps.setString(1,bpPrefixes);
					rs = ps.executeQuery();
					while(rs.next()){
						 map.put(rs.getString ("studio_prefix"),rs.getString ("studio_name"));
					}
			  }catch(Exception e){
				  logger.error(e);
			  }finally{
				  SqlUtils.closeResultSet(rs);
				  SqlUtils.closeStatement(ps);
				  SqlUtils.closeConnection(conn);
			  }
			  return map;
	  }
	
	 public static Map<String, String> getBPNameAttr(String bpPrefix) throws Exception{
		   Connection conn = null;
			  PreparedStatement ps = null;
			  ResultSet rs = null;
			  Map<String, String> map = new HashMap<String, String>();
			   logger.debug("getBPNameAttr -------- pre :"+bpPrefix);

			  try{
				  //get company info
				  conn = SqlUtils.getConnection();
					ps = conn.prepareStatement("select s.*,a.file_name,a.file_path from studio_bp_view s LEFT OUTER JOIN attribute_templates a ON ( s.studio_prefix = a.attr_prefix   " +
							" )   where  studio_prefix = ?");
					ps.setString(1,bpPrefix);
					rs = ps.executeQuery();
					if(rs.next()){
						String file_name = rs.getString("file_name");
						if(file_name == null || file_name.equalsIgnoreCase("NULL")){
							file_name = "";
							 map.put("notfound","true");
						}	
						 map.put("studio_name",rs.getString ("studio_name"));
						 map.put("file_name", file_name);
						 map.put("file_path",rs.getString ("file_path"));
				   logger.debug("getBPNameAttr --------***** map "+map);
					}
			  }catch(Exception e){
				  logger.error(e,e);
			  }finally{
				  SqlUtils.closeResultSet(rs);
				  SqlUtils.closeStatement(ps);
				  SqlUtils.closeConnection(conn);
			  }
			  return map;
	  }

}
