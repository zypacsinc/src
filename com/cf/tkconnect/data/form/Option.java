package com.cf.tkconnect.data.form;

public class Option {

	String name;
	String value;
	
	public Option(String name, String value){
		this.name = name;
		this.value = value;
	}
	
	public String getName(){
		return name;
	}
	
	public String getValue(){
		return value;
	}
	
}
