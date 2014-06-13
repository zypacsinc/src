package com.cf.tkconnect.data.process;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.xerces.parsers.DOMParser;

import com.cf.tkconnect.data.process.ReadXMLData;
import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;

public class ReadXMLData {
	
	static Log logger = LogSource.getInstance(ReadXMLData.class);
	
	boolean isfileuri = false;
	String filename;
	String xmldata;
	String de = null;
	String dd = null;
	String bpinfo = null;
	List<Map<String,String>> newlist = new ArrayList<Map<String,String>>();
	List<Map<String,String>> updatelist = new ArrayList<Map<String,String>>();

	String datavalue = null;
	Map<String,String> checkmap = new HashMap<String,String>();
	String key = "";
	String subkey ="";
	
	
	public ReadXMLData(String filename, boolean isfileuri){
		this.isfileuri = isfileuri;
		this.filename = filename;
	}

	
	public ReadXMLData(String xmldata){
		this.xmldata = xmldata;
		this.isfileuri = false;
	}
	
	public void setCheckMap(Map<String,String> checkmap, String key){
		this.checkmap = checkmap;
		this.key = key;
	}
	public void setCheckMap(Map<String,String> checkmap, String key, String subkey){
		this.checkmap = checkmap;
		this.key = key;
		this.subkey = subkey;
	}
	private void processList(Node node, boolean versionCheck) throws Exception {
		newlist = new ArrayList<Map<String,String>>();
		updatelist = new ArrayList<Map<String,String>>();
		NodeList children = node.getChildNodes();
	    for(int i = 0; i < children.getLength(); i++){
	    	  Node cnode = children.item(i);
	    	  if (cnode.getNodeType() != Node.ELEMENT_NODE)
	    		  continue;
	    	  Map<String,String> map = new HashMap<String,String>();
	    	  NodeList mchildren = cnode.getChildNodes();
	    	   	
	    	  for(int j = 0; j < mchildren.getLength(); j++){
		    	  Node ccnode =mchildren.item(j);
		    	  if (ccnode.getNodeType() != Node.ELEMENT_NODE)
		    		  continue;
		    	  map.put(ccnode.getNodeName(), ccnode.getTextContent());
	    	  }
	    	  logger.debug(" node name -----*****-- "+cnode.getNodeName()+" key :"+key+" map :"+map.get(key)+" chk :"+checkmap.get(map.get(key)));	
	          if(map.containsKey(key) && map.get(key).equalsIgnoreCase(checkmap.get(map.get(key)))){
	        	  if(versionCheck && map.containsKey(subkey)){
	        		  String ver = map.get(subkey);
	        		  String checkver = checkmap.get(key);
	        		  if(!checkver.equals(ver))
	        			  updatelist.add(map);
	        	  }
	        		  
	          }else 
	        	  newlist.add(map);
	    }	
	    logger.debug(node.getNodeName()+" "+ newlist);
	}
	
	private void processData(Node lwnode) throws Exception{
		 NodeList children = lwnode.getChildNodes();
	      for(int i = 0; i < children.getLength(); i++){
	    	  Node node = children.item(i);
	    	  if (node.getNodeType() != Node.ELEMENT_NODE)
	    		  continue;
	    logger.debug(" node name _------- "+node.getNodeName());	 
	    	 if( node.getNodeName().equalsIgnoreCase("data_definitions") )
	    		 dd = StringEscapeUtils.escapeXml( node.getTextContent() );
	    	 else  if( node.getNodeName().equalsIgnoreCase("data_elements") )
	    		 de = StringEscapeUtils.escapeXml( node.getTextContent() );
	    	 else  if( node.getNodeName().equalsIgnoreCase("_bp_list") )
	    		 processList(node, false);
	    	 else  if( node.getNodeName().equalsIgnoreCase("_studio_list") )
	    		 processList(node, true);
	    	 else  if( node.getNodeName().equalsIgnoreCase("project_list") )
	    		 processList(node, false);
	    	 else  if( node.getNodeName().equalsIgnoreCase("data_value") )
	    		 datavalue = StringEscapeUtils.escapeXml( node.getTextContent() );
	    	 else  if( node.getNodeName().equalsIgnoreCase("costcodes") )
	    		 processList(node , false);
	      }
	    
	   //   System.out.println("Parsing dd "+dd+" \n");
	   //   System.out.println("Parsing de : " +de + "\n");
	     
	}
	public String getDD(){
		return dd;
	}
	public String getDE(){
		return de;
	}
	public String getBPinfo(){
		return bpinfo;
	}
	public List<Map<String,String>> getNewList(){
		return newlist;
	}
	
	public List<Map<String,String>> getUpdateList(){
		return updatelist;
	}
	
	public String getDataValue(){
		return datavalue;
	}
	
	
  public  void parse() throws Exception {
    try {
      logger.debug("Parsing XML File: " + filename + " "+isfileuri);
      DOMParser parser = new DOMParser();
      parser.setFeature("http://xml.org/sax/features/validation", false);
      parser.setFeature("http://xml.org/sax/features/namespaces", false);
      if(isfileuri)
    	  parser.parse(filename);
      else
    	  parser.parse(new InputSource(new StringReader(xmldata)));
      
      Document doc = parser.getDocument();
      if(doc.getChildNodes().getLength() == 0)
    	  throw new Exception("XML document does not have proper tags"); // nothing to process
      
      Node lwnode = doc.getChildNodes().item(0);// list wrapper
      logger.debug("Parsing XML data  " +lwnode.getNodeName());
      if(!lwnode.getNodeName().equalsIgnoreCase("List_Wrapper"))
    	  throw new Exception("XML document does not have List_Wrapper tag"); // abort
      
      processData(lwnode);
    } catch (Exception e) {
    	logger.error(e,e);
    }
  }
}

 
