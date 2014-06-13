package com.cf.tkconnect.process;


import static org.apache.commons.lang3.math.NumberUtils.toLong;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cf.tkconnect.connector.TKConnector;
import com.cf.tkconnect.data.process.UnifierXMLServiceData;

import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.process.ResponseObject;
import com.cf.tkconnect.process.RunWebServices;
import com.cf.tkconnect.unifierservices.UnifierServices;
import com.cf.tkconnect.util.DataUtils;
import com.cf.tkconnect.util.FileUtils;
import com.cf.tkconnect.util.InitialSetUp;
import com.cf.tkconnect.util.WSConstants;
import com.cf.tkconnect.util.WSUtil;
import com.cf.tkconnect.util.xml.FileData;
import com.cf.tkconnect.util.xml.FileObject;
import com.cf.tkconnect.wsdl.WSDLServices;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringEscapeUtils;

public class RunWebServices {
	
	static Log logger = LogSource.getInstance(RunWebServices.class);
	int user_id=1;
//	int company_id;
	long saved_service_id = 0;
	
	HttpServletRequest req;
	HttpServletResponse res;
	String service_name="unifierwebservices";
	String method_name;
//	String company_url;
	Map<String,String> paramMap = new HashMap<String,String>();
	String file_path;
	String input_file_name;
	long service_id;
	String file_name_path;
	String output_zip_file ="";
	boolean haszipfile = false;
	ResponseObject respobj;
	boolean isthread = false;
	String save_name = "";
	String group_name = "";
	String prefix;
	
	public RunWebServices(HttpServletRequest req, HttpServletResponse res){
		this.req = req;
		this.res = res;
		//this.user_id =  toInt(req.getParameter("user_id"));
		//this.company_id = toInt(req.getParameter("company_id"));
		this.method_name = req.getParameter("method_name");
		this.save_name = req.getParameter("save_name");
		this.prefix = req.getParameter("prefix");
		this.saved_service_id = toLong(req.getParameter("saved_service_id"));
		this.paramMap.put("input_zip_file","");
		logger.debug("File RunWebServices path ************  "+this.method_name+" m: "+this.method_name+" u:");
	}

	public RunWebServices( Map<String,String> paramMap){
		
		//this.user_id =  toInt(paramMap.get("user_id"));
		//this.company_id = toInt(paramMap.get("company_id"));
		//this.service_name = paramMap.get("service_name");
		this.method_name = paramMap.get("method_name");
		//this.company_url = paramMap.get("company_url");
		this.file_name_path =  paramMap.get("file_path");
		this.save_name = paramMap.get("save_name");
		this.saved_service_id = toLong(paramMap.get("saved_service_id"));
		this.prefix = paramMap.get("prefix");
		this.paramMap.putAll(paramMap);
		haszipfile = true;
		logger.debug("File name path ************  "+file_name_path);
	}

	public RunWebServices(String method_name, String zip_file_name){
		
		//this.user_id =  toInt(paramMap.get("user_id"));
		//this.company_id = toInt(paramMap.get("company_id"));
		this.file_name_path =  zip_file_name;
		this.method_name = method_name;
		//this.company_url = paramMap.get("company_url");
		logger.debug("method_name ************  "+method_name);
	}
	protected void setServiceParams() throws Exception{
		
		List<Map<String,String>> params = WSDLServices.getMethods().get(this.method_name);
		
		for(Map<String,String> param : params){
			String param_name = param.get("param_name");
			paramMap.put(param_name, req.getParameter(param_name));
		}
	}
	
	public void downloadFileData() throws Exception{
		setServiceParams();
		 String str = generateData() ;
		 res.setContentType("application/text");
		 res.setHeader("Content-Disposition","attachment; filename=weblink_"+this.method_name+".xml");
		 res.getWriter().write(str);
	}

	
	public String runService() throws Exception{
		return runService(true);
	}
	public String runService(String filename) throws Exception{
		haszipfile = false;
		createServiceRecord("xslx");
		String resp = callWebService();
			logger.debug("sending respo ************  "+"<script>parent.getUploadResponse( '"+resp+"')</script>");
			return "<script>parent.getUploadResponse( '"+resp+"')</script>";
			
		
	}

	public String runService(boolean setparam) throws Exception{
		if(setparam){
			setServiceParams();
			writeToFile();
		}
		createServiceRecord("xml");
		String resp = callWebService();
		if(!setparam && !haszipfile){
			logger.debug("sending respo ************  "+"<script>parent.getUploadResponse( '"+resp+"')</script>");
			return "<script>parent.getUploadResponse( '"+resp+"')</script>";
			
		}else{
			return resp;
		}
	}
	public String runService(int file_service_id) throws Exception{
		this.saved_service_id = file_service_id;
		// need to resad service details in parametermap
		createServiceRecord("xml");
		String resp = callWebService();
		return resp;
	}
	
	public String runService(boolean setparam, String input_file_name) throws Exception{
		this.input_file_name = input_file_name;
		return runService(setparam);
	}

	public ResponseObject  runService( String input_file_name, String file_path) throws Exception{
		this.input_file_name = input_file_name;
		this.file_path = file_path;
		UnifierXMLServiceData sm = new UnifierXMLServiceData(this.file_path+File.separator+this.input_file_name,true);
		sm.parse();
		this.paramMap.putAll( sm.getMap());
		this.paramMap.putAll(sm.getServiceParameterMap());
		createServiceRecord("xml");
		callWebService();
		respobj.setAuditServiceId(this.service_id);
		
		return respobj;
	}
	
	public ResponseObject  runServiceFromFile(Map<String,String>  datamap) throws Exception{
		if(logger.isDebugEnabled())
			logger.debug("runService respo *********");
		this.paramMap.putAll( datamap);
		callWebService();
		return respobj;
	}
	public String saveFileData(boolean setparam) throws Exception{
		if(setparam)
			setServiceParams();
		writeToFile();
		createSaveRecord("xml");
		return "{\"saved\":\"true\"}";
	}

	
	protected String generateData() throws Exception{
			
			StringBuilder buf = new StringBuilder("");
			buf.append("<"+WSConstants.TKCONNECT_TAG+">");
			buf.append("<method_name>").append(this.method_name).append("</method_name>\n");
			buf.append("<service_name>").append(this.service_name).append("</service_name>\n");;
			buf.append("<company_url>").append(InitialSetUp.company.get("company_url")).append("</company_url>\n");;
			buf.append("<shortname>").append(paramMap.get("shortname")).append("</shortname>\n");;
			List<Map<String,String>> params = WSDLServices.getMethods().get(this.method_name);
			buf.append("<parameters>");
			for(Map<String,String> param : params){
				String param_name = param.get("param_name");
				if(param_name.equals("shortname") || param_name.equals("authcode"))
					continue;// don't save these values
				String val = WSUtil.jsfilter2(this.paramMap.get(param_name));
				buf.append("<"+param_name+">").append(val).append("</"+param_name+">\n");;
			}
			buf.append("</parameters>\r\n");
			buf.append("</"+WSConstants.TKCONNECT_TAG+">\r\n");
			logger.debug("generateData  str "+buf);
			return buf.toString();
	}
	
	protected void writeToFile() throws Exception{
		String str = generateData();
		file_path =InitialSetUp.basefilepath+File.separator+this.user_id+File.separator+WSUtil.getDateString();
		File f = new File(file_path);
		if(!f.exists())
			f.mkdirs();
		input_file_name = RandomStringUtils.randomAlphanumeric(10)+"_"+this.method_name.toLowerCase();
		String filepathwithname = file_path + File.separator+input_file_name+".xml";
		FileUtils.writeContent( filepathwithname, str);
		
	}

	protected void createSaveRecord(String fileext) throws Exception{
		
		String[] fileinfo = new String[2];
		fileinfo[0] =  this.file_path;
		fileinfo[1] = this.input_file_name+"."+fileext;
		DataUtils.processSaveData( fileinfo, this.save_name,this.input_file_name+"."+fileext,this.prefix,2);// run services
		
	}

protected void createServiceRecord(String fileext) throws Exception{
		
		this.service_id = 0; 
		try {
			logger.debug(" Create service");
			Map<String,String> map = new HashMap<String,String>();
			map.put("file_path",this.file_path);
			map.put("input_file_name", this.input_file_name+"."+fileext);
			map.put("output_file_name", (this.file_name_path==null?"":this.file_name_path));
			map.put("external_file_name",this.input_file_name);
			map.put("method_name",this.method_name);
			map.put("prefix",(this.prefix == null || prefix.trim().length()==0?this.method_name:this.prefix));
			map.put("saved_file_id","0");
			this.service_id = DataUtils.createAuditServiceRecord(map);
	         logger.debug(" The object inserted:::::::::::::::   :"+service_id);
		}catch(Exception e){
			logger.error(e,e);
		}
		
	}

	public String callWebService() throws Exception{
		if(this.method_name.equalsIgnoreCase("getCompleteBPRecord") )
			return getCompleteBP();
		else if(this.method_name.equalsIgnoreCase("createCompleteBPRecord") )
			return createFileServices();
		else if(this.method_name.equalsIgnoreCase("updateCompleteBPRecord") )
			return createFileServices();
		else if(this.method_name.equalsIgnoreCase("addCompleteBPLineItem") )
			return createFileServices();
		else if(this.method_name.equalsIgnoreCase("createScheduleActivitiesFromFileV2") )
			return createFileServices();
		else if(this.method_name.equalsIgnoreCase("updateScheduleActivitiesFromFileV2") )
			return createFileServices();
		else
			return callUnifierWebService();
		
		//return "{}";
	}
	
	protected String getCompleteBP() throws Exception{
		String recno =  paramMap.get("recordNumber");
		logger.debug("geteComplete  start param  :  "+recno+"  :::"+paramMap);
		if(this.service_name.equalsIgnoreCase("mainservice")){
			TKConnector wc = new TKConnector(InitialSetUp.company.get("company_url"), this.service_name);
			respobj =wc.getCompleteBPRecord(paramMap.get("shortname"),paramMap.get("authcode"),
					paramMap.get("projectNumber"), paramMap.get("BPName"), recno);
			writeOutputZip(respobj, true);
		}else{
			List<Map<String,String>> params = WSDLServices.getMethods().get(this.method_name);
			Object[] objarr = new String[WSDLServices.getMethods().get(this.method_name).size()];
			int i = 0;
			for(Map<String,String> param : params){
				objarr[i] = paramMap.get(param.get("param_name"));
				i++;
			}
			UnifierServices us = new UnifierServices(InitialSetUp.company.get("company_url"), this.method_name);
			respobj = us.callService( params, objarr);
			writeOutputZip(respobj, false);
			/*
			DocumentServiceImpl dl = new DocumentServiceImpl(this.company_url);
			XMLFileData xd =  dl.getCompleteBPRecord(paramMap.get("shortname"),paramMap.get("authcode"),
					paramMap.get("projectNumber"), paramMap.get("BPName"), recno);
			respobj = new ResponseObject();
			respobj.setStatusCode( xd.getStatusCode());
			respobj.setErrorStatus(xd.getErrorStatus());
			respobj.setXmlcontents(xd.getXmlcontents());
			logger.debug("geteComplete  status :  "+xd.getStatusCode()+" file "+xd.getFilename());
			FileOutputStream fp = null;
			try{
				if(xd.getFilename() != null && !xd.getFilename().trim().isEmpty() && xd.getFileData() != null){
					respobj.setFilename(xd.getFilename());
					output_zip_file = xd.getFilename();
					String output_file = file_path + File.separator+xd.getFilename();
					File f = new File(output_file); 
					fp = new FileOutputStream(f);
					fp.write(xd.getFileData(), 0, xd.getFileData().length);
					fp.flush();
				}else{
					logger.error("Complete record has no file name");
				}
				
			}catch(Exception e){
				logger.error(e,e);
			}finally{
				if(fp != null)
					fp.close();
			}
			*/
		}
		return processResponse();
	}
	
	protected String createFileServices( ) throws Exception{
		if(this.service_name.equalsIgnoreCase("mainservice")){
			TKConnector wc = new TKConnector(InitialSetUp.company.get("company_url"), this.service_name);
			FileObject[] files  = WSUtil.getZipFileObject(file_name_path);
			if(this.method_name.equalsIgnoreCase("createCompleteBPRecord") )
				respobj =wc.createCompleteBPRecord(paramMap.get("shortname"),paramMap.get("authcode"),
					paramMap.get("projectNumber"), paramMap.get("BPName"), paramMap.get("BPXML"), "yes", files);
			else if(this.method_name.equalsIgnoreCase("createCompleteBPRecord") )
				respobj =wc.updateCompleteBPRecord(paramMap.get("shortname"),paramMap.get("authcode"),
						paramMap.get("projectNumber"), paramMap.get("BPName"), paramMap.get("BPXML"), "yes", files);
			else if(this.method_name.equalsIgnoreCase("addCompleteBPLineItem") )
				respobj =wc.addCompleteBPLineItem(paramMap.get("shortname"),paramMap.get("authcode"),
						paramMap.get("projectNumber"), paramMap.get("BPName"), paramMap.get("BPXML"), "yes", files);
			else if(this.method_name.equalsIgnoreCase("createScheduleActivitiesFromFileV2") )
				respobj =wc.createScheduleActivitiesFromFileV2(paramMap.get("shortname"),paramMap.get("authcode"),
					paramMap.get("projectNumber"), paramMap.get("sheetName"), paramMap.get("options"), "yes", files[0]);
			else if(this.method_name.equalsIgnoreCase("updateScheduleActivitiesFromFileV2") )
				respobj =wc.updateScheduleActivitiesFromFileV2(paramMap.get("shortname"),paramMap.get("authcode"),
					paramMap.get("projectNumber"), paramMap.get("sheetName"), paramMap.get("options"), "yes", files[0]);
				
		}else{
			logger.debug("createComplete ^^^^^^^^^^^^^^^^^^^^^^^^");
			FileData[] files = WSUtil.getZipFileData(file_name_path);
			if(files != null && files.length > 0)
				files[0].setFilename(FileUtils.getFileName(paramMap.get("f_filename")));
			logger.debug("createComplete ^^^^^^^^---------- file : "+paramMap.get("f_filename")+" :: "+FileUtils.getFileName(paramMap.get("f_filename")));
			List<Map<String,String>> params = WSDLServices.getMethods().get(this.method_name);
			Object[] objarr = new Object[WSDLServices.getMethods().get(this.method_name).size()];
			int i = 0;
			for(Map<String,String> param : params){
				logger.debug("createComplete ^^^^^param ^^^^^^"+i+"* "+param);
				if(param.get("param_name").equals("files")){
					logger.debug("createComplete ^^^^^^^^^^$$$ "+i+" len  "+ files.length+" :::: "+param.get("count"));
					if(param.get("count").equals("1") && files != null && files.length > 0)
						objarr[i] = files[0];
					else if(param.get("count").equals("unbounded") && files != null )
						objarr[i] = files;
					logger.debug("createComplete ^^^^^check ^^^^^^"+i+"^^^^^^^^^^^^ "+objarr[i]);
				}else{
					objarr[i] = paramMap.get(param.get("param_name"));
				}
				
				i++;
			}
			UnifierServices us = new UnifierServices(InitialSetUp.company.get("company_url"), this.method_name, true);
			respobj = us.callService( params, objarr);
		}
		return processResponse();
	}
	/*
	protected String updateComplete() throws Exception{
		WebLinkConnector wc = new WebLinkConnector(this.company_url, this.service_name);
		if(this.service_name.equalsIgnoreCase("mainservice")){
			FileObject[] files  = WSUtil.getZipFileObject(file_name_path);
			respobj =wc.updateCompleteBPRecord(paramMap.get("shortname"),paramMap.get("authcode"),
					paramMap.get("projectNumber"), paramMap.get("BPName"), paramMap.get("BPXML"), "yes", files);
		}else{
			FileData[] files = WSUtil.getZipFileData(file_name_path);
			respobj =wc.updateCompleteBPRecord(paramMap.get("shortname"),paramMap.get("authcode"),
					paramMap.get("projectNumber"), paramMap.get("BPName"), paramMap.get("BPXML"), "yes", files);
		}
		return processResponse();
	}

	protected String addComplete() throws Exception{
		WebLinkConnector wc = new WebLinkConnector(this.company_url, this.service_name);
		if(this.service_name.equalsIgnoreCase("mainservice")){
			FileObject[] files  = WSUtil.getZipFileObject(file_name_path);
			respobj =wc.addCompleteBPLineItem(paramMap.get("shortname"),paramMap.get("authcode"),
					paramMap.get("projectNumber"), paramMap.get("BPName"), paramMap.get("BPXML"), "yes", files);
		}else{
			FileData[] files = WSUtil.getZipFileData(file_name_path);
			respobj =wc.addCompleteBPLineItem(paramMap.get("shortname"),paramMap.get("authcode"),
					paramMap.get("projectNumber"), paramMap.get("BPName"), paramMap.get("BPXML"), "yes", files);
		}
		return processResponse();
	}
	
	protected String createScheduleFile() throws Exception{
		if(this.service_name.equalsIgnoreCase("mainservice")){
			WebLinkConnector wc = new WebLinkConnector(this.company_url, this.service_name);
			FileObject[] files  = WSUtil.getZipFileObject(file_name_path);
			FileObject file = files[0];
			respobj =wc.createScheduleActivitiesFromFileV2(paramMap.get("shortname"),paramMap.get("authcode"),
					paramMap.get("projectNumber"), paramMap.get("sheetName"), paramMap.get("options"), "yes", file);
		}else{
			FileData[] files = WSUtil.getZipFileData(file_name_path);
			FileData file = files[0];
			respobj =wc.createScheduleActivitiesFromFileV2(paramMap.get("shortname"),paramMap.get("authcode"),
					paramMap.get("projectNumber"), paramMap.get("sheetName"), paramMap.get("options"), "yes", file);
		}
		return processResponse();
	}

	protected String updateScheduleFile() throws Exception{
		WebLinkConnector wc = new WebLinkConnector(this.company_url, this.service_name);
		if(this.service_name.equalsIgnoreCase("mainservice")){
			FileObject[] files  = WSUtil.getZipFileObject(file_name_path);
			FileObject file = files[0];
			respobj =wc.updateScheduleActivitiesFromFileV2(paramMap.get("shortname"),paramMap.get("authcode"),
					paramMap.get("projectNumber"), paramMap.get("sheetName"), paramMap.get("options"), "yes", file);
		}else{
			FileData[] files = WSUtil.getZipFileData(file_name_path);
			FileData file = files[0];
			respobj =wc.updateScheduleActivitiesFromFileV2(paramMap.get("shortname"),paramMap.get("authcode"),
					paramMap.get("projectNumber"), paramMap.get("sheetName"), paramMap.get("options"), "yes", file);
		}
		return processResponse();
	}
*/
	public ResponseObject getResponse(){
		return this.respobj;
	}
	
	public ResponseObject  runWebService(StringBuilder buf) throws Exception{
		
		buf.append(callUnifierWebService());
		
		if(logger.isDebugEnabled())
			logger.debug(" The runWebService  resp "+respobj);
		return this.respobj;
	}
	
	protected String callUnifierWebService() throws Exception{
		java.lang.reflect.Method method;
		try {
			
			Class<?>[] cl = new Class[ WSDLServices.getMethods().get(this.method_name).size()];
			List<Map<String,String>> params = WSDLServices.getMethods().get(this.method_name);
			Object[] objarr = new Object[WSDLServices.getMethods().get(this.method_name).size()];
			int i = 0;
			if(logger.isDebugEnabled())
				logger.debug(" The callGeneralWebService ::"+WSDLServices.getMethods().get(this.method_name).size() +" :"+this.method_name+" :: "+this.service_name+" "+params);
			for(Map<String,String> param : params){
				String count = param.get("count");
				Class<?> cs = String.class;
				objarr[i] = paramMap.get(param.get("param_name"));
				if(!count.equalsIgnoreCase("1")){
					cs = String[].class;
					if(paramMap.get(param.get("param_name")) != null){
						String[] strarr = new String[1];
						strarr[0] = paramMap.get(param.get("param_name"));
						objarr[i] = strarr;
					}
				}
				cl[i]= cs;
				if(logger.isDebugEnabled())
					logger.debug(" The callGeneralWebService params ::"+i+" :: "+param.get("param_name") +"  "+objarr[i]);
				i++;
			}
			if( this.service_name.equals("mainservice") ){
				TKConnector wc = new TKConnector(InitialSetUp.company.get("company_url"), this.service_name);
				method = wc.getClass().getMethod(this.method_name, cl);
				if(logger.isDebugEnabled())
				logger.debug(" The callGeneralWebService mm::"+method.getName());
				this.respobj = (ResponseObject)method.invoke (wc, objarr);
			}else{
				if(logger.isDebugEnabled())
					logger.debug("other +++ The callGeneralWebService ::");
				UnifierServices us = new UnifierServices(InitialSetUp.company.get("company_url"), this.method_name);
				this.respobj = us.callService( params, objarr);
			}
			//method = c.getDeclaredMethod (this.method_name, cl);
			return processResponse();
		} catch (SecurityException e) {
			logger.error(e,e);
			return  "{\"errors\":\"Errors , "+StringEscapeUtils.escapeXml(e.getMessage())+"\"}";
		} catch (NoSuchMethodException e) {
			logger.error(e,e);
			return  "{\"errors\":\"Errors , "+StringEscapeUtils.escapeXml(e.getMessage())+"\"}";
		}
		
	}
	
	protected String processResponse() throws Exception{
		if(respobj == null)
			return "{\"errors\":\"unknown errors\"}";
		String err = "";
		if(respobj.getErrorStatus() != null && respobj.getStatusCode() != 200){
			for(String e : respobj.getErrorStatus())
				err +=" "+e;
			
		}
		StringBuilder buf = new StringBuilder("{");
		buf.append("\"service_audit_id\":\"").append(service_id).append("\",");
		buf.append("\"statuscode\":\"").append(respobj.getStatusCode()).append("\",");
		buf.append("\"errorstatus\":\"").append(WSUtil.jsfilter2(err)).append("\",");
		buf.append("\"filename\":\"").append(WSUtil.jsfilter2(output_zip_file)).append("\",");
		buf.append("\"xmlcontents\":\"").append(WSUtil.jsfilter2(respobj.getXmlcontents())).append("\"}");
		// write output file & update db rec
		String resp_file_name = "";
        logger.debug(" The processResponse ::::::::::::::: " +respobj.getStatusCode()+"  :"+buf+"  error ::"+err);

		if(respobj.getStatusCode() == 200 && respobj.getXmlcontents() != null && respobj.getXmlcontents().trim().length() > 0){
			file_path =InitialSetUp.basefilepath+File.separator+this.user_id+File.separator+WSUtil.getDateString();
			FileUtils.checkAndCreatDir(file_path);
			resp_file_name = input_file_name+"_response.xml";
			String filepathwithname = file_path + File.separator+resp_file_name;
			FileUtils.writeContent( filepathwithname,  buf.toString());
		}
		DataUtils.updateServiceRecord(this.service_id, respobj.getStatusCode(), resp_file_name, this.output_zip_file, err);
		return buf.toString();
	}
	
	
	
	 public  String runGeneralWebService(String class_name, String method_name) throws Exception  {
	 
	        try   {
	          //  Class<?> thisClass = loader.loadClass(class_name);
	            Class<?> thisClass = Class.forName(class_name);
	           // Class<?>[] params = {};
	            Object[] paramsObj = {};
	            Object instance = thisClass.newInstance();
	            Method[] ms = thisClass.getDeclaredMethods();
	           // Method thisMethod = thisClass.getDeclaredMethod(method_name, params);
	            Method thisMethod = null;
	            for(Method tm : ms){
		            // run the testAdd() method on the instance:
	            	if(tm.getName().equalsIgnoreCase(method_name)){
	            		thisMethod = tm;
	            		break;
	            	}
	        	}
	            if(thisMethod == null)
	            	throw new Exception("Method name "+method_name+" not found.");
	            Class<?>[] params = thisMethod.getParameterTypes();
	            int i = 0;
	        	for(Class<?> param : params){
	        		paramsObj[i] = paramMap.get(param.getName());
	        		i++;
	        	}
	        	respobj = (ResponseObject) thisMethod.invoke(instance, paramsObj);
	        	return processResponse();
	        }
	        catch (ClassNotFoundException e)  {
	        	 logger.error(e,e);
	        }
	        return "{\"errors\":\"found errors\"}";
	    }

		private void writeOutputZip(ResponseObject resp, boolean isdatahandler) throws Exception{
			FileOutputStream fp = null;
			if(isdatahandler ){
				if(resp.getDataHandler() == null)
					return;
				resp.setFilename(resp.getDataHandler().getName());
			}
			try{
				if(resp.getFilename() != null && !resp.getFilename().trim().isEmpty() ){
					
					logger.debug("zip file names "+ resp.getFilename()+" file path :"+file_path );
					String output_file = file_path + File.separator+FileUtils.getFileNameWithoutExtension(resp.getFilename()) +".zip";
					output_zip_file = output_file;
					File f = new File(output_file); 
					fp = new FileOutputStream(f);
					if(isdatahandler)
						resp.getDataHandler().writeTo(fp);
					else	
						fp.write(resp.getFileData(), 0, resp.getFileData().length);
					fp.flush();
				}else{
					logger.error("Complete record has no file name");
				}
				
			}catch(Exception e){
				logger.error(e,e);
			}finally{
				if(fp != null)
					fp.close();
			}

		}

}
