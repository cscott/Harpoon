// NoGarbageCollector.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>NoGarbageCollector</code> provides a means of informing
 *  the program that no garbage collector is present.
 */

public class NoGarbageCollector extends GarbageCollector {
    NoGarbageCollector() {
    }

//      public RelativeTime getPreemptionLatency() {
//  	return new RelativeTime();
//      }

    public RelativeTime getPreemptionLatency() {
	// TODO
	// Must be defined, because declared as abstract in GarbageCollection.java

	return new RelativeTime();
    }
}
