package com.cf.tkconnect.data;

import java.io.File;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cf.tkconnect.data.process.ProcessBPXMLTemplate;
import com.cf.tkconnect.data.process.SaveData;

import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.process.ResponseObject;
import com.cf.tkconnect.process.RunWebServices;
import com.cf.tkconnect.util.DataUtils;
import com.cf.tkconnect.util.FileUtils;
import com.cf.tkconnect.util.InitialSetUp;
import com.cf.tkconnect.util.WSUtil;
import static org.apache.commons.lang3.math.NumberUtils.toInt;

public class GenerateXML {
	
	static Log logger = LogSource.getInstance(GenerateXML.class);
	HttpServletRequest req;
	HttpServletResponse res;
	//String method_name;
	String prefix;
	List<String[]> upperdata;
	List<String[]> lidata;
	List<String[]> upperrespdata;
	Map<String,List<String[]>> lidataMap = new HashMap<String,List<String[]>>();
	ProcessBPXMLTemplate bptemp;
	String bpname;
	List<String> bpdelist = new ArrayList<String>();
	List<String> bplidelist = new ArrayList<String>();
	List<Map<String,String>> xmlmap = new ArrayList<Map<String,String>>();
	List<ResponseObject> resplist = new ArrayList<ResponseObject>();
	Map<String,String[]> respmap = new HashMap<String,String[]>();
	SaveData sd;
	String inputFileName;
	String outputFileName;
	long service_id = 0;
	long savedFileId=0;
	boolean batch =false;
	int batchsize = 10;
	
	
	public GenerateXML(HttpServletRequest req, HttpServletResponse res){
		this.req = req;
		this.res = res;
		//this.method_name = req.getParameter("method_name");
		this.prefix = req.getParameter("prefix");
		this.batch = ("yes".equals(req.getParameter("batch"))?true:false) ;
		this.batchsize = toInt(req.getParameter("batchsize"),10);
	}
	public GenerateXML(String batch){
		this.batch = ("yes".equals(batch)?true:false) ;
	}
	
	public String process() throws Exception{
		sd = new SaveData(req,res);
		sd.processSave(1);
		this.bpname = sd.getBpName();
		this.bptemp = sd.getBpTemplate();
		this.upperdata = sd.getUpperData();
		this.lidata = sd.getLiData();
		this.savedFileId = sd.getSavedFileId();
		setup();
		if(batch)
			setBatchBPXML();
		else
			setBPXML();
		return runWebService();
		
	}
	
	public String processFileData(ProcessBPXMLTemplate bptemp,Map<String,Object> studiomap,List<String[]> upperrespdata,
				List<String[]> lidata,String inputfilename) throws Exception{

		sd = new SaveData(bptemp,studiomap);
		this.bpname = (String)studiomap.get("studio_name");
		this.prefix = (String)studiomap.get("studio_prefix");
		this.bptemp = bptemp;
		this.upperdata = upperrespdata;
		this.lidata = lidata;
		this.inputFileName = inputfilename;
		sd.setAbsoluteFileName(inputfilename);
		this.savedFileId = 0;
		if(logger.isDebugEnabled())
			logger.debug("processFileData the ---- prefix "+prefix+" bpname: "+bpname+"  ::"+inputfilename);
		setup();
		setBPXML();
		return runWebService();
		
	}
	
	private String runWebService() throws Exception{
		RunWebServices sr;
		int count = 0;
		upperrespdata = new ArrayList<String[]>();
		StringBuilder buf =new StringBuilder("[");
		ResponseObject respobj = null;
		for(Map<String,String> map : this.xmlmap){
			if(logger.isDebugEnabled())
				logger.debug("sending the xml data :: "+map);
			sr = new RunWebServices(map);
			StringBuilder sbuf = new StringBuilder();
			respobj = sr.runWebService(sbuf);
			
			String comma = "";
			if(count > 0) 
				comma=",";
			String data = "{ \"srno\":\""+map.get("srno")+"\", \"result\":" +sbuf.toString()+"}";
			buf.append(comma+data);
			count++;
			String[] respstr = respmap.get(map.get("srno"));
			if(logger.isDebugEnabled())
				  logger.debug(" The runWebService :::::::::::::::resp: " +buf);
			if(logger.isDebugEnabled())
				logger.debug("object recd is  "+respobj+"  ::"+respstr);
		//	resplist.add(respobj);
			respstr[1] = respobj.getStatusCode()+"";
			respstr[2] = respobj.getErrors();
			upperrespdata.add(respstr);
		}
		buf.append("]");
		setResponseFileName();
		if(logger.isDebugEnabled())
			  logger.debug(" The runWebService ::::::outputFileName::::::::: " +outputFileName);
		if(batch){
			String[] resp = new String[2];
			resp[0] =   respobj.getStatusCode()+"";
			resp[1] = respobj.getErrors();
			sd.buildResponseExcel(this.outputFileName, upperdata, lidata,batch,resp);
		}else
			sd.buildResponseExcel(this.outputFileName, upperrespdata, lidata,batch,null);
		createServiceRecord();
		String ret = "{\"service_id\":"+this.service_id+",\"saved_file_id\":"+this.savedFileId+",\"data\":"+buf.toString()+"   }";
		return ret;
	}
	
	public String getResponseFile(){
		return this.outputFileName;
	}

	private void setResponseFileName(){
		inputFileName = sd.getAbsoluteFileName();
		String path = FileUtils.getFilePath( inputFileName) ;
		String name = FileUtils.getFileNameWithoutExtension(FileUtils.getFileName( inputFileName))+"_response." ;
		String ext = FileUtils.getFileExtension( inputFileName) ;
		this.outputFileName = path+name+ext;
		if(logger.isDebugEnabled())
			logger.debug("object setResponseFileName is  "+name+"  ::"+this.outputFileName);
	}
	
	
	public List<Map<String,String>> getBPXML(){
		return this.xmlmap;
	}
	
	private void setup() throws Exception{
		if( this.lidata != null ){
			
			for(String[] strs : this.lidata){
				if(lidataMap.containsKey(strs[0]))
					lidataMap.get(strs[0]).add(strs);
				else{
					List<String[]> arr = new ArrayList<String[]>();
					arr.add(strs);
					lidataMap.put(strs[0], arr);
				}
			}
		}
		Iterator<String> it =  bptemp.getBPMap().keySet().iterator();
		while(it.hasNext())
			bpdelist.add(it.next());
				
		it =  bptemp.getBLIPMap().keySet().iterator();
		while(it.hasNext())
			bplidelist.add(it.next());
	}
	
	private void setBPXML() throws Exception{
		
		Map<String,String> map = new HashMap<String,String>();
		StringBuilder buf = new StringBuilder("<List_Wrapper>");
		int count = 0;
		String methodname = "";
		String path = FileUtils.getFilePath( inputFileName) ;
		for(String[] strs : this.upperdata){
			if(strs == null || strs.length < 3)
				continue;
			String[] rstrs = new String[strs.length+2]; 
			map = new HashMap<String,String>();
			String m = getMethodName(strs[1]);
			if(methodname.length() == 0 && m != null && m.trim().length() > 0 )
				methodname = m;
			if(m.equalsIgnoreCase(methodname) || m== null || m.trim().length()==0)
				map.put("method_name",methodname );
			else{
				map.put("method_name",m );
				methodname = m; // set it to a new method
			}	
			map.put("projectNumber", strs[2]);
			map.put("srno", strs[0]);
			map.put("shortname", InitialSetUp.company.get("shortname"));
			map.put("authcode", InitialSetUp.company.get("authcode"));
			map.put("BPName", this.bpname);
			buf = new StringBuilder("<List_Wrapper>");
			buf.append("<_bp>");
			for(int i = 3; i < strs.length; i++)
				buf.append("<"+bpdelist.get(i-3)+">").append(WSUtil.filter(strs[i])).append("</"+bpdelist.get(i-3)+">\n");
			//collect the li-items
			buf.append(getLineItems(strs[0]));
			buf.append("</_bp>");
			buf.append("</List_Wrapper>");
			map.put("BPXML", buf.toString());
			xmlmap.add(map);
			logger.info("bp generated ::   "+count+" bpxml :"+buf);
			writeXMLData(map);
			
			for(int i = 0; i < strs.length; i++){
				if(i == 0)
					rstrs[i] = strs[i];
				else
					rstrs[i+2] = strs[i];
			}
			count++;
			respmap.put(strs[0], rstrs);
			
		}
	}
	private void writeXMLData(	Map<String,String> map) {
		try{
			// save this file generate a
			String file = InitialSetUp.basefilepath+"/data/xml/"+FileUtils.getTodaysDateString();
			FileUtils.checkAndCreatDir(file);
			String outfile = file+"/"+FileUtils.getTodaysDateTimeString()+"run_.xml";
			StringBuilder b = new StringBuilder("<runxml>");
			b.append("<project>"+map.get("projectNumber")+"</project>");
			b.append("<BPName>"+map.get("BPName")+"</BPName>");
			b.append("<method_name>"+map.get("method_name")+"</method_name>");
			b.append("<filename>"+inputFileName+"</filename>");
			b.append("<data>"+map.get("BPXML")+"</date>").append("</runxml>");
		
			FileUtils.writeContent(outfile, b.toString());
		}catch(Exception e){}
	}

	private void setBatchBPXML() throws Exception{
		
		Map<String,String> map = new HashMap<String,String>();
		StringBuilder buf = new StringBuilder("<List_Wrapper>");
		int count = 0;
		int batchcount = 0;
		if(this.upperdata.size() == 0)
			return;
		String[] st = this.upperdata.get(0);
		if(st == null || st.length < 3){
			if(logger.isDebugEnabled())
				logger.debug("setBatch xmlupper  data :"+this.upperdata);
			return;
		}	
		String m = getMethodName(st[1]);
		String projectnumber = st[2];
		for(String[] strs : this.upperdata){
			if(strs == null || strs.length < 3)
				continue;
			String[] rstrs = new String[strs.length+2]; 
			
			if(count == 0 && (m == null || m.trim().length() == 0))
					throw new Exception("Invalid method name "+strs[1]); 
			if(count % this.batchsize == 0){
				if(batchcount > 0){
					buf.append("</List_Wrapper>");
					map.put("BPXML", buf.toString());
					xmlmap.add(map);
				}
				batchcount++;
				if(logger.isDebugEnabled())
					logger.debug("batch generated "+batchcount+" bpxml :"+buf);
				map = new HashMap<String,String>();
				buf = new StringBuilder("<List_Wrapper>");
				map.put("method_name",m );
				map.put("projectNumber", projectnumber);
				map.put("srno", strs[0]);
				map.put("shortname", InitialSetUp.company.get("shortname"));
				map.put("authcode", InitialSetUp.company.get("authcode"));
				map.put("BPName", this.bpname);
			}
			//collect bp data
			buf.append("<_bp>");
			for(int i = 3; i < strs.length; i++)
				buf.append("<"+bpdelist.get(i-3)+">").append(WSUtil.filter(strs[i])).append("</"+bpdelist.get(i-3)+">\n");
			//collect the li-items
			buf.append(getLineItems(strs[0]));
			buf.append("</_bp>");
			
			for(int i = 0; i < strs.length; i++){
				if(i == 0)
					rstrs[i] = strs[i];
				else
					rstrs[i+2] = strs[i];
			}
			count++;
			//Map<String,String[]> rmap = new HashMap<String,String[]>();
			respmap.put(strs[0], rstrs);
			
		}//for
		buf.append("</List_Wrapper>");
		map.put("BPXML", buf.toString());
		xmlmap.add(map);
			
	}

	private String getLineItems(String srno){
		if(!this.lidataMap.containsKey(srno))
			return "";
		StringBuilder buf = new StringBuilder("\n");
		List<String[]> liitems = this.lidataMap.get(srno);
		
		for(String[] strs : liitems){
			buf.append("\n<_bp_lineitems>");
			for(int i = 1; i < strs.length; i++)
				buf.append("<"+bplidelist.get(i-1)+">").append(WSUtil.filter(strs[i])).append("</"+bplidelist.get(i-1)+">\n");
			buf.append("\n</_bp_lineitems>");
		}
		return buf.toString();
		
	}
	
	private String getMethodName(String mn){
		if(mn.toLowerCase().equals("createBPRecord".toLowerCase()))
			return "createBPRecord";
		//if(mn.toLowerCase().equals("updateBPRecordV2".toLowerCase()))
		//	return "updateBPRecordV2";
		if(mn.toLowerCase().equals("updateBPRecord".toLowerCase()))
			return "updateBPRecord";
		//if(mn.toLowerCase().equals("createUpdateBPRecord".toLowerCase()))
		//	return "createUpdateBPRecord";
		return "";
	}
	
	protected void createServiceRecord() throws Exception{
		
		try {
			logger.debug(" Create service ");
			Map<String,String> map = new HashMap<String,String>();
			map.put("file_path" ,FileUtils.getFilePath(this.inputFileName));
			map.put("input_file_name", this.inputFileName);
			map.put("output_file_name", (this.outputFileName==null?"":this.outputFileName));
			map.put("external_file_name", FileUtils.getFileName( inputFileName));
			map.put("prefix",this.prefix);
			map.put("method_name","create/updatebprecord");
			map.put("saved_file_id",""+this.savedFileId);
			this.service_id = DataUtils.createAuditServiceRecord(map);
			if(logger.isDebugEnabled())
				logger.debug(" The object inserted:::::::::::::::   :"+service_id);
		}catch(Exception e){
			logger.error(e,e);
		} 
		
	}
}
