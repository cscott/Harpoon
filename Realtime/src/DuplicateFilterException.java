// DuplicateFilterException.java, created by Dumitru Daniliuc
// Copyright (C) 2003 Dumitru Daniliuc
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>PhysicalMemoryManger</code> can only accommodate one
 *  filter object for each type of memory. It throws this exception
 *  if an attempt is made to register more than one filter for a
 *  type of memory.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class DuplicateFilterException extends Exception {

    /** A constructor for <code>DuplicateFilterException</code>. */
    public DuplicateFilterException() {
	super();
    }

    /** A descriptive constructor for <code>DuplicateFilterException</code>.
     *
     *  @param s Description of the error.
     */
    public DuplicateFilterException(String s) {
	super(s);
    }
}
