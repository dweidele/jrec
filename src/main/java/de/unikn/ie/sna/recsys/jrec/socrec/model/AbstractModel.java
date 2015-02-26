package de.unikn.ie.sna.recsys.jrec.socrec.model;

public abstract class AbstractModel implements Model
{
    protected double minRating, ratingRangeSize, globalBias;
    protected int    features, users, items;

    @Override
    public double predict(int user, int item)
    {
        return denormalize(sigScore(user, item));
    }

    @Override
    public void beforeLearn(double average)
    {
        average = (average - this.minRating) / this.ratingRangeSize;
        this.globalBias = Math.log(average / (1 - average));
        beforeLearn();
    }

    protected double gradientCommon(int user, int item, double rating)
    {
        double sigScore = sigScore(user, item);
        double err = rating - denormalize(sigScore);
        return err * sigScore * (1 - sigScore) * this.ratingRangeSize;
    }

    private double sigScore(int user, int item)
    {
        return 1 / (1 + Math.exp(-score(user, item)));
    }

    private double denormalize(double sigScore)
    {
        return this.minRating + sigScore * this.ratingRangeSize;
    }

    protected abstract double score(int user, int item);

    protected abstract void beforeLearn();
}
