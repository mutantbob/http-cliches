package com.purplefrog.apachehttpcliches;

import com.purplefrog.httpcliches.*;
import org.apache.http.*;
import org.apache.http.entity.*;
import org.apache.http.message.*;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: thoth
 * Date: 5/15/13
 * Time: 12:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class PartialFileEntity
    extends AbstractHttpEntity
{
    private final File f;
    private final ByteRangeSpec brs;

    public PartialFileEntity(File f, ByteRangeSpec brs, ContentType contentType)
    {
        this.f = f;
        this.brs = brs;
        setContentType(contentType.toString());
    }

    public PartialFileEntity(File f, ByteRangeSpec brs, String contentType)
    {
        this.f = f;
        this.brs = brs;
        setContentType(contentType);
    }

    @Override
    public boolean isRepeatable()
    {
        return true;
    }

    @Override
    public long getContentLength()
    {
        return brs.length();
    }

    @Override
    public InputStream getContent()
        throws IOException, IllegalStateException
    {
        InputStream rval =new FileInputStream(f);
        long remaining = brs.start;
        while (remaining >0) {
            long n = rval.skip(brs.start);
            remaining -= n;
        }
        return rval;
    }

    @Override
    public void writeTo(OutputStream outputStream)
        throws IOException
    {
        InputStream istr = getContent();

        try {

            long remaining = brs.length();
            byte[] buffer = new byte[16<<10];
            while (remaining >0) {
                int n = istr.read(buffer, 0, (int)Math.min(buffer.length, remaining));
                if (n<0) {
                    throw new EOFException("unexpected EOF on file");
                }
                outputStream.write(buffer, 0, n);

                remaining -= n;
            }

            outputStream.flush();

        } finally {
            istr.close();
        }
    }

    @Override
    public boolean isStreaming()
    {
        return false;
    }

}
