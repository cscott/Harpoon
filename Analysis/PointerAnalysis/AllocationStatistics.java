// AllocationStatistics.java, created Sat Nov 30 22:21:12 2002 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HCodeFactory;
import harpoon.Analysis.Quads.CallGraph;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.Code;
import harpoon.Analysis.PointerAnalysis.Debug;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Collection;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * <code>AllocationStatistics</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: AllocationStatistics.java,v 1.2 2002-12-02 17:08:47 salcianu Exp $
 */
public class AllocationStatistics {
    
    /** Creates a <code>AllocationStatistics</code>. */
    public AllocationStatistics(Linker linker,
				String allocNumberingFileName,
				String instrumentationResultsFileName) {
	try { 
	    this.ans = 
		new AllocationNumberingStub(linker, allocNumberingFileName);
	    this.allocID2count = 
		readInstrumentationResults(instrumentationResultsFileName);
	}
	catch(IOException e) {
	    System.err.println("Cannot create AllocStatistics: " + e);
	    System.exit(1);
	}
    }

    // provides the map quad -> allocID (an integer)
    private AllocationNumberingStub ans;
    // map allocID -> count
    private Map allocID2count;


    /** Return the number of times the allocation <code>alloc</code>
        was executed.

	@param alloc allocation site

	@return number of times <code>alloc</code> was executed */
    public int getCount(Quad alloc) {
	Integer count = 
	    (Integer) allocID2count.get(new Integer(ans.allocID(alloc)));
	return (count == null) ? 0 : count.intValue();
    }


    private static class AllocationNumberingStub {
	
	/** Creates a <code>AllocationNumberingStub</code>. */
	public AllocationNumberingStub(Linker linker, String filename)
	    throws IOException {
	    BufferedReader br = new BufferedReader(new FileReader(filename));
	    int nb_methods = readInt(br);
	    for(int i = 0; i < nb_methods; i++)
		readMethodData(linker, br);	
	}
    
	private final Map method2quadID2counter = new HashMap();
	
	private Map getMap4Method(HMethod hm) {
	    Map map = (Map) method2quadID2counter.get(hm);
	    if(map == null) {
		map = new HashMap();
		method2quadID2counter.put(hm, map);
	    }
	    return map;
	}
	
	/** Return the integer ID for the allocation site <code>q</code>. */
	public int allocID(Quad q) {
	    HMethod hm = q.getFactory().getMethod();
	    Integer allocID = 
		(Integer) getMap4Method(hm).get(new Integer(q.getID()));
	    assert allocID != null : "Quad unknown: " + q + " #" + q.getID();
	    return allocID.intValue();
	}



	/////////////// PARSING METHODS ///////////////////////////////
	private void readMethodData(Linker linker, BufferedReader br) 
	    throws IOException {
	    HMethod hm = readMethod(linker, br);
	    int nb_allocs = readInt(br);
	    for(int i = 0; i < nb_allocs; i++) {
		int quadID = readInt(br);
		int counter = readInt(br);
		getMap4Method(hm).put
		    (new Integer(quadID), new Integer(counter));
	    }
	}

	private static HClass readClass(Linker linker, BufferedReader br)
	    throws IOException {
	    String class_name = br.readLine();
	    HClass hc = (HClass) primitives.get(class_name);
	    if(hc == null)
		hc = linker.forName(class_name);
	    return hc;
	}
	
	private static Map primitives;
	static {
	    primitives = new HashMap();
	    primitives.put("boolean", HClass.Boolean);
	    primitives.put("byte",    HClass.Byte);
	    primitives.put("char"   , HClass.Char);
	    primitives.put("double",  HClass.Double);
	    primitives.put("float",   HClass.Float);
	    primitives.put("int",     HClass.Int);
	    primitives.put("long",    HClass.Long);
	    primitives.put("short",   HClass.Short);
	    primitives.put("void",    HClass.Void);
	}
	
	private static HMethod  readMethod(Linker linker, BufferedReader br)
	    throws IOException {
	    HClass hc = readClass(linker, br);
	    String method_name = br.readLine();
	    int nb_params = readInt(br);
	    HClass[] ptypes = new HClass[nb_params];
	    for(int i = 0; i < nb_params; i++)
		ptypes[i] = readClass(linker, br);
	    return hc.getMethod(method_name, ptypes);
	}
    } // end of AllocationNumberingStub


    private static Map readInstrumentationResults
	(String instrumentationResultsFileName) throws IOException {
	BufferedReader br = 
	    new BufferedReader(new FileReader(instrumentationResultsFileName));
	Map allocID2count = new HashMap();
	int size = readInt(br);
	for(int i = 0; i < size; i++) {
	    int count = readInt(br);
	    allocID2count.put(new Integer(i), new Integer(count));
	}
	return allocID2count;
    }


    private static int readInt(BufferedReader br) throws IOException {
	return new Integer(br.readLine()).intValue();
    }


    public void printStatistics(Collection allocs, QuadVisitor visitor) {

	PrintWriter pw = new PrintWriter(System.out, true);

	class SiteStat implements Comparable {
	    public final Quad alloc_site;
	    public final int  alloc_count;
	    public SiteStat(Quad alloc_site, int alloc_count) {
		this.alloc_site  = alloc_site;
		this.alloc_count = alloc_count;
	    }
	    public int compareTo(Object o) {
		if(! (o instanceof SiteStat)) return -1;
		SiteStat s2 = (SiteStat) o;
		if (alloc_count < s2.alloc_count) return +1;
		else if (alloc_count > s2.alloc_count) return -1;
		else return 0;
	    }
	};

	int ss_size = 0;
	for(Iterator it = allocs.iterator(); it.hasNext(); ) {
	    Quad alloc_site = (Quad) it.next();
	    if(getCount(alloc_site) != 0) ss_size++;
	}
	SiteStat[] ss = new SiteStat[ss_size];

	long total_count = 0;
	int i = 0;
	for(Iterator it = allocs.iterator(); it.hasNext();) {
	    Quad alloc_site  = (Quad) it.next();
	    int  alloc_count = getCount(alloc_site);
	    if(alloc_count != 0) {
		total_count += alloc_count;
		ss[i++] = new SiteStat(alloc_site, alloc_count);
	    }
	}
	Arrays.sort(ss);

	long partial_count = 0;
	pw.println("Allocation Statistics BEGIN");
	for(i = 0; i < ss.length; i++) {
	    Quad site  = ss[i].alloc_site;
	    int count  = ss[i].alloc_count;
	    double frac = (count*100.0) / total_count;
	    partial_count += count;
	    pw.println
		(Debug.code2str(site) + "\n\t" + count +
		 " object(s) \n\t" + Debug.doubleRep(frac, 5, 2) + "%\n\t" +
		 site.getFactory().getMethod());
	    if(visitor != null)
		site.accept(visitor);
	    if((frac < 0.01) || 
	       (((double) partial_count / (double) total_count) > 0.90)) {
		i++;
		break;
	    }
	}
	pw.println
	    (i + ((i==1) ? " site allocates " : " sites allocate ") +
	     Debug.doubleRep((partial_count*100.0) / total_count, 5, 2) +
	     "% of all objects");
	pw.println("Allocation Statistics END");
	//pw.flush();
    }

    public void printStatistics(Collection allocs) {
	printStatistics(allocs, null);
    }

    /** Return a collection of all the allocation sites (quads) from
	the methods from the set <code>methods</code>.

	@param methods set of methods where we look for allocation
	sites
	@param hcf code factory that provides the code of the methods

	@return collection of all allocation sites (quads) from
	<code>methods</code> */
    public static Collection getAllocs(Set methods, HCodeFactory hcf) {
	List allocs = new LinkedList();
	for(Iterator it = methods.iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    Code code  = (Code) hcf.convert(hm);
	    if(code != null)
		allocs.addAll(code.selectAllocations());
	}
	return allocs;
    }
}
