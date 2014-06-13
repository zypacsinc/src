package com.cf.tkconnect.adapters;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.math.NumberUtils.toInt;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



import com.cf.tkconnect.database.SqlUtils;
import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.util.FileUtils;
import com.cf.tkconnect.util.InitialSetUp;

import com.cf.tkconnect.wsdl.GenerateWSDL;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

public class CreateService {

	static Log logger = LogSource.getInstance(CreateService.class);
	HttpServletRequest req;
	HttpServletResponse res;
	String servicename;
	String classname;
	String namespace;
	String methodname;
	String endpoint;
	String wsdlfile;
	int user_id ;
	StringBuilder buf = new StringBuilder("package com.cf.smartlink.services;\r\n ");
	StringBuilder sf = new StringBuilder("package com.cf.smartlink.services;\r\n ");
	List<String> p = new ArrayList<String>();
	List<String> ptype = new ArrayList<String>();
	
	public CreateService(HttpServletRequest req, HttpServletResponse res){
		this.req = req;
		this.res = res;
		this.servicename = req.getParameter("servicename");
		this.methodname = req.getParameter("methodname");
		this.namespace = req.getParameter("targetnamespace");
		this.endpoint = req.getParameter("endpoint");
		this.user_id = toInt(req.getParameter("user_id"));
	}
	
	public String start() throws Exception {
		
		if(checkService())
			return "{\"found\": \"true\"}";
		
		generateClassName();
		String s = generateCode();
		String filepathwithname = InitialSetUp.appHome+"/src/com/cf/smartlink/services/"+classname+".java";
		FileUtils.writeContent(filepathwithname, s);
		DynamicCompiler dcs = new DynamicCompiler();
		boolean resultsf = dcs.process(this.servicename , sf.toString());
		
		logger.debug("******  "+sf);

		DynamicCompiler dc = new DynamicCompiler();
		boolean result = dc.process(this.classname, s);
		GenerateWSDL gw = new GenerateWSDL(this.servicename, p);
		if(gw.process())
			this.wsdlfile = gw.getWSDLFile();
		int crid= createServiceRecord();
		return "{\"found\": \"false\", \"created\":\""+result+"\",\"service_id\":\""+crid+"\"  }";
	}

	private boolean checkService() throws Exception{
		//if(this.servicename.equalsIgnoreCase("unifierWebServices") || this.servicename.equalsIgnoreCase("mainservice"))
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = SqlUtils.getConnection();
//			System.out.println("Connection ------");
			ps = conn.prepareStatement("select * from services where service_name = ? and method_name = ? ");
			ps.setString(1, this.servicename);
			ps.setString(2, this.methodname);
			rs = ps.executeQuery();
			if(rs.next()){
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
	private int createServiceRecord() throws Exception{
		//if(this.servicename.equalsIgnoreCase("unifierWebServices") || this.servicename.equalsIgnoreCase("mainservice"))
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
        int coid = -1;
		try {
			conn = SqlUtils.getConnection();
			String sql = "insert into services (service_name,class_name,method_name,user_id,end_point,from_type,file_name) values(?,?,?,?,?,?,?) ";
			ps  = conn.prepareStatement( sql,Statement.RETURN_GENERATED_KEYS );

//			System.out.println("Connection ------");
			ps.setString(1, this.servicename);
			ps.setString(2, this.classname);
			ps.setString(3, this.methodname);
			ps.setInt(4, this.user_id);
			ps.setString(5, this.endpoint);
			ps.setInt(6, 1);
			ps.setString(7, this.wsdlfile);
			int up  = ps.executeUpdate();
	         rs =ps.getGeneratedKeys();
	         if(rs != null && rs.next())
	        	 coid = rs.getInt(1);
	         logger.debug(" The object inserted::::::::::::::: " +up+"  :"+coid);
	        // we need the new company id 

		}catch(Exception e){
			logger.error(e,e);
		} finally {
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return coid;
	}

	private void generateClassName(){
		this.classname = this.servicename+"_"+RandomStringUtils.randomAlphanumeric(4);
	}
	
	private String generateCode() throws Exception{
		buf.append(getHeader()).append(getMethodHeader());
		StringBuilder b = new StringBuilder("");
		StringBuilder b1 = new StringBuilder("");
		StringBuilder b2 = new StringBuilder("");
		sf.append("import java.util.*;\r\n import com.cf.smartlink.util.xml.XMLObject;\r\n");
		sf.append("\r\n public class "+this.servicename+"{\r\n");
		sf.append(" public XMLObject "+this.methodname+"(String shortname, String authcode ");
		StringBuilder adb = new StringBuilder("public static class "+StringUtils.capitalize(methodname)+" implements org.apache.axis2.databinding.ADBBean {\r\n");
		adb.append("public static final QName MY_QNAME = new QName(	\""+namespace+"\", \""+methodname+"\",\"ns2\");\r\n");
		adb.append("public "+(StringUtils.capitalize(methodname))+"(){}\r\n");
		String cons2 = "\npublic "+(StringUtils.capitalize(methodname))+"(String shortName,String authCode";
		StringBuilder str = new StringBuilder("");
		p.add("shortname");
		ptype.add("String");
		p.add("authcode");
		ptype.add("String");
		for(int i = 1; i < 8; i++){
			String param = req.getParameter("methodparam"+i);//paramtypeid
			if(param == null || param.trim().length() == 0)
				continue;
			String paramtype = req.getParameter("paramtypeid"+i);
			cons2 +=" ,"+paramtype+" "+param;
			b.append(getMethodDetail( param,   paramtype, b1, b2)).append("\r\n");
			str.append("set").append(StringUtils.capitalize(param)).append("(").append(param).append(");\r\n");	
			sf.append(", "+paramtype+" "+param);
			p.add(param);
			ptype.add(paramtype);
		}
		sf.append("){\r\n");
		sf.append(" return null;\r\n}\r\n}\r\n");
		cons2 +="){\n"+str+"}\r\n";
		adb.append(cons2);
		adb.append(b);
		buf.append(adb).append(getOM()).append(getSerialize1()).append(getSerialize2(b1)).append("\r\n");
		buf.append(generatePrefix()).append(writeStartElement()).append(writeAttribute()).append(registerPrefix()).append("\r\n");
		buf.append(getPullParser(b2)).append("\r\n}\r\n").append(getEnvelope());
		buf.append("}\r\n");
		return buf.toString();
	}
	
	
	private String getMethodDetail(String name,  String type, StringBuilder b , StringBuilder b2) {
		StringBuilder buf = new StringBuilder("");
		
		buf.append(type).append("  local").append(StringUtils.capitalize(name)).append(";");
		buf.append("boolean  local").append(StringUtils.capitalize(name)).append("Tracker = false;");
		buf.append("public boolean is").append(StringUtils.capitalize(name)).append("Specified() {\r\n");
		buf.append("return local").append(StringUtils.capitalize(name)).append("Tracker;		}\r\n");
		buf.append(" public ").append(type).append(" get").append(StringUtils.capitalize(name)).append("() { ");
		buf.append(" return local").append(StringUtils.capitalize(name)).append(";		}\r\n");
		buf.append(" public void set").append(StringUtils.capitalize(name)).append("(").append(type).append(" param) {");
		buf.append(" 	local").append(StringUtils.capitalize(name)).append("Tracker = true;");
		buf.append(" 	this.local").append(StringUtils.capitalize(name)).append(" = param;		} \r\n");
		
		
		b.append("if (local").append(StringUtils.capitalize(name)).append("Tracker) {\r\n");
		b.append("	namespace = \"").append(this.namespace).append("\";\r\n");
		b.append("writeStartElement(null, namespace, \""+name+"\", xmlWriter);\r\n");
		b.append("	if (local"+StringUtils.capitalize(name)+" == null)\r\n"); 
		b.append("	writeAttribute(\"xsi\",\"http://www.w3.org/2001/XMLSchema-instance\", \"nil\",\"1\", xmlWriter);\r\n");
		b.append("		else 	\r\n xmlWriter.writeCharacters(local").append(StringUtils.capitalize(name)).append(");\r\n");
		b.append("		xmlWriter.writeEndElement();	}\r\n");
		
		b2.append("	if (local").append(StringUtils.capitalize(name)).append("Tracker) {\r\n");
		b2.append("		elementList.add(new QName(\"").append(this.namespace).append("\",	\"").append(name).append("\"));\r\n");
		b2.append("		elementList.add(local"+StringUtils.capitalize(name)+" == null ? null: org.apache.axis2.databinding.utils.ConverterUtil.convertToString(local").append(StringUtils.capitalize(name)).append("));}\r\n");
		
		
		return buf.toString();
	}
	
	private String getSerialize1(){
		String ser ="\r\n	public void serialize(final QName parentQName,		javax.xml.stream.XMLStreamWriter xmlWriter)\r\n";
		ser +="	throws javax.xml.stream.XMLStreamException,	org.apache.axis2.databinding.ADBException {\r\n	serialize(parentQName, xmlWriter, false);\r\n	}\r\n";
		return ser;
	}
	
	private String getOM(){
		String othmth ="	public OMElement getOMElement(	final QName parentQName,final org.apache.axiom.om.OMFactory factory)	throws org.apache.axis2.databinding.ADBException {";
		othmth +="	org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(			this, MY_QNAME);	return factory.createOMElement(dataSource, MY_QNAME);}\n";
		return othmth;
	}
	
	private String getSerialize2(StringBuilder b){
		
		String othmth ="\r\n	public void serialize(final QName parentQName,	javax.xml.stream.XMLStreamWriter xmlWriter,		boolean serializeType)";
		othmth +="	throws javax.xml.stream.XMLStreamException,	org.apache.axis2.databinding.ADBException {\r\n";

		othmth +="	String prefix = null;	String namespace = null; prefix = parentQName.getPrefix();\r\n			namespace = parentQName.getNamespaceURI();\r\n";
		othmth +="	writeStartElement(prefix, namespace, parentQName.getLocalPart(),				xmlWriter);";

		othmth +="	if (serializeType) {String namespacePrefix = registerPrefix(xmlWriter,	\""+namespace+"\");\r\n";
		othmth +="	if ((namespacePrefix != null)	&& (namespacePrefix.trim().length() > 0)) {";
		othmth +="	writeAttribute(\"xsi\",\"http://www.w3.org/2001/XMLSchema-instance\",\"type\", namespacePrefix +";
		othmth +="\":"+this.methodname+"\", xmlWriter);\r\n} else {";
		othmth +="	writeAttribute(\"xsi\",	\"http://www.w3.org/2001/XMLSchema-instance\",	\"type\", \""+methodname+"\", xmlWriter);	} }\r\n";
		othmth += b;
		othmth +="xmlWriter.writeEndElement();\r\n}\r\n";
		return othmth;
	}
	
	private String generatePrefix(){
		StringBuilder buf = new StringBuilder(" private static String generatePrefix(String namespace) {\r\n");
		buf.append(" 	if (namespace.equals(\"").append(namespace).append("\")) {\r\n");
		buf.append(" 			return \"ns2\";	\r\n	}");
		buf.append(" 		return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();	}\r\n");
		return buf.toString();
	}
	
	private String getPullParser(StringBuilder b){
		StringBuilder buf = new StringBuilder(" public javax.xml.stream.XMLStreamReader getPullParser(	QName qName)");
			buf.append(" throws org.apache.axis2.databinding.ADBException {\r\n");
			buf.append(" java.util.ArrayList elementList = new java.util.ArrayList();\r\n");
			buf.append(" java.util.ArrayList attribList = new java.util.ArrayList();\r\n");
			buf.append(b);
			buf.append("return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());\r\n }\r\n");
			return buf.toString();
	
	}
	
	private String getEnvelope(){
		StringBuilder b1 = new StringBuilder("private org.apache.axiom.soap.SOAPEnvelope toEnvelope(	org.apache.axiom.soap.SOAPFactory factory,");
		b1.append(this.classname).append(".").append(StringUtils.capitalize(this.methodname)).append(" param,	boolean optimizeContent, QName methodQName)	\r\n	throws AxisFault {try {");
		b1.append("		org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();\r\n ");
		b1.append("			emptyEnvelope.getBody().addChild(param.getOMElement(	").append(this.classname).append(".").append(StringUtils.capitalize(this.methodname)).append(".MY_QNAME,	factory));	\r\n return emptyEnvelope;\r\n");
		b1.append("		} catch (org.apache.axis2.databinding.ADBException e) \r\n{		throw AxisFault.makeFault(e);\r\n	}\r\n		}\r\n");
		return b1.toString();
	}
	
	private String writeStartElement(){
		StringBuilder b1 = new StringBuilder("\r\nprivate void writeStartElement(String prefix,String namespace, String localPart,");
		b1.append("				javax.xml.stream.XMLStreamWriter xmlWriter)\r\n");
		b1.append("				throws javax.xml.stream.XMLStreamException {\r\n");
		b1.append("			String writerPrefix = xmlWriter.getPrefix(namespace);");
		b1.append("			if (writerPrefix != null) {\r\n");
		b1.append("				xmlWriter.writeStartElement(namespace, localPart);\r\n");
		b1.append("			} else {\r\n");
		b1.append("				if (namespace.length() == 0) \r\n");
		b1.append("					prefix = \"\";");
		b1.append("				else if (prefix == null) ");
		b1.append("					prefix = generatePrefix(namespace);\r\n");
		b1.append("				xmlWriter.writeStartElement(prefix, localPart, namespace);\r\n");
		b1.append("				xmlWriter.writeNamespace(prefix, namespace);\r\n");
		b1.append("				xmlWriter.setPrefix(prefix, namespace);\r\n");
		b1.append("			\r\n} \r\n	}");
		return b1.toString();
	}
	
	private String writeAttribute(){
		StringBuilder b1 = new StringBuilder("private void writeAttribute(String prefix,");
		b1.append("			String namespace, String attName,");
		b1.append("			String attValue,");
		b1.append("			javax.xml.stream.XMLStreamWriter xmlWriter)\r\n");
		b1.append("			throws javax.xml.stream.XMLStreamException {\r\n");
		b1.append("		if (xmlWriter.getPrefix(namespace) == null) {\r\n");
		b1.append("			xmlWriter.writeNamespace(prefix, namespace);\r\n");
		b1.append("			xmlWriter.setPrefix(prefix, namespace);}\r\n");
			
		b1.append("		xmlWriter.writeAttribute(namespace, attName, attValue);		}\r\n");
		return b1.toString();
	}
	
	
	private String registerPrefix(){
		
		StringBuilder b1 = new StringBuilder("private String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter,	String namespace) throws javax.xml.stream.XMLStreamException {");
		b1.append("	String prefix = xmlWriter.getPrefix(namespace);");
		b1.append("		if (prefix == null) {");
		b1.append("			prefix = generatePrefix(namespace);");
		b1.append("			javax.xml.namespace.NamespaceContext nsContext = xmlWriter.getNamespaceContext();");
		b1.append("			while (true) {");
		b1.append("				String uri = nsContext.getNamespaceURI(prefix);");
		b1.append("				if (uri == null || uri.length() == 0) ");
		b1.append("					break;");
		b1.append("				prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();");
		b1.append("			}");
		b1.append("			xmlWriter.writeNamespace(prefix, namespace);");
		b1.append("			xmlWriter.setPrefix(prefix, namespace);			}");
		b1.append("		return prefix; }\n");
		return b1.toString();
	} 
	
	private String getHeader(){
		String str ="\n\r import java.util.ArrayList;\n\rimport java.util.Iterator;\r\n";
		str +="import java.util.List;\n\r import org.apache.axis2.client.OperationClient;\n\r import javax.xml.namespace.QName;\n\rimport org.apache.axis2.AxisFault;\r\n";
		str +="import org.apache.axiom.om.OMElement;\n\r \n\r import com.cf.smartlink.util.xml.FileData;\n\r import com.cf.smartlink.util.xml.XMLObject;\r\n";

		String mth ="public class "+this.classname+" extends org.apache.axis2.client.Stub {\n\r";
		mth +="	protected org.apache.axis2.description.AxisOperation[] _operations;\r\n";
	//	mth +="	private static final Logger logger = Logger.getLogger(ServiceA.class);\n\r	";
		mth +="private String url;\r\n	private final String END_POINT = \"/ws/un/services/"+this.servicename+"\";\r\n";
		mth +="	private java.util.Map faultExceptionNameMap = new java.util.HashMap();\n\r	private java.util.Map faultExceptionClassNameMap = new java.util.HashMap();\r\n";
			
		mth +="	public "+this.classname+"(String baseurl)throws AxisFault {		this(null,baseurl,false);	}\r\n";
			
		mth +="	public "+this.classname+"(	org.apache.axis2.context.ConfigurationContext configurationContext,	String baseurl, boolean useSeparateListener)\r\n";
		mth +="			throws AxisFault {	this.url = baseurl;	populateAxisService();	\r\n_serviceClient = new org.apache.axis2.client.ServiceClient(	configurationContext, _service);\r\n";
		mth +="		_serviceClient.getOptions().setTo(	new org.apache.axis2.addressing.EndpointReference(url	+ END_POINT));\r\n		_serviceClient.getOptions().setUseSeparateListener(useSeparateListener);\r\n";
		mth +="		_serviceClient.getOptions().setSoapVersionURI(	org.apache.axiom.soap.SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);}\r\n";

		mth +="	private void populateAxisService() throws AxisFault {_service = new org.apache.axis2.description.AxisService(";
		mth +="				\""+servicename+"\" + 10);";//getUniqueSuffix());
		mth +="		addAnonymousOperations(); org.apache.axis2.description.AxisOperation __operation;\r\n _operations = new org.apache.axis2.description.AxisOperation[1];";
		mth +="		__operation = new org.apache.axis2.description.OutInAxisOperation();\r\n__operation.setName(new QName(";
		mth +="		\""+namespace+"\",	\""+this.methodname+"\"));	_service.addOperation(__operation);	\r\n_operations[0] = __operation;\r\n}\r\n";
		
		return str+mth;
	}
	
	private String getMethodHeader(){
		StringBuilder b1 = new StringBuilder("	public  com.cf.smartlink.util.xml.XMLObject "+this.methodname+"( ").append(StringUtils.capitalize(this.methodname)).append(" ").append( StringUtils.capitalize(this.methodname)).append("18) throws java.rmi.RemoteException	{");
				b1.append(" org.apache.axis2.context.MessageContext _messageContext = null;\r\n");
				b1.append(" try {OperationClient _operationClient = _serviceClient.createClient(_operations[0].getName());\r\n");
				b1.append(" 	_operationClient.getOptions().setAction(\"urn:").append(this.methodname).append("\");");
				b1.append(" 	_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(	true);\r\n");

				b1.append(" 	addPropertyToOperationClient(_operationClient,");
				b1.append(" 	org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,\"&\");\r\n");

					// create a message context
				b1.append(" 	_messageContext = new org.apache.axis2.context.MessageContext();\r\n");

					// create SOAP envelope with that payload
				b1.append(" 	org.apache.axiom.soap.SOAPEnvelope env = null;\r\n");

				b1.append(" 	env = toEnvelope(getFactory(_operationClient.getOptions()");
				b1.append(" 			.getSoapVersionURI()), "+StringUtils.capitalize(this.methodname)+"18,true, new QName(\"").append(methodname).append("\",		\""+methodname+"\"));\r\n");

					// adding SOAP soap_headers
				b1.append(" 	_serviceClient.addHeadersToEnvelope(env);\r\n");
					// set the message context with that soap envelope
				b1.append(" 	_messageContext.setEnvelope(env);");

					// add the message contxt to the operation client
				b1.append(" 	_operationClient.addMessageContext(_messageContext);\r\n");

					// execute the operation client
				b1.append(" 	_operationClient.execute(true);\r\n");

				b1.append(" 	org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient");
				b1.append(" 			.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);\r\n");
				b1.append(" 	org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();\r\n");
				b1.append(" 	return com.cf.smartlink.util.Axis2Utils.getXMLObject(_returnEnv.getBody().getFirstElement());\r\n");
				b1.append(" } catch (AxisFault f) {");

				b1.append(" 	OMElement faultElt = f.getDetail();\r\n");
				b1.append(" 	if (faultElt != null) {");
				b1.append(" 		if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(	faultElt.getQName(), \""+methodname+"\"))) {\r\n");
				b1.append(" 			try {");
				b1.append(" 				String exceptionClassName = (String) faultExceptionClassNameMap");
				b1.append(" 						.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(), \""+methodname+"\"));\r\n");
				b1.append(" 				 Class exceptionClass =  Class.forName(exceptionClassName);\r\n");
				b1.append(" 				 java.lang.reflect.Constructor constructor = exceptionClass.getConstructor(String.class);\r\n");
				b1.append(" 				 Exception ex = ( Exception) constructor.newInstance(f.getMessage());\r\n");
		 
				b1.append(" 				throw new java.rmi.RemoteException(ex.getMessage(), ex);\r\n");
				b1.append(" 			} catch ( Exception e) {\r\n");
				b1.append(" 				throw f;		}\r\n");
				b1.append(" 		} else 	throw f;\r\n");
						
				b1.append(" 	} else 	throw f;\r\n");
				b1.append(" } finally {");
				b1.append(" 	if (_messageContext.getTransportOut() != null) {\r\n");
				b1.append(" 		_messageContext.getTransportOut().getSender().cleanup(_messageContext);\r\n");
				b1.append(" \r\n	}	\r\n}\r\n	}\r\n");
				return b1.toString();

	}
	
	
	private String generateMainServiceClient() throws Exception {
	
		StringBuilder buf = new StringBuilder();	
	
		buf.append("package com.cf.smartlink.mainservice;\r\n");
		buf.append("import javax.xml.soap.MessageFactory;;\r\n");
			buf.append("import javax.xml.soap.SOAPBody;;\r\n");
			buf.append("import javax.xml.soap.SOAPConnection;;\r\n");
			buf.append("import javax.xml.soap.SOAPConnectionFactory;;\r\n");
			buf.append("import javax.xml.soap.SOAPElement;;\r\n");
			buf.append("import javax.xml.soap.SOAPEnvelope;;\r\n");
			buf.append("import javax.xml.soap.SOAPMessage;;\r\n");
			buf.append("import javax.xml.soap.SOAPPart;;\r\n");
			buf.append("import java.io.*;;\r\n");

			buf.append("import com.cf.smartlink.util.FileUtils;;\r\n");
			buf.append("import com.cf.smartlink.util.xml.XMLObject;;\r\n");
			buf.append("import com.cf.smartlink.util.xml.XmlReader;;\r\n");

			buf.append("public class "+this.classname+" {;\r\n");

			buf.append("	static com.cf.smartlink.log.Log logger = com.cf.smartlink.log.LogSource.getInstance("+this.classname+".class);;\r\n");

			buf.append("	String url;;\r\n");
			buf.append("	public MainServiceClient(String inpurl){;\r\n");
			buf.append("		url = inpurl+\"/ws/services/mainservice\";\r\n");
			buf.append("	};\r\n");


			buf.append("public XMLObject "+this.methodname+"(") ;
			int pi = 0;
			for(String pn : this.p){
				buf.append(" "+this.ptype.get(pi)+"  "+pn);
				if(pi > 0)
					buf.append(", ");
				pi++;
			}
			buf.append(") throws Exception {\r\n");
			buf.append("XMLObject returnobj = new XMLObject();\r\n");
			buf.append("ByteArrayOutputStream fout = null;	\r\n");
			buf.append("try {\r\n");		

  	     
//First create the connection
			buf.append("SOAPConnectionFactory soapConnFactory =        SOAPConnectionFactory.newInstance();\r\n");
			buf.append("SOAPConnection connection =       soapConnFactory.createConnection();\r\n");
	//Next, create the actual message
			buf.append("MessageFactory messageFactory = MessageFactory.newInstance();\r\n");
			buf.append("SOAPMessage message = messageFactory.createMessage();\r\n");
	//Create objects for the message parts            
			buf.append("SOAPPart soapPart =     message.getSOAPPart();\r\n");
			buf.append("SOAPEnvelope envelope = soapPart.getEnvelope();\r\n");
			buf.append("SOAPBody body =         envelope.getBody();\r\n");
			buf.append("logger.debug(\"send message:4\");\r\n");
			buf.append("SOAPElement bodyElement =   body.addChildElement(envelope.createName(\""+this.methodname+"\" ,  \"ns1\", \"http://general.service.webservices.skire.com\"));\r\n");
			for(String pn : this.p){
				buf.append("bodyElement.addChildElement(\""+pn+"\").addTextNode("+pn+")\r\n");
			}
	//Add content
	  //Save the message
			buf.append("message.saveChanges();\r\n");
			buf.append("message.writeTo(System.out);\r\n");
			buf.append("logger.debug(\"send message:\"+message.toString());\r\n");
	  //Send the message
			buf.append("SOAPMessage reply = connection.call(message, url);\r\n");
	//logger.debug(\"return message:\"+reply);\r\n");
			buf.append("fout = new ByteArrayOutputStream();\r\n");
			buf.append("reply.writeTo(fout);\r\n");
			buf.append("connection.close();\r\n ; fout.close();\r\n");
			buf.append("return parseData(os.toString());\r\n");
			buf.append("} catch(javax.xml.soap.SOAPException se) {\r\n");
			buf.append(" logger.error(se,se);\r\n");
			buf.append(" returnobj.setStatusCode(500);\r\n");
			buf.append("returnobj.setErrorStatus(new String[] {se.getMessage()} );\r\n");
			buf.append(" } catch(Exception e) {\r\n");
			buf.append("logger.error(e,e);\r\n");
			buf.append(" returnobj.setStatusCode(500);\r\n");
			buf.append("returnobj.setErrorStatus(new String[] {e.getMessage()} );\r\n");
			buf.append("}\r\n");
			buf.append("return returnobj;\r\n");
			buf.append("}\r\n");
			buf.append("}\r\n");
			
			
			buf.append("private XMLObject parseData(String data) throws Exception{\r\n");
				
			buf.append("	logger.debug(\"start parsing\");\r\n");
			buf.append("	XmlReader xmlReader = new XmlReader();\r\n");
			buf.append("	XMLObject hvalues = xmlReader.parseXmlData(data);\r\n");
			buf.append("	return hvalues;\r\n");
			buf.append("}\r\n");
			return buf.toString();
	}
}
