// ParseUtil.java, created Thu Nov 16 00:56:01 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.NoSuchClassException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
/**
 * <code>ParseUtil</code> implements simple parsers for common string
 * data types.  Input is from named resource files.
 * 
 * @author   C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ParseUtil.java,v 1.1.2.3 2001-06-17 22:35:59 cananian Exp $
 */
public abstract class ParseUtil {
    /** Reads from the given resource, ignoring '#' comments and blank lines,
     *  obeying 'include' directives, and invoking the given
     *  <code>StringParser</code> on any other lines. */
    public static void readResource(String resourceName, StringParser sp)
	throws IOException {
	InputStream is = ClassLoader.getSystemResourceAsStream(resourceName);
	if (is==null) throw new FileNotFoundException(resourceName);
	LineNumberReader r = new LineNumberReader(new InputStreamReader(is));
	String line;
	while (null != (line=r.readLine())) {
	    line = line.trim(); // remove white space from both sides.
	    // allow comments and blank lines.
	    int hash = line.indexOf('#');
	    if (hash>=0) line = line.substring(0, hash).trim();
	    if (line.length()==0) continue;
	    // check if this is an 'include' directive.
	    if (line.startsWith("include ")) {
		String nresource = line.substring(7).trim();
		try {
		    readResource(nresource, sp);
		} catch (IOException ex) {
		    System.err.println("Error reading "+nresource+" on line "+
				       r.getLineNumber()+" of "+resourceName);
		    throw ex;
		}
	    } else try {
		sp.parseString(line);
	    } catch (BadLineException ex) {
		ex.line = r.getLineNumber();
		throw ex; // rethrow.
	    }
	}
	r.close();
    }
    private static String firstWord(String str) throws BadLineException {
	str = str.trim();
	int sp = str.indexOf(' ');
	if (sp>=0) str=str.substring(0, sp);
	if (str.length()==0) throw new BadLineException("no word on line");
	return str;
    }
    /** Parse a string as a class name. */
    public static HClass parseClass(Linker l, String className)
	throws BadLineException {
	className = firstWord(className);
	try {
	    return l.forName(className);
	} catch (NoSuchClassException ex) {
	    throw new BadLineException("No such class: "+className);
	}
    }
    /** Parse a string as a method name + descriptor string. */
    public static HMethod parseMethod(Linker l, String methodName)
	throws BadLineException {
	methodName = firstWord(methodName);
	int lparen = methodName.indexOf('(');
	if (lparen < 0)
	    throw new BadLineException("No left paren in method descriptor: "+
				       methodName);
	int rparen = methodName.indexOf(')', lparen);
	if (rparen < 0)
	    throw new BadLineException("No right paren in method descriptor: "+
				       methodName);
	int dot = methodName.lastIndexOf('.', lparen);
	if (dot < 0)
	    throw new BadLineException("No dot separating class and method: "+
				       methodName);
	String desc = methodName.substring(lparen);
	String method = methodName.substring(dot+1, lparen);
	String classN = methodName.substring(0, dot);
	HClass hc = parseClass(l, classN);
	if (desc.length()>2) try { // explicit descriptor.
	    return hc.getDeclaredMethod(method, desc);
	} catch (NoSuchMethodError ex) {
	    throw new BadLineException("No method named "+method+" with "+
				       "descriptor "+desc+" in "+hc);
	} else { // no descriptor.  hopefully method name is unique.
	    HMethod[] hm = hc.getDeclaredMethods();
	    HMethod r = null;
	    for (int i=0; i<hm.length; i++)
		if (hm[i].getName().equals(method))
		    if (r==null) r = hm[i];
		    else throw new BadLineException("More than one method "+
						    "named "+method+" in "+hc);
	    if (r==null) throw new BadLineException("No method named "+method+
						    " in "+hc);
	    return r;
	}
    }
    /** Parse a string as a set.  The string may be surrounded by optional
     *  curly braces, and elements may be delimited either spaces or
     *  commas or both.  The given <code>StringParser</code> is invoked
     *  on each element in the set. */
    public static void parseSet(String s, StringParser sp)
	throws BadLineException {
	s = s.trim();
	// strip away braces from outside if present.
	if (s.length()>1 &&
	    s.charAt(0)=='{' && s.charAt(s.length()-1)=='}')
	    s = s.substring(1, s.length()-1).trim();
	// now get each element one-by-one.
	while (s.length()>0) {
	    // delimiters are ' ' and ','
	    int space = s.indexOf(' '); if (space<0) space = s.length();
	    int comma = s.indexOf(','); if (comma<0) comma = s.length();
	    int delimit = (space < comma) ? space : comma;
	    // this is the element:
	    sp.parseString(s.substring(0, delimit).trim());
	    // now go on to the next.
	    if (s.length()==delimit) break;
	    s = s.substring(delimit+1).trim();
	}
    }
    /** Callback interface for the resource parsing routines. */
    public static interface StringParser {
	public void parseString(String s) throws BadLineException ;
    }
    /** Exception thrown by the methods in <code>ParseUtil</code>
     *  to indicate an unparsable line in an input file. */
    public static class BadLineException extends IOException {
	/** readResource will set this to the right value. */
	int line=0;
	BadLineException(String message) { super(message); }
	public String toString() {
	    return super.toString() + " (line "+line+")";
	}
    }
}

