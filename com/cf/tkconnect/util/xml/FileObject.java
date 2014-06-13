package com.cf.tkconnect.util.xml;

import com.cf.tkconnect.util.xml.FileObject;

/**
 * FileObject.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Jun 07, 2006 (10:40:03 PDT) WSDL2Java emitter.
 */


public class FileObject  implements java.io.Serializable {
    private javax.activation.DataHandler datahandler;

    private java.lang.String filename;

    public FileObject() {
    }

    public FileObject(
           javax.activation.DataHandler datahandler,
           java.lang.String filename) {
           this.datahandler = datahandler;
           this.filename = filename;
    }


    /**
     * Gets the datahandler value for this FileObject.
     * 
     * @return datahandler
     */
    public javax.activation.DataHandler getDatahandler() {
        return datahandler;
    }


    /**
     * Sets the datahandler value for this FileObject.
     * 
     * @param datahandler
     */
    public void setDatahandler(javax.activation.DataHandler datahandler) {
        this.datahandler = datahandler;
    }


    /**
     * Gets the filename value for this FileObject.
     * 
     * @return filename
     */
    public java.lang.String getFilename() {
        return filename;
    }


    /**
     * Sets the filename value for this FileObject.
     * 
     * @param filename
     */
    public void setFilename(java.lang.String filename) {
        this.filename = filename;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof FileObject)) return false;
        FileObject other = (FileObject) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.datahandler==null && other.getDatahandler()==null) || 
             (this.datahandler!=null &&
              this.datahandler.equals(other.getDatahandler()))) &&
            ((this.filename==null && other.getFilename()==null) || 
             (this.filename!=null &&
              this.filename.equals(other.getFilename())));
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
        if (getDatahandler() != null) {
            _hashCode += getDatahandler().hashCode();
        }
        if (getFilename() != null) {
            _hashCode += getFilename().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(FileObject.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://xml.util.webservices.skire.com", "FileObject"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("datahandler");
        elemField.setXmlName(new javax.xml.namespace.QName("", "datahandler"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xml.apache.org/xml-soap", "DataHandler"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("filename");
        elemField.setXmlName(new javax.xml.namespace.QName("", "filename"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
