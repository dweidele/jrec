package de.unikn.ie.sna.recsys.jrec.socrec;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import de.unikn.ie.sna.recsys.jrec.socrec.cli.ModelOpts;
import de.unikn.ie.sna.recsys.jrec.socrec.cli.TrustNormOpts;
import de.unikn.ie.sna.recsys.jrec.socrec.evaluation.Evaluator;
import de.unikn.ie.sna.recsys.jrec.socrec.evaluation.SRDataSource;
import de.unikn.ie.sna.recsys.jrec.socrec.trustnorm.TrustNormalizer;

@SuppressWarnings("static-access")
public class MainCLI
{

    private static final String MODEL                  = "M";
    private static final String DATASOURCE             = "DS";
    private static final String TRUSTNORM              = "TN";
    private static final String MAX_THREADS            = "MT";
    private static final String FILE_RATINGS           = "FR";
    private static final String TEST_SIZE              = "TS";
    private static final String CROSS_VALIDATION_FOLDS = "CF";
    private static final String LATENT_FEATURES        = "LF";
    private static final String MAX_ITERATIONS         = "MI";
    private static final String LEARN_RATE             = "LR";
    private static final String LAMBDA_UI              = "LUI";
    private static final String FILE_LINKS             = "FL";
    private static final String LAMBDA_T               = "LT";
    private static final String ALPHA                  = "A";
    private static final String BIAS_TERMS             = "BT";
    private static final String COLD_START             = "CS";

    private static final Option OPT_MODEL              = OptionBuilder.withArgName("The model to be evaluated. Set to one of "
                                                                                           + ModelOpts.explain() + ".").isRequired()
                                                                      .hasArgs().withLongOpt("model").create(MainCLI.MODEL);
    private static final Option OPT_DATASOURCE         = OptionBuilder.withArgName("The full qualified class of data source to read input files with.").isRequired()
                                                                      .hasArgs().withLongOpt("datasource").create(MainCLI.DATASOURCE);
    private static final Option OPT_TRUSTNORM          = OptionBuilder.withArgName("The trust norm to evaluate with. Set to one of "
                                                                                           + TrustNormOpts.explain() + ".").isRequired()
                                                                      .hasArgs().withLongOpt("trustnorm").create(MainCLI.TRUSTNORM);
    private static final Option OPT_MAX_THREADS        = OptionBuilder.withArgName("Maximum amount of threads to be executed in parallel.")
                                                                      .isRequired().hasArg().withLongOpt("max_threads")
                                                                      .create(MainCLI.MAX_THREADS);
    private static final Option OPT_FILE_RATINGS       = OptionBuilder.withArgName("Path to the ratings file.").isRequired().hasArg()
                                                                      .withLongOpt("ratings").create(MainCLI.FILE_RATINGS);
    private static final Option OPT_TEST_SIZE          = OptionBuilder.withArgName("Amount of ratings to split into test set.")
                                                                      .isRequired().hasArg().withLongOpt("test_size")
                                                                      .create(MainCLI.TEST_SIZE);
    private static final Option OPT_FOLDS              = OptionBuilder.withArgName("Number of cross-validation folds.").isRequired()
                                                                      .hasArg().withLongOpt("folds").create(MainCLI.CROSS_VALIDATION_FOLDS);
    private static final Option OPT_FEATURES           = OptionBuilder.withArgName("Number of latent features.").isRequired().hasArg()
                                                                      .withLongOpt("features").create(MainCLI.LATENT_FEATURES);
    private static final Option OPT_MAX_ITRATIONS      = OptionBuilder.withArgName("Number of maximum iterations.").isRequired().hasArg()
                                                                      .withLongOpt("iterations").create(MainCLI.MAX_ITERATIONS);
    private static final Option OPT_LEARN_RATE         = OptionBuilder.withArgName("Learn rate of gradient descent.").isRequired().hasArg()
                                                                      .withLongOpt("learn_rate").create(MainCLI.LEARN_RATE);
    private static final Option OPT_LAMBDA_UI          = OptionBuilder.withArgName("Lambda for user and item feature matrix regularization.")
                                                                      .isRequired().hasArg().withLongOpt("lambda_ui")
                                                                      .create(MainCLI.LAMBDA_UI);
    private static final Option OPT_FILE_LINKS         = OptionBuilder.withArgName("Path to the links file.").isRequired().hasArg()
                                                                      .withLongOpt("links").create(MainCLI.FILE_LINKS);
    private static final Option OPT_ALPHA              = OptionBuilder.withArgName("Alpha for RSTE model. In case of SoRec this is used as lambdaV. ")
                                                                      .isRequired().hasArg().withLongOpt("alphas").create(MainCLI.ALPHA);
    private static final Option OPT_LAMBDA_T           = OptionBuilder.withArgName("Lambda for trust matrix regularization. In case of SVDPP this is used as lambdaJ.")
                                                                      .isRequired().hasArg().withLongOpt("lambda_t")
                                                                      .create(MainCLI.LAMBDA_T);
    private static final Option OPT_BIAS_TERMS         = OptionBuilder.withArgName("Activate user- and item-bias terms. ").isRequired()
                                                                      .hasArg().withLongOpt("biasTerms").create(MainCLI.BIAS_TERMS);
    private static final Option OPT_COLD_START         = OptionBuilder.withArgName("Make test set of 100% cold start users.").isRequired()
                                                                      .hasArg().withLongOpt("cold_start").create(MainCLI.COLD_START);

    public static void main(String[] args)
        throws Exception
    {
        // prepare command line options
        Options options = new Options();
        options.addOption(MainCLI.OPT_MODEL);
        options.addOption(MainCLI.OPT_DATASOURCE);
        options.addOption(MainCLI.OPT_TRUSTNORM);
        options.addOption(MainCLI.OPT_MAX_THREADS);
        options.addOption(MainCLI.OPT_FILE_RATINGS);
        options.addOption(MainCLI.OPT_TEST_SIZE);
        options.addOption(MainCLI.OPT_FOLDS);
        options.addOption(MainCLI.OPT_FEATURES);
        options.addOption(MainCLI.OPT_MAX_ITRATIONS);
        options.addOption(MainCLI.OPT_LEARN_RATE);
        options.addOption(MainCLI.OPT_LAMBDA_UI);
        options.addOption(MainCLI.OPT_FILE_LINKS);
        options.addOption(MainCLI.OPT_LAMBDA_T);
        options.addOption(MainCLI.OPT_ALPHA);
        options.addOption(MainCLI.OPT_BIAS_TERMS);
        options.addOption(MainCLI.OPT_COLD_START);

        // start command line
        CommandLine cl;
        try
        {
            GnuParser parser = new GnuParser();
            cl = parser.parse(options, args);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage() + "\n");
            // print help
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("jrec", options);
            return;
        }

        // fetch parameters
        Class<? extends de.unikn.ie.sna.recsys.jrec.socrec.model.Model> model = MainCLI.getOptionModel(cl, MainCLI.MODEL);
        Class<? extends SRDataSource> datasource = MainCLI.getOptionDatasource(cl, MainCLI.DATASOURCE);
        Class<? extends TrustNormalizer> trustnorm = MainCLI.getOptionTrustNorm(cl, MainCLI.TRUSTNORM);
        String ratings = MainCLI.getOptionString(cl, MainCLI.FILE_RATINGS);
        String links = MainCLI.getOptionString(cl, MainCLI.FILE_LINKS);
        SRDataSource ds = datasource.getConstructor(String.class, String.class).newInstance(ratings, links);
        int maxThreads = MainCLI.getOptionInt(cl, MainCLI.MAX_THREADS);
        int testSetSize = MainCLI.getOptionInt(cl, MainCLI.TEST_SIZE);
        int folds = MainCLI.getOptionInt(cl, MainCLI.CROSS_VALIDATION_FOLDS);
        int features = MainCLI.getOptionInt(cl, MainCLI.LATENT_FEATURES);
        int maxIterations = MainCLI.getOptionInt(cl, MainCLI.MAX_ITERATIONS);
        double[] learnRates = MainCLI.getOptionDoubleArray(cl, MainCLI.LEARN_RATE);
        double[] lambdaUIs = MainCLI.getOptionDoubleArray(cl, MainCLI.LAMBDA_UI);
        double[] lambdaTs = MainCLI.getOptionDoubleArray(cl, MainCLI.LAMBDA_T);
        double[] alphas = MainCLI.getOptionDoubleArray(cl, MainCLI.ALPHA);
        boolean biasTerms = MainCLI.getOptionBoolean(cl, MainCLI.BIAS_TERMS);
        boolean coldStart = MainCLI.getOptionBoolean(cl, MainCLI.COLD_START);

        // initialize evaluator
        Evaluator e = new Evaluator(model, trustnorm, ds, folds, testSetSize, coldStart);
        e.setMaxIterations(maxIterations);
        e.setMinIterations(1);
        e.setFoldsToEvaluate(folds);
        e.setFeatures(features);
        e.setLearnRates(learnRates);
        e.setAlphas(alphas);
        e.setLambdaUIs(lambdaUIs);
        e.setLambdaTs(lambdaTs);
        e.setMaxThreads(maxThreads);
        e.setBiasTerms(biasTerms);

        // start evaluation
        StringBuffer evaluation = new StringBuffer();
        StringBuffer statistic = new StringBuffer();
        e.evaluate(evaluation, statistic);

        // print results
        MainCLI.printResult(model.getSimpleName() + "-" + ds.getName() + "-statistic", statistic.toString());
        MainCLI.printResult(model.getSimpleName() + "-" + ds.getName() + "-evaluation", evaluation.toString());
    }

    private static void printResult(String suffix, String result)
    {
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss-");
            String date = sdf.format(new Date());
            FileWriter writer = new FileWriter(new File("../result/" + date + suffix + ".txt"));
            writer.write(result);
            writer.flush();
            writer.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static double[] getOptionDoubleArray(CommandLine cl, String option)
    {
        String value = cl.getOptionValue(option);
        String[] parts = value.split(",");
        double[] result = new double[parts.length];
        for (int i = 0; i < parts.length; i++)
        {
            result[i] = Double.parseDouble(parts[i]);
        }
        return result;
    }

    private static String getOptionString(CommandLine cl, String option)
    {
        return cl.getOptionValue(option);
    }

    private static boolean getOptionBoolean(CommandLine cl, String option)
    {
        return Boolean.parseBoolean(cl.getOptionValue(option));
    }

    private static int getOptionInt(CommandLine cl, String option)
    {
        return Integer.parseInt(cl.getOptionValue(option));
    }

    private static Class<? extends de.unikn.ie.sna.recsys.jrec.socrec.model.Model> getOptionModel(CommandLine cl, String option)
    {
        return ModelOpts.valueOf(cl.getOptionValue(option)).getClazz();
    }

    private static Class<? extends de.unikn.ie.sna.recsys.jrec.socrec.trustnorm.TrustNormalizer> getOptionTrustNorm(CommandLine cl,
                                                                                                                    String option)
    {
        return TrustNormOpts.valueOf(cl.getOptionValue(option)).getClazz();
    }

    @SuppressWarnings("unchecked")
	private static Class<? extends SRDataSource> getOptionDatasource(CommandLine cl, String option) throws Exception
    {
        return (Class<? extends SRDataSource>)Class.forName(cl.getOptionValue(option));
    }
}
