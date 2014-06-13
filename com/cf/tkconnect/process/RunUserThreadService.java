package com.cf.tkconnect.process;

import static org.apache.commons.lang3.math.NumberUtils.toInt;
import static org.apache.commons.lang3.math.NumberUtils.toLong;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis.utils.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import com.cf.tkconnect.database.SqlUtils;
import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;



public class RunUserThreadService {
//	HttpServletRequest req;
//	HttpServletResponse res;
	
	String service_ids;
	String batch_name;
	int user_id;
	int company_id;
	int thread_count;
	long batch_id = -1;
	long run_id = -1;
	
	String company_url;
	List<Map<String,String>> saved_service_list = new ArrayList<Map<String,String>>();
	
	static Log logger = LogSource.getInstance(RunUserThreadService.class);

	public RunUserThreadService(HttpServletRequest req, HttpServletResponse res){
//		this.req = req;
//		this.res = res;
		this.user_id =  toInt(req.getParameter("user_id"));
		this.company_id = toInt(req.getParameter("company_id"));
		this.batch_id = toLong(req.getParameter("data_param"));
	}
	public RunUserThreadService(int user_id, int company_id, long batch_id){
		this.user_id =  user_id;
		this.company_id = company_id;
		this.batch_id = batch_id;
	}

	public String startServiceRequest(){
		//run the service request using the batch_id
		try{
			logger.debug(" Create startServiceRequest "+batch_id);
			if(batch_id <=0)
				return "{\"batch_error\": \"Batch not found.\"}";
			getBatchSaveRecord();
			List<ResponseObject> list = runBatchService() ;
			return createBatchServiceRecords(list);
		}catch(Exception e){
			logger.error(e,e);
			return "{\"batch_error\": \"Errors occured will processing services.\"}";
		}
		
	}
	
	protected void getBatchSaveRecord() throws Exception{
		Connection conn = null;
		PreparedStatement ps = null;
		String select_service = "select * from batch_services_log where user_id = ? and batch_id = ?";
		ResultSet rs = null;
		try {
			 conn = SqlUtils.getConnection();
			 ps  = conn.prepareStatement( select_service );
	         ps.setInt(1,    this.user_id);
	         ps.setLong(2,      this.batch_id );
	         rs = ps.executeQuery();
	         if(rs.next()){
		         this.batch_name = rs.getString("batch_name");
		         this.thread_count = rs.getInt("thread_count");
		         this.batch_name = rs.getString("batch_name");
		        
	         }else
	        	 throw new Exception ("Vatch id value not correct");
	         
	         logger.debug(" The getBatchSaveRecord batch_services_log batch_id::::::::::::::: " +batch_id+" ::"+batch_name);
	         // save all the children
        	 SqlUtils.closeResultSet(rs) ;
 			 SqlUtils.closeStatement(ps);
 			 ps  = conn.prepareStatement( "select s.* from batch_services_details b join  saved_files s on (b.saved_service_id =  s.service_id) where b.batch_id =  ?  order by s.group_name,s.service_id desc");
 			 ps.setLong(12,      this.batch_id );
 			 rs =  ps.executeQuery();
 			 setSavedFilesList(rs);
 			 
 			 ps = conn.prepareStatement("insert into batch_run_log (batch_id) values (?) ",Statement.RETURN_GENERATED_KEYS);
			 ps.setLong(1,this.batch_id);
			 int up = ps.executeUpdate();
			 logger.debug(" The updating createBatchServiceRecords batch_services_log ::::::::::::::: "+up);
	         rs = ps.getGeneratedKeys();
		     if(rs != null && rs.next())
		        	 run_id = rs.getLong(1);

		     SqlUtils.closeResultSet(rs) ;
			 SqlUtils.closeStatement(ps);
			 ps = conn.prepareStatement("update batch_services_log set last_run_id = ?, last_run_time=? where batch_id = ?");
			 ps.setLong(1, this.run_id);
			 ps.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
			 ps.setLong(3,this.batch_id);
			 up = ps.executeUpdate();
			  logger.debug(" The updating createBatchServiceRecords batch_services_log ::::::::::::::: "+up);
			 SqlUtils.closeStatement(ps);
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			SqlUtils.closeResultSet(rs) ;
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
	}
	
	protected void setSavedFilesList(ResultSet rs) throws Exception{
		
		/*Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;*/
		try {
			/*conn = SqlUtils.getConnection();
			logger.debug("getSavedFiles  ");
			ps = conn.prepareStatement("select * from saved_files where company_id = ? and user_id = ?  and service_id in (?) order by group_name,service_id desc ");
			ps.setInt(1, company_id);
			ps.setInt(2, user_id);
			ps.setString(3, service_ids);
			rs = ps.executeQuery();*/
			while(rs.next()){
				Map<String,String> map = new HashMap<String,String>();
				map.put("saved_service_id",rs.getString("service_id"));
				map.put("file_name",rs.getString("file_name"));
				map.put("file_path",rs.getString("file_path"));
				map.put("user_id", ""+this.user_id);
				map.put("company_id", ""+this.company_id);
				map.put("company_url", this.company_url);
				map.put("method_name", rs.getString("method_name"));

				saved_service_list.add(map);
			}
			
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			/*
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
			*/
		}
		logger.debug("setSavedFilesList ********** "+saved_service_list);
		
	}

	protected String createBatchServiceRecords(List<ResponseObject> list) throws Exception{
		Connection conn = null;
		PreparedStatement ps = null;
		String insert_service = "insert into  batch_audit_services (batch_id,service_audit_id,run_id)  values (?,?,?)";
		StringBuilder buf = new StringBuilder("[");
		try {
			 conn = SqlUtils.getConnection();
			 ps  = conn.prepareStatement( insert_service );
			 int count = 0;
			 for(ResponseObject resp : list){
		         ps.setLong(1,    this.batch_id);
		         ps.setLong(2, resp.getAuditServiceId() );
		         ps.setLong(3, this.run_id );
		         ps.addBatch();
		         if(count > 0)
		        	 buf.append(",");
		         buf.append("{ \"saved_service_id\": \"").append(resp.getSavedServiceId()).append("\",");
		         buf.append(" \"audit_service_id\": \"").append(resp.getAuditServiceId()).append("\",");
		         buf.append(" \"statuscode\": \"").append(resp.getStatusCode() ).append("\",");
		         buf.append(" \"xmlcontents\": \"").append(StringEscapeUtils.escapeXml(resp.getXmlcontents())).append("\",");
		         buf.append(" \"errorStatus\": \"").append(StringEscapeUtils.escapeXml(resp.getErrors())).append("\"}");
		         count++;
			 }
	     	 int[] insertids = ps.executeBatch();
	     	//conn.commit();
	         logger.debug(" The createBatchServiceRecords batch_audit_services ::::::::::::::: "+insertids);
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
			buf.append("]");
		}
		return buf.toString();
	}
	
		
	protected   List<ResponseObject> runBatchService() {
	      List<Future<ResponseObject>> futurelist = new ArrayList<Future<ResponseObject>>();

	      ExecutorService executor = Executors.newFixedThreadPool(this.thread_count);
	      for (Map<String,String> map : this.saved_service_list) {
	    	  
	    	  RunServiceThread worker = new  RunServiceThread(map) ;
	    	  Future<ResponseObject> submit= executor.submit(worker);
	    	  futurelist.add(submit);

	      }
	      // This will make the executor accept no new threads
	      // and finish all existing threads in the queue
	      executor.shutdown();
	      // Wait until all threads are finish
	      while (!executor.isTerminated()) {
	      }
	      List<ResponseObject> resplist = new ArrayList<ResponseObject>();
	      for (Future<ResponseObject> future : futurelist) {
	        try {
	        	resplist.add(future.get());
	        } catch (InterruptedException e) {
	         	logger.error(e,e);
	        } catch (ExecutionException e) {
	        	logger.error(e,e);
	        }
	      }
	      if (futurelist.size() != resplist.size()){
	         throw new RuntimeException("Mis match responses -entries!!!"); 
	      }
	      return  resplist;

	 }
	
}
