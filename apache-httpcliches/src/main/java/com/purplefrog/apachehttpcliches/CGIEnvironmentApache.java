package com.purplefrog.apachehttpcliches;

import java.util.*;

import com.purplefrog.httpcliches.*;
import org.apache.http.*;
import org.apache.http.protocol.*;

/**
 * <p>Copyright (C) 2017 Robert Forsman, Ericsson SATV
 * $Author thoth $
 * $Date 5/12/17 $
 */
public class CGIEnvironmentApache
    extends CGIEnvironment
{
    public HttpRequest request;
    public HttpContext context;

    public CGIEnvironmentApache(Map<String, List<String>> args, HttpRequest req, HttpContext context)
    {
        super(args);
        this.request = req;
        this.context =context;
    }
}
