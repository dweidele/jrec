package de.unikn.ie.sna.recsys.jrec.socrec.evaluation;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import de.unikn.ie.sna.recsys.jrec.socrec.model.Model;
import de.unikn.ie.sna.recsys.jrec.socrec.trustnorm.Dataset;
import de.unikn.ie.sna.recsys.jrec.util.SparseVector;

public class Trainer
{
    public void train(Model model, Dataset dataset, int validationFold)
    {
        // determine average for ratings to be trained
        double average = 0, count = 0;
        for (int fold = 0; fold < dataset.getFolds(); fold++)
        {
            if (fold == validationFold)
            {
                continue;
            }
            for (Entry<Integer, SparseVector<Double>> users : dataset.getRatings(fold).entrySet())
            {
                for (Entry<Integer, Double> ratings : users.getValue().entrySet())
                {
                    average += ratings.getValue();
                    count++;
                }
            }
        }
        model.beforeLearn(average / count);
        for (int fold = 0; fold < dataset.getFolds(); fold++)
        {
            if (fold == validationFold)
            {
                continue;
            }
            for (Entry<Integer, SparseVector<Double>> users : dataset.getRatings(fold).entrySet())
            {
                for (Entry<Integer, Double> ratings : users.getValue().entrySet())
                {
                    model.learn(users.getKey(), ratings.getKey(), ratings.getValue());
                }
            }
        }
        model.afterLearn();
    }

    public void blackout(Model model, Dataset dataset, int validationFold)
    {
        boolean trained;
        // users
        for (int user = 0; user < dataset.getMaxUserId(); user++)
        {
            trained = false;
            for (int fold = 0; fold < dataset.getFolds(); fold++)
            {
                if (fold == validationFold)
                {
                    continue;
                }
                if (dataset.getRatings(fold).containsKey(user))
                {
                    trained = true;
                    break;
                }
            }
            if (!trained)
            {
                model.blackoutUser(user);
            }
        }
        // items
        Set<Integer> items = new HashSet<Integer>();
        for (int fold = 0; fold < dataset.getFolds(); fold++)
        {
            if (fold == validationFold)
            {
                continue;
            }
            for (SparseVector<Double> rating : dataset.getRatings(fold).values())
            {
                items.addAll(rating.keySet());
            }
        }
        for (int item = 0; item < dataset.getMaxItemId(); item++)
        {
            trained = false;
            if (items.contains(item))
            {
                trained = true;
            }
            if (!trained)
            {
                model.blackoutItem(item);
            }
        }

    }
}
