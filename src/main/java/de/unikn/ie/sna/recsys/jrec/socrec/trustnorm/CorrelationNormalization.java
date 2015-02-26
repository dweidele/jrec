package de.unikn.ie.sna.recsys.jrec.socrec.trustnorm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.unikn.ie.sna.recsys.jrec.util.SparseMatrixDouble;
import de.unikn.ie.sna.recsys.jrec.util.SparseVector;
import de.unikn.ie.sna.recsys.jrec.util.SparseVectorDouble;

public class CorrelationNormalization implements TrustNormalizer
{

    private Map<Integer, Double>        userMeans;
    private SparseVectorDouble          C_SUM;
    private Map<Integer, List<Integer>> zeroWeightRelations;

    @Override
    public void normalize(Dataset ds)
    {
        this.C_SUM = new SparseVectorDouble();
        this.userMeans = new HashMap<Integer, Double>();
        this.zeroWeightRelations = new HashMap<Integer, List<Integer>>();
        SparseMatrixDouble ratings = aggregateRatings(ds);
        Map<Integer, Set<Integer>> userItems = getUserItems(ratings);
        Map<Integer, Set<Integer>> itemUsers = getItemUsers(ratings);
        for (Entry<Integer, SparseVector<Double>> truster : ds.trusts.entrySet())
        {
            int user = truster.getKey();
            this.C_SUM.put(user, 0d);
            for (Integer trustee : truster.getValue().keySet())
            {
                double weight = ppmcc(user, trustee, userItems, itemUsers, ratings, false);
                if (weight == 0)
                {
                    if (!this.zeroWeightRelations.containsKey(user))
                    {
                        this.zeroWeightRelations.put(user, new ArrayList<Integer>());
                    }
                    this.zeroWeightRelations.get(user).add(trustee);
                }
                else
                {
                    this.C_SUM.put(user, this.C_SUM.get(user) + Math.abs(weight));
                    ds.trusts.get(user).put(trustee, weight);
                }
            }
        }

        // drop out zero weight relations
        for (Entry<Integer, List<Integer>> relations : this.zeroWeightRelations.entrySet())
        {
            int truster = relations.getKey();
            for (Integer trustee : relations.getValue())
            {
                ds.trusts.get(truster).remove(trustee);
            }
            if (ds.trusts.get(truster).size() == 0)
            {
                ds.trusts.remove(truster);
            }
        }

        // normalize sum of absolutes to 1
        int count = 0;
        for (Entry<Integer, SparseVector<Double>> truster : ds.trusts.entrySet())
        {
            count += truster.getValue().size();
            for (Entry<Integer, Double> trustee : truster.getValue().entrySet())
            {
                ds.trusts.get(truster.getKey()).put(trustee.getKey(), trustee.getValue() / this.C_SUM.get(truster.getKey()));
                ds.trustedBys.get(trustee.getKey()).put(truster.getKey(), trustee.getValue() / this.C_SUM.get(truster.getKey()));
            }
        }
        System.out.println(count / 2);

        // validate
        for (SparseVector<Double> values : ds.trusts.values())
        {
            double sum = 0;
            for (Double value : values.values())
            {
                sum += Math.abs(value);
            }
            if (Math.abs(1 - sum) > 0.0000001)
            {
                throw new RuntimeException("normalization failed: " + sum);
            }
        }
    }

    private SparseMatrixDouble aggregateRatings(Dataset ds)
    {
        SparseMatrixDouble ratings = new SparseMatrixDouble();
        for (int fold = 0; fold < ds.getFolds(); fold++)
        {
            for (Entry<Integer, SparseVector<Double>> user : ds.getRatings(fold).entrySet())
            {
                if (!ratings.containsKey(user.getKey()))
                {
                    ratings.put(user.getKey(), new SparseVectorDouble());
                }
                for (Entry<Integer, Double> item : user.getValue().entrySet())
                {
                    ratings.get(user.getKey()).put(item.getKey(), item.getValue());
                }
            }
        }
        return ratings;
    }

    private double meanUser(int user, Map<Integer, Set<Integer>> userItems, SparseMatrixDouble ratings)
    {
        if (this.userMeans.containsKey(user))
        {
            return this.userMeans.get(user);
        }
        double mean = 0;
        for (Integer item : userItems.get(user))
        {
            mean += ratings.get(user, item);
        }
        mean = mean / userItems.get(user).size();
        this.userMeans.put(user, mean);
        return mean;
    }

    private double ppmcc(int u, int v, Map<Integer, Set<Integer>> userItems, Map<Integer, Set<Integer>> itemUsers,
                         SparseMatrixDouble ratings, boolean fixMeans)
    {
        // determine common items
        Set<Integer> commonItems = new HashSet<Integer>();
        if (!userItems.containsKey(u) || !userItems.containsKey(v))
        {
            return 0;
        }
        for (Integer item : userItems.get(u))
        {
            if (userItems.get(v).contains(item))
            {
                commonItems.add(item);
            }
        }
        if (commonItems.size() == 0)
        {
            return 0;
        }
        // calculate ppmcc
        double ppmcc = 0;
        double uMean = 3;
        double vMean = 3;
        if (!fixMeans)
        {
            uMean = meanUser(u, userItems, ratings);
            vMean = meanUser(v, userItems, ratings);
        }
        double uVar = 0;
        double vVar = 0;
        for (Integer item : commonItems)
        {
            double uDiff = ratings.get(u, item) - uMean + 0.0001;
            double vDiff = ratings.get(v, item) - vMean + 0.0001;
            ppmcc += uDiff * vDiff;
            uVar += Math.pow(uDiff, 2);
            vVar += Math.pow(vDiff, 2);
        }
        return ppmcc / Math.sqrt(uVar * vVar);
    }

    private Map<Integer, Set<Integer>> getUserItems(SparseMatrixDouble ratings)
    {
        Map<Integer, Set<Integer>> userItems = new HashMap<Integer, Set<Integer>>();
        for (Entry<Integer, SparseVector<Double>> user : ratings.entrySet())
        {
            userItems.put(user.getKey(), user.getValue().keySet());
        }
        return userItems;
    }

    private Map<Integer, Set<Integer>> getItemUsers(SparseMatrixDouble ratings)
    {
        Map<Integer, Set<Integer>> itemUsers = new HashMap<Integer, Set<Integer>>();
        for (Entry<Integer, SparseVector<Double>> user : ratings.entrySet())
        {
            for (Integer item : user.getValue().keySet())
            {
                if (!itemUsers.containsKey(item))
                {
                    itemUsers.put(item, new HashSet<Integer>());
                }
                itemUsers.get(item).add(user.getKey());
            }
        }
        return itemUsers;
    }
}
