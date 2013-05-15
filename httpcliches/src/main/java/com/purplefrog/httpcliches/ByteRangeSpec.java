package com.purplefrog.httpcliches;

import java.util.regex.*;

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
    public long start;
    public Long end;

    public ByteRangeSpec(long start, Long end)
    {
        this.start = start;
        this.end = end;
    }

    public static ByteRangeSpec parseRange(String rangeHdr, long contentLength)
    {
        if (null == rangeHdr)
            return null;

        Pattern p = Pattern.compile("bytes=(\\d*)-(\\d*)");
        Matcher m = p.matcher(rangeHdr);

        long start;
        Long end=null;
        if (!m.matches()) {
            return null;
        }

        Long b_=null;
        String b = m.group(2);
        if (b.length()>0) {
            b_ = Long.parseLong(b);
        }

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
}
