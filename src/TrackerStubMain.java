// TrackerStubMain.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;

import imagerec.corba.CORBA;
import imagerec.graph.*;

/**
 * This is the main program for the tracker stub that can receive a series of alerts
 * from the ATR in place of the UAV OEP tracker.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class TrackerStubMain {

    /** The entry point to the stub of the Tracker main program
     * 
     *  @param args Should include parameters for contacting the CORBA nameservice.
     */
    public static void main(String args[]) {
	if (args.length<2) {
	    System.out.println("Usage: java -jar trackerStub.jar <CORBA name> [CORBA options]");
	    System.exit(-1);
	}

	AlertServer alertServer = new AlertServer(new CORBA(args), args[0], null);
	Node alertDisp = new AlertDisplay();

	alertServer.linkL(alertDisp);
	alertServer.run();
    }
}
