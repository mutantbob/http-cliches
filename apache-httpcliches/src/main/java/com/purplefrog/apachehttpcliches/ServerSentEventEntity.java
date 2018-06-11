package com.purplefrog.apachehttpcliches;

import java.io.*;
import java.util.function.*;

import org.apache.http.*;
import org.apache.http.message.*;

/**
 * <p>Copyright (C) 2018 Robert Forsman, Purple Frog Software
 * $Author thoth $
 * $Date 6/11/18 $
 */
public class ServerSentEventEntity
    implements HttpEntity
{
    protected final Supplier<String> eventSupplier;

    public ServerSentEventEntity(Supplier<String> eventSupplier)
    {
        this.eventSupplier = eventSupplier;
    }

    public boolean isRepeatable()
    {
        return false;
    }

    public boolean isChunked()
    {
        return true;
    }

    public long getContentLength()
    {
        return -1;
    }

    public Header getContentType()
    {
        return new BasicHeader("Content-Type", "text/event-stream");
    }

    public Header getContentEncoding()
    {
        return null;
    }

    public InputStream getContent()
        throws IOException, IllegalStateException
    {
        throw new IllegalStateException();
    }

    public void writeTo(OutputStream outstream)
        throws IOException
    {
        while (true) {
            String event = eventSupplier.get();
            StringBuilder wrapped = wrapEvent(event);
            byte[] msg = wrapped.toString().getBytes();
            outstream.write(msg);
            outstream.flush();

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isStreaming()
    {
        return false;
    }

    public void consumeContent()
        throws IOException
    {

    }

    //

    public static StringBuilder wrapEvent(String event)
    {
        StringBuilder wrapped = new StringBuilder();
        int cursor = 0;
        while (true) {
            int idx = event.indexOf('\n', cursor);
            if (idx<0) {

                break;
            } else {
                wrapped.append("data:");
                wrapped.append(event.substring(cursor, idx+1));
                cursor = idx+1;
            }
        }

        if (cursor<event.length()) {
            wrapped.append("data:");
            wrapped.append(event.substring(cursor));
            wrapped.append('\n');
        }
        wrapped.append('\n');
        return wrapped;
    }
}
