// EmbeddedManualMain.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;

import imagerec.corba.CORBA;
import imagerec.graph.*;

/**
 * This is the main program for manual ground control of the car.
 *
 * It can be used to test the car and as a prototype for adding 
 * embedded image recognition/tracking capabilities
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class EmbeddedManualMain {

    /** The entry point to the embedded controller for the car.
     * 
     *  @param args Should include parameters for contacting the CORBA nameservice.
     */
    public static void main(String args[]) {
	if (args.length<2) {
	    System.out.println("Usage: java -jar embeddedManual.jar [CORBA options]");
	    System.exit(-1);
	}

	(new Async(new Server(new CORBA(args), "Servo control", new Servo()))).run();
	(new Async(new Camera(new Client(new CORBA(args), "Camera")))).run();
    }
}
