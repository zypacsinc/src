package com.cf.tkconnect.util;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Vector;
import java.util.Map;

import com.cf.tkconnect.mail.EmailException;
import com.cf.tkconnect.mail.HtmlEmail;
import com.cf.tkconnect.util.MailHandler;
import com.cf.tkconnect.util.MailMessage;
import com.cf.tkconnect.util.MailUtils;
import com.cf.tkconnect.util.PropertyManager;

import java.io.File;

import static com.cf.tkconnect.util.WSConstants.INTEG_CF_EMAIL;
import static com.cf.tkconnect.util.WSConstants.COMPANY_URL;
import static com.cf.tkconnect.util.WSConstants.COMPANY_SHORTNAME;


public final class MailUtils 
{
	static com.cf.tkconnect.log.Log logger = com.cf.tkconnect.log.LogSource
  	.getInstance(MailUtils.class);
    public static String HOST = PropertyManager.getProperty("smartlink.mailserver.host","");
	private static MailHandler mailHandler;
	private static boolean initiated=false;
	private static String EMAIL_HOST_NAME ="";
	private static int EMAIL_HOST_PORT = 25;
    public static String HOST_EMAIL_ID = PropertyManager.getProperty("smartlink.host.email.id","");
    public static String SEND_EMAIL = PropertyManager.getProperty("smartlink.send.email","no");
    public static String COMPANY_NAME =  PropertyManager.getProperty("smartlink.company.name", PropertyManager.getProperty(COMPANY_SHORTNAME) );
	//initializes and starts the consumer which sends the mail.
	public static void Init()
	{
					
		if(HOST== null || HOST.trim().length() == 0) return;
		
		if(HOST.indexOf(":") > 0  ){
			EMAIL_HOST_NAME = HOST.substring(0,HOST.indexOf(":"));
			try{
				EMAIL_HOST_PORT = Integer.parseInt(HOST.substring(HOST.indexOf(":")+1));
			}catch(Exception e){}
		}else{
			try{
				EMAIL_HOST_PORT = Integer.parseInt(PropertyManager.getProperty("mailserver.port","25"));
			}catch(Exception e){}
			
		}
		if(EMAIL_HOST_PORT <=0 )EMAIL_HOST_PORT = 25;	
		mailHandler = new MailHandler();	 
	    logger.info("Mail Handler instantiated host:"+EMAIL_HOST_NAME+" port:"+EMAIL_HOST_PORT); 
	    initiated = true; 
	   
	}

	public static void sendMail(Vector<MailMessage> EmailList)
	{
		if(!"yes".equals(SEND_EMAIL)){
			logger.info("Do not send Email. ");
			return;
		}
		if (!initiated) 
		{
		   Init();
		}
		
		if (EmailList.size()==0 )
		{
			logger.info("EmailList is empty, not sending any mail");
			return;
		}
		//
		mailHandler.putMail(EmailList); 
	}

	
	

	public static int sendMailMessage(MailMessage msg) throws EmailException,MalformedURLException{
		// send HTML mail
		if(!doMail()) return 0;
		HtmlEmail email = new HtmlEmail();
		email.setHostName(EMAIL_HOST_NAME);
		email.setSmtpPort(EMAIL_HOST_PORT);
		List<Map> tolist = msg.getTo();
		for(Map tobean : tolist){
			logger.debug("sending email to:"+(String)tobean.get("email")+" name:"+(String)tobean.get("firstname")+" "+(String)tobean.get("lastname"));
			email.addTo((String)tobean.get("email"), (String)tobean.get("firstname")+" "+(String)tobean.get("lastname"));
		}
		email.setFrom(msg.getFromEmail(), msg.getFromName() );
		email.setSubject(msg.getSubject());
		email.setMsg(msg.getContents());
	
		List<Map> filelist =  msg.getAttachments();
		// add the attachmentet
		email.attach(filelist);
	
		// send the email
		email.send();
		return 0;
	}

	public static int sendMailMessage(String subject, String contents,String module, String filename, int status) throws EmailException,MalformedURLException{
		if(!doMail()) return 0;
		HtmlEmail email = new HtmlEmail();
		email.setHostName(EMAIL_HOST_NAME);
		email.setSmtpPort(EMAIL_HOST_PORT);
		logger.debug("sending email to:"+HOST_EMAIL_ID);
		email.addTo(HOST_EMAIL_ID, "");
		email.setFrom(HOST_EMAIL_ID, module );
		email.setSubject(subject);
		email.setMsg( createToEmailContents( contents,status,filename )  );
	
		// send the email
		email.send();
		return 0;

	}
	public static int sendCFMailMessage(String subject, String contents,String filename, int status, String attachment) throws EmailException,MalformedURLException{
		if(!doMail()) return 0;
		
		HtmlEmail email = new HtmlEmail();
		email.setHostName(EMAIL_HOST_NAME);
		email.setSmtpPort(EMAIL_HOST_PORT);
		logger.debug("sending email to:"+HOST_EMAIL_ID);
		//email.addTo(SUPPORT_SKIRE_EMAIL, "");
		email.addTo(INTEG_CF_EMAIL, "");
		email.setFrom(HOST_EMAIL_ID, "" );
		email.setSubject(subject);
		email.setMsg(  createSkireEmailContents(contents , status,filename )  );
		// send the email
		
		email.attach(filename,attachment+File.separator+filename,"");

		email.send();
		return 0;

	}
	
	private static String createToEmailContents(String contents, int status, String filename)
	{
		StringBuilder ms = new StringBuilder();			
		ms.append("<html><head><title>smartlink Email</title>");
		ms.append("<meta http-equiv=Content-Type content=text/html; charset=iso-8859-1>");
		ms.append("</head><body bgcolor=#FFFFFF text=#000000>");
		ms.append("<table cellpadding='0' cellspacing='0'>");
		ms.append("<tr><td><font face=Arial size=2.5 color='#000000'>service status</td></tr><tr><td>&nbsp;</td></tr>");
		ms.append("<BR>Status: <b>");
		ms.append(status).append("</b></font></td></tr>");
		ms.append("<tr><td>&nbsp;</td></tr>");							
		ms.append("<tr><td><font face=Arial size=2.5 color='#000000'>File name: <b>");
		ms.append(filename).append(" ").append("</b>");
		ms.append("<tr><td>&nbsp;</td></tr>");	
		
		ms.append("<tr><td><font face=Arial size=2.5 color='#000000'>From: smartlink ");
		ms.append("<BR>Sent For: <b>").append(" status report").append("</b>");
		ms.append("<tr><td>&nbsp;</td></tr>");	
				
		ms.append("<tr><td>&nbsp;</td></tr>");					
		ms.append("<tr><td><font face=Arial size=2.5 color='#000000'>");
		ms.append(contents).append("</font></td></tr>");
		ms.append("</table></body></html>");	
		logger.debug("to email content: " + ms.toString());							
		return ms.toString();											
	}

	private static String createSkireEmailContents(String contents, int status, String filename)
	{
		StringBuilder ms = new StringBuilder();			
		ms.append("<html><head><title>smartlink Email</title>");
		ms.append("<meta http-equiv=Content-Type content=text/html; charset=iso-8859-1>");
		ms.append("</head><body bgcolor=#FFFFFF text=#000000>");
		ms.append("<table cellpadding='0' cellspacing='0'>");
		ms.append("<tr><td><font face=Arial size=2.5 color='#000000'>Status Email</td></tr><tr><td>&nbsp;</td></tr>");
		ms.append("<BR>Status: <b>");
		ms.append(status).append("</b></font></td></tr>");
		ms.append("<tr><td>&nbsp;</td></tr>");							
		ms.append("<tr><td><font face=Arial size=2.5 color='#000000'>File name: <b>");
		ms.append(filename).append(" ").append("</b>");
		ms.append("<tr><td>&nbsp;</td></tr>");	
		
		ms.append("<tr><td><font face=Arial size=2.5 color='#000000'>From: smartlink ");
		ms.append("<BR>Sent For: <b>").append(" review ").append("</b>");
		ms.append("<tr><td>&nbsp;</td></tr>");	
		ms.append("<BR>Sent From: <b>").append( COMPANY_NAME ).append("</b>");
		ms.append("<tr><td>&nbsp;</td></tr>");	
		ms.append("<BR>Service sent to URL: <b>").append(PropertyManager.getProperty(COMPANY_URL) ).append("</b>");
		ms.append("<tr><td>&nbsp;</td></tr>");	
				
		ms.append("<tr><td>&nbsp;</td></tr>");					
		ms.append("<tr><td><font face=Arial size=2.5 color='#000000'>");
		ms.append(contents).append("</font></td></tr>");
		ms.append("</table></body></html>");	
		logger.debug("to email content: " + ms.toString());							
		return ms.toString();											
	}
	
    private static boolean doMail(){
		if(!"yes".equals(SEND_EMAIL)){
			logger.info("Do not send Email. ");
			return false;
		}
		if (!initiated) 
		{
		   Init();
		}
    	return initiated;
    }
} //class