package de.unikn.ie.sna.recsys.jrec.socrec.model;

import java.util.Arrays;

import de.unikn.ie.sna.recsys.jrec.util.DMatrixDouble;
import de.unikn.ie.sna.recsys.jrec.util.DVectorDouble;
import de.unikn.ie.sna.recsys.jrec.util.SparseMatrixDouble;

public class SGDBaseMF implements Model
{
    private boolean biasTerms;
    private DMatrixDouble U, I;
    private DVectorDouble W_U, W_I;
    private double        W, learnRate, lambdaU, lambdaI;
    private int           features;

    @Override
    public String getTitle()
    {
        return "SGDBaseMF";
    }

    @Override
    public void learn(int user, int item, double rating)
    {
        double e = rating - predict(user, item), u_f, i_f;
        for (int f = 0; f < this.features; f++)
        {
            u_f = this.U.get(user, f);
            i_f = this.I.get(item, f);
            this.U.get(user)[f] -= this.learnRate * (-e * i_f + this.lambdaU * u_f);
            this.I.get(item)[f] -= this.learnRate * (-e * u_f + this.lambdaI * i_f);
        }
        this.W_U.set(user, this.W_U.get(user) - this.learnRate * (-e));
        this.W_I.set(item, this.W_I.get(item) - this.learnRate * (-e));
        this.W -= this.learnRate * (-e);
    }

    @Override
    public double predict(int user, int item)
    {
        double dotV = 0;
        for (int f = 0; f < this.features; f++)
        {
            dotV += this.U.get(user, f) * this.I.get(item, f);
        }
        double result = this.W + dotV;
        if (this.biasTerms)
        {
            result += this.W_U.get(user) + this.W_I.get(item);
        }
        return result;
    }

    @Override
    public void init(int users, int items, int features, double learnRate, double lambdaU, double lambdaI, double lambdaT, double alpha,
                     SparseMatrixDouble trusts, SparseMatrixDouble trustedBys, double minRating, double maxRating, boolean biasTerms)
    {
        this.features = features;
        this.learnRate = learnRate;
        this.lambdaU = lambdaU;
        this.lambdaI = lambdaI;
        this.biasTerms = biasTerms;
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
        this.W = 0;
    }

    @Override
    public void beforeLearn(double average)
    {
        // no need in stochastic variant
    }

    @Override
    public void afterLearn()
    {
        // no need in stochastic variant
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
        // TODO Auto-generated method stub

    }
}
