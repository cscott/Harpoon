// AllocationStatistics.java, created Sat Nov 30 22:21:12 2002 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Instrumentation.AllocationStatistics;

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
import java.util.Collection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * <code>AllocationStatistics</code> reads the output produced by the
 * allocation instrumentation and offers support for gathering and
 * displaying statistics about the number of times each allocation
 * site from an instrumented program was executed.
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: AllocationStatistics.java,v 1.1 2003-02-03 16:20:31 salcianu Exp $
 * @see InstrumentAllocs
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


    /** Prints statitistics about the allocation sites from the
	collection <code>allocs</code>.  If <code>visitor</code> is
	non-null, it is called on each allocation site (this way, one
	can customize the displayed statistics).  The allocation sites
	are listed/visited in the decreasing order of the number of
	objects allocated there.  Sites that allocate too few objects
	(less than 1% of the total objects) are not considered.
     */
    public void printStatistics(Collection allocs, QuadVisitor visitor) {

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
	System.out.println("Allocation Statistics BEGIN");
	for(i = 0; i < ss.length; i++) {
	    Quad site  = ss[i].alloc_site;
	    int count  = ss[i].alloc_count;
	    double frac = (count*100.0) / total_count;
	    partial_count += count;
	    System.out.println
		(Debug.code2str(site) + "\n\t" + count +
		 " object(s) \n\t" + Debug.doubleRep(frac, 5, 2) + "%\n\t" +
		 site.getFactory().getMethod());
	    if(visitor != null)
		site.accept(visitor);
	    if((frac < 1) || // don't print sites with < 1% allocations
	       (((double) partial_count / (double) total_count) > 0.90)) {
		i++;
		break;
	    }
	}
	System.out.println
	    (i + ((i==1) ? " site allocates " : " sites allocate ") +
	     Debug.doubleRep((partial_count*100.0) / total_count, 5, 2) +
	     "% of all objects");
	System.out.println("Allocation Statistics END");
    }


    public void printStatistics(Collection allocs) {
	printStatistics(allocs, null);
    }

    /** Return a collection of all the allocation sites (quads) from
	the methods from the set <code>methods</code>.

	@param methods methods where we look for allocation sites
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
