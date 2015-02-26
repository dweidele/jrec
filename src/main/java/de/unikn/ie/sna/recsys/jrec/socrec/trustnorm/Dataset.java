package de.unikn.ie.sna.recsys.jrec.socrec.trustnorm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import de.unikn.ie.sna.recsys.jrec.util.SparseMatrixDouble;
import de.unikn.ie.sna.recsys.jrec.util.SparseVector;
import de.unikn.ie.sna.recsys.jrec.util.SparseVectorDouble;

public class Dataset
{
    protected final String         title;

    protected SparseMatrixDouble[] ratingsFolds;
    protected SparseMatrixDouble   testSet;
    protected SparseMatrixDouble   trusts;
    protected SparseMatrixDouble   trustedBys;

    protected int                  maxUserId;
    protected int                  maxItemId;
    protected double               minRating;
    protected double               maxRating;

    protected final int            folds;
    protected final int            testSetSize;

    public Dataset(String title, int folds, int testSetSize)
    {
        this.title = title;
        this.folds = folds;
        this.testSetSize = testSetSize;
    }

    public void setRatings(SparseMatrixDouble ratings, boolean coldStart)
    {
        // initialize rating folds
        this.ratingsFolds = new SparseMatrixDouble[this.folds];
        for (int i = 0; i < this.folds; i++)
        {
            this.ratingsFolds[i] = new SparseMatrixDouble();
        }

        // copy each rating to a random fold
        Random r = new Random();
        for (Entry<Integer, SparseVector<Double>> users : ratings.entrySet())
        {
            for (Entry<Integer, Double> items : users.getValue().entrySet())
            {
                int fold = r.nextInt(this.folds);
                if (!this.ratingsFolds[fold].containsKey(users.getKey()))
                {
                    this.ratingsFolds[fold].put(users.getKey(), new SparseVectorDouble());
                }
                this.ratingsFolds[fold].get(users.getKey()).put(items.getKey(), items.getValue());
            }
        }

        // extract test set
        this.testSet = new SparseMatrixDouble();
        int sampled = 0;
        Integer[] userIds = ratings.keySet().toArray(new Integer[] {});
        while (sampled < this.testSetSize)
        {
            // update userIds of train set after each 5000 ratings have been moved to test set
            if (sampled % 5000 == 0)
            {
                userIds = ratings.keySet().toArray(new Integer[] {});
            }
            // pick a random position within the userIds
            int userPos = r.nextInt(userIds.length);
            // determine the userId of this position
            int userId = userIds[userPos];
            if (coldStart)
            {
                // among all folds
                for (int fold = 0; fold < this.folds; fold++)
                {
                    // if there are ratings of the user within the fold
                    if (this.ratingsFolds[fold].containsKey(userId))
                    {
                        // move the triples to test test
                        if (!this.testSet.containsKey(userId))
                        {
                            this.testSet.put(userId, new SparseVectorDouble());
                        }
                        this.testSet.get(userId).putAll(this.ratingsFolds[fold].get(userId));
                        sampled += this.ratingsFolds[fold].get(userId).size();
                        // remove the triple from train set
                        this.ratingsFolds[fold].remove(userId);
                    }
                }
            }
            else
            {
                // pick a random fold
                int fold = r.nextInt(this.folds);
                // if there is at least one rating of the user within the fold
                if (this.ratingsFolds[fold].containsKey(userId))
                {
                    // pick a rated item randomly
                    Integer[] itemIds = this.ratingsFolds[fold].get(userId).keySet().toArray(new Integer[] {});
                    int itemPos = r.nextInt(itemIds.length);
                    int itemId = itemIds[itemPos];
                    // move the triple to test set
                    if (!this.testSet.containsKey(userId))
                    {
                        this.testSet.put(userId, new SparseVectorDouble());
                    }
                    this.testSet.get(userId).put(itemId, this.ratingsFolds[fold].get(userId).get(itemId));
                    // remove the triple from train set
                    this.ratingsFolds[fold].get(userId).remove(itemId);
                    if (this.ratingsFolds[fold].get(userId).size() == 0)
                    {
                        this.ratingsFolds[fold].remove(userId);
                    }
                    sampled++;
                }
            }
        }
    }

    public SparseMatrixDouble getRatings(int fold)
    {
        return this.ratingsFolds[fold];
    }

    public SparseMatrixDouble getTrusts()
    {
        return this.trusts;
    }

    public void setTrusts(SparseMatrixDouble trusts)
    {
        this.trusts = trusts;
    }

    public SparseMatrixDouble getTrustedBys()
    {
        return this.trustedBys;
    }

    public void setTrustedBys(SparseMatrixDouble trustedBys)
    {
        this.trustedBys = trustedBys;
    }

    public int getMaxItemId()
    {
        return this.maxItemId;
    }

    public int getMaxUserId()
    {
        return this.maxUserId;
    }

    public void setMaxItemId(int maxItemId)
    {
        this.maxItemId = maxItemId;
    }

    public void setMaxUserId(int maxUserId)
    {
        this.maxUserId = maxUserId;
    }

    public double getMaxRating()
    {
        return this.maxRating;
    }

    public void setMaxRating(double maxRating)
    {
        this.maxRating = maxRating;
    }

    public double getMinRating()
    {
        return this.minRating;
    }

    public void setMinRating(double minRating)
    {
        this.minRating = minRating;
    }

    public SparseMatrixDouble getTestSet()
    {
        return this.testSet;
    }

    public int getFolds()
    {
        return this.folds;
    }

    public void printStatistics(StringBuffer buffer)
    {
        buffer.append("Data set: " + this.title + "\n");
        // folds statistics
        buffer.append("-------------------------------" + "\n");
        buffer.append("FOLD\tUSERS\tRATINGS\tITEMS" + "\n");
        int sumRatings = 0;
        Set<Integer> sumUsers = new HashSet<Integer>();
        Set<Integer> sumItems = new HashSet<Integer>();
        for (int i = 0; i < this.folds; i++)
        {
            int countUsers = 0;
            int countRatings = 0;
            Set<Integer> items = new HashSet<Integer>();
            for (Entry<Integer, SparseVector<Double>> user : this.ratingsFolds[i].entrySet())
            {
                if (!sumUsers.contains(user.getKey()))
                {
                    sumUsers.add(user.getKey());
                }
                countUsers++;
                Iterator<Entry<Integer, Double>> ratingsIterator = user.getValue().entrySet().iterator();
                while (ratingsIterator.hasNext())
                {
                    Entry<Integer, Double> item = ratingsIterator.next();
                    if (!items.contains(item.getKey()))
                    {
                        items.add(item.getKey());
                    }
                    if (!sumItems.contains(item.getKey()))
                    {
                        sumItems.add(item.getKey());
                    }
                    countRatings++;
                    sumRatings++;
                }
            }
            buffer.append("#" + i + ":\t" + countUsers + "\t" + countRatings + "\t" + items.size() + "\n");
        }

        // test set statistics
        int sumTestUsers = 0;
        int sumTestRatings = 0;
        Set<Integer> sumTestItems = new HashSet<Integer>();
        if (this.testSet != null)
        {
            for (Entry<Integer, SparseVector<Double>> user : this.testSet.entrySet())
            {
                sumTestUsers++;
                Iterator<Entry<Integer, Double>> ratingsIterator = user.getValue().entrySet().iterator();
                while (ratingsIterator.hasNext())
                {
                    Entry<Integer, Double> item = ratingsIterator.next();
                    if (!sumTestItems.contains(item.getKey()))
                    {
                        sumTestItems.add(item.getKey());
                    }
                    sumTestRatings++;
                }
            }
        }

        // trust statistics
        int trustsSize = 0;
        for (Entry<Integer, SparseVector<Double>> trust : this.trusts.entrySet())
        {
            trustsSize += trust.getValue().size();
        }
        // trustedBys statistics
        int trustedBysSize = 0;
        for (Entry<Integer, SparseVector<Double>> trustedBy : this.trustedBys.entrySet())
        {
            trustedBysSize += trustedBy.getValue().size();
        }

        buffer.append("-------------------------------" + "\n");
        buffer.append("Number of users:\t" + sumUsers.size() + "\n");
        buffer.append("Number of items:\t" + sumItems.size() + "\n");
        buffer.append("Number of ratings:\t" + sumRatings + "\n");
        buffer.append("Number of test users:\t" + sumTestUsers + "\n");
        buffer.append("Number of test items:\t" + sumTestItems.size() + "\n");
        buffer.append("Number of test ratings:\t" + sumTestRatings + "\n");
        buffer.append("Number of trusts:\t" + trustsSize + "\n");
        buffer.append("Number of trusted-bys:\t" + trustedBysSize + "\n");
        buffer.append("Maximum user ID:\t" + this.maxUserId + "\n");
        buffer.append("Maximum item ID:\t" + this.maxItemId + "\n");
        buffer.append("Maximum rating: \t" + this.maxRating + "\n");
        buffer.append("Minimum rating: \t" + this.minRating + "\n");
        buffer.append("Number of folds:\t" + this.folds + "\n");
    }
}
