// Alert.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.corba.CommunicationsAdapter;
import imagerec.corba.CommunicationsModel;
import imagerec.corba.CORBA;

/**
 * {@link Alert} is a {@link Client} which sets an alert syscond
 * for every input image.  This effectively tells the tracking
 * system: "I've found it - and here's how far away it is!".
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class Alert extends Client {
    /** Construct an {@link Alert} node with the default {@link CommunicationsModel}
     *  and name of server for integration with existing BBN UAV software. 
     *
     *  @param args The argument list given at the command line.
     *              Should include -ORBInitRef NameService=corbaloc::[host]:[port]/NameService
     *              or -ORBInitRef NameService=file://dir/ns
     */
    public Alert(String args[]) {
	this(new CORBA(args), "ATR Alert");
    }

    /** Construct an {@link Alert} node with <code>name</code> as 
     *  the name of the alert server to connect to.
     *
     *  @param cm a {@link CommunicationsModel} which provides the transport mechanism.
     *  @param name the name of the server to connect to.
     */
    public Alert(CommunicationsModel cm, String name) {
	try {
	    cs = cm.setupAlertClient(name);
	} catch (Exception e) {
	    throw new Error(e);
	}
    }

    /** The <code>process</code> call that results in setting the Alert syscond
     *  with information from the input image: <code>(c1, c2, c3)</code>
     *  set with the relative target location.
     *
     *  @param id The input image that contains data for the tracker.
     */
    public void process(final ImageData id) {
	final CommunicationsAdapter finalCS = cs;
	(new Thread() {
	    public void run() {
		finalCS.alert(id.c1, id.c2, id.c3);
	    }
	}).start();
    }
}
