// ReceiverStubZENMain.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;

import imagerec.corba.CORBA;

/**
 * This is the main program for the receiver stub that can send a series of images to 
 * the ATR in place of the UAV OEP receiver.  This entry point uses ZEN as the CORBA 
 * implementation.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class ReceiverStubZENMain {

    /** The entry point to the stub of the Receiver main program
     * 
     *  @param args Should include parameters for contacting the CORBA nameservice using Zen
     */
    public static void main(String args[]) {
	CORBA.implementation = CORBA.ZEN;
	ReceiverStubMain.main(args);
    }
}
