// MemOptMain.java, created Tue Apr 23 19:00:41 2002 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.BufferedReader;

import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.CachingCodeFactory;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.QuadClassHierarchy;
import harpoon.Analysis.MetaMethods.SmartCallGraph;
import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.PointerAnalysis.AllocationNumbering;

import harpoon.Analysis.MemOpt.IncompatibilityAnalysis;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.QuadSSI;


/**
 * <code>MemTestMain</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: MemOptMain.java,v 1.1 2002-04-24 01:21:49 salcianu Exp $
 */
public abstract class MemOptMain {
    
    private static AllocationNumbering an;
    private static Linker linker;
    private static Set mroots;
    private static HMethod mainM;

    public static void main(String[] args) {
	if(args.length != 2) {
	    System.err.println
		("Usage:\n\tMemTestMain " + 
		 "<alloc_numbering_file> <instr_result_file>");
	    System.exit(1);
	}
	
	read_data(args);

	IncompatibilityAnalysis ia = get_ia();

	// debug messages
	ia.printSomething();

	// DO SOMETHING

    }


    // Return an IncompatibilityAnalysis object
    private static IncompatibilityAnalysis get_ia() {
	HCodeFactory hcf_no_ssa = an.codeFactory();

	assert
	    hcf_no_ssa.getCodeName().equals(QuadNoSSA.codename) :
	    "QuadNoSSA expected instead of " + hcf_no_ssa;

	assert
	    hcf_no_ssa instanceof CachingCodeFactory :
	    "Not a CachingCodeFactory!" + hcf_no_ssa;

	CallGraph cg = build_call_graph(hcf_no_ssa);

        QuadSSI.KEEP_QUAD_MAP_HACK = true;
        CachingCodeFactory hcf_ssi =
	    new CachingCodeFactory(QuadSSI.codeFactory(hcf_no_ssa), true);
	
	return new IncompatibilityAnalysis(mainM, hcf_ssi, cg);
    }


    // build a (smart) call graph for hcf_no_ssa
    private static CallGraph build_call_graph(HCodeFactory hcf_no_ssa) {
	ClassHierarchy ch = 
	    new QuadClassHierarchy(linker, mroots, hcf_no_ssa);

	// filter out things that are not hmethods
        for (Iterator it = mroots.iterator(); it.hasNext(); ) {
            Object atom = it.next();
            if (!(atom instanceof HMethod)) it.remove();
        }
	
        // now add static initializers;
        for(Iterator it = ch.classes().iterator(); it.hasNext(); ) {
            HClass hcl = (HClass) it.next();
            HMethod hm = hcl.getClassInitializer();
            if (hm != null)
                mroots.add(hm);
        }

        return new SmartCallGraph((CachingCodeFactory) hcf_no_ssa, ch, mroots);
    }


    // Read data from the disk
    private static void read_data(String[] args) {
	try {
	    read_an(args[0]);
	    read_instr_result(args[1]);
	}
	catch(Exception e) {
	    System.err.println("Error reading data: " + e);
	    System.exit(1);
	}
    }	


    private static void read_an(String filename) throws Exception {
	ObjectInputStream ois = 
	    new ObjectInputStream(new FileInputStream(filename));
	an     = (AllocationNumbering) ois.readObject();
	linker = (Linker)  ois.readObject();
	mroots = (Set)     ois.readObject();
	mainM  = (HMethod) ois.readObject();
	ois.close();
    }


    private static Map alloc2counter;

    private static void read_instr_result(String filename) throws Exception {
	Map id2counter = new HashMap();
	BufferedReader br = new BufferedReader(new FileReader(filename));
	int size = new Integer(br.readLine()).intValue();

	System.out.println("DEBUG: size = " + size);

	for(int i = 0; i < size; i++) {
	    id2counter.put(new Integer(i), new Integer(br.readLine()));
	}

	alloc2counter = new HashMap();
	for(Iterator it = an.getAllocs().iterator(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    alloc2counter.put
		(q, (Integer) id2counter.get(new Integer(an.allocID(q))));
	}
    }
    
}
