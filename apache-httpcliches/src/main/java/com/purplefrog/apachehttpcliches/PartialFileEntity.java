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
    protected final ByteRangeSpec[] brss;
    private TransferCallback callback;
    protected String underlyingMIME;

    protected byte[][] subHeaders=null;
    protected String boundary=null;

    public PartialFileEntity(File f, ByteRangeSpec brs, ContentType contentType)
    {
        this(f, brs, contentType, null);
    }

    public PartialFileEntity(File f, ByteRangeSpec brs, ContentType contentType, TransferCallback callback)
    {
        this(f, brs, contentType.toString(), callback);
    }

    public PartialFileEntity(File f, ByteRangeSpec[] brss, ContentType contentType, TransferCallback callback)
        throws IOException
    {
        this(f, brss, contentType.toString(), callback);
    }

    public PartialFileEntity(File f, ByteRangeSpec brs, String contentType)
    {
        this(f, brs, contentType, null);
    }

    public PartialFileEntity(File f, ByteRangeSpec brs, String contentType, TransferCallback callback)
    {
        this.f = f;
        this.brss = new ByteRangeSpec[]{brs};
        this.callback = callback;
        underlyingMIME = contentType;

        setContentType(contentType);
    }

    public PartialFileEntity(File f, ByteRangeSpec[] brss, String contentType, TransferCallback callback)
        throws IOException
    {
        this.f = f;
        this.brss = brss;
        this.callback = callback;
        underlyingMIME = contentType;
        if (brss.length==1)
            setContentType(contentType);
        else {
            boundary = MultipartBoundaryPicker.chooseBoundary(brss, f);
            setContentType("multipart/byteranges; boundary="+boundary);
        }
    }

    /**
     * android lacks the ContentType class, and its compilers just can't handle linking against libraries where they're forced to think about it.
     * @return
     */
    public static PartialFileEntity forAndroid(File f, ByteRangeSpec brs, String contentType)
    {
        return new PartialFileEntity(f, brs, contentType, null);
    }

    /**
     * android lacks the ContentType class, and its compilers just can't handle linking against libraries where they're forced to think about it.
     * @return
     */
    public static PartialFileEntity forAndroid(File f, ByteRangeSpec brs, String contentType, TransferCallback callback)
    {
        return new PartialFileEntity(f, brs, contentType, callback);
    }

    @Override
    public boolean isRepeatable()
    {
        return true;
    }

    @Override
    public long getContentLength()
    {
        if (1==brss.length)
            return brss[0].length(f.length());

        byte[][] subHeaders = getSubHeaders();
        long sum=0;
        for (byte[] subHeader : subHeaders) {
            sum += subHeader.length;
        }
        for (ByteRangeSpec br : brss) {
            sum += br.length(f.length());
        }
        return sum;
    }

    public byte[][] getSubHeaders()
    {
        if (subHeaders==null)
            subHeaders = fabricateSubHeaders();
        return subHeaders;
    }

    public byte[][] fabricateSubHeaders()
    {
        long fileLength = f.length();
        byte[][] rval = new byte[brss.length+1][];
        for (int i=0; i<brss.length; i++) {

            String tmp = "--"+boundary+"\r\n"
                         +"Content-Type: "+underlyingMIME+"\r\n"
                         +"Content-Range: "+brss[i].asContentRangeHeader(fileLength)+"\r\n" +
                         "\r\n";
            if (i>0)
                tmp = "\r\n"+tmp;
            rval[i] = tmp.getBytes();
        }
        rval[brss.length] = ("--"+boundary+"--\r\n").getBytes();
        return rval;
    }

    @Override
    public InputStream getContent()
        throws IOException, IllegalStateException
    {
        logger.debug("getContent()");
        if (1==brss.length) {
            InputStream rval = new FileInputStream(f);
            long remaining = brss[0].start;
            while (remaining>0) {
                long n = rval.skip(brss[0].start);
                remaining -= n;
            }
            return rval;
        } else {
            return new MultipartStream();
        }
    }

    @Override
    public void writeTo(OutputStream outputStream)
        throws IOException
    {
        logger.debug("writeTo()");
        InputStream istr = getContent();

        try {

            long remaining = getContentLength();

            if (null != callback)
                try {
                    callback.logContentLength(remaining);
                } catch (Exception e) {
                    logger.warn("transfer callback malfunctioned", e);
                }

            byte[] buffer = new byte[getBufferSize()];
            long now = System.currentTimeMillis();
            while (remaining >0) {
                int n = istr.read(buffer, 0, (int)Math.min(buffer.length, remaining));
                long n1 = System.currentTimeMillis();
                readMillisAccum += n1-now; now = n1;
                if (n<0) {
                    throw new EOFException("unexpected EOF on file");
                }
                outputStream.write(buffer, 0, n);

                if (null !=callback)
                    try {
                        callback.logBytesWritten(n);
                    } catch (Exception e) {
                        logger.warn("transfer callback malfunctioned", e);
                    }

                n1 = System.currentTimeMillis();
                writeMillisAccum += n1-now; now = n1;

                remaining -= n;
                logRemaining(remaining);

            }

            outputStream.flush();

            if (null != callback)
                try {
                    callback.logCompleted();
                } catch (Exception e) {
                    logger.warn("transfer callback malfunctioned", e);
                }

        } finally {
            if (null != callback)
                try {
                    callback.logEnd();
                } catch (Exception e) {
                    logger.warn("transfer callback malfunctioned", e);
                }

            istr.close();
        }
    }

    public int getBufferSize()
    {
        return 64<<10;
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


    public Header[] extraHeaders()
    {
        if (brss.length==1) {
            String val = brss[0].asContentRangeHeader(f.length());
            BasicHeader contentRange = new BasicHeader("Content-Range", val);
            return new Header[]{
                contentRange,
                new BasicHeader("Accept-Ranges", "bytes")
            };
        } else {
            return new Header[] {
                new BasicHeader("Accept-Ranges", "bytes")
            };
        }
    }

    public String getBoundary()
    {
        return boundary;
    }


    public class MultipartStream
        extends InputStream
    {
        int partIndex=0;
        long partCursor=0;

        @Override
        public int read(byte[] bytes, int pos, int toRead)
            throws IOException
        {
            if (partIndex>brss.length*2)
                return -1;

            byte[][]subHeaders = getSubHeaders();
            int rval=0;
            RandomAccessFile raf=null;
            try {
                while (toRead>0 && partIndex<=brss.length*2) {
                    if (partIndex%2==0) {
                        byte[] shi = subHeaders[partIndex/2];
                        int tr2 = (int) Math.min(toRead, shi.length-partCursor);
                        System.arraycopy(shi, (int) partCursor, bytes, pos, tr2);
                        partCursor += tr2;
                        pos += tr2;
                        toRead -= tr2;
                        rval += tr2;

                        if (partCursor >= shi.length) {
                            partIndex++;
                            partCursor=0;
                        }
                    } else {
                        if (raf==null)
                            raf = new RandomAccessFile(f, "r");

                        ByteRangeSpec bRange = brss[partIndex/2];
                        long brLen = bRange.length(raf.length());
                        raf.seek(bRange.start+partCursor);
                        int tr2 = (int)Math.min(toRead, brLen-partCursor);
                        int n = raf.read(bytes, pos, tr2);

                        partCursor += n;
                        pos += n;
                        rval += n;
                        toRead -= n;

                        if (partCursor>=brLen) {
                            partIndex++;
                            partCursor=0;
                        }
                    }
                }
            } finally {
                if (raf!=null)
                    raf.close();
            }

            return rval;
        }

        @Override
        public int read()
            throws IOException
        {
            byte[] tmp = new byte[1];
            int n = read(tmp);
            if (n!=1)
                return -1;
            else
                return tmp[0];
        }
    }
}
