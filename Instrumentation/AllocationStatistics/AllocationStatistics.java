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
 * @version $Id: AllocationStatistics.java,v 1.3 2003-02-09 18:31:00 salcianu Exp $
 * @see InstrumentAllocs
 */
public class AllocationStatistics {

    /** Create an <code>AllocationStatistics</code> object.

	@param ani <code>AllocationNumberingInterf</code> object that
	provides the mapping between allocation sites and globally
	unique integer IDs.

	@param instrumentationResultFileName name of the file that
	holds the result of the instrumentation: for each unique ID,
	the number of times the associated allocation site was
	executed and the total amount of memory allocated there. */
    public AllocationStatistics(AllocationNumberingInterf ani,
				String instrumentationResultsFileName) {
	try {
	    this.ani = ani;
	    this.allocID2data = 
		parseInstrumentationResults(instrumentationResultsFileName);
	}
	catch(IOException e) {
	    System.err.println("Cannot create AllocStatistics: " + e);
	    System.exit(1);
	}
    }


    /** Create a <code>AllocationStatistics</code>.

	@param linker <code>Linker</code> used to load the classes.
	
	@param allocNumberingFileName name of the file that stores the
	textualized form of the <code>AllocationNumberingStub</code>
	(that associates to each allocation site a unique integer ID).
	
	@param instrumentationResultFileName name of the file that
	holds the result of the instrumentation: for each unique ID,
	the number of times the associated allocation site was
	executed. */
    public AllocationStatistics(Linker linker,
				String allocNumberingFileName,
				String instrumentationResultsFileName) {
	try { 
	    this.ani = 
		new AllocationNumberingStub(linker, allocNumberingFileName);
	    this.allocID2data = 
		parseInstrumentationResults(instrumentationResultsFileName);
	}
	catch(IOException e) {
	    System.err.println("Cannot create AllocStatistics: " + e);
	    System.exit(1);
	}
    }

    /** data computed by the instrumentation */
    private static class AllocData {
	public AllocData(final long objCount, final long memAmount) {
	    this.objCount  = objCount;
	    this.memAmount = memAmount;
	}
	public final long objCount;
	public final long memAmount;
    }


    // provides the map quad -> allocID (an integer)
    private AllocationNumberingInterf ani;
    // map allocID -> count
    private Map/*<Integer,AllocData>*/ allocID2data;


    /** Return the number of times the allocation <code>alloc</code>
        was executed.

	@param alloc allocation site

	@return number of times <code>alloc</code> was executed */
    public long getCount(Quad alloc) {
	AllocData ad = 
	    (AllocData) allocID2data.get(new Integer(ani.allocID(alloc)));
	return (ad == null) ? 0 : ad.objCount;
    }


    /** Return the total amount of memory allocated at the allocation
        <code>alloc</code>.

	@param alloc allocation site

	@return total amount of memory allocated at <code>alloc</code> */
    public long getMemAmount(Quad alloc) {
	AllocData ad = 
	    (AllocData) allocID2data.get(new Integer(ani.allocID(alloc)));
	return (ad == null) ? 0 : ad.memAmount;
    }


    /** Prints statitistics about the allocation sites from the
	collection <code>allocs</code>.  If <code>visitor</code> is
	non-null, it is called on each allocation site (this way, one
	can customize the displayed statistics).  The allocation sites
	are listed/visited in the decreasing order of the number of
	objects allocated there.  Sites that allocate too few objects
	(less than 1% of the total objects) are not considered. */
    public void printStatistics(Collection allocs, QuadVisitor visitor) {

	class SiteStat implements Comparable {
	    public final Quad allocSite;
	    public final long allocCount;
	    public final long memAmount;
	    public SiteStat(Quad allocSite,
			    long allocCount, long memAmount) {
		this.allocSite  = allocSite;
		this.allocCount = allocCount;
		this.memAmount  = memAmount;
	    }
	    public int compareTo(Object o) {
		if(! (o instanceof SiteStat)) return -1;
		SiteStat s2 = (SiteStat) o;
		if (allocCount < s2.allocCount) return +1;
		else if (allocCount > s2.allocCount) return -1;
		else return 0;
	    }
	};

	int ss_size = 0;
	for(Iterator it = allocs.iterator(); it.hasNext(); ) {
	    Quad allocSite = (Quad) it.next();
	    if(getCount(allocSite) != 0) ss_size++;
	}
	SiteStat[] ss = new SiteStat[ss_size];

	long total_count = 0;
	int i = 0;
	for(Iterator it = allocs.iterator(); it.hasNext();) {
	    Quad allocSite  = (Quad) it.next();
	    long allocCount = getCount(allocSite);
	    if(allocCount != 0) {
		total_count += allocCount;
		ss[i++] = new SiteStat(allocSite, allocCount,
				       getMemAmount(allocSite));
	    }
	}
	Arrays.sort(ss);

	long partial_count = 0;
	System.out.println("Allocation Statistics BEGIN");
	for(i = 0; i < ss.length; i++) {
	    Quad site  = ss[i].allocSite;
	    long count  = ss[i].allocCount;
	    double frac = (count*100.0) / total_count;
	    partial_count += count;
	    System.out.println
		(Debug.code2str(site) + "\n\t" + 
		 count + " object(s)\n\t" +
		 ss[i].memAmount + " byte(s)\n\t" +
		 Debug.doubleRep(frac, 5, 2) + "% of all objects\n\t" +
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


    /** Parse the text file produced by an instrumented program.

	@param instrumentationResultsFileName name of the file holding the
	instrumentation results
	
	@return map that attaches to each unique ID the data collected
	by the instrumentation for the corresponding allocation
	site. */
    public static Map/*<Integer,AllocData>*/ parseInstrumentationResults
	(String instrumentationResultsFileName) throws IOException {
	BufferedReader br = 
	    new BufferedReader(new FileReader(instrumentationResultsFileName));
	Map/*<Integer, AllocData>*/ allocID2data = new HashMap();
	int size = readInt(br);
	for(int i = 0; i < size; i++) {
	    long objCount  = readLong(br);
	    long memAmount = readLong(br);
	    allocID2data.put
		(new Integer(i), new AllocData(objCount, memAmount));
	}
	return allocID2data;
    }


    private static int readInt(BufferedReader br) throws IOException {
	return Integer.parseInt(br.readLine());
    }

    private static long readLong(BufferedReader br) throws IOException {
	return Long.parseLong(br.readLine());
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
