// MemOptMain.java, created Tue Apr 23 19:00:41 2002 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import java.lang.Comparable;

import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.BufferedReader;

import gnu.getopt.Getopt;

import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.SerializableCodeFactory;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.QuadClassHierarchy;
import harpoon.Analysis.MetaMethods.SmartCallGraph;
import harpoon.Analysis.MetaMethods.MetaCallGraphImpl;
import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.PointerAnalysis.AllocationNumbering;
import harpoon.Analysis.PointerAnalysis.Debug;

import harpoon.Analysis.MemOpt.IncompatibilityAnalysis;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.ANEW;

/**
 * <code>MemTestMain</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: MemOptMain.java,v 1.10 2003-01-07 15:05:35 salcianu Exp $
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
	parse_options(args);
	read_data();

	print_alloc_stats();
	if(ALLOC_STAT_ONLY)
	    return;

	IncompatibilityAnalysis ia = get_ia();
	statistics(ia);
    }


    // if true, then only memory allocation stats will be printed
    private static boolean ALLOC_STAT_ONLY = false;
    // the name of the file that contains the map allocation site -> int id
    private static String  ALLOC_NUMBERING_FILE;
    // the name of the file that was dumped by the instrumentation
    private static String  INSTR_DUMPED_FILE;

    private static void parse_options(String[] args) {
	Getopt g = new Getopt("MemOptMain", args, "a", null);
	int c;

	while((c = g.getopt()) != -1)
	    switch(c) {
	    case 'a':
		ALLOC_STAT_ONLY = true;
		break;
	    default:
		System.err.println("Unknown options!");
		System.exit(1);
	    }

	int optind = g.getOptind();

	if(args.length - optind != 2) {

	    System.out.println("Only " + (args.length - optind) + " args");

	    System.out.println
		("Usage:\n\tMemTestMain [<options>] " + 
		 "<alloc_numbering_file> <instr_result_file>\n" +
		 "<alloc_numbering_file>  serialization of " +
		 "the AllocationNumbering used by the instrumentation\n" +
		 "<instr_dumped_file>     file dumped out by " + 
		 "the instrumentation");
	    System.exit(1);
	}

	ALLOC_NUMBERING_FILE = args[optind];
	INSTR_DUMPED_FILE    = args[optind+1];
    }


    // Return an IncompatibilityAnalysis object
    private static IncompatibilityAnalysis get_ia() {
	CallGraph cg = build_call_graph();

	SafeCachingCodeFactory sccf =
	    new SafeCachingCodeFactory(hcf_no_ssa);

        QuadSSI.KEEP_QUAD_MAP_HACK = true;
        hcf_ssi =
	    new CachingCodeFactory(QuadSSI.codeFactory(sccf), true);
	
	return new IncompatibilityAnalysis(mainM, hcf_ssi, cg, linker);
    }


    // build a (smart) call graph for hcf_no_ssa
    private static CallGraph build_call_graph() {
	MetaCallGraphImpl.COLL_HACK = false;

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

        return new SmartCallGraph(hcf_no_ssa, linker, ch, mroots);
    }


    // Read data from the disk
    private static void read_data() {
	try {
	    read_an(ALLOC_NUMBERING_FILE);
	    read_instr_result(INSTR_DUMPED_FILE);
	}
	catch(Exception e) {
	    System.out.println("Error reading data: " + e);
	    System.exit(1);
	}
    }	

    // essentially a CachingCodeFactory that ignores all calls to clear()
    private static class SafeCachingCodeFactory
	implements SerializableCodeFactory {
	private final HCodeFactory ccf;
	public SafeCachingCodeFactory(CachingCodeFactory ccf) {
	    this.ccf = ccf;
	}	
	public HCode convert(HMethod m) { return ccf.convert(m); }
	public String getCodeName() { return ccf.getCodeName(); }
	public void clear(HMethod m) { /* ignore */}
    }


    private static void read_an(String filename) throws Exception {

	System.out.println("Deserializing AllocationNumbering from " +
			   filename);

	ObjectInputStream ois = 
	    new ObjectInputStream(new FileInputStream(filename));

	System.out.println("step 1");

	an     = (AllocationNumbering) ois.readObject();

	System.out.println("step 2");

	linker = (Linker)  ois.readObject();

	System.out.println("step 3");

	mroots = (Set)     ois.readObject();

	System.out.println("step 4");

	mainM  = (HMethod) ois.readObject();

	System.out.println("step 5");

	ois.close();

	hcf_no_ssa = (CachingCodeFactory) an.codeFactory();

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
	    Integer count = new Integer(br.readLine());
	    id2counter.put(new Integer(i), count);
	}

	alloc2counter = new HashMap();
	for(Iterator it = an.getAllocs().iterator(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    alloc2counter.put
		(q, (Integer) id2counter.get(new Integer(an.allocID(q))));
	}
    }

    // print allocation statistics
    private static void print_alloc_stats() {

	class SiteStat implements Comparable {
	    public final Quad alloc_site;
	    public final int  alloc_count;
	    public SiteStat(Quad alloc_site, int alloc_count) {
		this.alloc_site  = alloc_site;
		this.alloc_count = alloc_count;
	    }
	    public int compareTo(Object o) {
		if(! (o instanceof SiteStat)) {
		    System.out.println("o is " + o.getClass().getName());
		}
		SiteStat s2 = (SiteStat) o;
		if (alloc_count < s2.alloc_count) return +1;
		else if (alloc_count > s2.alloc_count) return -1;
		else return 0;
	    }
	};

	int ss_size = 0;
	for(Iterator it = alloc2counter.entrySet().iterator(); it.hasNext();) {
	    Map.Entry entry = (Map.Entry) it.next();
	    int alloc_count = ((Integer) entry.getValue()).intValue();
	    if(alloc_count != 0) ss_size++;
	}
	SiteStat ss[] = new SiteStat[ss_size];

	int total_count = 0;

	int i = 0;
	for(Iterator it = alloc2counter.entrySet().iterator();
	    it.hasNext();) {
	    Map.Entry entry = (Map.Entry) it.next();
	    int alloc_count = ((Integer) entry.getValue()).intValue();
	    if(alloc_count != 0) {
		total_count += alloc_count;
		Quad alloc_site = (Quad) entry.getKey();
		ss[i++] = new SiteStat(alloc_site, alloc_count);
	    }
	}

	Arrays.sort(ss);

	int partial_count = 0;
	System.out.println("Allocation Statistics BEGIN");
	for(i = 0; i < ss.length; i++) {
	    Quad site  = ss[i].alloc_site;
	    int count  = ss[i].alloc_count;
	    double frac = (count*100.0) / total_count;
	    if(frac < 5) break;
	    partial_count += count;
	    System.out.println
		(Debug.code2str(site) + "\n\t" + count +
		 " object(s) \n\t" + Debug.doubleRep(frac, 5, 2) + "%\n\t" +
		 site.getFactory().getMethod());
	}
	System.out.println
	    (i + ((i==1) ? " site allocates " : " sites allocate") +
	     Debug.doubleRep((partial_count*100.0) / total_count, 5, 2) +
	     "% of all objects");
	System.out.println
	    ("None of the other allocation sites allocates more than 5%");
	System.out.println("Allocation Statistics END");
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

    // return # times q was executed
    private static int alloc2int(Quad q) {
	Integer i = (Integer) alloc2counter.get(q);
	if(i == null) {
	    // instruction never executed; it wasn't even included
	    // in the allocation statistics -> return 0
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

	    if(!ia.allMethods().contains(hm)) {
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

