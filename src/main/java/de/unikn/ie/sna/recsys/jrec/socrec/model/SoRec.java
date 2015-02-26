package de.unikn.ie.sna.recsys.jrec.socrec.model;

import java.util.Arrays;
import java.util.Map.Entry;

import de.unikn.ie.sna.recsys.jrec.util.DMatrixDouble;
import de.unikn.ie.sna.recsys.jrec.util.DVectorDouble;
import de.unikn.ie.sna.recsys.jrec.util.SparseMatrixDouble;
import de.unikn.ie.sna.recsys.jrec.util.SparseVector;
import de.unikn.ie.sna.recsys.jrec.util.SparseVectorDouble;

public class SoRec extends AbstractModel
{
    private boolean biasTerms;
    private DMatrixDouble U, I, V, tempU, tempI, tempV;
    private DVectorDouble W_U, W_I, tempW_U, tempW_I;
    private double        learnRate, lambdaT, lambdaU, lambdaI, lambdaV, minC, rangeC;
    private SparseMatrixDouble trusts, commonGradientCacheC;

    @Override
    public String getTitle()
    {
        return "SoRec";
    }

    @Override
    protected double score(int user, int item)
    {
        double dotV = 0;
        for (int f = 0; f < this.features; f++)
        {
            dotV += this.U.get(user, f) * this.I.get(item, f);
        }
        double result = this.globalBias + dotV;
        if (this.biasTerms)
        {
            result += this.W_U.get(user) + this.W_I.get(item);
        }
        return result;
    }

    @Override
    public void beforeLearn()
    {
        // clear tempU & tempW_U & tempV
        for (int user = 0; user < this.users; user++)
        {
            this.tempW_U.set(user, 0d);
            for (int f = 0; f < this.features; f++)
            {
                this.tempU.get(user)[f] = 0d;
                this.tempV.get(user)[f] = 0d;
            }
        }
        // clear tempI & tempW_I
        for (int item = 0; item < this.items; item++)
        {
            this.tempW_I.set(item, 0d);
            for (int f = 0; f < this.features; f++)
            {
                this.tempI.get(item)[f] = 0d;
            }
        }

        // learn V
        double commonGradient;
        for (Entry<Integer, SparseVector<Double>> truster : this.trusts.entrySet())
        {
            if (!this.commonGradientCacheC.containsKey(truster.getKey()))
            {
                this.commonGradientCacheC.put(truster.getKey(), new SparseVectorDouble());
            }
            for (Entry<Integer, Double> trustee : truster.getValue().entrySet())
            {
                commonGradient = commonGradientC(truster.getKey(), trustee.getKey());
                this.commonGradientCacheC.get(truster.getKey()).put(trustee.getKey(), commonGradient);
                for (int f = 0; f < this.features; f++)
                {
                    this.tempV.get(trustee.getKey())[f] += commonGradient * this.U.get(truster.getKey())[f];
                }
            }
        }
    }

    @Override
    public void learn(int user, int item, double rating)
    {
        double commonGradient = gradientCommon(user, item, rating);
        for (int f = 0; f < this.features; f++)
        {
            this.tempU.get(user)[f] += commonGradient * this.I.get(item, f);
            this.tempI.get(item)[f] += commonGradient * this.U.get(user, f);
        }
        this.tempW_U.set(user, this.tempW_U.get(user) + commonGradient);
        this.tempW_I.set(item, this.tempW_I.get(item) + commonGradient);
    }

    @Override
    public void afterLearn()
    {
        // gradient descent in U and W_U
        double influence;
        for (int user = 0; user < this.users; user++)
        {
            this.W_U.set(user, this.W_U.get(user) - this.learnRate * (-this.tempW_U.get(user)));
            for (int f = 0; f < this.features; f++)
            {
                influence = 0;
                if (this.trusts.containsKey(user))
                {
                    for (Entry<Integer, Double> trustee : this.trusts.get(user).entrySet())
                    {
                        influence += this.commonGradientCacheC.get(user, trustee.getKey()) * this.V.get(trustee.getKey(), f);
                    }
                    influence *= this.lambdaT;
                }
                this.U.get(user)[f] -= this.learnRate * (-this.tempU.get(user, f) - influence + this.lambdaU * this.U.get(user, f));
            }
        }

        // gradient descent in V
        for (int user = 0; user < this.users; user++)
        {
            for (int f = 0; f < this.features; f++)
            {
                this.V.get(user)[f] -= this.learnRate * (-this.tempV.get(user, f) * this.lambdaT + this.lambdaV * this.V.get(user, f));
            }
        }

        // gradient descent in I and W_I
        for (int item = 0; item < this.items; item++)
        {
            this.W_I.set(item, this.W_I.get(item) - this.learnRate * (-this.tempW_I.get(item)));
            for (int f = 0; f < this.features; f++)
            {
                this.I.get(item)[f] -= this.learnRate * (-this.tempI.get(item, f) + this.lambdaI * this.I.get(item, f));
            }
        }
    }

    @Override
    public void init(int users, int items, int features, double learnRate, double lambdaU, double lambdaI, double lambdaT, double lambdaV,
                     SparseMatrixDouble trusts, SparseMatrixDouble trustedBys, double minRating, double maxRating, boolean biasTerms)
    {
        this.users = users;
        this.items = items;
        this.features = features;
        this.learnRate = learnRate;
        this.lambdaU = lambdaU;
        this.lambdaI = lambdaI;
        this.lambdaT = lambdaT;
        this.lambdaV = lambdaV;
        this.trusts = trusts;
        this.biasTerms = biasTerms;
        this.minRating = minRating;
        this.ratingRangeSize = maxRating - minRating;
        this.minC = Double.MAX_VALUE;

        // initialize model
        this.U = new DMatrixDouble();
        this.U.setSize(users, features);
        this.U.init(0, 0.1);
        this.I = new DMatrixDouble();
        this.I.setSize(items, features);
        this.I.init(0, 0.1);
        this.V = new DMatrixDouble();
        this.V.setSize(users, features);
        this.V.init(0, 0.1);
        this.W_U = new DVectorDouble(users);
        this.W_U.init(0d);
        this.W_I = new DVectorDouble(items);
        this.W_I.init(0d);

        // initialize temporary matrices
        this.tempU = new DMatrixDouble();
        this.tempU.setSize(this.users, this.features);
        this.tempI = new DMatrixDouble();
        this.tempI.setSize(this.items, this.features);
        this.tempV = new DMatrixDouble();
        this.tempV.setSize(users, features);
        this.tempW_U = new DVectorDouble(users);
        this.tempW_I = new DVectorDouble(items);
        this.commonGradientCacheC = new SparseMatrixDouble();

        // calculate range C
        double maxC = Double.MIN_VALUE;
        for (Entry<Integer, SparseVector<Double>> truster : trusts.entrySet())
        {
            for (Entry<Integer, Double> trustee : truster.getValue().entrySet())
            {
                this.minC = Math.min(this.minC, trustee.getValue());
                maxC = Math.max(maxC, trustee.getValue());
            }
        }
        this.rangeC = maxC - this.minC;
    }

    private double commonGradientC(int truster, int trustee)
    {
        double sigScore = 0, err;
        for (int f = 0; f < this.features; f++)
        {
            sigScore += this.U.get(truster, f) * this.V.get(trustee, f);
        }
        sigScore = 1 / (1 + Math.exp(-sigScore));
        err = this.trusts.get(truster, trustee) - (this.minC + sigScore * this.rangeC);
        return err * sigScore * (1 - sigScore) * this.rangeC;
    }

    @Override
    public void blackoutUser(int user)
    {
        // TODO check whether it really improves, as there can be learned U and/or V from network
        Arrays.fill(this.U.get(user), 0d);
        Arrays.fill(this.V.get(user), 0d);
    }

    @Override
    public void blackoutItem(int item)
    {
        Arrays.fill(this.I.get(item), 0d);
    }

    @Override
    public void print()
    {
        // TODO Auto-generated method stub

    }
}
