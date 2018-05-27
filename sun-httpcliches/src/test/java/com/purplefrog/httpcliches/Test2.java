package com.purplefrog.httpcliches;

import java.lang.reflect.*;
import java.util.*;
import javax.jws.*;
import junit.framework.*;

/**
 * <p>Copyright (C) 2012 Robert Forsman, Purple Frog Software
 * $Author thoth $
 * $Date 5/14/12 $
 */
public class Test2
    extends TestCase
{

    public void test1()
        throws CGIWebMethod.CGISOAPTransformException, NoSuchMethodException
    {
        Method m = Test2.class.getMethod("bacon", String.class, int.class, Integer.class, Integer.class, CGIEnvironment.class);
        Map<String, List<Object>> cgiArgs = HTMLTools.parseCGIArgs("alpha=bob&beta=7&gamma=-40");

        CGIEnvironment ce = new CGIEnvironment(cgiArgs);
        Object[] params = CGIWebMethod.transformCGIArgumentsToJavaParams(m, ce);
        assertEquals(5, params.length);
        assertEquals("bob", params[0]);
        assertEquals(7, params[1]);
        assertEquals(-40, params[2]);
        assertEquals(null, params[3]);
        assertEquals(ce, params[4]);
    }


    @WebMethod
    public static void bacon(
        @WebParam(name="alpha")
        String a,
        @WebParam(name="beta")
        int b,
        @WebParam(name="gamma")
        Integer c,
        @WebParam(name="delta")
        Integer d,
        @WebParam
        CGIEnvironment e)
    {
        // do nothing
    }
}
