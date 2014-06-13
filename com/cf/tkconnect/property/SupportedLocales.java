
                                        
package com.cf.tkconnect.property;

import java.util.*;
import java.io.*;

import com.cf.tkconnect.property.PropertiesUtil;
import com.cf.tkconnect.property.SupportedLocales;


public class SupportedLocales{

     //private static final String LOCALES_FILE = "locales.properties";
     private static final String DELIMITER = ",";
     private static final String COMMENT = "#";
    
     private static SupportedLocales instance = null;
     private List localesList = null;    // list of locale objects.
    
     private SupportedLocales(){
         // private no -op constructor
     }
    
     /** Singleton instance accesss method.
       */
     public static SupportedLocales getInstance(){
         if(instance == null){
             instance = new SupportedLocales();
         }
         return instance;
     }
    
    
     /** Reads from the locales.properties file.Returns the list of Objects 
       * containing supported locales.
       */
    
     public List getLocalesList(){
    
         if(localesList == null){
             localesList = new ArrayList();
             BufferedReader br = null;
    
             try{
                 br = new BufferedReader(new FileReader(PropertiesUtil.getLocalePropertiesFilePath()));
             }
             catch (FileNotFoundException fex){
                 fex.printStackTrace();
                 System.exit(1);
             }
             String s = null;
             String language = null;
             String country = null;
             String variant = null;
    
             try{
    
                 while((s = br.readLine())!= null){
    
                   if(s.startsWith(COMMENT)){
                       continue;
                   }
                   StringTokenizer tokenizer = new StringTokenizer(s,DELIMITER);
                   while(tokenizer.hasMoreTokens()){
                       language = tokenizer.nextToken();
                       country  = tokenizer.nextToken();
                       // variant = tokenizer.nextToken();
                   }
    
                   Locale locale = null;
                   /*
                   if(variant == null){
                       locale = new Locale(language,country);
                   }
                   else{
                       locale = new Locale(language,country,variant);
                   }
                   */
                   locale = new Locale(language,country);
    
                   localesList.add(locale);
    
                 }
             }
    
             catch(Exception ex){
                 ex.printStackTrace();
                 System.exit(1);
             }
         }
         return localesList;
    
     }



}

