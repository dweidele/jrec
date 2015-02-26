package de.unikn.ie.sna.recsys.jrec.socrec.evaluation;

import java.util.Map.Entry;

import de.unikn.ie.sna.recsys.jrec.socrec.model.Model;
import de.unikn.ie.sna.recsys.jrec.socrec.trustnorm.Dataset;
import de.unikn.ie.sna.recsys.jrec.util.SparseVector;

public class Criterion
{
    public double rmseTrain(Model model, Dataset dataset, int validationFold)
    {
        double rmse;
        double sum = 0;
        int count = 0;
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
                    rmse = model.predict(users.getKey(), ratings.getKey());
                    // TODO should not happen
                    // rmse = Math.min(dataset.getMaxRating(), rmse);
                    // rmse = Math.max(dataset.getMinRating(), rmse);
                    rmse = ratings.getValue() - rmse;
                    rmse = Math.pow(rmse, 2);
                    sum += rmse;
                    count++;
                }
            }
        }
        return Math.sqrt(sum / count);
    }

    public double rmseValidate(Model model, Dataset dataset, int validationFold)
    {
        double rmse;
        double sum = 0;
        int count = 0;
        for (int fold = 0; fold < dataset.getFolds(); fold++)
        {
            if (fold != validationFold)
            {
                continue;
            }
            for (Entry<Integer, SparseVector<Double>> users : dataset.getRatings(fold).entrySet())
            {
                for (Entry<Integer, Double> ratings : users.getValue().entrySet())
                {
                    rmse = model.predict(users.getKey(), ratings.getKey());
                    // TODO should not happen
                    // rmse = Math.min(dataset.getMaxRating(), rmse);
                    // rmse = Math.max(dataset.getMinRating(), rmse);
                    rmse = ratings.getValue() - rmse;
                    rmse = Math.pow(rmse, 2);
                    sum += rmse;
                    count++;
                }
            }
        }
        return Math.sqrt(sum / count);
    }

    public double rmseTest(Model model, Dataset dataset)
    {
        double rmse;
        double sum = 0;
        int count = 0;
        for (int fold = 0; fold < dataset.getFolds(); fold++)
        {
            for (Entry<Integer, SparseVector<Double>> users : dataset.getTestSet().entrySet())
            {
                for (Entry<Integer, Double> ratings : users.getValue().entrySet())
                {
                    rmse = model.predict(users.getKey(), ratings.getKey());
                    // TODO should not happen
                    // rmse = Math.min(dataset.getMaxRating(), rmse);
                    //rmse = Math.max(dataset.getMinRating(), rmse);
                    rmse = ratings.getValue() - rmse;
                    rmse = Math.pow(rmse, 2);
                    sum += rmse;
                    count++;
                }
            }
        }
        return Math.sqrt(sum / count);
    }
}
