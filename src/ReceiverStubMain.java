// ReceiverStubMain.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;

import imagerec.graph.*;

/**
 * This is the main program for the receiver stub that can send a series of images
 * to the ATR in place of the UAV OEP receiver.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class ReceiverStubMain {

    /** The entry point to the stub of the Receiver main program
     *
     *  @param args Should include parameters for contacting the CORBA nameservice.
     */
    public static void main(String args[]) {
	if (args.length<2) {
	    System.out.println("Usage: jar -x receiverStub.jar <jarFile|'none'> <filePrefix> <num> [CORBA options]");
	    System.exit(-1);
	}

	Node atr = new ATRClient(args);
	Node load;
	if (args[0].equalsIgnoreCase("none")) {
	    load = new Load(args[1], Integer.parseInt(args[2]), atr);
	} else {
	    load = new Load(args[0], args[1], Integer.parseInt(args[2]), atr);
	}
	while (true) {
	    load.run();
	}
    }
}
