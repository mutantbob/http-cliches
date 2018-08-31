package com.purplefrog.apachehttp5cliches;

import java.net.*;

import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.message.*;
import org.apache.hc.core5.http.nio.*;
import org.apache.hc.core5.http.protocol.*;
import org.apache.hc.core5.net.*;
import org.apache.log4j.*;

/**
 * <p>Copyright (C) 2018 Robert Forsman, Ericsson SATV
 * $Author thoth $
 * $Date 8/20/18 $
 */
public class RedirectResponse
{
    private static final Logger logger = Logger.getLogger(RedirectResponse.class);

    public static AsyncResponseProducer redirectRelative(HttpContext context, String relativeURL)
    {
        HttpResponse resp = new BasicHttpResponse(302);
        String other = redirectPath(context, relativeURL);
        resp.addHeader("Location", other);
        return new BasicResponseProducer(resp, "redirect : "+other+"\n");
    }

    public static String redirectPath(HttpContext context, String newPath)
    {
        BasicHttpRequest req = (BasicHttpRequest) context.getAttribute(HttpCoreContext.HTTP_REQUEST);
        URIAuthority target = req.getAuthority();
        try {
            URI uri = new URI("http", target.getUserInfo(), target.getHostName(), target.getPort(), req.getRequestUri(), null, null);
            return uri.resolve(newPath).toString();
        } catch (URISyntaxException e) {
            logger.warn("wat", e);
        }

        // fake it if the above failed

        StringBuilder rval = new StringBuilder("http://"+target.getHostName()+":"+target.getPort());
        if ( ! newPath.startsWith("/")) {
            rval.append('/');
        }
        rval.append(newPath);
        return rval.toString();
    }
}
