// Option.java, created Wed Apr  9 13:52:03 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Options;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.StringTokenizer;

import java.io.PrintStream;

/**
 * <code>Option</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: Option.java,v 1.4 2004-02-08 03:21:58 cananian Exp $
 */
public abstract class Option {

    public Option(String optionName, String argSummary, String optArgSummary,
		  String helpMessage) {
	this.optionName = optionName;

	if(argSummary == null) argSummary = "";
	if(optArgSummary == null) optArgSummary = "";

	StringTokenizer argTokenizer = new StringTokenizer(argSummary);
	StringTokenizer optArgTokenizer = new StringTokenizer(optArgSummary);

	this.compArgs = new String[argTokenizer.countTokens()];
	this.optArgs  = new String[optArgTokenizer.countTokens()];
	this.description = 
	    buildDescription(optionName, argTokenizer, optArgTokenizer);
	this.helpMessage = helpMessage;
    }

    public Option(String optionName, String argSummary, String helpMessage) {
	this(optionName, argSummary, null, helpMessage);
    }

    public Option(String optionName, String helpMessage) {
	this(optionName, null, null, helpMessage);
    }

    public Option(String optionName) {
	this(optionName, null, null, null);
    }

    protected final String optionName;
    protected final String[] compArgs;
    protected final String[] optArgs;
    protected final String description;
    protected final String helpMessage;

    protected void setArg(int i, String arg) { compArgs[i] = arg; }
    public String getArg(int i) { return compArgs[i]; }

    protected void setOptionalArg(int i, String arg) { optArgs[i] = arg; }
    public String getOptionalArg(int i) { return optArgs[i]; }

    public String optionName() { return optionName; }
    public int numberArgs() { return compArgs.length; }
    public int numberOptionalArgs() { return optArgs.length; }
    public String description() { return description; }

    public abstract void action();
    
    public void printHelp(PrintStream ps) {
	ps.println(description);
	if((helpMessage != null) && (helpMessage.length() > 0)) {
	    ps.print("\t");
	    ps.println(helpMessage);
	}
    }

    public String toString() { return optionName; }


    private static String buildDescription(String optionName,
					   StringTokenizer argTok,
					   StringTokenizer optArgTok) {
	StringBuffer sb = new StringBuffer();

	sb.append("-");
	if(optionName.length() > 1)
	    sb.append("-");
	sb.append(optionName);

	if(argTok.countTokens() > 0) {
	    sb.append(" ");
	    while(argTok.hasMoreTokens()) {
		sb.append(" ");
		sb.append(argTok.nextToken());
	    }
	}

	if(optArgTok.countTokens() > 0) {
	    sb.append("  [");
	    while(optArgTok.hasMoreTokens()) {
		sb.append(" ");
		sb.append(optArgTok.nextToken());
	    }
	    sb.append(" ]");
	}

	return sb.toString();
    }


    public static String[] parseOptions(List<Option> options,
					String[] args) {
	List<String> unparsedArgs = 
	    parseOptions(options, array2list(args));
	return 
	    unparsedArgs.toArray(new String[unparsedArgs.size()]);
    }


    public static List<String> parseOptions(List<Option> options,
						List<String> args) {
	
	Map<String,Option> arg2option = new HashMap<String,Option>();
	for(Option option : options) {
	    arg2option.put(option.optionName(), option);
	}

	List<String> unparsedArgs = new LinkedList<String>();

	for(ListIterator<String> it = args.listIterator(); it.hasNext();) {
	    String arg = it.next();
	    if(!isOption(arg) || !arg2option.containsKey(getOption(arg))) {
		unparsedArgs.add(arg);
		continue;
	    }

	    Option option = arg2option.get(getOption(arg));
	    parseOptionArgs(option, it);

	    option.action();
	}
	
	return unparsedArgs;
    }

    // checks whether arg is an option (i.e., it starts with a '-')
    public static boolean isOption(String arg) {
	return (arg.length() > 0) && (arg.charAt(0) == '-');
    }

    // peels off all the '-' characters from the beginning of arg
    public static String getOption(String arg) {
	int i = 0; 
	while((i < arg.length()) && (arg.charAt(i) == '-')) i++;
	assert i > 0 : arg + " is not an option at all";
	return arg.substring(i);
    }

    private static void parseOptionArgs(Option option,
					ListIterator<String> it) {
	for(int i = 0; i < option.numberArgs(); i++) {
	    String arg = null;
	    if(!it.hasNext() || isOption(arg = it.next())) {
		System.err.println("Error while parsing argument #" + i +
				   " for option " + option);
		System.err.println("Help:");
		option.printHelp(System.err);
		System.exit(1);
	    }
	    option.setArg(i, arg);
	}

	for(int i = 0; i < option.numberOptionalArgs(); i++) {
	    if(!it.hasNext()) break;
	    String arg = it.next();
	    if(isOption(arg)) {
		it.previous(); // put the arg back
		return;
	    }
	    option.setOptionalArg(i, arg);
	}
    }


    public static List/*<String>*/ array2list(String[] a) {
	List/*<String>*/ list = new LinkedList/*<String>*/();
	for(int i = 0; i < a.length; i++) {
	    if(a[i] != null)
		list.add(a[i]);
	}
	return list;
    }


    public static void main(String[] args) {
	List/*<Option>*/ options = new LinkedList/*<Option>*/();
	options.add
	    (new Option("fileoption", "<file>", "File to compile") {
		public void action() {
		    System.out.println("Option " + description());
		}});
	options.add
	    (new Option("fast") {
		public void action() {
		    System.out.println("Option " + description());
		}});
	options.add
	    (new Option("seriousOpt", "<arg1> <arg2>",
			"<optArg1> <optArg2> <optArg3>",
			"Serious arg!") {
		public void action() {
		    System.out.println("Option " + description());
		}});

	args = parseOptions(options, args);

	System.out.print("Remaining options: [ ");
	for (int i = 0; i < args.length; i++)
	    System.out.print(args[i] + " ");
	System.out.println("]");
    }

}
