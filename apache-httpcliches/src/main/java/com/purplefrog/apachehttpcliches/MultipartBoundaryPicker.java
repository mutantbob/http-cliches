package com.purplefrog.apachehttpcliches;

import java.io.*;
import java.util.*;

import com.purplefrog.httpcliches.*;

/**
 * <p>Copyright (C) 2018 Robert Forsman, Ericsson SATV
 * $Author thoth $
 * $Date 6/12/18 $
 */
public class MultipartBoundaryPicker
{
    public static int MARKOV_BUFFER_SIZE=4<<10;
    protected static final char[] symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    public static String chooseBoundary(ByteRangeSpec[] brss, File f)
        throws IOException
    {
        StringBuilder tmp=new StringBuilder();
        Random r = new Random();
        for (int i=0; i<1; i++) {
            tmp.append(symbols[r.nextInt(symbols.length)]);
        }

        RandomAccessFile raf = new RandomAccessFile(f, "r");
        try {
            while(true) {
                byte[] boundary = tmp.toString().getBytes();

                int[] markov = new int[256];
                for (int i = 0; i<brss.length; i++) {
                    updateMarkovForBoundary(boundary, markov, raf, brss[i]);
                }
                if (!anyHits(markov))
                    break;

                tmp.append(pickLeastUsedSymbol(symbols, markov));
            }
        } finally {
            raf.close();
        }

        return tmp.toString();
    }

    public static char pickLeastUsedSymbol(char[] symbols, int[] markov)
    {
        char rval=symbols[0];
        int count=markov[0];
        if (count==0)
            return rval;
        for (int i=1; i<symbols.length; i++) {
            char sym = symbols[i];
            if (count >= markov[sym]) {
                count = markov[sym];
                rval = sym;
            }
        }
        return rval;
    }

    public static void updateMarkovForBoundary(byte[] boundary, int[] markov, RandomAccessFile raf, ByteRangeSpec brs)
        throws IOException
    {
        byte[] buffer = new byte[MARKOV_BUFFER_SIZE];
        int bytesInBuffer;

        long fileCursor = brs.start;
        long end = brs.end==null ? raf.length() :  brs.end;
        raf.seek(fileCursor);
        {
            int toRead = (int)Math.min(buffer.length , end-fileCursor+1);
            int n = raf.read(buffer, 0, toRead);
            if (n<0)
                throw new EOFException("file too short for byte range spec "+brs.asContentRangeHeader(raf.length()));
            bytesInBuffer = n;
            fileCursor+=n;
        }
        while (true) {
            for (int i=0; i+boundary.length < bytesInBuffer; i++) {
                if (matchBytesAt(boundary, buffer, i)) {
                    int b = 0xff & buffer[i+boundary.length];
                    markov[b]++;
//                    System.out.println("found at "+(i+fileCursor-bytesInBuffer));
                }
            }

            if (fileCursor > end)
                break;

            int s2 = boundary.length;
            if (bytesInBuffer>s2) {
//                System.out.println("shift "+(bytesInBuffer-s2)+" ; fileCursor="+fileCursor);
                System.arraycopy(buffer, bytesInBuffer-s2, buffer, 0, s2);
                bytesInBuffer = s2;
            }
            int toRead = (int)Math.min(buffer.length-s2, end-fileCursor+1);
            int n = raf.read(buffer, s2, toRead);
            if (n<1)
                throw new EOFException("file too short for byte range spec "+brs.asContentRangeHeader(raf.length()));
            bytesInBuffer += n;
            fileCursor += n;
        }
    }

    public static boolean matchBytesAt(byte[] a, byte[] b, int bPtr)
    {
        for (int i=0; i<a.length; i++) {
            if (a[i] != b[bPtr+i])
                return false;
        }
        return true;
    }

    public static boolean anyHits(int[] markov)
    {
        for (int count : markov) {
            if (count>0)
                return true;
        }
        return false;
    }
}
