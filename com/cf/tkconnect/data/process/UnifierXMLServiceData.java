package com.cf.tkconnect.data.process;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.xerces.parsers.DOMParser;

import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.util.WSConstants;
import com.cf.tkconnect.util.xml.ParseXML;

public class UnifierXMLServiceData {
	
	static Log logger = LogSource.getInstance(UnifierXMLServiceData.class);
	
	boolean isfileuri = true;// default is true
	String filename;
	String xmldata;
	Map<String,String> pmap = new HashMap<String,String>();

	Map<String,String> smpmap = new HashMap<String,String>();

	StringBuilder buf = new StringBuilder("");
	
	
	public UnifierXMLServiceData(String filename, boolean isfileuri){
		this.isfileuri = isfileuri;
		this.filename = filename;
	}
		
	private String processSMDatax(Node lwnode) throws Exception{
		 NodeList children = lwnode.getChildNodes();
		 buf.append("{\"file_type\":\"smartlink\"");
	      for(int i = 0; i < children.getLength(); i++){
	    	  Node node = children.item(i);
	    	  if (node.getNodeType() != Node.ELEMENT_NODE)
	    		  continue;
	    logger.debug(" node name _------- "+node.getNodeName());	
	    		buf.append(",");
	    		pmap.put(node.getNodeName().toLowerCase(), node.getTextContent());
			 buf.append("\""+node.getNodeName().toLowerCase()+"\":\"").append( StringEscapeUtils.escapeXml(node.getTextContent() )).append("\"");
	      }
	      buf.append("}");
	     return buf.toString();
	}
	
	private String processSMServiceData(Node lwnode) throws Exception{
		 NodeList children = lwnode.getChildNodes();
		 int count = 0;
		 buf.append("{\"file_type\":\"smartlink\"");
	      for(int i = 0; i < children.getLength(); i++){
	    	  Node node = children.item(i);
	    	  if (node.getNodeType() != Node.ELEMENT_NODE)
	    		  continue;
	    logger.debug(" node name _------- "+node.getNodeName());	
	    		buf.append(",");
	    		if(node.getNodeName().equals("parameters"))
	    			setServiceParams(node);	
	    		else{	
	    			pmap.put(node.getNodeName(), node.getTextContent());
	    			buf.append("\""+node.getNodeName().toLowerCase()+"\":\"").append( StringEscapeUtils.escapeXml(node.getTextContent() )).append("\"");
	    		}
			 count++;
	      }
	      buf.append("}");
	     return buf.toString();
	}
	
	private String setServiceParams(Node lwnode) throws Exception{
		 NodeList children = lwnode.getChildNodes();
		 int count = 0;
		 buf.append("\"parameters\":[");
	      for(int i = 0; i < children.getLength(); i++){
	    	  Node node = children.item(i);
	    	  if (node.getNodeType() != Node.ELEMENT_NODE)
	    		  continue;
	    logger.debug(" parameters node name _------- "+node.getNodeName());	
	    		
    			smpmap.put(node.getNodeName(), node.getTextContent());
    			if(count > 0)
    				buf.append(",");
				 buf.append("{\""+node.getNodeName().toLowerCase()+"\":\"").append( StringEscapeUtils.escapeXml(node.getTextContent() )).append("\"}");
				 count++;
	      }
	      buf.append("]");
	     return buf.toString();
	}
	
	
	private String processUlinkData() throws Exception{
		ParseXML parser = new ParseXML();
		HashMap<String,Object> inputMap = new HashMap<String,Object>();
		StringBuffer buf = new StringBuffer();
		List<String> errList = parser.validateSchemaAndProcessXML(null,
				filename, inputMap, buf);
		if(errList.size() > 0){
			String errors = "";
			for(String err : errList)
				errors +=err+",";
			String str = "{\"errors\":\""+errors+" \", \"file_type\":\"ulink\" }";
			pmap.put("errors", errors);
			return str;
		}
		return getUlinkJson( inputMap);
	}
	
	private String getUlinkJson(HashMap<String,Object> inputMap) throws Exception {
		Set<Entry<String, Object>> mapSet = inputMap.entrySet();
        Iterator<Entry<String, Object>> it= mapSet.iterator();
    	StringBuffer buf = new StringBuffer("{\"file_type\":\"ulink\"");
    	Map<String,String> umap = new HashMap<String,String>();
    	umap.put("file_type", "ulink");
        while(it.hasNext()){
            Map.Entry<String, Object> m=it.next();
            String key = m.getKey();
            String newkey = key;
            if(key.startsWith("_"))
            	newkey = key.substring(1);
            newkey = newkey.toLowerCase();
            Object value= m.getValue();
            String val = "";
            if(value != null){
            	if(value instanceof String){
            		val = StringEscapeUtils.escapeXml(value.toString());
            		pmap.put(key, val);
            	}else{
            		
            		List<String> list = (List<String>)value;
            		StringBuffer b = new StringBuffer("[");
            		for(int i = 0; i < list.size(); i++){
            			if(i > 0)
            				b.append(",");
            			b.append("{").append("\"").append(newkey).append("\":").append("\"").append(StringEscapeUtils.escapeXml(list.get(i))).append("\"}");
            		}
            		b.append("]");
            		val = b.toString();
            	}
            }		
        	buf.append(",").append("\"").append(newkey).append("\":").append("\"").append(StringEscapeUtils.unescapeCsv(val)).append("\"");
        	umap.put(newkey,val);
        }
	    buf.append("}");
	    logger.debug("^^^^^^^^^^umap :: "+umap+"    &&&&&& "+inputMap);
	    pmap.clear();
	    pmap.putAll(umap);
        return buf.toString();
     
	}
	public Map<String,String> getMap(){
			return pmap;
	}
	public Map<String,String> getServiceParameterMap(){
		return smpmap;
}
  public  String parse()  {
    try {
      logger.debug("Parsing XML File: " + filename + " "+isfileuri);
      DOMParser parser = new DOMParser();
      parser.setFeature("http://xml.org/sax/features/validation", false);
      parser.setFeature("http://xml.org/sax/features/namespaces", false);
      if(!isfileuri)
    	  throw new Exception("Supports only  Files");
      parser.parse(filename);
      
      Document doc = parser.getDocument();
      if(doc.getChildNodes().getLength() == 0)
    	 return "{\"errors\":\"Invalid XML document, does not have proper tags ulink or smartlink\"}"; // nothing to process
      
      Node lwnode = doc.getChildNodes().item(0);// list wrapper
      logger.debug("Parsing XML data  " +lwnode.getNodeName());
//      if(!lwnode.getNodeName().equalsIgnoreCase(WSConstants.SMART_LINK) && !lwnode.getNodeName().equalsIgnoreCase(WSConstants.ULINK_TAG))
//    	  throw new Exception("XML document does not have smartlink/ulink tag"); // abort
      if(lwnode.getNodeName().equalsIgnoreCase(WSConstants.TK_LINK))
    	  return processSMServiceData(lwnode);
       parser = null;// remove this parse
       return processUlinkData();
    } catch (Exception e) {
    	logger.error(e,e);
    	pmap.put("errors", e.getMessage());
   	 	return "{\"errors\":\"Invalid XML document, "+StringEscapeUtils.escapeXml(e.getMessage())+"\"}"; // nothing to process

    }
     
  }
}

 
