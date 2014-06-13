package com.cf.tkconnect.util.xml;

/*

 Author Cyril Furtado
 September 1, 2005
 loose validation of the schema

 */

import java.io.File;
import java.io.FileInputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.util.xml.BPTagErrorHandler;
import com.cf.tkconnect.util.xml.GeneralTagHandler;
import com.cf.tkconnect.util.xml.ParseXML;


public class ParseXML {


	static Log logger = com.cf.tkconnect.log.LogSource.getInstance(ParseXML.class);


	protected static final String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
	public ParseXML() {

	}

	public ParseXML(String bpschemaurl, String documenturl,
			Map<String,Object> inputMap) throws IOException, SAXException {

		validateSchemaAndProcessXML(bpschemaurl, documenturl, inputMap,null);
	}

	public List<String> validateSchemaAndProcessXML(String SchemaUrl,
			String XmlDocumentUrl, Map<String,Object> inputMap, StringBuffer inputXmlBuffer) throws IOException,
			SAXException {
		/*boolean validate = true;
		if(SchemaUrl == null || SchemaUrl.length() == 0) {
			validate = false;
		}*/
		XMLReader parser = null;
		try{
            parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);
		}catch(Exception e){
			
		}
		File xmlfile = null;
		FileInputStream fis = null;
		InputSource isrc = null;
		List<String> errList = new ArrayList<String>();
		try {
			DefaultHandler contentHandler = new GeneralTagHandler(inputMap, inputXmlBuffer);
			ErrorHandler errorHandler = new BPTagErrorHandler();
			parser.setErrorHandler(errorHandler);
			parser.setContentHandler(contentHandler);
			xmlfile = new File(XmlDocumentUrl);
			fis = new FileInputStream(xmlfile);
			isrc = new InputSource(fis);
			parser.parse(isrc);	
			errList =  ((GeneralTagHandler)contentHandler).getErrorList();
		} catch (java.io.IOException ioe) {
			logger.error("IOException" + ioe.getMessage());
			errList = new ArrayList<String>();
			errList.add(ioe.getMessage());
		} catch (SAXException e) {
			logger.debug("SAXException" + e.getMessage());
			errList = new ArrayList<String>();
			errList.add(e.getMessage());
		} catch(Exception e) {
			logger.error(e,e);
			logger.debug("Exception " + e.getMessage());
			errList = new ArrayList<String>();
			errList.add(e.getMessage());
		}
		finally {
			if(fis != null) {
				fis.close();
			}
		}
		return errList;
	}

	public List<String> processXMLContents(String SchemaUrl,
			String XmlDocumentContents, Map<String,Object> inputMap, StringBuffer inputXmlBuffer) throws IOException,
			SAXException {
		XMLReader parser = null;
		try{
            parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);
		}catch(Exception e){
			
		}
		InputSource isrc = null;
		List<String> errList = new ArrayList<String>();
		try {
			DefaultHandler contentHandler = new GeneralTagHandler(inputMap, inputXmlBuffer);
			ErrorHandler errorHandler = new BPTagErrorHandler();
			parser.setErrorHandler(errorHandler);
			parser.setContentHandler(contentHandler);
			isrc = new InputSource(new CharArrayReader(XmlDocumentContents.toCharArray()));
			parser.parse(isrc);	
			errList =  ((GeneralTagHandler)contentHandler).getErrorList();
		} catch (java.io.IOException ioe) {
			logger.error("IOException" + ioe.getMessage());
			errList = new ArrayList<String>();
			errList.add(ioe.getMessage());
		} catch (SAXException e) {
			logger.debug("SAXException" + e.getMessage());
			errList = new ArrayList<String>();
			errList.add(e.getMessage());
		} catch(Exception e) {
			logger.error(e,e);
			logger.debug("Exception " + e.getMessage());
			errList = new ArrayList<String>();
			errList.add(e.getMessage());
		}
		finally {
		}
		return errList;
	}

}