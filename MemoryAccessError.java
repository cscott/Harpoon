// MemoryAccessError.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>MemoryAccessError</code> is thrown when a NoHeapRealtimeThread
 *  tries to access the heap.
 * 
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class MemoryAccessError extends Error
    implements java.io.Serializable {

    /** Constructor, no description. */

    public MemoryAccessError() { 
	super(); 
    }
    
    /** Constructor with description. */

    public MemoryAccessError(String description) { 
	super(description); 
    }

}
