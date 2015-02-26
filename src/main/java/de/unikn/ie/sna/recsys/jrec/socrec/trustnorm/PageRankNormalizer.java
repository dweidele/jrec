package de.unikn.ie.sna.recsys.jrec.socrec.trustnorm;

import java.util.Map.Entry;

import de.unikn.ie.sna.recsys.jrec.util.SparseVector;
import de.unikn.ie.sna.recsys.jrec.util.SparseVectorDouble;

public class PageRankNormalizer implements TrustNormalizer
{

    private SparseVectorDouble TRUSTER_WEIGHTS;
    private SparseVectorDouble TRUSTER_WEIGHTS_TEMP;

    @Override
    public void normalize(Dataset ds)
    {
        this.TRUSTER_WEIGHTS = new SparseVectorDouble();
        this.TRUSTER_WEIGHTS_TEMP = new SparseVectorDouble();

        // initialize trust matrices with equal trust norm
        EqualNormalizer en = new EqualNormalizer();
        en.normalize(ds);

        // initialize each user weight with 1 
        for (Entry<Integer, SparseVector<Double>> truster : ds.trusts.entrySet())
        {
            this.TRUSTER_WEIGHTS.put(truster.getKey(), 1d);
        }

        // perform page rank
        double change = 1, THRESHOLD = 0.0001, dampingFactor = 0.85;
        double norm = (1 - dampingFactor) / ds.trusts.size();
        while (change > THRESHOLD)
        {
            for (Entry<Integer, SparseVector<Double>> truster : ds.trusts.entrySet())
            {
                double sum = 0;
                for (Integer trustee : truster.getValue().keySet())
                {
                    // TODO in case of directed networks the following line must be adjusted 
                    // to make use of OUTgoing edges for trustee
                    sum += this.TRUSTER_WEIGHTS.get(trustee) / ds.trusts.get(trustee).size();
                }
                sum *= dampingFactor;
                this.TRUSTER_WEIGHTS_TEMP.put(truster.getKey(), norm + sum);
            }
            change = 0;
            for (Entry<Integer, SparseVector<Double>> truster : ds.trusts.entrySet())
            {
                change += Math.abs(this.TRUSTER_WEIGHTS.get(truster.getKey()) - this.TRUSTER_WEIGHTS_TEMP.get(truster.getKey()));
                this.TRUSTER_WEIGHTS.put(truster.getKey(), this.TRUSTER_WEIGHTS_TEMP.get(truster.getKey()));
            }
        }

        // apply page rank weights to data set trust matrices
        for (Entry<Integer, SparseVector<Double>> truster : ds.trusts.entrySet())
        {
            double sum = 0;
            for (Integer trustee : truster.getValue().keySet())
            {
                sum += this.TRUSTER_WEIGHTS.get(trustee);
            }
            for (Integer trustee : truster.getValue().keySet())
            {
                ds.trusts.get(truster.getKey()).put(trustee, this.TRUSTER_WEIGHTS.get(trustee) / sum);
                ds.trustedBys.get(trustee).put(truster.getKey(), this.TRUSTER_WEIGHTS.get(trustee) / sum);
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
