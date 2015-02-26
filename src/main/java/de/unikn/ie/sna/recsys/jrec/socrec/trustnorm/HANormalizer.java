package de.unikn.ie.sna.recsys.jrec.socrec.trustnorm;

import java.util.Map.Entry;

import de.unikn.ie.sna.recsys.jrec.util.SparseMatrixDouble;
import de.unikn.ie.sna.recsys.jrec.util.SparseVector;
import de.unikn.ie.sna.recsys.jrec.util.SparseVectorDouble;

public class HANormalizer implements TrustNormalizer
{

    private SparseMatrixDouble C;
    private SparseVectorDouble C_SUM;

    @Override
    public void normalize(Dataset ds)
    {
        this.C = new SparseMatrixDouble();
        this.C_SUM = new SparseVectorDouble();

        // calculate normalization and save to C
        double d_in, d_out;
        for (Entry<Integer, SparseVector<Double>> truster : ds.trusts.entrySet())
        {
            d_out = truster.getValue().size();
            if (!this.C.containsKey(truster.getKey()))
            {
                this.C.put(truster.getKey(), new SparseVectorDouble());
                this.C_SUM.put(truster.getKey(), 0d);
            }
            for (Entry<Integer, Double> trustee : truster.getValue().entrySet())
            {
                // TODO in case of directed networks the following line must be adjusted to make use of INgoing edges
                d_in = ds.trusts.get(trustee.getKey()).size();
                double trustFactor = Math.sqrt(d_in / (d_out + d_in));
                this.C_SUM.put(truster.getKey(), this.C_SUM.get(truster.getKey()) + trustFactor);
                this.C.get(truster.getKey()).put(trustee.getKey(), trustFactor);
            }
        }

        // copy from C to data set and normalize sum per truster = 1
        for (Entry<Integer, SparseVector<Double>> truster : this.C.entrySet())
        {
            for (Entry<Integer, Double> trustee : truster.getValue().entrySet())
            {
                ds.trusts.get(truster.getKey()).put(trustee.getKey(),
                                                    this.C.get(truster.getKey(), trustee.getKey()) / this.C_SUM.get(truster.getKey()));
                ds.trustedBys.get(trustee.getKey()).put(truster.getKey(),
                                                        this.C.get(truster.getKey(), trustee.getKey()) / this.C_SUM.get(truster.getKey()));
            }
        }

        // validate
        for (SparseVector<Double> values : ds.trusts.values())
        {
            double sum = 0;
            for (Double value : values.values())
            {
                sum += value;
            }
            if (Math.abs(1 - sum) > 0.0000001)
            {
                throw new RuntimeException("normalization failed: " + sum);
            }
        }
    }
}
