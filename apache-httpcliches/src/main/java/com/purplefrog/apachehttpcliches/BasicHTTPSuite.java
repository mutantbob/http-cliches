package com.purplefrog.apachehttpcliches;

import org.apache.http.*;
import org.apache.http.impl.*;
import org.apache.http.params.*;
import org.apache.http.protocol.*;
import org.apache.log4j.*;

import java.util.concurrent.*;

public class BasicHTTPSuite
{
    protected static final Logger logger = Logger.getLogger(BasicHTTPAcceptLoop.class);
    public final HttpParams params;
    public final HttpService httpService;
    public final HttpRequestHandlerResolver registry;
    protected Executor executor;

    public BasicHTTPSuite(HttpRequestHandlerResolver registry, Executor executor)
    {
        params = clicheParams();
        this.registry = registry;
        this.executor = executor;

        HttpProcessor httpproc = clicheProcessor();

        // Set up the HTTP service
        httpService = new HttpService(
            httpproc,
            new DefaultConnectionReuseStrategy(),
            new DefaultHttpResponseFactory(),
            registry,
            params);
    }

    public void setReadTimeoutSeconds(int seconds)
    {
        httpService.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, seconds *1000);
    }

    public HttpProcessor clicheProcessor()
    {
        // Set up the HTTP protocol processor
        return new ImmutableHttpProcessor(new HttpResponseInterceptor[] {
            new ResponseDate(),
            new ResponseServer(),
            new ResponseContent(),
            new ResponseConnControl()
        });
    }

    public HttpParams clicheParams()
    {
        HttpParams p = new SyncBasicHttpParams();
        p
            .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 30*1000)
            .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
            .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
            .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
            .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");
        return p;
    }

    public Runnable fabricateWorker(HttpServerConnection conn)
    {
        return new HTTPWorkerThread(httpService, conn);
    }
}
