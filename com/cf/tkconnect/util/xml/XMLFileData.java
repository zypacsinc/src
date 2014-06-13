package com.cf.tkconnect.util.xml;

public class XMLFileData extends XMLFileObject {
	
	byte[] data;
	String filename;
	
	public byte[] getFileData(){
		return this.data;
	}
	
	public void setFileData(byte[] data){
		this.data = data;
	}
	
	public void setFilename(String filename){
		this.filename = filename;
	}

	public String getFilename(){
		return this.filename ;
	}
}
