package de.unikn.ie.sna.recsys.jrec.socrec.model;

import de.unikn.ie.sna.recsys.jrec.util.SparseMatrixDouble;

public interface Model
{
    double predict(int user, int item);

    String getTitle();

    void init(int users, int items, int features, double learnRate, double lambdaU, double lambdaI, double lambdaT, double alpha,
              SparseMatrixDouble trusts, SparseMatrixDouble trustedBys, double minRating, double maxRating, boolean biasTerms);

    void blackoutUser(int user);

    void blackoutItem(int item);

    void learn(int user, int item, double rating);

    void beforeLearn(double average);

    void afterLearn();

    void print();
}
