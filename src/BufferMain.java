// BufferMain.java, created by Wes Beebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package imagerec;
import imagerec.corba.CORBA;
import imagerec.graph.*;

/**
 * This creates a buffer between the receiver and the ATR.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class BufferMain {
    /** This creates a buffer.
     *
     *  @param args Should include buffer size.
     */
    public static void main(String args[]) {
	if (args.length<2) {
	    System.out.println("Usage: java -jar buffer.jar ");
	    System.out.println("       <CORBA name for server> <CORBA name for client>");
	    System.out.println("       [CORBA options]");
	    System.exit(-1);
	}

	ATR server = new ATR(new CORBA(args), args[1], null);
	ATRClient client = new ATRClient(new CORBA(args), args[2]);
	Buffer buffer = new Buffer(Integer.parseInt(args[0]), null);

	server.linkL(buffer.linkL(client)).run();
    }
}
