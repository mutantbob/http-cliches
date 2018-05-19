package com.purplefrog.apachehttpcliches;

import java.io.*;
import java.net.*;
import java.util.*;
import com.purplefrog.httpcliches.*;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.*;
import org.apache.http.*;
import org.apache.http.impl.*;
import org.apache.http.protocol.*;
import org.apache.log4j.*;


/**
 * <p>Copyright (C) 2012 Robert Forsman, Purple Frog Software
 * $Author thoth $
 * $Date 6/25/12 $
 */
public class ApacheCGI
{
    private static final Logger logger = Logger.getLogger(ApacheCGI.class);

    public static final FileItemFactory fileItemFactory;

    static {
        File dir = new File("/tmp/httpUploads");
        dir.mkdirs();
        fileItemFactory = new DiskFileItemFactory(64 << 20, dir);
    }

    /**
     * <p> Note: In the case of a POST, this routine will consume the request body.
     * This means that a subsequent call to this same method will have nothing to read, so hang on to the results.</p>
     */
    public static Map<String, List<Object>> parseCGI(HttpRequest req)
        throws URISyntaxException, IOException, FileUploadException
    {
        String method = req.getRequestLine().getMethod();
        if ("POST".equals(method)) {
            Header ct_ = req.getFirstHeader("Content-Type");
            if (ct_ !=null ) {
                String contentType = ct_.getValue();
                String[] elements = contentType.split(";");
                if ("multipart/form-data".equals(elements[0])) {

                    FileUploadBase upload = new FileUpload(fileItemFactory);

                    List<FileItem> parts = upload.parseRequest(new ApacheMultipartContext(req));

                    Map<String, List<Object>> map = new TreeMap<>();
                    for (FileItem part : parts) {
                        Object val;
                        if (part.isFormField())
                            val = new String(part.get(), "UTF-8");
                        else
                            val = part;
                        List<Object> x = map.get(part.getFieldName());
                        if (null == x) {
                            x = new ArrayList<>();
                            map.put(part.getFieldName(), x);
                        }
                        x.add(val);
                    }
                    return map;
                }
            }

            return HTMLTools.parseCGIArgs(ApacheHTTPCliches.requestBodyAsString(req));

        } else if ("GET".equals(method)) {
            return HTMLTools.parseCGIArgs(new URI(req.getRequestLine().getUri()));
        } else {
            return null;
        }
    }

    public static CGIEnvironment parseEnv(HttpRequest req, HttpContext context)
        throws IOException, URISyntaxException, FileUploadException
    {
        CGIEnvironment rval = new CGIEnvironmentApache(parseCGI(req), req, context);
        HttpHost targetHost = ApacheHTTPCliches.httpTargetHost(context);
        rval.serverName = targetHost.getHostName();
        rval.serverPort = targetHost.getPort();
        rval.pathInfo = req.getRequestLine().getUri();

        SocketHttpServerConnection conn=ApacheHTTPCliches.remoteAddress(context);
        if (null != conn) {
            rval.remoteAddress=conn.getRemoteAddress();
            rval.remotePort = conn.getRemotePort();
            logger.info("remote address "+rval.remoteAddress.getHostAddress()+":"+rval.remotePort);
        } else {
            logger.info("remote address info null");
        }
        return rval;
    }
}
