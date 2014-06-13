package com.cf.tkconnect;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

import org.apache.log4j.Logger;

import com.cf.tkconnect.adapters.CreateService;
import com.cf.tkconnect.admin.Company;
import com.cf.tkconnect.data.DownloadFile;
import com.cf.tkconnect.data.Favorites;
import com.cf.tkconnect.data.GenerateXML;
import com.cf.tkconnect.data.TKUnifierMetaData;
import com.cf.tkconnect.data.process.BatchServices;
import com.cf.tkconnect.data.process.SaveData;
import com.cf.tkconnect.data.process.UnifierXMLServiceData;
import com.cf.tkconnect.process.ProcessSync;
import com.cf.tkconnect.process.ResponseObject;
import com.cf.tkconnect.process.RunUserThreadService;
import com.cf.tkconnect.process.RunWebServices;
import com.cf.tkconnect.server.WebSession;
import com.cf.tkconnect.util.DataUtils;
import com.cf.tkconnect.util.PropertyManager;
import com.cf.tkconnect.util.InitialSetUp;

import static org.apache.commons.lang3.math.NumberUtils.toInt;
import static org.apache.commons.lang3.math.NumberUtils.toLong;

public class TKConnectServlet extends HttpServlet{
	private static final Logger logger = Logger.getLogger(TKConnectServlet.class);
		
	public void init(){
		// read the properties file
		//System.out.println("Connect servlet started");
		String dir = PropertyManager.getProperty("tkconnect.directory");
		if(logger.isDebugEnabled())
			logger.debug("webLink Servlet --------####### "+dir+"  :: "+System.getProperty("catalina.home"));
		InitialSetUp setup = new InitialSetUp();
		setup.init(System.getProperty("catalina.home")+"/webapps/bpsync");
	//	String relativeWebPath = "/WEB-INF/lib/datasource.properties";
	//	String dbabspath = getServletContext().getRealPath(relativeWebPath);
		
		
	}
	
	public void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
		HttpSession session = req.getSession(true);
		
		String ipAddr = req.getRemoteAddr();
		if(logger.isDebugEnabled())
			logger.debug("webLink Servlet ********** get "+ipAddr+" sess "+session.getId()+" new :"+session.isNew()+" cr: "+session.getCreationTime()+" last: "+session.getLastAccessedTime());
		String actionType = req.getParameter("actiontype");
		if(actionType != null)
				actionType = actionType.trim();
		int company_id = toInt( req.getParameter("company_id"));
		if(logger.isDebugEnabled())
			logger.debug(" The action type value *** is "+actionType+"  ::"+"service_info".equals(actionType));		
		try   {	
			String jsonstr = null;
			if("get_companies".equals(actionType)){
				//int user_id = toInt( req.getParameter("user_id"));
				Company co = new Company(1);
				 jsonstr = co.getCompanies();// returns json str 
			}else if("get_company_data".equals(actionType)) {
				//int user_id = toInt( req.getParameter("user_id"));
				Company co = new Company(1);
				 jsonstr = co.getCompanyData(company_id); 
			}else if("all_bp_sync".equals(actionType)) {
				ProcessSync ps = new ProcessSync();
				jsonstr = ps.getAllStudioInfo();		
			}else if("sync_bp".equals(actionType)) {
				ProcessSync ps = new ProcessSync();
				String bpprefix = req.getParameter("bp_prefix");
				jsonstr = ps.getBPAttributes(bpprefix,true);		
			}else if("sync_project".equals(actionType)) {
				ProcessSync ps = new ProcessSync();
				jsonstr = ps.synchProjects();	
				TKUnifierMetaData ud = new TKUnifierMetaData();
				jsonstr = ud.getProjectList();
			}else if("sys_data_type".equals(actionType)) {
				//int user_id = toInt( req.getParameter("user_id"));
				String data_type = req.getParameter("data_param");
				TKUnifierMetaData ud = new TKUnifierMetaData();
				jsonstr = ud.getSysInfo( data_type);	
			}else if("project_list".equals(actionType)) {
			//	int user_id = toInt( req.getParameter("user_id"));
				TKUnifierMetaData ud = new TKUnifierMetaData();
				jsonstr = ud.getProjectList();	
			}else if("service_info".equals(actionType)) {
				String request_type= req.getParameter("request_type");
				jsonstr = DataUtils.getServiceDetails(request_type);	
				if(logger.isDebugEnabled())
					logger.debug("service info  "+request_type+" ::"+jsonstr );
			}else if("bp_info".equals(actionType)) {
				ProcessSync ps = new ProcessSync();
				String bpPrefix = req.getParameter("bp_prefix");
				jsonstr = ps.getBPAttributes(bpPrefix,false);		
			}else if("bp_list".equals(actionType)) {
				TKUnifierMetaData ud = new TKUnifierMetaData();
				jsonstr = ud.getBPList();	
			}else if("dd_list".equals(actionType)) {
				TKUnifierMetaData ud = new TKUnifierMetaData();
				jsonstr = ud.getDDList();	
			}else if("de_list".equals(actionType)) {
				TKUnifierMetaData ps = new TKUnifierMetaData();
				jsonstr = ps.getDEList();		
			}else if("login".equals(actionType)) {
				WebSession ws = new WebSession(req,res);
				jsonstr = ws.validateUser();
			}else if("fav_methods".equals(actionType)) {
				//int user_id = toInt( req.getParameter("user_id"));
				String methods = req.getParameter("data_param");
				Favorites fv = new Favorites();
				jsonstr = fv.setFavoriteMethods(methods);
			}else if("fav_bps".equals(actionType)) {
				//int user_id = toInt( req.getParameter("user_id"));
				String methods = req.getParameter("data_param");
				Favorites fv = new Favorites();
				jsonstr = fv.setFavoriteBPs(methods);
			}else if("fav_projects".equals(actionType)) {
				//int user_id = toInt( req.getParameter("user_id"));
				String methods = req.getParameter("data_param");
				Favorites fv = new Favorites();
				jsonstr = fv.setFavoriteProjects(methods);
			}else if("get_fav_methods".equals(actionType)) {
				//int user_id = toInt( req.getParameter("user_id"));
				Favorites fv = new Favorites();
				jsonstr = fv.getFavoriteMethods();
			}else if("shell_types".equals(actionType)) {
			//	int user_id = toInt( req.getParameter("user_id"));
				TKUnifierMetaData ud = new TKUnifierMetaData();
				jsonstr = ud.getShellTypes();	
			}else if("get_saved_list".equals(actionType)) {
				String query = req.getParameter("data_param");
				TKUnifierMetaData ud = new TKUnifierMetaData();
				jsonstr = ud.getSavedFiles(query);
			}else if("wbs_codes".equals(actionType)) {
				String projectnumber = req.getParameter("projectnumber");
				if(logger.isDebugEnabled())
					logger.debug("wbs codes   :"+projectnumber);
				TKUnifierMetaData ud = new TKUnifierMetaData();
				jsonstr = ud.getWBSCodes(projectnumber);
			}else if("get_saved_file".equals(actionType)) {
				// not used using downloadfile
				String fileid = req.getParameter("data_param");
				//UnifierXMLServiceData sxml = new UnifierXMLServiceData(filepath,true);
				//jsonstr = sxml.parse();
				if(logger.isDebugEnabled())
					logger.debug(" The get_saved_file  "+jsonstr);
			
			}else if("get_batch_webservices".equals(actionType)) {
				//int user_id = toInt( req.getParameter("user_id"));
				TKUnifierMetaData ud = new TKUnifierMetaData();
				jsonstr = ud.getBatchServiceList();
				if(logger.isDebugEnabled())
					logger.debug(" The get_user_webservices  "+jsonstr);
			}else if("run_service_list".equals(actionType)) {
				TKUnifierMetaData ud = new TKUnifierMetaData();
				String query = req.getParameter("data_query");
				jsonstr = ud.getRunServiceList(query);
				if(logger.isDebugEnabled())
					logger.debug(" The run_service_list query "+query);
			}else if("directory_service".equals(actionType)) {
				String data_param = req.getParameter("data_param");
				if("start".equalsIgnoreCase(data_param))
					InitialSetUp.startPolling();
				else if("stop".equalsIgnoreCase(data_param))
					InitialSetUp.stopPolling();
				if(logger.isDebugEnabled())
					logger.debug(" The action directory service "+data_param);
			}else if("get_service_details".equals(actionType)) {
				//int user_id = toInt( req.getParameter("user_id"));
				TKUnifierMetaData ud = new TKUnifierMetaData();
				String id = req.getParameter("data_param");
				jsonstr = ud.getRunServiceDetails(toLong(id));
			}else if("test_ping".equals(actionType)) {
				ProcessSync ps = new ProcessSync();
				jsonstr =ps.testPing(req.getParameter("shortname") , req.getParameter("authcode"), req.getParameter("company_url"));
			}else{//
				if(logger.isDebugEnabled())
					logger.debug(" The action disp not found ");
				req.getRequestDispatcher("jsp/index.html").forward(req,res);
				return;
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
		
		logger.debug("WebLink Servlet  post");
		String actionType = req.getParameter("actiontype");
		logger.debug(" The action type value is --"+actionType);
		String jsonstr ="";
			try	{
				if("create_company".equals(actionType )){
					//int user_id = toInt( req.getParameter("user_id")); 
					Company co = new Company(req,1); //1 is userid
					jsonstr = co.createCompany(); 
					ProcessSync ps = new ProcessSync();
					String ret = ps.getAllStudioInfo();	
					if(ret.indexOf("errors") < 0){
						co.setDataSet(1);
						co.updateSyncInfo();
					}	
					// now update company data with sync info
				} else if("update_company".equals(actionType )){
					//int user_id = toInt( req.getParameter("user_id")); 
					Company co = new Company(req,1);
					jsonstr = co.updateCompany(); 
					String directory_service = req.getParameter("directory_service");
					if("start".equalsIgnoreCase(directory_service))
						InitialSetUp.startPolling();
					else if("stop".equalsIgnoreCase(directory_service))
						InitialSetUp.stopPolling();
					if(logger.isDebugEnabled())
						logger.debug(" The action directory service "+directory_service);
				} else if("update_reset_company".equals(actionType )){
					//int user_id = toInt( req.getParameter("user_id")); 
					Company co = new Company(req,1);
					jsonstr = co.updateCompany(); 
					co.cleanupCompanyData();
					ProcessSync ps = new ProcessSync();
					String ret =ps.getAllStudioInfo();	
					if(ret.indexOf("errors") < 0){
						co.setDataSet(1);
						co.updateSyncInfo();
					}	
				} else if("save_batch_services".equals(actionType )){
					BatchServices rws = new BatchServices(req,res);
					jsonstr = rws.saveBatchService(); 
					
				} else if("run_webservice".equals(actionType )){
					GenerateXML gx = new GenerateXML(req,res);
					jsonstr = gx.process();
				} else if("save_data".equals(actionType )){
					SaveData sd = new SaveData(req,res);
					sd.processSave(0);
				} else if("upload_data".equals(actionType )){
					RunWebServices rws = new RunWebServices(req,res);
					rws.downloadFileData();
					return;
				} else if("run_single_service".equals(actionType )){
					RunWebServices rws = new RunWebServices(req,res);
					jsonstr = rws.runService();
				} else if("download_file".equals(actionType )){
					DownloadFile rws = new DownloadFile(req,res);
					rws.processDownload();
					return;
				} else if("new_service".equals(actionType )){
					CreateService rws = new CreateService(req,res);
					jsonstr = rws.start();
					
				}else{//file upload
					logger.debug("WebLink Servlet file ** post no actiontype seen");
				}
				logger.debug(" The post action jsonstr value is --"+jsonstr);
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

}