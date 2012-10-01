package com.purplefrog.httpcliches;

import java.io.*;
import java.net.*;

import com.sun.net.httpserver.*;
import org.apache.log4j.*;

/**
 * <p>Copyright (C) 2012 Robert Forsman, Purple Frog Software
 * $Author thoth $
 * $Date 8/23/12 $
 */
public class DirectorySunHandler
    implements HttpHandler
{
    private static final Logger logger = Logger.getLogger(DirectorySunHandler.class);


    public final String prefix;
    public final File parentDir;

    public DirectorySunHandler(String prefix, File parentDir)
    {
        this.prefix = prefix;
        this.parentDir = parentDir;
    }

    @Override
    public void handle(HttpExchange hx)
        throws IOException
    {
        HTTPTools.Response rval;
        try {
            rval = handle_(hx);
        } catch (Exception e) {
            rval = new HTTPTools.SimpleResponse(500, "I am full of explosions\n"+ Util2.stringStackTrace(e));
        }

        rval.apply(hx);
    }

    public HTTPTools.Response handle_(HttpExchange hx)
        throws MalformedURLException, FileNotFoundException
    {
        String path = hx.getRequestURI().getPath();
        if (!path.startsWith(prefix)) {
            return new HTTPTools.SimpleResponse(500, "path does not match configured prefix");
        }

        String suffix = path.substring(prefix.length());
        while (suffix.startsWith("/"))
            suffix = suffix.substring(1);

        if (dangerousDotDot(suffix)) {
            return new HTTPTools.SimpleResponse(404, "Not Found");
        }

        File target = new File(parentDir, suffix);

        logger.debug("target file is "+target);

        if (target.isDirectory()) {
            return new HTTPTools.RedirectResponse(HTTPTools.urlFor(hx, prefix+"/"+suffix+"/index.html"));
        }

        if (! target.exists()) {
            return new HTTPTools.SimpleResponse(404, "Not Found");
        }

        if ("GET".equals(hx.getRequestMethod())) {
            return new HTTPTools.StreamedResponse(200, new FileInputStream(target), target.length(), Util2.mimeTypeFor(target));
        } else {
            return new HTTPTools.SimpleResponse(405, "Method Not Allowed");
        }
    }

    public static boolean dangerousDotDot(String relativePath)
    {
        return relativePath.matches("^(.*?/)?\\.\\./");
    }
}
