package com.cf.tkconnect.process;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.process.ProcessFileService;

import static com.cf.tkconnect.util.InitialSetUp.max_thread_count;

	public class ProcessFileService {

		static Log logger = LogSource.getInstance(ProcessFileService.class);

	    private static ExecutorService pool;
		private static boolean servicestart = false;
	    public static final int poolSize = max_thread_count;// max no to refresh at a time
		
	    public ProcessFileService( )  {
	    }
	 
		public static void init() throws Exception{
			if(!servicestart){
				pool = Executors.newFixedThreadPool(poolSize);
				servicestart = true;
			}
		}

		
	    public static void serve(Runnable runthread) {
		   
	      try {
	    	  init();
	    	  logger.info("file server pool to execute new thread.");
	          pool.execute(runthread);
	        
	      } catch (Exception e) {
			logger.error(e,e);
	        
	      }
	   }
	    
		public static void shutdown(){
			if(servicestart){
				pool.shutdown();
				servicestart = false;
			}
		}
	 
}
