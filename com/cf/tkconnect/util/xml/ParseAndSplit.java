package com.cf.tkconnect.util.xml;

/*

 Author Cyril Furtado
 September 20, 2006
 loose validation of the schema

 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.util.xml.BPTagErrorHandler;
import com.cf.tkconnect.util.xml.ParseAndSplit;
import com.cf.tkconnect.util.xml.SplitBPTagHandler;


public class ParseAndSplit {


	static Log logger = com.cf.tkconnect.log.LogSource.getInstance(ParseAndSplit.class);


	protected static final String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
	public ParseAndSplit() {

	}

	public ParseAndSplit( String documenturl,
			List<String> inputList) throws IOException, SAXException {

		ProcessXML( documenturl, inputList);
	}

	public List<String> ProcessXML(	String XmlDocumentUrl, List<String> inputList) throws IOException,
			SAXException {
		XMLReader parser = null;
		logger.error("ProcessXML start" );
		try{
            parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);
		}catch(Exception e){
			
		}
		File xmlfile = null;
		FileInputStream fis = null;
		InputSource isrc = null;
		List<String> errList = new ArrayList<String>();
		try {
			logger.error("ProcessXML parse" );
			DefaultHandler contentHandler = new SplitBPTagHandler(inputList);
			ErrorHandler errorHandler = new BPTagErrorHandler();
			parser.setErrorHandler(errorHandler);
			parser.setContentHandler(contentHandler);
			xmlfile = new File(XmlDocumentUrl);
			fis = new FileInputStream(xmlfile);
			isrc = new InputSource(fis);
			parser.parse(isrc);	
			errList =  ((SplitBPTagHandler)contentHandler).getErrorList();
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

}