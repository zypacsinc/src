/**
 * XMLFileObject.java
 *
 
 */

package com.cf.tkconnect.util.xml;

import com.cf.tkconnect.util.xml.XMLFileObject;

public class XMLFileObject  implements java.io.Serializable {
    private javax.activation.DataHandler dataHandler;
    private java.lang.String[] errorStatus;
    private int statusCode;
    private java.lang.String xmlcontents;

    public XMLFileObject() {
    }

    public XMLFileObject(
           javax.activation.DataHandler dataHandler,
           java.lang.String[] errorStatus,
           int statusCode,
           java.lang.String xmlcontents) {
           this.dataHandler = dataHandler;
           this.errorStatus = errorStatus;
           this.statusCode = statusCode;
           this.xmlcontents = xmlcontents;
    }


    /**
     * Gets the dataHandler value for this XMLFileObject.
     * 
     * @return dataHandler
     */
    public javax.activation.DataHandler getDataHandler() {
        return dataHandler;
    }


    /**
     * Sets the dataHandler value for this XMLFileObject.
     * 
     * @param dataHandler
     */
    public void setDataHandler(javax.activation.DataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }


    /**
     * Gets the errorStatus value for this XMLFileObject.
     * 
     * @return errorStatus
     */
    public java.lang.String[] getErrorStatus() {
        return errorStatus;
    }


    /**
     * Sets the errorStatus value for this XMLFileObject.
     * 
     * @param errorStatus
     */
    public void setErrorStatus(java.lang.String[] errorStatus) {
        this.errorStatus = errorStatus;
    }


    /**
     * Gets the statusCode value for this XMLFileObject.
     * 
     * @return statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }


    /**
     * Sets the statusCode value for this XMLFileObject.
     * 
     * @param statusCode
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }


    /**
     * Gets the xmlcontents value for this XMLFileObject.
     * 
     * @return xmlcontents
     */
    public java.lang.String getXmlcontents() {
        return xmlcontents;
    }


    /**
     * Sets the xmlcontents value for this XMLFileObject.
     * 
     * @param xmlcontents
     */
    public void setXmlcontents(java.lang.String xmlcontents) {
        this.xmlcontents = xmlcontents;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof XMLFileObject)) return false;
        XMLFileObject other = (XMLFileObject) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.dataHandler==null && other.getDataHandler()==null) || 
             (this.dataHandler!=null &&
              this.dataHandler.equals(other.getDataHandler()))) &&
            ((this.errorStatus==null && other.getErrorStatus()==null) || 
             (this.errorStatus!=null &&
              java.util.Arrays.equals(this.errorStatus, other.getErrorStatus()))) &&
            this.statusCode == other.getStatusCode() &&
            ((this.xmlcontents==null && other.getXmlcontents()==null) || 
             (this.xmlcontents!=null &&
              this.xmlcontents.equals(other.getXmlcontents())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getDataHandler() != null) {
            _hashCode += getDataHandler().hashCode();
        }
        if (getErrorStatus() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getErrorStatus());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getErrorStatus(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        _hashCode += getStatusCode();
        if (getXmlcontents() != null) {
            _hashCode += getXmlcontents().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(XMLFileObject.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://generalInterface.messenger.skire.com", "XMLFileObject"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dataHandler");
        elemField.setXmlName(new javax.xml.namespace.QName("", "dataHandler"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xml.apache.org/xml-soap", "DataHandler"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("errorStatus");
        elemField.setXmlName(new javax.xml.namespace.QName("", "errorStatus"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("statusCode");
        elemField.setXmlName(new javax.xml.namespace.QName("", "statusCode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("xmlcontents");
        elemField.setXmlName(new javax.xml.namespace.QName("", "xmlcontents"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
