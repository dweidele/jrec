package de.unikn.ie.sna.recsys.jrec.socrec;

import java.io.FileWriter;
import java.util.Map.Entry;
import java.util.Random;

import de.unikn.ie.sna.recsys.jrec.util.DMatrixDouble;
import de.unikn.ie.sna.recsys.jrec.util.SparseMatrixDouble;
import de.unikn.ie.sna.recsys.jrec.util.SparseVector;

public class DataGenerator
{
    private static final String             OUT_DIR           = "src/main/assembly/data/ratingpred/synthetic/";
    private static final String             OUT_RAT           = DataGenerator.OUT_DIR + "ratings.txt";
    private static final String             OUT_WOT           = DataGenerator.OUT_DIR + "trust.txt";

    private static final int                USERS             = 71000;
    private static final int                ITEMS             = 104000;
    private static final int                FEATURES          = 5;
    private static final int                CONNECTIONS       = 509000;
    private static final int                RATINGS           = 575000;
    private static final double             GAMMA             = 0.5;

    private static final int                MIN_RATING        = 1;
    private static final int                RATING_RANGE_SIZE = 4;

    private static final DMatrixDouble      U                 = new DMatrixDouble();
    private static final DMatrixDouble      I                 = new DMatrixDouble();
    private static final SparseMatrixDouble C                 = new SparseMatrixDouble();
    private static final SparseMatrixDouble R                 = new SparseMatrixDouble();

    public static void main(String[] args)
        throws Exception
    {
        // initialize latent matrices randomly gaussian
        System.out.print("Initializing latent " + DataGenerator.FEATURES + "-factors for " + DataGenerator.USERS + " users and "
                         + DataGenerator.ITEMS + " items...");
        DataGenerator.U.setSize(DataGenerator.USERS, DataGenerator.FEATURES);
        DataGenerator.I.setSize(DataGenerator.ITEMS, DataGenerator.FEATURES);
        Random random = new Random();
        for (int u = 0; u < DataGenerator.USERS; u++)
        {
            for (int f = 0; f < DataGenerator.FEATURES; f++)
            {

                DataGenerator.U.get(u)[f] = random.nextGaussian();
            }
        }
        for (int i = 0; i < DataGenerator.ITEMS; i++)
        {
            for (int f = 0; f < DataGenerator.FEATURES; f++)
            {
                DataGenerator.I.get(i)[f] = random.nextGaussian();
            }
        }
        System.out.println("done.");
        // generate social network
        System.out.print("Generating " + DataGenerator.CONNECTIONS + " social connections...");
        int a, b;
        for (int c = 0; c < DataGenerator.CONNECTIONS; c++)
        {
            a = random.nextInt(DataGenerator.USERS);
            b = random.nextInt(DataGenerator.USERS);
            while (a == b || DataGenerator.C.get(a, b) != null)
            {
                a = random.nextInt(DataGenerator.USERS);
                b = random.nextInt(DataGenerator.USERS);
            }
            DataGenerator.C.get(a).put(b, 1d);
        }
        System.out.println("done.");
        // save social network
        System.out.print("Saving social network to " + DataGenerator.OUT_WOT + "...");
        FileWriter out = new FileWriter(DataGenerator.OUT_WOT);
        for (Entry<Integer, SparseVector<Double>> truster : DataGenerator.C.entrySet())
        {
            for (Entry<Integer, Double> trustee : truster.getValue().entrySet())
            {
                out.write(truster.getKey() + "\t" + trustee.getKey() + "\n");
                out.flush();
            }
        }
        out.close();
        System.out.println("done.");
        // generate ratings
        System.out.print("Generating ratings...");
        double ratingUser, ratingFriend, ratingFriends, rating;
        int u, i;
        for (int r = 0; r < DataGenerator.RATINGS; r++)
        {
            u = random.nextInt(DataGenerator.USERS);
            i = random.nextInt(DataGenerator.ITEMS);
            while (DataGenerator.R.get(u, i) != null)
            {
                u = random.nextInt(DataGenerator.USERS);
                i = random.nextInt(DataGenerator.ITEMS);
            }
            ratingUser = 0;
            for (int f = 0; f < DataGenerator.FEATURES; f++)
            {
                ratingUser += DataGenerator.U.get(u, f) * DataGenerator.I.get(i, f);
            }
            ratingFriends = 0;
            for (Integer v : DataGenerator.C.get(u).keySet())
            {
                ratingFriend = 0;
                for (int f = 0; f < DataGenerator.FEATURES; f++)
                {
                    ratingFriend += DataGenerator.U.get(v, f) * DataGenerator.I.get(i, f);
                }
                ratingFriends += (1 / DataGenerator.C.get(u).size()) * ratingFriend;
            }
            rating = (DataGenerator.GAMMA * ratingUser) + ((1 - DataGenerator.GAMMA) * ratingFriends);
            rating = 1 / (1 + Math.exp(-rating));
            rating = DataGenerator.MIN_RATING + rating * DataGenerator.RATING_RANGE_SIZE;
            DataGenerator.R.get(u).put(i, rating);
        }
        System.out.println("done.");
        // save ratings
        System.out.print("Saving ratings to " + DataGenerator.OUT_RAT + "...");
        out = new FileWriter(DataGenerator.OUT_RAT);
        for (Entry<Integer, SparseVector<Double>> user : DataGenerator.R.entrySet())
        {
            for (Entry<Integer, Double> item : user.getValue().entrySet())
            {
                out.write(user.getKey() + "\t" + item.getKey() + "\t" + item.getValue() + "\n");
            }
            out.flush();
        }
        out.close();
        System.out.println("done.");
    }
}
