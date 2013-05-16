package com.purplefrog.apachehttpcliches;

import com.purplefrog.httpcliches.*;
import org.apache.http.*;
import org.apache.http.entity.*;
import org.apache.http.message.*;
import org.apache.log4j.*;

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
    private static final Logger logger = Logger.getLogger(PartialFileEntity.class);


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

    /**
     * android lacks the ContentType class, and its compilers just can't handle linking against libraries where they're forced to think about it.
     * @return
     */
    public static PartialFileEntity forAndroid(File f, ByteRangeSpec brs, String contentType)
    {
        return new PartialFileEntity(f, brs, contentType);
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
            byte[] buffer = new byte[64<<10];
            long now = System.currentTimeMillis();
            while (remaining >0) {
                int n = istr.read(buffer, 0, (int)Math.min(buffer.length, remaining));
                long n1 = System.currentTimeMillis();
                readMillisAccum += n1-now; now = n1;
                if (n<0) {
                    throw new EOFException("unexpected EOF on file");
                }
                outputStream.write(buffer, 0, n);
                n1 = System.currentTimeMillis();
                writeMillisAccum += n1-now; now = n1;

                remaining -= n;
                logRemaining(remaining);

                now = System.currentTimeMillis();
            }

            outputStream.flush();

        } finally {
            istr.close();
        }
    }

    long lastLogged = 0;
    private long readMillisAccum=0;
    long writeMillisAccum=0;

    private void logRemaining(long remaining)
    {
        if (true)
            return; // suppress logging

        long now = System.currentTimeMillis();
        if (now < lastLogged+1000) {
            return;
        }

        logger.debug("transfer remaining: "+remaining);
        logger.debug("R/W balance = "+readMillisAccum+"/"+writeMillisAccum);
        lastLogged = now;
        readMillisAccum = 0;
        writeMillisAccum = 0;
    }

    @Override
    public boolean isStreaming()
    {
        return false;
    }

}
