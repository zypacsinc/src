package com.cf.tkconnect.util;
import java.util.Properties;
import java.lang.Runnable;
import java.lang.Thread;
import java.util.Vector;
import java.util.Iterator;

import com.cf.tkconnect.util.MailHandler;
import com.cf.tkconnect.util.MailMessage;
import com.cf.tkconnect.util.MailUtils;
import com.cf.tkconnect.util.PropertyManager;


public final class MailHandler implements Runnable
{
    static org.apache.commons.logging.Log logger = 
    	org.apache.commons.logging.LogFactory.getLog(MailHandler.class);

	Vector<MailMessage> EmailQueue;
	boolean MailAvailable=false;
	String type;    
	String defaultbcc ="";
	int maxQueueSize=2000;
	Thread mailThread;
	Properties props;
	//populate the EmailQueue.
	//Start email thread..ie this.

	public MailHandler()
	{
		props = System.getProperties();;
	  try
		{
			String MailSize = PropertyManager.getProperty("email.queue.size", "1000");			
			maxQueueSize=(new Integer(MailSize)).intValue();
			logger.debug("maxQueueSize:" + maxQueueSize);
				
			//Initialize the vector EmailQueue
			EmailQueue = new Vector<MailMessage>();
			mailThread = new Thread(this,"MailThread");
			mailThread.start();
		}
		catch ( Exception e)
		{
		   logger.error("error while initializing email:" , e);
		}
	}
		
		
//This is consumer.
	public synchronized void putMail(Vector<MailMessage> EmailList)
	{
		if (EmailList.isEmpty())
		{
			logger.debug("EmailList is empty, not sending any mail");
			return;
		}		
		
		//Add given messages to EmailQueue email queue 
		int anticipatedSize=EmailList.size() + EmailQueue.size();		
		if (anticipatedSize < maxQueueSize)
		{
			EmailQueue.addAll(EmailList);		
			logger.debug("current size of EmailList:" + EmailList.size());
		}
		else
		{
			 //add element so that the size does not exceed 2000.		 
			  logger.info("EmailQueue size exceeding specified EmailQueue Max size:" + maxQueueSize + ", limiting it to " + maxQueueSize);
			  logger.debug("If the size is not limited, it could be: " + anticipatedSize + " emails in queue");
			  int noToBeAdded = maxQueueSize - EmailQueue.size();
			  Iterator it = EmailList.iterator();
			  for( int i=0; i < noToBeAdded ; i++)
			  {
			     EmailQueue.add((MailMessage)it.next());
			  }		  		  		  
		}        
		// notify consumer that mail is available		
		MailAvailable = true;
        notifyAll();
	}
    
	//consumer
	//gets email messages from EmailQueue
	public synchronized MailMessage getMail()
	{
	   MailMessage msg = null;
	   while (!MailAvailable) 
		{
           try 
		   {
              // wait for producer to put mail
               wait();
           } catch (InterruptedException e) 
		   {
           }
	    }//while									
		//Take next message     
		try 
		{   
			msg = (MailMessage) EmailQueue.firstElement();
			EmailQueue.remove(0);				
		}
		catch (Exception e)
		{
		}							
		if (EmailQueue.isEmpty())
			  MailAvailable = false;    			  
		notifyAll();
		return msg;
	}


	private int sendMessage(MailMessage skireMsg) 
	{
		try{
		
			return MailUtils.sendMailMessage(skireMsg);
		}
		catch(Exception me)
        {
		   //current server does not work, switch to next one.
		    //EmailTransport.close();					
        	logger.error("Connection/session Exception:", me);
        	logger.debug("continued to next server");
			//Put the current message into retry vector for future trial
			return 1;						
        }
	}	
	
	public void run ()
	{
	    logger.debug ("Thread started to send the email");
		MailMessage skireMsg;
		//connect to the the mail server
		while(true)
		{	
			//Vector EmailList = new Vector();
			//syncronzied method. It will wait till the email arrives in EmailQueue.
			skireMsg = getMail();
			
			int retVal = sendMessage(skireMsg);
			if (retVal==1) 		//mail could not be sent as server is down   
			{
			   //try again, if it fails at this time, it will be discarded.
	   		   sendMessage(skireMsg);   
			}
			
		}//while(true)
	} //run




} //class

