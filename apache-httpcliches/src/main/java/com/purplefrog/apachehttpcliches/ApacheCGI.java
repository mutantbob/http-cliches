package com.purplefrog.apachehttpcliches;

import java.io.*;
import java.net.*;
import java.util.*;
import com.purplefrog.httpcliches.*;
import org.apache.http.*;
import org.apache.http.protocol.*;
import org.apache.log4j.*;

/**
 * <p>Copyright (C) 2012 Robert Forsman, Purple Frog Software
 * $Author thoth $
 * $Date 6/25/12 $
 */
public class ApacheCGI
{
    private static final Logger logger = Logger.getLogger(ApacheCGI.class);

    /**
     * <p> Note: In the case of a POST, this routine will consume the request body.
     * This means that a subsequent call to this same method will have nothing to read, so hang on to the results.</p>
     */
    public static Map<String, List<String>> parseCGI(HttpRequest req)
        throws URISyntaxException, IOException
    {
        String method = req.getRequestLine().getMethod();
        if ("POST".equals(method)) {
            return HTMLTools.parseCGIArgs(ApacheHTTPCliches.requestBodyAsString(req));
        } else if ("GET".equals(method)) {
            return HTMLTools.parseCGIArgs(new URI(req.getRequestLine().getUri()));
        } else {
            return null;
        }
    }

    public static CGIEnvironment parseEnv(HttpRequest req, HttpContext context)
        throws IOException, URISyntaxException
    {
        CGIEnvironment rval = new CGIEnvironment(parseCGI(req));
        HttpHost targetHost = ApacheHTTPCliches.httpTargetHost(context);
        rval.serverName = targetHost.getHostName();
        rval.serverPort = targetHost.getPort();
        rval.pathInfo = req.getRequestLine().getUri();
        return rval;
    }
}
