package com.purplefrog.httpcliches;

import java.text.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.lang.reflect.*;

import org.apache.log4j.*;

/**
 * <p>Copyright (C) 2012 Robert Forsman, Purple Frog Software
 * <br>$ Author thoth $
 * <br>$ Date: Oct 11, 2010$
 */
@SuppressWarnings({"UtilityClassWithoutPrivateConstructor"})
public class HTMLTools
{
    private static final Logger logger = Logger.getLogger(HTMLTools.class);
    public static final DateFormat RFC1123FORMAT = new SimpleDateFormat("EEE, dd MMM yyyyy HH:mm:ss z", Locale.US);

    public static Map<String, List<String>> parseCGIArgs(URI uri)
    {
        return parseCGIArgs(uri.getRawQuery());
    }

    public static Map<String, List<String>> parseCGIArgs(String rawQuery)
    {
        logger.debug("parseCGIArgs('"+rawQuery+"')");
        Map<String, List<String>> cgiParameters = new TreeMap<String, List<String>>();
        if (rawQuery !=null) {
            return parseCGIQueryString(rawQuery);
        }
        return cgiParameters;
    }

    public static Map<String, List<String>> parseCGIQueryString(String queryString)
    {
        Map<String, List<String>> cgiParameters = new HashMap<String, List<String>>();

        if (queryString==null)
            return cgiParameters;
        
        for (String kv_ : queryString.split("&")) {
            String[] kv = kv_.split("=", 2);

            String key_;
            String value_;
            if (kv.length >=2 ) {
                key_ = kv[0];
                value_ = kv[1];
                if (kv.length >2 ) {
                    logger.error("excess = in CGI parameter '"+kv_+"', ignoring");
                }
            } else if (kv.length ==1) {
                value_ = key_ = kv[0];
            } else {
                logger.error("blam, kv_.length="+kv.length+"; ignoring");
                continue;
            }


            String key = unescapeCGI(key_);
            List<String> va = cgiParameters.get(key);
            if (va==null) {
                va = new ArrayList<String>(1);
                cgiParameters.put(key, va);
            }
            va.add(unescapeCGI(value_));
        }

        return cgiParameters;
    }

    public static String firstOrNull(List<String> list)
    {
        if (list ==null || list.isEmpty())
            return null;
        else
            return list.get(0);
    }

    public static String unescapeCGI(String escapedCGIText)
    {
        return decodeEntities(decodePercent(escapedCGIText.replaceAll("\\+", " ")));
    }

    public static String decodeEntities(String title)
    {
        if (title==null)
            return null;
        StringBuilder rval = new StringBuilder();
        for (int i=0; i<title.length(); i++) {
            char h = title.charAt(i);
            if (h=='&') {
                int i2 = title.indexOf(';', i+2);
                if (i2<0) {
                    // blargh
                    rval.append(h);
                } else {
                    rval.append(parseEntityName(title.substring(i+1, i2)));
                    i=i2;
                }
            } else {
                rval.append(h);
            }
        }
        return rval.toString();
    }

    public static char parseEntityName(String key)
    {
        if ("amp".equals(key))
            return '&';
        else if ("gt".equals(key)) {
            return '>';
        } else if ("lt".equals(key)) {
            return '<';
        } else if (key.charAt(0)=='#') {
            return (char)Integer.parseInt(key.substring(1));
        } else {
            return key.charAt(0);
        }
    }

    public static String decodePercent(CharSequence src)
    {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        for (int i=0; i<src.length(); i++) {
            char ch = src.charAt(i);
            if (ch == '%') {
                if (i+2<src.length()) {
                    buf.write((byte)Integer.parseInt(src.subSequence(i+1, i+3).toString(), 16));
                    i+=2;
                } else {
                    logger.warn("short % escape in CGI parameter");
                    break;
                }
            } else {
                buf.write(ch);
            }
        }

        return Util2.utf8(buf.toByteArray());
    }

    public static String percentEncodeFormValue(String raw)
    {

        try {
            byte[] buffer = raw.getBytes("UTF-8");

            StringBuilder rval = new StringBuilder();

            for (byte b : buffer) {
                char ch = (char) (0xff & b);

                if (Character.isLetterOrDigit(ch)) {
                    rval.append(ch);
                } else if ('-' == ch
                           || '_' == ch
                           || '.' == ch
                           || '~' == ch) {
                    rval.append(ch);
                } else {
                    rval.append("%" + Character.forDigit(0xf & (ch >> 4), 16) +
                                Character.forDigit(ch & 0xf, 16));
                }
            }

            return rval.toString();
        } catch (UnsupportedEncodingException e) {
            throw new UndeclaredThrowableException(e, "your JVM doesn't support UTF-8");
        }
    }

    public static String escapeHTML(CharSequence label)
    {
        StringBuilder rval = new StringBuilder();

        for (int i=0; i<label.length(); i++) {
            char ch = label.charAt(i);

            if (ch == '&') {
                rval.append("&amp;");
            } else if (ch == '<') {
                rval.append("&lt;");
            } else if (ch == '>') {
                rval.append("&gt;");
            } else if (ch == '"') {
                rval.append("&quot;");
            } else {
                // there's probably stuff I didn't think of yet
                rval.append(ch);
            }
        }

        return rval.toString();
    }

    public static String escapeFormValue(CharSequence text)
    {
        if (text==null)
            return null;

        StringBuilder rval = new StringBuilder();

        for (int i=0; i<text.length(); i++) {
            char ch = text.charAt(i);

            if (ch == '&') {
                rval.append("&amp;");
            } else if (ch == '<') {
                rval.append("&lt;");
            } else if (ch == '>') {
                rval.append("&gt;");
            } else if (ch == '"') {
                rval.append("&quot;");
            } else if (ch <32) {
                rval.append("&#"+(int)ch + ";");
            } else {
                // there's probably stuff I didn't think of yet
                rval.append(ch);
            }
        }

        return rval.toString();
    }

    /**
     * I'm not entirely sure if this solves my problem,
     * partly because I'm not sure that I know what problem I have.
     */
    public static String escapeCGIParam(CharSequence text)
    {
        StringBuilder rval = new StringBuilder();

        for (int i=0; i<text.length(); i++) {
            char ch = text.charAt(i);

            if (ch == '&') {
                rval.append("&amp;");
            } else if (ch == '<') {
                rval.append("&lt;");
            } else if (ch == '>') {
                rval.append("&gt;");
            } else if (ch == '"') {
                rval.append("&quot;");
            } else if (ch <32 || ch == '=') {
                rval.append("&#"+(int)ch + ";");
            } else {
                // there's probably stuff I didn't think of yet
                rval.append(ch);
            }
        }

        return rval.toString();
    }

    public static String htmlTitleAndBody(String title, CharSequence body)
    {
        return "<html>\n"
               + "<head>\n"
               + "<title>"+ title +"</title>\n"
               + "</head>\n"
               + "<body>\n"
               +body
               +"</body>\n"
               + "</html>\n";
    }

    public static String toCDATA(String s)
    {
        StringBuilder rval = new StringBuilder();

        int cursor=0;
        while (true) {
            int idx = s.indexOf("]]>", cursor);
            if (idx<0) {
                rval.append("<![CDATA[");
                rval.append(s.substring(cursor));
                rval.append("]]>");
                break;
            }

            final int endIndex = idx + 1;
            rval.append("<![CDATA[");
            rval.append(s.substring(cursor, endIndex));
            rval.append("]]>");
            cursor = endIndex;
        }

        return rval.toString();
    }

    public static String rfc1123(long epochMilliseconds)
    {

        return RFC1123FORMAT.format(new Date(epochMilliseconds));
    }

}
