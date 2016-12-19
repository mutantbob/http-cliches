package com.purplefrog.httpcliches;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import org.apache.log4j.*;

/**
 * <p>Copyright (C) 2012 Robert Forsman, Purple Frog Software
 * <br>$ Author thoth $
 * <br>$ Date: Jul 6, 2010$
 */
public class Util2
{
    private static final Logger logger = Logger.getLogger(Util2.class);
    
    public static byte[] slurp(InputStream istr)
    {
        ByteArrayOutputStream ostr = new ByteArrayOutputStream();

        byte[] buffer = new byte[16<<10];

        try {
            while (true) {
                int n = istr.read(buffer);
                if (n<1)
                    break;
                ostr.write(buffer, 0, n);
            }
        } catch (IOException e) {
            logger.error("slurp malfunction", e);
        }

        return ostr.toByteArray();
    }

    public static String slurp2(InputStream istr)
    {
        return utf8(slurp(istr));
    }

    public static String utf8(byte[] bytes)
    {
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UndeclaredThrowableException(e, "what kind of candy-assed JVM doesn't have UTF-8?");
        }
    }

    public static <T extends Comparable<? super T>> List<T> sorted(Collection<T> items)
    {
        List<T> rval = new ArrayList<T>(items);
        Collections.sort(rval);
        return rval;
    }

    public static String slurp(Reader r)
        throws IOException
    {
        StringWriter rval = new StringWriter();
        char[] buffer = new char[4<<10];

        while (true) {
            int n = r.read(buffer);
            if (n<1)
                break;
            rval.write(buffer, 0, n);
        }

        return rval.toString();
    }

    public static String stringStackTrace(Throwable e)
    {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static String mimeTypeFor(File target)
    {
        return mimeTypeFor(target.getPath());
    }

    public static String mimeTypeFor(String path)
    {
        final String pathLower = path.toLowerCase();
        if (pathLower.endsWith(".html")) {
            return "text/html";
        } else if (pathLower.endsWith(".css")) {
            return "text/css";
        } else if (pathLower.endsWith(".js")) {
            return "application/javascript";
        } else if (pathLower.endsWith(".png")) {
            return "image/png";
        } else if (pathLower.endsWith(".mp4")) {
            return "video/mp4";
        } else if (pathLower.endsWith(".mkv")) {
            return "video/x-matroska";
        } else if (pathLower.endsWith(".txt")) {
            return "text/plain";
        } else if (pathLower.endsWith(".m3u")) {
            return "audio/x-mpegurl";
        } else if (pathLower.endsWith(".m3u8")) {
            return "application/vnd.apple.mpegurl";
        } else if (pathLower.endsWith(".ts")) {
            return "video/MP2T";
        } else if (pathLower.endsWith(".mpeg")) {
            return "video/mpeg";
        } else if (pathLower.endsWith(".svg")) {
            return "image/svg+xml";
        } else {
            if (true)
                return URLConnection.guessContentTypeFromName(path);
            return "application/binary";
        }
    }

    /**
     * RFC2616 section 14.35
     *
     * <p>
     *     "100-200" becomes new Long[] { 100, 200 }
     * <br>"0-500" becomes new Long[] { 0, 500 }
     * <br>"-500" becomes new Long[] { -500, null }
     *
     * <p> Anything we can not understand causes us to return null, indicating we should ignore this malformed header.</p>
     *
     * @param rangeHdr
     * @return
     *
     * @see #computeStartForRange(Long[], long)
     */
    public static Long[] parseRangeHeader(String rangeHdr)
    {
        if (null==rangeHdr)
            return null;

        Pattern p = Pattern.compile("bytes=([0-9]*)-([0-9]*)");
        Matcher m = p.matcher(rangeHdr);
        if (m.matches()) {
            String start_ = m.group(1);
            String end_ = m.group(2);

            Long start, end;
            if (start_.length() <1)
                start = null;
            else
                start = Long.parseLong(start_);

            if (end_.length()<1) {
                end = null;
            } else {
                end = Long.parseLong(end_);
                if (start==null) { // oddity in the parsing
                    start = -end;
                    end=null;
                }
            }

            return new Long[]{start, end};
        } else {
            return null;
        }
    }


    public static long computeStartForRange(Long[] range, long entityLength)
    {
        if (range[0]<0) {
            return entityLength +range[0];
        } else {
            return range[0];
        }
    }

    public static long computeEndForRange(Long[] range, long entityLength)
    {
        if (null==range[1])
            return entityLength-1;
        else
            return Math.min(range[1], entityLength-1);
    }

}
