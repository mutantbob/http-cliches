package com.purplefrog.apachehttpcliches;

import java.net.*;

import org.apache.http.*;
import org.apache.http.message.*;
import org.apache.http.protocol.*;

/**
 * This is a self-contained response object for the Apache HTTP core
 * components library.
 *
 * <p>Copyright (C) 2012 Robert Forsman, Purple Frog Software
 * $Author thoth $
 * $Date 12/2/11 $
 */
public class EntityAndHeaders
{
    public int statusCode;
    public Header[] extraHeaders;
    public HttpEntity en;

    public EntityAndHeaders(int statusCode, Header[] extraHeaders, HttpEntity en)
    {
        this.statusCode = statusCode;
        this.extraHeaders = extraHeaders;
        this.en = en;
    }

    public EntityAndHeaders(int statusCode, HttpEntity en, Header... extraHeaders)
    {
        this.statusCode = statusCode;
        this.extraHeaders = extraHeaders;
        this.en = en;
    }

    public void apply(HttpResponse resp)
    {
        resp.setStatusCode(statusCode);
        if (null != extraHeaders) {
            for (Header header : extraHeaders) {
                resp.setHeader(header);
            }
        }
        resp.setEntity(en);
    }

    public void addHeader(String key, String value)
    {
        addHeader(new BasicHeader(key, value));
    }

    public synchronized void addHeader(Header hdr)
    {
        Header[] newVal;
        if (extraHeaders == null ) {
            newVal = new Header[] { hdr};
        } else {
            newVal = new Header[extraHeaders.length + 1];
            System.arraycopy(extraHeaders, 0, newVal, 1, extraHeaders.length);
            newVal[0] = hdr;
        }
        extraHeaders = newVal;
    }

    public static EntityAndHeaders plainTextPayload(int statusCode, String payload)
    {
        return plainPayload(statusCode, payload, "text/plain");
    }

    public static EntityAndHeaders plainPayload(int statusCode, String payload, String mimeType)
    {
        return new EntityAndHeaders(statusCode, new Header[]{new BasicHeader("Content-Type", mimeType)}, ApacheHTTPCliches.boringStringEntity(payload));
    }

    public static EntityAndHeaders plainPayload(int statusCode, byte[] payload, String mimeType)
    {
        return new EntityAndHeaders(statusCode, null, ApacheHTTPCliches.boringByteEntity(payload, mimeType));
    }

    /**
     * @see ApacheHTTPCliches#redirectPath(HttpContext, String)
     */
    public static class Redirect
        extends EntityAndHeaders
    {
        /**
         * @see ApacheHTTPCliches#redirectPath(HttpContext, String)
         */
        public Redirect(String newAbsoluteURL, String payload)
        {
            this(newAbsoluteURL, payload, "text/plain");
        }

        /**
         * @see ApacheHTTPCliches#redirectPath(HttpContext, String)
         */
        public Redirect(String newAbsoluteURL, String payload, String contentType)
        {
            super(302, new Header[] {
                new BasicHeader("Location", newAbsoluteURL)
            }, ApacheHTTPCliches.boringStringEntity(payload, contentType));
        }

        /**
         * @see ApacheHTTPCliches#redirectPath(HttpContext, String)
         */
        public Redirect(URL newAbsoluteURL)
        {
            this(newAbsoluteURL.toString());
        }

        /**
         * @see ApacheHTTPCliches#redirectPath(HttpContext, String)
         */
        public Redirect(String newAbsoluteURL)
        {
            super(302, new Header[] {
                new BasicHeader("Location", newAbsoluteURL)
            }, ApacheHTTPCliches.boringStringEntity("the document you want is currently at "+newAbsoluteURL+"\n", "text/plain"));
        }
    }
}
