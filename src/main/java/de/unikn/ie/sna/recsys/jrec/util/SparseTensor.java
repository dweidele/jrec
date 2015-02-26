package de.unikn.ie.sna.recsys.jrec.util;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map.Entry;

public class SparseTensor<T> extends HashMap<Integer, SparseMatrix<T>>
{

    private static final long serialVersionUID = 1L;

    private T classHelper;

    public T get(int x, int y, int z)
    {
        if (super.containsKey(x))
        {
            return super.get(x).get(y, z);
        }
        else
        {
            return null;
        }
    }

    @Override
    public SparseMatrix<T> get(Object x)
    {
        if (x instanceof Integer)
        {
            if (super.get(x) == null)
            {
                super.put((Integer) x, new SparseMatrix<T>());
            }
        }
        return super.get(x);
    }

    public void toStream(OutputStream os)
    {
        try
        {
            OutputStreamWriter writer = new OutputStreamWriter(os);
            for (Entry<Integer, SparseMatrix<T>> entry : super.entrySet())
            {
                for (Entry<Integer, SparseVector<T>> entry2 : entry.getValue().entrySet())
                {
                    for (Entry<Integer, T> entry3 : entry2.getValue().entrySet())
                    {
                        writer.write(entry.getKey() + " " + entry2.getKey() + " " + entry3.getKey() + " "
                            + entry3.getValue() + "\n");
                    }
                }
            }
            writer.flush();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void toFile(String filename)
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(filename);
            toStream(fos);
            fos.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void fromFile(String filename)
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            String[] parts;
            while ((line = reader.readLine()) != null)
            {
                parts = line.split(" ");
                int t = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);
                int v = Integer.parseInt(parts[2]);
                T value = (T) this.classHelper.getClass().getConstructor(String.class).newInstance(parts[3]);
                super.get(t).get(m).put(v, value);
            }
            reader.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
