package com.purplefrog.apachehttpcliches;

import org.apache.http.impl.*;
import org.apache.http.protocol.*;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class DualHTTPAcceptLoop
    extends BasicHTTPSuite
    implements Runnable
{
    final ServerSocket ss1;
    final ServerSocket ss2;

    boolean pleaseStop;

    public DualHTTPAcceptLoop(ServerSocket ss1, ServerSocket ss2, HttpRequestHandlerRegistry registry, ExecutorService executor)
    {
        super(registry, executor);

        this.ss1 = ss1;
        this.ss2 = ss2;
    }

    public void run()
    {
        executor.execute(()->acceptLoop(ss2));
        acceptLoop(ss1);
    }

    private void acceptLoop(ServerSocket serversocket)
    {
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
            ss1.close();
        } catch (IOException e) {
            logger.warn("malfunction closing server socket 1", e);
        }
        try {
            ss2.close();
        } catch (IOException e) {
            logger.warn("malfunction closing server socket 2", e);
        }
    }

    public String getAddresses()
    {
        return ss1.getLocalSocketAddress() +" & "+ss2.getLocalSocketAddress();
    }
}
