package com.cf.tkconnect.util;


import static com.cf.tkconnect.util.FileUtils.*;
import static com.cf.tkconnect.util.InitialSetUp.attachzip;
import static com.cf.tkconnect.util.InitialSetUp.sendemail;
import static com.cf.tkconnect.util.InitialSetUp.sendemailoncomplete;
import static com.cf.tkconnect.util.InitialSetUp.senderrormail;
import static com.cf.tkconnect.util.InitialSetUp.useCallbackList;
import static com.cf.tkconnect.util.InitialSetUp.useftp;
import static com.cf.tkconnect.util.WSConstants.*;


import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.log.WebLinkLogLoader;
import com.cf.tkconnect.process.ResponseObject;
import com.cf.tkconnect.util.FileUtils;
import com.cf.tkconnect.util.MailUtils;
import com.cf.tkconnect.util.PropertyManager;
import com.cf.tkconnect.util.WSUtil;
import com.cf.tkconnect.util.ZipFiles;
import com.cf.tkconnect.util.xml.FileData;
import com.cf.tkconnect.util.xml.FileObject;
import com.cf.tkconnect.util.xml.XMLFileObject;
import com.cf.tkconnect.util.xml.XMLObject;

import java.io.File;
import static java.io.File.separator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ArrayList;
import java.util.Set; 
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.commons.io.IOUtils;

public class WSUtil  {

	static Log logger = LogSource.getInstance(WSUtil.class);

	private static boolean stop = false; 
	private static String CALLBACK_ID = "<callback_id>";
	
	public WSUtil() {
	}

	public static WSUtil getInstance() {
		return new WSUtil();
	}

	public static String getUrl(String inpurl) {
		if (inpurl == null)
			return "";
		StringBuilder buf = new StringBuilder();
		String url = inpurl.toLowerCase();
		if (url != null && !url.startsWith("http"))
			buf.append( "http://");
		buf.append(url);
		if (url != null && url.endsWith("/"))
			buf.append( "ws/services");
		else
			buf.append("/ws/services");
		logger.debug("getUrl :"+buf);
		return buf.toString();
	}

	public static String getDocUrl(String inpurl) {
		if (inpurl == null)
			return "";
		StringBuilder buf = new StringBuilder();
		String url = inpurl.toLowerCase();
		if (url != null && !url.startsWith("http"))
			buf.append( "http://");
		buf.append(url);
		if (url != null && url.endsWith("/"))
			buf.append( "ws/us/services");
		else
			buf.append("/ws/un/services");
		logger.debug("getDocUrl :"+buf);
		return buf.toString();
	}

	public static String getMainServiceUrl(String inpurl) {

		return (getUrl(inpurl) + "/mainservice");
	}

	public static String geDocumentServiceUrl(String inpurl) {

		return (getDocUrl(inpurl) + "/UnifierWebServices");
	}
	public static String getServiceUrl(String inpurl, String service) {
		
		return (getUrl(inpurl) + "/" + service);
	}
	
	public static String toXML(List list, String maintag, String subtag,
			boolean setmaintag, boolean setsubtag, String excludes,
			String supress, int indent) {
		return toXML(list, subtag, maintag, setmaintag, setsubtag, excludes,
				supress, indent, true);
	}

	public static String toXML(List list, String maintag, String subtag,
			boolean setmaintag, boolean setsubtag, String excludes,
			String supress, int indent, boolean setvalue) {
		return "";
	}

	public static String getIndent(int ct) {
		StringBuffer buf = new StringBuffer("");
		for (int i = 0; i < ct; i++)
			buf.append(" ");
		return buf.toString();

	}

	public static ResponseObject convertXMLObject(XMLFileObject xmlfileobj) {
		ResponseObject obj = new ResponseObject();
		obj.setErrorStatus(xmlfileobj.getErrorStatus());
		obj.setStatusCode(xmlfileobj.getStatusCode());
		obj.setXmlcontents(xmlfileobj.getXmlcontents());
		obj.setDataHandler(xmlfileobj.getDataHandler());
		return obj;
	}

	public static ResponseObject convertXMLObject(XMLObject xmlobj) {
		ResponseObject obj = new ResponseObject();
		obj.setErrorStatus(xmlobj.getErrorStatus());
		obj.setStatusCode(xmlobj.getStatusCode());
		obj.setXmlcontents(xmlobj.getXmlcontents());
		return obj;
	}

	public static ResponseObject convertXMLErrorObject(
			int code, String err) {
		String status[] = new String[1];
		status[0] = new String (err);
		ResponseObject obj = new ResponseObject();
		obj.setStatusCode(code);
		obj.setErrorStatus(status);
		return obj;
	}

	public static String getDateString() {
		GregorianCalendar gc = new GregorianCalendar();
		return MONTHS[gc.get(Calendar.MONTH)] + "-" + gc.get(Calendar.DATE)
				+ "-" + gc.get(Calendar.YEAR);
	}
	
	public static synchronized void setStop(boolean s) {
    	stop = s;
    }
    
	public static  void processResponse(ResponseObject responseObj, String documentPath,  String today, boolean callback) {
		processResponse(responseObj, documentPath, today, callback, false);
	}	
	public static  void processResponse(ResponseObject responseObj, String documentPath,  String today, boolean callback, boolean delayresponse) {
		int ind = documentPath.lastIndexOf(separator);
		int extInd = documentPath.lastIndexOf(".");
		String fName_noext = documentPath.substring(ind + 1, extInd);
		String fileName_withExt = documentPath.substring(ind + 1);
		String filepath = documentPath.substring(0,ind);
		try {
			if (responseObj != null) {
				int statusCode = responseObj.getStatusCode();
				logger.debug("Response code: " + statusCode);
				if (statusCode != 200) {
					processError(responseObj, fileName_withExt, today, statusCode , documentPath, delayresponse);
					return;
				}
				String recdXML = responseObj.getXmlcontents();
				if (recdXML != null && recdXML.length() > 0) {
					if(callback){
						processCallback(recdXML,fName_noext,today);
						return;
					}
					String outfilename = fName_noext + OUTPUT_FILENAME;
					logger.debug("Writing XML contents extracted from response object returned by the server to : "
									+ outfilename);
					WebLinkLogLoader.JobLogger
							.debug("Writing XML contents extracted from response object returned by the server to : "
									+ outfilename);
					//logger.debug("recdxml:"+recdXML);
					writeOutputFile(today + separator
							+ outfilename, recdXML);
					logger.debug("Finished writing received xml file.");
					WebLinkLogLoader.JobLogger
							.debug("Finished writing received xml file.");
					if (responseObj.getDataHandler() != null) 
						       processFiles(responseObj, documentPath, recdXML, fileName_withExt, fName_noext, today, delayresponse);
					else {// no files
						moveFiles(
								TempFileServiceBaseDirectory,
								SuccessFileServiceBaseDirectory
										+ separator + today,
								fileName_withExt);
					}
				} else {
					
					moveFiles(
							(delayresponse?filepath : TempFileServiceBaseDirectory),
							SuccessFileServiceBaseDirectory
									+ separator + today,
							fileName_withExt);
				}
				// move attachment files from temp to success
				String tempdirname = "_" + fileName_withExt;
				FileUtils.moveAttachmentDirs(FileUtils.TempFileServiceAttDirectory + File.separator + tempdirname, 
						FileUtils.SuccessFileServiceBaseDirectory + File.separator + today + File.separator + 
						WSConstants.ATTACHMENTS_FILE_DIRECTORY + File.separator + tempdirname);
				
		        String sendsuccessmail = PropertyManager.getProperty(SEND_EMAIL_ONSUCCESS,"no"); 
				if("yes".equals(sendsuccessmail))
					MailUtils.sendMailMessage("SMARTLINK success for file name:"+fileName_withExt,recdXML,"SMARTLINK",fileName_withExt, statusCode);
				
				if("yes".equals(PropertyManager.getProperty(SEND_EMAIL_ONCOMPLETE,"no")))
					MailUtils.sendMailMessage("SMARTLINK process is complete for file name:"+fileName_withExt,recdXML,"SMARTLINK process complete",fileName_withExt, statusCode);
			}	
			
		} catch (Exception e) {

			logger.error(e,e);
			WebLinkLogLoader.JobErrorLogger 
					.error("ProcessResponse() error for request file "
							+ fileName_withExt);
			WebLinkLogLoader.JobErrorLogger.error("ProcessResponse() error: "
					+ e.getMessage());
			try {
				moveFiles( TempFileServiceBaseDirectory,
						 ErrorFileServiceBaseDirectory
								+ separator + today, fileName_withExt);
				// move attachment files from temp to error
				String tempdirname = "_" + fileName_withExt;
				FileUtils.moveAttachmentDirs(FileUtils.TempFileServiceAttDirectory + File.separator + tempdirname, 
						FileUtils.ErrorFileServiceBaseDirectory + File.separator + today + File.separator + 
						WSConstants.ATTACHMENTS_FILE_DIRECTORY + File.separator + tempdirname);
			} catch (Exception ex) {
				logger.error(ex, ex);
			}
		}
	}

	private static void processError(ResponseObject responseObj, String fileName_withExt, String today, int statusCode,String documentPath,boolean delayresponse) throws Exception{
         
        
		WebLinkLogLoader.JobErrorLogger
		.error("Server returned error code: "
				+ statusCode + " for request file: "
				+ fileName_withExt);
		String[] err = responseObj.getErrorStatus();
		// add the error tags here
		StringBuilder buf = new StringBuilder();
		int ind = documentPath.lastIndexOf(separator);
		String filepath = documentPath.substring(0,ind);
		if (err != null && err.length > 0) {
			buf.append(" <error>\n");
			buf.append("  <status_code>").append(
					statusCode + "").append("</status_code>\n");
			for (int i = 0; i < err.length; i++) {
				if (err[i].trim().length() > 0) {
					logger.error(err[i]);
					WebLinkLogLoader.JobErrorLogger.error(err[i]);
					buf.append("    <message>").append(filter( err[i]) )
							.append("</message>\n");
				}
			}
			buf.append(" </error>\n");
			logger.debug("Moving error files from "
					+ (delayresponse?filepath :  TempFileServiceBaseDirectory)
					+ " to "
					+  ErrorFileServiceBaseDirectory
					+ separator + today);
			 moveErrorFiles(
					(delayresponse?filepath :  TempFileServiceBaseDirectory),
					 ErrorFileServiceBaseDirectory
							+ separator + today,
					fileName_withExt, buf.toString(), true);
			// move attachment files from temp to error
			String tempdirname = "_" + fileName_withExt;
			FileUtils.moveAttachmentDirs(FileUtils.TempFileServiceAttDirectory + File.separator + tempdirname, 
					FileUtils.ErrorFileServiceBaseDirectory + File.separator + today + File.separator + 
					WSConstants.ATTACHMENTS_FILE_DIRECTORY + File.separator + tempdirname);
			 
			 if("yes".equals(useftp)){
				 sendFileToFTPServer(ErrorFileServiceBaseDirectory
							+ separator + today+separator +fileName_withExt);
			 }
			
		}
		logger.debug("process error:"+senderrormail+" sendemail:"+sendemail);
		if("yes".equals(senderrormail))
			MailUtils.sendMailMessage("SMARTLINK error for file name:"+fileName_withExt,filter(buf.toString()),"Web Service",fileName_withExt, statusCode);
		if("yes".equals(PropertyManager.getProperty(SEND_EMAIL_ONCOMPLETE,"no")))
			MailUtils.sendMailMessage("SMARTLINK process is complete for file name:"+fileName_withExt,filter(buf.toString()),"SMARTLINK process complete",fileName_withExt, statusCode);
		if("yes".equals(sendemail) && SERVER_ERROR_CODE == statusCode){
			MailUtils.sendCFMailMessage("SMARTLINK error for file name:"+fileName_withExt,  filter(buf.toString()), fileName_withExt, statusCode, ErrorFileServiceBaseDirectory
					+ separator + today);
		}
	}


	public static  void processExtResponse(ResponseObject responseObj , String inputxml, List<String> failurelist, List<String> successlist, List<String> responselist) {
		try {
			if (responseObj != null) {
				String contents = "";
				int statusCode = responseObj.getStatusCode();
				logger.debug("processExtResponse Response code: " + statusCode);
				if (statusCode == OK_CODE){ 
					contents = responseObj.getXmlcontents();
					successlist.add(stripISOTags(inputxml));
					if (contents != null && contents.length() > 0) {
						contents = "<response>\n"+contents+"\n</response>\n";
						responselist.add(getProcessedContents(contents,inputxml));
					}
				}else{
					 contents = processExtError(responseObj,statusCode);
					 logger.debug("processExtResponse  in else before failurelist size::"+failurelist.size());
					 failurelist.add(getProcessedContents(contents,inputxml));
				}
			}	
		} catch (Exception e) {

			logger.error(e,e);
			WebLinkLogLoader.JobErrorLogger
					.error("ProcessResponse() error for request  ");
			WebLinkLogLoader.JobErrorLogger.error("ProcessResponse() error: "
					+ e.getMessage());
		}
	}
	public static  void processObjectResponse(ResponseObject responseObj , String inputxml, List<String> failurelist, List<String> successlist, Map inputMap) {
		try {
			if (responseObj != null) {
				String contents = "";
				int statusCode = responseObj.getStatusCode();
				logger.debug("processExtResponse Response code: " + statusCode);
				if (statusCode == OK_CODE){ 
					contents = responseObj.getXmlcontents();
					 String objectname = (String)inputMap.get(OBJECTNAME) ;
					 String filtercondition = (String)inputMap.get(FILTERCONDITION);	
					 if(objectname.equalsIgnoreCase("bp_info")){
						 // bp name is filter condition, we need to save this file check for prev version get a new version
					 }
					successlist.add(stripISOTags(inputxml));
					if (contents != null && contents.length() > 0) {
						contents = "<response>\n"+contents+"\n</response>\n";
		//				responselist.add(getProcessedContents(contents,inputxml));
					}
				}else{
					 contents = processExtError(responseObj,statusCode);
					 logger.debug("processExtResponse  in else before failurelist size::"+failurelist.size());
					 failurelist.add(getProcessedContents(contents,inputxml));
				}
			}	
		} catch (Exception e) {

			logger.error(e,e);
			WebLinkLogLoader.JobErrorLogger
					.error("ProcessResponse() error for request  ");
			WebLinkLogLoader.JobErrorLogger.error("ProcessResponse() error: "
					+ e.getMessage());
		}
	}

	private static String getProcessedContents(String contentsval, String inputxmlval){
		StringBuilder buf = new StringBuilder();
		String inputxml = stripISOTags(inputxmlval);
		String contents = stripISOTags(contentsval);
		int ind = inputxml.indexOf("<smartlink>");
		//logger.debug("in getProcessedContents contents::"+contents+" inputxml::"+inputxml+" ind::"+ind);
		if(ind  >= 0){
			buf.append("<smartlink>\n" );
			buf.append(contents).append("\n");
			buf.append(inputxml.substring(ind+7));
			buf.append("\n");
		}else{
			buf.append("<smartlink>\n" );
			buf.append(contents).append("\n");
			buf.append(inputxml).append("\n");
			buf.append("</smartlink>\n" );
		}
		//logger.debug("getProcessedContents buf::"+buf.toString());
		return buf.toString();
	}
	
	private static String stripISOTags(String inputxml){
		if(inputxml == null) return "";
		StringBuilder buf = new StringBuilder();
		int startindex = inputxml.indexOf("<?xml version");
		if(startindex < 0) return inputxml;
		int endindex = inputxml.indexOf("?>", startindex) ;
		if(endindex <= startindex){
			logger.error("stripISOTags found errors:"+inputxml);
			return inputxml;
		}
			// now strip this 
		buf.append(inputxml.substring(0, startindex));
		buf.append(inputxml.substring(endindex+2)); // skip ?>
		return buf.toString();
	}
	
	private static String processExtError(ResponseObject responseObj, int statusCode) throws Exception{
         
        
		WebLinkLogLoader.JobErrorLogger.error("Server returned error code: "+ statusCode + " for request external "		);
		String[] err = responseObj.getErrorStatus();
		// add the error tags here
		StringBuilder buf = new StringBuilder();
		if (err != null && err.length > 0) {
			buf.append(" <error>\n");
			buf.append("  <status_code>").append(
					statusCode + "").append("</status_code>\n");
			for (int i = 0; i < err.length; i++) {
				if (err[i].trim().length() > 0) {
					logger.error(err[i]);
					WebLinkLogLoader.JobErrorLogger.error(err[i]);
					buf.append("    <message>").append(err[i])
							.append("</message>\n");
				}
			}
			buf.append(" </error>\n");
			
		}
		return buf.toString();
	}

	
	private static void processFiles(ResponseObject responseObj,String documentPath,  String recdXML, String fileName_withExt, String fName_noext, String today,boolean delayresponse ) throws IOException{
		boolean ok = processAttachment( responseObj,  fName_noext,  today, documentPath, delayresponse);
		if (!ok) {
			 moveFiles(
					 TempFileServiceBaseDirectory,
					 ErrorFileServiceBaseDirectory
							+ separator + today,
					fileName_withExt);
			// move attachment files from temp to error
			String tempdirname = "_" + fileName_withExt;
			FileUtils.moveAttachmentDirs(FileUtils.TempFileServiceAttDirectory + File.separator + tempdirname, 
					FileUtils.ErrorFileServiceBaseDirectory + File.separator + today + File.separator + 
					WSConstants.ATTACHMENTS_FILE_DIRECTORY + File.separator + tempdirname);

			deleteOutputFile(documentPath);
			WebLinkLogLoader.JobErrorLogger
					.error("Error processing attachment for request file: "
							+ fileName_withExt
							+ "\n The following response from the server is not saved.");
			WebLinkLogLoader.JobErrorLogger.debug("recd xml:"+recdXML);
		} else {
					moveFiles(
							 TempFileServiceBaseDirectory,
							 SuccessFileServiceBaseDirectory
									+ separator
									+ today,
							fileName_withExt);
					// move attachment files from temp to success
					String tempdirname = "_" + fileName_withExt;
					FileUtils.moveAttachmentDirs(FileUtils.TempFileServiceAttDirectory + File.separator + tempdirname, 
							FileUtils.SuccessFileServiceBaseDirectory + File.separator + today + File.separator + 
							WSConstants.ATTACHMENTS_FILE_DIRECTORY + File.separator + tempdirname);
		}
		
	}

	
	private static void processCallback(String recdXML, String fName_noext, String today) throws IOException{
		
		int ind = recdXML.toLowerCase().indexOf(CALLBACK_ID);
		int id = 0;
		if(ind >= 0){
			String sub = recdXML.substring(ind+CALLBACK_ID.length());
			ind =  sub.indexOf("<");
			if(ind > 0) sub = sub.substring(0,ind);
			try{
				id = Integer.parseInt(sub);
			}catch(Exception e){}
			logger.debug("processCallback sub:"+sub+" id:"+id);
			if(id <= 0) return;
		}
		String outfilename = fName_noext + "__"+id+"__.xml";
		logger.debug("(CallBack)Writing XML contents extracted from response object returned by the server to : "
						+ outfilename);
		WebLinkLogLoader.JobLogger
				.debug("(CallBack)Writing XML contents extracted from response object returned by the server to : "
						+ outfilename);
		logger.debug("processCallback:"+recdXML);
			 copyFileContent(
					 TempFileServiceBaseDirectory+separator +fName_noext+".xml",
					 CallbackFileServiceBaseDirectory
							+ separator + today+separator +outfilename,
							recdXML, true);
	logger.debug("(CallBack)Finished writing received xml file:"+outfilename);
		WebLinkLogLoader.JobLogger
						.debug("(CallBack)Finished writing received xml file.");
			 
		// set this in the Map
		useCallbackList(true,id, CallbackFileServiceBaseDirectory
							+ separator + today+ separator +outfilename);
	}

	public static void processCallbackResponse(ResponseObject responseObj,int id, String filepathwithname, String outfilename, String today) throws IOException{

		String recdXML = responseObj.getXmlcontents();
		int status = responseObj.getStatusCode();
		logger.debug("(After CallBack)Writing XML contents extracted from response status:"+status+" value returned by the server to : "
						+ outfilename);
		WebLinkLogLoader.JobLogger
				.debug("(After CallBack)Writing XML contents extracted from response object returned by the server to : "
						+ outfilename);
		 if(status != OK_CODE){
			 if(status == INVALID_ID_CODE  )
				 recdXML = getErrorString(responseObj.getErrorStatus() );
			 copyFileContent(
					 filepathwithname,
					 ErrorFileServiceBaseDirectory
							+ separator + today+separator +outfilename,
							recdXML, true);
			 if("yes".equals(useftp)){
				 sendFileToFTPServer(ErrorFileServiceBaseDirectory
							+ separator + today+separator +outfilename);
			 }
		 }else{
			 copyFileContent(
					 filepathwithname ,
					 SuccessFileServiceBaseDirectory
								+ separator + today+ separator
								+ outfilename, recdXML, true );
			 if(recdXML != null || recdXML.trim().length() > 0)
				 writeOutputFile(outfilename, recdXML );
		 }
		 try{
		 if("yes".equals(sendemailoncomplete) )
			 MailUtils.sendMailMessage("smartlink Error"," Input file name:"+outfilename+" processing has completed."+" \nStatus:"+status+" \n"+ recdXML, "process complete",outfilename, status );
		 else if("yes".equals(senderrormail) &&  status != OK_CODE)
			 MailUtils.sendMailMessage("Errors occured for input file name:"+outfilename,"Status:"+status+" \n"+  recdXML, "process complete",outfilename, status);
		 }catch(Exception me){
			 logger.error(me,me);
		 }
		//logger.debug("(After CallBack)recdXML:"+recdXML);
		 logger.debug("(After CallBack)Finished writing received xml file:"+outfilename);
		WebLinkLogLoader.JobLogger
						.debug("(After CallBack)Finished writing received xml file.");
			 
	}
	
	public static void sendFileToFTPServer(String file) {
		List<String> files = new ArrayList<String>();
		files.add(file);
		sendFilesToFTPServer(files);
	}
	public static void sendFilesToFTPServer(List<String> files) {
		try{
			// get the put directory
			String dir =  PropertyManager.getProperty(PROPERTY_FTP_RESPONSE_DIRECTORY,"output");
			logger.debug("sending  attachments...to:"+dir);
//			if(ftpclient != null){
//				ftpclient.changeDir(dir);
//				ftpclient.putFiles(dir, files);
//				ftpclient.setBaseDirectory();
//			}
		 }catch(Exception me){
			 logger.error(me,me);
		 }
			
	}
	
	private static boolean processAttachment(ResponseObject responseObj, String fName_noext, String today,String documentPath,boolean delayresponse) {
		logger.debug("Begin Processing attachments...");
		WebLinkLogLoader.JobLogger.debug("Begin Processing attachments...");
		DataHandler attachHandler = responseObj.getDataHandler();
		String receivedfileName = attachHandler.getName();
		logger.debug("get the received(attachment)file name:"
				+ receivedfileName);
		if (receivedfileName == null) {
			logger.error("Could not get the attachment file name.");
			WebLinkLogLoader.JobErrorLogger
					.error("Could not get the attachment file name.");
			return false;
		} else {
			String fname =  ResponseFileServiceBaseDirectory
					+ separator + today + separator + fName_noext
					+ ATTACH_FILENAME;
			OutputStream os = null;
			try {
				File f = new File(fname);
				os = new FileOutputStream(f);
				attachHandler.writeTo(os);
				logger.debug("Finished saving attachment file with name: "
						+ fname);
				WebLinkLogLoader.JobLogger
						.debug("Finished saving attachment file with name: "
								+ fname);
				return true;
			} catch (IOException e) {
				logger.error("Error saving attachment file: "
						+ receivedfileName);
				logger.error(e, e);
				WebLinkLogLoader.JobLogger
						.error("Error saving attachment file: "
								+ receivedfileName);
				WebLinkLogLoader.JobLogger.error(e.getMessage());
				WebLinkLogLoader.JobErrorLogger
						.error("Error saving attachment file: "
								+ receivedfileName);
				WebLinkLogLoader.JobErrorLogger.error(e.getMessage());
				return false;
			} finally {
				try {
					if (os != null) {
						os.close();
					}
				} catch (Exception e) {
					logger.error(e, e);
				}
			}

		}
	}

	public static String getErrorString(String[] errors){
		StringBuilder buf = new StringBuilder();
		if(errors == null) return buf.toString();
		for(String error:errors)
			buf.append(error).append("\n");
		return buf.toString();
	}

	public static String getCallbackFileName(String filename, int id, boolean withext){
		if( filename== null  ) return "";
		int ind = filename.indexOf("__"+id+"__");
		if(ind < 0) return filename;
		String sub = filename.substring(0,ind);
		if(withext) sub = sub+".xml";
		return sub;
	}
	
    public static boolean stopped() {
    	return stop;
    }
    
    public static String filter(String value) {

        if (value == null)
            return (null);

        char content[] = new char[value.length()];
        value.getChars(0, value.length(), content, 0);
        StringBuilder result = new StringBuilder(content.length + 50);
        for (int i = 0; i < content.length; i++) {
        	//TW#22454 Need to support unicode charset for integration, so below check is commented.
      	  //if(content[i] > 127) continue;// remove non ascii chars
            switch (content[i]) {
            case '<':
                result.append("&lt;");
                break;
            case '>':
                result.append("&gt;");
                break;
            case '&':
                result.append("&amp;");
                break;
            case '"':
                result.append("&quot;");
                break;
            default:
                result.append(content[i]);
            }
        }
        return (result.toString());

    }

	public static boolean checkInputValue(String s, Map inputMap) {
		Set keySet = inputMap.keySet();
		if (!keySet.contains(s))
			return false;
		else {
			String v = (String) inputMap.get(s);
			if (v == null || v.trim().length() == 0)
				return false;
		}
		return true;
	}

	 public static int toInt(String val){
		 if(val == null || val.trim().length() == 0) return 0;
		 try{
			 return Integer.parseInt(val);
		 }catch(Exception e){
			 return 0;
		 }
	 }

	 
	 public static int toInt(String val, int defaultvalue){
		 if(val == null || val.trim().length() == 0) return defaultvalue;
		 try{
			 return Integer.parseInt(val);
		 }catch(Exception e){
			 return defaultvalue;
		 }
	 }
 
	 public static long toLong(String val){
		 if(val == null || val.trim().length() == 0) return 0;
		 try{
			 return Long.parseLong(val);
		 }catch(Exception e){
			 return 0;
		 }
	 }
	 public static FileObject[] getZipFileObject(String fullpath) 
			throws Exception 		{
		 FileObject[] files = new FileObject[1];
		 File f = new File(fullpath);
			logger.debug("getZipFileObject filename exis: " + f.exists() + ";filename name: " + f.getName());
			if(f.exists()){
				javax.activation.DataHandler handlers = new javax.activation.DataHandler(
						new javax.activation.FileDataSource(f.getAbsolutePath()));
				files[0] = new FileObject(handlers, f.getName());
			}
		 return files;
		 
		}
	 public static byte[] getByteData(DataHandler dataHandler)throws Exception{
		 final InputStream in = dataHandler.getInputStream(); 
		 byte[] byteArray=org.apache.commons.io.IOUtils.toByteArray(in);
		 return byteArray;
	 }
	 public static FileData[] getZipFileData(String fullpath) 
				throws Exception 		{
		 FileData[] files = new FileData[1];
			 File f = new File(fullpath);
				logger.debug("getZipFileData filename exis: " + f.exists() + ";filename name: " + f.getName());
				if(f.exists()){
					files[0] = new FileData(IOUtils.toByteArray( new FileInputStream(f)), f.getName());
				}
			 return files;
			 
	}
	public static FileObject[] getBPAttachments(String principalPath,
			String dirpath, List<String> filenames, String today, String iszipfilex, String zipfile) 
		throws Exception 
	{
		String iszipfile = "no";
		if (zipfile != null && zipfile.trim().length() > 0 )
			iszipfile = "yes";
		else if ( "no".equalsIgnoreCase(attachzip)) iszipfile = "no";

		logger.debug("iszipfile bb : " + iszipfile+"  attachzip:"+attachzip+"   filenames:" +filenames);
		File principalFile = new File(principalPath);

		List<String> allowedZips = new ArrayList<String>();
		for ( String ext : WSConstants.ALLOWED_ZIPS )
			allowedZips.add(ext);
		
		if (filenames == null || filenames.isEmpty())  
			return null;
		
		// Check if we have the right attachments
		FileObject[] files = new FileObject[1];
		boolean error = checkFiles( iszipfile,  zipfile,   principalFile, filenames,  today);
		if(error) return files;
		File tempzipfiledir = new File(FileUtils.TempFileServiceAttDirectory + File.separator + "_" + principalFile.getName());
		if ( !tempzipfiledir.exists() )
			tempzipfiledir.mkdirs();
		
		logger.debug("iszipfile: " + iszipfile + ";attachzip: " + attachzip);
		if ( "yes".equalsIgnoreCase(iszipfile) ){// move the zip file from request to temp dir
			logger.debug("FileUtils.InputFileServiceAttDirectory: " + FileUtils.InputFileServiceAttDirectory + ";tempzipfiledir.getAbsolutePath(): " + tempzipfiledir.getAbsolutePath());
			FileUtils.copyFiles(FileUtils.InputFileServiceAttDirectory,	tempzipfiledir.getAbsolutePath(), zipfile, zipfile, true);
			processZipFile(  zipfile,   principalFile,  tempzipfiledir, files,  today);
		}else{
			for (String filename : filenames)			// move the attachment files from request to temp dir
				FileUtils.copyFiles(FileUtils.InputFileServiceAttDirectory,	tempzipfiledir.getAbsolutePath(), filename, filename, true);
			// Zipping the attachment files
			if("no".equalsIgnoreCase(attachzip)){
				files = new FileObject[filenames.size()];
				int i = 0;
				for (String filename : filenames){
					File f = new File(tempzipfiledir.getAbsolutePath(),filename);
					logger.debug(" filename exis: " + f.exists() + ";filename name: " + filename);
					if(f.exists()){
						javax.activation.DataHandler handlers = new javax.activation.DataHandler(
								new javax.activation.FileDataSource(f.getAbsolutePath()));
						files[i] = new FileObject(handlers, f.getName());
					}
					i++; 
				}
			}else{// if create zip this is default
				File zippy = new File(FileUtils.TempFileServiceAttDirectory + File.separator + "_" + principalFile.getName() + ".zip");
				ZipFiles zf = new ZipFiles();
				zf.startZip(tempzipfiledir.getAbsolutePath(), zippy.getAbsolutePath());
				
				javax.activation.DataHandler handlers = new javax.activation.DataHandler(
						new javax.activation.FileDataSource(zippy.getAbsolutePath()));
				logger.debug("zip filename: " + zippy.getName() + ";handler name: " + handlers.getName());
				files[0] = new FileObject(handlers, zippy.getName());
				
			}
		}
		
		return files;
	}

	public static String getFileAttachments(String principalPath,
			String dirpath, List<String> filenames, String today, String zipfile) 
		throws Exception {
		String iszipfile = "yes";
		if (zipfile != null && zipfile.trim().length() > 0 )
			iszipfile = "yes";
		else if ( "no".equalsIgnoreCase(attachzip)) iszipfile = "no";
		if(logger.isDebugEnabled())
			logger.debug("iszipfile bb : " + iszipfile+"  attachzip:"+attachzip+"   filenames:" +filenames);
		File principalFile = new File(principalPath);

		List<String> allowedZips = new ArrayList<String>();
		for ( String ext : WSConstants.ALLOWED_ZIPS )
			allowedZips.add(ext);
		
		if (filenames == null || filenames.isEmpty())  
			return null;
		
		// Check if we have the right attachments
		FileObject[] files = new FileObject[1];
		boolean error = checkFiles( iszipfile,  zipfile,   principalFile, filenames,  today);
		if(error) return "";
		File tempzipfiledir = new File(FileUtils.TempFileServiceAttDirectory + File.separator + "_" + principalFile.getName());
		if ( !tempzipfiledir.exists() )
			tempzipfiledir.mkdirs();
		if(logger.isDebugEnabled()){
			logger.debug("iszipfile: " + iszipfile + ";attachzip: " + attachzip);
			logger.debug("FileUtils.InputFileServiceAttDirectory: " + FileUtils.InputFileServiceAttDirectory + ";tempzipfiledir.getAbsolutePath(): " + tempzipfiledir.getAbsolutePath());
		}
		return FileUtils.copyFiles(FileUtils.InputFileServiceAttDirectory,	tempzipfiledir.getAbsolutePath(), zipfile, zipfile, true);
		//processZipFile(  zipfile,   principalFile,  tempzipfiledir, files,  today);
		
	}

	private static void processZipFile( String zipfile,  File principalFile, File tempzipfiledir,FileObject[] files, String today) throws Exception{
		// Check for compressed file extension
		//String extension = "";
		int lastindex = zipfile.lastIndexOf(".");
		logger.debug("processZipFile zip zipfile: " +zipfile + "  lastindex: " + lastindex);
		if ( (lastindex == -1 ) || (lastindex == zipfile.length()-1) )
		{
			StringBuilder message = new StringBuilder();
			message.append("Empty compressed file extension");
			WebLinkLogLoader.JobErrorLogger.error(message.toString());
			logger.error(message.toString());
			String tempdirname = "_" + principalFile.getName();
			logger.debug("processZipFile zip tempdirname: " +tempdirname + "  FileUtils.TempFileServiceBaseDirectory: " + lastindex);
			FileUtils.copyFiles(FileUtils.TempFileServiceBaseDirectory, FileUtils.ErrorFileServiceBaseDirectory + File.separator + today, 
					principalFile.getName(), principalFile.getName(), true);
			logger.debug("processZipFile zip tempdirname: " +tempdirname + "  FileUtils.TempFileServiceBaseDirectory: " + lastindex);		
			FileUtils.moveAttachmentDirs(tempzipfiledir.getAbsolutePath(), 
					FileUtils.ErrorFileServiceBaseDirectory + File.separator + today + File.separator + 
					WSConstants.ATTACHMENTS_FILE_DIRECTORY + File.separator + tempdirname);
					logger.debug("processZipFile zip tempdirname: " +tempdirname + "  FileUtils.TempFileServiceBaseDirectory: " + lastindex);		
			throw new Exception(message.toString());
		}
		else
		{
			//extension = zipfile.substring(zipfile.lastIndexOf(".")+1);
			/*
			if ( !allowedZips.contains(extension.toLowerCase()) )
			{
				// Unsupported compressed file extension
				StringBuilder message = new StringBuilder();
				message.append("Unsupported compressed file extension: ").append(extension);
				SamrtlinkLogLoader.JobErrorLogger.error(message.toString());
				logger.error(message.toString());
				String tempdirname = "_" + principalFile.getName();
				FileUtils.copyFiles(FileUtils.TempFileServiceBaseDirectory, FileUtils.ErrorFileServiceBaseDirectory + File.separator + today, 
						principalFile.getName(), principalFile.getName(), true);
				FileUtils.moveAttachmentDirs(tempzipfiledir.getAbsolutePath(), 
						FileUtils.ErrorFileServiceBaseDirectory + File.separator + today + File.separator + 
						WSConstants.ATTACHMENTS_FILE_DIRECTORY + File.separator + tempdirname);
				throw new Exception(message.toString());
			}
			else
			{
			We dont care what the zip file extension is. Later realease we may open it up
			*/
				File att = new File(tempzipfiledir.getAbsolutePath() + File.separator + zipfile );
				logger.debug("Found the zip file in attachments: " + att.getAbsolutePath());
				javax.activation.DataHandler handlers = new javax.activation.DataHandler(
						new javax.activation.FileDataSource(att.getAbsolutePath()));
				files[0] = new FileObject(handlers, att.getName());
			//}
		}

	}
	
	private static boolean checkFiles(String iszipfile, String zipfile,  File principalFile, List<String> filenames, String today) throws Exception {
		boolean error = false;
		if ( "yes".equalsIgnoreCase(iszipfile) )
		{
			// Check for the existence of the zip file
			File zf = new File(FileUtils.InputFileServiceAttDirectory + File.separator + zipfile);
			logger.error("checking zip for:"+zf.getAbsolutePath());
			if ( !zf.exists() )	{
				StringBuilder message = new StringBuilder();
				message.append("The specified zip file: ").append(zipfile).append(" could not be found in the directory: ").append(
						FileUtils.InputFileServiceAttDirectory);
				WebLinkLogLoader.JobErrorLogger.error(message.toString());
				logger.error(message.toString());

				moveErrorFiles(TempFileServiceBaseDirectory, ErrorFileServiceBaseDirectory + separator + today,
						principalFile.getName(), message.toString(), true);
				
				throw new Exception(message.toString());				
			}
		}
		else
		{
			StringBuilder errorfiles = new StringBuilder();
			List<File> existingFiles = new ArrayList<File>();
			for (String filename : filenames) 
			{

				File srcAtt = new File(FileUtils.InputFileServiceAttDirectory + File.separator + filename);
				logger.error("checking att for:"+srcAtt.getAbsolutePath());
				if ( !srcAtt.exists() )
				{
					error = true;
					errorfiles.append(filename).append("; ");
					logger.error("not found checking att for:"+srcAtt.getAbsolutePath());
				}
				else
				{
					existingFiles.add(srcAtt);
				}
			}
			
			if ( error )
			{
				WebLinkLogLoader.JobErrorLogger.error("One or more attachments listed in the web request do not exist in the directory: " +
					FileUtils.InputFileServiceAttDirectory +" error files :"+errorfiles.toString() );
				logger.error("One or more attachments listed in the web request do not exist in the directory: " +
						FileUtils.InputFileServiceAttDirectory+" error files :"+errorfiles.toString() );

				// move attachment files from request attachments to error
				String tempdirname = "_" + principalFile.getName();
				for ( File existing : existingFiles )
				{
					copyFiles(FileUtils.InputFileServiceAttDirectory, FileUtils.ErrorFileServiceBaseDirectory + File.separator + today + 
						File.separator + WSConstants.ATTACHMENTS_FILE_DIRECTORY + File.separator + tempdirname, 
						existing.getName(), existing.getName(), true);
				}
				moveErrorFiles(
						 TempFileServiceBaseDirectory,
						 ErrorFileServiceBaseDirectory
								+ separator + today,
								principalFile.getName(),"One or more attachments listed in the web request do not exist in the directory: " +
								FileUtils.InputFileServiceAttDirectory+" error files :"+errorfiles.toString(), true);
				
				throw new Exception("One or more attachments listed in the web request do not exist in the directory: " +
						FileUtils.InputFileServiceAttDirectory+" error files :"+errorfiles.toString() );
				
			}			
		}
		return error;
	}
	
	public static boolean isBlankOrNull(String str){
		if(str == null || str.trim().length() == 0)
			return true;
		return false;
	}
	
	
	public static Date parseDate(String date, String format){
		if(date == null || date.length() == 0 || format== null)
			return null;
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try{
			Date dt = sdf.parse( date, new ParsePosition(0));
			return dt;
		}catch(Exception e){
			return null;
		}
	
	}
	
	 public static String jsfilter(String value) {

	        if (value == null)
	            return ("");

	        char content[] = new char[value.length()];
	        value.getChars(0, value.length(), content, 0);
	        StringBuffer result = new StringBuffer(content.length + 50);
	        for (int i = 0; i < content.length; i++) {
	            switch (content[i]) {
	            case '\'':
	            case '"':
	                result.append('\\');
	                result.append(content[i]);
	                break;
	            case '\r':
	            case '\n':
	                break;
	    		default:
	    			result.append(content[i]);
	    			break;
	            }
	        }
	        return (result.toString());

	    }
	 
	 public static String jsfilter2(String value) {

	        if (value == null)
	            return ("");

	        char content[] = new char[value.length()];
	        value.getChars(0, value.length(), content, 0);
	        StringBuffer result = new StringBuffer(content.length + 50);
	        for (int i = 0; i < content.length; i++) {
	            switch (content[i]) {
	            case '<':
	                result.append("&lt;");
	                break;
	            case '>':
	                result.append("&gt;");
	                break;
	            case '&':
	                result.append("&amp;");
	                break;
	            case '"':
	                result.append('\\');
	                result.append(content[i]);
	                break;
	            case '\\':
	                result.append('\\');
	                result.append(content[i]);
	                break;
	            case '\r':
	            case '\n':
	                break;
	    		default:
	    			result.append(content[i]);
	    			break;
	            }
	        }
	        return (result.toString());

	    }

	 public static String getExcelFileName(String filename){
		 if(isBlankOrNull(filename))
			 return "";
		 int index = filename.indexOf("unifier_");
		 if(index <=0)
			 return filename;
		 return filename.substring(index);
	 }
	 
} // end class
