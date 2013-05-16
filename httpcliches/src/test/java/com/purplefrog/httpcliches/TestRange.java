package com.purplefrog.httpcliches;

import junit.framework.*;

/**
 * <p>Copyright (C) 2013 Robert Forsman, Ericsson SATV
 * $Author thoth $
 * $Date 1/4/13 $
 */
public class TestRange
    extends TestCase
{
    public void test1()
    {
        Long[] x = Util2.parseRangeHeader("bytes=0-499");
        assertEquals(0L, (long)x[0]);
        assertEquals(499L, (long)x[1]);

        checkRangeInterpretation(0, 499, x, 1000);
    }

    public void test2()
    {

        Long[] x = Util2.parseRangeHeader("bytes=500-999");
        assertEquals(500L, (long)x[0]);
        assertEquals(999L, (long)x[1]);

        checkRangeInterpretation(500, 999, x, 1000);
    }

    public void test3()
    {

        Long[] x = Util2.parseRangeHeader("bytes=-500");
        assertEquals(-500L, (long)x[0]);
        assertNull( x[1]);

        checkRangeInterpretation(500, 999, x, 1000);
        checkRangeInterpretation(300, 799, x, 800);
    }

    public void test4()
    {

        Long[] x = Util2.parseRangeHeader("bytes=9500-");
        assertEquals(9500L, (long)x[0]);
        assertNull( x[1]);

        checkRangeInterpretation(9500, 9999, x, 10000);
    }

    public void test5()
    {
        assertNull(Util2.parseRangeHeader(null));
    }

    public static void checkRangeInterpretation(int expectedStart, int expectedEnd, Long[] x, int entityLength)
    {
        assertEquals(expectedStart, Util2.computeStartForRange(x, entityLength));
        assertEquals(expectedEnd, Util2.computeEndForRange(x, entityLength));
    }
}
