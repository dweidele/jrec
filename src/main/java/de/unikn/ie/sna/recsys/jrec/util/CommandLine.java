package de.unikn.ie.sna.recsys.jrec.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Command line parser
 */
public class CommandLine
{

    private final Map<String, String> help;
    private final Map<String, String> value;

    private static final String       DELIMITER = ";,";

    /**
     * Creates a new {@link CommandLine}
     * 
     * @param args
     *            command line arguments
     */
    public CommandLine(String[] args)
    {
        this.value = new HashMap<String, String>();
        this.help = new HashMap<String, String>();

        int i = 0;
        int argc = args.length;
        while (i < argc)
        {
            String s = args[i];
            if ((s = parse_name(s)) != null)
            {
                if (this.value.containsKey(s))
                {
                    throw new RuntimeException("the parameter " + s + " is already specified");
                }
                if ((i + 1) < argc)
                {
                    String s_next = String.valueOf(args[i + 1]);
                    if (parse_name(s_next) == null)
                    {
                        this.value.put(s, s_next);
                        i++;
                    }
                    else
                    {
                        this.value.put(s, "");
                    }
                }
                else
                {
                    this.value.put(s, "");
                }
            }
            else
            {
                throw new RuntimeException("cannot parse " + s);
            }
            i++;
        }
    }

    /**
     * Checks if an input string starts with "-" or "--"
     * 
     * @param s
     *            the input string
     * @return string with cropped "-" or "--" if has been found. null else.
     */
    private String parse_name(String s)
    {
        if ((s.length() > 0) && (s.charAt(0) == '-'))
        {
            if ((s.length() > 1) && (s.charAt(1) == '-'))
            {
                s = s.substring(2);
            }
            else
            {
                s = s.substring(1);
            }
            return s;
        }
        else
        {
            return null;
        }
    }

    public boolean hasParameter(String parameter)
    {
        return this.value.containsKey(parameter);
    }

    public void print_help()
    {
        for (Entry<String, String> pv : this.help.entrySet())
        {
            System.out.println("-" + pv.getKey());
            for (int i = pv.getKey().length() + 1; i < 16; i++)
            {
                System.out.println(" ");
            }
            String s_out = pv.getValue();
            while (s_out.length() > 0)
            {
                if (s_out.length() > (72 - 16))
                {
                    int p = s_out.substring(0, 72 - 16).lastIndexOf(" \t");
                    if (p == 0)
                    {
                        p = 72 - 16;
                    }
                    System.out.println(s_out.substring(0, p));
                    s_out = s_out.substring(p + 1, s_out.length() - p);
                }
                else
                {
                    System.out.println(s_out);
                    s_out = "";
                }
                if (s_out.length() > 0)
                {
                    for (int i = pv.getKey().length() + 1; i < 16; i++)
                    {
                        System.out.println(" ");
                    }
                }
            }
        }
    }

    public String registerParameter(final String parameter, final String help)
    {
        this.help.put(parameter, help);
        return parameter;
    }

    public void checkParameters()
    {
        // make sure there is no parameter specified on the cmdline that is not
        // registered:
        for (Entry<String, String> entry : this.value.entrySet())
        {
            if (!this.help.containsKey(entry.getKey()))
            {
                throw new RuntimeException("the parameter " + entry.getKey() + " does not exist");
            }
        }
    }

    public String getValue(final String parameter)
    {
        return this.value.get(parameter);
    }

    public String getValue(final String parameter, final String default_value)
    {
        if (hasParameter(parameter))
        {
            return this.value.get(parameter);
        }
        else
        {
            return default_value;
        }
    }

    public double getValue(final String parameter, final double default_value)
    {
        if (hasParameter(parameter))
        {
            return Double.parseDouble(this.value.get(parameter));
        }
        else
        {
            return default_value;
        }
    }

    public int getValue(final String parameter, final int default_value)
    {
        if (hasParameter(parameter))
        {
            return Integer.parseInt(this.value.get(parameter));
        }
        else
        {
            return default_value;
        }
    }

    public List<String> getStrValues(final String parameter)
    {
        List<String> result = Util.tokenize(this.value.get(parameter), CommandLine.DELIMITER);
        return result;
    }

    List<Integer> getIntValues(final String parameter)
    {
        List<Integer> result;
        List<String> result_str = getStrValues(parameter);
        result = new ArrayList<Integer>(result_str.size());
        for (int i = 0; i < result.size(); i++)
        {
            result.set(i, Integer.parseInt(result_str.get(i)));
        }
        return result;
    }

    List<Double> getDblValues(final String parameter)
    {
        List<Double> result;
        List<String> result_str = getStrValues(parameter);
        result = new ArrayList<Double>(result_str.size());
        for (int i = 0; i < result.size(); i++)
        {
            result.set(i, Double.parseDouble(result_str.get(i)));
        }
        return result;
    }
}
