// MemoryParameters.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>MemoryParameters</code> stores parameters to pace the rate
 *  of memory allocation.  MemoryParameters are passed into the 
 *  <code>RealtimeThread</code> constructor.
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class MemoryParameters {

    /** Specifies no maximum limit */

    public static final long NO_MAX = -1;

    /** */

    private long allocationRate;

    /** */

    private long maxImmortal;

    /** */

    private long maxMemoryArea;

    /** */

    private MemoryArea memoryArea;


    // CONSTRUCTORS IN SPECS

    /** */

    public MemoryParameters(long maxMemoryArea, long maxImmortal) 
	throws IllegalArgumentException {
	this.maxMemoryArea = Math.max(maxMemoryArea, NO_MAX);
	this.maxImmortal = Math.max(maxImmortal, NO_MAX);
	this.allocationRate = NO_MAX;
    }

    /** */

    public MemoryParameters(long maxMemoryArea, long maxImmortal, 
			    long allocationRate) 
	throws IllegalArgumentException {
	this.maxMemoryArea = Math.max(maxMemoryArea, NO_MAX);
	this.maxImmortal = Math.max(maxImmortal, NO_MAX);
	this.allocationRate = Math.max(allocationRate, NO_MAX);
    }


    // CONSTRUCTORS NOT IN SPECS

    /** */

    public MemoryParameters(MemoryArea memoryArea) {
	this.memoryArea = memoryArea;
    }


    // METHODS IN SPECS


    /** Get the allocation rate. Units are in bytes per second. */

    public long getAllocationRate() {
	return allocationRate;
    }

    /** Get the limit on the amount of memory the thread may allocate
     *  in the immortal area. Units are in bytes.
     */
    public long getMaxImmortal() {
	return maxImmortal;
    }

    /** Get the limit on the amount of memory the thread may allocate
     *  in the memory area. Units are in bytes.
     */
    public long getMaxMemoryArea() {
	return maxMemoryArea;
    }

    /** A limit on the rate of allocation in the heap. */
    public void setAllocationRate(long rate) {
	allocationRate = Math.max(rate, NO_MAX);
    }

    public boolean setAllocationRateIfFeasible(int allocationRate) {
	// TODO
	// How do we check that the task set is still feasible?

	return false;
    }

    public boolean setMaxImmortalIfFeasible(long maximum) {
	// TODO
	// How do we check that the task set is still feasible?

	return false;
    }

    public boolean setMaxMemoryAreaIfFeasible(long maximum) {
	// TODO
	// How do we check that the task set is still feasible?

	return false;
    }


    // METHODS NOT IN SPECS

    /** */

    public MemoryArea getMemoryArea() {
	return memoryArea;
    }

}
