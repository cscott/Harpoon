// BDWGarbageCollector.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>BDWGarbageCollector</code> provides a means of interfacing with
 *  the Boehm-D-Waters garbage collector. 
 */

public class BDWGarbageCollector extends GarbageCollector {
    BDWGarbageCollector() {
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
