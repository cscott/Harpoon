// MZFCompressor.java, created Fri Nov  9 21:23:56 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.SizeOpt;

import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.Linker;
import harpoon.Util.Default;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 * The <code>MZFCompressor</code> class implements a class transformation
 * aimed at eliminating "mostly-zero" (or "mostly (any constant)")
 * fields from classes (thus reducing memory consumption).  Each class
 * is transformed into several, each with one additional field added.
 * Thus we only need allocate fields that this particular instance
 * will actually use.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MZFCompressor.java,v 1.1.2.1 2001-11-10 17:31:46 cananian Exp $
 */
public class MZFCompressor {
    final HCodeFactory parent;
    
    /** Creates a <code>MZFCompressor</code>, using the field profiling
     *  information found in the resource at <code>resourcePath</code>.
     *  This resource should be a unprocessed file in the format output
     *  by the <code>SizeCounters</code> package. */
    public MZFCompressor(Linker linker, HCodeFactory hcf, ClassHierarchy ch,
			 String resourcePath) {
	this.parent = hcf;
	ConstructorClassifier cc = new ConstructorClassifier(hcf, ch);
        ProfileParser pp = new ProfileParser(linker, resourcePath);
	// okay, process this data.  what we want is a list of fields
	// sorted by savedbytes.
	// xx debug: show java.lang.String.
	System.out.println("SORTED FIELDS of java.lang.String: "+
			   sortFields(linker.forName("java.lang.String"),pp,cc)
			   );
	//List sorted = sortFields(xx, pp, new ConstructorClassifier());
    }
    public HCodeFactory codeFactory() { return parent; }

    /** Return a list.  element 0 of the list is the field most likely
     *  to save the most bytes. Elements are pairs; first element is
     *  the field; second element is the 'mostly value'. */
    List sortFields(HClass hc, ProfileParser pp, ConstructorClassifier cc) {
	// make a comparator that operates on Map.Entries contained in the
	// ProfileParser's valueInfo map, which sorts on 'saved bytes'.
	final Comparator c = new Comparator() {
		public int compare(Object o1, Object o2) {
		    // compare based on values in the entry; these
		    // are Longs representing 'saved bytes'
		    return ((Comparable) ((Map.Entry)o1).getValue())
			.compareTo(((Map.Entry)o2).getValue());
		}
	    };
	// okay, now make a list of fields and their maximal 'saved bytes'
	// mostly-N entry.
	HField[] flds = hc.getDeclaredFields();
	List l = new ArrayList(flds.length);
	for (Iterator it=Arrays.asList(flds).iterator(); it.hasNext(); ) {
	    HField hf = (HField) it.next();
	    // filter out 'bad' fields.
	    if (!cc.isGood(hf)) continue;
	    // find the 'mostly value' which will save the most space.
	    Map.Entry me = (Map.Entry)
		Collections.max(pp.valueInfo(hf).entrySet(), c);
	    // add an entry to l for sorting.
	    l.add(Default.pair(hf, me));
	}
	// okay, now sort the fields by saved bytes.
	Collections.sort(l, new Comparator() {
		// compare second element of pair.
		// (negate comparator so largest 'saved bytes' field is first)
		public int compare(Object o1, Object o2) {
		    return -c.compare(((List)o1).get(1), ((List)o2).get(1));
		}
	    });
	// finally, create a final list by stripping the 'saved bytes' info
	// (which we no longer need) and keeping only the field/mostlyN info.
	List nl = new ArrayList(l.size());
	for (Iterator it=l.iterator(); it.hasNext(); ) {
	    List pair = (List) it.next();
	    HField hf = (HField) pair.get(0);
	    Map.Entry me = (Map.Entry) pair.get(1);
	    Integer mostlyN = (Integer) me.getKey();
	    nl.add(Default.pair(hf, mostlyN));
	}
	// ta-da!
	return Collections.unmodifiableList(nl);
    }
}
