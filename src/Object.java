// Object.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package java.lang;
import javax.realtime.MemoryArea;

/** Stub to be replaced in the compiler by the real java.lang.Object 
 *  with the field memoryArea added.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Object {

    /** */

    public MemoryArea memoryArea;

    /** */
    
    public String toString() {
	return "";
    }
    
    /** */

    public final Class getClass() {
	return null;
    }

    public Object clone() {
	return null;
    }

    // Borrowed from standart java.lang.Object;
    public boolean equals(Object obj) {
	return (this == obj);
    }

    // Borrowed from standart java.lang.Object;
    public final native void wait(long timeout) throws InterruptedException;

    // Borrowed from standart java.lang.Object;
    public final void wait(long timeout, int nanos) throws InterruptedException {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout value is negative");
        }

        if (nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException(
				"nanosecond timeout value out of range");
        }

	if (nanos >= 500000 || (nanos != 0 && timeout == 0)) {
	    timeout++;
	}

	wait(timeout);
    }
}
