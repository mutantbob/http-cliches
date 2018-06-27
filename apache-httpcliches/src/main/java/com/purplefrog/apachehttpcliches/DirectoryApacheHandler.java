package com.purplefrog.apachehttpcliches;

import java.io.*;
import java.net.*;

import com.purplefrog.httpcliches.*;
import org.apache.http.*;
import org.apache.http.entity.*;
import org.apache.http.message.*;
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

    public final static BasicHeader ACCEPT_RANGES_BYTES = new BasicHeader("Accept-Ranges", "bytes");


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
        handle(request, response, context, null);
    }

    public void handle(HttpRequest request, HttpResponse response, HttpContext context, TransferCallback callback)
        throws HttpException, IOException
    {
        final RequestLine rl = request.getRequestLine();
        final String method = rl.getMethod();

        logger.debug(method+" "+rl.getUri());

        EntityAndHeaders rval;
        try {
            URI uri = URI.create(rl.getUri());
            if (uri.getPath().startsWith(prefix)) {
                URI suffix = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath().substring(prefix.length()), uri.getQuery(), uri.getFragment());

                if ("GET".equals(method)) {
                    rval = handleGET(context, suffix, ApacheHTTPCliches.getRangeHeader(request), callback);
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

    public EntityAndHeaders handleGET(HttpContext context, URI suffix_, String range, TransferCallback callback)
        throws IOException
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

            ContentType mime = ApacheHTTPCliches.mimeTypeFor(target);
            EntityFactory factory = (f, brss, contentType)->DirectoryApacheHandler.this.entityFor(f, brss, contentType, callback);
            if (range==null) {
                HttpEntity entity = factory.entityFor(target, new ByteRangeSpec[]{ new ByteRangeSpec(0, target.length()-1)}, mime.getMimeType());
                return new EntityAndHeaders(200, entity, ACCEPT_RANGES_BYTES);
            } else
                return handleSubset2(target, mime, range, factory);


        } else {
            return EntityAndHeaders.plainTextPayload(404, "Not Found");
        }
    }

    public static EntityAndHeaders handleSubset2(File f, ContentType mime, String rangeHeader, EntityFactory factory)
        throws IOException
    {
        long totalFileLength = f.length();

        ByteRangeSpec[] brs = ByteRangeSpec.parseMultiRange(rangeHeader, totalFileLength);
        if (null==brs) {
            logger.warn("null ByteRangeSpec after parsing "+rangeHeader);
            EntityAndHeaders rval = EntityAndHeaders.plainTextPayload(416, "Range Not Satisfiable\ncould not parse   "+rangeHeader+"\n");
            rval.addHeader("Content-Range", "bytes */"+f.length());
            return rval;
        }

        StringBuilder errLog = new StringBuilder();
        if (!ByteRangeSpec.satisfiable(brs, totalFileLength, errLog)) {
            EntityAndHeaders rval = EntityAndHeaders.plainTextPayload(416, "Range Not Satisfiable\n"+errLog+"\n");
            rval.addHeader("Content-Range", "bytes */"+f.length());
            return rval;
        }

        PartialFileEntity en = factory.entityFor(f, brs, mime.getMimeType());
        return new EntityAndHeaders(206, en, en.extraHeaders());
    }

    public static EntityAndHeaders handleSubset(File f, String contentType, String rangeHeader, EntityFactory factory)
        throws IOException
    {
        long totalFileLength = f.length();

        ByteRangeSpec[] brs = ByteRangeSpec.parseMultiRange(rangeHeader, totalFileLength);
        if (null==brs) {
            logger.warn("null ByteRangeSpec after parsing "+rangeHeader);
            return new EntityAndHeaders(200, new FileEntity(f, contentType));
        }

        PartialFileEntity en = factory.entityFor(f, brs, contentType);
        return new EntityAndHeaders(206, en, en.extraHeaders());
    }

    public PartialFileEntity entityFor(File f, ByteRangeSpec[] brss, String contentType, TransferCallback callback)
        throws IOException
    {
        return new PartialFileEntity(f, brss, contentType, callback);
    }

    public String indexThingyFor(File target)
    {
        return "index.html";
    }

    public interface EntityFactory
    {
        PartialFileEntity entityFor(File f, ByteRangeSpec[] brss, String contentType)
            throws IOException;
    }
}
