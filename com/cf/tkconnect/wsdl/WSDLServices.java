package com.cf.tkconnect.wsdl;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.ow2.easywsdl.schema.api.Element;
import org.ow2.easywsdl.schema.api.Schema;
import org.ow2.easywsdl.schema.impl.ComplexTypeImpl;
import org.ow2.easywsdl.schema.impl.SimpleTypeImpl;
import org.ow2.easywsdl.wsdl.WSDLFactory;

import org.ow2.easywsdl.wsdl.api.Description;
import org.ow2.easywsdl.wsdl.api.Service;
import org.ow2.easywsdl.wsdl.api.Types;
import org.ow2.easywsdl.wsdl.api.WSDLReader;
import org.xml.sax.InputSource;

import com.cf.tkconnect.data.TKUnifierMetaData;
import com.cf.tkconnect.database.SqlUtils;
import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.util.InitialSetUp;
import com.cf.tkconnect.util.PropertyManager;
import com.cf.tkconnect.wsdl.WSDLServices;


public final class WSDLServices {
	
	static Log logger = LogSource.getInstance(WSDLServices.class);
	// get this from lib in class path
	static String wsdlfile = InitialSetUp.appHome+ "/wsdl";
	static String serviceName;
	static String  methodsInJson;
	static boolean parsed = false;
	static Map<String,String> jsonService = new HashMap<String,String>();
	static Map<String,List<Map<String,String>>> unifierMethodMap = new HashMap<String,List<Map<String,String>>>();
	// list servicename , for each service, its all metho
	static Map<String,Map<String,List<Map<String,String>>>>allServicesMethodMap = new HashMap<String,Map<String,List<Map<String,String>>>>();
	static{
		wsdlfile = wsdlfile+"/"+PropertyManager.getSysProperty("tkconnect.wsdl.filename");
		logger.info("WSDL file is set :"+wsdlfile);
	}
	
	
	private static synchronized void  parseUnifierWSDL() throws Exception{
		if(parsed)
			return;
		WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
		//File file = new File("C:/software/tomcat-7.0.26/webapps/smartlink/wsdl/UnifierWebServices9.12.0.0.wsdl");
		File file = new File(wsdlfile);
		FileInputStream fi = new FileInputStream(file);
		InputSource is = new InputSource(fi);
		Description desc = reader.read(is);

		// Select a service
		Service service = desc.getServices().get(0);
		serviceName = service.getQName().getLocalPart();
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
				buf.append("\"parameters\":").append(" [");
				ComplexTypeImpl ci =  (ComplexTypeImpl)e.getType();
				List<Element> es = ci.getSequence().getElements();
				List<Map<String,String>> list = new ArrayList<Map<String,String>>();
				for(int j = 0; j < es.size(); j++){
					Element ei = es.get(j);
					Map<String,String> paramMap = new HashMap<String,String>();
					buf.append("{\"param_name\": \"").append(ei.getQName().getLocalPart()).append("\",");
					buf.append("\"type\": \"").append(ei.getType().getQName().getLocalPart()).append("\",");
					buf.append("\"count\":\"").append(ei.getMaxOccurs()).append("\"}");
					paramMap.put("param_name", ei.getQName().getLocalPart());
					paramMap.put("count",ei.getMaxOccurs());
					paramMap.put("type",ei.getType().getQName().getLocalPart());
					list.add(paramMap);
					if( j < es.size()-1)
						buf.append(",");
					//System.out.println("Sub Ele "+ei.getQName().getLocalPart()+" :: "+ei.getMaxOccurs()+" :: "+ei.getType().getQName().getLocalPart());
				}
					buf.append("]");
					buf.append("}");
					count++;
					unifierMethodMap.put(name, list);
			}
			
		}
	//	TKUnifierMetaData ud = new TKUnifierMetaData();
	//	buf.append(",").append( ud.getWebServicesList(unifierMethodMap, true));
		buf.append("]");// methods list
		buf.append("}");
		System.out.println(" "+buf);
		// now fetch from database

		methodsInJson = buf.toString();
		parsed = true;
	
	}
	private boolean checkService() throws Exception{
		//if(this.servicename.equalsIgnoreCase("unifierWebServices") || this.servicename.equalsIgnoreCase("mainservice"))
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			conn = SqlUtils.getConnection();
//			System.out.println("Connection ------");
			ps = conn.prepareStatement("select * from services  order by service_name  ");
			rs = ps.executeQuery();
			
			while(rs.next()){
					
				
				return true;
			}
			
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return false;
		
	}
	
	public static String getServiceName() throws Exception{
		parseUnifierWSDL();
		return serviceName;
	}
	
	public static String getMethodsInJson() throws Exception{
		parseUnifierWSDL();
		return methodsInJson;
	}
	
	public static String getServiceMethodsInJson(String sname) throws Exception{
		if(jsonService.containsKey(sname))
			return jsonService.get(sname);
		return "{}";
	}
	public static Map<String,List<Map<String,String>>> getMethods() throws Exception{
		parseUnifierWSDL();
		return unifierMethodMap;
	}
	
	public static Map<String,List<Map<String,String>>> getServiceMethods(String sname) throws Exception{
		if(allServicesMethodMap.containsKey(sname))
			return allServicesMethodMap.get(sname);
		return new HashMap<String,List<Map<String,String>>>();
	}
	public  static String  parseWSDL(String filename) throws Exception{
		WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
		File file = new File(filename);
		FileInputStream fi = new FileInputStream(file);
		InputSource is = new InputSource(fi);
		Description desc = reader.read(is);
		Map<String,List<Map<String,String>>> serviceMethodMap = new HashMap<String,List<Map<String,String>>>();
		// Select a service
		Service service = desc.getServices().get(0);
		String clserviceName = service.getQName().getLocalPart();
		logger.debug("Service "+service.getQName().getLocalPart());
		
		Types ts = null;
		if(desc instanceof org.ow2.easywsdl.wsdl.impl.wsdl11.DescriptionImpl)
			 ts = ((org.ow2.easywsdl.wsdl.impl.wsdl11.DescriptionImpl)desc).getTypes();
		else if(desc instanceof org.ow2.easywsdl.wsdl.impl.wsdl20.DescriptionImpl)
			 ts  =((org.ow2.easywsdl.wsdl.impl.wsdl20.DescriptionImpl)desc).getTypes();
		if(ts == null)
			throw new Exception("WSDL is not in proper format");
		
		List<Schema> ss = ts.getSchemas();
		StringBuilder buf = new StringBuilder("{").append("\"service\":\"").append(service.getQName().getLocalPart()).append("\"");
		buf.append(",\"methods\":[");
		for(int i = 0; i < ss.size(); i++){
			Schema s = ss.get(i);
		
			List<Element> ee = s.getElements();
			for(int k = 0; k < ee.size(); k++){
				Element e = ee.get(k);
				String name = e.getQName().getLocalPart();
				logger.debug("general method ----------***** "+e.getQName().getLocalPart()+"  "+e.getType().getClass().getName());
				if(k > 0 && k % 2 == 0 )
					buf.append(",");
				if(k % 2 == 0 )
					buf.append("{");
				if(k % 2 == 1 )
					buf.append(",");
				if(name.endsWith("Response")){
					if(e.getType() instanceof org.ow2.easywsdl.schema.impl.SimpleTypeImpl){
						SimpleTypeImpl ei =  (SimpleTypeImpl)e.getType();
						buf.append("\"response\":{");
						buf.append("\"param_name\": \"").append(e.getQName().getLocalPart()).append("\",");
						buf.append("\"type\": \"").append(ei.getQName().getLocalPart()).append("\",");
						buf.append("\"count\":\"").append(e.getMaxOccurs()).append("\"}");
					}else if(e.getType() instanceof org.ow2.easywsdl.schema.impl.ComplexTypeImpl){
						ComplexTypeImpl ci =  (ComplexTypeImpl)e.getType();
						logger.debug("resp method ***** "+e.getQName().getLocalPart()+"  "+ci.getSequence());

						List<Element> es = ci.getSequence().getElements();
						buf.append("\"response\":{");
						for(int j = 0; j < es.size(); j++){
							Element ei = es.get(j);
							logger.debug("resp method param --- ***** "+ei.getQName().getLocalPart()+"  "+ei.getType());
							buf.append("\"param_name\": \"").append(ei.getQName().getLocalPart()).append("\",");
							buf.append("\"type\": \"").append(ei.getType()).append("\",");
							buf.append("\"count\":\"").append(ei.getMaxOccurs()).append("\"");
							if( j < es.size()-1)
								buf.append(",");
						}
						buf.append("}");
					}
				}else{	
					buf.append("\"method_name\":\"").append(name).append("\",");
					buf.append("\"parameters\":").append(" [");
					List<Map<String,String>> list = new ArrayList<Map<String,String>>();
					logger.debug(" method ***** "+e.getQName().getLocalPart()+"  ");
					if(e.getType() instanceof org.ow2.easywsdl.schema.impl.SimpleTypeImpl){
						
						SimpleTypeImpl ei =  (SimpleTypeImpl)e.getType();
						Map<String,String> paramMap = new HashMap<String,String>();
						buf.append("{\"param_name\": \"").append(e.getQName().getLocalPart()).append("\",");
						buf.append("\"type\": \"").append(ei.getQName().getLocalPart()).append("\",");
						buf.append("\"count\":\"").append(e.getMaxOccurs()).append("\"}");
						paramMap.put("param_name", ei.getQName().getLocalPart());
						paramMap.put("count", "1");
						paramMap.put("type","");
						list.add(paramMap);
					}else if(e.getType() instanceof org.ow2.easywsdl.schema.impl.ComplexTypeImpl){
						ComplexTypeImpl ci =  (ComplexTypeImpl)e.getType();
						logger.debug(" method ***** "+e.getQName().getLocalPart()+"  c::"+ ci.hasComplexContent()+" :s:"+ci.hasSimpleContent()+"  ::"+ ci.hasSequence());
						List<Element> es = ci.getSequence().getElements();
						for(int j = 0; j < es.size(); j++){
							Element ei = es.get(j);
							logger.debug("req  method param --- %%%%%%%%***** "+ei.getQName().getLocalPart()+"  "+ei.getType());
							Map<String,String> paramMap = new HashMap<String,String>();
							buf.append("{\"param_name\": \"").append(ei.getQName().getLocalPart()).append("\",");
							buf.append("\"type\": \"").append(ei.getType()).append("\",");
							buf.append("\"count\":\"").append(ei.getMaxOccurs()).append("\"}");
							paramMap.put("param_name", ei.getQName().getLocalPart());
							paramMap.put("count", ei.getQName().getLocalPart());
							paramMap.put("type","");
							list.add(paramMap);
							if( j < es.size()-1)
								buf.append(",");
							//System.out.println("Sub Ele "+ei.getQName().getLocalPart()+" :: "+ei.getMaxOccurs()+" :: "+ei.getType().getQName().getLocalPart());
						}
					}
					buf.append("]");
					serviceMethodMap.put(name, list);	
				}
				if(k % 2 == 1 )
					buf.append("}");
			}// for k
		}// for i
		
		buf.append("]}");
		allServicesMethodMap.put(clserviceName, serviceMethodMap);
		jsonService.put(clserviceName, buf.toString());
		logger.debug("generated ******** "+buf);
		return buf.toString();
	}

	public  static String  parseWSDLR(String filename) throws Exception{
		WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
		File file = new File(filename);
		FileInputStream fi = new FileInputStream(file);
		InputSource is = new InputSource(fi);
		Description desc = reader.read(is);
		Map<String,List<Map<String,String>>> serviceMethodMap = new HashMap<String,List<Map<String,String>>>();
		// Select a service
		Service service = desc.getServices().get(0);
		String clserviceName = service.getQName().getLocalPart();
		logger.debug("Service "+service.getQName().getLocalPart());
		
		Types ts = null;
		if(desc instanceof org.ow2.easywsdl.wsdl.impl.wsdl11.DescriptionImpl)
			 ts = ((org.ow2.easywsdl.wsdl.impl.wsdl11.DescriptionImpl)desc).getTypes();
		else if(desc instanceof org.ow2.easywsdl.wsdl.impl.wsdl20.DescriptionImpl)
			 ts  =((org.ow2.easywsdl.wsdl.impl.wsdl20.DescriptionImpl)desc).getTypes();
		if(ts == null)
			throw new Exception("WSDL is not in proper format");
		Map<String,String> tmap = new HashMap<String,String>();
		List<Schema> ss = ts.getSchemas();
		StringBuilder buf = new StringBuilder("{").append("\"service\":\"").append(service.getQName().getLocalPart()).append("\"");
		buf.append(",\"methods\":[");
		for(int i = 0; i < ss.size(); i++){
			Schema s = ss.get(i);
		
			List<Element> ee = s.getElements();
			for(int k = 0; k < ee.size(); k++){
				Element e = ee.get(k);
				String name = e.getQName().getLocalPart();
				logger.debug("general method ----------***** "+e.getQName().getLocalPart()+"  "+e.getType().getClass().getName()+" ");
				if(name.endsWith("Response")){
					if(e.getType() instanceof org.ow2.easywsdl.schema.impl.SimpleTypeImpl){
						SimpleTypeImpl ei =  (SimpleTypeImpl)e.getType();
						
						buf.append("\"response\":{");
						buf.append("\"param_name\": \"").append(e.getQName().getLocalPart()).append("\",");
						buf.append("\"type\": \"").append(ei.getQName().getLocalPart()).append("\",");
						buf.append("\"count\":\"").append(e.getMaxOccurs()).append("\"}");
					}else if(e.getType() instanceof org.ow2.easywsdl.schema.impl.ComplexTypeImpl){
						ComplexTypeImpl ci =  (ComplexTypeImpl)e.getType();
						logger.debug("resp method ***** "+e.getQName().getLocalPart()+"  "+ci.getSequence());

						List<Element> es = ci.getSequence().getElements();
						buf.append("\"response\":{");
						for(int j = 0; j < es.size(); j++){
							Element ei = es.get(j);
							logger.debug("resp method param --- ***** "+ei.getQName().getLocalPart()+"  "+ei.getType());
							buf.append("\"param_name\": \"").append(ei.getQName().getLocalPart()).append("\",");
							buf.append("\"type\": \"").append(ei.getType()).append("\",");
							buf.append("\"count\":\"").append(ei.getMaxOccurs()).append("\"");
							if( j < es.size()-1)
								buf.append(",");
						}
						buf.append("}");
					}
				}else{	
					buf.append("\"method_name\":\"").append(name).append("\",");
					buf.append("\"parameters\":").append(" [");
					List<Map<String,String>> list = new ArrayList<Map<String,String>>();
					logger.debug(" method ***** "+e.getQName().getLocalPart()+"  ");
					if(e.getType() instanceof org.ow2.easywsdl.schema.impl.SimpleTypeImpl){
						
						SimpleTypeImpl ei =  (SimpleTypeImpl)e.getType();
						Map<String,String> paramMap = new HashMap<String,String>();
						buf.append("{\"param_name\": \"").append(e.getQName().getLocalPart()).append("\",");
						buf.append("\"type\": \"").append(ei.getQName().getLocalPart()).append("\",");
						buf.append("\"count\":\"").append(e.getMaxOccurs()).append("\"}");
						paramMap.put("param_name", ei.getQName().getLocalPart());
						paramMap.put("count", "1");
						paramMap.put("type","");
						list.add(paramMap);
					}else if(e.getType() instanceof org.ow2.easywsdl.schema.impl.ComplexTypeImpl){
						ComplexTypeImpl ci =  (ComplexTypeImpl)e.getType();
						logger.debug(" method ***** "+e.getQName().getLocalPart()+"  c::"+ ci.hasComplexContent()+" :s:"+ci.hasSimpleContent()+"  ::"+ ci.hasSequence());
						List<Element> es = ci.getSequence().getElements();
						for(int j = 0; j < es.size(); j++){
							Element ei = es.get(j);
							logger.debug("req  method param --- %%%%%%%%***** "+ei.getQName().getLocalPart()+"  "+ei.getType());
							Map<String,String> paramMap = new HashMap<String,String>();
							buf.append("{\"param_name\": \"").append(ei.getQName().getLocalPart()).append("\",");
							buf.append("\"type\": \"").append(ei.getType()).append("\",");
							buf.append("\"count\":\"").append(ei.getMaxOccurs()).append("\"}");
							paramMap.put("param_name", ei.getQName().getLocalPart());
							paramMap.put("count", ei.getQName().getLocalPart());
							paramMap.put("type","");
							list.add(paramMap);
							if( j < es.size()-1)
								buf.append(",");
							//System.out.println("Sub Ele "+ei.getQName().getLocalPart()+" :: "+ei.getMaxOccurs()+" :: "+ei.getType().getQName().getLocalPart());
						}
					}
					buf.append("]");
					serviceMethodMap.put(name, list);	
				}
				if(k % 2 == 1 )
					buf.append("}");
			}// for k
		}// for i
		
		buf.append("]}");
		allServicesMethodMap.put(clserviceName, serviceMethodMap);
		jsonService.put(clserviceName, buf.toString());
		logger.debug("generated ******** "+buf);
		return buf.toString();
	}

	public static void main(String[] args) throws Exception{
		wsdlfile = "C:/software/tomcat-8-RC5/webapps/tkconnect/wsdl/UnifierWebServices9.13.0.2.wsdl";
		parseUnifierWSDL();
	}
}

