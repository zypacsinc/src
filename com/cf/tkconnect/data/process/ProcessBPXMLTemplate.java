package com.cf.tkconnect.data.process;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.apache.xerces.parsers.DOMParser;

import com.cf.tkconnect.data.process.ProcessBPXMLTemplate;
import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;

public class ProcessBPXMLTemplate {
	
	static Log logger = LogSource.getInstance(ProcessBPXMLTemplate.class);

	List<Map<String,Object>> delist = new ArrayList<Map<String,Object>>();
	Map<String,String> bpmap = new HashMap<String,String>();
	Map<String,String> studiomap = new HashMap<String,String>();
	Map<String,String> bplimap = new HashMap<String,String>();
	boolean isfileuri = false;
	String filename;
	String xmldata;

	public ProcessBPXMLTemplate(String filename, boolean isfileuri){
			this.isfileuri = isfileuri;
			this.filename = filename;
	}

		
	public ProcessBPXMLTemplate(String xmldata){
			this.xmldata = xmldata;
			this.isfileuri = false;
	}

	private List<Map<String,String>> getOptionsList(Node node){// Options tag
		  NodeList optnodes = node.getChildNodes();//Option tags
		  List<Map<String,String>> optlist = new ArrayList<Map<String,String>>();
	      for(int j = 0; j < optnodes.getLength(); j++){
	    	  Node optnode = optnodes.item(j);
	    	  if (optnode.getNodeType() != Node.ELEMENT_NODE)
	    		  continue;
	    //	  logger.debug("getOptionsList  :"+ optnode.getNodeName()+"  : " +optnode.getChildNodes().getLength());
	    	  NodeList optnodelist = optnode.getChildNodes();// Name & value
	    	 Map<String,String> omap = new HashMap<String,String>();
	  	      for(int k = 0; k < optnodelist.getLength(); k++){
	  	    	  Node onode = optnodelist.item(k);
	  	    	  if (onode.getNodeType() != Node.ELEMENT_NODE)
	  	    		  continue;
	  	    	  String c = onode.getTextContent();
	  	    	  if(c == null || c.equalsIgnoreCase("null"))
	  	    		  c = "";
	  	    	  omap.put(onode.getNodeName(), c);
	  	      }
	  	    optlist.add(omap);
	      }
    //	  logger.debug("getOptionsList ---------- :"+ optlist);
	      return optlist;
	}
	
	private List<Map<String,Object>> processDE(Document doc){
	     NodeList bpdelist = doc.getElementsByTagName("DataElement");
	     List<Map<String,Object>> dexlist = new ArrayList<Map<String,Object>>();
	     Map<String,String> dupmap = new HashMap<String,String>();
	      for(int i = 0; i < bpdelist.getLength(); i++){
	    	  Node denode = bpdelist.item(i);
	    	  if (denode.getNodeType() != Node.ELEMENT_NODE)
	    		  continue;
	    	  NodeList bpdesublist = denode.getChildNodes();
	 	      Map<String,Object> map = new HashMap<String,Object>();
	 	      String denamevalue = "";
	    	  for(int j = 0; j < bpdesublist.getLength(); j++){
		    	  Node dnode = bpdesublist.item(j);
		    	  if (dnode.getNodeType() != Node.ELEMENT_NODE)
		    		  continue;
		    	  if (dnode.getNodeName().equalsIgnoreCase("Options")){
		    		  List<Map<String,String>> optlist = getOptionsList(dnode);
		    		  map.put(dnode.getNodeName(), optlist);
		    		 logger.debug(dnode.getNodeName()+" dnode.getNodeName()  :"+optlist );
		    	  }else{
		    		  if(dnode.getNodeName().equalsIgnoreCase("Name"))
		    			  denamevalue = dnode.getTextContent();
		    		  map.put(dnode.getNodeName(), dnode.getTextContent());
		    	  }
	    	  }
	    	//  logger.debug(denamevalue+" check for dename  :"+dupmap.containsKey(denamevalue) );
	    	  if(!dupmap.containsKey(denamevalue)){
	    		  dexlist.add(map);
	    		  dupmap.put(denamevalue, "1");
	    	  }
	      }
	      return dexlist;
	}

	private Map<String,String> getBPlineitemsMap(Node node){
		  NodeList bplinodes = node.getChildNodes();
		  Map<String,String> bplimap = new HashMap<String,String>();
	      for(int j = 0; j < bplinodes.getLength(); j++){
	    	  Node linode = bplinodes.item(j);
	    	  if (linode.getNodeType() != Node.ELEMENT_NODE)
	    		  continue;
	    	  bplimap.put(linode.getNodeName(), linode.getTextContent());
	  	      
	      }
	      return bplimap;
	}
	

	private Map<String,String> processBP(Document doc){
	     NodeList bpdelist = doc.getElementsByTagName("_bp_import");
	      Map<String,String> map = new HashMap<String,String>();
	      for(int i = 0; i < bpdelist.getLength(); i++){
	    	  Node denode = bpdelist.item(i);
	    	  if (denode.getNodeType() != Node.ELEMENT_NODE)
	    		  continue;
	    	  NodeList bpdesublist = denode.getChildNodes();
	    	  for(int j = 0; j < bpdesublist.getLength(); j++){
		    	  Node dnode = bpdesublist.item(j);
		    	  if (dnode.getNodeType() != Node.ELEMENT_NODE)
		    		  continue;
		    	  if (dnode.getNodeName().equalsIgnoreCase("_bp_lineitems")){
		    		  bplimap = getBPlineitemsMap(dnode);
		    		  //map.put(dnode.getNodeName(), "_bp_lineitems");
		    	  }else{
		    		  map.put(dnode.getNodeName(), dnode.getTextContent());
		    	  }
	    	  }
	    	  
	      }
	      return map;
	}
	
	private Map<String,String> processStudio(Document doc){
	     NodeList bpstlist = doc.getElementsByTagName("studio");
	      Map<String,String> map = new HashMap<String,String>();
	      if(logger.isDebugEnabled())
	    	  logger.debug("processStudio  -- :"+bpstlist.getLength());
	      if(bpstlist.getLength() == 0)
	    	  return map;
	      NodeList bpchlist = bpstlist.item(0).getChildNodes();
	      for(int i = 0; i < bpchlist.getLength(); i++){
	    	  Node denode = bpchlist.item(i);
		      if(logger.isDebugEnabled())
		    	  logger.debug("processStudio  denode.getNodeName() -- :"+denode.getNodeName());
	    	  if (denode.getNodeType() != Node.ELEMENT_NODE)
	    		  continue;
	    	  map.put(denode.getNodeName(), denode.getTextContent());
	      }
	      return map;
	 } 
	    	  
	
	public Map<String,String> getBPMap(){
		return bpmap;
	}
	public Map<String,String> getBLIPMap(){
		return bplimap;
	}

	public List<Map<String,Object>> getDEList(){
		return delist;
	}

	public Map<String,String> getStudio(){
		return studiomap;
	}
	
  public void processBPtemplate() {
    try {
    	logger.debug("Parsing XML File: " + filename + "\n\n");
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
      if(!lwnode.getNodeName().equalsIgnoreCase("List_Wrapper"))
    	  throw new Exception("XML document does not have List_Wrapper tag"); // abort
      delist = processDE( doc);
			
      bpmap = processBP( doc);
      studiomap = processStudio( doc);
      if(logger.isDebugEnabled())
  		 logger.debug("processBPtemplate 88888888888888888888888888  :"+studiomap );
      
    } catch (Exception e) {
    	logger.error(e,e);
    }
  }
}

 
