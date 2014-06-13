package com.cf.tkconnect.process;

import java.util.Map;
import java.util.concurrent.Callable;
import static org.apache.commons.lang3.math.NumberUtils.toLong;

import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;



	public class RunServiceThread extends RunWebServices implements Callable<ResponseObject> {

		static Log logger = LogSource.getInstance(RunServiceThread.class);

	    
		
		public RunServiceThread(Map<String,String> map){
			super(map);
		}
		
		  @Override
		public ResponseObject call() {
			  ResponseObject resp = null;
			  try{
				  resp =  runService(  this.paramMap.get("file_name"),   this.paramMap.get("file_path"));
				  
			  }catch(Exception e){
				  resp = new ResponseObject();
				  resp.setStatusCode(500);
				  String[] str = {"Error is getting web services response"};
				  resp.setErrorStatus(str);
			  }
			  resp.setSavedServiceId(toLong(this.paramMap.get("saved_service_id")));
			  return resp;
		} 
	 
		
	 
}
