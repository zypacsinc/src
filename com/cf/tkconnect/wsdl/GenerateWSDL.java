package com.cf.tkconnect.wsdl;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;

import org.ow2.easywsdl.wsdl.WSDLFactory;
//import org.ow2.easywsdl.wsdl.api.Binding;
//import org.ow2.easywsdl.wsdl.api.BindingOperation;
import org.ow2.easywsdl.wsdl.api.Description;

import org.ow2.easywsdl.wsdl.api.WSDLException;

import org.ow2.easywsdl.wsdl.api.WSDLWriter;
import org.ow2.easywsdl.tooling.java2wsdl.JavaToEasyWSDL;
import org.ow2.easywsdl.tooling.java2wsdl.WSDLGenerationContext;

import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.util.FileUtils;
import com.cf.tkconnect.util.InitialSetUp;

public class GenerateWSDL {

	static Log logger = LogSource.getInstance(GenerateWSDL.class);
	String classname;
	String wsdlfile;
	List<String> params; 
	public   GenerateWSDL(String classname, List<String> params) {
		this.classname = classname;
		this.params = params;
	}
	
	public  boolean process() {
		boolean verbose = true;
		 String targetMapping ="";
		 boolean customNS = false;
		 List<String> polymorphClasses = new ArrayList<String>();
		 boolean polymorph = false;
		List<String> files = new ArrayList<String>();
			String f = "com.cf.smartlink.services."+this.classname;
			files.add(f);
			//files.add("com.cf.smartlink.util.xml.XMLObject");
			//files.add("com.cf.smartlink.util.Axis2Utils");
			logger.debug(" start  class   ::   "+f);
			JavaToEasyWSDL transformer = new JavaToEasyWSDL(verbose);
			List<Class<?>> classes = computeClassList(files);
			logger.debug(" no of classes found "+classes.size()+"  "+classes);
			try {
				
				WSDLGenerationContext context = transformer
						.generateWSDL(classes, polymorph, polymorphClasses);
				
				Description desc = context.processWSDL();
				logger.debug("ccccccccccccccccc desc ----- "+desc);
				desc.setTargetNamespace("http://general.service.webservices.skire.com");
				
				WSDLWriter writer = WSDLFactory.newInstance().newWSDLWriter();

				if (customNS)
				{
					StringTokenizer tok = new StringTokenizer(targetMapping,",");
					String[] cNS = new String[tok.countTokens()];
					int cpt =0;
					while (tok.hasMoreTokens())
					{
						cNS[cpt++]=tok.nextToken();
					}
					writer.useCustomNamespacesPrefixes(cNS);
				}else
				{
					writer.useNormalizedNamespacesPrefixes();
				}
				
				//Document doc = writer.getDocument(desc);
				
				//TODO : Find a better way to sort elements in the XML than
				// this xsl transformation...
		       // Document doc2 = XMLSorter.sortNodes(doc);				
			//	String res = org.ow2.easywsdl.schema.util.XMLPrettyPrinter.prettyPrint(doc2,"utf8");
				String out = writer.writeWSDL(desc);
				
				for(int i = 0; i < params.size(); i++){
					String rep = "\"arg"+i+"\"";
					String with ="\"" +params.get(i)+"\"";
					out =StringUtils.replace(out, rep, with);
				}
				logger.debug(" out "+out);
				wsdlfile = InitialSetUp.appHome+"/wsdl/"+classname+".wsdl";
				FileUtils.writeContent(wsdlfile, out);
			} catch (WSDLException e) {
				// TODO Auto-generated catch block
				logger.error(e,e);
				return false;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error(e,e);
				return false;
			}
		return true;
	}
	
	public String getWSDLFile(){
		return this.wsdlfile;
	}

	private static List<Class<?>> computeClassList(List<String> files2) {
		List<Class<?>> res = new ArrayList<Class<?>>();
		for (String string : files2) {
			Class<?> clazz = null;
			try {
				clazz = Class.forName(string);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			res.add(clazz);
			
				
					logger.debug("Generate WSDL for POJO "		+ clazz.getName());
				
			
		}
		return res;
	}
	
}
