// UnsupportedPhysicalMemoryException.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/**
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

/** Thrown when the underlying hardware does not support the type of physical
 *  memory given to the physical memory <code>create()</code> method.
 */
public class UnsupportedPhysicalMemoryException extends Exception
    implements java.io.Serializable {
    
    /** A constructor for <code>UnsupportedPhysicalMemoryException</code>. */
    public UnsupportedPhysicalMemoryException() {
	super();
    }

    /** A descriptive constructor for <code>UnsupportedPhysicalMemoryException</code>. */
    public UnsupportedPhysicalMemoryException(String s) {
	super(s);
    }
};
