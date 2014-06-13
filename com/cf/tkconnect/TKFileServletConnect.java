package com.cf.tkconnect;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

import org.apache.log4j.Logger;

import com.cf.tkconnect.data.form.FileUpload;
import com.cf.tkconnect.process.RunWebServices;

import com.cf.tkconnect.util.PropertyManager;
import com.cf.tkconnect.util.InitialSetUp ;

import static org.apache.commons.lang3.math.NumberUtils.toInt;

public class TKFileServletConnect extends HttpServlet{
	private static final Logger logger = Logger.getLogger(TKFileServletConnect.class);
		
	public void init(){
		// read the properties file
		
		String dir = PropertyManager.getProperty("smartlink.directory");
		if(logger.isDebugEnabled())
			logger.debug("webLink Servlet -------- "+dir+"  :: "+System.getProperty("catalina.home"));
//		InitialSetUp setup = new InitialSetUp();
//		setup.init(System.getProperty("catalina.home")+"/webapps/smartlink");
	}
	
	public void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
		
		String ipAddr = req.getRemoteAddr();
		if(logger.isDebugEnabled())
			logger.debug("webLink Servlet  "+ipAddr);
		String actionType = req.getParameter("actiontype");
		if(actionType != null)
				actionType = actionType.trim();
		int company_id = toInt( req.getParameter("company_id"));
		if(logger.isDebugEnabled())
			logger.debug(" The action type value *** is "+actionType+" "+("project_list".equals(actionType))+"   ::"+req.getParameter("company_id"));		
		try   {	
			String jsonstr = null;
			if("dummy_data".equals(actionType )){
				if(logger.isDebugEnabled())
					logger.debug(" The action disp ");
				req.getRequestDispatcher("jsp/index.html").forward(req,res);
			}
			//logger.debug(" The resp id is "+jsonstr);
			if(jsonstr == null || jsonstr.trim().length() == 0)
				jsonstr ="{}";
			res.getWriter().write(jsonstr);
	    }
	    catch (ServletException e)	    {
	      logger.error(e,e);
	    }
	    catch (IOException e)	    {
	    	logger.error(e,e);
	    }
		catch(Exception e){
	    	logger.error(e,e);
		}
	}//end of doGet
	
	public void doPost(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException {		
		if(logger.isDebugEnabled())
			logger.debug("WebLink File Servlet  post");
		String actionType = req.getParameter("actiontype");
		if(logger.isDebugEnabled())
			logger.debug(" The action type value is --"+actionType);
		String jsonstr ="";
			try	{
				if("download_data".equals(actionType )){ 
					RunWebServices rws = new RunWebServices(req,res);
					rws.downloadFileData();
					return;
				}else if("download_excel".equals(actionType )){
						RunWebServices rws = new RunWebServices(req,res);
						rws.downloadFileData();
						return;
					}else{//file upload
						if(logger.isDebugEnabled())
							logger.debug("WebLink File Servlet file ** post");
						FileUpload fu = new FileUpload(req,res);
						jsonstr =fu.processUpload();
				}
				if(jsonstr == null || jsonstr.trim().length() == 0)
					jsonstr ="{}";
				res.getWriter().write(jsonstr);
			}	
			catch (ServletException e)
		    {
				res.getWriter().write("failed "+e.getMessage());
			     logger.error(e,e);
			}
		    catch (IOException e)
		    {
		    	logger.error(e,e);
		    }
			catch (Exception e)
		    {					
				logger.error(e,e);
		    }
		}
	
	/*
	
	public JSONObject toJson() throws Exception {
		logger.debug("TO JSON OBJECT CALL ");
		JSONArray jsonArray = JSONArray.fromObject(null);
		//logger.debug("==== : "+jsonArray);
		
		Map map = new HashMap();
		map.put("datasets", jsonArray);

		JSONObject jsonObject = JSONObject.fromObject(map);
		//logger.debug("==== json :"+jsonObject);
		
		return jsonObject;
	} 
	
	
	public JSONArray toJsonArray(List listobj) throws Exception {
		logger.debug("TO JSON OBJECT CALL ");
		JSONArray jsonArray = JSONArray.fromObject(listobj);
		
		//logger.debug("==== : "+jsonArray);		
		return jsonArray;
	}
	*/
}