// ************************************************************************
//    $Id: CommandLineSpec.java,v 1.1 2002-07-02 15:35:26 wbeebee Exp $
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

// -- jTools Import --
import edu.uci.ece.ac.util.*;

// -- Java JDK Import --
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * This class represent the specification for an application command
 * line arguments. It provides a way of defining both optional and
 * compulsory arguments.
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public class CommandLineSpec {
    
    private Hashtable cmdSpec = new Hashtable();
    private Hashtable reqCmdSpec = new Hashtable();
    
    /**
     * Adds a compulsory command line argument i.e. one that must be
     * present when the application is called.
     *
     * @param argSpec an <code>ArgSpec</code> value
     */
    public void addRequiredArg(ArgSpec argSpec) {
        Assert.preCondition(argSpec != null);
        this.reqCmdSpec.put(argSpec.getName(), argSpec);
    }

    /**
     * Gets the required command line argument having the given name
     *
     * @param name a <code>String</code> value representing the argument name.
     * @return an <code>ArgSpec</code> value that contains the
     * specification for the required argument.
     * @exception AssertionException if some pre/post condition is violated
     */
    public ArgSpec getRequiredArg(String name) {
        Assert.preCondition(name != null);
        return (ArgSpec)this.reqCmdSpec.get(name);
    }

    /**
     * Adds a command line argument i.e. one that must be present when
     * the application is called.
     * @param argSpec an <code>ArgSpec</code> value
     * @exception AssertionException if an error occurs
     */
    public void addArg(ArgSpec argSpec) {
        Assert.preCondition(argSpec != null);
        this.cmdSpec.put(argSpec.getName(), argSpec);
    }


    /**
     * Gets the specification associated with a command line argument
     * having the given name
     *
     * @param name a <code>String</code> valuerepresenting the
     * argument name.
     * @return an <code>ArgSpec</code> value that contains the
     * specification for the required argument.
     */
    public ArgSpec getArg(String name) {
        Assert.preCondition(name != null);
        return (ArgSpec)this.cmdSpec.get(name);
    }


    /**
     * Gets the number of required command line arguments.
     *
     * @return an <code>int</code> value representing the number of
     * required command line args.
     */
    public int getRequiredArgNum() {
        return this.reqCmdSpec.size();
    }

    /**
     * Gets the number of command line argument.
     *
     * @return an <code>int</code> value representing the number of
     * command line args.
     */
    public int getArgNum() {
        return this.cmdSpec.size();
    }

    public String toString() {
        String str = "Command Line Argument Specification:\n   ";
        Enumeration iterator = this.reqCmdSpec.elements();
        while (iterator.hasMoreElements()) 
            str += iterator.nextElement() + " ";

        iterator = this.cmdSpec.elements();

        str += "[ ";
        while (iterator.hasMoreElements())
            str += iterator.nextElement() + " ";

        str += "]";
        return str;
    }
}

