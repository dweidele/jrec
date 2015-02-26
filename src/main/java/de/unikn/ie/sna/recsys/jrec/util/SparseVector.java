package de.unikn.ie.sna.recsys.jrec.util;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map.Entry;

public class SparseVector<T> extends HashMap<Integer, T>
{

    private static final long serialVersionUID = 1L;

    public T get(int x)
    {
        if (super.containsKey(x))
        {
            return super.get(x);
        }
        else
        {
            return null;
        }
    }

    public void toStream(OutputStream os)
    {
        try
        {
            OutputStreamWriter writer = new OutputStreamWriter(os);
            for (Entry<Integer, T> entry : super.entrySet())
            {
                writer.write(entry.getKey() + " " + entry.getValue() + "\n");
            }
            writer.flush();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
