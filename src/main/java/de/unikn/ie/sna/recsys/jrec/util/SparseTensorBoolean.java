package de.unikn.ie.sna.recsys.jrec.util;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map.Entry;

public class SparseTensorBoolean extends HashMap<Integer, SparseMatrixBoolean>
{

    private static final long serialVersionUID = 1L;

    public boolean get(int x, int y, int z)
    {
        if (super.containsKey(x))
        {
            return super.get(x).get(y, z);
        }
        else
        {
            return false;
        }
    }

    @Override
    public SparseMatrixBoolean get(Object x)
    {
        if (x instanceof Integer)
        {
            if (super.get(x) == null)
            {
                super.put((Integer) x, new SparseMatrixBoolean());
            }
        }
        return super.get(x);
    }

    public void toStream(OutputStream os)
    {
        try
        {
            OutputStreamWriter writer = new OutputStreamWriter(os);
            for (Entry<Integer, SparseMatrixBoolean> entry : super.entrySet())
            {
                for (Entry<Integer, SparseVectorBoolean> entry2 : entry.getValue().entrySet())
                {
                    int count = 0;
                    for (Integer value : entry2.getValue())
                    {
                        writer.write(entry.getKey() + " " + entry2.getKey() + " " + count++ + " " + value + "\n");
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

    public void fromFile(String filename)
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            String[] parts;
            while ((line = reader.readLine()) != null)
            {
                parts = line.split("\\s+");
                int t = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);
                int value = Integer.parseInt(parts[2]);
                get(t).get(m).add(value);
            }
            reader.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
