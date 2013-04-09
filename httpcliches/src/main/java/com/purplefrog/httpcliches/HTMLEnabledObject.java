package com.purplefrog.httpcliches;

import org.stringtemplate.v4.*;
import org.stringtemplate.v4.misc.*;

/**
 * This class is a marker object designed for use with the {@link MagicAdaptor} {@link ModelAdaptor}.
 * That adaptor adds an "html" property to objects which is a version of the objects {@link Object#toString()} method quoted for use in HTML.
 *
 * <p>Copyright (C) 2012 Robert Forsman, Purple Frog Software
 * $Author thoth $
 * $Date 8/29/12 $
 */
public class HTMLEnabledObject
{
    private final Object base;

    public HTMLEnabledObject(Object base)
    {
        this.base = base;
    }

    @Override
    public String toString()
    {
        return base.toString();
    }

    /**
     * returns an STGroup with a {@link MagicAdaptor} pre-loaded with the HTML magic.
     * If crazyGeneral is true, then this model is registered for all classes (Object and its derivatives).
     * If crazyGeneral is false then this model is only registered for objects derived from {@link HTMLEnabledObject}.
     * @param crazyGeneral should we enable ".html" suffixes for all objects?  If false, only enable for objects derived from HTMLEnabledObject.
     * @param delimiterStartChar popular choices are '<' and '$'
     * @param delimiterStopChar popular choices are '>' and '$'
     * @return
     */
    public static STGroup makeSTGroup(boolean crazyGeneral, char delimiterStartChar, char delimiterStopChar)
    {
        STGroup g = new STGroup(delimiterStartChar, delimiterStopChar);
        g.registerModelAdaptor(crazyGeneral ? Object.class : HTMLEnabledObject.class,
            new MagicAdaptor());
        return g;
    }

    public static class MagicAdaptor
        implements ModelAdaptor
    {
        public Object getProperty(Interpreter interp, ST self, Object o, Object property, String propertyName)
            throws STNoSuchPropertyException
        {
            if ("html".equals(propertyName)) {
                return HTMLTools.escapeHTML(o.toString());
            } else {
                if (o instanceof HTMLEnabledObject)
                    o = ((HTMLEnabledObject)o).base;
                return new ObjectModelAdaptor().getProperty(interp, self, o, property, propertyName);
            }
        }
    }
}
