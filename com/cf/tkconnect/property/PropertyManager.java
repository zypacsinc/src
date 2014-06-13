/**
 * PropertyManager.java
 */
      
package com.cf.tkconnect.property;

import java.util.*;
import java.io.*;

import com.cf.tkconnect.log.*;
import com.cf.tkconnect.property.PropertyManager;

/**
 * Manages properties for the entire Jive system. Properties are merely
 * pieces of information that need to be saved in between server restarts.
 * <p>
 * At the moment, properties are stored in a Java Properties file. In a version
 * of Jive coming soon, the properties file format will move to XML. XML
 * properties will allow hierarchical property structures which may mean the
 * API of this class will have to change.
 * <p>
 * Jive properties are only meant to be set and retrevied by core Jive classes.
 * Therefore, skin writers should probably ignore this class.
 * <p>
 * This class is implemented as a singleton since many classloaders seem to
 * take issue with doing classpath resource loading from a static context.
 */
public class PropertyManager {

    private static final Log logger = LogSource.getInstance( PropertyManager.class );

    private static PropertyManager manager = null;
    private static Object managerLock = new Object();
    private static String propsName = "smartlink.properties";

    /**
     * Returns a Skire property
     *
     * @param name the name of the property to return.
     * @returns the property value specified by name.
     */
    public static String getProperty(String name) {
        if (manager == null) {
            synchronized(managerLock) {
                if (manager == null) {
                    manager = new PropertyManager(propsName);
                }
            }
        }
        return manager.getProp(name);
    }

    public static String getProperty(String name, String defval) {
        if (manager == null) {
            synchronized(managerLock) {
                if (manager == null) {
                    manager = new PropertyManager(propsName);
                }
            }
        }
        return manager.getProp(name, defval);
    }

    public static PropertyManager getPropertyManager(String pathName) {
		if (pathName == null) {
			if (manager == null) {
			    synchronized(managerLock) {
			        if (manager == null) {
			            manager = new PropertyManager(propsName);
			        }
			    }
			}
			return manager;
		}
		else
			return new PropertyManager(pathName);
    }

    public static Enumeration getPropertyNames() {
        if (manager == null) {
            synchronized(managerLock) {
                if (manager == null) {
                    manager = new PropertyManager(propsName);
                }
            }
        }
        return manager.getPropNames();
    }

    /**
     * Returns true if the properties are readable. This method is mainly
     * valuable at setup time to ensure that the properties file is setup
     * correctly.
     */
    public static boolean propertyFileIsReadable() {
        if (manager == null) {
            synchronized(managerLock) {
                if (manager == null) {
                    manager = new PropertyManager(propsName);
                }
            }
        }
        return manager.propFileIsReadable();
    }

    /**
     * Returns true if the properties are writable. This method is mainly
     * valuable at setup time to ensure that the properties file is setup
     * correctly.
     */
    public static boolean propertyFileIsWritable() {
        if (manager == null) {
            synchronized(managerLock) {
                if (manager == null) {
                    manager = new PropertyManager(propsName);
                }
            }
        }
        return manager.propFileIsWritable();
    }

    /**
     * Returns true if the smartlink.properties file exists where the path property
     * purports that it does.
     */
    public static boolean propertyFileExists() {
        if (manager == null) {
            synchronized(managerLock) {
                if (manager == null) {
                    manager = new PropertyManager(propsName);
                }
            }
        }
        return manager.propFileExists();
    }

    private Properties properties = null;
    private Object propertiesLock = new Object();
    private String resourceURI;

    /**
     * Singleton access only.
     */
    public PropertyManager(String resourceURI) {
        this.resourceURI = resourceURI;
    }
   
    /**
     * Gets a smartlink property.
     * The properties file should be accesible from the classpath. Additionally,
     * it should have a path field that gives the full path to where the
     * file is located. Getting properties is a fast operation.
     */
    public String getProp(String name) {
        //If properties aren't loaded yet. We also need to make this thread
        //safe, so synchronize...
        if (properties == null) {
            synchronized(propertiesLock) {
                //Need an additional check
                if (properties == null) {
                    loadProps();
                }
            }
        }
        return properties.getProperty(name);
    }

    public String getProp(String name, String defval) {
        //If properties aren't loaded yet. We also need to make this thread
        //safe, so synchronize...
        if (properties == null) {
            synchronized(propertiesLock) {
                //Need an additional check
                if (properties == null) {
                    loadProps();
                }
            }
        }
        return properties.getProperty(name, defval);
    }

   
    public Properties getProperties() {
        if (properties == null) {
            synchronized(propertiesLock) {
                if (properties == null) {
                    loadProps();
                }
            }
        }
        return properties;
    }

    public Enumeration getPropNames() {
        //If properties aren't loaded yet. We also need to make this thread
        //safe, so synchronize...
		if (properties == null) {
            synchronized(propertiesLock) {
                //Need an additional check
                if (properties == null) {
                    loadProps();
                }
            }
        }
        return properties.propertyNames();
    }

    /**
     * Loads Skire properties from the disk.
     */
    private void loadProps() {
        Properties _properties = new Properties();
        InputStream in = null;
        try {
            logger.info("Loading properties: "+resourceURI);
            in = getClass().getResourceAsStream(resourceURI);
            _properties.load(in);
			properties = _properties;
        }
        catch (IOException ioe) {
            logger.error("Error loading properties: "+resourceURI, ioe);
        }
        catch (RuntimeException ex) 
        {
        	logger.error("Error loading properties: "+resourceURI,ex);
        	throw ex;
        }
        finally {
            try {
                in.close();
            } catch (Exception e) { }
        }
    }

    /**
     * Returns true if the properties are readable. This method is mainly
     * valuable at setup time to ensure that the properties file is setup
     * correctly.
     */
    public boolean propFileIsReadable() {
        try {
            getClass().getResourceAsStream(resourceURI);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns true if the skire.properties file exists where the path property
     * purports that it does.
     */
    public boolean propFileExists() {
        String path = getProp("path");
		File file = new File(path);
        if (file.isFile()) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Returns true if the properties are writable. This method is mainly
     * valuable at setup time to ensure that the properties file is setup
     * correctly.
     */
    public boolean propFileIsWritable() {
        String path = getProp("path");
		File file = new File(path);
		if (file.isFile()) {
			//See if we can write to the file
			if (file.canWrite()) {
                return true;
            }
			else {
                return false;
            }
        }
        else {
            return false;
        }
    }

}
