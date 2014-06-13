package com.cf.tkconnect.process;

import static com.cf.tkconnect.util.FileUtils.SuccessFileServiceBaseDirectory;
import static com.cf.tkconnect.util.FileUtils.TempFileServiceBaseDirectory;
import static com.cf.tkconnect.util.FileUtils.createDateDirs;
import static com.cf.tkconnect.util.FileUtils.moveFiles;
import static com.cf.tkconnect.util.WSConstants.SEND_EMAIL_ONERROR;
import static com.cf.tkconnect.util.WSUtil.getDateString;
import static java.io.File.separator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import com.cf.tkconnect.data.GenerateXML;
import com.cf.tkconnect.data.ReadExcel;
import com.cf.tkconnect.data.process.ProcessBPXMLTemplate;
import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.util.FileUtils;
import com.cf.tkconnect.util.PropertyManager;
import com.cf.tkconnect.util.WSUtil;

public class ProcessXLSXFile {
	static Log logger = LogSource.getInstance(ProcessXLSXFile.class);
	
	private String documentPath = null;
	private String today = "";

	public void processFile(String documentPath) throws Exception {
		
		boolean error = false;
		this.documentPath = documentPath;
		List<String> errList = new ArrayList<String>();
		String fileName = null;
		String xslxtempfile;
		try {
			today = getDateString();
			createDateDirs(today);
		
			/*
			 * after the file is processed, move the file to output directory to
			 * prevent duplicate processing.
			 */
			fileName = documentPath.substring(documentPath
					.lastIndexOf(File.separator) + 1);
			if(logger.isDebugEnabled())
				logger.debug("File processFile xlsx path ************ "+fileName);
			// move the file to temp directory
			 xslxtempfile =FileUtils.copyFiles(FileUtils.InputFileServiceBaseDirectory,
					FileUtils.TempFileServiceBaseDirectory, fileName, fileName,
					true);
			 File file = new File(xslxtempfile);
			 String isbatch = checkBatch(xslxtempfile);
			 ReadExcel re = new ReadExcel( FileUtils.getFileName( this.documentPath), file,0);
			 re.process();
			 ProcessBPXMLTemplate bptemp =re.getBPTemp();
			 List<String[]> upperdata = re.getUpperData();
			 List<String[]> lidata = re.getLiData();
			 Map<String,Object> studiomap = re.getStudioMap();
			 error = re.hasError();
			// check the basic criteria for the input read from the file.
			if(logger.isDebugEnabled())
				logger.debug("File processFile xlsx error ************ "+re.getError());
			 if (error) {
					// do not proceed if there is an error. // move the request file to error directory
					logger.error("*****Invalid input file: "+ fileName);
					FileUtils.moveFiles(FileUtils.TempFileServiceBaseDirectory,
							FileUtils.ErrorFileServiceBaseDirectory
									+ File.separator + today, fileName);
					return;
			} 
			GenerateXML dx = new GenerateXML(isbatch);
			dx.processFileData(bptemp, studiomap, upperdata, lidata,xslxtempfile);
			String responseFileName = dx.getResponseFile();
			if(logger.isDebugEnabled())
				logger.debug("File processFile xlsx responseFileName ************ "+responseFileName);
			FileUtils.moveFiles(TempFileServiceBaseDirectory,
						SuccessFileServiceBaseDirectory+ separator + today,
								FileUtils.getFileName(responseFileName));
			FileUtils.moveFiles(TempFileServiceBaseDirectory,
					SuccessFileServiceBaseDirectory+ separator + today,FileUtils.getFileName(fileName));
		}catch(Exception e)	{	
			logger.error(e,e);
			FileUtils.moveFiles(FileUtils.TempFileServiceBaseDirectory,
					FileUtils.ErrorFileServiceBaseDirectory
							+ File.separator + today, fileName);
		}finally{
	        String inputerror = PropertyManager.getProperty(SEND_EMAIL_ONERROR,"no"); 
			if(error && "yes".equals(inputerror)){
				StringBuilder buf = new StringBuilder();
				for (int i = 0; i < errList.size(); i++) 
					buf.append(errList.get(i));
				
			//	MailUtils.sendMailMessage("tkconnect could not proceed for file name:"+fileName, "Input XML file "+fileName+" could not be successfully parsed reasons:"+buf.toString(),"smartlink Status",fileName,500);
			}
		}
	}
	
	private String checkBatch(String filename){
		if(filename == null)
			return "no";
		String str = FileUtils.getFileName(filename);
		if(str == null || str.trim().length() == 0)
			return "no";
		if(str.indexOf("_bp_batch") > 0)
			return "yes";
		return "no";
	}

}
