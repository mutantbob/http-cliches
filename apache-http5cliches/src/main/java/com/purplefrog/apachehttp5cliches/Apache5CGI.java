package com.purplefrog.apachehttp5cliches;

import java.net.*;
import java.nio.charset.*;
import java.util.*;

import com.purplefrog.httpcliches.*;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.protocol.*;

/**
 * <p>Copyright (C) 2018 Robert Forsman, Ericsson SATV
 * $Author thoth $
 * $Date 8/30/18 $
 */
public class Apache5CGI
{
    public static com.purplefrog.httpcliches.CGIEnvironment parseEnv(Message<HttpRequest, byte[]> message, HttpContext context)
        throws URISyntaxException
    {
        HttpRequest req = message.getHead();
        Map<String, List<Object>> args;
        if ("GET".equals(req.getMethod())) {
            args = HTMLTools.parseCGIArgs(req.getUri());
        } else {
            byte[] b = message.getBody();
            String payload = new String(b, Charset.forName("UTF-8"));
            args = HTMLTools.parseCGIArgs(payload);
        }

        return new CGIEnvironment(args);
    }
}
