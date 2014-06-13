/**
 * @(#)SkireResourceBundle.java	08/08/01
 *
 * Copyright 2000-2006 by Skire, Inc.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Skire, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Skire, Inc.
 *
 *
 * @author  Kamlesh Sharma
 */

                                        
package com.cf.tkconnect.property;

 import java.util.*;
import java.io.*;

import com.cf.tkconnect.property.PropertiesUtil;
import com.cf.tkconnect.property.SupportedBundles;
 

 public class SupportedBundles{

     private static final String COMMENT = "#";

     private static SupportedBundles instance = null;
     private List bundlesList = null;   // list of bundle names as strings

     private SupportedBundles(){
         // private no -op constructor       
     }


     /** Singleton instance accesss method.
       */
     public static SupportedBundles getInstance(){
         if(instance == null){
             instance = new SupportedBundles();
         }
         return instance;
     }

     /** Reads from the bundles.properties file.Resturns the list of Strings 
       * containing supported bundle names.
       */

     public List getBundlesList(){

         if(bundlesList == null){
             bundlesList = new ArrayList();
             BufferedReader br = null;
             String bundlesPropFile = null;

             try{
                 // kam -change properties file loading later
                  
                  br = new BufferedReader(new FileReader(PropertiesUtil.getBundlePropertiesFilePath()));
             }
             catch(Exception fex){
                 fex.printStackTrace();
                 System.exit(1);
             }
             String bundleName = null;

             try{

                 while((bundleName = br.readLine())!= null){

                   if(bundleName.startsWith(COMMENT)){
                       continue;
                   }

                   bundlesList.add(bundleName);

                 }
             }
             catch(Exception ex){
                 ex.printStackTrace();
                 System.exit(1);
             }
         }
         return bundlesList;

     }


   
 }
 
