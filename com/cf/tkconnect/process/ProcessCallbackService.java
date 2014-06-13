package com.cf.tkconnect.process;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Map;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import com.cf.tkconnect.connector.TKConnector;
import com.cf.tkconnect.process.ProcessCallbackService;
import com.cf.tkconnect.process.ResponseObject;
import com.cf.tkconnect.util.InitialSetUp;
import com.cf.tkconnect.util.PropertyManager;


import static com.cf.tkconnect.util.InitialSetUp.callback_poll_server;
import static com.cf.tkconnect.util.InitialSetUp.max_thread_count;
import static com.cf.tkconnect.util.InitialSetUp.useCallbackList;
import static com.cf.tkconnect.util.WSConstants.ID_PRCOCESS_INCOMPLETE_CODE;
import static com.cf.tkconnect.util.WSUtil.getDateString;
import static com.cf.tkconnect.util.WSUtil.processCallbackResponse;

public class ProcessCallbackService {
	
	static com.cf.tkconnect.log.Log logger = com.cf.tkconnect.log.LogSource
									.getInstance(ProcessCallbackService.class);

    private static ExecutorService filepool;
    private String today ;
    String serverUrl ;
    String shortName;
	Timer timer;
	public ProcessCallbackService(){
		serverUrl = InitialSetUp.company.get("url");
		shortName = InitialSetUp.company.get("shortname");
		today = getDateString();
	}

	public void start(){
		
		 timer = new Timer();
		 logger.debug("Callback server time:"+callback_poll_server);
	     timer.schedule(new CallbackTask(),20000,callback_poll_server);
	     filepool =    Executors.newFixedThreadPool(max_thread_count);

		
	}
	
	
	public void stop(){
		
		 if(timer != null)
			 timer.cancel();
		
	}
	

	class CallbackTask extends TimerTask  {
		
		public void run() {
			
			Map<Integer,String> callbacklist = useCallbackList(false,0,null);
			logger.debug(" will query server for delayed responses list::"+callbacklist);
			if(callbacklist == null || callbacklist.size() == 0)
				return;
			//List<Runnable> calltasks = new ArrayList<Runnable>();
			for(int id : callbacklist.keySet()){
				 File file = new File(callbacklist.get(id));
				 logger.debug("delay list  id:"+id+" file:"+callbacklist.get(id));
				 if(file == null || !file.exists()) continue;
				 filepool.execute(new CallbackFileThread(file,id));
				 
			 }
			// process the callback services 
			// get the delay director for today
			
		}
		
		File getFile(File[] files, int id ){
			for(File file : files){
				 if( file.getName().indexOf("__"+id+"__") > 0)
					 return file;
			 }
			return null;
		}
		
	 }// end class DelayedThread
	
	class CallbackFileThread implements Runnable  {

		File file;
		int id;
		
		CallbackFileThread(File file, int id){
			this.file = file;
			this.id = id;
		}
		
		
		public void run() {
			try{
			TKConnector uc = new TKConnector(serverUrl);
			ResponseObject responseObj = uc.getIDResponse(InitialSetUp.company.get("shortname"),InitialSetUp.company.get("authcode"),""+id);
			if(responseObj.getStatusCode() == ID_PRCOCESS_INCOMPLETE_CODE)	
				useCallbackList(true,id,file.getAbsolutePath());
				// this gets it back in the loop
			else
				processCallbackResponse( responseObj,id,  file.getAbsolutePath(), file.getName(), today);
			}catch(Exception e){
				logger.error(e, e);
			}
		}
		
	}
	 
}
