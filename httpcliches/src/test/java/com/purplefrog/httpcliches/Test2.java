package com.purplefrog.httpcliches;

import java.util.*;

import junit.framework.*;
import org.stringtemplate.v4.*;

/**
 * <p>Copyright (C) 2012 Robert Forsman, Purple Frog Software
 * $Author thoth $
 * $Date 9/7/12 $
 */
public class Test2
    extends TestCase
{

    public void test1()
    {

        if (false) {
            ST st = new ST(HTMLEnabledObject.makeSTGroup(false, '$', '$'), "a$b$c$d$e");

            st.add("b", new HTMLEnabledObject("x&<y>"));
            st.add("d", "_");

            assertEquals("ax&<y>c_e", st.render());
        }

        {
            ST st = new ST(HTMLEnabledObject.makeSTGroup(false, '$', '$'), "a$b.html$c$b$e");

            st.add("b", new HTMLEnabledObject("x&<y>"));


            assertEquals("ax&amp;&lt;y&gt;cx&<y>e", st.render());
        }

        {
            ST st = new ST(HTMLEnabledObject.makeSTGroup(true, '$', '$'), "a$b.html$c$b$e");

            st.add("b", "x&<y>");


            assertEquals("ax&amp;&lt;y&gt;cx&<y>e", st.render());
        }
    }

    public void test2()
    {
        ST st = new ST(HTMLEnabledObject.makeSTGroup(true, '$', '$'), "$x$\n" +
            "$x.html$\n" +
            "$x.a$\n" +
            "$x.a.html$\n" +
            "$x.b$\n" +
            "$x.b.html$\n" +
            "$y.bacon$\n" +
            "$y.bacon.html$\n" +
            "$y.values$\n" +
            "$y.values.html$\n" +
            "q\n" +
            "$y.values:{v|$v.html$\n}$");

        st.add("x", new Exp("i<j", "k>j"));

        TreeMap<Object, Object> map = new TreeMap<Object, Object>();
        map.put("bacon", "tasty&delicious");
        st.add("y", map);

        String render = st.render();
        render = render.replaceAll("\r\n", "\n");
        assertEquals("i<j&k>j\n"+
            "i&lt;j&amp;k&gt;j\n"+
            "i<j\n"+
            "i&lt;j\n"+
            "k>j\n"+
            "k&gt;j\n"+
            "tasty&delicious\n"+
            "tasty&amp;delicious\n"+
            "tasty&delicious\n"+
            "[tasty&amp;delicious]\n"+
            "q\n"+
            "tasty&amp;delicious\n" , render);
    }


    public static class Exp
    {
        public Object a;
        public Object b;

        public Exp(Object a, Object b)
        {
            this.a = a;
            this.b = b;
        }


        public String toString()
        {
            return a+"&"+b;
        }
    }

}
