package com.cf.tkconnect.data.form;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.cf.tkconnect.data.process.ProcessUnifierObjectInfo;
import com.cf.tkconnect.database.SqlUtils;
import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.process.ResponseObject;
import com.cf.tkconnect.process.RunWebServices;
import com.cf.tkconnect.util.DataUtils;
import com.cf.tkconnect.util.InitialSetUp;
import com.cf.tkconnect.util.WSUtil;

public class WBSCodes {
	static Log logger = LogSource.getInstance(WBSCodes.class);
		String projectnumber;
		
	public WBSCodes(String projectnumber){
		this.projectnumber = projectnumber;
	}
	Map<Integer,Map<String,Object>> wbsmap = new HashMap<Integer,Map<String,Object>>();
	Map<String,String> wbscodes = new HashMap<String,String>();
	List<Integer> leafchildren = new ArrayList<Integer>();
	
	public String getWBSCodes() throws Exception{
		int count = setCodes();
		if(count == 0){
			synchWBS();
			count = setCodes();
			if(count == 0)
				return "[]";
		}
			
		return generateCode();
	}
	
	private int setCodes() throws Exception{
		//TODO need to configure this
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int count = 0;
		try {
			conn = SqlUtils.getConnection();
//			SELECT s.studio_prefix, a.file_name
			String sql = "select * from wbs_codes   where projectnumber = ? order by orderid";
			ps = conn.prepareStatement(sql);
			ps.setString(1, projectnumber);
			rs = ps.executeQuery();
			int maxindentlevel = 0;
			
			while(rs.next()){
				Map<String,Object> map = new HashMap<String,Object>();
				int wbscodeid = rs.getInt("wbscodeid");
				map.put("wbscodeid", wbscodeid);
				map.put("orderid", rs.getInt("orderid"));
				map.put("parentid", rs.getInt("parentid"));
				map.put("wbscode", rs.getString("wbscode"));
				map.put("wbsitem", rs.getString("wbsitem"));
				wbsmap.put(wbscodeid,map);
				if(logger.isDebugEnabled())
					logger.debug("setCodes  --"+wbscodeid );
				 if( maxindentlevel < rs.getInt("indentlevel"))
					 maxindentlevel =  rs.getInt("indentlevel");
				count++; 
			}
			if(count == 0)
				return 0; 
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			sql = "select * from wbs_codes where projectnumber = ? and indentlevel = ? ";
			ps = conn.prepareStatement(sql);
			ps.setString(1, projectnumber);
			ps.setInt(2, maxindentlevel);
			rs = ps.executeQuery();
			// these are all child which do not have children
			while(rs.next())
				leafchildren.add(rs.getInt("wbscodeid"));
			if(logger.isDebugEnabled())
				logger.debug(maxindentlevel+" leafchildren  --"+leafchildren );
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return count;
		
	}
	private String generateCode(){
		StringBuilder buf = new StringBuilder("[");
		int count = 0;
		for(int bitemid : leafchildren){
			String wbscode = getCode(bitemid,"");
			String wbsitem = (String)wbsmap.get(bitemid).get("wbsitem");
			wbscodes.put(wbscode,wbsitem);
			if(count  > 0)
				buf.append(",");
			buf.append("{\"wbscode\":\"").append(wbscode).append("\",\"wbsitem\":\"").append(WSUtil.jsfilter2(wbsitem)).append("\"}");
			count++;
		}
		buf.append("]");
		if(logger.isDebugEnabled())
			logger.debug("Codes generated : "+buf);
		return buf.toString();
	}
	
	private String getCode(int wbscodeid, String wbscode){
		if(logger.isDebugEnabled())
			logger.debug("getCode  "+wbscodeid );
		String code = (String)wbsmap.get(wbscodeid).get("wbscode");
		int parentid = (Integer)wbsmap.get(wbscodeid).get("parentid");
		String cd = (wbscode.trim().length()==0?code:code +"~~"+wbscode);
		if(parentid == 1){
			return cd;
		}
		
		return getCode(parentid, cd);
	}
	
	
	public synchronized void synchWBS() {
		   // returns JSON of 
		   try{
			   Map<String,String> paramMap = new HashMap<String,String>();
			   paramMap.put("method_name", "getWBSCodes");
			   paramMap.put("projectnumber",projectnumber);
			   paramMap.put("shortname", InitialSetUp.company.get("shortname"));
			   paramMap.put("authcode", InitialSetUp.company.get("authcode"));
			   paramMap.put("options","");
			   String input_file_name=projectnumber+"_wbscodes";
			   RunWebServices rs = new RunWebServices(paramMap);
			   rs.runService(false,input_file_name);
			   ResponseObject  responseObjData =rs.getResponse();
			   DataUtils.createServiceRecord("getWBSCodes", projectnumber,responseObjData.getStatusCode() );
			    if( responseObjData.getStatusCode() != 200){
			    	String err = WSUtil.filter(responseObjData.getErrors());
					  return;
			    }
				  // save the file & update database
				ProcessUnifierObjectInfo pod = new ProcessUnifierObjectInfo(   responseObjData.getXmlcontents() );
				//FileUtils.writeXMLContents("sync","projects", company_id, responseObjData.getXmlcontents());
				 pod.processWBSXMLData(projectnumber)   ;
		   }catch(Exception e){
			   logger.error(e,e);
			  
		   }
		}
}
