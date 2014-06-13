package com.cf.tkconnect.wsdl;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.easywsdl.schema.api.Element;
import org.ow2.easywsdl.schema.api.Schema;
import org.ow2.easywsdl.schema.impl.ComplexTypeImpl;
import org.ow2.easywsdl.wsdl.WSDLFactory;
//import org.ow2.easywsdl.wsdl.api.Binding;
//import org.ow2.easywsdl.wsdl.api.BindingOperation;
import org.ow2.easywsdl.wsdl.api.Description;
import org.ow2.easywsdl.wsdl.api.Service;
import org.ow2.easywsdl.wsdl.api.Types;
import org.ow2.easywsdl.wsdl.api.WSDLReader;
import org.xml.sax.InputSource;

import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.wsdl.ParseWSDL;


public class ParseWSDL {
	
	static Log logger = LogSource.getInstance(ParseWSDL.class);
	
	String wsdlfile;
	String serviceName;
	String methodsInJson;
	String paramsInJson;
	List<Map<String,String>> paramsInList;
	String methodname;
	boolean singlemethod = false;
	
	public ParseWSDL(String wsdlfile){
		this.wsdlfile = wsdlfile;
	}
	
	public ParseWSDL(String wsdlfile, String methodname){
		this.wsdlfile = wsdlfile;
		this.methodname = methodname;
		this.singlemethod = true;
	}
	
	public void  parse() throws Exception{
		WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
		//File file = new File("C:/java/wsdl/UnifierWebServices9.11.0.0.wsdl");
		File file = new File(wsdlfile);
		FileInputStream fi = new FileInputStream(file);
		InputSource is = new InputSource(fi);
		Description desc = reader.read(is);
		StringBuilder mbuf = new StringBuilder();
		paramsInList = new ArrayList<Map<String,String>>();
		// Select a service
		Service service = desc.getServices().get(0);
		this.serviceName = service.getQName().getLocalPart();
		logger.debug("Service "+service.getQName().getLocalPart());
		Types ts = ((org.ow2.easywsdl.wsdl.impl.wsdl11.DescriptionImpl)desc).getTypes();
		List<Schema> ss = ts.getSchemas();
		StringBuilder buf = new StringBuilder("{").append("\"service\":\"").append(service.getQName().getLocalPart()).append("\"");
		buf.append(",\"methods\":[");
		for(int i = 0; i < ss.size(); i++){
			Schema s = ss.get(i);
			List<Element> ee = s.getElements();
			int count = 0;
			for(int k = 0; k < ee.size(); k++){
				Element e = ee.get(k);
				String name = e.getQName().getLocalPart();
				if(name.endsWith("Response"))
					continue;
				if(count > 0 )
					buf.append(",");
				
				buf.append("{\"method_name\":\"").append(name).append("\",");
				if(this.singlemethod && name.equalsIgnoreCase(methodname)){
					mbuf.append("{\"method_name\":\"").append(name).append("\",");
					mbuf.append("\"parameters\":").append(" [");
				}	
				buf.append("\"parameters\":").append(" [");
				ComplexTypeImpl ci =  (ComplexTypeImpl)e.getType();
				List<Element> es = ci.getSequence().getElements();
				for(int j = 0; j < es.size(); j++){
					Element ei = es.get(j);
					buf.append("{\"param_name\": \"").append(ei.getQName().getLocalPart()).append("\",");
					buf.append("\"type\": \"").append(ei.getType().getQName().getLocalPart()).append("\",");
					buf.append("\"count\":\"").append(ei.getMaxOccurs()).append("\"}");
					if( j < es.size()-1)
						buf.append(",");
					if(this.singlemethod && name.equalsIgnoreCase(methodname)){
						mbuf.append("{\"param_name\": \"").append(ei.getQName().getLocalPart()).append("\",");
						mbuf.append("\"type\": \"").append(ei.getType().getQName().getLocalPart()).append("\",");
						mbuf.append("\"count\":\"").append(ei.getMaxOccurs()).append("\"}");
						if( j < es.size()-1)
							mbuf.append(",");
						Map<String,String> pmap = new HashMap<String,String>();
						pmap.put("param_name", ei.getQName().getLocalPart());
						pmap.put("type", ei.getType().getQName().getLocalPart());
						pmap.put("count", ei.getMaxOccurs());
						paramsInList.add(pmap);
					}
					//System.out.println("Sub Ele "+ei.getQName().getLocalPart()+" :: "+ei.getMaxOccurs()+" :: "+ei.getType().getQName().getLocalPart());
				}
				buf.append("]");
				buf.append("}");
				if(this.singlemethod && name.equalsIgnoreCase(methodname))
					mbuf.append("]").append("}");
				
				count++;
			}
		}
		buf.append("]}");
		this.methodsInJson = buf.toString();
		this.paramsInJson = mbuf.toString();
	}
	
	public String getServiceName(){
		return this.serviceName;
	}
	
	public String getMethodsInJson(){
		return this.methodsInJson;
	}
	
	public String getParamsInJson(){
		return this.paramsInJson;
	}
	
	public List<Map<String,String>> getParamsInList(){
		return this.paramsInList;
	}
}

