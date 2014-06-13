package com.cf.tkconnect.data;

import static org.apache.commons.lang3.math.NumberUtils.toInt;

import java.io.File;

import java.util.List;
import java.util.Map;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.cf.tkconnect.data.form.BPAttributeData;
import com.cf.tkconnect.data.process.ProcessBPXMLTemplate;
import com.cf.tkconnect.data.process.SaveData;

import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;

import com.cf.tkconnect.util.DataUtils;
import com.cf.tkconnect.util.FileUtils;

public class DownloadFile {

	static Log logger = LogSource.getInstance(DownloadFile.class);
	int user_id;
	int company_id;
	HttpServletRequest req;
	HttpServletResponse res;
	int id;
	String fileaction;
	String file_name;
	//String exporttype;
	String filetype;
	String prefix;
	String template;
	
	
	public DownloadFile(HttpServletRequest req, HttpServletResponse res){
		this.req = req;
		this.res = res;
		this.id = toInt(req.getParameter("id"));
		this.fileaction =req.getParameter("fileaction");
		//this.exporttype =req.getParameter("exporttype");
		this.filetype =req.getParameter("filetype");
		this.prefix =req.getParameter("prefix");
		this.template =req.getParameter("template");
		if(logger.isDebugEnabled())
			logger.debug("DownloadFile  ::"+this.fileaction+" id:"+id+" prefix:"+prefix);
	}
	
	public void processDownload() throws Exception{
		if(fileaction == null)
			throw new Exception("No fileaction found.");
//		if(fileaction.equals("service"))
//			processServiceFile();
		if(fileaction.equals("service_audit"))
			downloadServiceAuditFile();
		else if(fileaction.equals("excel_template"))
			processExcelTemplate();
		else if(fileaction.equals("saved_file"))
			downloadSavedFile();
		else if(fileaction.equals("excel_data"))
			processExcelData();
		
		
	}
	/*
	
	private void processServiceFile(){
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select * from services where service_id = ?"; 
		try{
			conn = SqlUtils.getConnection();
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			if(rs.next()){
				file_name = rs.getString("file_name");
				String name =  rs.getString("service_name")+"_"+rs.getString("method_name");
				downloadFile(name);
			}
		logger.debug("service file name : "+file_name);	
		}catch(Exception e){
			logger.error(e,e);
		} finally {
			SqlUtils.closeResultSet(rs);
			SqlUtils.closeStatement(ps);
			SqlUtils.closeConnection(conn);
		}
	}
	*/
	private void downloadServiceAuditFile(){
		
		String file = "output_file_name";
		String filetype = this.fileaction =req.getParameter("fileaction2");
		Map<String,Object> map = DataUtils.getServiceAuditFile( id, filetype);
		logger.debug("service file filetype : "+filetype);	
		if("zipfile".equalsIgnoreCase(filetype))
			file = "output_zip_file";
		try{
			
				file_name = (String)map.get("file_path")+FileUtils.getFileName((String)map.get(file));
				String name = (String) map.get("external_file_name");
				int ind = name.indexOf("unifier_");
				if(ind > 0)
					name = name.substring(ind);
				downloadFile(name);
			
		logger.debug("service file name : "+file_name+ "  ::"+name );	
		}catch(Exception e){
			logger.error(e,e);
		} 
	}
	
	private void downloadSavedFile() throws Exception{
		Map<String,String> map =DataUtils.getSaveRecord(id);
		String file_path = map.get("file_path");
		String file_name = map.get("file_name");
		String save_name = map.get("save_name");
		this.file_name = file_path+File.separator+file_name;
		downloadFile(file_name);
	}
	
	private void processExcelTemplate(){
		try{
			if(logger.isDebugEnabled())
				logger.debug("processExcelTemplate  ::"+this.prefix);
			//build the data
			BPAttributeData bpattr = new BPAttributeData(this.prefix,"");
			ProcessBPXMLTemplate bptemp =  bpattr.getBPDesignDetails();
			TKUnifierMetaData ud = new TKUnifierMetaData(); 
			Map<String,Object> studiomap = ud.getStudioInfo(this.prefix);
			WriteExcelBook bk = new WriteExcelBook(bptemp, studiomap);
			res.setHeader("Content-Disposition","attachment; filename=unifier_"+this.prefix+"_bp.xlsx");
			bk.createWB(res);
		}catch(Exception e){
			logger.error(e,e);
		}
	}
	private void processExcelData(){
		try{
			if(logger.isDebugEnabled())
				logger.debug("processExcelData  ::"+this.prefix);
			//build the data
			SaveData sd = new SaveData(req,res);
			sd.buildCSVData();
			List<String[]> upperdata = sd.getUpperData();
			List<String[]> lidata = sd.getLiData() ;
			BPAttributeData bpattr = new BPAttributeData(this.prefix,"");
			ProcessBPXMLTemplate bptemp =  bpattr.getBPDesignDetails();
			TKUnifierMetaData ud = new TKUnifierMetaData(); 
			Map<String,Object> studiomap = ud.getStudioInfo(this.prefix);
			WriteExcelBook bk = new WriteExcelBook(bptemp, studiomap);
			res.setHeader("Content-Disposition","attachment; filename=unifier_"+this.prefix+"_bp_data.xlsx");
			bk.writeWB(res, upperdata, lidata);
			bk.createWB(res);
		}catch(Exception e){
			logger.error(e,e);
		}
	}
	private void downloadFile(String name) throws Exception{
		res.setHeader("Content-Disposition","attachment; filename="+name);
		if(logger.isDebugEnabled())
			logger.debug("downloadFile  file_name  ::"+this.file_name);
		//build the data
		res.getOutputStream().write(IOUtils.toByteArray(new java.io.FileInputStream(new File(file_name))));
		
		//IOUtils.copy(), res.getWriter());
		/* List<String> list = IOUtils.readLines(new java.io.FileInputStream(new File(file_name))) ;
		 res.setContentType("application/text");
		 res.setHeader("Content-Disposition","attachment; filename=service_"+name);
		 for(String str : list)
			 res.getWriter().write(str);*/
		res.getOutputStream().flush();
		 res.flushBuffer();
		
	}

	
}
