package de.unikn.ie.sna.recsys.jrec.util;

public class SparseMatrixDouble extends SparseMatrix<Double>
{
    private static final long serialVersionUID = 1L;

    public SparseMatrixDouble()
    {
    }

    public SparseMatrixDouble(int iColumn, int jColumn, int valueColumn)
    {
        super(iColumn, jColumn, valueColumn, Double.class);
    }

}
