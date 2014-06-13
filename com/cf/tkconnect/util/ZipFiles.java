/*
 * Created on Sep 23, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.cf.tkconnect.util;

import java.io.*;
import java.util.zip.*;

import com.cf.tkconnect.log.*;
import com.cf.tkconnect.util.ZipFiles;

/**
 * @author cyrilf
 * 
 */
public class ZipFiles 
{
	static Log logger = LogSource.getInstance(ZipFiles.class);

	public ZipFiles() {}

	public void startZip(String inputfile, String zipfilename) 
	{
		logger.debug("Zipping dir startZip inputfile:" + inputfile);
		ZipOutputStream out = null;
		FileOutputStream f = null;
		try 
		{
			File inp = new File(inputfile);
			if (!inp.exists())
			{
				return;
			}

			f = new FileOutputStream(zipfilename);

			/*
			 * CheckedOutputStream csum = new CheckedOutputStream( f, new
			 * Adler32());
			 */
			out = new ZipOutputStream(new BufferedOutputStream(f));
			out.setComment("Zipped files for business process.");
			out.setLevel(Deflater.DEFAULT_COMPRESSION);
			doZip(out, inp.getAbsolutePath(), new String(inp.getAbsolutePath()));
			logger.debug("Zipping dir endZip zipfilename:" + zipfilename);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
		finally 
		{
			try 
			{
				if (out != null) 
				{
					out.close();
				}
				if (f != null)
				{
					f.close();
				}
			} 
			catch (Exception ee) {} 
		}
	}

	public void doZip(ZipOutputStream out, String inputfile, String basedir)
			throws Exception 
	{
		File f = new File(inputfile);
		if (f.isDirectory()) 
		{
			// if(!inputfile.endsWith(File.separator)) inputfile += "/";
			logger.debug("Zipping dir information dir:" + inputfile);
			ZipEntry ze = new ZipEntry(getBaseDir(inputfile, basedir));
			File[] listfiles = f.listFiles();
			if (listfiles == null)
			{
				return;
			}
			for (int i = 0; i < listfiles.length; i++) 
			{
				if (listfiles[i].isFile())
				{
					writeFile(out, listfiles[i].getAbsolutePath(), basedir);
				}
				else if (listfiles[i].isDirectory())
				{
					doZip(out, listfiles[i].getAbsolutePath(), basedir);
				}
			}
		} 
		else
		{
			writeFile(out, f.getAbsolutePath(), "");
		}
	}

	public void writeFile(ZipOutputStream out, String filename, String basedir)
			throws Exception 
	{
		byte[] buffer = new byte[1024];
		FileInputStream in = null;
		try 
		{
			in = new FileInputStream(filename);
			out.putNextEntry(new ZipEntry(getBaseDir(filename, basedir)));
			int len;
			while ((len = in.read(buffer)) > 0)
			{
				out.write(buffer, 0, len);
			}
		} 
		catch (IOException io) {} 
		finally 
		{
			try 
			{
				out.closeEntry();
				if (in != null)
				{
					in.close();
				}
			} 
			catch (IOException ioe) {} 
		}
	}

	public String getBaseDir(String dir, String basedir) 
	{
		if (dir == null || dir.length() == 0)
		{
			return dir;
		}

		String returnstr = dir;
		int c = dir.indexOf(basedir);
		if (c >= 0)
		{
			returnstr = dir.substring(basedir.length());
		}
		logger.debug("getBaseDir dir:" + dir + " basedir:" + basedir
				+ " returnstr:" + returnstr);
		return returnstr;
	}
}
