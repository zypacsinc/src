/**
 * MainServicesServiceLocator.java
 *
 * Author Cyril Furtado
 * smartlink for Integration 
 *
 */

package com.cf.tkconnect.mainservice;

import com.cf.tkconnect.mainservice.MainServices;
import com.cf.tkconnect.mainservice.MainServicesService;
import com.cf.tkconnect.mainservice.MainserviceSoapBindingStub;



public class MainServicesServiceLocator extends 
       org.apache.axis.client.Service implements MainServicesService {

	
	private static final long serialVersionUID = 1L;
	// Use to get a proxy class for mainservice
    private  String mainservice_address = "http://localhost";

    public java.lang.String getmainserviceAddress() {
        return mainservice_address;
    }

    public void setmainserviceAddress(java.lang.String mainservice_address) {
         this.mainservice_address = mainservice_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String mainserviceWSDDServiceName = "mainservice";
    private java.lang.String documentserviceWSDDServiceName = "UnifierWebServices";

    public java.lang.String getmainserviceWSDDServiceName() {
        return mainserviceWSDDServiceName;
    }

    public java.lang.String getdocumentserviceWSDDServiceName() {
        return documentserviceWSDDServiceName;
    }

    public void setmainserviceWSDDServiceName(java.lang.String name) {
        mainserviceWSDDServiceName = name;
    }

    public MainServices getmainservice() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(mainservice_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getmainservice(endpoint);
    }

   
    

    public MainServices getmainservice(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            MainserviceSoapBindingStub _stub = new MainserviceSoapBindingStub(portAddress, this);
            _stub.setPortName(getmainserviceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (MainServices.class.isAssignableFrom(serviceEndpointInterface)) {
                MainserviceSoapBindingStub _stub = new MainserviceSoapBindingStub(new java.net.URL(mainservice_address), this);
                _stub.setPortName(getmainserviceWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        String inputPortName = portName.getLocalPart();
        if ("mainservice".equals(inputPortName)) {
            return getmainservice();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName(mainservice_address, "MainServicesService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("mainservice"));
        }
        return ports.iterator();
    }

}
