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
	    System.out.println("Usage: java -jar trackerStub.jar [timer] <CORBA name> [CORBA options]");
	    System.exit(-1);
	}

	Node timer;
	AlertServer alertServer;
	Node alertDisp = new AlertDisplay();
	boolean useTimer = args[0].equalsIgnoreCase("timer");

	if(useTimer){
	    timer = new Timer(false, true, "Tracking", null);
	    alertServer = new AlertServer(new CORBA(args), args[1], null);
	} else {
	    timer = new Node();
	    alertServer = new AlertServer(new CORBA(args), args[0], null);
	}

	alertServer.linkL(timer.linkL(alertDisp));
	alertServer.run();
    }
}
