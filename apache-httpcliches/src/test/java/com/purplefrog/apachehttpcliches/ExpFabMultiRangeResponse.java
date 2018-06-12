package com.purplefrog.apachehttpcliches;

import java.io.*;

import com.purplefrog.httpcliches.*;
import org.apache.http.*;

/**
 * <p>Copyright (C) 2018 Robert Forsman, Ericsson SATV
 * $Author thoth $
 * $Date 6/12/18 $
 */
public class ExpFabMultiRangeResponse
{
    public static void main(String[] argv)
        throws IOException
    {
        File f = new File("/homes5/thoth/charles/httyd-4000.ts");
        PartialFileEntity en = new PartialFileEntity(f, new ByteRangeSpec[]{
            new ByteRangeSpec(1000, 4000L),
            new ByteRangeSpec(13000, 19000L)
        }, "video/mpegts", null);

        System.out.println("Content-Length: "+en.getContentLength());
        Header ct = en.getContentType();
        System.out.println(ct.getName()+": "+ct.getValue());

        Header[] hdrs = en.extraHeaders();
        for (Header hdr : hdrs) {
            System.out.println(hdr.getName()+": "+hdr.getValue());
        }
        System.out.println();

        en.writeTo(new FileOutputStream("/tmp/multirange.body"));

    }

    public static class Two
    {
        public static void main(String[] argv)
            throws IOException
        {

            File f = new File("/homes5/thoth/charles/httyd-4000.ts");
            PartialFileEntity en = new PartialFileEntity(f, new ByteRangeSpec[]{
                new ByteRangeSpec(1000, 4000L),
            }, "video/mpegts", null);

            System.out.println("Content-Length: "+en.getContentLength());
            Header ct = en.getContentType();
            System.out.println(ct.getName()+": "+ct.getValue());

            Header[] hdrs = en.extraHeaders();
            for (Header hdr : hdrs) {
                System.out.println(hdr.getName()+": "+hdr.getValue());
            }
            System.out.println();

            en.writeTo(new FileOutputStream("/tmp/multirange.body"));

        }
    }
}
