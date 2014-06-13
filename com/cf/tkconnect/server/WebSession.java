package com.cf.tkconnect.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.cf.tkconnect.TKConnectServlet;
import com.cf.tkconnect.database.SqlUtils;

public class WebSession {

	private static final Logger logger = Logger.getLogger(TKConnectServlet.class);
	HttpServletRequest req;
	HttpServletResponse res;
	static java.security.MessageDigest digest = null;
	
	public  WebSession(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
		this.req = req;
		this.res = res;
		
	}

	public String validateUser() throws Exception{
		PreparedStatement ps = null;
	    Connection c = null;
	    ResultSet rs = null;
	    int user_id = -1;
        logger.debug(" login user id :"+user_id);
	    HttpSession ses = req.getSession(true);
        logger.debug(" login u222222222 :"+ses);
	   
	    String sesid = ses.getId();
	    logger.debug(" login 333333333 :"+sesid);
	    String ue = req.getParameter("user_email");
        logger.debug(" login 444444 :"+ue);
        if(ue== null || !StringUtils.isAlphanumeric(ue)){
	    	 logger.debug(" login 555555  return :"+user_id);
	    	return "{\"user_id\": \"-1\", \"session\":\"-1\" }";
	    }
	    StringBuilder buf = new StringBuilder("{\"user_id\": ");
	    try { 
	        logger.debug(" login 66666666 :"+user_id);
	    	
	    	 String up = getSHADigest(req.getParameter("user_password"));
	    	 c = SqlUtils.getConnection();		    	 
	         ps = c.prepareStatement("select * from user_info where user_email = ? and user_password = ?");
	         ps.setString(1, ue);
	         ps.setString(2, up);
	         rs = ps.executeQuery();
		     if(null != rs && rs.next()){
		         user_id = rs.getInt("user_id");
		         logger.debug(" login user id :"+user_id);
		         //
		           buf.append("\"").append(user_id).append("\"");
		    	   buf.append(", \"session\":\""+sesid+"\"");
		    	   buf.append(", \"firstname\":\""+rs.getString("firstname")+"\"");
		    	   buf.append(", \"lastname\":\""+rs.getString("lastname")+"\"");
		    	  SqlUtils.closeResultSet(rs);
		    	  SqlUtils.closeStatement(ps);
		    	
			       ps = c.prepareStatement("insert into login_info (user_id,session_id,ip_address) values(?,?,?)");
			       ps.setInt(1,  user_id);
			       ps.setString(2,  ses.getId());
			       ps.setString(3, this.req.getRemoteAddr());
		    	   ps.executeUpdate();
		    	   buf.append(", \"session\":\""+sesid+"\"");
		         
		     }else
		    	 buf.append("\"-1\"");
		     
		   } catch (Exception ex) {
		    	  logger.error(ex,ex);
		    	  buf.append("\"-1\"");
		     } finally {
		    	  SqlUtils.closeResultSet(rs);
		    	  SqlUtils.closeStatement(ps);
		    	  SqlUtils.closeConnection(c);
		     }
	         buf.append("}");
		return buf.toString();
	}
	
	public synchronized  final String getSHADigest(String data) {
		if (data == null)
			return "";
	
        if (digest == null) {
            try {
                digest = java.security.MessageDigest.getInstance("SHA");
            }
            catch (java.security.NoSuchAlgorithmException nsae) {
                System.err.println("Failed to load the SHA MessageDigest. ");
                nsae.printStackTrace();
            }
        }
        digest.update(data.getBytes());
        return toHexString(digest.digest());
    }
	
	 public  final String toHexString(byte hash[]) {
	        StringBuffer buf = new StringBuffer(hash.length * 2);
	        int i;

	        for (i = 0; i < hash.length; i++) {
	            if (((int) hash[i] & 0xff) < 0x10) {
	                buf.append("0");
	            }
	            buf.append(Long.toString((int) hash[i] & 0xff, 16));
	        }
	        return buf.toString();
	    }
}
