package de.unikn.ie.sna.recsys.jrec.util;

import java.util.HashMap;

public class SparseMatrixBoolean extends HashMap<Integer, SparseVectorBoolean>
{

    private static final long serialVersionUID = 1L;

    public boolean get(int x, int y)
    {
        if (super.containsKey(x))
        {
            return super.get(x).get(y);
        }
        else
        {
            return false;
        }
    }

    @Override
    public SparseVectorBoolean get(Object x)
    {
        if (x instanceof Integer)
        {
            if (super.get(x) == null)
            {
                super.put((Integer) x, new SparseVectorBoolean());
            }
        }
        return super.get(x);
    }
}
