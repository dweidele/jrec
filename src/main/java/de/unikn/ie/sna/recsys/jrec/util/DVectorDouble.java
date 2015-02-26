package de.unikn.ie.sna.recsys.jrec.util;

public class DVectorDouble extends DVector<Double>
{

    public DVectorDouble()
    {
        super(Double.class);
    }

    public DVectorDouble(int dim)
    {
        super(dim, Double.class);
    }

}
