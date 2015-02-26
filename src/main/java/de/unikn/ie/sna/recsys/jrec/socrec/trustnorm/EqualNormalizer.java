package de.unikn.ie.sna.recsys.jrec.socrec.trustnorm;

import java.util.Map.Entry;

import de.unikn.ie.sna.recsys.jrec.util.SparseVector;

public class EqualNormalizer implements TrustNormalizer
{

    @Override
    public void normalize(Dataset ds)
    {
        for (Entry<Integer, SparseVector<Double>> truster : ds.trusts.entrySet())
        {
            for (Integer trustee : truster.getValue().keySet())
            {
                ds.trusts.get(truster.getKey()).put(trustee, 1d / truster.getValue().size());
                ds.trustedBys.get(trustee).put(truster.getKey(), 1d / truster.getValue().size());
            }
        }
        for (Entry<Integer, SparseVector<Double>> trustee : ds.trustedBys.entrySet())
        {
            for (Integer truster : trustee.getValue().keySet())
            {
                ds.trusts.get(truster).put(trustee.getKey(), 1d / ds.trusts.get(truster).size());
                ds.trustedBys.get(trustee.getKey()).put(truster, 1d / ds.trusts.get(truster).size());
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
