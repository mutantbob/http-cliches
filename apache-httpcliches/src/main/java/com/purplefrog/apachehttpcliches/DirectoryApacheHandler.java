package com.purplefrog.apachehttpcliches;

import java.io.*;
import java.net.*;
import com.purplefrog.httpcliches.*;
import org.apache.http.*;
import org.apache.http.entity.*;
import org.apache.http.protocol.*;
import org.apache.log4j.*;

/**
* <p>Copyright (C) 2012 Robert Forsman, Purple Frog Software
* $Author thoth $
* $Date 3/6/12 $
*/
public class DirectoryApacheHandler
    implements HttpRequestHandler
{
    private static final Logger logger = Logger.getLogger(DirectoryApacheHandler.class);


    public final String prefix;
    public final File contentDir;

    public DirectoryApacheHandler(File contentDir, String webPrefix)
    {
        this.contentDir = contentDir;
        prefix = webPrefix;
    }

    public void handle(HttpRequest request, HttpResponse response, HttpContext context)
        throws HttpException, IOException
    {
        final RequestLine rl = request.getRequestLine();
        final String method = rl.getMethod();

        logger.debug(method+" "+rl.getUri());

        EntityAndHeaders rval;
        try {
            URI uri = new URI(null, null, rl.getUri(), null);
            if (uri.getPath().startsWith(prefix)) {
                URI suffix = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath().substring(prefix.length()), uri.getQuery(), uri.getFragment());

                if ("GET".equals(method)) {
                    rval = handleGET(context, suffix);
                } else {
                    rval = EntityAndHeaders.plainTextPayload(501, "501 Not Implemented");
                }
            } else {
                rval = EntityAndHeaders.plainTextPayload(404, "404 not found");
            }

        } catch (Throwable e) {
            logger.warn("handler malfunctioned", e);
            rval = EntityAndHeaders.plainTextPayload(500, "I am full of explosions!\n" + Util2.stringStackTrace(e));
        }
        rval.apply(response);
    }

    public EntityAndHeaders handleGET(HttpContext context, URI suffix_)
    {
        String suffix = suffix_.getPath();
        if ("".equals(suffix)) {
            String u = ApacheHTTPCliches.redirectPath(context, prefix+"/");
            return new EntityAndHeaders.Redirect(u, "redirect to \n"+u);
        }

        suffix = ApacheHTTPCliches.crushDotDot(suffix);
        if (suffix==null) {
            return EntityAndHeaders.plainTextPayload(404, "Not Found");
        }

        File target = new File(contentDir, suffix);

        logger.debug("mapped to "+target);

        if (target.isDirectory()) {
            String u = ApacheHTTPCliches.redirectPath(context, (prefix+"/" +suffix+"/").replaceAll("/+","/")
                + indexThingyFor(target));
            return new EntityAndHeaders.Redirect(u, "redirect to \n"+u);
        }

        if (target.exists()) {
            return new EntityAndHeaders(200, null, new FileEntity(target, ApacheHTTPCliches.mimeTypeFor(target)));

        } else {
            return EntityAndHeaders.plainTextPayload(404, "Not Found");
        }
    }

    public String indexThingyFor(File target)
    {
        return "index.html";
    }

}
