package com.purplefrog.httpcliches;

import java.lang.annotation.*;
import java.util.*;

import javax.jws.*;

import junit.framework.*;

/**
 * <p>Copyright (C) 2012 Robert Forsman, Purple Frog Software
 * $Author thoth $
 * $Date 9/8/11 $
 */
public class Test1
    extends TestCase
{
    public void test1()
    {
        assertEquals("bob rules", HTMLTools.decodeEntities("bob rules"));
        assertEquals(" & < >  ", HTMLTools.decodeEntities(" &amp; &lt; &gt; &#32;"));

        assertEquals(" &fail ", HTMLTools.decodeEntities(" &fail "));

        assertEquals("<![CDATA[bob]]>", HTMLTools.toCDATA("bob"));
        assertEquals("<![CDATA[]]]]]><![CDATA[]>>>>]]>", HTMLTools.toCDATA("]]]]>>>>"));
    }

    public static void cgifake1(@WebParam Map<String,List<String>> cgi)
    {

    }

    public static void cgifake2(@WebParam(name = "b") Boolean b1)
    {

    }

    public void test2()
        throws CGIWebMethod.CGISOAPTransformException, NoSuchMethodException
    {
        Map<String, List<String>> cgi = new TreeMap<String, List<String>>();

        Annotation[] a = Test1.class.getMethod("cgifake1", Map.class).getParameterAnnotations()[0];

        Object x = CGIWebMethod.transform(Map.class, a, new CGIEnvironment(cgi));
        assertEquals(cgi, x);
    }

    public static void cgifake2b(@WebParam(name = "b") boolean b1)
    {

    }

    public void test3()
        throws NoSuchMethodException, CGIWebMethod.CGISOAPTransformException
    {

        Annotation[] a = Test1.class.getMethod("cgifake2", Boolean.class).getParameterAnnotations()[0];

        assertEquals(Boolean.TRUE, checkTransform(a, Boolean.class, "b", "on"));

        assertEquals(Boolean.TRUE, checkTransform(a, Boolean.class, "b", "true"));
        assertEquals(Boolean.TRUE, checkTransform(a, Boolean.class, "b", "1"));

        assertEquals(Boolean.FALSE, checkTransform(a, Boolean.class, "b", "off"));
        assertEquals(Boolean.FALSE, checkTransform(a, Boolean.class, "b", "false"));
        assertEquals(Boolean.FALSE, checkTransform(a, Boolean.class, "b", "0"));

        assertNull(checkTransform(a, Boolean.class, "blargh", "0"));

    }

    public void test4()
        throws NoSuchMethodException, CGIWebMethod.CGISOAPTransformException
    {

        Annotation[] a = Test1.class.getMethod("cgifake2b", boolean.class).getParameterAnnotations()[0];

        assertEquals(Boolean.TRUE, checkTransform(a, boolean.class, "b", "on"));

        assertEquals(Boolean.TRUE, checkTransform(a, boolean.class, "b", "true"));
        assertEquals(Boolean.TRUE, checkTransform(a, boolean.class, "b", "1"));

        assertEquals(Boolean.FALSE, checkTransform(a, boolean.class, "b", "off"));
        assertEquals(Boolean.FALSE, checkTransform(a, boolean.class, "b", "false"));
        assertEquals(Boolean.FALSE, checkTransform(a, boolean.class, "b", "0"));

        try {

            checkTransform(a, boolean.class, "blargh", "0");
            fail("did not throw the exception I need");

        } catch (CGIWebMethod.CGISOAPTransformException e) {
            assertTrue("threw properly", true);

        }

    }

    public static Object checkTransform(Annotation[] a, Class<?> parameterType, String parameterName, String... values)
        throws CGIWebMethod.CGISOAPTransformException
    {
        Map<String, List<String>> cgi = new TreeMap<String, List<String>>();

        cgi.put(parameterName, Arrays.asList(values));
        return CGIWebMethod.transform(parameterType, a, new CGIEnvironment(cgi));
    }

    //

    public static void cgifake3(@WebParam(name = "sa") String[] sa1)
    {

    }

    public void test5()
        throws NoSuchMethodException, CGIWebMethod.CGISOAPTransformException
    {

        Map<String, List<String>> cgi = new TreeMap<String, List<String>>();

        Annotation[] a = Test1.class.getMethod("cgifake3", String[].class).getParameterAnnotations()[0];

        Object o = CGIWebMethod.transform(String[].class, a, new CGIEnvironment(cgi));

        if (o instanceof String[]) {
            String[] sa = (String[]) o;
            assertEquals(0, sa.length);
        } else {
            fail("not a String[]");
        }
    }

    //

    public static void cgifake4(@WebParam(name = "i") Integer i1)
    {

    }

    public void test6()
        throws NoSuchMethodException, CGIWebMethod.CGISOAPTransformException
    {

        Map<String, List<String>> cgi = new TreeMap<String, List<String>>();

        Annotation[] a = Test1.class.getMethod("cgifake4", Integer.class).getParameterAnnotations()[0];

        assertNull(checkTransform(a, Integer.class, "i", ""));

        assertEquals(7, checkTransform(a, Integer.class, "i", "7"));

        assertEquals(-42, checkTransform(a, Integer.class, "i", "-42"));

    }
}
