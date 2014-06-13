package com.cf.tkconnect.util.xml;


import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import java.util.List;
import java.util.ArrayList;
import java.io.CharArrayReader;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.InputSource;

import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.log.WebLinkLogLoader;
import com.cf.tkconnect.util.xml.XMLObject;
import com.cf.tkconnect.util.xml.XmlReader;

public class XmlReader 
    extends DefaultHandler
    implements ContentHandler,  ErrorHandler, // standard
                LexicalHandler // extensions (beta)

{
	static Log logger = LogSource.getInstance(XmlReader.class);
	protected XMLReader fSAXParser;
	protected int fIndent;

	
	// input 
	private XMLObject hvalues ;
	private int errorStatus =0;
	protected String name = "";
	protected  String text = "";
	protected  String attrtext = "";
	private  StringBuilder buf = new StringBuilder("");
	private List<String> errors = new ArrayList<String>();
	private String aName = "";
	protected final String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
	
	public void setDocumentLocator(Locator locator) {
	    //logger.debug("setDocumentLocator("+locator+")");
	}


    public void startDocument() throws SAXException {
        //logger.debug("startDocument()");
		hvalues = new XMLObject();
    }


    public void startElement(String uri, String localpart, String rawname, Attributes attrs)
        throws SAXException {

       
        if( rawname == null)
        	return;
        name = rawname.toLowerCase();
		
        //logger.debug("ElementName-- "+name +" attr "+attrs.getLength());
        text = "";
        if (attrs != null  && "errorstatus".equals(name)) {
	        for (int i = 0; i < attrs.getLength(); i++) {
	          aName = attrs.getLocalName(i); // Attr name
			  if(aName == null) continue;
			  //logger.debug("Attr name-- "+aName+" ::"+attrs.getQName(i));
			  aName = aName.toLowerCase();
			}//for
          }
			
		attrtext ="";

    }  

    public void characters(char[] ch, int offset, int length) throws SAXException {
	    
		text = new String(ch, offset, length);
		if( text == null )	text = "";
		 //text = text.trim();
		 buf.append(text);
		//logger.debug("charac elem name ="+name+" text= "+text);
		

    }

    public void endElement(String uri, String localpart, String rawname) throws SAXException {
    	if( rawname == null)
    		return;
    	  name = rawname.toLowerCase();
  		if(name.equalsIgnoreCase("statusCode")  )
  			try{
  				hvalues.setStatusCode(Integer.parseInt(buf.toString()));
  			}catch(Exception e){hvalues.setStatusCode(1000);}
		else if(name.equalsIgnoreCase("errorStatus")  ){
			if(!"arrayType".equalsIgnoreCase(aName))
				errors.add(buf.toString());
		}else if(name.equalsIgnoreCase("xmlcontents")  ){
				hvalues.setXmlcontents(buf.toString());
				//logger.debug("EndElement name "+name+ " value::"+buf.toString());
		}
		buf = new StringBuilder("");
			// jobRelated
		//logger.debug("EndElement name "+name);

    }

    public void startCDATA() throws SAXException {
        //logger.debug("startCDATA()");
    }

    public void endCDATA() throws SAXException {
        //logger.debug(" endCDATA() "+name);
    }


    public void endDocument() throws SAXException {
		hvalues.setErrorStatus(errors.toArray(new String[] {}));
        //logger.debug("endDocument() "+hvalues.toString());
		// we need to check for duplication pass the routing 
    }
    public void comment(char[] ch, int offset, int length) throws SAXException {
        //logger.debug("comment ");
    }

    protected void printIndent() {
        for (int i = 0; i < fIndent; i++) {
            System.out.print(' ');
        }
    }
    public void startDTD(String name, String publicId, String systemId)
        throws SAXException {
        printIndent();
        fIndent++;
    }

    public void startEntity(String name) throws SAXException {
        printIndent();
        fIndent++;
    }

    public void endDTD() throws SAXException {
        logger.debug("endDTD()");
    } // endDTD
    public void endEntity(String name) throws SAXException {
        fIndent--;
        printIndent();
    }
    public void endPrefixMapping(String prefix) throws SAXException {
        fIndent--;
        printIndent();
    }

    public void skippedEntity(String name) throws SAXException {
        printIndent();
    }
    //
    
    // Warning. 
    public void warning(SAXParseException ex) {
        logger.debug("[Warning] "+
                           getLocationString(ex)+": "+
                           ex.getMessage());
    }

    /** Error. */
    public void error(SAXParseException ex) {
        logger.debug("[Error] "+
                           getLocationString(ex)+": "+
                           ex.getMessage());
    }

    /** Fatal error. */
    public void fatalError(SAXParseException ex) throws SAXException {
        logger.debug("[Fatal Error] "+
                           getLocationString(ex)+": "+
                           ex.getMessage());
        throw ex;
    }

    protected String getLocationString(SAXParseException ex) {
          StringBuffer str = new StringBuffer();
  
          String systemId = ex.getSystemId();
          if (systemId != null) {
              int index = systemId.lastIndexOf('/');
              if (index != -1) 
                  systemId = systemId.substring(index + 1);
              str.append(systemId);
          }
          str.append(':');
          str.append(ex.getLineNumber());
          str.append(':');
          str.append(ex.getColumnNumber());
  
          return str.toString();
  
      } // getLocationString(SAXParseException):String


    public XMLObject  parseXmlFile(char[] buff)  {
        
        // construct handler
        // construct parser; set features
		try{
			logger.debug("parseXmlFile------ ");
	        XMLReader parser = getXmlParser();;
	        // parser files
	    	InputSource inp = null;
	    	CharArrayReader car = null;
	            try {
	            	car = new CharArrayReader(buff);
	    			inp = new InputSource(car);
	            	parser.parse(inp);
	            }
	            catch (SAXException e) {
					logger.debug("Error SAX in parseXmlFile------ "+e.getMessage());
	                Exception ex = e.getException();
	                throw ex != null ? ex : e;
	            }catch(IOException ioe){
	    	        throw  ioe;
	            }finally{
	               	 if( car != null ) car.close();
					 
	            }
		}catch(Exception ie){
			logger.debug("Error in parseXmlFile------ "+ie.getMessage());
		}
         return hvalues;   
    } // 

    public   XmlReader()  {
    }
	
    public XMLObject  parseXmlFile(String filename)  {
		//
		try{
		XMLReader parser = getXmlParser();
		// parser files
		
		InputSource inp = null;
		CharArrayReader car = null;
		 try {
		    	FileReader rd = new FileReader(filename);
				inp = new InputSource(rd);
		    	parser.parse(inp);
		    }
		    catch (SAXException e) {
		        Exception ex = e.getException();
		        throw ex != null ? ex : e;
		    }catch(IOException ioe){
		        throw  ioe;
		    }finally{
		       	 if( car != null ) car.close();
				   ;
				 
		    }
		}catch (Exception e) {
			logger.debug("Error in XmlReader "+e.getMessage());
			logger.debug(e,e);
	    }   
		return hvalues;
    } // 
	
    public XMLObject  parseXmlData(String data)  {
		//
		try{
		XMLReader parser = getXmlParser();
		// parser files
		
		InputSource inp = null;
		CharArrayReader car = null;
		 try {
		    	StringReader rd = new StringReader(data);
				inp = new InputSource(rd);
		    	parser.parse(inp);
		    }
		    catch (SAXException e) {
		        Exception ex = e.getException();
		        throw ex != null ? ex : e;
		    }catch(IOException ioe){
		        throw  ioe;
		    }finally{
		       	 if( car != null ) car.close();
				   ;
				 
		    }
		}catch (Exception e) {
			logger.debug("Error in parseXmlData "+e.getMessage());
			logger.debug(e,e);
	    }   
		return hvalues;
    } // 
	
	 protected XMLReader getXmlParser() throws Exception {
    	//
    	XMLReader parser = null;
        // use default parser?
    	
        // create parser
        try {
            parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);
        }
        catch (Exception e) {
            logger.debug("error: Unable to instantiate parser ("+DEFAULT_PARSER_NAME+")");
            throw e;
        }

    	// set handlers
    	try {
    	    parser.setFeature("http://xml.org/sax/features/namespaces", true);
    	}
    	catch (SAXException e) {
    		throw e;
    	}
    	try {
    	    parser.setFeature("http://xml.org/sax/features/validation", false);
    	}
    	catch (SAXException e) {
    		throw e;
    	}
    	
    	parser.setContentHandler(this);
    	parser.setErrorHandler(this);
    	try {
    	    parser.setProperty("http://xml.org/sax/properties/lexical-handler", this);
    	}
    	catch (SAXException e) {
    		throw e;
    	}
		
    	return parser;
    }
	/*public static Document getXMLDoc(String xmlDocAsString) throws IOException, SAXException {
        if (xmlDocAsString != null) {
            return getXMLDoc(new InputSource(new FileReader(xmlDocAsString)));
        }
        return null;
    }
	public static Document getXMLDoc(InputSource source) throws IOException, SAXException {        
        if (source != null) {
            DOMParser parser = new DOMParser();
            parser.parse(source);
            return parser.getDocument();
        }
        return null;
    }
	*/

} // XmlReader

//--------------

