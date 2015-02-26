package de.unikn.ie.sna.recsys.jrec.socrec.evaluation;

import de.unikn.ie.sna.recsys.jrec.socrec.trustnorm.Dataset;

public abstract class SRDataSource
{
    public abstract Dataset loadData(int folds, int validationSize, boolean coldStart);

    public abstract String getName();
}
