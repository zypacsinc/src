package com.cf.tkconnect.util;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import com.cf.tkconnect.util.MailMessage;

public final class MailMessage
{
	static com.cf.tkconnect.log.Log logger = com.cf.tkconnect.log.LogSource
	.getInstance(MailMessage.class);
  	String Info=""; //used in case the Scheduler wants to give more information
  	//if the message fails.
	Map from; //we will have only one defa email sender
	List<Map> to;
	String subject;
	String contents;
	List<Map> bcc;
	List<Map> cc;
	String defaultbcc ="";
	boolean isHTML = false;
	List<Map> embedHTMLImages;
	List<Map> attachmentlist = new ArrayList<Map>();
	String fromName;
	String fromEmail;
	/*    all to,cc,bcc,from are comma or semicolon separated email addresses.
		the methods setFrom,setTo, setCC, setBCC expects comma separated emails addresses
		the methods getXXX return comma separated email addresses
		they get user emails by calling method:getUserEmailIds() and populate from,cc,bcc,to as comma
		 Since you have to convert Ids to email addresses from databse, the class requires WebProject
		object to instantiate.
		subject and contents are strings.
		 Map consists of elements emailid,firstname,lastname
	*/  
	
	

    public MailMessage(String info)
	{
			this.Info = info;
    }

	public void setFrom(Map from)
	{
		this.from = from;
		if(from == null) return;
		fromName = (String)from.get("firstname")+" "+(String)from.get("lastname") ;
		fromEmail= 	(String)from.get("email");
	}

	public void setFromName(String fromname)
	{
		fromName = fromname;
		
	}
	public void setFromEmail(String fromemail)
	{
		this.fromEmail = fromemail;
	}
	
	public Map getFrom()
	{
		return this.from;
	}

	public String getFromName()
	{
		return  fromName;
	}
	
	public String getFromEmail()
	{
		return fromEmail;
	}

	public void setFromIds(String fromIds)
	{
		List<Map> fromlist = getUserEmailIds(fromIds);
		if(fromlist  != null && fromlist.size()>  0) from = fromlist.get(0);
	}

	public void setTo(List<Map> to)
	{
		this.to = to;
	}
    public void addTo(Map tobean){
    	if(to == null) to = new ArrayList<Map>();
    	to.add(tobean);
    	logger.debug("AddTo :"+to.size()+":"+tobean);
    }
	public void setIsHTML(boolean isHTML)
	{
		this.isHTML = isHTML;
	}

	public boolean getIsHTML()
	{
		return this.isHTML ;
	}
	
	public List<Map> getTo()
	{
		return this.to;
	}
	public void addToEmail(String toemail)
	{
		addToDetails(0,"","",toemail);
	}
	public void addToDetails(int userid,String firstname,String lastname, String toemail)
	{
		if(to == null) to = new ArrayList<Map>();
		Map map = new HashMap();
		map.put("email",toemail);
		map.put("firstname",firstname);
		map.put("lastname",lastname);
		map.put("userid",userid);
		to.add(map);
	}

	//sets comma or semicolon separated email addresses.
	public void setToIds(String toIds)
	{
		this.to = getUserEmailIds(toIds);
	}

	//sets comma or semicolon separated email addresses.
	public void setCC(List<Map> cc)
	{
		this.cc = cc;
	}
	public void setCCIds(String ccIds)
	{
		this.cc = getUserEmailIds(ccIds);
	}

	public List<Map> getCC()
	{
		return this.cc;
	}

	public void setBCC(List<Map> bcc)
	{
		this.bcc = bcc;
	}

	public void addBCC(Map addBcc)
	{
		if (this.bcc==null)
			this.bcc = new ArrayList<Map>();
		this.bcc.add( addBcc);
	}

	public void setBCCIds(String bccIds)
	{
		this.bcc = getUserEmailIds(bccIds);
	}

	public List getBCC()
	{
		return this.bcc;
	}

	//sets comma or semicolon separated email addresses.
	public void setSubject(String subject)
	{
		this.subject=subject;
	}

	public String getSubject()
	{
		return this.subject;
	}


	//sets comma or semicolon separated email addresses.
	public void setContents(String contents)
	{
		this.contents=contents;
	}

	public String getContents()
	{
		return this.contents;
	}

	public void setInfo(String info)
	{
		this.Info=info;
	}

	public String getInfo()
	{
		return this.Info;
	}


	public void setAttachments(List<Map> list)
	{
		this.attachmentlist=list;
	}

	public List<Map> getHTMLImages()
	{
		return this.embedHTMLImages;
	}

	public void setHTMLImages(List<Map> embedHTMLImages)
	{
		this.embedHTMLImages = embedHTMLImages;
	}
	
	public List<Map> getAttachments()
	{
		return this.attachmentlist;
	}

	public void setMessage(List<Map> to, String subject, String contents)
	{
		this.to			=	to;
		this.subject	=	subject;
		this.contents	=	contents;
	}

	public void setMessage(List<Map> to, List<Map> cc, String subject, String contents)
	{
		this.to			= 	to;
		this.cc			=	cc;
		this.subject	=	subject;
		this.contents	=	contents;
	}

	public void setMessage(List<Map> to, List<Map> cc, List<Map> bcc, String subject, String contents)
	{
		this.to		= 	to;
		this.cc 	= 	cc;
		this.bcc		= 	bcc;
		this.subject	=	subject;
		this.contents	=	contents;
	}

	public void setMessage(Map from, List<Map> to, List<Map> cc, List<Map> bcc, String subject, String contents)
	{
		this.from 	= 	from;
		this.to		= 	to;
		this.cc 	=	cc;
		this.bcc		= 	bcc;
		this.subject	=	subject;
		this.contents	=	contents;
	}

	public void setMessageIds(String toIds, String subject, String contents)
	{
		setToIds(toIds);;
		this.subject	=	subject;
		this.contents	=	contents;
		
	}

	public void setMessageIds(String toIds, String ccIds, String subject, String contents)
	{
		setCCIds(ccIds);
		setMessageIds( toIds,  subject,  contents);
	}

	public void setMessageIds(String toIds, String ccIds, String bccIds, String subject, String contents)
	{
		setBCCIds(bccIds);
		setMessageIds( toIds,  ccIds,  subject,  contents);
	}

	public void setMessageIds(String fromIds, String toIds, String ccIds, String bccIds, String subject, String contents)
	{
		setFromIds(fromIds);
		setMessageIds( toIds,  ccIds,  bccIds,  subject,  contents);
	}

   // gets the list of user-emails, given comma separated user-ids.

   private List<Map> getUserEmailIds(String  userids){
	
	   List<Map> ulist = new ArrayList<Map>();
	   if(userids == null || userids.trim().length() == 0) return ulist;
	   java.util.StringTokenizer st = new java.util.StringTokenizer(userids,",");
	   if(st == null) return ulist;
	   while(st.hasMoreTokens()){
		   Map map = new HashMap();
		   map.put("email", (String)st.nextToken());
		   ulist.add(map);
	   }
	   
	   return ulist;
   }


} //class
