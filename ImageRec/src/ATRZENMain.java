// ATRZENMain.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;

import imagerec.corba.CORBA;

/**
 * This is the main program for the MIT ATR one-component system
 * for use in the BBN UAV OEP, ZEN version.
 *  
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class ATRZENMain {

    /** The entry point to the ATR main program.
     *
     *  @param args Should include parameters for contacting
     *              the CORBA nameservice.
     */
    public static void main(String args[]) {
	CORBA.implementation = CORBA.ZEN;
	ATRMain.main(args);
    }
}
