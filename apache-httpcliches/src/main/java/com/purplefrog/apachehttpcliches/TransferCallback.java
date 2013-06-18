package com.purplefrog.apachehttpcliches;

/**
* Created with IntelliJ IDEA.
* User: thoth
* Date: 6/6/13
* Time: 12:16 PM
* To change this template use File | Settings | File Templates.
*/
public interface TransferCallback
{
    /** how many bytes we intend to transmit*/
    void logContentLength(long count);

    void logBytesWritten(int count);

    /**
     * we transmitted all the bytes we wanted to
     */
    void logCompleted();

    /**
     * the loop is finished, whether there was an exception or not
     */
    void logEnd();
}
