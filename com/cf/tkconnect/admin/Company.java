package com.cf.tkconnect.admin;

import java.sql.Timestamp;
import javax.servlet.http.*;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.sql.Statement;

import com.cf.tkconnect.database.SqlUtils;
import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.process.ProcessSync;
import com.cf.tkconnect.process.ResponseObject;
import com.cf.tkconnect.util.InitialSetUp;
import com.cf.tkconnect.util.WSUtil;
import com.cf.tkconnect.util.PropertyManager;

import static org.apache.commons.lang3.math.NumberUtils.toInt;

public class Company {   
	
	static Log logger = LogSource.getInstance(Company.class);

	private int company_id;
	private int user_id;
	private String registryPrefix;
	private String companyName ="ForeSee";
	private String shortName;
	private String authCode;
	private String companyURL;
	private String dirLocation;
	private String fileLocation;
	private Timestamp lastModified;
	private int data_set = 0;
	private  final String COMPANY_INSERT_SQL    = "insert into company ( registry_prefix, company_name, shortname, authcode, company_url,lastmodified) VALUES (?,?,?,?,?,?)";
	private  final String COMPANY_UPDATE_SQL    = "update company set  company_name=?, shortname=?,authcode=?, company_url=? , dir_location =?,file_location=? where company_id=?";
	private  final String COMPANY_SELECT_SQL = "select c.* from company c join user_company u on( c.company_id = u.company_id) where u.user_id=?";
		
	public Company( int user_id){
		this.user_id = user_id;
	}
	public Company(HttpServletRequest req, int user_id){
		//this.companyName = req.getParameter("company_name");
		this.registryPrefix = req.getParameter("registry_prefix");
		this.shortName = req.getParameter("shortname");
		this.authCode = req.getParameter("authcode");
		this.companyURL = req.getParameter("company_url");
		this.dirLocation = req.getParameter("dir_location");
		this.fileLocation = req.getParameter("file_location");
		this.company_id = toInt(req.getParameter("company_id"),1);
	}
	
	public int getId() {
		return company_id;
	}
	public String getRegistryPrefix(){
		return registryPrefix;
	}
	public String getCompanyName(){
		return companyName;
	}
	public String getShortName(){
		if(shortName == null)
			return "";
		return shortName;
	}
	public String getAuthCode(){
		if(authCode == null)
			return "";
		return authCode;
	}
	public String getCompanyURL(){
		return companyURL;
	}
	public int getDataSet(){
		return data_set;
	}
	
	public Timestamp getLastModified(){
		return lastModified;
	}
	
	
	public String createCompany()  {		
			Connection conn =null;
			PreparedStatement ps  = null;
			ResultSet rs = null;
			this.company_id = -1;
			try {
				logger.debug(" Create Company");
					 conn = SqlUtils.getConnection();
					 ps  = conn.prepareStatement( COMPANY_INSERT_SQL,Statement.RETURN_GENERATED_KEYS );
			         ps.setString(1,     getRegistryPrefix());
			         ps.setString(2,      getCompanyName() );
			         ps.setString(3,     getShortName());
			         ps.setString(4,     getAuthCode());
			         ps.setString(5,     getCompanyURL());
			         ps.setTimestamp(6, (new Timestamp(System.currentTimeMillis())));
			         int insertid = ps.executeUpdate();
			         rs =ps.getGeneratedKeys();
			         int coid = -1;
			         if(rs != null && rs.next())
			        	 coid = rs.getInt(1);
			         logger.debug(" The object inserted::::::::::::::: " +insertid+"  :"+coid);
			        // we need the new company id 
			         if( coid > 0){
				    	 this.company_id = coid;
				    	 // now generate the directory for this company & insert user co table
				    	// createCompanyDirectory();
				    	 SqlUtils.closeStatement(ps);
				    	 ps  = conn.prepareStatement( "insert into user_company (user_id,company_id) values (?,?)" );
				    	 ps.setInt(1, user_id);
				    	 ps.setInt(2, coid);
				    	 ps.executeUpdate();
			         }	 
				    	
			} catch (Exception e){
				logger.error(e,e);
				return "{ \"company_id\": \""+this.company_id+"\", \"errors\": \"Errors occurred\" }"  ;
			} finally {
				SqlUtils.closeResultSet(rs);
		    	  SqlUtils.closeStatement(ps);
		    	  SqlUtils.closeConnection(conn);
			}
			return "{ \"company_id\": \""+this.company_id+"\"}"  ;
		}
	
	public String updateCompany() throws Exception {		
		Connection conn =null;
		PreparedStatement ps  = null;
	
		try {
			 	if(logger.isDebugEnabled())
			 		logger.debug(" Update Company");
				 conn = SqlUtils.getConnection();
				 ps  = conn.prepareStatement( COMPANY_UPDATE_SQL );
		         ps.setString(1,      getCompanyName() );
		         ps.setString(2,     getShortName());
		         ps.setString(3,     getAuthCode());
		         ps.setString(4,     getCompanyURL());
		         ps.setString(5,     this.dirLocation);
		         ps.setString(6,     this.fileLocation);
		         ps.setInt(7,     this.company_id);
		         int insertid = ps.executeUpdate();
		         if(logger.isDebugEnabled())
		        	 logger.debug(" The object updated::::::::::::::: " +insertid+" "+getCompanyURL());
		         boolean requpdate = false;
		         if(!InitialSetUp.company.get("shortname").equals(getShortName()) || !InitialSetUp.company.get("authcode").equals(getAuthCode())
		        		 ||!InitialSetUp.company.get("company_url").equals(getCompanyURL()) )
		        	 requpdate = true;	 
		         InitialSetUp.company.put("company_name", getCompanyName());
		         InitialSetUp.company.put("shortname", getShortName());
		         InitialSetUp.company.put("authcode", getAuthCode());
		         InitialSetUp.company.put("company_url", getCompanyURL());
		         InitialSetUp.company.put("dir_location",  this.dirLocation);
		         InitialSetUp.company.put("file_location",  this.fileLocation);
		         InitialSetUp.setUpDirectories();
		        
		         PropertyManager.saveUserProperties(InitialSetUp.appHome);
		       // we need to reset all the info for this Co
		       // int cleanup =  cleanupCompanyData();
		        if(logger.isDebugEnabled())
		        	logger.debug(" The object clean up:::::::requpdate:::::::: " +requpdate+" sn:"+InitialSetUp.company.get("shortname")+" au:"+InitialSetUp.company.get("authcode"));
		        if(requpdate){
		        	//cleanupCompanyData();
		        	ProcessSync pss = new ProcessSync();
		        	ResponseObject rc =pss.ping(InitialSetUp.company.get("shortname") ,InitialSetUp.company.get("authcode"),
		        			InitialSetUp.company.get("company_url"));
					int code = rc.getStatusCode() ;
					return "{\"statuscode\":"+code+",\"errors\":\""+rc.getErrors() +"\" }";
		        }
		        
		} catch (Exception e){
			logger.error(e,e);
			return "{ \"company_id\": \""+this.company_id+"\", \"errors\": \"Errors occurred\" }"  ;
			
		} finally {
	    	  SqlUtils.closeStatement(ps);
	    	  SqlUtils.closeConnection(conn);
		}
		return "{ \"company_id\": \""+this.company_id+"\"}"  ;
	}
		
	public String updateSyncInfo() throws Exception {		
		Connection conn =null;
		PreparedStatement ps  = null;
	
		try {
			logger.debug(" Update Company Sync");
				 conn = SqlUtils.getConnection();
				 ps  = conn.prepareStatement( "update company set data_set=?, data_set_date=? where company_id=?" );
		         ps.setInt(1,  data_set   );
		         ps.setTimestamp(2, (new Timestamp(System.currentTimeMillis())));

		         ps.setInt(3,     this.company_id);
		         int insertid = ps.executeUpdate();
		} catch (Exception e){
			logger.error(e,e);
			return "{ \"company_id\": \""+this.company_id+"\", \"errors\": \"Errors occurred\" }"  ;
			
		} finally {
	    	  SqlUtils.closeStatement(ps);
	    	  SqlUtils.closeConnection(conn);
		}
		return "{ \"company_id\": \""+this.company_id+"\"}"  ;
	}
	
	
		public String getCompanies() throws Exception {
			logger.debug("  Get Companies for  "+user_id);
			
		    StringBuilder buf = new StringBuilder("[");
	      try { 
			    	 buf.append("{");
			    	 buf.append("\"company_id\":\"").append(InitialSetUp.company.get("company_id")).append("\",");
			    	 buf.append("\"company_name\":\"").append(WSUtil.jsfilter2(InitialSetUp.company.get("company_name"))).append("\",");
			    	 buf.append("\"registry_prefix\":\"").append(InitialSetUp.company.get("registry_prefix")).append("\",");
			    	 buf.append("\"shortname\":\"").append(WSUtil.jsfilter(InitialSetUp.company.get("shortname"))).append("\",");
			    	 buf.append("\"authcode\":\"").append(InitialSetUp.company.get("authcode")).append("\",");
			    	 buf.append("\"company_url\":\"").append(InitialSetUp.company.get("company_url")).append("\",");
			    	 buf.append("\"dir_location\":\"").append(WSUtil.jsfilter2(InitialSetUp.company.get("dir_location"))).append("\",");
			    	 buf.append("\"file_location\":\"").append(WSUtil.jsfilter2(InitialSetUp.company.get("file_location"))).append("\"");
			    	 buf.append("}");
			    
			     buf.append("]");
		      } catch (Exception ex) {
		    	  logger.error(ex,ex);
		    	  return "[]";
		      }
		     logger.debug("the size of the companies is "+buf); 
		     return buf.toString();
		}
		public String getCompanies_db() throws Exception {
			logger.debug("  Get Companies for  "+user_id);
			PreparedStatement ps = null;
		    Connection c = null;
		    ResultSet rs = null;
		    StringBuilder buf = new StringBuilder("[");
	      try { 
		    	 c = SqlUtils.getConnection();		    	 
		         ps = c.prepareStatement(COMPANY_SELECT_SQL);
		         ps.setInt(1, user_id);
		         rs = ps.executeQuery();
		         int count = 0;
			     while(null != rs && rs.next()){
			    	 if(count > 0)
			    		 buf.append(",");
			    	 buf.append("{");
			    	 buf.append("\"company_id\":\"").append(rs.getInt("company_id")).append("\",");
			    	 buf.append("\"company_name\":\"").append(WSUtil.jsfilter2(rs.getString("company_name"))).append("\",");
			    	 buf.append("\"registry_prefix\":\"").append(rs.getString("registry_prefix")).append("\",");
			    	 buf.append("\"shortname\":\"").append(WSUtil.jsfilter(rs.getString("shortname"))).append("\",");
			    	 buf.append("\"authcode\":\"").append(rs.getString("authcode")).append("\",");
			    	 buf.append("\"company_url\":\"").append(rs.getString("company_url")).append("\",");
			    	 buf.append("\"dir_location\":\"").append(WSUtil.jsfilter2(rs.getString("dir_location"))).append("\"");
			    	 buf.append("\"file_location\":\"").append(WSUtil.jsfilter2(InitialSetUp.company.get("file_location"))).append("\"");
			    	 buf.append("}");
			    	 count++;
			     }
			     buf.append("]");
		      } catch (Exception ex) {
		    	  logger.error(ex,ex);
		    	  return "[]";
		      } finally {
		    	  SqlUtils.closeResultSet(rs);
		    	  SqlUtils.closeStatement(ps);
		    	  SqlUtils.closeConnection(c);
		     }
		     logger.debug("the size of the companies is "+buf); 
		     return buf.toString();
		}
		
		public String getCompanyData(int company_id) throws Exception {
			logger.debug(" SmartlinkDBHandler : Get CompanyData");
			PreparedStatement ps = null;
		    Connection c = null;
		    ResultSet rs = null;	    
		    StringBuilder buf = new StringBuilder("");
	      try { 
		    	 c = SqlUtils.getConnection();		    	 
		    	 ps = c.prepareStatement("select * from company where company_id = ?");
		         ps.setInt(1, company_id);
		         rs = ps.executeQuery();
			     while(null != rs && rs.next()){
			    	  setCompanyId(rs.getInt("company_id"));
			    	  setCompanyName(rs.getString("company_name"));
			    	  setRegistryPrefix(rs.getString("registry_prefix"));
			    	  setShortName(rs.getString("shortname"));
			    	  setAuthCode(rs.getString("authcode"));
			    	  setCompanyURL(rs.getString("company_url"));
			    	  setDirLocation(rs.getString("dir_location"));
				    	 buf.append("{");
				    	 buf.append("\"company_id\":").append(rs.getInt("company_id")).append("\",");
				    	 buf.append("\"company_name\":").append(rs.getString("company_name")).append("\",");
				    	 buf.append("\"registry_prefix\":").append(rs.getString("registry_prefix")).append("\",");
				    	 buf.append("\"shortname\":").append(rs.getString("shortname")).append("\",");
				    	 buf.append("\"authcode\":").append(rs.getString("authcode")).append("\",");
				    	 buf.append("\"company_url\":").append(rs.getString("company_url")).append("\",");
				    	 buf.append("\"dir_location\":").append(rs.getString("dir_location")).append("\"");
				    	 buf.append("}");
			     }
		      } catch (Exception ex) {
		    	  logger.error(ex,ex);
		      } finally {
		    	  SqlUtils.closeResultSet(rs);
		    	  SqlUtils.closeStatement(ps);
		    	  SqlUtils.closeConnection(c);
		     }
		     logger.debug("the company object is set "); 
			return buf.toString();
		}
		
	
	public void setCompanyId(int company_id) {
		this.company_id = company_id;
	}
	public void setRegistryPrefix(String registryPrefix){
		this.registryPrefix = registryPrefix;
	}
	public void setCompanyName(String companyName) {
		//this.companyName = companyName;
	}
	public void setShortName(String shortName){
		this.shortName = shortName;
	}
	public void setAuthCode(String authCode){
		this.authCode = authCode;
	}
	public void setCompanyURL(String companyURL){
		this.companyURL = companyURL;
	}
	public void setLastModified(Timestamp lastModified) {
		this.lastModified = lastModified;
	}
	
	public void setDirLocation(String dirLocation) {
		this.dirLocation = dirLocation;
	}
	
	public void setDataSet(int data_set) {
		this.data_set = data_set;
	}
	public synchronized int cleanupCompanyData() throws Exception {
		if(logger.isDebugEnabled())
			logger.debug("  delete data for  Companies for  "+company_id);
		PreparedStatement ps = null;
	    Connection c = null;
	  
	    
      try { 
	    	 c = SqlUtils.getConnection();		    	 
	         ps = c.prepareStatement("delete from data_elements where company_id = ?");
	         ps.setInt(1, this.company_id);
	         ps.executeUpdate();
	         ps.close();
	         ps = c.prepareStatement("delete from studio_log where company_id = ?");
	         ps.setInt(1, this.company_id);
	         ps.executeUpdate();
	         ps.close();
	         ps = c.prepareStatement("delete from bp_log where company_id = ?");
	         ps.setInt(1, this.company_id);
	         ps.executeUpdate();
	         ps.close();
	         ps = c.prepareStatement("delete from project_log where company_id = ?");
	         ps.setInt(1, this.company_id);
	         ps.executeUpdate();
	         ps.close();
	 		if(logger.isDebugEnabled())
				logger.debug("  delete data for  Companies for -- done  "+company_id);
	      } catch (Exception ex) {
	    	  logger.error(ex,ex);
	    	  return -1;
	      } finally {
	    	 
	    	  SqlUtils.closeStatement(ps);
	    	  SqlUtils.closeConnection(c);
	     }
	    
	     return 1;
		
	}
	

}