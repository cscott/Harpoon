// EmbeddedMain.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;

import imagerec.graph.*;

/**
 * This is the main program that should run on the embedded processor in the UAV.
 * It has to compete with the hard real-time load of the mission computer.
 *
 * This is the main program for the embedded component of the two-part ATR.
 * It has been optimized for "bang/bunk" and does coarse image recognition 
 * with limited CPU.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class EmbeddedATRMain {

    /** The entry point to the embedded part of the two-component version of the ATR.
     *
     *  @param args Should include parameters for contacting the CORBA nameservice.
     */
    public static void main(String args[]) {
	System.out.println("Interface has not been defined yet...");
	System.exit(-1);
    }
}
