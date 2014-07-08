package com.cf.tkconnect.unifierservices;


import java.io.File;
import java.io.FileOutputStream;

import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import static org.apache.commons.lang3.math.NumberUtils.toInt;
import org.w3c.dom.NodeList;


import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.process.ResponseObject;
import com.cf.tkconnect.util.xml.FileData;

public class UnifierServices {

	static Log logger = LogSource.getInstance(UnifierServices.class);
	String url;
	String method_name;
	boolean hasfiles = false;;
	
	public UnifierServices(String url, String method_name){
		this.url = url+"/ws/un/services/UnifierWebServices";
		this.method_name = method_name;
	}
	public UnifierServices(String url, String method_name, boolean hasfiles){
		this.url = url+"/ws/un/services/UnifierWebServices";
		this.method_name = method_name;
		this.hasfiles = hasfiles;
	}
	
	public ResponseObject callService( List<Map<String,String>> params, Object[] objarr)  {
		
		ResponseObject resp = new ResponseObject();
    	try {
    			logger.debug("send callService:1    "+method_name);
    	      	     
            //First create the connection
				SOAPConnectionFactory soapConnFactory = 
				                   SOAPConnectionFactory.newInstance();
				SOAPConnection connection = 
				                    soapConnFactory.createConnection();
				
				MessageFactory messageFactory = MessageFactory.newInstance();
				SOAPMessage message = messageFactory.createMessage();
				SOAPPart soapPart =     message.getSOAPPart();
				
				SOAPEnvelope envelope = soapPart.getEnvelope();
			
				SOAPBody bodyx =         envelope.getBody();
				logger.debug("send callService------:4 :: "+params);
				SOAPElement bodyElement = 	    bodyx.addChildElement(envelope.createName(method_name ,  "ns1", 
				        "http://general.service.webservices.skire.com"));  
				int i = 0;
				for(Map<String,String> param : params){
					if(param.get("param_name").equals("files")){
						logger.debug("createComplete ^^^^%%%%%^^^^^^$$$ "+i+" :: "+objarr[i]);
						if(param.get("count").equals("1")){
							FileData fd = (FileData) objarr[i];
							if(fd != null){
								bodyElement.addChildElement( "filename" ).addTextNode(fd.getFilename());
								byte[] dh = fd.getByteDatahandler();
								//java.nio.ByteBuffer bytebuf = null;
								 if(dh != null && dh.length> 0){
					                // bytebuf = java.nio.ByteBuffer.wrap(dh);
					                // bodyElement.addChildElement("datahandler").addTextNode(bytebuf.toString());
					                 bodyElement.addChildElement("datahandler").addTextNode(org.apache.commons.codec.binary.Base64.encodeBase64String(dh));
					                 logger.debug("createComplete ^^^^%%%%%^^^^^^$byte$ "+dh.length+" :: ");
					             }
								}
						}else{// multiple files
							FileData[] fdarr = (FileData[]) objarr[i];
							for(FileData fd : fdarr){
								if(fd == null)
									continue;
								SOAPElement fileelement = bodyElement.addChildElement("FileData");
								fileelement.addChildElement( "filename" ).addTextNode(fd.getFilename());
								byte[] dh = fd.getByteDatahandler();
								
								logger.debug("createComplete ^^^^%%%%%^^^^^^$byte$ "+i+" :: "+dh.length);
					             if(dh != null && dh.length> 0){
					            	 fileelement.addChildElement("datahandler").addTextNode( org.apache.commons.codec.binary.Base64.encodeBase64String(dh));
					             }
							}
						}
					}else
						bodyElement.addChildElement( param.get("param_name") ).addTextNode((objarr[i] != null) ? objarr[i].toString() : "");
					i++;
				}
				  //Save the message
				  //Send the message
				message.saveChanges();
				//File f = new File("c:/files/asreq.txt");
			//	FileOutputStream fs = new FileOutputStream(f);
				//message.writeTo(fs);   
				logger.debug("send message:6  sending ");  
			
				SOAPMessage reply = connection.call(message, url);
				
				resp = processResponse(reply);
				logger.debug("return soap message  recd: "+resp);
				    //Close the connection            
				connection.close();

           } catch(Exception e) {
        	   resp.setStatusCode(500);
				String[] err ={"Error occurred in processing "+e.getMessage()};
				if(method_name.equalsIgnoreCase("ping")){
					err[0] ="Error occurred in connecting to server "+url;
					resp.setStatusCode(501);
				}
				resp.setErrorStatus(err);
        	   logger.error(e,e);
		   }
    	return resp;
    }
	
	private ResponseObject processResponse(SOAPMessage reply){
		ResponseObject resp = new ResponseObject();
		FileOutputStream fs = null;
		try{
			if(reply == null){
				resp.setStatusCode(500);
				String[] err ={"Error occurred in processing"};
				resp.setErrorStatus(err);
				return resp;
			}	
			File f = new File("c:/files/as.txt");
			fs = new FileOutputStream(f);
			reply.writeTo(fs);   
			
			NodeList nl = reply.getSOAPBody().getElementsByTagNameNS("http://xml.util.webservices.skire.com/xsd", "statusCode");
			if(nl.getLength() > 0)
				resp.setStatusCode(toInt(nl.item(0).getTextContent()));
			nl = reply.getSOAPBody().getElementsByTagNameNS("http://xml.util.webservices.skire.com/xsd", "xmlcontents");
			if(nl.getLength() > 0)
				resp.setXmlcontents(nl.item(0).getTextContent());
			 nl = reply.getSOAPBody().getElementsByTagNameNS("http://xml.util.webservices.skire.com/xsd", "errorStatus");
			if(nl.getLength() > 0){
				 String[] err = new String[nl.getLength()];
				for(int i = 0; i < nl.getLength(); i++)
					err[i] = nl.item(i).getTextContent();
				resp.setErrorStatus(err);
			}
			if(this.method_name.equalsIgnoreCase("getCompleteBPRecord")){
				nl = reply.getSOAPBody().getElementsByTagNameNS("http://xml.util.webservices.skire.com/xsd", "filename");
				if(nl.getLength() > 0){// look for attachments
					resp.setFilename(nl.item(0).getTextContent());
					nl = reply.getSOAPBody().getElementsByTagNameNS("http://xml.util.webservices.skire.com/xsd", "datahandler");
					if(nl.getLength() > 0){
						Base64 base64obj = new Base64();
					
						byte[] data = base64obj.decode(nl.item(0).getTextContent().getBytes());
						resp.setFileData(data);
						
					}
				}	
				
			}
		}catch(Exception e){
			logger.error(e,e);
		}finally{
			try{
			if(fs != null)
				fs.close();
			}catch(Exception e){}
		}
		return resp;
	}


}
