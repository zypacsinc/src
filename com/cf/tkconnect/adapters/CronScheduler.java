
package com.cf.tkconnect.adapters;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerMetaData;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.cf.tkconnect.data.process.BatchServices;
import com.cf.tkconnect.database.SqlUtils;
import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.process.RunUserThreadService;

/**
 * This Example will demonstrate all of the basics of scheduling capabilities of
 * Quartz using Cron Triggers.
 * 
 * @author Bill Kratzer
 */
public class CronScheduler {
	static Log logger = LogSource.getInstance(CronScheduler.class);
    static SchedulerFactory sf = new StdSchedulerFactory();
    static Scheduler sched ;


    public static void start() throws Exception {

        logger.info("------- Initializing -------------------");
        sched = sf.getScheduler();
        // First we must get a reference to a scheduler

        logger.info("------- Initialization Complete --------");

        // job  will run every SAT at 10 am
        JobDetail job = newJob(BatchJob.class)
            .withIdentity("batchjob", "batchgroup")
            .build();
        
        CronTrigger trigger = newTrigger()
            .withIdentity("batchtrigger", "batchgroup")
            .withSchedule(cronSchedule("0 01 10 ? * SAT"))
            .build();
        
        Date ft = sched.scheduleJob(job, trigger);
        logger.info(job.getKey() + " has been scheduled to run at: " + ft
                + " and repeat based on expression: "
                + trigger.getCronExpression());

        logger.info("------- Starting Scheduler ----------------");

        // All of the jobs have been added to the scheduler, but none of the
        // jobs
        // will run until the scheduler has been started
        sched.start();

        logger.info("------- Started Scheduler -----------------");


 
        logger.info("------- Shutdown Complete -----------------");

        SchedulerMetaData metaData = sched.getMetaData();
        logger.info("Executed " + metaData.getNumberOfJobsExecuted() + " jobs.");

    }
    
    public static void  shutDown() throws Exception{
        sched.shutdown(true);
   	
    }
    class BatchJob implements Job {
    	List<Map<String,Object>> batchlist = new ArrayList<Map<String,Object>>();
        public BatchJob() {
        }

        public void execute(JobExecutionContext context)        throws JobExecutionException {
            // This job simply prints out its job name and the
            // date and time that it is running
            JobKey jobKey = context.getJobDetail().getKey();
            logger.info("BatchJob says: " + jobKey + " executing at " + new Date());
            // need to find all batches scheduled to run for the week
    		Connection conn = null;
    		PreparedStatement ps = null;
    		ResultSet rs = null;
    		
    		String sql = "select * from batch_services_log where schedule_status = ? "	;
    		logger.debug("BatchJob    :: "+sql );
    		StringBuilder buf = new StringBuilder("");
    		try {
    			conn = SqlUtils.getConnection();
    			ps = conn.prepareStatement(sql);
    			ps.setInt(1, 2);//weeks
    			rs = ps.executeQuery();
    			while(rs.next()){
    				//
    				Map<String,Object> map = new HashMap<String,Object>();
    				map.put("user_id", rs.getInt("user_id"));
    				map.put("company_id", rs.getInt("company_id"));
    				map.put("batch_id", rs.getLong("batch_id"));
    				batchlist.add(map);
    			}
			}catch(Exception e){
				logger.error(e,e);
			} finally {
				SqlUtils.closeResultSet(rs);
				SqlUtils.closeStatement(ps);
				SqlUtils.closeConnection(conn);
			}
    		runServices();
        }

        private void runServices(){
        	try{
        		for(Map<String,Object> map : batchlist){
        			int user_id = (Integer)map.get("user_id");
        			int company_id =  (Integer)map.get("company_id");
        			long batch_id = (Long)map.get("batch_id");
        			logger.debug("Run batch for "+batch_id);
        			RunUserThreadService rut =new  RunUserThreadService(user_id,company_id,batch_id);
        			rut.startServiceRequest();
        		}
        	}catch(Exception e){
        		
        	}
        }
    } 

    public static void main(String[] args) throws Exception {

    	CronScheduler example = new CronScheduler();
        example.start();
    }

}
