// OffsetOutOfBoundsException.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>OffsetOutOfBoundsException</code> means that the value given
 *  in the offset parameter of a RawMemoryAccess method is either 
 *  negative or outside the memory area.
 */

public class OffsetOutOfBoundsException extends Exception
    implements java.io.Serializable {

    /** Constructor, no description. */
    public OffsetOutOfBoundsException() {
	super();
    }

    /** Constructor with description. */
    public OffsetOutOfBoundsException(String s) {
	super(s);
    }
};
