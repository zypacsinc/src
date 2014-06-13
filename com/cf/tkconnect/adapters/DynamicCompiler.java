package com.cf.tkconnect.adapters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Locale;
 
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.cf.tkconnect.log.Log;
import com.cf.tkconnect.log.LogSource;
import com.cf.tkconnect.util.InitialSetUp;

/**
 * Dynamic java class compiler and executer  <br>
 * Demonstrate how to compile dynamic java source code, <br>
 * instantiate instance of the class, and finally call method of the class <br>
 
 *
 */
public class DynamicCompiler
{
	static Log logger = LogSource.getInstance(DynamicCompiler.class);
    /** where shall the compiled class be saved to (should exist already) */
    private static String classOutputFolder = "/com/cf/smartlink/services";
 
    
    
    
    public  class SLDiagnosticListener implements DiagnosticListener<JavaFileObject>
    {
        public void report(Diagnostic<? extends JavaFileObject> diagnostic)
        {
 
            logger.debug("Line Number->" + diagnostic.getLineNumber());
            logger.debug("code->" + diagnostic.getCode());
            logger.debug("Message->"
                               + diagnostic.getMessage(Locale.ENGLISH));
            logger.debug("Source->" + diagnostic.getSource());
            logger.debug(" ");
        }
    }
 
    /** java File Object represents an in-memory java source file <br>
     * so there is no need to put the source file on hard disk  **/
    public  class InMemoryJavaFileObject extends SimpleJavaFileObject
    {
        private String contents = null;
 
        public InMemoryJavaFileObject(String className, String contents) throws Exception       {
            super(URI.create("string:///" + className.replace('.', '/')
                             + Kind.SOURCE.extension), Kind.SOURCE);
            this.contents = contents;
        }
 
        public CharSequence getCharContent(boolean ignoreEncodingErrors)
                throws IOException       {
            return contents;
        }
    }
 
    /** Get a simple Java File Object ,<br>
     * It is just for demo, content of the source code is dynamic in real use case */
    private  JavaFileObject getJavaFileObject(String class_name,String java_src)
    {
        JavaFileObject so = null;
        try      {
            so = new InMemoryJavaFileObject(class_name,java_src);
        }
        catch (Exception e)    {
           logger.error(e,e);
        }
        return so;
    }
 
    /** compile your files by JavaCompiler */
    public  boolean compile(Iterable<? extends JavaFileObject> files)
    {
        //get system compiler:
    	try{
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        
        // for compilation diagnostic message processing on compilation WARNING/ERROR
        SLDiagnosticListener c = new SLDiagnosticListener();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(c, Locale.ENGLISH, null);
        if(fileManager == null)
        	throw new RuntimeException("fileManager is null");
        //specify classes output folder
        String o = InitialSetUp.appHome+"/WEB-INF";
        String out = o+"/classes";
        StringBuilder cp = new StringBuilder(o+"/classes;");
        cp.append(o+"/lib/"+"axiom-api-1.2.13.jar;").append(o+"/lib/"+"axiom-dom-1.2.13.jar;");
        cp.append(o+"/lib/"+"axiom-impl-1.2.13.jar;").append(o+"/lib/"+"axis2-adb-1.6.2.jar;");
        cp.append(o+"/lib/"+"axis2-kernel-1.6.2.jar;").append(o+"/lib/"+"axis2-metadata-1.6.2.jar;");
        cp.append(o+"/lib/"+"axis2-tools-1.0.jar;").append(o+"/lib/"+"axis2-transport-http-1.6.2.jar;");
        cp.append(o+"/lib/"+"wsdl4j-1.6.2.jar;").append(o+"/lib/"+"smartlink.jar;");
        
        File f = new File(InitialSetUp.appHome+"/error.txt");
        BufferedWriter w = new BufferedWriter(new FileWriter(f));
        Iterable options = Arrays.asList("-d", out,"-cp",cp.toString());
        JavaCompiler.CompilationTask task = compiler.getTask(w, fileManager,
                                                             c, options, null,  files);
        boolean result = task.call();
        logger.debug("compilation result "+result);
        return result;
    	}catch(Exception e){
    		logger.error(e,e);
    	}
    	return false;
    }
 
    /** run class from the compiled byte code file by URLClassloader */
    public  void runIt(String class_name, String method_name)   {
        // Create a File object on the root of the directory
        // containing the class file
        File file = new File(classOutputFolder);
 
        try
        {
            // Convert File to a URL
            URL url = file.toURL(); // file:/classes/demo
            URL[] urls = new URL[] { url };
 
            // Create a new class loader with the directory
            ClassLoader loader = new URLClassLoader(urls);
            
            // Load in the class; Class.childclass should be located in
            // the directory file:/class/demo/
          //  Class<?> thisClass = loader.loadClass(class_name);
            Class<?> thisClass = Class.forName(class_name);
            Class<?>[] params = {};
            Object[] paramsObj = {};
            Object instance = thisClass.newInstance();
           // Method[] ms = thisClass.getDeclaredMethods();
            Method thisMethod = thisClass.getDeclaredMethod(method_name, params);
 
            // run the testAdd() method on the instance:
            thisMethod.invoke(instance, paramsObj);
        }
        catch (MalformedURLException e)     {
        }
        catch (ClassNotFoundException e)  {
        }
        catch (Exception ex)        {
           logger.error(ex,ex);
        }
    }
 
    public  boolean process(String class_name,String java_src) throws Exception
    {
        //1.Construct an in-memory java source file from your dynamic code
        JavaFileObject file = getJavaFileObject(class_name,java_src);
        Iterable<? extends JavaFileObject> files = Arrays.asList(file);
        logger.debug("process-> ----------------"+files );
        //2.Compile your files by JavaCompiler
        return compile(files);
 
        //3.Load your class by URLClassLoader, then instantiate the instance, and call method by reflection
        //runIt(class_name,"method_name");
      }
}
