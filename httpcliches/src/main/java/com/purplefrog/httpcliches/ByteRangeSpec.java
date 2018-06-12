package com.purplefrog.httpcliches;

import java.util.*;
import java.util.regex.*;

import org.apache.log4j.*;

/**
 *
 * RFC 2616
 * section 14.35.1 Byte Ranges

* User: thoth
* Date: 5/15/13
* Time: 11:41 AM

*/
public class ByteRangeSpec
{
    private static final Logger logger = Logger.getLogger(ByteRangeSpec.class);

    public long start;
    public Long end;

    public ByteRangeSpec(long start, Long end)
    {
        this.start = start;
        this.end = end;
    }

    public static ByteRangeSpec[] parseMultiRange(String rangeHdr, long contentLength)
    {
        if (null == rangeHdr)
            return null;

        if (!rangeHdr.startsWith("bytes=")) {
            logger.debug("range header does not begin with 'bytes='; was "+rangeHdr);
            return null;
        }

        String rangeStrings = rangeHdr.substring(6);

        String[] parts = rangeStrings.trim().split("\\s*,\\s*");

        Pattern p = Pattern.compile("(\\d*)-(\\d*)");

        ByteRangeSpec[] rval = new ByteRangeSpec[parts.length];
        for (int i = 0; i<parts.length; i++) {
            Matcher m = p.matcher(parts[i]);
            rval[i] = matcherToBRS(m, contentLength);
            if (rval[i]==null) {
                logger.debug("bad byte range spec in multipart : "+parts[i]);
                return null;
            }
        }
        return rval;
    }

    public static ByteRangeSpec parseRange(String rangeHdr, long contentLength)
    {
        if (null == rangeHdr)
            return null;

        Pattern p = Pattern.compile("bytes=(\\d*)-(\\d*)");
        Matcher m = p.matcher(rangeHdr);

        return matcherToBRS(m, contentLength);
    }

    public static ByteRangeSpec matcherToBRS(Matcher m, long contentLength)
    {
        if (!m.matches()) {
            return null;
        }

        long start;
        Long b_=null;
        String b = m.group(2);
        if (b.length()>0) {
            b_ = Long.parseLong(b);
        }

        Long end=null;
        String a = m.group(1);
        if (a.length()>0) {
            start = Long.parseLong(a);
            end = b_;
        } else {
            if (b_==null)
                return null; // malformed "bytes=-"

            start = contentLength - b_;
            end = contentLength -1;
        }

        return new ByteRangeSpec(start, end);
    }

    /**
     * @throws NullPointerException if you didn't make the {@link #end} not null
     */
    public long length()
    {
        return end-start + 1;
    }

    public long length(long underlyingLength)
    {
        long end = this.end==null ? (underlyingLength-1) : this.end;
        return end-start + 1;
    }

    public String asContentRangeHeader(long underlyingLength)
    {
        long end_ = end==null ? (underlyingLength-1):end;
        return "bytes "+(start+"-"+end_+"/"+underlyingLength);
    }
}
