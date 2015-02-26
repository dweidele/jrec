package de.unikn.ie.sna.recsys.jrec.socrec.cli;

import de.unikn.ie.sna.recsys.jrec.socrec.model.BaseMF;
import de.unikn.ie.sna.recsys.jrec.socrec.model.RSTE;
import de.unikn.ie.sna.recsys.jrec.socrec.model.SVDPP;
import de.unikn.ie.sna.recsys.jrec.socrec.model.SoRec;
import de.unikn.ie.sna.recsys.jrec.socrec.model.SocialMF;

public enum ModelOpts
{
    BASE_MF(BaseMF.class),
    SOCIAL_MF(SocialMF.class),
    RSTE(RSTE.class),
    SOREC(SoRec.class),
    SVDPP(SVDPP.class);

    private final Class<? extends de.unikn.ie.sna.recsys.jrec.socrec.model.Model> clazz;

    private ModelOpts(Class<? extends de.unikn.ie.sna.recsys.jrec.socrec.model.Model> clazz)
    {
        this.clazz = clazz;
    }

    public Class<? extends de.unikn.ie.sna.recsys.jrec.socrec.model.Model> getClazz()
    {
        return this.clazz;
    }

    public static String explain()
    {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < ModelOpts.values().length; i++)
        {
            if (i > 0)
            {
                buffer.append(", ");
            }
            buffer.append("'" + ModelOpts.values()[i].name() + "'");
        }
        return buffer.toString();
    }
}
