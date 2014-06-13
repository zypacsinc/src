package com.cf.tkconnect.util;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpException;

import com.cf.tkconnect.util.PingServer;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;

import static com.cf.tkconnect.util.WSUtil.getMainServiceUrl;



public class PingServer {
	static com.cf.tkconnect.log.Log logger = com.cf.tkconnect.log.LogSource
			.getInstance(PingServer.class);

	

	public static String getServerResponse(String urlStr) {

		String response = null;
		HttpURLConnection con = null;
		try {
			// Execute the method.
			//URL url = new URL(getMainServiceUrl(urlStr));
			//logger.debug("pinging the url string urlStr::"+urlStr);
			URL url = new URL(urlStr);
			con = (HttpURLConnection)url.openConnection();
			int statusCode = con.getResponseCode();
			//logger.debug("getServerResponse for url:" + url	+ "  recd statusCode:" + statusCode);
			if (statusCode != HttpStatus.SC_OK) {
				response = "Method failed: " + statusCode;
			}

		} catch (HttpException e) {
			response = "Fatal protocol violation: " + e.getMessage();
			
		} catch (IOException e) {
			response = "Fatal transport error: " + e.getMessage();
			
		} finally {
			// Release the connection.
			if(con != null) {
				con.disconnect();
			}
		}
		//logger.debug("getServerResponse:"+response);
		return response;
	}
}