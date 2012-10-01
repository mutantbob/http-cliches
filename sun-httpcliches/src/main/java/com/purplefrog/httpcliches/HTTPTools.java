package com.purplefrog.httpcliches;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;

import com.sun.net.httpserver.*;

/**
 * <<p>Copyright (C) 2012 Robert Forsman, Purple Frog Software
 * <br>$ Author thoth $
 * <br>$ Date: May 4, 2011$
 */
public class HTTPTools
{

    public static void clicheError(HttpExchange hx, int statusCode, String message)
        throws IOException
    {
        String payload = statusCode+" "+message+"\n";
        byte[] payload_ = payload.getBytes("UTF-8");

        sendSimpleResponse(hx, statusCode, payload_, "text/plain; charset=utf-8");
    }

    public static void sendSimpleResponse(HttpExchange hx, int statusCode, byte[] payload, String contentType)
        throws IOException
    {
        new SimpleResponse(statusCode, payload, contentType).apply(hx);
    }


    public static void sendSimpleResponse(HttpExchange hx, int statusCode, String payload, String contentType)
        throws IOException
    {
        sendSimpleResponse(hx, statusCode, payload.getBytes("UTF-8"), contentType+"; charset=utf-8");
    }

    public static void clicheRedirect(HttpExchange hx, URL u)
        throws IOException
    {
        hx.getResponseHeaders().add("Location", u.toString());
        sendSimpleResponse(hx, 302, "redirect", "text/plain");
    }

    public static URL urlFor(HttpExchange hx)
        throws MalformedURLException
    {
        String hostPort = hx.getRequestHeaders().getFirst("Host");
        if (hostPort==null) {
            InetSocketAddress addr = hx.getLocalAddress();
            return new URL("http", addr.getAddress().getHostAddress(), addr.getPort(), "/");
        } else {
            return new URL("http://"+hostPort+"/");
        }
    }

    /**
     * What is the address of our server according to the client?
     */
    public static String serverAddress(HttpExchange hx)
        throws MalformedURLException
    {
        String hostPort = hx.getRequestHeaders().getFirst("Host");
        if (hostPort==null) {
            InetSocketAddress addr = hx.getLocalAddress();

            return addr.getAddress().getHostAddress();
        }

        int idx = hostPort.indexOf(':');
        if (idx<0)
            return hostPort;
        else
            return hostPort.substring(0,idx);

    }

    public static URL urlFor(HttpExchange hx, String path)
        throws MalformedURLException
    {
        return new URL(urlFor(hx), path);
    }

    public static void addExpiresHeader(HttpExchange hx, Long wallClockMillis)
    {
        if (null != wallClockMillis) {
            hx.getResponseHeaders().add("Expires", HTMLTools.rfc1123(wallClockMillis));
        }
    }

    public interface Response
    {
         void apply(HttpExchange hx)
             throws IOException;

    }

    public static class SimpleResponse
        implements Response
    {
        public int statusCode;
        public byte[] payload;
        public String contentType;

        public SimpleResponse(int statusCode, byte[] payload, String contentType)
        {
            this.contentType = contentType;
            this.statusCode = statusCode;
            this.payload = payload;
        }

        public SimpleResponse(int statusCode, String payload)
        {
            this(statusCode, payload, "text/plain");
        }

        public SimpleResponse(int statusCode, String payload, String contentType)
        {
            this.contentType = contentType+"; charset=utf-8";
            this.statusCode = statusCode;
            try {
                this.payload = payload.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new UndeclaredThrowableException(e, "what kind of chicken shit VM doesn't support UTF-8?");
            }
        }

        public void apply(HttpExchange hx)
            throws IOException
        {
            hx.getResponseHeaders().add("Content-Type", contentType);
            hx.sendResponseHeaders(statusCode, payload.length);

            OutputStream ostr = hx.getResponseBody();
            ostr.write(payload);
            ostr.close();
        }
    }

    public static class StreamedResponse
        implements Response
    {
        private int statusCode;
        private InputStream istr;
        private long contentLength;
        private String contentType;

        public StreamedResponse(int statusCode, InputStream istr, long contentLength, String contentType)
        {
            this.statusCode = statusCode;
            this.istr = istr;
            this.contentLength = contentLength;
            this.contentType = contentType;
        }

        public void apply(HttpExchange hx)
            throws IOException
        {
            hx.getResponseHeaders().add("Content-Type", contentType);
            hx.sendResponseHeaders(statusCode, contentLength);

            long cursor=0;
            byte[] buffer = new byte[16<<10];

            OutputStream ostr = hx.getResponseBody();
            try {
                while (cursor<contentLength) {
                    int n = istr.read(buffer);

                    if (n<0) {
                        break;
                    }

                    ostr.write(buffer, 0, n);
                    cursor+=n;
                }
            } finally {
                ostr.close();
                istr.close();
            }
        }
    }

    public static class RedirectResponse
        implements Response
    {
        private URL u;

        public RedirectResponse(URL u)
        {
            this.u = u;
        }

        public void apply(HttpExchange hx)
            throws IOException
        {
            clicheRedirect(hx, u);
        }
    }
}
