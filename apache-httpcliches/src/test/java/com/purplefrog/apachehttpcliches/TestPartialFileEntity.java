package com.purplefrog.apachehttpcliches;

import java.io.*;

import com.purplefrog.httpcliches.*;
import junit.framework.*;

/**
 * <p>Copyright (C) 2018 Robert Forsman, Ericsson SATV
 * $Author thoth $
 * $Date 6/12/18 $
 */
public class TestPartialFileEntity
    extends TestCase
{
    public void test1()
        throws IOException
    {
        File f = new File("/tmp/pfe.markov.bin");
        RandomAccessFile raf = new RandomAccessFile(f, "rw");

        int mbSize = MultipartBoundaryPicker.MARKOV_BUFFER_SIZE;

        for (int i = mbSize-10; i<mbSize+10; i++) {
            makeTestPattern1(raf, i, 4*mbSize);

            int[] markov = new int[256];

            MultipartBoundaryPicker.updateMarkovForBoundary("a".getBytes(), markov, raf, new ByteRangeSpec(0, 9000L));

            assertEquals("malfunction for "+i, 1, markov['b']);

        }
    }

    public void test2()
        throws IOException
    {
        File f = new File("/tmp/pfe.markov.bin");
        RandomAccessFile raf = new RandomAccessFile(f, "rw");

        int mbSize = MultipartBoundaryPicker.MARKOV_BUFFER_SIZE;

        for (int i = 2*mbSize-10; i<2*mbSize+10; i++) {
            makeTestPattern1(raf, i, 4*mbSize);

            int[] markov = new int[256];

            MultipartBoundaryPicker.updateMarkovForBoundary("a".getBytes(), markov, raf, new ByteRangeSpec(0, 9000L));

            assertEquals("malfunction for "+i, 1, markov['b']);

        }
    }

    /**
     * create a file of the form c*abc* where the first string of cs is count long
     */
    public static void makeTestPattern1(RandomAccessFile raf, int count, int maxSize)
        throws IOException
    {
        raf.seek(0);

        for (int j = 0; j<count; j++) {
            raf.write((byte) 'c');
        }
        raf.write((byte) 'a');
        raf.write((byte) 'b');
        while (raf.getFilePointer()<maxSize)
            raf.write((byte) 'c');
    }
}
