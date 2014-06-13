
                                        
package com.cf.tkconnect.property;

 import java.util.*;

import com.cf.tkconnect.property.ResourceBundleManager;
import com.cf.tkconnect.property.SupportedBundles;
import com.cf.tkconnect.property.SupportedLocales;

 public class ResourceBundleManager{

     //singleton instance
     private static ResourceBundleManager instance = null;
     // This map will store key-value pairs where keys will be 
     // the hash of bundleName+Locale string and the values will
     // be the correponding loaded ResourceBundle resident in memory.
     //protected static Map resourceMap = new HashMap();
     //private static Locale defaultLocale = new Locale("en","US");
     //private static boolean isLoaded = false;
     
     protected static Map resourceMap = null;
     private static Locale defaultLocale = null;
     private static boolean isLoaded = false;

     public static ResourceBundleManager getInstance(){
         if(instance == null){
             instance = new ResourceBundleManager();
         }
         return instance;
     }

     // protected constructor. Sub classes can override.
     protected ResourceBundleManager(){
         resourceMap = new HashMap();
         defaultLocale = new Locale("en","US");
         isLoaded = false;
     }
     /**Gets the localized version of pre-loaded Resource bundle for the given
       *bundle name.
       */ 
     
     public ResourceBundle getLocalizedBundle(String bundleName,Locale locale){
         if(!isLoaded){
            loadResourceBundles();
            isLoaded = true;
         }
         String bundleString = bundleName + locale.toString();
         Integer hashInt = getHashKey(bundleString);
         ResourceBundle bundle = (ResourceBundle)resourceMap.get(hashInt);
         if(bundle == null){
             StringBuffer sb = new StringBuffer(100);
             sb.append("The bundle ");
             sb.append(bundleName);
             sb.append(" is not supported.Please check the bundles.properties files.");
             throw new RuntimeException(sb.toString());
         }
         hashInt = null; // nullify for GC
         return bundle;
     }


     /**Gets the default pre-loaded Resource bundle for the given
       *bundle name.
       */
     public ResourceBundle getDefaultBundle(String bundleName){
         return getLocalizedBundle(bundleName,defaultLocale);
     }

     /** Loads all the bundles in memory.The bundles are kept in a hash map in which
       * keys are the hashcode of bundleName+locale string and the value is the corresponding
       * bundle itself.The retrieval can than be made of appropriate bundle by providing the 
       * right key.
       */

     public void loadResourceBundles(){

         // Remove time stats later.
         if(!isLoaded){

             long beginTime = System.currentTimeMillis();

             List bundlesList = SupportedBundles.getInstance().getBundlesList();
             List localesList = SupportedLocales.getInstance().getLocalesList();


             Iterator bit = bundlesList.iterator();
             
             while (bit.hasNext()) {

                 Iterator lit = localesList.iterator();
                 String name = (String)bit.next();
                 while(lit.hasNext()){
                     Locale locale = (Locale)lit.next();
                     ResourceBundle bundle = ResourceBundle.getBundle(name,locale);
                     String keyString = name + locale.toString();
                     Integer hashKey = getHashKey(keyString);
                     resourceMap.put(hashKey,bundle);

                 }
             }
             isLoaded = true;
             long endTime = System.currentTimeMillis();
             long totalTime = (endTime - beginTime)/1000;
         }

     }

     /** Gets the resource map.Only protected access in case sub class need direct access.
       * Not used in current context.
       */

     protected Map getResourceMap(){
         return resourceMap;
     }

     /* Gets the hashcode of the String argument.The string argument here needs to be the append
      * of Locale object string to the bundle name.Converts into Integer object before
      * returning as it will be used for insertion/retrieval from bundle map.
      * Internal use only.
      */
     private Integer getHashKey(String str){
         return new Integer(str.hashCode());
     }

 }


 
