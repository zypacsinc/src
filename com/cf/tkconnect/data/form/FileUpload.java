package com.cf.tkconnect.data.form;

import java.io.File;
import java.io.FileOutputStream;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;


import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.cf.tkconnect.data.ReadExcel;
import com.cf.tkconnect.data.process.UnifierXMLServiceData;
import com.cf.tkconnect.database.SqlUtils;
import com.cf.tkconnect.process.RunWebServices;
import com.cf.tkconnect.util.DataUtils;
import com.cf.tkconnect.util.FileUtils;
import com.cf.tkconnect.wsdl.WSDLServices;

import static org.apache.commons.lang3.math.NumberUtils.toInt;
import static org.apache.commons.lang3.math.NumberUtils.toLong;

public class FileUpload {

	private static final Logger logger = Logger.getLogger(FileUpload.class);
	HttpServletResponse res;
	HttpServletRequest req;
	int user_id;
	private String fileactiontype;
	Map<String,String> pmap = new HashMap<String,String>();
	
	public FileUpload(HttpServletRequest req, HttpServletResponse res){
		this.res = res;
		this.req = req;
	}
	
	public String processUpload() {
		  String str = "";
		  try {
				Collection<Part> parts = req.getParts();
				Iterator<Part> it = parts.iterator();
				while(it.hasNext()){
						Part p = it.next();
						String value = Streams.asString(p.getInputStream());
						String paramname = p.getName();
						logger.debug("params ****** "+paramname+":::"+p.getContentType());
						if(p.getContentType() == null ){// parameter part
							if(paramname != null && paramname.equalsIgnoreCase("fileactiontype"))
								fileactiontype = value;
							if(paramname.startsWith("z_")) 
								paramname = paramname.substring(2);
							pmap.put(paramname, value);
							logger.debug("params ******name "+paramname+"  value:::"+value);
						}else{// file part
							if(logger.isDebugEnabled())
								  logger.debug(" in process ********* ");
							str = processFilePart(paramname,p);
							break;
						}
				}
				if(logger.isDebugEnabled())
					logger.debug(" pmap ********* "+pmap);
		} catch (Exception e) {
			logger.error(e,e);
			 str = "{\"errors\":\"Invalid Excel document, "+StringEscapeUtils.escapeXml(e.getMessage())+"\"}"; // nothing to process
		}
		  if(logger.isDebugEnabled())
			  logger.debug(" str  value of json********* "+str);
		 return str;
	}
	
	private String processFilePart(String paramname, Part p) throws Exception {
		String str = "";
		try{
			String[] file  = writeFile(p);
			 logger.debug("recd from tun paramname  ********* "+paramname+" :::"+fileactiontype);
			if(paramname.equalsIgnoreCase("f_fileclick")){
				if(fileactiontype.equalsIgnoreCase("runwebservice")){
					str = processFileAction(file);
				}
				else if(fileactiontype.equalsIgnoreCase("savefileservice")){
					String file_path =file[1]+File.separator+ file[0];
					pmap.put("input_zip_file",file_path);
					RunWebServices rws = new RunWebServices( pmap);
					str = rws.saveFileData(false);
					 logger.debug("recd from tun str  value of json********* "+str);
				}else if(fileactiontype.equalsIgnoreCase("excel_data")){
					//now process this excel data, read all data
					
					str = processExcelData( file);
				}
				
			}
			}catch (Exception e)	{
				logger.error(e,e);
				 str = "{\"errors\":\"document, "+StringEscapeUtils.escapeXml(e.getMessage())+"\"}"; // nothing to process
			}
				
		return "<script>parent.getUploadResponse( '"+str+"')</script>";
		//return str;
	}
	
	private String processExcelData(String[] fileinfo) throws Exception{
		String name = pmap.get("f_filename"); 
		String prefix = "**error**";
		if(!pmap.containsKey("prefix")) 
			prefix =FileUtils.getBPPrefixFromFile(name);			// need to set prefix
		else
			prefix = pmap.get("prefix");
		
		String file_path =fileinfo[1]+File.separator+ FileUtils.getFileName( fileinfo[0]);
		File file = new File(file_path);
		Map<String,String> map = DataUtils.processSaveData(fileinfo,FileUtils.getFileName(name),name,prefix,1);// from excel
		
		long fileid = toLong(map.get("fileid")) ;
		//  save this record
		ReadExcel re = new ReadExcel( FileUtils.getFileName( name), file,fileid);
		re.process();
		return re.getJsonData();
	}
	
	private String[] writeFile(Part p) throws Exception{

			
			String name = pmap.get("f_filename");
			String[] file = FileUtils.getFilePath(1, name);
			String file_path =file[1]+File.separator+ FileUtils.getFileName( file[0]);
//			p.write(file_path);
			byte[] bytes = org.apache.commons.io.IOUtils.toByteArray(p.getInputStream());
			logger.debug("ffffffffffffff****** "+file_path+" ::"+fileactiontype+" "+file[0]+" :: "+file[1]+" len :"+bytes.length);
			FileOutputStream output = new FileOutputStream(new File(file_path));
			org.apache.commons.io.IOUtils.write(bytes, output);
			
			return file;
	}

	private String processFileAction(String[] file)  throws Exception{
		String file_path =file[1]+File.separator+ file[0];
		if("runwebservice".equalsIgnoreCase(fileactiontype))
			return processFileWebService(file_path);
			
		else if("importwsdlfile".equalsIgnoreCase(fileactiontype))
			return  processWSDLFile( file);
		else if("fileupload".equalsIgnoreCase(fileactiontype))
			return  processFileUpload( file);
		 return "{\"errors\":\"Invalid Action type\"}";
	}
	
	private String processFileWebService(String file_path) 	 throws Exception{
	// now process
		logger.debug("processFileWebService ffffffffffffff---- "+file_path+" ::"+fileactiontype);
		pmap.put("file_path",file_path);
		RunWebServices rws = new RunWebServices( pmap);
		return rws.runService(false);
		
	}
	
	private String processFileUpload(String[] file) 	 throws Exception{	
			// create a record after parsing this file
		try{
			String file_path =file[1]+File.separator+ file[0];
			UnifierXMLServiceData sxml = new UnifierXMLServiceData(file_path,true);
			String name = pmap.get("f_filename");
			String prefix = pmap.get("prefix");
			String jsonstr = sxml.parse();
			 Map<String,String> map =sxml.getMap();
			 logger.debug("****** ----------json- "+jsonstr);
			 logger.debug("****** ---------- ^^^^^ map- "+map);
			if( map.containsKey("errors")){
				// errors found
				return jsonstr;
			}
			// check for details if shortname found
			map.put("company_id", "1");
			map.put("file_name",file[0]);
			map.put("file_path",file[1]);
			map.put("name", name);
			map.put("prefix", prefix);
			map.put("external_name", name);
			map.put("uploadtype", "3");
			long id = DataUtils.createSaveRecord(map);
			
			// save file
			return jsonstr;
		}catch (Exception e)	{
			logger.error(e,e);
			return "{\"errors\":\"Invalid XML document, "+StringEscapeUtils.escapeXml(e.getMessage())+"\"}"; // nothing to process

		}	
	}
	
	
	private String processWSDLFile(String[] file)  throws Exception{
		String filename =file[1]+File.separator+ file[0];
		try{
			return WSDLServices.parseWSDL(filename);
		}catch (Exception e){
			logger.error(e,e);
			return "{\"errors\":\"Invalid WSDL  "+StringEscapeUtils.escapeXml(e.getMessage())+"\"}"; // nothing to process
		}
	}
	
	
	

}
