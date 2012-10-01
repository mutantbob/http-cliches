package com.purplefrog.apachehttpcliches;

import java.io.*;
import java.net.*;
import org.apache.http.*;
import org.apache.http.protocol.*;
import org.apache.log4j.*;

/**
* <p>Copyright (C) 2012 Robert Forsman, Purple Frog Software
* $Author thoth $
* $Date 11/29/11 $
*/
public class HTTPWorkerThread
    implements Runnable
{
    private static final Logger logger = Logger.getLogger(HTTPWorkerThread.class);

    private final HttpService httpservice;
    private final HttpServerConnection conn;

    public HTTPWorkerThread(HttpService httpservice, HttpServerConnection conn)
    {
        this.httpservice = httpservice;
        this.conn = conn;
    }

    public void run() {
        logger.debug("New connection thread");
        HttpContext context = new BasicHttpContext(null);
        try {
            while (!Thread.interrupted() && conn.isOpen()) {
                httpservice.handleRequest(conn, context);
            }
        } catch (ConnectionClosedException ex) {
            logger.debug("Client closed connection", ex);
        } catch (SocketTimeoutException ex) {
            logger.debug("got bored of waiting for client", ex);
        } catch (IOException ex) {
            logger.warn("I/O error", ex);
        } catch (HttpException ex) {
            logger.warn("Unrecoverable HTTP protocol violation", ex);
        } finally {
            try {
                conn.shutdown();
            } catch (IOException ignore) {}
        }
    }

}
