package de.unikn.ie.sna.recsys.jrec.util;

import java.util.HashSet;

public class SparseVectorBoolean extends HashSet<Integer>
{

    private static final long serialVersionUID = 1L;

    public boolean get(int x)
    {
        if (super.contains(x))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
