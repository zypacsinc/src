package com.cf.tkconnect.process;

import com.cf.tkconnect.util.xml.XMLObject;

import javax.activation.DataHandler;

public class ResponseObject extends XMLObject {
	
	private DataHandler dh = null;
	private String filename = null;
	private long audit_service_id = -1;
	private long saved_service_id = -1;
	byte[] data;
	
	public byte[] getFileData(){
		return this.data;
	}
	
	public void setFileData(byte[] data){
		this.data = data;
	}
	
	
	public DataHandler getDataHandler() {
		return dh;
	}

	public void setDataHandler(DataHandler dh) {
		this.dh = dh;
	}
	
	public String getFilename(){
		return this.filename;
	}
	public void setFilename(String filename){
		 this.filename = filename;
	}
	
	public long getAuditServiceId(){
		return this.audit_service_id;
	}
	public void setAuditServiceId(long audit_service_id){
		 this.audit_service_id = audit_service_id;
	}
	
	public long getSavedServiceId(){
		return this.saved_service_id;
	}
	public void setSavedServiceId(long saved_service_id){
		 this.saved_service_id = saved_service_id;
	}
	
	public String getErrors(){
		StringBuilder buf = new StringBuilder("");
		if(errorStatus != null){
			for(String e : errorStatus)
				buf.append(" "+e);
		}
		return buf.toString();
	}
}
