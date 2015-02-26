package de.unikn.ie.sna.recsys.jrec.util;

import java.io.FileWriter;
import java.lang.reflect.Array;

public class DVector<T>
{
    protected int          dim;
    protected T[]          value;
    private final Class<?> tClazz;

    public DVector(Class<T> tClazz)
    {
        this.tClazz = tClazz;
        this.dim = 0;
        this.value = null;
    }

    public DVector(int p_dim, Class<T> tClazz)
    {
        this.tClazz = tClazz;
        this.dim = 0;
        this.value = null;
        setSize(p_dim);
    }

    public T get(int x)
    {
        return this.value[x];
    }

    public void set(int x, T val)
    {
        this.value[x] = val;
    }

    @SuppressWarnings("unchecked")
    public void setSize(int p_dim)
    {
        this.dim = p_dim;
        this.value = (T[]) Array.newInstance(this.tClazz, this.dim);
    }

    public void init(T v)
    {
        for (int i = 0; i < this.dim; i++)
        {
            this.value[i] = v;
        }
    }

    public void assign(T[] v)
    {
        if (v.length != this.dim)
        {
            setSize(v.length);
        }
        for (int i = 0; i < this.dim; i++)
        {
            this.value[i] = v[i];
        }
    }

    public void save(String filename)
    {
        try
        {
            FileWriter writer = new FileWriter(filename);

            for (int i = 0; i < this.dim; i++)
            {
                writer.write(this.value[i] + "\n");
            }
            writer.flush();
            writer.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public int getDim()
    {
        return this.dim;
    }
}
