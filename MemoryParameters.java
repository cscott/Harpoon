// MemoryParameters.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>MemoryParameters</code>
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class MemoryParameters {

    /** */

    public static final long NO_MAX = -1;

    /** */

    private long allocationRate;

    /** */

    private long maxImmortal;

    /** */

    private long maxMemoryArea;

    /** */

    private MemoryArea memoryArea;

    /** */

    public MemoryParameters(MemoryArea memoryArea) {
	this.memoryArea = memoryArea;
    }

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

    /** */

    public long getAllocationRate() {
	return allocationRate;
    }

    /** */

    public long getMaxImmortal() {
	return maxImmortal;
    }

    /** */

    public long getMaxMemoryArea() {
	return maxMemoryArea;
    }

    /** */

    public MemoryArea getMemoryArea() {
	return memoryArea;
    }

    /** */

    public void setAllocationRate(long rate) {
	allocationRate = Math.max(rate, NO_MAX);
    }
}
