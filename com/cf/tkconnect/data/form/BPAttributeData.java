package com.cf.tkconnect.data.form;


import com.cf.tkconnect.data.form.BPAttributeData;
import com.cf.tkconnect.data.process.ProcessBPXMLTemplate;
import com.cf.tkconnect.database.SqlUtils;
import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.util.FileUtils;
import com.cf.tkconnect.util.WSUtil;
import static org.apache.commons.lang3.math.NumberUtils.toInt;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class BPAttributeData {
	
	static Log logger = LogSource.getInstance(BPAttributeData.class);
	
	String responseXML = null;
	String attr_name;
	int company_id =1;
	String prev_file_path;
	String prev_file_name;
	String new_file_path;
	String new_file_name;
	String attr_prefix;
	int studio_version;
	Date published_date;
	boolean prevTemplateFound = false;
	ProcessBPXMLTemplate bptempPrev = null;
	ProcessBPXMLTemplate bptempNew = null;
	boolean hasXMLdata = false;

	public BPAttributeData(int company_id,String attr_prefix, String attr_name){
//		this.company_id = company_id;
		this.attr_prefix = attr_prefix;
		this.attr_name = attr_name;
	}
	public BPAttributeData(String attr_prefix, String attr_name){
//		this.company_id = company_id;
		this.attr_prefix = attr_prefix;
		this.attr_name = attr_name;
	}
	public BPAttributeData(int company_id, String responseXML,String attr_prefix, String attr_name){
		this.responseXML = responseXML;
//		this.company_id = company_id;
		this.attr_prefix = attr_prefix;
		this.attr_name = attr_name;
		this.hasXMLdata = true;
	}
	
	
	
	private void init() throws Exception{
		// get record from database
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			conn = SqlUtils.getConnection();
			ps = conn.prepareStatement("select * from attribute_templates where  attr_prefix=? ");
			ps.setString(1, attr_prefix);
			rs = ps.executeQuery();
			if(rs != null && rs.next()){
				prev_file_path = rs.getString("file_path");
				prev_file_name = rs.getString("file_name");
				prevTemplateFound = true;
				this.attr_name = rs.getString("attr_name");
			}else
				prevTemplateFound = false;
			
		}finally{
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		
	}
	
	private int setRecord() throws Exception{
		Connection conn = null;
		PreparedStatement ps = null;
		int update = 0;
	
		try{
			logger.info(" create/updated attr file ------------- prevTemplateFound "+prevTemplateFound+" :: hasXMLdata "+hasXMLdata+"  prefix:"+attr_prefix);
			conn = SqlUtils.getConnection();
			if(prevTemplateFound)		// found record  update record 
				ps = conn.prepareStatement("update attribute_templates set lastmodified = ?, file_name=?, file_path= ? where  attr_prefix = ?  ");
			else		// found record  update record 
				ps = conn.prepareStatement("insert into attribute_templates (lastmodified,file_name,file_path,attr_prefix,attr_name)  values (?,?,?,?,?)  ");
			ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			ps.setString(2, new_file_name);
			ps.setString(3, new_file_path);
			ps.setString(4, attr_prefix);
			if(!prevTemplateFound)
				ps.setString(5, attr_name);
			update =  ps.executeUpdate();
			logger.info(" create/updated attr result "+update+" :: hasXMLdata "+hasXMLdata);
			SqlUtils.closeStatement(ps);
			// now also update studio_log
			if(this.hasXMLdata){
				String s = "update studio_log set lastmodified = ?, studio_version =?, published_date=? where studio_prefix = ?";
				ps = conn.prepareStatement(s);
				ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
				ps.setInt(2, this.studio_version);
				ps.setTimestamp(3, (this.published_date == null?null: new Timestamp(this.published_date.getTime())));
				ps.setString(4, attr_prefix);
				update =  ps.executeUpdate();
				logger.info(s+" update "+update+" :: studio_version "+studio_version+" :"+published_date);
			}
		}finally{
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
		return update;
	}
	
	
	public String updateAndCompare() throws Exception {
		init();
		String str = check();
		setRecord();
		return str;
	}
	
	public String getAttributeDetails() throws Exception {// 
		init();
		logger.debug("getAttributeDetails ---------"+prevTemplateFound);
		if(prevTemplateFound && !this.hasXMLdata){//  prev found get a template
			String prevfilename = prev_file_path+File.separator+prev_file_name;
			return buildBPData(prevfilename);
		}else{// get the XML save it, update database 
			setNewFile();
			
			String str =  buildBPData((new_file_path+File.separator+new_file_name));
			setRecord();
			return str;
		}
	}
	public ProcessBPXMLTemplate getBPDesignDetails() throws Exception {// 
		init();
		logger.debug("ProcessBPXMLTemplate ------ "+prevTemplateFound);
		if(prevTemplateFound && !this.hasXMLdata){//  prev found get a template
			String prevfilename = prev_file_path+File.separator+prev_file_name;
			return getBPInfo(prevfilename);
		}else{// get the XML save it, update database 
			setNewFile();
			setRecord();
			return getBPInfo((new_file_path+File.separator+new_file_name));
		}
	}
	
	private ProcessBPXMLTemplate getBPInfo(String filename) throws Exception {
		File f = new File(filename);
		if(!f.exists())
			throw new Exception("Could not find file :"+filename);
		bptempPrev = null;
		bptempPrev = new  ProcessBPXMLTemplate(filename,true);
		bptempPrev.processBPtemplate();
		/*
		Map<String,String> bplimap = bptempPrev.getBLIPMap();
		Map<String,String> bpmap = bptempPrev.getBPMap();
		List<Map<String,Object>> demap = bptempPrev.getDEList() ;
		*/
		//logger.debug("buildBPData "+filename+"  "+buf);
		return bptempPrev;
	}
	
	
	
	private String buildBPData(String filename) throws Exception {
		File f = new File(filename);
		if(!f.exists())
			throw new Exception("Could not find file :"+filename);
		bptempPrev = null;
		bptempPrev = new  ProcessBPXMLTemplate(filename,true);
		bptempPrev.processBPtemplate();
		this.studio_version = toInt(bptempPrev.getStudio().get("studio_version"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String pubstr = "";
		if(bptempPrev.getStudio().containsKey("published_date") ){
			pubstr = bptempPrev.getStudio().get("published_date");
			if(pubstr != null && pubstr.length() > 19)
				pubstr = pubstr.substring(0, 19);
			logger.debug("buildBPData ---------published_date**** "+pubstr );
			this.published_date = sdf.parse(pubstr , new ParsePosition(0));
		}	
		Map<String,String> bplimap = bptempPrev.getBLIPMap();
		Map<String,String> bpmap = bptempPrev.getBPMap();
		List<Map<String,Object>> demap = bptempPrev.getDEList() ;
		StringBuilder buf = new StringBuilder("{");
		buf.append("\"studio_version\": \"").append(this.studio_version).append("\",");
		buf.append("\"published_date\": \"").append(pubstr).append("\",");
		buf.append("\"bp_de\": \"").append(buildString(bpmap)).append("\",");
		buf.append("\"bp_de\": \"").append(buildString(bpmap)).append("\",");
		buf.append("\"bp_li_de\":\"").append(buildString(bplimap)).append("\",");
		buf.append("\"de\":").append(buildDEJson(demap)).append("}");
		//logger.debug("buildBPData "+filename+"  "+buf);
		return buf.toString();
	}

	public String getBPJsonData() throws Exception {
		logger.debug("getBPJsonData ---------**** "+bptempPrev.getStudio() );
		this.studio_version = toInt(bptempPrev.getStudio().get("studio_version"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.z");
		this.published_date = sdf.parse(bptempPrev.getStudio().get("published_date") , new ParsePosition(0));
		Map<String,String> bplimap = bptempPrev.getBLIPMap();
		Map<String,String> bpmap = bptempPrev.getBPMap();
		List<Map<String,Object>> demap = bptempPrev.getDEList() ;
		StringBuilder buf = new StringBuilder("{");
		buf.append("\"bp_de\": \"").append(buildString(bpmap)).append("\",");
		buf.append("\"bp_li_de\":\"").append(buildString(bplimap)).append("\",");
		buf.append("\"de\":").append(buildDEJson(demap)).append("}");
		//logger.debug("buildBPData "+filename+"  "+buf);
		return buf.toString();
	}

	
	private String buildString(Map<String,String> bpmap){// build comma sep string of de's
		StringBuilder buf = new StringBuilder("");
		Iterator<String> it = bpmap.keySet().iterator();
		int count = 0;
		while(it.hasNext()){
			if(count > 0)
				buf.append(",");
			buf.append(it.next());
			count++;
		}
		return buf.toString();
	}

	private String buildDEJson(List<Map<String,Object>> bpinfolist){// build comma sep string of de's
		StringBuilder buf = new StringBuilder("[");
		//logger.debug("options ---------**** "+bpinfolist );
		for(int i = 0; i < bpinfolist.size(); i++){
			Map<String,Object> bpmap = bpinfolist.get(i);
			Iterator<String> it = bpmap.keySet().iterator();
		//	logger.debug("options ----&&&&&&&&&** "+bpmap );
			int count = 0;
			if(i > 0)
				buf.append(",");
			buf.append("{");
			while(it.hasNext()){
				String key = it.next();
				if(count > 0)
					buf.append(",");
				if(key.equalsIgnoreCase("Options")){
					List<Map<String,String>> list = ( List<Map<String,String>>) bpmap.get(key);
				//	logger.debug(key+"   options **** "+list );
					buf.append("\"options\":").append("[");
					StringBuilder debuf = new StringBuilder("");
					for(int j = 0; j < list.size(); j++ ){
						Map<String,String> demap = list.get(j);
						Iterator<String> deit = demap.keySet().iterator();
						if(j > 0)
							debuf.append(",");
						debuf.append("{");
						int decount = 0;
						while(deit.hasNext()){
							if(decount > 0)
								debuf.append(",");
							String dekey = deit.next();
							String devalue = demap.get(dekey);
							if(devalue == null || devalue.equalsIgnoreCase("null"))
								devalue = "";
							debuf.append("\"").append(dekey).append("\":");
							debuf.append("\"").append(WSUtil.jsfilter2(devalue)).append("\"");
							decount++;
						}
						debuf.append("}");
					}//j 
					buf.append(debuf);
					buf.append("]");
				}else{
					buf.append("\"").append(key);
					buf.append("\": \"");
					String value = (String)bpmap.get(key);
					if(value == null || value.equalsIgnoreCase("null"))
						value = "";
					buf.append(WSUtil.jsfilter2(value));
					buf.append("\"");
				}
				count++;
			}// while it
			buf.append("}");
		}// for i
		buf.append("]");
		return buf.toString();
	}

	private String check() throws Exception {
		// get the prev version
		if(!prevTemplateFound)
			return null; // nothing to compare
		String prevfilename = prev_file_path+File.separator+prev_file_name;
		File f = new File(prevfilename);
		if(!f.exists())
			throw new Exception("Could not find file :"+prevfilename);
		
		bptempPrev = new  ProcessBPXMLTemplate(prevfilename,true);
		bptempPrev.processBPtemplate();
		setNewFile();
		bptempNew = new  ProcessBPXMLTemplate(new_file_path+File.separator+new_file_name,true);
		bptempNew.processBPtemplate();
		return compare();
	}

	private void setNewFile() throws Exception {
		String[] str = FileUtils.writeXMLContents("bp_info",attr_prefix, company_id, responseXML);
		new_file_path = str[1];
		new_file_name = str[0];
		logger.debug("setNewFile "+new_file_name+" "+new_file_path);
		
	}
	
	private String compare() {
		Map<String,String> newBPMap =bptempNew.getBPMap();
		Map<String,String> prevBPMap =bptempPrev.getBPMap();
//		List<Map<String,Object>> prevDElist = bptempPrev.getDEList();
		StringBuilder rbuf = new StringBuilder("{");
		rbuf.append("\"deletedDE\" :\""+  getMissingDE(prevBPMap,newBPMap)+"\",");//deleted BP DE
		rbuf.append("\"newDE\" :\""+  getMissingDE(newBPMap,prevBPMap)+"\",");// new BP DE
		rbuf.append("\"deletedLIDE\" :\""+  getMissingDE(bptempPrev.getBLIPMap(),bptempNew.getBLIPMap())+"\",");//deleted BP LI DE
		rbuf.append("\"newLIDE\" :\""+ getMissingDE(bptempNew.getBLIPMap(),bptempPrev.getBLIPMap())+"\",");// new BP LI DE
		StringBuilder buf = new StringBuilder();
		Iterator<String> it = newBPMap.keySet().iterator();
		while(it.hasNext()){
			String keyde = it.next();
			if(buf.length() > 0)
				buf.append(",");
			buf.append(keyde);
		}
		rbuf.append("\"bpDE\" :\""+ buf.toString()+"\",");
		Map<String,String> newBPLIMap =bptempNew.getBLIPMap();
		it = newBPLIMap.keySet().iterator();
		while(it.hasNext()){
			String keyde = it.next();
			if(buf.length() > 0)
				buf.append(",");
			buf.append(keyde);
			// options
		}
		rbuf.append("\"bpLIDE\" :\""+ buf.toString()+"\",");
		List<Map<String,Object>> newDElist = bptempNew.getDEList();
		rbuf.append(getDEJson(newDElist));
		rbuf.append("}");
		return rbuf.toString();
		
	}
	
	private String getDEJson(List<Map<String,Object>> newDElist){
		if(newDElist == null || newDElist.size()==0)
			return "[]";
		StringBuilder buf = new StringBuilder("[");
		for(Map<String,Object> map : newDElist){
			Iterator<String> it = map.keySet().iterator();
			buf.append("{");
			int i = 0;
			while(it.hasNext()){
				String key = it.next();
				if(i > 0)
					buf.append(",");
				if(key.equalsIgnoreCase("Options")){
					StringBuilder opbuf = new StringBuilder("[");
					List<Map<String,String>> optlist = (List<Map<String,String>>)map.get("Options");
					int count = 0;
					for(Map<String,String> omap : optlist){
						if(count > 0)
							opbuf.append(",");
						opbuf.append("{");
						opbuf.append("\"option_name\":\"").append(WSUtil.filter((String)omap.get("Name"))).append("\",");// filter 
						opbuf.append("\"option_value\":\"").append(WSUtil.filter((String)omap.get("Value"))).append("\",");
						opbuf.append("}\n");
						count++;
					}
					opbuf.append("]");
					buf.append("\""+key+"\":").append(opbuf.toString());
				}else{
					buf.append("\""+key+"\":\"").append((String)map.get(key)).append("\"");
				}
				i++;
			}
		}		
		buf.append("]");
		return buf.toString();
	}
	
	// Name, Label,Type,InputType,DataDefinition,Options,Mandatory
	private String getMissingDE(Map<String,String> map, Map<String,String> checkmap){
		StringBuilder buf = new StringBuilder();
		Iterator<String> it = map.keySet().iterator();
		while(it.hasNext()){
			String keyde = it.next();
			if(!checkmap.containsKey(keyde)){
				if(buf.length() > 0)
					buf.append(",");
				buf.append(keyde);
			}
		}
		return buf.toString();
	}
	
	public String getAttrName(){
		return this.attr_name;
	}
	
}
