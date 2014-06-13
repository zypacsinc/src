package com.cf.tkconnect.mainservice;


import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import java.io.*;

import com.cf.tkconnect.mainservice.MainServiceClient;
import com.cf.tkconnect.util.FileUtils;
import com.cf.tkconnect.util.xml.XMLObject;
import com.cf.tkconnect.util.xml.XmlReader;

public class MainServiceClient {
	
	static com.cf.tkconnect.log.Log logger = com.cf.tkconnect.log.LogSource
	.getInstance(MainServiceClient.class);

	String url;
	public MainServiceClient(String inpurl){
		url = inpurl+"/ws/services/mainservice";
		logger.debug("Main client:"+url);
	}
	
	public static void main(String[] args){
		MainServiceClient udr = new MainServiceClient("http://dt-cfurtado:8081");
		//udr.getBPRecord();
	}
	
	public XMLObject getUDRData(String shortname,String  authcode,String  projectNumber,String  reportName)	{
		XMLObject returnobj = new XMLObject();
		
		//FileOutputStream fout = null;	
		File tempfile = null;
    	try {
    			logger.debug("send message:1");
    	      	     
            //First create the connection
				SOAPConnectionFactory soapConnFactory = 
				                   SOAPConnectionFactory.newInstance();
				SOAPConnection connection = 
				                    soapConnFactory.createConnection();
				//Next, create the actual message
				MessageFactory messageFactory = MessageFactory.newInstance();
				SOAPMessage message = messageFactory.createMessage();
				//Create objects for the message parts            
				SOAPPart soapPart =     message.getSOAPPart();
				SOAPEnvelope envelope = soapPart.getEnvelope();
				SOAPBody body =         envelope.getBody();
				logger.debug("send message:4");
				SOAPElement bodyElement = 
				    body.addChildElement(envelope.createName("getUDRData" ,  "ns1", 
				        "http://general.service.webservices.skire.com"));
				logger.debug("send message:5");
				//Add content
				bodyElement.addChildElement("shortname").addTextNode((shortname != null) ? shortname : "");
				bodyElement.addChildElement("authcode").addTextNode((authcode != null) ? authcode : "");
				bodyElement.addChildElement("projectNumber").addTextNode((projectNumber != null) ? projectNumber : "");
				bodyElement.addChildElement("reportName").addTextNode((reportName != null) ? reportName : "");
				logger.debug("send message:6");  
				  //Save the message
				message.saveChanges();
				message.writeTo(System.out);
				logger.debug("send message:"+message.toString());
				  //Send the message
				SOAPMessage reply = connection.call(message, url);
				logger.debug("return message:"+reply);
				//tempfile = FileUtils.getSaveFileforUDR();
				//String filename = tempfile.getAbsolutePath()   ;// "c:/soapdata.xml";
				//logger.debug("send filename:"+filename);
				ByteArrayOutputStream sw = new ByteArrayOutputStream();
//				fout = new FileOutputStream (filename);
//				reply.writeTo(fout);
				reply.writeTo(sw);
				    //Close the connection            
				connection.close();
				return parseData(sw.toString());
	        } catch(javax.xml.soap.SOAPException se) {
	            logger.error(se,se);
	            returnobj.setStatusCode(500);
	            returnobj.setErrorStatus(new String[] {se.getMessage()} );
           } catch(Exception e) {
        	   logger.error(e,e);
	            returnobj.setStatusCode(500);
	            returnobj.setErrorStatus(new String[] {e.getMessage()} );
           }finally{
        	  
		   }
        return returnobj;
    }

	private XMLObject parseData(String data) throws Exception{
	
			logger.debug("start parsing");
			XmlReader xmlReader = new XmlReader();
			XMLObject hvalues = xmlReader.parseXmlData(data);
			
			return hvalues;
	}
	
}
