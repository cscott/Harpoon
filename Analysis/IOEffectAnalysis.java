// IOEffectAnalysis.java, created Wed Sep  7 11:14:07 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.Linker;

import harpoon.Analysis.CallGraph;

import harpoon.Util.ParseUtil;

/**
 * <code>IOEffectAnalysis</code> is a simple analysis that detects
 * whether a method may (transitively) execute an input/output
 * operation.  This analysis detects I/O operations executed directly
 * by a method, or by one of its transitive callees.
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: IOEffectAnalysis.java,v 1.3 2005-09-16 19:08:45 salcianu Exp $ */
public class IOEffectAnalysis {

    /** Creates a <code>IOEffectAnalysis</code> object.

	@param cg Callgraph used to find the transitive callees of
	each method this analysis is queried about. */
    public IOEffectAnalysis(CallGraph cg) {
	this.cg = cg;
    }

    private final CallGraph cg;

    /** Checks whether the method <code>hm</code> (transitively)
        executes any input/output operation.  The implementation uses
        caching such that a series of quesries about the I/O status of
        all program methods (in any order) has time complexity linear
        in the number of queries + the size of the callgraph (the
        number of both nodes and arcs). */
    public boolean doesIO(HMethod hm) {
	Boolean answer = cacheDoesIO.get(hm);
	if(answer == null) {
	    answer = doesIO(hm, new HashSet<HMethod>());
	    cacheDoesIO.put(hm, answer);
	}
	return answer.booleanValue();
    }


    // checks whether we already have a result for hm; if not, invokes
    // _doesIO to do some more serious computation
    private boolean doesIO(HMethod hm, Set<HMethod> inProgress) {
	Boolean answer = cacheDoesIO.get(hm);
	if(answer == null) {
	    answer = _doesIO(hm, inProgress);
	    cacheDoesIO.put(hm, answer);
	}
	return answer.booleanValue();
    }
    private final Map<HMethod,Boolean> cacheDoesIO = new HashMap<HMethod,Boolean>();

    // handles the inProgress set to avoid infinite cycles
    // will never be called with hm in inProgress.
    private Boolean _doesIO(HMethod hm, Set<HMethod> inProgress) {
	if(!inProgress.add(hm)) {
	    throw new Error("strange");
	}
	Boolean answer = __doesIO(hm, inProgress);
	inProgress.remove(hm);
	return answer;
    }

    // does the real work (somebody has to work!)
    private Boolean __doesIO(HMethod hm, Set<HMethod> inProgress) {
	if(ioMethods == null) {
	    initIOMethods(hm.getDeclaringClass().getLinker());
	}
	if(ioMethods.contains(hm)) return Boolean.TRUE;

	for(HMethod callee : cg.calls(hm)) {
	    // avoid cycles
	    if(inProgress.contains(callee)) continue;

	    if(doesIO(callee, inProgress)) {
		return Boolean.TRUE;
	    }
	}
	
	return Boolean.FALSE;
    }


    private String ioMethodsPropertiesFileName() {
	return 
	    "harpoon/Analysis/io-methods." + 
	    harpoon.Main.Settings.getStdLibVerName() + 
	    ".properties";
    }

    // init the set of methods that perform IO operations
    private void initIOMethods(final Linker linker) {
	ioMethods = new HashSet<HMethod>();
	try {
	    ParseUtil.readResource
		(ioMethodsPropertiesFileName(),
		 new ParseUtil.StringParser() {
		    public void parseString(String line) throws ParseUtil.BadLineException {
			ioMethods.add(ParseUtil.parseMethod(linker, line.trim()));
		    }
		});
	}
	catch(java.io.IOException ex) {
	    throw new RuntimeException
		("Error reading I/O methods from " + ioMethodsPropertiesFileName(),
		 ex);
	}
    }
    private Set<HMethod> ioMethods = null;

    // Methods that perform I/O.  The analysis detects all methods
    // that transitively invoke any of these methods.
    private static String[][] methods = {
	{"java.io.File", "length0"},
	
	{"java.io.FileInputStream", "open"},
	{"java.io.FileInputStream", "close"},
	{"java.io.FileInputStream", "available"},
	{"java.io.FileInputStream", "read"},
	{"java.io.FileInputStream", "readBytes"},
	
	{"java.io.FileOutputStream", "open"},
	{"java.io.FileOutputStream", "openAppend"},
	{"java.io.FileOutputStream", "close"},
	{"java.io.FileOutputStream", "write"},
	{"java.io.FileOutputStream", "writeBytes"},
	
	{"java.net.SocketOutputStream", "socketWrite"},
	{"java.net.SocketInputStream",  "socketRead"},
	
	{"java.net.PlainSocketImpl", "socketClose"},
	{"java.net.PlainSocketImpl", "socketAvailable"},
	
	{"java.net.PlainSocketImpl", "socketAccept"},
	{"java.net.PlainSocketImpl", "socketBind"},
	{"java.net.PlainSocketImpl", "socketCreate"},
	{"java.net.PlainSocketImpl", "socketListen"}
    };

}
