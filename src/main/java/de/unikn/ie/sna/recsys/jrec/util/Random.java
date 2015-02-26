package de.unikn.ie.sna.recsys.jrec.util;

public class Random
{

    public static double ran_gaussian()
    {
        // method from Joseph L. Leva: "A fast normal Random number generator"
        double u, v, x, y, Q;
        do
        {
            do
            {
                u = Random.ran_uniform();
            }
            while (u == 0.0);
            v = 1.7156 * (Random.ran_uniform() - 0.5);
            x = u - 0.449871;
            y = Math.abs(v) + 0.386595;
            Q = x * x + y * (0.19600 * y - 0.25472 * x);
            if (Q < 0.27597)
            {
                break;
            }
        }
        while ((Q > 0.27846) || ((v * v) > (-4.0 * u * u * Math.log(u))));
        return v / u;
    }

    public static double ran_gaussian(double mean, double stdev)
    {
        if ((stdev == 0.0) || (Double.isNaN(stdev)))
        {
            return mean;
        }
        else
        {
            return mean + stdev * Random.ran_gaussian();
        }
    }

    public static double ran_uniform()
    {
        return Math.random();
    }

    public static double ran_exp()
    {
        return -Math.log(1 - Random.ran_uniform());
    }

}
