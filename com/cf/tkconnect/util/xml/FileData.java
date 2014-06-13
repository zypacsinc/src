package com.cf.tkconnect.util.xml;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;



public class FileData implements java.io.Serializable   {
		/**
		 * field for Datahandler
		 */
		
	 private java.lang.String filename;
	 private byte[] datahandler;
	 private DataHandler localDatahandler;
	 
	 public FileData() {
	    }

	    public FileData(  byte[] datahandler,  java.lang.String filename) {
	           this.datahandler = datahandler;
	           this.filename = filename;
	    }
	 
	 
	 public String getFilename(){
		 return this.filename;
	 }
	 public void setFilename(String filename){
		 this.filename = filename;
	 }
	 public byte[] getByteDatahandler(){
		 return  this.datahandler;
	 }
	 public void setByteDatahandler(byte[] dataHandler){
		 this.datahandler = dataHandler;
	 }
	 
		public void serialize(final QName parentQName,
				javax.xml.stream.XMLStreamWriter xmlWriter,
				boolean serializeType)
				throws javax.xml.stream.XMLStreamException
				 {

			String prefix = null;
			String namespace = null;

			prefix = parentQName.getPrefix();
			namespace = parentQName.getNamespaceURI();
			writeStartElement(prefix, namespace, parentQName.getLocalPart(),
					xmlWriter);

			if (serializeType) {

				String namespacePrefix = registerPrefix(xmlWriter,
						"http://xml.util.webservices.skire.com/xsd");
				if ((namespacePrefix != null)
						&& (namespacePrefix.trim().length() > 0)) {
					writeAttribute("xsi",
							"http://www.w3.org/2001/XMLSchema-instance",
							"type", namespacePrefix + ":FileData", xmlWriter);
				} else {
					writeAttribute("xsi",
							"http://www.w3.org/2001/XMLSchema-instance",
							"type", "FileData", xmlWriter);
				}

			}
			
				namespace = "http://xml.util.webservices.skire.com/xsd";
				writeStartElement(null, namespace, "datahandler", xmlWriter);

				if (localDatahandler != null) {
					try {
						org.apache.axiom.util.stax.XMLStreamWriterUtils
								.writeDataHandler(xmlWriter, localDatahandler,
										null, true);
					} catch (java.io.IOException ex) {
						throw new javax.xml.stream.XMLStreamException(
								"Unable to read data handler for datahandler",
								ex);
					}
				} else {

					writeAttribute("xsi",
							"http://www.w3.org/2001/XMLSchema-instance", "nil",
							"1", xmlWriter);

				}

				xmlWriter.writeEndElement();
			
			
				namespace = "http://xml.util.webservices.skire.com/xsd";
				writeStartElement(null, namespace, "filename", xmlWriter);

				if (this.filename == null) {
					// write the nil attribute

					writeAttribute("xsi",
							"http://www.w3.org/2001/XMLSchema-instance", "nil",
							"1", xmlWriter);

				} else {

					xmlWriter.writeCharacters(this.filename);

				}

				xmlWriter.writeEndElement();
			
			xmlWriter.writeEndElement();

		}

		/**
		 * Utility method to write an element start tag.
		 */
		private void writeStartElement(String prefix,
				String namespace, String localPart,
				javax.xml.stream.XMLStreamWriter xmlWriter)
				throws javax.xml.stream.XMLStreamException {
			String writerPrefix = xmlWriter.getPrefix(namespace);
			if (writerPrefix != null) {
				xmlWriter.writeStartElement(namespace, localPart);
			} else {
				if (namespace.length() == 0) {
					prefix = "";
				} else if (prefix == null) {
					prefix = "ns1";
				}

				xmlWriter.writeStartElement(prefix, localPart, namespace);
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}
		}

		/**
		 * Util method to write an attribute with the ns prefix
		 */
		private void writeAttribute(String prefix,
				String namespace, String attName,
				String attValue,
				javax.xml.stream.XMLStreamWriter xmlWriter)
				throws javax.xml.stream.XMLStreamException {
			if (xmlWriter.getPrefix(namespace) == null) {
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}
			xmlWriter.writeAttribute(namespace, attName, attValue);
		}

		/**
		 * Util method to write an attribute without the ns prefix
		 */
		private void writeAttribute(String namespace,
				String attName, String attValue,
				javax.xml.stream.XMLStreamWriter xmlWriter)
				throws javax.xml.stream.XMLStreamException {
			if (namespace.equals("")) {
				xmlWriter.writeAttribute(attName, attValue);
			} else {
				registerPrefix(xmlWriter, namespace);
				xmlWriter.writeAttribute(namespace, attName, attValue);
			}
		}

		/**
		 * Register a namespace prefix
		 */
		private String registerPrefix(
				javax.xml.stream.XMLStreamWriter xmlWriter,
				String namespace)
				throws javax.xml.stream.XMLStreamException {
			String prefix = xmlWriter.getPrefix(namespace);
			if (prefix == null) {
				prefix = "ns1";
				javax.xml.namespace.NamespaceContext nsContext = xmlWriter
						.getNamespaceContext();
				while (true) {
					String uri = nsContext.getNamespaceURI(prefix);
					if (uri == null || uri.length() == 0) {
						break;
					}
					//prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
				}
				xmlWriter.writeNamespace(prefix, namespace);
				xmlWriter.setPrefix(prefix, namespace);
			}
			return prefix;
		}
}
