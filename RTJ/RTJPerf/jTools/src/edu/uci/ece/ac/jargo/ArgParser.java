// ************************************************************************
//    $Id: ArgParser.java,v 1.1 2002-07-02 15:35:26 wbeebee Exp $
// ************************************************************************
//
//                               jTools
//
//               Copyright (C) 2001-2002 by Angelo Corsaro.
//                         <corsaro@ece.uci.edu>
//                          All Rights Reserved.
//
//   Permission to use, copy, modify, and distribute this software and
//   its  documentation for any purpose is hereby  granted without fee,
//   provided that the above copyright notice appear in all copies and
//   that both that copyright notice and this permission notice appear
//   in  supporting  documentation. I don't make  any  representations
//   about the  suitability  of this  software for any  purpose. It is
//   provided "as is" without express or implied warranty.
//
//
// *************************************************************************
//  
// *************************************************************************

package edu.uci.ece.ac.jargo;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Properties;

import edu.uci.ece.ac.util.*;

/**
 * This is an utility class that provide a way of parsing command line
 * arguments. It provide a representation of command line argument as
 * collection of <code>CommandLineArgument</code>
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public class ArgParser {
    
    public static String GNU_ARGUMENT_PREFIX = "--";

    private String argumentPrefix = GNU_ARGUMENT_PREFIX;
    private final Hashtable argList = new Hashtable();
    private CommandLineSpec commandLineSpec;
    private HelpHandler helpHandler;

    
    public ArgParser(CommandLineSpec spec,
                     HelpHandler helpHandler,
                     String argumentPrefix) 
    {
        Assert.preCondition(argumentPrefix != null && spec != null);
        this.argumentPrefix = argumentPrefix;
        this.commandLineSpec = spec;
        this.helpHandler = helpHandler;
    }
    
    public ArgParser(CommandLineSpec spec, HelpHandler helpHandler){
        this(spec, helpHandler, GNU_ARGUMENT_PREFIX);
    }

    public ArgParser(CommandLineSpec spec) {
        this(spec, new HelpHandler(spec.toString()), GNU_ARGUMENT_PREFIX);
    }
    
    public void parse(String[] args) throws Exception {
        Assert.preCondition(args != null);

        if (this.commandLineSpec.getRequiredArgNum() > args.length)
            this.helpHandler.handleHelp();
        
        int compulsoryArgs = 0;
        int i = 0;
        ArgSpec spec;
        String arg;
            
        while (i < args.length) {
            while (args[i].startsWith(argumentPrefix) == false)
                i++;

            // Now we have the first desired arg.
            arg = args[i].substring(argumentPrefix.length());

            if (this.isHelpArg(arg))
                this.helpHandler.handleHelp();
            
            spec = this.commandLineSpec.getArg(arg);

            if (spec == null) {
                spec = this.commandLineSpec.getRequiredArg(arg);
                if (spec == null)
                    break;

                // Make sure that the repetition of the same argument
                // does not get counted more than once.
                if (this.argList.get(arg) == null)
                    compulsoryArgs++;
            }
            
            ArgValue argVal = new ArgValue(spec, args, ++i, spec.argNum());
            this.argList.put(spec.getName(), argVal);
            argVal = null;
            i += spec.argNum();
        }
        
        if (compulsoryArgs < this.commandLineSpec.getRequiredArgNum()) {
            this.helpHandler.handleHelp();
            //   throw new InvalidArgumentValueException("Required Argument Missing!");
        }
    }

    public ArgValue getArg(String name) {
        Assert.preCondition(name != null);
        return (ArgValue)this.argList.get(name);
    }

    public ArgValue getArg(ArgSpec spec) throws AssertionException {
        Assert.preCondition(spec != null);
        return (ArgValue)this.argList.get(spec.getName());
    }
    
    private boolean isHelpArg(String str) {
        return str.equals("help") || str.equals("h") || str.equals("?");
    }

    public boolean isArgDefined(String argName) {
        return (this.argList.get(argName) != null);
    }
}
