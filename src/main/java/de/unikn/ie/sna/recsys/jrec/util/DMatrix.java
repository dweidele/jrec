package de.unikn.ie.sna.recsys.jrec.util;

import java.io.FileWriter;
import java.lang.reflect.Array;

public class DMatrix<T>
{
    protected String[]     col_names;
    protected int          dim1;
    protected int          dim2;
    protected T[][]        value;
    private final Class<?> tClazz;

    public DMatrix(int p_dim1, int p_dim2, Class<T> tClazz)
    {
        this.tClazz = tClazz;
        this.dim1 = 0;
        this.dim2 = 0;
        this.value = null;
        setSize(p_dim1, p_dim2);
    }

    public DMatrix(Class<T> tClazz)
    {
        this.tClazz = tClazz;
        this.dim1 = 0;
        this.dim2 = 0;
        this.value = null;
    }

    @SuppressWarnings("unchecked")
    public void setSize(int p_dim1, int p_dim2)
    {
        this.dim1 = p_dim1;
        this.dim2 = p_dim2;
        this.value = (T[][]) Array.newInstance(this.tClazz, this.dim1, this.dim2);
        this.col_names = new String[this.dim2];
        for (int i = 0; i < this.dim2; i++)
        {
            this.col_names[i] = "";
        }
    }

    public T get(int x, int y)
    {
        assert ((x < this.dim1) && (y < this.dim2));
        return this.value[x][y];
    }

    public T[] get(int x)
    {
        assert ((x < this.dim1));
        return this.value[x];
    }

    public void save(String filename)
    {
        save(filename, false);
    }

    public void save(String filename, boolean has_header)
    {
        try
        {
            FileWriter writer = new FileWriter(filename);
            if (has_header)
            {
                for (int i_2 = 0; i_2 < this.dim2; i_2++)
                {
                    if (i_2 > 0)
                    {
                        writer.write("\t");
                    }
                    writer.write(this.col_names[i_2]);
                }
                writer.write("\n");
            }
            for (int i_1 = 0; i_1 < this.dim1; i_1++)
            {
                for (int i_2 = 0; i_2 < this.dim2; i_2++)
                {
                    if (i_2 > 0)
                    {
                        writer.write("\t");
                    }
                    writer.write(this.value[i_1][i_2].toString());
                }
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}
