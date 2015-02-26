package de.unikn.ie.sna.recsys.jrec.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map.Entry;

public class SparseMatrix<T> extends HashMap<Integer, SparseVector<T>>
{

    private static final long serialVersionUID = 1L;

    private Class<T> valueClazz;

    private int iColumn;
    private int jColumn;
    private int valueColumn;

    private T defaultValue;

    public SparseMatrix()
    {

    }

    public SparseMatrix(int iColumn, int jColumn, T defaultValue)
    {
        this.iColumn = iColumn;
        this.jColumn = jColumn;
        this.defaultValue = defaultValue;
    }

    public SparseMatrix(int iColumn, int jColumn, int valueColumn, Class<T> valueClazz)
    {
        this.iColumn = iColumn;
        this.jColumn = jColumn;
        this.valueColumn = valueColumn;
        this.valueClazz = valueClazz;
    }

    @Override
    public SparseVector<T> get(Object x)
    {
        if (x instanceof Integer)
        {
            if (super.get(x) == null)
            {
                super.put((Integer) x, new SparseVector<T>());
            }
        }
        return super.get(x);
    }

    public T get(int x, int y)
    {
        if (super.containsKey(x))
        {
            return super.get(x).get(y);
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
            for (Entry<Integer, SparseVector<T>> entry : super.entrySet())
            {
                for (Entry<Integer, T> ventry : entry.getValue().entrySet())
                {
                    writer.write(entry.getKey() + " " + ventry.getKey() + " " + ventry.getValue() + "\n");
                }
            }
            writer.flush();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void fromFile(String filename, String split)
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            String[] parts;
            T value;
            while ((line = reader.readLine()) != null)
            {
                parts = line.split(split);
                int m = Integer.parseInt(parts[this.iColumn]);
                int v = Integer.parseInt(parts[this.jColumn]);
                if (this.defaultValue != null)
                {
                    value = this.defaultValue;
                }
                else
                {
                    value = this.valueClazz.getConstructor(String.class).newInstance(parts[this.valueColumn]);
                }
                get(m).put(v, value);
            }
            reader.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
