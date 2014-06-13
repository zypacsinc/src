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

import com.cf.tkconnect.util.WSUtil;
import com.cf.tkconnect.util.xml.GeneralTagHandler;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;


public class GeneralTagHandler extends DefaultHandler  {
	
    static com.cf.tkconnect.log.Log logger =
    	com.cf.tkconnect.log.LogSource.getInstance(GeneralTagHandler.class);

	private StringBuffer textBuffer;

	private StringBuffer inputXmlBuffer = new StringBuffer();

	public boolean validationError = false;
	
	private boolean isbpxml = false;
	private boolean fieldname = false;
	private boolean filtervalue = false;
	private boolean options = false;
	private ArrayList<String> fieldNameList = null;
	private List<String> filterValueList = null;
	private List<String> filelist = new ArrayList<String>();
	private StringBuilder optionsbuf = new StringBuilder();

	Map<String,Object> inputMap;
	
	private ArrayList<String> errorList = new ArrayList<String>();

	private String content;
	//private StringBuffer xmlBuffer = new StringBuffer();

	// ===========================================================
	// SAX DocumentHandler methods
	// ===========================================================
	public GeneralTagHandler(Map<String,Object> inputMap, StringBuffer inputXmlBuffer) {
		this.inputMap = inputMap;
		this.inputXmlBuffer = inputXmlBuffer;

	}

	public void endDocument() throws SAXException {
		logger.debug("end filelist:"+filelist);
		inputMap.put(FILELIST,filelist);

	}

	public void startElement(String namespaceURI, String sName, // simple name
			String qName, // qualified name
			Attributes attrs) throws SAXException {
			
		if(!qName.equals(sName) && !NS_URI.equals(namespaceURI)){
			logger.info("Namespace URI should be : "+NS_URI+", but found :"+namespaceURI);
			validationError = true;
			throw new SAXException("Namespace URI should be : "+NS_URI+", but found :"+namespaceURI);
		}	
		
		textBuffer = new StringBuffer();
		if (!isbpxml)
			logger.info("Namespace start should be sName: "+sName);
		if (isbpxml)
			inputXmlBuffer.append("<").append(sName).append(">");
		
		if (options)
			optionsbuf.append("<").append(sName).append(">");
		
		if (sName.equalsIgnoreCase(LISTWRAPPER)) {
			inputXmlBuffer = new StringBuffer();
			inputXmlBuffer.append("<").append(sName).append(">");
			isbpxml = true;
			
		}
		if (sName.equalsIgnoreCase(SCHEDULEOPTIONS) || sName.equalsIgnoreCase(FILTEROPTIONS) || sName.equalsIgnoreCase(UPDATE_BPRECORDV2_OPTIONS)) {
			options = true;
			optionsbuf.append("<").append(sName).append(">");
		}
		if (sName.equalsIgnoreCase(FIELDNAMELIST)) {
			//have to extract <fieldname></fieldname>
			fieldname = true;
			fieldNameList = new ArrayList<String>();
		}
		if (sName.equalsIgnoreCase(FILTERVALUELIST)) {
			//have to extract <filtervalue></filtervalue>
			filtervalue = true;
			filterValueList = new ArrayList<String>();
		}
		if (sName.equalsIgnoreCase(FILELIST)) {
			//have to extract <filtervalue></filtervalue>
		}
		//xmlBuffer.append("<").append(qName).append(">");
	}

	public void endElement(String namespaceURI, String sName, String qName)
			throws SAXException {

		//xmlBuffer.append("</").append(qName).append(">\n");
		if (isbpxml){
			inputXmlBuffer.append(WSUtil.filter(textBuffer.toString()));
			inputXmlBuffer.append("</").append(sName).append(">");
		}
		if (options){
			optionsbuf.append(WSUtil.filter(textBuffer.toString()));
			optionsbuf.append("</").append(sName).append(">");			
		    logger.debug("endElement: " + optionsbuf );		
		}
		if (sName.equalsIgnoreCase(SHORTNAME)
				|| sName.equalsIgnoreCase(AUTHENTICATIONKEY)
				|| sName.equalsIgnoreCase(PROJECTNUMBER)
				|| sName.equalsIgnoreCase(CLONEPROJECTNUMBER)
				|| sName.equalsIgnoreCase(BPNAME)
				|| sName.equalsIgnoreCase(OBJECTNAME)
				|| sName.equalsIgnoreCase(RECORDNUMBER)
				|| sName.equalsIgnoreCase(PLANNINGITEM)
				|| sName.equalsIgnoreCase(SERVICENAME)
				|| sName.equalsIgnoreCase(COLUMNNAME)
				|| sName.equalsIgnoreCase(REPORTNAME)
				|| sName.equalsIgnoreCase(COPYFROMASSET)
				|| sName.equalsIgnoreCase(ASSETCLASSNAME)
				|| sName.equalsIgnoreCase(SHEETNAME)
				|| sName.equalsIgnoreCase(COPY_FROM_USER_PREFERENCE_TEMPLATE)
				|| sName.equalsIgnoreCase(COPYFROMRECORD)
				|| sName.equalsIgnoreCase(CMCODE)
				|| sName.equalsIgnoreCase(CLASSNAME)
				|| sName.equalsIgnoreCase(CREATESHELL)
				|| sName.equalsIgnoreCase(UPDATESHELL)
				|| sName.equalsIgnoreCase(GETSHELLLIST)
				|| sName.equalsIgnoreCase(SHELLTYPE)
				|| sName.equalsIgnoreCase(SHELLNUMBER)
				|| sName.equalsIgnoreCase(COPYFROMSHELLTEMPLATE)
				|| sName.equalsIgnoreCase(ZIPFILE)
				|| sName.equalsIgnoreCase(FIELDNAMES)
				|| sName.equalsIgnoreCase(SPACETYPE)
				|| sName.equalsIgnoreCase(FILTERCONDITION)
				|| sName.equalsIgnoreCase(CREATEUPDATEROLE)
				|| sName.equalsIgnoreCase(CREATEUPDATERESOURCE)
				|| sName.equalsIgnoreCase(GETROLELIST)
				|| sName.equalsIgnoreCase(GETRESCOURCELIST)
				
				) {
			if(inputMap.get(sName.toLowerCase()) == null) 
				inputMap.put(sName.toLowerCase(), textBuffer.toString());
			else 
				errorList.add("Duplicate values exist for : "+ sName);
			
		} else if (sName.equalsIgnoreCase(LISTWRAPPER)) {
			if(inputMap.get(INPUTXML) == null) {
				inputMap.put(INPUTXML, inputXmlBuffer.toString());
				isbpxml = false;
			}
			else {
				errorList.add("Duplicate values exist for : "+ sName);
			}
		} else if(sName.equalsIgnoreCase(FIELDNAMELIST)) {
			if(inputMap.get(sName.toLowerCase()) == null) {
				fieldname = false;
				inputMap.put(sName.toLowerCase(),fieldNameList);
			}
			else {
				errorList.add("Duplicate values exist for : "+ sName);
			}
		} else if(sName.equalsIgnoreCase(FILTERVALUELIST)) {
			if(inputMap.get(sName.toLowerCase()) == null) {
				filtervalue = false;
				inputMap.put(sName.toLowerCase(),filterValueList);
			}
			
		} else if(sName.equalsIgnoreCase(FILELIST)) {
			if(textBuffer.length() > 0) {
				 
					logger.debug("adding filename : "+ textBuffer);
					if(!filelist.contains(textBuffer.toString()))
						filelist.add(textBuffer.toString());
				
			}
			
			textBuffer = new StringBuffer();
		} else if(sName.equalsIgnoreCase(SCHEDULEOPTIONS)){
			options = false;
			inputMap.put(SCHEDULEOPTIONS,optionsbuf.toString());
		} else if(sName.equalsIgnoreCase(FILTEROPTIONS)  ){
			options = false;
			inputMap.put(FILTEROPTIONS
					,optionsbuf.toString());	
		} else if(sName.equalsIgnoreCase(UPDATE_BPRECORDV2_OPTIONS)){
			options = false;
			inputMap.put(UPDATE_BPRECORDV2_OPTIONS,optionsbuf.toString());
		} 
		
		if (sName.equalsIgnoreCase("wbs_code"))
			logger.debug("wbs_code: finished processing");
		
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
