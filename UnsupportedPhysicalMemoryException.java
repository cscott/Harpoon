// UnsupportedPhysicalMemoryException.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>UnsupportedPhysicalMemoryException</code> is thrown when
 *  a PhysicalMemoryArea is constructed with invalid parameters.
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class UnsupportedPhysicalMemoryException extends Exception
    implements java.io.Serializable {
    
    /** Constructor, no description. */

    public UnsupportedPhysicalMemoryException() {
	super();
    }

    /** Constructor, with description. */

    public UnsupportedPhysicalMemoryException(String s) {
	super(s);
    }
};
