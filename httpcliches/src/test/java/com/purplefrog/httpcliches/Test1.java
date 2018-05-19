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
        Map<String, List<Object>> cgi = new TreeMap<String, List<Object>>();

        Annotation[] a = Test1.class.getMethod("cgifake1", Map.class).getParameterAnnotations()[0];

        Object x = CGIWebMethod.transform(Map.class, a, new CGIEnvironment(cgi), "placeholder");
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

        // booleans are a special case.  The checkboxes in HTML, if unchecked, just don't appear in the parameter list
        // So if we provide no arguments, I still expect a False.
        assertEquals(Boolean.FALSE, checkTransform(a, boolean.class, "b"));
    }

    public static Object checkTransform(Annotation[] a, Class<?> parameterType, String parameterName, String... values)
        throws CGIWebMethod.CGISOAPTransformException
    {
        Map<String, List<Object>> cgi = new TreeMap<String, List<Object>>();

        if (0<values.length)
            cgi.put(parameterName, Arrays.asList((Object[])values));

        return CGIWebMethod.transform(parameterType, a, new CGIEnvironment(cgi), "placeholder");
    }

    //

    public static void cgifake3(@WebParam(name = "sa") String[] sa1)
    {

    }

    public void test5()
        throws NoSuchMethodException, CGIWebMethod.CGISOAPTransformException
    {

        Map<String, List<Object>> cgi = new TreeMap<String, List<Object>>();

        Annotation[] a = Test1.class.getMethod("cgifake3", String[].class).getParameterAnnotations()[0];

        Object o = CGIWebMethod.transform(String[].class, a, new CGIEnvironment(cgi), "placeholder");

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
        Annotation[] a = Test1.class.getMethod("cgifake4", Integer.class).getParameterAnnotations()[0];

        assertNull(checkTransform(a, Integer.class, "i", ""));

        assertEquals(7, checkTransform(a, Integer.class, "i", "7"));

        assertEquals(-42, checkTransform(a, Integer.class, "i", "-42"));
    }

    //

    public static void cgifake5(@WebParam(name = "l") long la1)
    {

    }

    public void test7()
        throws NoSuchMethodException, CGIWebMethod.CGISOAPTransformException
    {
        Annotation[] a = Test1.class.getMethod("cgifake5", long.class).getParameterAnnotations()[0];

        assertEquals(7L, checkTransform(a, long.class, "l", "7"));

        assertEquals(42L, checkTransform(a, long.class, "l", "42"));

        assertEquals(-99L, checkTransform(a, long.class, "l", "-99"));

        assertEquals(9000000000L, checkTransform(a, long.class, "l", "9000000000"));


        try {
            checkTransform(a, long.class, "wrong", "9000000000");
            fail();
        } catch (CGIWebMethod.CGISOAPTransformException e) {
            assertTrue("threw the right exception", true);
        }
    }

    //

    public static void cgifake8(@WebParam(name = "l") Long la1)
    {

    }

    public void test8()
        throws NoSuchMethodException, CGIWebMethod.CGISOAPTransformException
    {
        Annotation[] a = Test1.class.getMethod("cgifake8", Long.class).getParameterAnnotations()[0];

        assertEquals(7L, checkTransform(a, Long.class, "l", "7"));

        assertEquals(42L, checkTransform(a, Long.class, "l", "42"));

        assertEquals(-99L, checkTransform(a, Long.class, "l", "-99"));

        assertEquals(9000000000L, checkTransform(a, Long.class, "l", "9000000000"));

        assertNull(checkTransform(a, Long.class, "wrong", ""));
        assertNull(checkTransform(a, Long.class, "wrong", "9000000000"));

    }

    //

    public static void cgifake9(@WebParam(name = "l") Double da1)
    {

    }

    public void test9()
        throws NoSuchMethodException, CGIWebMethod.CGISOAPTransformException
    {
        Annotation[] a = Test1.class.getMethod("cgifake9", Double.class).getParameterAnnotations()[0];

        assertEquals(7.0, checkTransform(a, Double.class, "l", "7"));

        assertEquals(42.0, checkTransform(a, Double.class, "l", "42"));

        assertEquals(-99.3, checkTransform(a, Double.class, "l", "-99.3"));

        assertEquals(9000000000.0, checkTransform(a, Double.class, "l", "9000000000"));

        assertNull(checkTransform(a, Long.class, "wrong", ""));
        assertNull(checkTransform(a, Long.class, "wrong", "9000000000"));

    }

    //

    public static void cgifake10(@WebParam(name="windows")int[] windows)
    {

    }

    public void test10()
        throws NoSuchMethodException, CGIWebMethod.CGISOAPTransformException
    {
        Annotation[] a = Test1.class.getMethod("cgifake10", int[].class).getParameterAnnotations()[0];

        org.junit.Assert.assertArrayEquals(new int[]{1,10,60}, (int[])checkTransform(a, int[].class, "windows", "1", "10", "60"));

        org.junit.Assert.assertArrayEquals(new int[0], (int[]) checkTransform(a, int[].class, "windows"));
    }

    //

    public static void cgifake11(@WebParam(name="weights")double[] weights)
    {

    }

    public void test11()
        throws CGIWebMethod.CGISOAPTransformException, NoSuchMethodException
    {
        Annotation[] a = Test1.class.getMethod("cgifake11", double[].class).getParameterAnnotations()[0];

        double[] got=(double[]) checkTransform(a, double[].class, "weights", "77", "", "1.2", "-4.0", "", "");
        assertEquals(3, got.length);
        assertEquals(77.0, got[0]);
        assertEquals(1.2, got[1]);
        assertEquals(-4.0, got[2]);
//        org.junit.Assert.assertArrayEquals(new double[]{77, 1.2, -4}, got);
    }
    //

    public static void cgifake12(@WebParam(name="flags")boolean[] flags)
    {

    }

    public void test12()
        throws CGIWebMethod.CGISOAPTransformException, NoSuchMethodException
    {
        Annotation[] a = Test1.class.getMethod("cgifake12", boolean[].class).getParameterAnnotations()[0];

        boolean[] got=(boolean[]) checkTransform(a, boolean[].class, "flags", "1", "0", "true", "false");
        assertEquals(4, got.length);
        assertEquals(true, got[0]);
        assertEquals(false, got[1]);
        assertEquals(true, got[2]);
        assertEquals(false, got[3]);
//        org.junit.Assert.assertArrayEquals(new double[]{77, 1.2, -4}, got);
    }

    static enum Bacon
    {
        tasty, delicious
    }

    public static void cgifake13(@WebParam(name="bacon")Bacon bacon)
    {

    }

    public void test13()
        throws CGIWebMethod.CGISOAPTransformException, NoSuchMethodException
    {
        Annotation[] a = Test1.class.getMethod("cgifake13", Bacon.class).getParameterAnnotations()[0];

        Bacon got=(Bacon) checkTransform(a, Bacon.class, "bacon", "tasty");
        assertEquals(Bacon.tasty, got);
    }
}
