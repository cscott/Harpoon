// NSZENMain.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec;

import imagerec.corba.CORBA;

/** 
 * This is an easy-to-use front-end to the ZEN CORBA name services.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class NSZENMain {
    /** The front end to the ZEN CORBA nameservice.
     *
     *  @param args Should contain a location to establish initial contact.
     */
    public static void main(String args[]) {
	CORBA.implementation = CORBA.ZEN;
	NSMain.main(args);
    }
}
