// Counters.java, created Thu Feb 22 21:48:58 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Runtime;

import java.lang.reflect.Field;
/**
 * <code>Counters</code> uses reflection to provide a very light-weight
 * counter package -- light-weight in terms of FLEX code required to
 * add/enable counters, not necessarily light-weight in terms of
 * execution time.  All counters are thread-safe.  We use reflection
 * at initialization time and at program termination to initialize
 * and report counter values, which minimizes the amount of
 * counter-specific code generation required by the FLEX side of
 * this.  No special runtime support is required.
 * <p>
 * The dynamic generation of counter fields in this class is
 * handled by <code>harpoon.Analysis.Counters.CounterFactory</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Counters.java,v 1.1.2.1 2001-02-23 06:17:00 cananian Exp $
 */
public class Counters {
    /** Initialize by using reflection to store new <code>Object</code>s in
     *  all (FLEX-generated) fields of this class with names starting
     *  with "LOCK_".
     */
    static {
	Field[] field = Counters.class.getDeclaredFields();
	for (int i=0; i<field.length; i++)
	    if (field[i].getName().startsWith("LOCK_"))
		try {
		    field[i].set(null, new Object());
		} catch (IllegalAccessException e) {
		    System.err.println("SKIPPING INIT OF "+field[i]);
		}
    }

    /** Report counter values to <code>System.err</code>, using reflection
     *  to discover all fields starting with "COUNTER_". */
    public static void report() {
	Field[] field = Counters.class.getDeclaredFields();
	for (int i=0; i<field.length; i++)
	    if (field[i].getName().startsWith("COUNTER_"))
		try {
		    String name = field[i].getName().substring(8);
		    Field Flck = Counters.class.getDeclaredField("LOCK_"+name);
		    synchronized(Flck.get(null)) {
			System.err.println(name+": "+field[i].get(null));
		    }
		} catch (NoSuchFieldException e) {
		    System.err.println("CAN'T FIND FIELD: "+e);
		} catch (IllegalAccessException e) {
		    System.err.println("CAN'T READ FIELD: "+e);
		}
    }
}
