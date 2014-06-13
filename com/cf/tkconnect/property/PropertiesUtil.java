
package com.cf.tkconnect.property;

import java.io.*;
import java.util.*;

import com.cf.tkconnect.util.PropertyManager;


/** Utility class to get the configured values from projectSource.properties
  * file.
  */

public final class PropertiesUtil{


    // directory where all bundle(*.properties) files will be read from.
    public static final String BUNDLES_READ_DIR     = "bundles.read.dir";
    // directory where all bundle(*.properties) files will be written to.
    // This directory will hold all the generated properties files with
    // keys that were originally missing from the properties file that were read in.
    public static final String BUNDLES_WRITE_DIR    = "bundles.write.dir";

    // files with supported bundles and locales names 
    public static final String SUPPORTED_BUNDLES_FILE = "bundles.properties";
    public static final String SUPPORTED_LOCALES_FILE = "locales.properties";

    // flag that denotes whether to write/generate any properties files.
    public static final String GENERATE_PROPERTIES  = "bundles.generate.properties";



    public static String getBundlePropertiesFilePath(){

        //String bPropFile = "d:/UnifiedPS/i18nproperties/bundles.properties"; 
        
        String bPropDir = PropertyManager.getProperty(BUNDLES_READ_DIR);
        String bPropFile = bPropDir + "/" + SUPPORTED_BUNDLES_FILE;
        
        return bPropFile;

    }


    public static String getLocalePropertiesFilePath(){

        //String lPropFile =  "d:/UnifiedPS/i18nproperties/locales.properties"; 
        
        String lPropDir = PropertyManager.getProperty(BUNDLES_READ_DIR);
        String lPropFile =  lPropDir + "/" + SUPPORTED_LOCALES_FILE;
        
        return lPropFile;

    }

    public static String getPropertiesReadDir(){
        
        //String readDir = "d:/UnifiedPS/i18nproperties";
        //return readDir;
        
        return PropertyManager.getProperty(BUNDLES_READ_DIR);
    }


    public static String getPropertiesWriteDir(){
        
        //String writeDir = "d:/UnifiedPS/i18nproperties/write";
        //return writeDir;
        
        return PropertyManager.getProperty(BUNDLES_WRITE_DIR);
    }

    public static boolean getGeneratePropertyFlag(){

        //return true;
        
        String generateProp = PropertyManager.getProperty(GENERATE_PROPERTIES);
        return Boolean.valueOf(generateProp).booleanValue();
        
    }


    public static void createOrAppendToPropertyFile(String key,String value,String bundleName, Locale locale){
        String language = locale.getLanguage();
        String country = locale.getCountry();

        String writeDir = getPropertiesWriteDir();
        String destinationFile = writeDir + File.separator + bundleName + 
                                 "_" + language + "_" + country + ".properties";
        // Filewriter creates a new file if it does not exist.
        try{

            BufferedWriter out = new BufferedWriter(new FileWriter(destinationFile,true));
            out.write("\n");
            out.write(key + "=" + value);
            out.flush();
            out.close();
        }
        catch(IOException ioe){
            ioe.printStackTrace();
            return;
        }


    }
}
