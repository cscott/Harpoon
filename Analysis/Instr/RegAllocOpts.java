// RegAllocOpts.java, created Thu Oct 19 19:14:47 2000 by pnkfelix
// Copyright (C) 2000 Felix S Klock II <pnkfelix@mit.edu>
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
 * <br> "FORCE_GLOBAL <em>method name</em> ..." forces the register
 * allocator to use a global allocation strategy on <em>method
 * name</em>, rather than whatever default stategy is in place. 
 * <br> "FORCE_COALESCE <em>method name</em> ..." forces global
 * allocation with non-conservative move coalescing to take place,
 * even when it might result in additional memory traffic.
 * <br> "# <em>comment</em>" is a comment line (not strictly an
 * option, since this line is ignored by this, but too useful to omit) 
 * <p>
 * The method names given in the file should be disjoint.
 * <p>
 * After these options have been loaded, this class can be used to
 * produce wrapper <code>RegAlloc.Factory</code>s around other
 * <code>RegAlloc.Factory</code>s
 * 
 * @author  Felix S Klock II <pnkfelix@mit.edu>
 * @version $Id: RegAllocOpts.java,v 1.1.2.1 2000-10-20 08:44:44 pnkfelix Exp $
 */
public class RegAllocOpts {
    
    HashSet forceLocal;
    HashSet forceGlobal;
    HashSet forceCoalesce;

    /** Creates a <code>RegAllocOpts</code>. */
    public RegAllocOpts(String filename) {
	forceLocal = new HashSet(5);
	forceGlobal = new HashSet(5);
	forceCoalesce = new HashSet(5);

        if (filename != null) {
	    try {
		readOptions(filename);
	    } catch (IOException e) {
		System.out.println(e.getMessage());
		System.out.println
		    ("Error reading regalloc options file; no options being used");
		forceLocal.clear(); forceGlobal.clear(); forceCoalesce.clear();
	    }
	}
    }

    public RegAlloc.Factory factory(final RegAlloc.Factory hc) {
	return new RegAlloc.Factory() {
	    public RegAlloc makeRegAlloc(Code c) {
		String name = nameFor(c).trim();
		
		if (forceLocal.contains(name)) {
		    // System.out.println(" * USING FL FOR "+name);
		    return RegAlloc.LOCAL.makeRegAlloc(c);
		} else if (forceGlobal.contains(name)) {
		    return RegAlloc.GLOBAL.makeRegAlloc(c);
		} else if (forceCoalesce.contains(name)) {
		    // System.out.println(" * USING FC FOR "+name);
		    return GraphColoringRegAlloc.AGGRESSIVE_FACTORY.makeRegAlloc(c);
		} else {
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
	    HashSet addToSet = null;
	    if (s.toUpperCase().equals("FORCE_LOCAL")) {
		addToSet = forceLocal;
	    } else if (s.toUpperCase().equals("FORCE_GLOBAL")) {
		addToSet = forceGlobal;
	    } else if (s.toUpperCase().equals("FORCE_COALESCE")) {
		addToSet = forceCoalesce;
	    } else {
		System.out.println("unknown RegAlloc option: "+s+
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
    
}
