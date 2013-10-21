package com.purplefrog.apachehttpcliches;

import org.apache.http.entity.*;

import java.io.*;

/**
* Created with IntelliJ IDEA.
* User: thoth
* Date: 10/21/13
* Time: 12:52 PM
* To change this template use File | Settings | File Templates.
*/
public class HeadEntity
    extends AbstractHttpEntity
{
    protected long contentLength;

    public HeadEntity(String mime, long contentLength)
    {
        setContentType(mime);
        setChunked(false);
        this.contentLength = contentLength;
    }

    public boolean isRepeatable()
    {
        return true;
    }

    public long getContentLength()
    {
        return contentLength;
    }

    public InputStream getContent()
        throws IOException, IllegalStateException
    {
        return new ByteArrayInputStream(new byte[0]);
    }

    public void writeTo(OutputStream outputStream)
        throws IOException
    {
        outputStream.close();
    }

    public boolean isStreaming()
    {
        return false;
    }
}
