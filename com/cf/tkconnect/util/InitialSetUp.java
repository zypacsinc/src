/*
 * Created on Sep 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.cf.tkconnect.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.commons.io.IOUtils;




import com.cf.tkconnect.adapters.CronScheduler;
import com.cf.tkconnect.adapters.fileservice.FileDrop;
import com.cf.tkconnect.admin.Company;
import com.cf.tkconnect.database.SqlUtils;
import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.log.WebLinkLogLoader;
import com.cf.tkconnect.process.ProcessCallbackService;
import com.cf.tkconnect.util.FileUtils;
import com.cf.tkconnect.util.PropertyManager;
import com.cf.tkconnect.util.InitialSetUp;
import com.cf.tkconnect.util.WSConstants;

import static com.cf.tkconnect.util.WSConstants.*;
import static com.cf.tkconnect.util.WSUtil.toInt;

/** 
 * @author cyril
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public final class InitialSetUp   {
	static Log logger = LogSource.getInstance(InitialSetUp.class);
	
	public static Map<String,String> company = new HashMap<String,String>();
 	public static  int Interval =60000;// default settings for polling the directory
 	public static String base_directory = "";
// 	public static String authKey = ""; //set in ConfigUI
// 	public static String url = ""; //set in ConfigUI
 	public static boolean useProxy = false; //set in ConfigUI
 	static FileDrop fileSource = null;
 	ProcessCallbackService callbackservicetask = null;
 	public static boolean isCallbackService = false; //set in dynamically 
 	public static boolean isSystemCallbackService = false; //set in ConfigUI
 	public static String servicestyle = DOCUMENT_SERVICES; //default service
 	public static  int max_thread_count = MAX_THREADS;// default settings
 	public static  int callback_poll_server = 30;//12000;// default 200 mins
 	
 	public static  Map<Integer,String> callbacklist = new HashMap<Integer,String>();// default 200 mins
 	public static String sendemail = "no"; //if timekarma needs to be sent an email
 	public static String sendemailoncomplete = "no"; //if timekarma needs to be sent an emai
 	public static String senderrormail = "no"; //
 	public static boolean nocallback = false;
 	//public static FTPFileService ftpservice;
 	public static String useftp;
 	public static String cronScheduler="no";
 	//public static Client ftpclient;
 	public static String basefilepath = ""; //base path for storing all processed reqd files
 	public static boolean isusermode = true;
 	public static int SOCKET_PORT = 0;
	public static String directoryService = "no";
	public static String writeerror = "yes"; //thsse 3 are only for writing the response for external files
	public static String writesuccess = "yes"; // usage only
	public static String writeresponse = "yes"; 
	public static int runningmode = TKLINK_EXTERNAL; //default setting for weblink running mode  is external 
	public static String isproxyset= "no";
	public static String attachzip= "yes";
	public static boolean started = false;
	public static boolean inprocess		= false;
	public static  int  cleanupinterval = 21600000; // 6 hours = 21600000 ms
	public static  int  syncinterval = 7; // 6 hours = 21600000 ms
	public static String appHome = "";;
	private static String database_alter ="no";
	public static boolean directoriesSet = false;
	
    public void init(String appHome) {  
    	
    	
    	this.appHome = appHome;
    	if(logger.isDebugEnabled())
    		logger.debug("connect in  starting with prop appHome ::"+appHome);    	
	//	PropertyManager.setPropsName(PROPERTY_FILENAME);
     /*   String callbackservicestr = PropertyManager.getProperty(PROPERTY_CALLBACK_RESPONSE);
        sendemail = PropertyManager.getProperty(SEND_TK_EMAIL,"no");
        sendemailoncomplete = PropertyManager.getProperty(SEND_EMAIL_ONCOMPLETE,"no");
        senderrormail = PropertyManager.getProperty(SEND_EMAIL_ONERROR,"no");
         if(callbackservicestr != null && callbackservicestr.equals("yes")) isSystemCallbackService = true;
        isCallbackService = isSystemCallbackService;
      writesuccess = PropertyManager.getProperty(PROPERTY_WRITE_ONSUCCESS,"yes");
        writeerror	 = PropertyManager.getProperty(PROPERTY_WRITE_ONERROR,"yes"); 
        writeresponse = PropertyManager.getProperty(PROPERTY_WRITE_RESPONSE,"yes");
      */
        String interval = PropertyManager.getSysProperty(PROPERTY_SCAN_INTERVAL); 
        syncinterval = toInt(PropertyManager.getSysProperty(UNIFIER_SYNC_INTERVAL),7); 
           attachzip = PropertyManager.getProperty(PROPERTY_ATTACH_ZIP,"yes");
   cronScheduler = PropertyManager.getSysProperty(CRON_JOB_SCHEDULER,"no");
        directoryService = PropertyManager.getProperty(PROPERTY_DIRECTORY_SERVICE,"no");
       database_alter = PropertyManager.getCustomProperty("tkconnect.database.alter","no");
       
        // start of the cron scheduler
        if("yes".equals(cronScheduler))
			try{
				CronScheduler.start();
			 }catch(Exception e){ logger.error(e,e);}
				 
		if("yes".equals(PropertyManager.getProperty(PROPERTY_ISPROXYSET,"no")))
			useProxy  = true;
	

        if("no".equals(PropertyManager.getProperty(PROPERTY_NO_CALLBACK,"no"))){ // set for backward compatibilty
        		isCallbackService = false;
        		nocallback = true;;
        }
        
			String socketport = PropertyManager.getProperty(PROPERTY_SOCKET_PORT,"9000");
        	SOCKET_PORT =toInt(socketport,9000);
        
       
	    if (interval != null) {
	    		Interval = toInt(interval);
	    		if(Interval < MIN_INTERVAL) 
	    			Interval = MIN_INTERVAL;
	    }

		base_directory = PropertyManager.getProperty(PROPERTY_BASE_DIRECTORY,"");
		basefilepath = PropertyManager.getProperty(PROPERTY_WEB_FILE_ROOT_PATH,"");
		// get all company details only first time from it will come from database
		Company co = new Company(1);
		company.put("company_name", co.getCompanyName());
		company.put("company_url", PropertyManager.getProperty(COMPANY_URL));
		company.put("shortname", PropertyManager.getProperty(COMPANY_SHORTNAME));
		company.put("authcode", PropertyManager.getProperty(COMPANY_AUTHCODE));
		company.put("file_location",basefilepath);
		company.put("dir_location", base_directory);
		if(logger.isDebugEnabled())
			logger.debug("in standalonemode setting dir_location:::::"+company.get("dir_location")+" url::"+company.get("url"));
		// process this from the UI  on startup
		//setUpDirectories();
		
/*	    if(useProxy) {
        	setProxy();
        }else
        	unsetProxy();
 */       
		if(base_directory != null && base_directory.trim().length() > 0 && 
				basefilepath != null &&basefilepath.trim().length() > 0 )
					setUpDirectories();
		
		this.setUpDatabase();
		// clean up custom properties
		if(logger.isDebugEnabled())
			logger.debug("clean up prop customs:");
		cleanUpCustomProperties();
		
    }
    
    
    public static void setUpDirectories(){
    	if(directoriesSet)
    		return;
    	try{
    		
			FileUtils.checkAndCreateFileDirectorySystem(base_directory);
			createCompanyDirectory();
			if("yes".equals(directoryService))
				startPolling();
			directoriesSet = true;
		}catch(IOException ioe){
			logger.error(ioe,ioe);
		
	    }catch(Exception e){
			logger.error(e,e);
		}
    }
    
    private void cleanUpCustomProperties(){
    	String filepath = this.appHome+"/WEB-INF/classes/tkconnect_custom.properties"	;
     	File f = new File(filepath);
       	if(logger.isDebugEnabled())
    			logger.debug("in cleanUpCustomProperties setting filepath:::::"+filepath+" ::"+f.exists());
       	if(f.exists()){
    		try{
	    		FileOutputStream output = new FileOutputStream(f);
	    		IOUtils.write("", output);
    		}catch(Exception e){
    			logger.error(e,e);
    		}
    	}
    }
    
    public static String createCompanyDirectory() throws IOException{
		String dirLocation = InitialSetUp.basefilepath+File.separator+"unifier";
		if(logger.isDebugEnabled())
			logger.debug("in createCompanyDirectory setting dir::::"+dirLocation);
		File f = new File(dirLocation);
		if(!f.exists())
			f.mkdirs();
		f = new File(dirLocation+File.separator+"bp_info");
		if(!f.exists())
			f.mkdirs();
		f = new File(dirLocation+File.separator+"bp_data");
		if(!f.exists())
			f.mkdirs();
		f = new File(dirLocation+File.separator+"shell_info");
		if(!f.exists())
			f.mkdirs();
		f = new File(dirLocation+File.separator+"sync");
		if(!f.exists())
			f.mkdirs();
		return dirLocation;
	}

    public void setUpDatabase() {
    	//process datascource.
    	logger.info("setting up database details  ::"+database_alter);
    	Connection con = null;
    	
    	try{
    		con =SqlUtils.getConnection();
    		if(con == null)
    			throw new Exception("Could not connect to Database");
    		boolean dbexists = setCompanyData(con);
    		if(dbexists )
    			if( !"yes".equals(database_alter))
    				return; // found & set company
    			
    		// you need to create from sqlscript
    		logger.info("Creating & setting up sql table & scripts-------------------"+dbexists+" ::"+database_alter);
    		List<String> list = getSqlScript(dbexists);
        	createTables(list,con);
        	setCompanyData(con);
    		
    	}catch(Exception e){
    		logger.error(e,e);
    	}finally{
    		logger.info("finished setting up database details");
    		
    		SqlUtils.closeConnection(con);
    	}
    	
    }
    private List<String> getSqlScript(boolean dbexists) throws Exception{
    	String alterfilename ="";
    	InputStream   in = null;
    	if(!dbexists)
    		in = getClass().getClassLoader().getResourceAsStream("/tkconnect.sql");
    	else{
	    	if("yes".equals(database_alter)){
	    		alterfilename = PropertyManager.getCustomProperty("tkconnect.database.alter_filename","");
	    		logger.info("Creating & setting up sql table & scripts-------------------"+alterfilename);
	    		if(alterfilename != null && alterfilename.trim().length() > 0)
	    			in = getClass().getClassLoader().getResourceAsStream("/"+alterfilename);
	    	    
	    	}
    	}
    	if(in == null)
    		throw new Exception("Invalid file name "+alterfilename);
//    	InputStream   in = getClass().getClassLoader().getResourceAsStream("/tktemp.sql");
		List<String> lines =IOUtils.readLines(in);
		List<String> sqlscripts = new ArrayList<String>();
    	StringBuilder buf = new StringBuilder();
    	for(String line : lines){
    		if(line == null || line.trim().length()== 0 || line.startsWith("--"))
    			continue;
    		if(line.endsWith(";")){
    			buf.append(line.trim());
    			sqlscripts.add(buf.toString());
    			 buf = new StringBuilder();
    		}else
    			buf.append(line.trim());
    		
    	}
    	if(!dbexists){
    	// company insert
    		String sql = "INSERT INTO company (company_name,registry_prefix, shortname, authcode,company_url) " +
    				"VALUES ('Name', 'unifier', '"+company.get("shortname")+"','"+company.get("authcode")+"', '"+company.get("company_url")+"');";
    		sqlscripts.add(sql);
    	}
    	return sqlscripts;
    }
    private void createTables(List<String> sqllist, Connection conn) throws Exception{
        /* Creating a statement object that we can use for running various
         * SQL statements commands against the database.*/
        PreparedStatement ps = null;
     
        try{
            for(String sql : sqllist){
            	if(sql.endsWith(";"))
            		sql = sql.substring(0,sql.length()-1);
            	//System.out.println("Sql --:"+sql);
            	ps = conn.prepareStatement(sql);
            	int up = ps.executeUpdate();
            	if(logger.isDebugEnabled())
            		logger.debug("executed sql :"+sql);
            	ps.close();
            }
        }catch(SQLException se){
        	se.printStackTrace();
        	
        }finally{
        	if(ps != null)
        		ps.close();
        	logger.info("sql is created");
        }
    }

    private boolean setCompanyData(Connection con){
    	Statement ps = null;
    	ResultSet rs = null;
    	try{
			ps = con.createStatement();
    		rs = ps.executeQuery("select * from company ");
    		if(rs.next()){
    			company.put("company_id",  rs.getInt("company_id")+"");
    			company.put("shortname",  rs.getString("shortname"));
    			company.put("authcode",  rs.getString("authcode"));
    			company.put("company_url",  rs.getString("company_url"));
    			//company.put("company_name",  rs.getString("company_name"));
    			company.put("dir_location",  rs.getString("dir_location"));
    			company.put("file_location",  rs.getString("file_location"));
    			company.put("registry_prefix",  rs.getString("registry_prefix"));
    			return true;
    		}
		}catch(SQLException se){
			logger.error(se,se);
			return false;
			
		}finally{
			SqlUtils.closeResultSet(rs);
    		SqlUtils.closeStatement(ps);
		}
    	return false;
    }
    
  public static void startPolling() throws Exception{
	  if(directoryService.equalsIgnoreCase("yes")){
		  	if(logger.isDebugEnabled())
		  		logger.debug("startPolling  started polling directory ");
		    fileSource = new FileDrop();
			fileSource.init(Interval);
			fileSource.activate();
	  }
  }
    
    public static void stopPolling() {
    	if(logger.isDebugEnabled())
	  		logger.debug("stopPolling  stop polling directory ");
//		if(runningmode !=TKLINK_EXTERNAL) 
    	if(fileSource != null)
			fileSource.deActivate();
//		if(runningmode!=TKLINK_STANDARD)		
//			callbackservicetask.stop();
//		if("yes".equals(useftp) && ftpservice != null){
//			ftpservice.stop();
//		}
    }
    
    public static synchronized Map<Integer,String> useCallbackList(boolean set, int setvalue, String directory){
		Map<Integer,String> dlist = new HashMap<Integer,String>();
    	if(set){
    		callbacklist.put(setvalue,directory);
    	}else{// its consume
    		for( int key : callbacklist.keySet() )
    			dlist.put(key,callbacklist.get(key));
    		
    		callbacklist.clear();
    	}
    	return dlist;
    }
    
    private static void setProxy() {
    	String proxyHost = PropertyManager.getProperty(PROPERTY_PROXYHOST);
    	if(proxyHost == null || proxyHost.trim().length() == 0 ) 
    		return;
    	String proxyPort = PropertyManager.getProperty(PROPERTY_PROXYPORT);
    	if(proxyPort == null || proxyPort.trim().length() == 0) {
    		proxyPort = "80";
    	}
    	try {
	    	System.setProperty("http.proxyHost",proxyHost.trim());
	    	System.setProperty("http.proxyPort",proxyPort.trim());
    	}
    	catch(SecurityException e) {
    		WebLinkLogLoader.getLogger(InitialSetUp.class).error(e.getMessage());
    	}
    
    }
    private static void unsetProxy() {
    	try {
	    	System.clearProperty("http.proxyHost");
	    	System.clearProperty("http.proxyPort");
    	}
    	catch(SecurityException e) {
    		WebLinkLogLoader.getLogger(InitialSetUp.class).error(e.getMessage());
    	}
    
    }
    
    /**
     * Thread that periodically cleans up the request/attachments directory.
     */
    class SyncTask extends TimerTask
    {
    	@Override
    	public void run() 
    	{
    		if(logger.isDebugEnabled())
    			logger.debug("SyncTask thread started at: " + new java.util.Date());
    		// first check if service is running
    		while(inprocess){
    			try{
    				Thread.sleep(1000*60*60*24*syncinterval);
    				//call syncup
    				
    			}catch(InterruptedException ie){}
    		}
    	}
    }

    class CleanupTask extends TimerTask
    {
    	@Override
    	public void run() 
    	{
    		if(logger.isDebugEnabled())
    			logger.debug("Cleanup thread started at: " + new java.util.Date());
    		// first check if service is running
    		int count = 0;
    		while(inprocess){
    			try{
    				Thread.sleep(1000*60*60);
    				count++;
    				if(count > 10){
    					if(logger.isDebugEnabled())
    						logger.debug("Cleanup thread exited at: " + new java.util.Date());
    					return;// too many tries do it nexttinme
    				}
    			}catch(InterruptedException ie){}
    		}
    		// Clean up the files based on time calculation
    		File reqatt = new File(FileUtils.InputFileServiceAttDirectory);
    		File[] files = reqatt.listFiles();
    		long currentTime = System.currentTimeMillis();
    		for ( File f : files )
    		{
    			long modifiedTime = f.lastModified();
    			// file older than 5 hours (18000000 ms)
    			if(logger.isDebugEnabled())
    				logger.debug("currentTime - modifiedTime: " + (currentTime - modifiedTime));
    			if ( Math.abs(modifiedTime - currentTime) > 18000000 )
    			{
        			logger.info("Deleting file: " + f.getAbsolutePath());
        			f.delete();    				
    			}
    		}
    		if(logger.isDebugEnabled())
    			logger.debug("Cleanup thread stopped at: " + new java.util.Date());
    	}
    }

}
