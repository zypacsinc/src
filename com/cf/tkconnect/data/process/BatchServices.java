package com.cf.tkconnect.data.process;

import static org.apache.commons.lang3.math.NumberUtils.toInt;
import static org.apache.commons.lang3.math.NumberUtils.toLong;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cf.tkconnect.data.TKUnifierMetaData;
import com.cf.tkconnect.database.SqlUtils;
import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.util.FileUtils;
import com.cf.tkconnect.util.WSConstants;
import com.cf.tkconnect.util.WSUtil;



public class BatchServices {

		HttpServletRequest req;
		HttpServletResponse res;
		
		String service_ids;
		String batch_name;
		int user_id;
		int company_id;
		int thread_count;
		long batch_id = -1;
		long run_id = -1;
		int service_id_count =0;
		int schedule_status = 0;
		List<Long> saved_service_list = new ArrayList<Long>();
		
		static Log logger = LogSource.getInstance(BatchServices.class);

		public BatchServices(HttpServletRequest req, HttpServletResponse res){
			this.req = req;
			this.res = res;
			this.service_ids = this.req.getParameter("service_ids");
			this.batch_name = this.req.getParameter("batch_name");
			this.user_id =  toInt(req.getParameter("user_id"));
			this.company_id = toInt(req.getParameter("company_id"));
			this.thread_count = toInt(req.getParameter("thread_count"));
			this.service_id_count = toInt(req.getParameter("service_id_count"));
			this.schedule_status = toInt(req.getParameter("schedule_status"));
		}

		public BatchServices(HttpServletRequest req, HttpServletResponse res, boolean get){
			this.req = req;
			this.res = res;
			this.company_id = toInt(req.getParameter("company_id"));
			this.batch_id = toLong(this.req.getParameter("batch_id"));
			this.user_id =  toInt(req.getParameter("user_id"));
		}	
		
		public String saveBatchService(){
			//run the service request ids & batch name
			try{
				setBatchServiceIds();
				createBatchSaveRecord();
			}catch(Exception e){
				logger.error(e,e);
				return "{\"errors\": \"Errors occured will processing services.\"}";
			}
			return "{\"saved\": \"true\"}";
		}

		
		public String getBatchResults(){
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			StringBuilder buf = new StringBuilder("[");
			try {
				conn = SqlUtils.getConnection();
				logger.debug("selected ids   ");
				ps = conn.prepareStatement("select * from batch_services_log where  batch_id ?  ");
				ps.setLong(1, batch_id);
				rs = ps.executeQuery();
				if(rs.next()){
					this.run_id = rs.getLong("last_run_id");
					if(this.run_id <= 0)
						throw new Exception("No results found");
					SqlUtils.closeResultSet(rs);
					SqlUtils.closeStatement(ps);
					ps = conn.prepareStatement("select s.* from batch_audit_services b join services_audit_log s on (b.service_audit_id = s.id) where b.batch_id = ? and b.run_id = ?    ");
					ps.setLong(1, batch_id);
					ps.setLong(2, run_id);
					int count = 0;
					while(rs.next()){
						if(count > 0)
							buf.append(",");
						SimpleDateFormat sdf = new SimpleDateFormat(WSConstants.TIME_FORMAT);
						buf.append("{\"saved_service_id\":\"").append(rs.getLong("saved_service_id")).append("\",");
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
						buf.append("\"service_details\":").append(TKUnifierMetaData.getServiceFileDetails(filename));
						int rc = rs.getInt("response_code");
						if(rc == 200)
							buf.append(",\"output_details\":").append(FileUtils.getFileContents(outfilename));
						buf.append("}");
					}
					
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
		
		protected void createBatchSaveRecord() throws Exception{
			Connection conn = null;
			PreparedStatement ps = null;
			String insert_service = "insert into  batch_services_log (batch_name, thread_count,service_count,schedule_status) "
			+" values(?,?,?,?)";
			ResultSet rs = null;
			try {
				//logger.debug(" Create createBatchSaveRecord");
				 conn = SqlUtils.getConnection();
				 ps  = conn.prepareStatement( insert_service,Statement.RETURN_GENERATED_KEYS );
		         ps.setString(1,     this.batch_name);
		         ps.setInt(2,      this.thread_count );
		         ps.setInt(3,      this.service_id_count );
		         ps.setInt(4,      this.schedule_status );
		         ps.executeUpdate();
		         rs = ps.getGeneratedKeys();
		         if(rs != null && rs.next())
		        	 batch_id = rs.getLong(1);
		         
		         logger.debug(" The createBatchSaveRecord batch_services_log batch_id::::::::::::::: " +batch_id+"  ::"+saved_service_list);
		         // save all the children
		         if(saved_service_list.size() > 0 && batch_id > 0){// prep for batch insert
		        	 SqlUtils.closeResultSet(rs) ;
		 			 SqlUtils.closeStatement(ps);
		 			 ps  = conn.prepareStatement( "insert into batch_services_details (batch_id,saved_service_id) values (?,?) ");
		 			 for(Long id : saved_service_list){
		 				 ps.setLong(1, batch_id);
		 				 ps.setLong(2, id);
		 				 ps.addBatch();
		 			 }
		 			 ps.executeBatch();
		 			 SqlUtils.closeResultSet(rs) ;
		 			 SqlUtils.closeStatement(ps);
		 			 ps  = conn.prepareStatement( "update batch_services_log set service_count = ?  where batch_id = ? " );
		 			 ps.setInt(1,    saved_service_list.size());
			         ps.setLong(2,      this.batch_id );
			         ps.executeUpdate();
		         }
		         
		         
			}catch(Exception e){
				logger.error(e,e);
			} finally {
				SqlUtils.closeResultSet(rs) ;
				SqlUtils.closeStatement(ps);
				SqlUtils.closeConnection(conn);
			}
		}
	
		protected void setBatchServiceIds() throws Exception{
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				
				List<String> groups = new ArrayList<String>();
				conn = SqlUtils.getConnection();
				logger.debug("selected ids   ");
				ps = conn.prepareStatement("select * from saved_files where  service_id in (?) order by group_name,service_id desc ");
				ps.setString(1, service_ids);
				rs = ps.executeQuery();
				while(rs.next()){
					String group_name = rs.getString("group_name");
					if(group_name != null && group_name.trim().length() > 0 && !groups.contains(group_name))
						groups.add(group_name);
					else{
						if(!saved_service_list.contains(rs.getLong("service_id")))
							saved_service_list.add(rs.getLong("service_id"));
					}
				}
				logger.debug(" getBatchDetails ids "+saved_service_list+" groups :"+groups);
				if(groups.size() > 0){
					SqlUtils.closeResultSet(rs);
					SqlUtils.closeStatement(ps);
					// now build a query to get all groups 
					StringBuilder buf = new StringBuilder();
					int i = 0;
					for(String g: groups){
						if(i > 0)
							buf.append(",");
						buf.append(g);
						i++;
					}
					logger.debug(" getBatchDetails group names :"+buf);
					ps = conn.prepareStatement("select * from saved_files where  group_name in (?) order by group_name,service_id desc ");
					ps.setString(1, buf.toString());
					rs = ps.executeQuery();
					while(rs.next()){
						//String group_name = rs.getString("group_name");
						long id = rs.getLong("service_id");
						if(!saved_service_list.contains(id))
							saved_service_list.add(rs.getLong("service_id"));
					}
				}
			}catch(Exception e){
				logger.error(e,e);
			} finally {
				
				SqlUtils.closeResultSet(rs);
				SqlUtils.closeStatement(ps);
				SqlUtils.closeConnection(conn);
				
			}
		}

		protected void getBatchDetails() throws Exception{
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				conn = SqlUtils.getConnection();
				logger.debug("selected ids   ");
				ps = conn.prepareStatement("select * from batch_services_log where  batch_id ?  ");
				ps.setLong(1, batch_id);
				rs = ps.executeQuery();
				if(rs.next()){
					this.run_id = rs.getLong("last_run_id");
				}
				
			}catch(Exception e){
				logger.error(e,e);
			} finally {
				
				SqlUtils.closeResultSet(rs);
				SqlUtils.closeStatement(ps);
				SqlUtils.closeConnection(conn);
				
			}
				
			
		}

}
