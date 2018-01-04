package com.purplefrog.apachehttpcliches;

import org.apache.http.*;

import java.nio.charset.*;
import java.util.*;

public class AuthHelper
{
    static Base64.Decoder decoder64 = Base64.getDecoder();
    static Charset utf8 = Charset.forName("UTF-8");

    public static <T> T check(HttpRequest req, Callback<T> callback)
    {
        Header auth_ = req.getFirstHeader("Authorization");

        if (auth_==null)
            return null;

        String scheme = auth_.getValue().trim();
        if (scheme.startsWith("Basic ")) {
            String auth64 = scheme.substring(6).trim();
            String auth = new String(decoder64.decode(auth64), utf8);
            String[] parts = auth.split(":", 2);
            return callback.checkBasic(parts[0], parts[1]);
        } else {
            return callback.unknownAuth(scheme);
        }
    }

    public interface Callback<T>
    {
        T checkBasic(String user, String password);

        T unknownAuth(String scheme);
    }
}