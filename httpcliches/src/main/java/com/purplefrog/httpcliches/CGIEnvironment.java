package com.purplefrog.httpcliches;

import java.net.*;
import java.util.*;

/**
 * <p>Copyright (C) 2012 Robert Forsman, Purple Frog Software
 * $Author thoth $
 * $Date 8/2/12 $
 */
public class CGIEnvironment
{
    public String serverName;
    public int serverPort;
    public String pathInfo;
    public Map<String, List<String>> args;
    public InetAddress remoteAddress=null;
    public int remotePort=0;

    public CGIEnvironment(Map<String, List<String>> args)
    {
         this.args = args;
    }

    public URL getThisURL()
        throws MalformedURLException
    {
        return new URL("http", serverName, serverPort, pathInfo);
    }

    public String remoteAddressString()
    {
        if (remoteAddress!=null)
            return remoteAddress.getHostAddress();
        else
            return null;
    }
}
