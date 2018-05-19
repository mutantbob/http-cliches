package com.purplefrog.apachehttpcliches;

import org.apache.commons.fileupload.*;
import org.apache.http.*;
import org.apache.http.entity.*;

import java.io.*;
import java.nio.charset.*;

public class ApacheMultipartContext
    implements RequestContext
{
    private final HttpRequest req;

    public ApacheMultipartContext(HttpRequest req)
    {
        this.req = req;
    }

    public String getCharacterEncoding()
    {
        if (req instanceof HttpEntityEnclosingRequest) {
            final HttpEntityEnclosingRequest request = (HttpEntityEnclosingRequest) req;

            HttpEntity en = request.getEntity();
            Charset charSet = ContentType.getOrDefault(en).getCharset();
            return null;
        }

        return null;
    }

    public String getContentType()
    {
        Header ct = req.getFirstHeader("Content-Type");
        return ct==null ? null : ct.getValue();
    }

    public int getContentLength()
    {
        Header cl = req.getFirstHeader("Content-Length");
        return cl==null ? -1 : Integer.parseInt(cl.getValue());
    }

    public InputStream getInputStream()
        throws IOException
    {

        final HttpEntityEnclosingRequest request = (HttpEntityEnclosingRequest) req;

        HttpEntity en = request.getEntity();
        final InputStream istr = en.getContent();
        return istr;
    }
}
