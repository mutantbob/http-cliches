package com.purplefrog.apachehttpcliches;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import javax.jws.*;
import com.purplefrog.httpcliches.*;
import org.apache.commons.fileupload.*;
import org.apache.http.*;
import org.apache.http.protocol.*;
import org.apache.log4j.*;

/**
 * <p>Copyright (C) 2012 Robert Forsman, Purple Frog Software
 * $Author thoth $
 * $Date 6/29/12 $
 */
public class ApacheCGIWebMethod
    implements HttpRequestHandler
{
    private static final Logger logger = Logger.getLogger(ApacheCGIWebMethod.class);

    public final String prefix;

    Map<String, Object> thingies = new HashMap<String, Object>();

    public ApacheCGIWebMethod(String prefix)
    {
        this.prefix = prefix;
    }

    public void register(String tag, Object thingus)
    {
        thingies.put(tag, thingus);
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
        throws HttpException, IOException
    {

        EntityAndHeaders x;
        try {
            URI uri = new URI(request.getRequestLine().getUri());
            String path = uri.getPath().substring(prefix.length());
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            int idx = path.indexOf('/');
            if (idx<0) {
                x=null;
            } else {
                String prefix = path.substring(0, idx);
                Object thingus = thingies.get(prefix);
                if (null == thingus) {
                    x=null;
                } else {
                    x = processThingus_(request, path, idx, thingus, context);
                }
            }
        } catch (Exception e) {
            logger.warn("malfunction transforming CGI request to WebMethod", e);
            x=EntityAndHeaders.plainTextPayload(500, "blam:"+ Util2.stringStackTrace(e));
        }

        if (x==null)
            x = EntityAndHeaders.plainTextPayload(404, "not found");

        x.apply(response);
    }

    /**
     * This exists to be overridden by the user.
     */
    public EntityAndHeaders processThingus_(HttpRequest request, String path, int idx, Object thingus, HttpContext context)
        throws InvocationTargetException, IllegalAccessException, IOException, URISyntaxException, CGIWebMethod.CGISOAPTransformException, FileUploadException
    {
        return processThingus(thingus, request, path.substring(idx+1), context);
    }

    /**
     * Find a method on thingus annotated with an {@link WebMethod} whose name matches methodName.
     * Then extract CGI parameters from req and translate them into the paramaters of the relevant method
     * according to {@link WebParam} annotations on those parameters.
     *
     * @return null if there is no matching @WebMethod. If the invocation returns an {@link EntityAndHeaders} we return that, otherwise we'll return a 200/OK.
     *
     * @see CGIWebMethod#transformCGIArgumentsToJavaParams(Method, CGIEnvironment)
     */
    public static EntityAndHeaders processThingus(Object thingus, HttpRequest req, String methodName, HttpContext context)
        throws InvocationTargetException, IllegalAccessException, IOException, URISyntaxException, CGIWebMethod.CGISOAPTransformException, FileUploadException

    {
        Class<? extends Object> cls = thingus.getClass();
        Method m = CGIWebMethod.matchName(cls, methodName);

        if (m==null) {
            logger.debug(cls.getName()+" has no method with @WebMethod matching '"+methodName+"'");
            return null;
        }

        CGIEnvironment cgiEnv = ApacheCGI.parseEnv(req, context);

        Object[] params = CGIWebMethod.transformCGIArgumentsToJavaParams(m, cgiEnv);

        Object result = m.invoke(thingus, params);

        WebParam ra = m.getAnnotation(WebParam.class);
        if (ra!=null) {

        }

        if (result instanceof EntityAndHeaders)
            return (EntityAndHeaders) result;

        return EntityAndHeaders.plainTextPayload(200, "OK");
    }

}
