// MarkSweepGarbageCollector.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>MarkSweepGarbageCollector</code> provides a means of interfacing
 *  with the Mark and Sweep precise garbage collector written by Karen.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class MarkSweepGarbageCollector extends GarbageCollector {
    MarkSweepGarbageCollector() {
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
