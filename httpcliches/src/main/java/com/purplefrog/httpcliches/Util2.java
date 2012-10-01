package com.purplefrog.httpcliches;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

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

        final String pathLower = target.getPath().toLowerCase();
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
        } else if (pathLower.endsWith(".txt")) {
            return "text/plain";
        } else {
            if (true)
                return URLConnection.guessContentTypeFromName(target.getName());
            return "application/binary";
        }
    }
}