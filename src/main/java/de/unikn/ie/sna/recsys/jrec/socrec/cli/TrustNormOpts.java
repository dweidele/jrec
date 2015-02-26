package de.unikn.ie.sna.recsys.jrec.socrec.cli;

import de.unikn.ie.sna.recsys.jrec.socrec.trustnorm.CorrelationNormalization;
import de.unikn.ie.sna.recsys.jrec.socrec.trustnorm.EqualNormalizer;
import de.unikn.ie.sna.recsys.jrec.socrec.trustnorm.HANormalizer;
import de.unikn.ie.sna.recsys.jrec.socrec.trustnorm.PageRankNormalizer;
import de.unikn.ie.sna.recsys.jrec.socrec.trustnorm.SoRecNormalizer;
import de.unikn.ie.sna.recsys.jrec.socrec.trustnorm.TrustNormalizer;

public enum TrustNormOpts
{
    EQUAL(EqualNormalizer.class),
    SOREC(SoRecNormalizer.class),
    PAGERANK(PageRankNormalizer.class),
    HA(HANormalizer.class),
    CORRELATION(CorrelationNormalization.class);

    private final Class<? extends TrustNormalizer> clazz;

    private TrustNormOpts(Class<? extends TrustNormalizer> clazz)
    {
        this.clazz = clazz;
    }

    public Class<? extends TrustNormalizer> getClazz()
    {
        return this.clazz;
    }

    public static String explain()
    {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < TrustNormOpts.values().length; i++)
        {
            if (i > 0)
            {
                buffer.append(", ");
            }
            buffer.append("'" + TrustNormOpts.values()[i].name() + "'");
        }
        return buffer.toString();
    }
}
