// RegAllocOpts.java, created Thu Oct 19 19:14:47 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeFactory;
import harpoon.Backend.Generic.Code;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.LineNumberReader;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * <code>RegAllocOpts</code> encapsulates a set of modifications to
 * how register allocation will be performed on a particular set of
 * methods.  The choice of modifications are read in from a seperate
 * file, and then code factories produced by this will use the options
 * given the file to change how register allocation is done in certain
 * cases. 
 * <p>
 * Currently, the file supports the following options:
 * <br> "FORCE_LOCAL <em>method name</em> ..." forces the register allocator to
 * use a local allocation strategy on <em>method name</em> rather than
 * whatever default strategy is in place. 
 * <br> "DISABLE_REACHING_DEFS <em>method name</em> ..." forces the
 * register allocator to use a local allocation strategy on <em>method
 * name</em> rather than whatever default strategy is in place, and
 * <b>in addition</b> disables ReachingDefs analysis in local register
 * allocation.  This is a sick hack added just to compile certain spec
 * classes before his deadline.
 * <br> "FORCE_GLOBAL <em>method name</em> ..." forces the register
 * allocator to use a global allocation strategy on <em>method
 * name</em>, rather than whatever default stategy is in place. 
 * <br> "FORCE_COALESCE <em>method name</em> ..." forces global
 * allocation with non-conservative move coalescing to take place,
 * even when it might result in additional memory traffic.
 * <br> "DISABLE_COALESCE <em>method name</em> ..." turns off 
 * move coalescing on global allocation.  
 * <br> "FORCE_APPEL <em>method name</em> ..." forces the register
 * allocator to use Appel-style register allocation on <em>method
 * name</em>, rather than whatever default strategy is in place.
 * <br> "FORCE_FELIX <em>method name</em> ..." forces the register
 * allocator to use Appel-style register allocation on <em>method
 * name</em> (with modifications for architectural indepedence),
 * rather than whatever default strategy is in place.
 * <br> "# <em>comment</em>" is a comment line (not strictly an
 * option, since this line is ignored by this, but too useful to omit) 
 * <p>
 * The method names given in the file should be disjoint; the behavior
 * of allocation is undefined when a <em>method</em> is listed under
 * multiple options.
 * <p> 
 * A set of methods with a common prefix can be passed as the <em>method name</em> argument
 * to a single option by appending the wildcard character '*' to the prefix.
 * <p>
 * After these options have been loaded, this class can be used to
 * produce wrapper <code>RegAlloc.Factory</code>s around other
 * <code>RegAlloc.Factory</code>s
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: RegAllocOpts.java,v 1.1.2.8 2001-07-08 20:15:34 pnkfelix Exp $ */
public class RegAllocOpts {
    public static final boolean INFO = false;
    Filter disableReachingDefs;
    Filter forceLocal;
    Filter forceGlobal;
    Filter forceAppel;
    Filter forceFelix;
    Filter forceCoalesce;
    Filter disableCoalesce;

    /** Creates a <code>RegAllocOpts</code>. */
    public RegAllocOpts(String filename) {
	disableReachingDefs = new Filter();
	forceLocal = new Filter();
	forceGlobal = new Filter();
	forceCoalesce = new Filter();
	forceAppel = new Filter();
	forceFelix = new Filter();
	disableCoalesce = new Filter();

        if (filename != null) {
	    try {
		if (INFO) System.out.println("reading options from "+filename);
		readOptions(filename);
	    } catch (IOException e) {
		System.out.println(e.getMessage());
		System.out.println
		    ("Error reading regalloc options file; no options being used");
		forceLocal.clear(); forceGlobal.clear(); forceCoalesce.clear();
		disableCoalesce.clear();
	    }
	}
    }

    public RegAlloc.Factory factory(final RegAlloc.Factory hc) {
	return new RegAlloc.Factory() {
	    public RegAlloc makeRegAlloc(Code c) {
		String name = nameFor(c).trim();
		
		if (disableReachingDefs.contains(name)) {
		    if (INFO) System.out.println(" * USING DRd FOR "+name);
		    return LocalCffRegAlloc.RD_DISABLED_FACTORY.makeRegAlloc(c);
		} else if (forceLocal.contains(name)) {
		    if (INFO) System.out.println(" * USING FL FOR "+name);
		    return RegAlloc.LOCAL.makeRegAlloc(c);
		} else if (forceGlobal.contains(name)) {
		    if (INFO) System.out.println(" * USING FG FOR "+name);
		    return RegAlloc.GLOBAL.makeRegAlloc(c);
		} else if (forceCoalesce.contains(name)) {
		    if (INFO) System.out.println(" * USING FC FOR "+name);
		    return GraphColoringRegAlloc.AGGRESSIVE_FACTORY.makeRegAlloc(c);
		} else if (forceAppel.contains(name)) {
		    if (INFO) System.out.println(" * USING Appel FOR "+name);
		    return AppelRegAlloc.FACTORY.makeRegAlloc(c);
		} else if (forceFelix.contains(name)) {
		    if (INFO) System.out.println(" * USING Felix FOR "+name);
		    return AppelRegAllocFsk.FACTORY.makeRegAlloc(c);
		} else if (disableCoalesce.contains(name)) {
		    if (INFO) System.out.println(" * USING DC FOR "+name);
		    return GraphColoringRegAlloc.BRAINDEAD_FACTORY.makeRegAlloc(c);
		} else {
		    if (INFO) System.out.println(" * USING NM FOR "+name);
		    return hc.makeRegAlloc(c);
		}
	    }
	};
    }

    private static String nameFor(Code c) {
	HMethod hm = c.getMethod();
	return hm.getDeclaringClass().getName() +"."+ hm.getName();
    }

    private void readOptions(String filename) throws IOException {
	LineNumberReader r = new LineNumberReader(new FileReader(filename));
	String line;
	while (null != (line=r.readLine())) {
	    line = line.trim(); // remove white space from both sides.
	    
	    // allow comments and blank lines.
	    if (line.startsWith("#") || line.length()==0) continue;
	    
	    StringTokenizer st = new StringTokenizer(line);
	    String s = st.nextToken();
	    Filter addToSet = null;
	    if (s.toUpperCase().equals("DISABLE_REACHING_DEFS")) {
		addToSet = disableReachingDefs;
	    } else if (s.toUpperCase().equals("FORCE_LOCAL")) {
		addToSet = forceLocal;
	    } else if (s.toUpperCase().equals("FORCE_GLOBAL")) {
		addToSet = forceGlobal;
	    } else if (s.toUpperCase().equals("FORCE_COALESCE")) {
		addToSet = forceCoalesce;
	    } else if (s.toUpperCase().equals("FORCE_APPEL")) {
		addToSet = forceAppel;
	    } else if (s.toUpperCase().equals("FORCE_FELIX")) {
		addToSet = forceFelix;
	    } else if (s.toUpperCase().equals("DISABLE_COALESCE")) {
		addToSet = disableCoalesce;
	    } else {
		System.err.println("unknown RegAlloc option: "+s+
				   " line: "+r.getLineNumber());
		continue;
	    }

	    while (st.hasMoreTokens()) {
		String tkn = st.nextToken();
		addToSet.add(tkn);
	        // System.out.println("adding "+tkn+" to "+s);
	    }
	}

	r.close();
	
    }

    /** Filter is a set of strings. */
    class Filter {
	// AF(c) = { s | s in c.names OR exists p in c.prefixMatches
	//               such that s begins-with p }
	HashSet names = new HashSet(5); 
	ArrayList prefixMatches = new ArrayList(5);

	public boolean contains(String name) {
	    if (names.contains(name)) 
		return true;
	    
	    for(Iterator pfs=prefixMatches.iterator();pfs.hasNext();){
		String s = (String) pfs.next();
		if (name.startsWith(s)) 
		    return true;
	    }

	    return false;
	}
	
	public void add(String str) {
	    if (!str.endsWith("*")) {
		addName(str);
	    } else {
		addPrefix(str.substring(0,str.length()-1));
	    }
	}
	public void addName(String str) { names.add(str); }
	public void addPrefix(String prefix) { prefixMatches.add(prefix); }

	public void clear() { names.clear(); prefixMatches.clear(); }
    }
    
}
