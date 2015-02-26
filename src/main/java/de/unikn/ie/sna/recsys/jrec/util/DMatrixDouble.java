package de.unikn.ie.sna.recsys.jrec.util;

public class DMatrixDouble extends DMatrix<Double>
{

    public DMatrixDouble()
    {
        super(Double.class);
    }

    public void init(double mean, double stdev)
    {
        for (int i_1 = 0; i_1 < this.dim1; i_1++)
        {
            for (int i_2 = 0; i_2 < this.dim2; i_2++)
            {
                this.value[i_1][i_2] = Random.ran_gaussian(mean, stdev);
            }
        }
    }

    public void init_column(double mean, double stdev, int column)
    {
        for (int i_1 = 0; i_1 < this.dim1; i_1++)
        {
            this.value[i_1][column] = Random.ran_gaussian(mean, stdev);
        }
    }
}
