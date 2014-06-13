package com.cf.tkconnect.database;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Properties;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;


import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

public final class SqlUtils {
	static 	DataSource ds = null;
	static String databaseUrl = "jdbc:derby:connectDB;create=true";

    private static String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private static String protocol = "jdbc:derby:";
	
    private static final Logger logger = Logger.getLogger(SqlUtils.class);
	
	
	static void createDataSource() throws Exception  {
		      ds  = new DataSource();
		      ds.setPoolProperties(getPoolProperties());
		      // test the database is created
	}
	
	
	public static DataSource getDataSource()  throws Exception{
		if( ds == null )
			createDataSource();
		return ds;
	}

	public static Connection getConnection()  throws Exception{
		if( ds == null )
			createDataSource();
		return ds.getConnection();
		
	}
	
	public static Connection checkDB()  throws Exception{
		
	        Connection conn = null;
	       
	        String dbName = "connectDB"; // the name of the database
	        try{
	        	loadDriver();
	        	//DriverManager.registerDriver(DriverManager.getDriver(driver));
	        	 conn = DriverManager.getConnection(protocol + dbName
	                     + ";create=true");
	        	
	            return conn;
	            
	        }catch(SQLException se){
	        	logger.error(se,se);
	        	
	       
	        }
	    return null;
	}
	
	public static void closeConnection(Connection conn){
		try{
			DbUtils.close(conn);
		}catch(Exception e){}
	}
	public static void closeStatement(Statement stmt) {
		try{
			DbUtils.close(stmt);
		}catch(Exception e){}
	}

	public static void closeResultSet(ResultSet rs){
		try{
			DbUtils.close(rs);
		}catch(Exception e){}
	}
	
	public static void setDatabaseURL(String urlstring){
		SqlUtils.databaseUrl =urlstring;
	}
	
	private static PoolProperties getPoolProperties() throws Exception{
		PoolProperties p = new PoolProperties();
        p.setUrl(SqlUtils.databaseUrl);
        
        p.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
      //  p.setUsername("tkuser");
       // p.setPassword("tkuser");
        p.setJmxEnabled(true);
        p.setTestWhileIdle(false);
        p.setTestOnBorrow(true);
        p.setValidationQuery("SELECT 1");
        p.setTestOnReturn(false);
        p.setValidationInterval(30000);
        p.setTimeBetweenEvictionRunsMillis(30000);
        p.setMaxActive(100);
        p.setInitialSize(10);
        p.setMaxWait(10000);
        p.setRemoveAbandonedTimeout(60);
        p.setMinEvictableIdleTimeMillis(30000);
        p.setMinIdle(10);
        p.setLogAbandoned(true);
        p.setRemoveAbandoned(true);
        p.setJdbcInterceptors(
          "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
          "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
        
       return p;
	}
	 private static void loadDriver() {
	        /*
	         *  The JDBC driver is loaded by loading its class.
	         *  If you are using JDBC 4.0 (Java SE 6) or newer, JDBC drivers may
	         *  be automatically loaded, making this code optional.
	         *
	         *  In an embedded environment, this will also start up the Derby
	         *  engine (though not any databases), since it is not already
	         *  running. In a client environment, the Derby engine is being run
	         *  by the network server framework.
	         *
	         *  In an embedded environment, any static Derby system properties
	         *  must be set before loading the driver to take effect.
	         */
	        try {
	            Class.forName(driver).newInstance();
	            System.out.println("Loaded the appropriate driver");
	        } catch (ClassNotFoundException cnfe) {
	            System.err.println("\nUnable to load the JDBC driver " + driver);
	            System.err.println("Please check your CLASSPATH.");
	            cnfe.printStackTrace(System.err);
	        } catch (InstantiationException ie) {
	            System.err.println(
	                        "\nUnable to instantiate the JDBC driver " + driver);
	            ie.printStackTrace(System.err);
	        } catch (IllegalAccessException iae) {
	            System.err.println(
	                        "\nNot allowed to access the JDBC driver " + driver);
	            iae.printStackTrace(System.err);
	        }
	    }
	 
	 
	 public static Connection getConnectionx()
	   {
	   //   ## DEFINE VARIABLES SECTION ##
	   // define the driver to use 
	      String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	   // the database name  
	      String dbName="connectDBx";
	   // define the Derby connection URL to use 
	      String connectionURL = "jdbc:derby:" + dbName + ";create=true";

	      	      //   Beginning of JDBC code sections   
	      //   ## LOAD DRIVER SECTION ##
	      try	        {
	          /*
	          **  Load the Derby driver. 
	          **     When the embedded Driver is used this action start the Derby engine.
	          **  Catch an error and suggest a CLASSPATH problem
	           */
	         loadDriver();
	      } catch(Exception e)     {
	          System.err.print("no database found: ");
	          System.err.println(e.getMessage());
	          System.out.println("\n    >>> Please check your CLASSPATH variable   <<<\n");
	      }
	      //  Beginning of Primary DB access section
	      //   ## BOOT DATABASE SECTION ##
	      Connection conn = null;
	     try {
	            // Create (if needed) and connect to the database
	            conn = DriverManager.getConnection(connectionURL);		 
	            System.out.println("Connected to database " + dbName);
	            
	            //   ## INITIAL SQL SECTION ## 
	            //   Create a statement to issue simple commands.  
	           
	           

	             // Release the resources (clean up )
	            				
	            System.out.println("Closed connection");

	            //   ## DATABASE SHUTDOWN SECTION ## 
	            /*** In embedded mode, an application should shut down Derby.
	               Shutdown throws the XJ015 exception to confirm success. ***/		
	            /*
	            if (driver.equals("org.apache.derby.jdbc.EmbeddedDriver")) {
	               boolean gotSQLExc = false;
	               try {
	                  DriverManager.getConnection("jdbc:derby:;shutdown=true");
	               } catch (SQLException se)  {	
	                  if ( se.getSQLState().equals("XJ015") ) {		
	                     gotSQLExc = true;
	                  }
	               }
	               if (!gotSQLExc) {
	               	  System.out.println("Database did not shut down normally");
	               }  else  {
	                  System.out.println("Database shut down normally");	
	               }  
	            }
	            */
	            
	         //  Beginning of the primary catch block: uses errorPrint method
	         }  catch (Throwable e)  {   
	            /*       Catch all exceptions and pass them to 
	            **       the exception reporting method             */
	        	 e.printStackTrace();
	            System.out.println(" . . . exception thrown:");
	            errorPrint(e);
	         }
	         System.out.println("Getting Started With Derby JDBC program ending.");
	         return conn;
	      }
	     //   ## DERBY EXCEPTION REPORTING CLASSES  ## 
	    /***     Exception reporting methods
	    **      with special handling of SQLExceptions
	    ***/
	      static void errorPrint(Throwable e) {
	         if (e instanceof SQLException) 
	            SQLExceptionPrint((SQLException)e);
	         else {
	            System.out.println("A non SQL error occured.");
	            e.printStackTrace();
	         }   
	      }  // END errorPrint 

	    //  Iterates through a stack of SQLExceptions 
	      static void SQLExceptionPrint(SQLException sqle) {
	         while (sqle != null) {
	            System.out.println("\n---SQLException Caught---\n");
	            System.out.println("SQLState:   " + (sqle).getSQLState());
	            System.out.println("Severity: " + (sqle).getErrorCode());
	            System.out.println("Message:  " + (sqle).getMessage()); 
	            sqle.printStackTrace();  
	            sqle = sqle.getNextException();
	         }
	   }  //  END SQLExceptionPrint   	
	 
	 
}
