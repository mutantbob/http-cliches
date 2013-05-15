package com.purplefrog.httpcliches;

import junit.framework.*;

/**
 * Created with IntelliJ IDEA.
 * User: thoth
 * Date: 5/15/13
 * Time: 11:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestByteRangeSpec
    extends TestCase
{

    protected final int M = 1000000;

    public void test1()
    {
        ByteRangeSpec brs1 = ByteRangeSpec.parseRange("bytes=0-499", M);

        assertEquals( 0, brs1.start);
        assertEquals( 499, brs1.end.longValue());
        assertEquals(500, brs1.length());
    }

    public void test2()
    {
        ByteRangeSpec brs1 = ByteRangeSpec.parseRange("bytes=500-999", M);

        assertEquals( 500, brs1.start);
        assertEquals( 999, brs1.end.longValue());
        assertEquals(500, brs1.length());

    }

    public void test3()
    {
        ByteRangeSpec brs1 = ByteRangeSpec.parseRange("bytes=-500", M);
        assertEquals(M-500, brs1.start);
        assertEquals(M-1, brs1.end.longValue());
        assertEquals(500, brs1.length());
    }

    public void test4()
    {
        ByteRangeSpec brs1 = ByteRangeSpec.parseRange("bytes=9500-", M);
        assertEquals(9500, brs1.start);
        assertNull(brs1.end);
    }

}
