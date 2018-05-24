package com.purplefrog.httpcliches;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import javax.jws.*;
import com.sun.net.httpserver.*;
import org.apache.log4j.*;

/**
 * <p>Copyright (C) 2012 Robert Forsman, Purple Frog Software
 * $Author thoth $
 * $Date 5/14/12 $
 */
public class SunCGIWebMethod
    implements HttpHandler
{
    private static final Logger logger = Logger.getLogger(SunCGIWebMethod.class);

    Map<String, Object> thingies = new HashMap<String, Object>();
    
    public void register(String tag, Object thingus)
    {
        thingies.put(tag, thingus);
    }

    @Override
    public void handle(HttpExchange hx)
        throws IOException
    {
        HTTPTools.Response x;
        try {
            URI uri = hx.getRequestURI();
            String path = uri.getPath();
            int idx = path.indexOf('/');
            if (idx<0) {
                x=null;
            } else {
                String prefix = path.substring(0, idx);
                Object thingus = thingies.get(prefix);
                if (null == thingus) {
                    x=null;
                } else {
                    x = processThingus(thingus, hx, path.substring(idx+1));
                }
            }
        } catch (Exception e) {
            logger.warn("malfunction transforming CGI request to WebMethod", e);
            x=null;
        }

        if (x==null)
            x = new HTTPTools.SimpleResponse(404, "not found");
        
        x.apply(hx);
    }

    public static HTTPTools.Response processThingus(Object thingus, HttpExchange hx, String suffix)
        throws IOException, InvocationTargetException, IllegalAccessException, CGIWebMethod.CGISOAPTransformException
    {
        Method m = CGIWebMethod.matchName(thingus.getClass(), suffix);

        if (m==null)
            return null;

        Map<String, List<Object>> cgiArgs = SunCGI.parseCGI(hx);

        Object[] params = CGIWebMethod.transformCGIArgumentsToJavaParams(m, new CGIEnvironment(cgiArgs));

        Object result = m.invoke(thingus, params);

        WebParam ra = m.getAnnotation(WebParam.class);
        if (ra!=null) {

        }

        return new HTTPTools.SimpleResponse(200, "OK");
    }


}
