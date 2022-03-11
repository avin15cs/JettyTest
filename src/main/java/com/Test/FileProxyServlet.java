package com.Test;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import java.io.*;

public class FileProxyServlet extends HttpServlet
{

    public static String OUTPUT_DIR = null;
    private File outDir=null;


    public void init( ServletConfig config ) throws ServletException
    {
        try
        {
            super.init( config);
//            OUTPUT_DIR = config.getServletContext().getContext("/cts/output").getRealPath("/");
            OUTPUT_DIR=config.getInitParameter("fileContentBase");
            outDir = new File(OUTPUT_DIR).getAbsoluteFile();
        }
        catch (Exception e)
        {
//            _realtimeLogger.info( "Exception while initializing the com.jetty.Test.FileProxyServlet");
            System.out.println("Exception while initializing the com.jetty.Test.FileProxyServlet");
        }
    }

    /**
     * Handle a 'doGet' Request
     *
     * @param  req   the Servlet Request
     * @param  res   the Servlet Response
     * @throws ServletException
     * @throws IOException
     */
    public final void doGet( HttpServletRequest req, HttpServletResponse res )
            throws ServletException, IOException
    {
        doPost( req, res );
    }


    /**
     * Handle a 'doPost' Request
     *
     * @param  req   the Servlet Request
     * @param  res   the Servlet Response
     * @throws ServletException
     * @throws IOException
     */
    public final void doPost( HttpServletRequest req, HttpServletResponse res )
            throws ServletException, IOException
    {
        execute( req, res );
    }

    /**
     *  Sends a file to the ServletResponse output stream.  Typically
     *  you want the browser to receive a different name than the
     *  name the file has been saved in your local database, since
     *  your local names need to be unique.
     *
     *  @param req The request
     *  @param resp The response
     *  @throws IOException if it fails
     */
    private void execute( HttpServletRequest req, HttpServletResponse resp )
            throws IOException
    {
        boolean isClearOnExit = true;
        String clearOnExit = req.getHeader("clearOnExit");
        System.out.println("clearOnExit : " + clearOnExit);
        if( clearOnExit != null && clearOnExit.equals("false"))
        {
            isClearOnExit = false;
        }
//        String theOrgFilename = req.getServletPath();
        String theOrgFilename = req.getPathInfo();
        String theLocalFileName = null;
        try
        {
            if( theOrgFilename != null )
            {
                int index = theOrgFilename.lastIndexOf( "/");
                theOrgFilename = theOrgFilename.substring( index + 1 );
                theLocalFileName = OUTPUT_DIR + theOrgFilename;
            }
            File theFile = new File(theLocalFileName);

            if(!theFile.getCanonicalPath().toLowerCase().startsWith(outDir.getCanonicalPath().toLowerCase())){
                throw new Exception("Invalid file path " + theLocalFileName);
            }

            int length ;
            ServletOutputStream op = resp.getOutputStream();
            ServletContext context = getServletConfig().getServletContext();
            String mimetype = context.getMimeType( theLocalFileName );
            resp.setContentType( (mimetype != null) ? mimetype : "application/octet-stream" );
            resp.setHeader( "Content-Disposition", "attachment; filename=\"" + theOrgFilename + "\"" );

            byte[] bbuf = new byte[2048];
            DataInputStream in = new DataInputStream(new FileInputStream(theFile));

            while ((length = in.read(bbuf)) != -1)
            {
                op.write(bbuf,0,length);
            }
            in.close();
            op.flush();
            op.close();
            System.out.println("Successfully downloaded the file : " + theLocalFileName );
        }
        catch(Exception e)
        {
            System.out.println("Failed to download the file : " + theLocalFileName);
        }
        finally
        {
//            _realtimeLogger.debug( "Deleting the local file : " + theLocalFileName );
            System.out.println("Deleting the local file : " + theLocalFileName);
            if( isClearOnExit )
            {
                if( theLocalFileName != null && new File(theLocalFileName).exists())
                {
                    if (!new File(theLocalFileName).delete())
//                        _realtimeLogger.debug("File " + theLocalFileName + " could not be deleted");
                        System.out.println("File " + theLocalFileName + " could not be deleted");
                }
            }
        }
    }
}
