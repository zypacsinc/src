package com.cf.tkconnect.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringUtils;

import com.cf.tkconnect.database.SqlUtils;
import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.util.WSConstants;
import com.cf.tkconnect.util.WSUtil;


public class Favorites {
	
static Log logger = LogSource.getInstance(Favorites.class);
	
	int user_id =1;
	
	
	public Favorites( int user_id){
		this.user_id = user_id;
	}

	public Favorites( ){
		
	}
	public String setFavoriteMethods(String methods) throws Exception {
		
		return setFavorite( methods,  "user_fav_methods",  "method_name");
	}
	
	public String setFavoriteBPs(String bps) throws Exception {
			
			return setFavorite( bps,  "user_fav_bps",  "studio_prefix");
		}
	
	public String setFavoriteProjects(String methods) throws Exception {
		
		return setFavorite( methods,  "user_fav_projects",  "projectnumber");
	}
	
	private String setFavorite(String favorites, String table_name, String fav_type) throws Exception {
		// first delete all methods 
		Connection conn = null;
		PreparedStatement ps = null;
		int count = 0;
		try{
			conn = SqlUtils.getConnection();
			ps = conn.prepareStatement("delete  from "+table_name+" where user_id = ?");
			ps.setInt(1, user_id); 
			ps.executeUpdate();
			SqlUtils.closeStatement(ps);

			if(favorites == null || favorites.trim().length() == 0)
				return "{\"update\": \"0\" }";
			String[] favarr = StringUtils.split(favorites, ',');
			ps = conn.prepareStatement("insert  into "+table_name+" ( user_id,"+fav_type+") values (?,?) ");
			for(String f : favarr){
				ps.setInt(1, user_id); 
				ps.setString(2,f);
				ps.addBatch();
				count++;
			}
			int[] ret = ps.executeBatch();
		}catch(Exception e){
			logger.error(e,e);
			return "{\"update\": \"-1\" }";
		} finally {
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return "{\"update\": \""+count+"\" }";
	}


	public String getFavoriteMethods() throws Exception{
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuilder buf = new StringBuilder("[");
		try {
			conn = SqlUtils.getConnection();
//			System.out.println("Connection ------");
			ps = conn.prepareStatement("select * from user_fav_methods where  user_id = ? ");
			ps.setInt(1, user_id);
			rs = ps.executeQuery();
			int count = 0;
			while(rs.next()){
				if(count > 0)
					buf.append(",");
				buf.append("{\"name\":\"").append(rs.getString("method_name")).append("\"");
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
	
	

}
