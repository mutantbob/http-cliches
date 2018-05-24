package com.purplefrog.httpcliches;

import java.io.*;
import java.net.*;
import java.util.*;
import com.sun.net.httpserver.*;
import org.apache.log4j.*;

/**
 * <p>Copyright (C) 2012 Robert Forsman, Purple Frog Software
 * $Author thoth $
 * $Date 6/26/12 $
 */
public class SunCGI
{
    private static final Logger logger = Logger.getLogger(SunCGI.class);


    public static Map<String, List<Object>> parseCGI(HttpExchange hx)
        throws IOException
    {
        if ("GET".equals(hx.getRequestMethod())) {
            return HTMLTools.parseCGIArgs(hx.getRequestURI());

        } else if ("POST".equals(hx.getRequestMethod())) {
            String bod = Util2.slurp(new InputStreamReader(hx.getRequestBody()));
            return HTMLTools.parseCGIArgs(bod);

        } else {
            throw new UnsupportedOperationException("NYI");
        }
    }

    public static CGIEnvironment parseEnv(HttpExchange hx)
        throws IOException
    {
        CGIEnvironment rval = new CGIEnvironment(parseCGI(hx));


        String hostPort = hx.getRequestHeaders().getFirst("Host");
        if (hostPort==null) {
            InetSocketAddress sa = hx.getLocalAddress();

            rval.serverName = sa.getAddress().getHostAddress();
            rval.serverPort = sa.getPort();
        } else {
            int idx = hostPort.indexOf(':');
            rval.serverPort = 80;
            if (idx<0) {
                rval.serverName = hostPort;
            } else {
                rval.serverName = hostPort.substring(0, idx);
                try {
                    rval.serverPort = Integer.parseInt(hostPort.substring(idx+1));
                } catch (NumberFormatException e) {
                    logger.warn("bad port number after : in Host field of HTTP request ('"+hostPort.substring(idx+1)+"')", e);
                }
            }
        }

        rval.pathInfo = hx.getRequestURI().getPath();

        return rval;
    }
}
