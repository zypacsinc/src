package com.cf.tkconnect.data.process;

import static org.apache.commons.lang3.math.NumberUtils.toLong;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cf.tkconnect.data.TKUnifierMetaData;
import com.cf.tkconnect.data.WriteExcelBook;
import com.cf.tkconnect.data.form.BPAttributeData;
import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.util.DataUtils;
import com.cf.tkconnect.util.FileUtils;

import au.com.bytecode.opencsv.CSVReader;

public class SaveData {

	static Log logger = LogSource.getInstance(SaveData.class);
	HttpServletRequest req;
	HttpServletResponse res;
	String method_name;

	Map<String,String> paramMap = new HashMap<String,String>();
	String abs_file_name;
	String save_name;
	String prefix;
	long fileid;
	String upper_csvdata;
	String li_csvdata;
	char separator = ',';
	List<String[]> upperdata;
	List<String[]> lidata;
	ProcessBPXMLTemplate bptemp;
	String bpname;
	Map<String,Object> studiomap ;
	
	public SaveData(HttpServletRequest req, HttpServletResponse res){
		this.req = req;
		this.res = res;
		//this.user_id =  toInt(req.getParameter("user_id"));
		this.upper_csvdata = req.getParameter("upper_csvdata");
		this.li_csvdata = req.getParameter("li_csvdata");
		this.method_name = req.getParameter("method_name");
		this.prefix = req.getParameter("prefix");
		this.save_name = req.getParameter("save_name");
		this.fileid = toLong(req.getParameter("fileid"));
		//this.paramMap.put("input_zip_file","");
		if(logger.isDebugEnabled())
			logger.debug("File SaveData path ************fileid  "+this.fileid+" m: "+this.method_name+" sn:"+this.save_name);
	}
	
	public SaveData(ProcessBPXMLTemplate bptemp, Map<String,Object> studiomap){
		this.bptemp = bptemp;
		this.studiomap = studiomap;
		
	}
	
	public String processSave(int type) throws Exception{
		
		try{
			buildCSVData();
			//if there is a file id
			String filename = "";
			Map<String,String> fmap = null;
			if(fileid == 0){
				filename = "unifier_"+this.prefix+"_bp.xlsx";
				 fmap =DataUtils.processSaveData(FileUtils.getFilePath(1, filename), this.save_name,filename, this.prefix,type) ;// type 0 -save 1- run
				 this.fileid = toLong(fmap.get("fileid"));
			}else
				fmap =DataUtils.getSaveRecord(fileid);
			abs_file_name =fmap.get("file_path")+File.separator+ FileUtils.getFileName( fmap.get("file_name"));
			if(logger.isDebugEnabled())
				logger.debug("File SaveData file_path ************ --file_path  "+abs_file_name);
			buildExcel();
		}catch(Exception e){
			return "{\"errors\": \""+e.getMessage()+"\" }";
		}
		return "{\"fileid\":'+fileid+'}";
	}
	
	
	public void buildCSVData() throws Exception{
		Reader reader = new java.io.StringReader(upper_csvdata);
		CSVReader csvreader = new CSVReader(reader, separator);
		try{
			this.upperdata = csvreader.readAll();
			csvreader.close();
			if(logger.isDebugEnabled())
				logger.debug("File buildCSVData path ************upperdata  "+upperdata);
			reader = new java.io.StringReader(li_csvdata);
			csvreader = new CSVReader(reader, separator);
			this.lidata = csvreader.readAll();
			
		
		}finally{
			csvreader.close();
		}
	}
	public void setAbsoluteFileName(String filename){
		this.abs_file_name = filename;
	}
	public String getAbsoluteFileName(){
		return this.abs_file_name;
	}
	
	public List<String[]> getUpperData(){
		return this.upperdata;
	}

	public List<String[]> getLiData(){
		return this.lidata;
	}

	public String getBpName(){
		return this.bpname;
	}
	
	public Map<String,Object> getStudio(){
		return this.studiomap;
	}
	
	public long getSavedFileId(){
		return this.fileid ;
	}
	
	public ProcessBPXMLTemplate getBpTemplate(){
		return this.bptemp ;
	}
	
	private void buildExcel() throws Exception{
		try{
			
			//build the data
			BPAttributeData bpattr = new BPAttributeData(this.prefix,"");
			this.bptemp =  bpattr.getBPDesignDetails();
			TKUnifierMetaData ud = new TKUnifierMetaData();
			studiomap = ud.getStudioInfo(this.prefix);
			this.bpname = (String) studiomap.get("studio_name");
			if(logger.isDebugEnabled())
				logger.debug("buildExcel  ::"+this.prefix+"   :"+studiomap);
			WriteExcelBook bk = new WriteExcelBook(bptemp, studiomap);
			bk.saveWB(abs_file_name,upperdata, lidata) ;
		}catch(Exception e){
			logger.error(e,e);
			throw e;
		}
	}
	
	public void buildResponseExcel(String filename, 	List<String[]> upperdata,List<String[]> lidata, boolean batch, String[] respdata)throws Exception{
		
		try{
			if(logger.isDebugEnabled())
				logger.debug("buildResponseExcel  ::"+filename);
			//build the data
			this.abs_file_name = filename;
			WriteExcelBook bk = new WriteExcelBook(bptemp, studiomap);
			bk.setIsResponse(true);
			bk.saveWB(abs_file_name,upperdata, lidata) ;
			if(batch)
				bk.setBatch(batch, respdata);
		}catch(Exception e){
			logger.error(e,e);
			throw e;
		}
	}
	
	
}
