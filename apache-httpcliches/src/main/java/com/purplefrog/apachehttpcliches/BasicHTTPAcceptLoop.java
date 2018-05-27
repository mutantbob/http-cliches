package com.purplefrog.apachehttpcliches;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import org.apache.http.*;
import org.apache.http.impl.*;
import org.apache.http.params.*;
import org.apache.http.protocol.*;
import org.apache.log4j.*;

/**
 *
 * This class is based on an example from the Apache web site and makes it really easy to start work on your web server.
 * When it's time to complicate things you can either edit the source or override a few methods.
 *<pre>
 final HttpRequestHandlerRegistry handlers = new HttpRequestHandlerRegistry();

 int port = 8099;
 BasicHTTPAcceptLoop loop = new BasicHTTPAcceptLoop(port, handlers, Executors.newCachedThreadPool());

 handlers.register("/mystuff*", new MyAwesomeThing());

 loop.run();
</pre>
 * <p>Copyright (C) 2012 Robert Forsman, Purple Frog Software
 * $Author thoth $
 * $Date 11/29/11 $
 */
public class BasicHTTPAcceptLoop
    extends BasicHTTPSuite
    implements Runnable
{
    public final ServerSocket serversocket;

    private boolean pleaseStop = false;

    public BasicHTTPAcceptLoop(int port, HttpRequestHandlerResolver registry, Executor executor)
        throws IOException
    {
        this(new ServerSocket(port), registry, executor);
    }

    /**
     *
     * @param ss Probably {@link ServerSocket#ServerSocket(int)}; but may be the result of {@link javax.net.ssl.SSLServerSocketFactory#createServerSocket(int)}
     * @param registry
     * @param executor
     * @see com.purplefrog.httpcliches.Util2#makeSSLSocketFactory(InputStream, String, String)
     */
    public BasicHTTPAcceptLoop(ServerSocket ss, HttpRequestHandlerResolver registry, Executor executor)
    {
        super(registry, executor);
        serversocket = ss;
    }

    public void run() {
        logger.info("Listening on port " + serversocket.getLocalPort());
        try {

            while (!Thread.interrupted() && !pleaseStop) {
                // Set up HTTP connection
                Socket socket = serversocket.accept();
                DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
                logger.debug("Incoming connection from " + socket.getInetAddress().getHostAddress());
                conn.bind(socket, params);

                // Start worker thread
                Runnable t = fabricateWorker(conn);
                executor.execute(t);
            }

        } catch (InterruptedIOException ex) {
            logger.trace("oh well", ex);
        } catch (IOException e) {
            logger.warn("I/O error initialising connection thread: ", e);
        }
        logger.error("accept loop for port "+serversocket.getLocalPort()+" ending");
    }

    public void pleaseStop()
    {
        pleaseStop = true;
        try {
            serversocket.close();
        } catch (IOException e) {
            logger.warn("malfunction closing server socket", e);
        }
    }

    public SocketAddress getAddress()
    {
        return serversocket.getLocalSocketAddress();
    }
}
