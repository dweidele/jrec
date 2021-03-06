package de.unikn.ie.sna.recsys.jrec.socrec.model;

import java.util.Arrays;
import java.util.Map.Entry;

import de.unikn.ie.sna.recsys.jrec.util.DMatrixDouble;
import de.unikn.ie.sna.recsys.jrec.util.DVectorDouble;
import de.unikn.ie.sna.recsys.jrec.util.SparseMatrixDouble;

public class SocialMF extends AbstractModel
{
    private boolean biasTerms;
    private DMatrixDouble U, I, tempU, tempI, cacheHatU;
    private DVectorDouble W_U, W_I, tempW_U, tempW_I;
    private double        learnRate, lambdaU, lambdaI, lambdaT;
    private SparseMatrixDouble trusts, trustedBys;

    @Override
    public String getTitle()
    {
        return "SocialMF";
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
        // TODO try to just create new instances (this might save memory)
        // clear tempU & tempW_U & cacheHatU
        for (int user = 0; user < this.users; user++)
        {
            this.tempW_U.set(user, 0d);
            for (int f = 0; f < this.features; f++)
            {
                this.tempU.get(user)[f] = 0d;
                this.cacheHatU.get(user)[f] = null;
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
    }

    @Override
    public void learn(int user, int item, double rating)
    {
        double gradientCommon = gradientCommon(user, item, rating);
        // double error = rating - score(user, item);
        for (int f = 0; f < this.features; f++)
        {
            this.tempU.get(user)[f] += gradientCommon * this.I.get(item, f);
            this.tempI.get(item)[f] += gradientCommon * this.U.get(user, f);
        }
        this.tempW_U.set(user, this.tempW_U.get(user) + gradientCommon);
        this.tempW_I.set(item, this.tempW_I.get(item) + gradientCommon);
    }

    @Override
    public void afterLearn()
    {
        // calculate hatUs before any U is being updated
        for (int user = 0; user < this.users; user++)
        {
            for (int f = 0; f < this.features; f++)
            {
                hatU(user, f);
            }
        }

        // gradient descent in U and W_U
        for (int user = 0; user < this.users; user++)
        {
            this.W_U.set(user, this.W_U.get(user) - this.learnRate * (-this.tempW_U.get(user)));
            for (int f = 0; f < this.features; f++)
            {
                // sum of others influencing this user
                double trustedBys = 0;
                if (this.trustedBys.containsKey(user))
                {
                    for (Entry<Integer, Double> truster : this.trustedBys.get(user).entrySet())
                    {
                        trustedBys += truster.getValue() * hatU(truster.getKey(), f);
                    }
                }
                this.U.get(user)[f] -= this.learnRate * (-this.tempU.get(user, f) + this.lambdaU * this.U.get(user, f)
                // trust influence 
                                                         + this.lambdaT * hatU(user, f)
                // trustedBy influence 
                                       - this.lambdaT * trustedBys);
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

    private double hatU(int user, int feature)
    {
        if (this.cacheHatU.get(user, feature) == null)
        {
            double influence = 0;
            if (this.trusts.containsKey(user))
            {
                for (Entry<Integer, Double> trustee : this.trusts.get(user).entrySet())
                {
                    influence += trustee.getValue() * this.U.get(trustee.getKey(), feature);
                }
            }
            this.cacheHatU.get(user)[feature] = this.U.get(user, feature) - influence;
        }
        return this.cacheHatU.get(user, feature);
    }

    @Override
    public void init(int users, int items, int features, double learnRate, double lambdaU, double lambdaI, double lambdaT, double alpha,
                     SparseMatrixDouble trusts, SparseMatrixDouble trustedBys, double minRating, double maxRating, boolean biasTerms)
    {
        // store parameters
        this.users = users;
        this.items = items;
        this.features = features;
        this.learnRate = learnRate;
        this.lambdaU = lambdaU;
        this.lambdaI = lambdaI;
        this.lambdaT = lambdaT;
        this.biasTerms = biasTerms;
        this.trusts = trusts;
        this.trustedBys = trustedBys;
        this.minRating = minRating;
        this.ratingRangeSize = maxRating - minRating;

        // initialize model
        this.U = new DMatrixDouble();
        this.U.setSize(users, features);
        this.U.init(0, 0.1);
        this.I = new DMatrixDouble();
        this.I.setSize(items, features);
        this.I.init(0, 0.1);
        this.W_U = new DVectorDouble(users);
        this.W_U.init(0d);
        this.W_I = new DVectorDouble(items);
        this.W_I.init(0d);

        // initialize temporary matrices
        this.tempU = new DMatrixDouble();
        this.tempU.setSize(this.users, this.features);
        this.tempI = new DMatrixDouble();
        this.tempI.setSize(this.items, this.features);
        this.tempW_U = new DVectorDouble(users);
        this.tempW_I = new DVectorDouble(items);
        this.cacheHatU = new DMatrixDouble();
        this.cacheHatU.setSize(users, features);
    }

    @Override
    public void blackoutUser(int user)
    {
        Arrays.fill(this.U.get(user), 0d);
    }

    @Override
    public void blackoutItem(int item)
    {
        Arrays.fill(this.I.get(item), 0d);
    }

    @Override
    public void print()
    {
        // print latent user matrix
        System.out.print("Latent users:\n");
        System.out.print("-------------\n");
        for (int u = 0; u < this.users; u++)
        {
            for (int f = 0; f < this.features; f++)
            {
                System.out.print(this.U.get(u, f) + "\t");
            }
            System.out.print("\n");
        }
        // print latent item matrix
        System.out.print("\nLatent items:\n");
        System.out.print("-------------\n");
        for (int i = 0; i < this.items; i++)
        {
            for (int f = 0; f < this.features; f++)
            {
                System.out.print(this.I.get(i, f) + "\t");
            }
            System.out.print("\n");
        }
    }
}
