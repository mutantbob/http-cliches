package com.purplefrog.apachehttpcliches;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.charset.*;
import java.util.regex.*;
import com.purplefrog.httpcliches.*;
import org.apache.http.*;
import org.apache.http.entity.*;
import org.apache.http.impl.*;
import org.apache.http.message.*;
import org.apache.http.protocol.*;
import org.apache.http.util.*;
import org.apache.log4j.*;

/**
 * <p>Copyright (C) 2012 Robert Forsman, Purple Frog Software
 * $Author thoth $
 * $Date 12/2/11 $
 */
public class ApacheHTTPCliches
{
    private static final Logger logger = Logger.getLogger(ApacheHTTPCliches.class);
    public static final Pattern DOT_DOT = Pattern.compile("([^/]+)/+\\.\\./");
    public static final Pattern DOT_DOT_START = Pattern.compile("^(.*/)?\\.\\.(/.*)?$");

    public static void clicheError(HttpResponse resp, int statusCode, String statusExplanation)
    {
        resp.setStatusLine(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), statusCode, statusExplanation));
        resp.setEntity(boringStringEntity(statusExplanation, "text/plain"));
    }

    public static StringEntity boringStringEntity(String payload, String mimeType)
    {
        return new StringEntity(payload, ContentType.create(mimeType, "UTF-8"));
    }

    public static StringEntity boringStringEntity(String payload)
    {
        try {
            return new StringEntity(payload, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UndeclaredThrowableException(e, "Your JVM doesn't support UTF-8");
        }
    }

    public static HttpEntity boringByteEntity(byte[] payload, String mimeType)
    {
        final ByteArrayEntity rval = new ByteArrayEntity(payload);
        rval.setContentType(mimeType);
        return rval;
    }

    public static void sendSimpleResponse(HttpResponse resp, int statusCode, String payload, String mimeType)
    {
        resp.setStatusCode(statusCode);
        resp.setEntity(boringStringEntity(payload, mimeType));
    }

    public static String getRangeHeader(HttpRequest request)
    {
        return getFirstHeader(request, "Range");
    }

    public static String getFirstHeader(HttpRequest request, String key)
    {
        Header rval = request.getFirstHeader(key);
        return null==rval ? null : rval.getValue();
    }

    public static String redirectPath(HttpContext context, String newPath)
    {
        HttpHost target = httpTargetHost(context);
        HttpRequest req = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
        req.getRequestLine().getUri();
        String tmp = target.toURI() + req.getRequestLine().getUri();
        try {
            URI uri = new URI(tmp);
            return uri.resolve(newPath).toString();
        } catch (URISyntaxException e) {
            logger.warn("wat", e);
        }

        StringBuilder rval = new StringBuilder(target.toURI());
        if ( ! newPath.startsWith("/")) {
            rval.append('/');
        }
        rval.append(newPath);
        return rval.toString();
    }

    public static HttpHost httpTargetHost(HttpContext context)
    {
        final HttpHost rval = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
        if (null != rval)
            return rval;

        BasicHttpRequest req = (BasicHttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
        Header[] hdrs = req.getHeaders("Host");
        if (hdrs.length<1)
            return new HttpHost("", 80);
        String hostport = hdrs[0].getValue();
        int idx = hostport.lastIndexOf(':');
        if (idx<0)
            return new HttpHost(hostport);
        else
            return new HttpHost(hostport.substring(0, idx), Integer.parseInt(hostport.substring(idx+1)));
    }


    public static SocketHttpServerConnection remoteAddress(HttpContext ctx)
    {
        return (SocketHttpServerConnection) ctx.getAttribute("http.connection");
    }


    public static String crushDotDot(String path)
    {
        while (true) {
            Matcher m = DOT_DOT.matcher(path);
            if (m.find()) {
                if ("..".equals(m.group(1)))
                    break; // it starts with ..

                String newPath = path.substring(0, m.start()) + path.substring(m.end());
                logger.debug("rewrite"+path+" to "+newPath);
                path = newPath;
            } else {
                break;
            }
        }

        if (DOT_DOT_START.matcher(path).matches())
            return null;

        return path;
    }

    public static ContentType mimeTypeFor(File target)
    {
        return ContentType.create(Util2.mimeTypeFor(target));
    }

    public static ContentType mimeTypeFor(String path)
    {
        return ContentType.create(Util2.mimeTypeFor(path));
    }

    public static String requestBodyAsString(HttpRequest request_)
        throws IOException
    {
        if (!(request_ instanceof HttpEntityEnclosingRequest)) {
            return null;
        }

        final HttpEntityEnclosingRequest request = (HttpEntityEnclosingRequest) request_;

        HttpEntity en = request.getEntity();
        final InputStream istr = en.getContent();
        Charset charSet = ContentType.getOrDefault(en).getCharset();
        Reader r = charSet ==null ? new InputStreamReader(istr):new InputStreamReader(istr, charSet);
        return Util2.slurp(r);
    }

    public static InputStream requestBodyAsInputStream(HttpRequest request_)
        throws IOException
    {
        if (!(request_ instanceof HttpEntityEnclosingRequest)) {
            return null;
        }

        final HttpEntityEnclosingRequest request = (HttpEntityEnclosingRequest) request_;

        HttpEntity en = request.getEntity();
        final InputStream istr = en.getContent();
        return istr;
    }

    public static URI uriFor(HttpContext context, String relative)
        throws URISyntaxException
    {
        return uriFor(context).resolve(relative);
    }

    public static URI uriFor(HttpContext context)
        throws URISyntaxException
    {
        return new URI(httpTargetHost(context).toURI());
    }

    public static Long[] parseRangeHeader(HttpRequest request)
    {
        Header range_ = request.getFirstHeader("Range");
        return Util2.parseRangeHeader(range_ == null ? null : range_.getValue());
    }
}
