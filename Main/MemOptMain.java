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

import harpoon.ClassFile.*;
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
import harpoon.Analysis.PointerAnalysis.Debug;

import harpoon.Analysis.MemOpt.IncompatibilityAnalysis;

import harpoon.IR.Quads.*;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.ANEW;

/**
 * <code>MemTestMain</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: MemOptMain.java,v 1.2 2002-04-24 23:04:09 salcianu Exp $
 */
public abstract class MemOptMain {
    
    private static AllocationNumbering an;
    private static Linker linker;
    private static Set mroots;
    private static HMethod mainM;
    private static CachingCodeFactory hcf_no_ssa;
    private static CachingCodeFactory hcf_ssi;

    //private static HCodeFactory parent;

    public static void main(String[] args) {
	if(args.length != 2) {
	    System.out.println
		("Usage:\n\tMemTestMain " + 
		 "<alloc_numbering_file> <instr_result_file>");
	    System.exit(1);
	}
	
	read_data(args);

	/*
	parent = hcf_no_ssa;
	while(parent instanceof CachingCodeFactory) {
	    System.out.println("caching code factory <-");
	    parent = ((CachingCodeFactory) parent).parent;
	}
	*/

	IncompatibilityAnalysis ia = get_ia();

	// debug messages
	ia.printSomething();

	statistics(ia);
    }



    // Return an IncompatibilityAnalysis object
    private static IncompatibilityAnalysis get_ia() {
	CallGraph cg = build_call_graph();

        QuadSSI.KEEP_QUAD_MAP_HACK = true;
        hcf_ssi =
	    new CachingCodeFactory(QuadSSI.codeFactory(hcf_no_ssa), true);

	System.out.println("hcf_ssi = " + hcf_ssi.hashCode());
	
	return new IncompatibilityAnalysis(mainM, hcf_ssi, cg);
    }


    // build a (smart) call graph for hcf_no_ssa
    private static CallGraph build_call_graph() {
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

        return new SmartCallGraph(hcf_no_ssa, ch, mroots);
    }


    // Read data from the disk
    private static void read_data(String[] args) {
	try {
	    read_an(args[0]);
	    read_instr_result(args[1]);
	}
	catch(Exception e) {
	    System.out.println("Error reading data: " + e);
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

	hcf_no_ssa = (CachingCodeFactory) an.codeFactory();

	System.out.println("hcf_no_ssa = " + hcf_no_ssa.hashCode());

	assert
	    hcf_no_ssa.getCodeName().equals(QuadNoSSA.codename) :
	    "QuadNoSSA expected instead of " + hcf_no_ssa;
    }


    private static Map alloc2counter;

    private static void read_instr_result(String filename) throws Exception {
	Map id2counter = new HashMap();
	BufferedReader br = new BufferedReader(new FileReader(filename));
	int size = new Integer(br.readLine()).intValue();

	for(int i = 0; i < size; i++) {
	    id2counter.put(new Integer(i), new Integer(br.readLine()));
	}

	alloc2counter = new HashMap();
	for(Iterator it = an.getAllocs().iterator(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    alloc2counter.put
		(q, (Integer) id2counter.get(new Integer(an.allocID(q))));
	}

	/*
	System.out.println("alloc2counter BEGIN");
	for(Iterator it = alloc2counter.entrySet().iterator();
	    it.hasNext(); ) {
	    Map.Entry entry = (Map.Entry) it.next();
	    System.out.println(entry.getKey() + " -> " + entry.getValue());
	}
	System.out.println("alloc2counter END");
	*/
    }
    
    
    private static class TypeStat {
	HClass hclass;
	long alloc_sites_heap   = 0L;
	long alloc_sites_static = 0L;
	long objects_heap   = 0L;
	long objects_static = 0L;

	TypeStat(HClass hclass) {
	    this.hclass = hclass;
	}

	public String toString() {
	    return
		alloc_sites_heap + " " + alloc_sites_static + " | " +
		objects_heap + " " + objects_static + " || " + hclass;
	}
    }


    private static TypeStat get_type_stat(HClass hclass) {
	TypeStat result = (TypeStat) hclass2stat.get(hclass);
	if(result == null)
	    hclass2stat.put(hclass, result = new TypeStat(hclass));
	return result;
    }
    private static Map hclass2stat = new HashMap();


    private static int alloc2int(Quad q) {
	Integer i = (Integer) alloc2counter.get(q);
	if(i == null) {
	    System.out.println("#WARNING: " + q);
	    return 0;
	}
	return i.intValue();
    }

    private static HMethod get_method(Quad q) {
	return q.getFactory().getMethod();
    }

    private static void statistics(IncompatibilityAnalysis ia) {
	for(Iterator it = an.getAllocs().iterator(); it.hasNext(); ) {
	    
	    Quad q = (Quad) it.next();
	    HMethod hm = get_method(q);

	    if(!ia.allMethods.contains(hm)) {
		//System.out.println("Warn: " + hm + " not in the ssi cache!");
		continue;
	    }

	    /*
	    System.out.println("----------------------------------");
	    System.out.println("Good: " + hm + " is in the ssi cache!");
	    System.out.println("Seen by ia? " + ia.allMethods.contains(hm));
	    */

	    QuadNoSSA code = (QuadNoSSA) q.getFactory().getParent();

	    if(q instanceof ANEW) {
		TypeStat ts = get_type_stat(((ANEW) q).hclass());
		ts.alloc_sites_heap++;
		ts.objects_heap += alloc2int(q);
	    }
	    else if(q instanceof NEW) {
		TypeStat ts = get_type_stat(((NEW) q).hclass());
		
		QuadNoSSA hcode = (QuadNoSSA) hcf_no_ssa.convert(hm);

		if(!hcode.getElementsL().contains(q))
		    System.out.println("LAE! " + hm + " | " + 
				       " | hcode = " + hcode + 
				       " | code  = " + code);

		assert code == hcode : "not equal!";
		
		Quad qssi = ia.getSSIQuad(q);
		
		assert qssi != null : "null qssi";

		/*
		if(qssi == null) {
		    System.out.println
			("WARNING\n\t" + 
			 Debug.code2str(q) + " | " + q.hashCode() + "\n\t" + 
			 hm + "\n");
		    continue;
		}
		*/

		if(ia.isSelfIncompatible(qssi)) {
		    ts.objects_heap += alloc2int(q);
		    ts.alloc_sites_heap++;
		}
		else {
		    ts.objects_static += alloc2int(q);
		    ts.alloc_sites_static++;
		}
	    }
	}

	for(Iterator it = hclass2stat.entrySet().iterator(); it.hasNext(); ) {
	    Map.Entry entry = (Map.Entry) it.next();
	    System.out.println(entry.getValue());
	}
    }

}


/*
class UnmodCachingCodeFactory extends CachingCodeFactory {
    private final CachingCodeFactory ccf;

    public UnmodCachingCodeFactory(CachingCodeFactory ccf) {
	this.ccf = ccf;
    }

    public void clear(HMethod hm) {
	// ignored!
    }

    public void put(HMethod m, HCode hc) {
	// ignored!
    }

    public HCode convert(HMethod hm) {
	return ccf.convert(hm);
    }
    
    public String getCodeName() {
	return ccf.getCodeName();
    }

}
*/
