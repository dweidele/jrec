package de.unikn.ie.sna.recsys.jrec.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Util
{

    public static double sqr(double d)
    {
        return d * d;
    }

    public static List<String> tokenize(final String str, final String delimiter)
    {
        List<String> result = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(str, delimiter);
        while (st.hasMoreTokens())
        {
            result.add(st.nextToken());
        }
        return result;
    }

    public static long getusertime()
    {
        return System.currentTimeMillis();
    }

    public static int rand()
    {
        return (int) (Math.random() * Integer.MAX_VALUE);
    }

}
