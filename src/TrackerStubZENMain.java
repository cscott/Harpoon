// TrackerStubZENMain.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;

import imagerec.corba.CORBA;

/** 
 * This is the main program for the tracker stub that can receive a series of alerts
 * from the ATR in place of the UAV OEP tracker.  It uses the ZEN ORB.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class TrackerStubZENMain {

    /** The entry point to the stub of the Tracker main program
     *
     *  @param args Should include parameters for contacting the ZEN CORBA nameservice.
     */
    public static void main(String args[]) {
	CORBA.implementation = CORBA.ZEN;
	TrackerStubMain.main(args);
    }
}
