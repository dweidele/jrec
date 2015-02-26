package de.unikn.ie.sna.recsys.jrec.socrec.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.unikn.ie.sna.recsys.jrec.util.DMatrixDouble;
import de.unikn.ie.sna.recsys.jrec.util.DVectorDouble;
import de.unikn.ie.sna.recsys.jrec.util.SparseMatrixDouble;
import de.unikn.ie.sna.recsys.jrec.util.SparseVectorDouble;

public class SVDPP extends AbstractModel
{
    private boolean                    biasTerms;
    private DMatrixDouble              U, I, J, tempU, tempI, tempJ;
    private DVectorDouble              W_U, W_I, tempW_U, tempW_I;
    private double                     learnRate, lambdaU, lambdaI, lambdaJ;
    private Map<Integer, Set<Integer>> UI;

    // rated items influence cache
    private SparseMatrixDouble         RIIC;

    @Override
    public String getTitle()
    {
        return "SVD++";
    }

    @Override
    protected double score(int user, int item)
    {
        double dotV = 0;
        for (int f = 0; f < this.features; f++)
        {
            dotV += (this.U.get(user, f) + ratedItemsInfluence(user, f)) * this.I.get(item, f);
        }
        double result = this.globalBias + dotV;
        if (this.biasTerms)
        {
            result += this.W_U.get(user) + this.W_I.get(item);
        }
        return result;
    }

    @Override
    protected void beforeLearn()
    {
        // clear tempU & tempW_U
        for (int user = 0; user < this.users; user++)
        {
            this.tempW_U.set(user, 0d);
            for (int f = 0; f < this.features; f++)
            {
                this.tempU.get(user)[f] = 0d;
            }
        }

        // clear tempI, tempJ & tempW_I
        for (int item = 0; item < this.items; item++)
        {
            this.tempW_I.set(item, 0d);
            for (int f = 0; f < this.features; f++)
            {
                this.tempI.get(item)[f] = 0d;
                this.tempJ.get(item)[f] = 0d;
            }
        }
    }

    @Override
    public void learn(int user, int item, double rating)
    {
        // save rated items per user
        if (!this.UI.containsKey(user))
        {
            this.UI.put(user, new HashSet<Integer>());
        }
        this.UI.get(user).add(item);
        // compute gradient
        double commonGradient = gradientCommon(user, item, rating);
        // double error = rating - score(user, item);
        for (int f = 0; f < this.features; f++)
        {
            this.tempU.get(user)[f] += commonGradient * this.I.get(item, f);
            this.tempI.get(item)[f] += commonGradient * (this.U.get(user, f) + ratedItemsInfluence(user, f));
            double commonUpdate = commonGradient * this.I.get(item, f);
            commonUpdate /= Math.sqrt(this.UI.get(user).size());
            for (int j : this.UI.get(user))
            {
                this.tempJ.get(j)[f] += commonUpdate;
            }
        }
        this.tempW_U.set(user, this.tempW_U.get(user) + commonGradient);
        this.tempW_I.set(item, this.tempW_I.get(item) + commonGradient);
    }

    @Override
    public void afterLearn()
    {
        // enable cache after first iteration, as now all rated items are known
        if (this.RIIC == null)
        {
            this.RIIC = new SparseMatrixDouble();
        }
        // gradient descent in U and W_U
        for (int user = 0; user < this.users; user++)
        {
            this.W_U.set(user, this.W_U.get(user) - this.learnRate * (-this.tempW_U.get(user)));
            for (int f = 0; f < this.features; f++)
            {
                this.U.get(user)[f] -= this.learnRate * (-this.tempU.get(user, f) + this.lambdaU * this.U.get(user, f));
                // clear rated user item influence cache
                if (this.RIIC.containsKey(user))
                {
                    this.RIIC.get(user).clear();
                }
            }
        }

        // gradient descent in I, J and W_I
        for (int item = 0; item < this.items; item++)
        {
            this.W_I.set(item, this.W_I.get(item) - this.learnRate * (-this.tempW_I.get(item)));
            for (int f = 0; f < this.features; f++)
            {
                this.I.get(item)[f] -= this.learnRate * (-this.tempI.get(item, f) + this.lambdaI * this.I.get(item, f));
                this.J.get(item)[f] -= this.learnRate * (-this.tempJ.get(item, f) + this.lambdaJ * this.J.get(item, f));
            }
        }
    }

    private double ratedItemsInfluence(int user, int f)
    {
        if (this.RIIC == null)
        {
            return 0;
        }
        else
        {
            if (this.RIIC.containsKey(user))
            {
                if (this.RIIC.get(user).containsKey(f))
                {
                    return this.RIIC.get(user, f);
                }
            }
            else
            {
                this.RIIC.put(user, new SparseVectorDouble());
            }
            double influence = 0;
            if (this.UI.containsKey(user))
            {
                for (Integer j : this.UI.get(user))
                {
                    influence += this.J.get(j, f);
                }
                influence /= Math.sqrt(this.UI.get(user).size());
            }
            this.RIIC.get(user).put(f, influence);
            return influence;
        }
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
        this.lambdaJ = lambdaT;
        this.biasTerms = biasTerms;
        this.minRating = minRating;
        this.ratingRangeSize = maxRating - minRating;

        // initialize model
        this.U = new DMatrixDouble();
        this.U.setSize(users, features);
        this.U.init(0, 0.1);
        this.I = new DMatrixDouble();
        this.I.setSize(items, features);
        this.I.init(0, 0.1);
        this.J = new DMatrixDouble();
        this.J.setSize(items, features);
        this.J.init(0, 0.1);
        this.W_U = new DVectorDouble(users);
        this.W_U.init(0d);
        this.W_I = new DVectorDouble(items);
        this.W_I.init(0d);
        this.UI = new HashMap<Integer, Set<Integer>>();

        // initialize temporary matrices
        this.tempU = new DMatrixDouble();
        this.tempU.setSize(this.users, this.features);
        this.tempI = new DMatrixDouble();
        this.tempI.setSize(this.items, this.features);
        this.tempJ = new DMatrixDouble();
        this.tempJ.setSize(this.items, this.features);
        this.tempW_U = new DVectorDouble(users);
        this.tempW_I = new DVectorDouble(items);
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
        Arrays.fill(this.J.get(item), 0d);
    }

    @Override
    public void print()
    {
        // TODO Auto-generated method stub
    }
}
