package com.cf.tkconnect;


import com.cf.tkconnect.data.form.BPAttributeData;
import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;


public class AdminController {

	static Log logger = LogSource.getInstance(BPAttributeData.class);

	private static AdminController m_instance = null;
	
	public static AdminController getInstance(){
		logger.debug(" SmartlinkController Class");
		if( null != m_instance){
			return m_instance;
		}
		else if( null == m_instance){
			m_instance = new AdminController();
		}
		
		return m_instance;
	}
	

}
