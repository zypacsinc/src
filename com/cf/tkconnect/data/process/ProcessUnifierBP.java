package com.cf.tkconnect.data.process;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.xerces.parsers.DOMParser;

import com.cf.tkconnect.data.process.ProcessUnifierBP;
import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;

public class ProcessUnifierBP {
	
	static Log logger = LogSource.getInstance(ProcessUnifierBP.class);

	List<Map<String,Object>> bplist = new ArrayList<Map<String,Object>>();
	 
	

	private List<Map<String,String>> getBPlineitemsList(Node node){
		  NodeList bplinodes = node.getChildNodes();
	      List<Map<String,String>> bplilist = new ArrayList<Map<String,String>>();
	      for(int j = 0; j < bplinodes.getLength(); j++){
	    	  Node linode = bplinodes.item(j);
	    	  if (linode.getNodeType() != Node.ELEMENT_NODE)
	    		  continue;
	    	  NodeList lisubnodes = linode.getChildNodes();
			  Map<String,String> bplimap = new HashMap<String,String>();
		      for(int k = 0; k < lisubnodes.getLength(); k++){
		    	  Node lisubnode = lisubnodes.item(k);
		    	  if (lisubnode.getNodeType() != Node.ELEMENT_NODE)
		    		  continue;
		    	  bplimap.put(lisubnode.getNodeName(), lisubnode.getTextContent());
		      }
		      bplilist.add(bplimap);
	      }
	      return bplilist;
	}
	

	private List<Map<String,Object>> processBP(Document doc){
	     NodeList bpdelist = doc.getElementsByTagName("_bp");
	     List<Map<String,Object>> blist = new ArrayList<Map<String,Object>>();
	      for(int i = 0; i < bpdelist.getLength(); i++){
	    	  Node denode = bpdelist.item(i);
	    	  if (denode.getNodeType() != Node.ELEMENT_NODE)
	    		  continue;
		      Map<String,Object> map = new HashMap<String,Object>();
	    	  NodeList bpdesublist = denode.getChildNodes();
	    	  for(int j = 0; j < bpdesublist.getLength(); j++){
		    	  Node dnode = bpdesublist.item(j);
		    	  if (dnode.getNodeType() != Node.ELEMENT_NODE)
		    		  continue;
		    	  if (dnode.getNodeName().equalsIgnoreCase("_bp_lineitems")){
		    		  List<Map<String,String>> limaplist = getBPlineitemsList(dnode);
		    		  map.put(dnode.getNodeName(), limaplist);
		    	  }else{
		    		  map.put(dnode.getNodeName(), dnode.getTextContent());
		    	
		    	  }
	    	  }
	    	  blist.add(map);
	      }
	      return blist;
	}
	
	public List<Map<String,Object>> getBPMapList(){
		return bplist;
	}


	
  public void processBPtemplate(String uri) throws Exception {
    //String uri = "C:/Users/cyrilf/Downloads/test2.xml";
    try {
      System.out.println("Parsing XML File: " + uri + "\n\n");
      DOMParser parser = new DOMParser();
      parser.setFeature("http://xml.org/sax/features/validation", false);
      parser.setFeature("http://xml.org/sax/features/namespaces", false);
      parser.parse(uri);
      Document doc = parser.getDocument();
      if(doc.getChildNodes().getLength() == 0)
    	  throw new Exception("XML document does not have proper tags"); // nothing to process
      Node lwnode = doc.getChildNodes().item(0);// list wrapper
      if(!lwnode.getNodeName().equalsIgnoreCase("List_Wrapper"))
    	  throw new Exception("XML document does not have List_Wrapper tag"); // abort
      bplist = processBP( doc);
      
    } catch (Exception e) {
      logger.error(e,e);
      //System.out.println("Error: " + e.getMessage());
    }
  }
}

 
