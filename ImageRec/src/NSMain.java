// NSMain.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;

import imagerec.corba.CORBA;

import org.jacorb.naming.NameServer;
import edu.uci.ece.zen.naming.NamingService;

/**
 * This is an easy-to-use front-end to the JacORB/ZEN CORBA naming services.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class NSMain {
    /** The front end to the CORBA nameservice.
     *
     *  @param args Should contain a location to establish initial contact.
     */
    public static void main(String args[]) {
	if (args.length<1) {
	    System.out.println("Usage: java -jar ns.jar <contact>");
	    System.out.println();
	    System.out.println("Where: <contact> refers to the initial point of contact.");
	    System.out.println("       ex: ~/.jacorb");
	    System.exit(-1);
	}

	switch (CORBA.implementation) {
	case CORBA.JACORB: {
	    CORBA.setupJacORB();
	    NameServer.main(args);
	    break;
	}
	case CORBA.ZEN: {
	    CORBA.setupZen();
	    NamingService.main(args);
	    break;
	}
	default: {
	    System.out.println("Error: No CORBA implementation selected.");
	    System.exit(-1);
	}
	}
    }
}
