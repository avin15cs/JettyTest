package com.Test;

import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * This servlet is responsible for cleaning up the output files in http cache
 */
public class CleanupJobServlet extends HttpServlet
{
    private static final long serialVersionUID = 0xC9F28807E52FL;

    public static String OUTPUT_DIR = null;
    private File outDir=null;


    public void init( ServletConfig config ) throws ServletException
    {
        try
        {
//            _realtimeLogger =  DfLogger.getLogger(this);
//            OUTPUT_DIR = config.getServletContext().getContext("/cts/output").getRealPath("/");
            OUTPUT_DIR=config.getInitParameter("fileContentBase");
            outDir = new File(OUTPUT_DIR).getAbsoluteFile();
//            _realtimeLogger.info( "CleanupJobServlet is initialized" );
            System.out.println("CleanupJobServlet is initialized" );
        }
        catch (Exception e)
        {
            System.out.println( "Exception while initializing the CleanupJobServlet");
        }
    }

    protected void doGet( HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException
    {
        boolean isSuceeded =  false;
        String theFilePath = null;
        try
        {
            theFilePath = httpServletRequest.getHeader("filepath");
            theFilePath = StringEscapeUtils.escapeHtml(theFilePath);
            System.out.println( "HTTP URL is : " + theFilePath);
            String theLocalFilePath = computeLocalFilePath(theFilePath);
            System.out.println( "The Local File Path is : " + theLocalFilePath);
            File localFile = new File(theLocalFilePath);

            if(!localFile.getCanonicalPath().toLowerCase().startsWith(outDir.getCanonicalPath().toLowerCase())){
                throw new Exception("Invalid file path " + theLocalFilePath);
            }

            if( localFile.exists())
            {
                if (localFile.delete())
                    isSuceeded = true;
                else
                    System.out.println("File " + theLocalFilePath + " could not be deleted");
            }
        }
        catch(Exception e)
        {
            System.out.println("Failed to delete the file : " + theFilePath + e.getMessage());
            isSuceeded = false;
        }
        if ( isSuceeded)
        {
            System.out.println("Successfully deleted the file :" + theFilePath );
            setResponse( httpServletResponse, "OK", "Successfully deleted the file :" + theFilePath);
        }
        else
        {
            System.out.println( "Failed to delete the file : " + theFilePath);
            setResponse( httpServletResponse, "ERROR", "Failed to delete the file : " + theFilePath);
        }

    }

    protected void doPost( HttpServletRequest theHttpServletRequest, HttpServletResponse theHttpServletResponse )
            throws ServletException, IOException
    {
        doGet(theHttpServletRequest, theHttpServletResponse);
    }

    private String computeLocalFilePath( String theFilePath )
    {
        theFilePath = normalizePath(theFilePath);
        int theIndex =  theFilePath.lastIndexOf( "/");
        return  OUTPUT_DIR + theFilePath.substring( theIndex );
    }

    /**
     * Set the response
     *
     * @param response the http response
     * @param statuscode a status
     * @param message a message
     * @throws IOException thrown if out
     */
    protected void setResponse( HttpServletResponse response, String statuscode, String message) throws IOException
    {
        response.setContentType("text/plain");
        response.setHeader("status_code", statuscode);
        response.setHeader("status_message", message);
        PrintWriter out = response.getWriter();
        out.println(message);
    }
    // make url-like path
    private String normalizePath( String thePath )
    {
        String theNormalizedPath;
        if( File.separatorChar != '/' )
        {
            theNormalizedPath = thePath.replace( File.separatorChar, '/' );
        }
        else
        {
            theNormalizedPath = thePath;
        }
        if ( !theNormalizedPath.startsWith("/") )
        {
            theNormalizedPath = "/" + theNormalizedPath;
        }
        return theNormalizedPath;
    }

}


