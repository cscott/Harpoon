// Flags.java, created Wed Jul 20 06:31:03 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;

import harpoon.Util.Options.Option;

/**
 * <code>Flags</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: Flags.java,v 1.1 2005-08-10 02:58:19 salcianu Exp $
 */
public abstract class Flags {

    static boolean VERBOSE_TYPE_FILTER = false;

    public static boolean SHOW_INTRA_PROC_RESULTS = false;

    static boolean SHOW_INTRA_PROC_CONSTRAINTS = false;

    static boolean SHOW_TRIM_STATS = false;

    static boolean SHOW_METHOD_SCC = false;

    static boolean VERBOSE_CLONE = false;

    static boolean VERBOSE_ARRAYCOPY = false;

    static boolean VERBOSE_CALL = false;

    public static int     MAX_INTRA_SCC_ITER = 1000;

    public static boolean FLOW_SENSITIVITY = true;

    static boolean USE_FRESHEN_TRICK = false;

    static List<Option> getOptions() {
	List<Option> opts = new LinkedList<Option>();
	opts.add(new Option("pa2:verbose-type-filter", "") {
	    public void action() {
		VERBOSE_TYPE_FILTER = true;
	    }});
	opts.add(new Option("pa2:show-intra-proc-results", "") {
	    public void action() {
		SHOW_INTRA_PROC_RESULTS = true;
	    }});
	opts.add(new Option("pa2:show-intra-proc-constraints", "") {
	    public void action() {
		SHOW_INTRA_PROC_CONSTRAINTS = true;
	    }});
	opts.add(new Option("pa2:show-trim-stats") {
	    public void action() {
		SHOW_TRIM_STATS = true;
	    }});
	opts.add(new Option("pa2:show-method-scc", "") {
	    public void action() {
		SHOW_METHOD_SCC = true;
	    }});
	opts.add(new Option("pa2:verbose-clone", "") {
	    public void action() {
		VERBOSE_CLONE = true;
	    }});
	opts.add(new Option("pa2:verbose-arraycopy", "") {
	    public void action() {
		VERBOSE_ARRAYCOPY = true;
	    }});
	opts.add(new Option("pa2:verbose-call", "") {
	    public void action() {
		VERBOSE_CALL = true;
		System.out.println("VERBOSE_CALL");
	    }});
	opts.add(new Option("pa2:max-intra-scc-iter", "<number>", "") {
	    public void action() {
		MAX_INTRA_SCC_ITER = Integer.parseInt(getArg(0));
		System.out.println("MAX_INTRA_SCC_ITER set to " + MAX_INTRA_SCC_ITER);
	    }});
	opts.add(new Option("pa2:flow-insensitive", "Turn off the default pointer analysis flow sensitivity") {
	    public void action() {
		FLOW_SENSITIVITY = false;
	    }});
	opts.add(new Option("pa2:use-freshen-trick", "") {
	    public void action() {
		USE_FRESHEN_TRICK = true;
		System.out.println("USE_FRESHEN_TRICK");
	    }});

	return opts;
    }
    
}
