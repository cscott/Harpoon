// TeeMain.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;

import imagerec.graph.*;
import imagerec.corba.CORBA;

/**
 * This is the main for an image stream duplicator.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class TeeMain {

    /** The entry point of the image stream duplicator.
     *
     *  @param args Should include parameters for creating a server 
     *              and two clients.
     */
    public static void main(String args[]) {
	if (args.length<4) {
	    System.out.println("Usage: java -jar tee.jar");
	    System.out.println("       <CORBA name for server>");
	    System.out.println("       <CORBA name for client1>");
	    System.out.println("       <CORBA name for client2>");
	    System.out.println("       [CORBA options]");
	    System.exit(-1);
	}
	
	(new ATR(new CORBA(args), args[0], 
		 new Node(new Async(new ATRClient(new CORBA(args), args[1])),
			  new ATRClient(new CORBA(args), args[2])))).run();

    }
}
