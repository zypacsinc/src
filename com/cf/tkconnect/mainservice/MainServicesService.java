/**
 * MainServicesService.java
 *
* Author Cyril Furtado
 * smartlink for Integration 
 *
 * */

package com.cf.tkconnect.mainservice;

import com.cf.tkconnect.mainservice.MainServices;


public interface MainServicesService extends javax.xml.rpc.Service {
    public java.lang.String getmainserviceAddress();

    public MainServices getmainservice() throws javax.xml.rpc.ServiceException;

    public MainServices getmainservice(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
