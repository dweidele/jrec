package de.unikn.ie.sna.recsys.jrec.socrec.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.unikn.ie.sna.recsys.jrec.socrec.model.Model;
import de.unikn.ie.sna.recsys.jrec.socrec.trustnorm.Dataset;
import de.unikn.ie.sna.recsys.jrec.socrec.trustnorm.TrustNormalizer;

public class Evaluator
{
    protected Dataset                    dataset;
    private final int                    folds;
    private int                          foldsToEvaluate, features, maxIterations, minIterations, maxThreads, settings;
    protected Map<String, String>        parameterInfo;
    private double[]                     learnRates, alphas, lambdaUIs, lambdaTs;
    private boolean                      biasTerms;
    private double[][][][]               validations;
    private final Class<? extends Model> recClazz;

    //    private final EvaluatorFrame         frame;

    public Evaluator(Class<? extends Model> recClazz, Class<? extends TrustNormalizer> normClazz, SRDataSource ds, int folds,
                     int testSetSize, boolean coldStart)
    {
        this.recClazz = recClazz;
        this.parameterInfo = new HashMap<String, String>();
        this.folds = folds;
        this.parameterInfo.put("folds", String.valueOf(folds));
        this.parameterInfo.put("testSetSize", String.valueOf(testSetSize));
        this.parameterInfo.put("coldStart", String.valueOf(coldStart));
        this.dataset = ds.loadData(this.folds, testSetSize, coldStart);
        try
        {
            normClazz.newInstance().normalize(this.dataset);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        //        this.frame = new EvaluatorFrame();
        //        this.frame.setVisible(true);
    }

    public void setMaxIterations(int maxIterations)
    {
        this.maxIterations = maxIterations;
        this.parameterInfo.put("maxIterations", String.valueOf(maxIterations));
    }

    public void setMinIterations(int minIterations)
    {
        this.minIterations = minIterations;
        this.parameterInfo.put("minIterations", String.valueOf(minIterations));

    }

    public void setFoldsToEvaluate(int foldsToEvaluate)
    {
        this.foldsToEvaluate = foldsToEvaluate;
        this.parameterInfo.put("foldsToEvaluate", String.valueOf(foldsToEvaluate));
    }

    public void setFeatures(int features)
    {
        this.features = features;
        this.parameterInfo.put("features", String.valueOf(features));
    }

    public void setLearnRates(double[] learnRates)
    {
        this.learnRates = learnRates;
    }

    public void setLambdaUIs(double[] lambdaUIs)
    {
        this.lambdaUIs = lambdaUIs;
    }

    public void setLambdaTs(double[] lambdaTs)
    {
        this.lambdaTs = lambdaTs;
    }

    public void setAlphas(double[] alphas)
    {
        this.alphas = alphas;
    }

    public void setBiasTerms(boolean biasTerms)
    {
        this.biasTerms = biasTerms;
        this.parameterInfo.put("biasTerms", String.valueOf(biasTerms));
    }

    public void setMaxThreads(int maxThreads)
    {
        this.maxThreads = maxThreads;
        this.parameterInfo.put("maxThreads", String.valueOf(maxThreads));
    }

    public void evaluate(final StringBuffer evaluation, final StringBuffer statistic)
    {
        generateSettings();
        validateSettings(evaluation);
        printStats(statistic);
        determineBestAndTestSetting(statistic);
    }

    private void determineBestAndTestSetting(final StringBuffer statistic)
    {
        // initialize with first setting before first iteration
        int bestIteration = this.minIterations - 1, bestSetting = 0;
        double bestFoldSumRmseValidate = 0;
        for (int fold = 0; fold < this.validations.length; fold++)
        {
            bestFoldSumRmseValidate += this.validations[fold][bestSetting][bestIteration][0];
        }
        // search for best setting and iteration over all folds
        double foldSumRmseValidate = 0;
        for (int setting = 0; setting < this.settings; setting++)
        {
            for (int iteration = 0; iteration < this.maxIterations; iteration++)
            {
                if (iteration < this.minIterations - 1)
                {
                    continue;
                }
                foldSumRmseValidate = 0;
                for (int fold = 0; fold < this.validations.length; fold++)
                {
                    foldSumRmseValidate += this.validations[fold][setting][iteration][0];
                }
                if (foldSumRmseValidate < bestFoldSumRmseValidate)
                {
                    bestIteration = iteration;
                    bestSetting = setting;
                    bestFoldSumRmseValidate = foldSumRmseValidate;
                }
            }
        }
        println(statistic, "Best setting: " + "LR=" + this.learnRates[bestSetting] + ", LUI=" + this.lambdaUIs[bestSetting] + ", LT="
                           + this.lambdaTs[bestSetting] + ", A=" + this.alphas[bestSetting]);
        println(statistic, "RMSE(Ø," + +(bestIteration + 1) + ")=" + (bestFoldSumRmseValidate / this.foldsToEvaluate) + "\n");

        // train test model
        println(statistic, "----------------------- TEST --------------------------");
        if (this.dataset.getTestSet() == null)
        {
            println(statistic, "no test set");
            return;
        }
        final int finalBestSetting = bestSetting;
        final int finalBestIteration = bestIteration;
        final double[] testRMSEs = new double[this.foldsToEvaluate];
        for (int nonTrainFold = 0; nonTrainFold < this.foldsToEvaluate; nonTrainFold++)
        {
            final int finalNonTrainFold = nonTrainFold;
            Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            Trainer trainer = new Trainer();
                            Criterion opt = new Criterion();
                            Model model = newModel(Evaluator.this.learnRates[finalBestSetting], Evaluator.this.lambdaUIs[finalBestSetting],
                                                   Evaluator.this.lambdaTs[finalBestSetting], Evaluator.this.alphas[finalBestSetting],
                                                   Evaluator.this.biasTerms);
                            for (int i = 0; i < finalBestIteration + 1; i++)
                            {
                                trainer.train(model, Evaluator.this.dataset, finalNonTrainFold);
                            }

                            // test test model
                            testRMSEs[finalNonTrainFold] = opt.rmseTest(model, Evaluator.this.dataset);
                        }
                        catch (Throwable t)
                        {
                            t.printStackTrace();
                            println(statistic, t.getMessage());
                        }
                    }
                };
            t.start();
            awaitThreads(this.maxThreads);
        }
        awaitThreads(1);
        double testRMSE = 0;
        for (int nonTrainFold = 0; nonTrainFold < this.foldsToEvaluate; nonTrainFold++)
        {
            println(statistic, "RMSE(" + nonTrainFold + "," + (bestIteration + 1) + ")=" + testRMSEs[nonTrainFold]);
            testRMSE += testRMSEs[nonTrainFold];
        }
        testRMSE /= this.foldsToEvaluate;
        println(statistic, "RMSE(Ø," + (bestIteration + 1) + ")=" + testRMSE);
    }

    private void printStats(final StringBuffer statistic)
    {
        println(statistic, "Summary of '" + newModel(0, 0, 0, 0, false).getTitle() + "':");
        println(statistic, "------------------- PARAMETERS ------------------------");
        for (Entry<String, String> info : this.parameterInfo.entrySet())
        {
            println(statistic, info.getKey() + "=" + info.getValue());
        }
        println(statistic, "--------------------- DATASET -------------------------");
        this.dataset.printStatistics(statistic);
        println(statistic, "-------------------- VALIDATION -----------------------");
    }

    private void validateSettings(final StringBuffer evaluation)
    {
        this.validations = new double[this.foldsToEvaluate][this.learnRates.length][this.maxIterations][2];
        println(evaluation,
                "FOLD;SETTING;ITER;RMSE(validate);IMPROVEMENT(validate);RMSE(train);IMPROVEMENT(train);EVAL_MILLISEC(validate);TRAIN_MILLISEC(train)");
        // for each fold
        for (int validationFold = 0; validationFold < this.foldsToEvaluate; validationFold++)
        {
            //            this.frame.setValidationFold(validationFold);
            final int finalValidationFold = validationFold;
            for (int setting = 0; setting < this.settings; setting++)
            {
                //                this.frame.setSettingId(setting);
                final int finalSetting = setting;
                Thread t = new Thread()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                Criterion opt = new Criterion();
                                Trainer trainer = new Trainer();
                                // new model
                                Model model = newModel(Evaluator.this.learnRates[finalSetting], Evaluator.this.lambdaUIs[finalSetting],
                                                       Evaluator.this.lambdaTs[finalSetting], Evaluator.this.alphas[finalSetting],
                                                       Evaluator.this.biasTerms);
                                trainer.blackout(model, Evaluator.this.dataset, finalValidationFold);
                                // initial validation
                                double lastRmseValidate, lastRmseTrain, currentRmseValidate, currentRmseTrain, rmseValidateImprovement, rmseTrainImprovement;
                                long tTime = 0, vTime = 0;
                                vTime = System.currentTimeMillis();
                                lastRmseValidate = opt.rmseValidate(model, Evaluator.this.dataset, finalValidationFold);
                                vTime = System.currentTimeMillis() - vTime;
                                lastRmseTrain = opt.rmseTrain(model, Evaluator.this.dataset, finalValidationFold);
                                println(evaluation, finalValidationFold + ";" + finalSetting + ";0;" + lastRmseValidate + ";;"
                                                    + lastRmseTrain + ";" + vTime + ";" + tTime);
                                // for each iteration
                                for (int iteration = 0; iteration < Evaluator.this.maxIterations; iteration++)
                                {
                                    // train model
                                    tTime = System.currentTimeMillis();
                                    trainer.train(model, Evaluator.this.dataset, finalValidationFold);
                                    tTime = System.currentTimeMillis() - tTime;
                                    // validate model
                                    vTime = System.currentTimeMillis();
                                    currentRmseValidate = opt.rmseValidate(model, Evaluator.this.dataset, finalValidationFold);
                                    vTime = System.currentTimeMillis() - vTime;
                                    currentRmseTrain = opt.rmseTrain(model, Evaluator.this.dataset, finalValidationFold);
                                    // calculate improvement
                                    rmseValidateImprovement = lastRmseValidate - currentRmseValidate;
                                    rmseTrainImprovement = lastRmseTrain - currentRmseTrain;
                                    // save validation
                                    println(evaluation, finalValidationFold + ";" + finalSetting + ";" + (iteration + 1) + ";"
                                                        + currentRmseValidate + ";" + rmseValidateImprovement + ";" + currentRmseTrain
                                                        + ";" + rmseTrainImprovement + ";" + vTime + ";" + tTime);
                                    setValidation(finalValidationFold, finalSetting, iteration, currentRmseValidate, currentRmseTrain);
                                    lastRmseValidate = currentRmseValidate;
                                    lastRmseTrain = currentRmseTrain;
                                    //                                    // update frame
                                    //                                    if (iteration % 100 == 0)
                                    //                                    {
                                    //                                        Evaluator.this.frame.setIteration(iteration);
                                    //                                        Evaluator.this.frame.setRmseTrain(currentRmseTrain);
                                    //                                        Evaluator.this.frame.setRmseTrainDiff(rmseTrainImprovement);
                                    //                                        Evaluator.this.frame.setRmseValidate(currentRmseValidate);
                                    //                                        Evaluator.this.frame.setRmseValidateDiff(rmseValidateImprovement);
                                    //                                        Evaluator.this.frame.repaint();
                                    //                                    }
                                }
                            }
                            catch (Throwable t)
                            {
                                t.printStackTrace();
                                println(evaluation, t.getMessage());
                            }
                        };
                    };
                t.start();
                awaitThreads(this.maxThreads);
            }
        }
        awaitThreads(1);
    }

    private void generateSettings()
    {
        this.settings = 0;
        if (this.lambdaUIs.length < 1 || this.lambdaTs.length < 1 || this.alphas.length < 1 || this.learnRates.length < 1)
        {
            throw new RuntimeException("one of setting arrays is too short");
        }
        List<Double> lr = new ArrayList<Double>();
        List<Double> lui = new ArrayList<Double>();
        List<Double> lt = new ArrayList<Double>();
        List<Double> a = new ArrayList<Double>();
        for (double learnRate : this.learnRates)
        {
            for (double lambdaUI : this.lambdaUIs)
            {
                for (double lambdaT : this.lambdaTs)
                {
                    for (double alpha : this.alphas)
                    {
                        this.settings++;
                        lr.add(learnRate);
                        lui.add(lambdaUI);
                        lt.add(lambdaT);
                        a.add(alpha);
                    }
                }
            }
        }
        this.learnRates = toArray(lr);
        this.parameterInfo.put("learnRate", Arrays.toString(this.learnRates));
        this.lambdaUIs = toArray(lui);
        this.parameterInfo.put("lambdaUIs", Arrays.toString(this.lambdaUIs));
        this.lambdaTs = toArray(lt);
        this.parameterInfo.put("lambdaTs", Arrays.toString(this.lambdaTs));
        this.alphas = toArray(a);
        this.parameterInfo.put("alphas", Arrays.toString(this.alphas));
        System.out.println("Generated " + this.settings + " settings to be validated.");
    }

    private double[] toArray(List<Double> list)
    {
        double[] res = new double[list.size()];
        for (int i = 0; i < list.size(); i++)
        {
            res[i] = list.get(i);
        }
        return res;
    }

    private synchronized void setValidation(int fold, int learnRate, int iteration, double rmseValidate, double rmseTrain)
    {
        Evaluator.this.validations[fold][learnRate][iteration][0] = rmseValidate;
        Evaluator.this.validations[fold][learnRate][iteration][1] = rmseTrain;
    }

    private void awaitThreads(int i)
    {
        while (Thread.activeCount() > i)
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private synchronized Model newModel(double learnRate, double lambdaUI, double lambdaT, double alpha, boolean biasTerms)
    {
        try
        {
            Model rec = this.recClazz.newInstance();
            rec.init(this.dataset.getMaxUserId() + 1, this.dataset.getMaxItemId() + 1, this.features, learnRate, lambdaUI, lambdaUI,
                     lambdaT, alpha, this.dataset.getTrusts(), this.dataset.getTrustedBys(), this.dataset.getMinRating(),
                     this.dataset.getMaxRating(), biasTerms);
            return rec;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void println(StringBuffer buffer, String line)
    {
        System.out.println(line);
        buffer.append(line + "\n");
    }

}
