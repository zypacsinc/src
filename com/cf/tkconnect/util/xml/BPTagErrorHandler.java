package com.cf.tkconnect.util.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.cf.tkconnect.util.xml.BPTagErrorHandler;

public class BPTagErrorHandler implements ErrorHandler{

	static com.cf.tkconnect.log.Log logger = com.cf.tkconnect.log.LogSource
												.getInstance(BPTagErrorHandler.class);

	
	public void error(SAXParseException arg0) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void fatalError(SAXParseException arg0) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void warning(SAXParseException arg0) throws SAXException {
		// TODO Auto-generated method stub
		
	}

}
