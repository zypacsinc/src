package com.cf.tkconnect.util.xml;

/*

 Author Cyril Furtado
 September 1, 2005
 loose validation of the schema

 */
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import static com.cf.tkconnect.util.WSConstants.*;

import com.cf.tkconnect.process.ProcessExternal;
import com.cf.tkconnect.util.WSUtil;
import com.cf.tkconnect.util.xml.SplitBPTagHandler;

import java.util.ArrayList;
import java.util.List;



public class SplitBPTagHandler extends DefaultHandler  {
	
    static com.cf.tkconnect.log.Log logger =
    	com.cf.tkconnect.log.LogSource.getInstance(SplitBPTagHandler.class);

	private StringBuilder textBuffer;

	private StringBuilder inputXmlBuffer = new StringBuilder();

	public boolean validationError = false;
	
	private boolean isbpxml = false;
	private boolean fieldname = false;
	private boolean filtervalue = false;
	private ArrayList<String> fieldNameList = null;
	private ArrayList<String> filterValueList = null;
	private boolean nspresent	= false;
	private String nsvalue	= "";
	private StringBuilder uribuf = new StringBuilder();
	
	List<String> inputList;
	
	private ArrayList<String> errorList = new ArrayList<String>();

	private String content;
	//private StringBuffer xmlBuffer = new StringBuffer();

	public void startPrefixMapping(String prefix, String uri)
	    throws SAXException {
	
	logger.debug("beginning split startPrefixMapping  prefix:"+prefix+" uri:"+uri);
    	uribuf.append("  xmlns");
		if(prefix !=null && prefix.trim().length() >0)uribuf.append(":").append(prefix);
		uribuf.append("=\"").append(uri).append("\" ");
    	logger.debug("startPrefixMapping split prefix:"+prefix+" uri:"+uri);

    	logger.debug("split-- startPrefixMapping  uribuf:"+uribuf);
	
	} // s
	// ===========================================================
	// SAX DocumentHandler methods
	// ===========================================================
	public SplitBPTagHandler(List<String> inputList) {
		this.inputList = inputList;
		this.inputXmlBuffer = new StringBuilder();

	}

	public void endDocument() throws SAXException {
		//logger.debug("end xml:"+xmlBuffer);
		ProcessExternal.nspresent = nspresent;
		ProcessExternal.nsvalue = uribuf.toString();
		
	}

	public void startElement(String namespaceURI, String sName, // simple name
			String qName, // qualified name
			Attributes attrs) throws SAXException {
		//logger.info("Namespace URI should found :"+namespaceURI);
		if(!nspresent && namespaceURI != null && namespaceURI.length() >0){
			nspresent = true;
			nsvalue =  namespaceURI;
		}
		/*
		if(!qName.equals(sName) && !NS_URI.equals(namespaceURI)){
			logger.info("Namespace URI should be : "+NS_URI+", but found :"+namespaceURI);
			validationError = true;
			throw new SAXException("Namespace URI should be : "+NS_URI+", but found :"+namespaceURI);
		}	
		*/	
		if (attrs != null && sName.equalsIgnoreCase("File")) {
    		int len = attrs.getLength();
    		logger.debug("start attr len:"+len);
		 		
		           for (int i = 0; i < len; i++){ 
				       if(attrs.getQName(i).equalsIgnoreCase("xsi:schemaLocation")){
							uribuf.append(attrs.getQName(i)).append("=\"").append(attrs.getValue(i)).append("\"");
							logger.debug("split attr attributes getLocalName(i):"+attrs.getLocalName(i)+" name:"+attrs.getQName(i)+"   value:"+attrs.getValue(i)+" attrs.getUri:"+attrs.getURI(i)+" type:"+attrs.getType(i)  );
							break;
					   }
					    
					}
            		
				
		}	
		textBuffer = new StringBuilder();		
		if (isbpxml)
			inputXmlBuffer.append("<").append(qName).append(">");
		//logger.debug("startElement field name:"+ qName+" sname:"+sName+" namespace uri:"+namespaceURI);	
		if (sName.equalsIgnoreCase(TKCONNECT_TAG)) {
			inputXmlBuffer = new StringBuilder();
			inputXmlBuffer.append("<").append(qName).append(">");
			isbpxml = true;
			
		}
		//xmlBuffer.append("<").append(qName).append(">");
	}

	public void endElement(String namespaceURI, String sName, String qName)
			throws SAXException {

		//xmlBuffer.append("</").append(qName).append(">\n");
		if (isbpxml){
			inputXmlBuffer.append(WSUtil.filter(textBuffer.toString()));
			inputXmlBuffer.append("</").append(qName).append(">");
			textBuffer = new StringBuilder();
		} 
		if (sName.equalsIgnoreCase(TKCONNECT_TAG)) {
			inputList.add(inputXmlBuffer.toString());
			inputXmlBuffer = new StringBuilder();
		}		
	}

	public void characters(char buf[], int offset, int len) throws SAXException {
		content = new String(buf, offset, len);
		//xmlBuffer.append(content);
		if(fieldname) {
			if(content.trim().length() > 0) {
				//logger.debug("adding field name: "+ content);			
				fieldNameList.add(content.trim());
			}
		} else if(filtervalue) {
			if(content.trim().length() > 0) {
				//logger.debug("adding filter value: "+ content);			
				filterValueList.add(content.trim());
			}
		}
		else {
			textBuffer.append(content);
		}
	}

	public SAXParseException saxParseException = null;

	public void error(SAXParseException exception) throws SAXException {
		validationError = true;
		saxParseException = exception;
	}

	public void fatalError(SAXParseException exception) throws SAXException {
		validationError = true;
		saxParseException = exception;
	}

	public void warning(SAXParseException exception) throws SAXException {
		logger.info("warning " + exception.getMessage());

	}
	
	public ArrayList<String> getErrorList() {
		return errorList;
	}

}
