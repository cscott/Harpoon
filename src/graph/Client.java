// Client.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.corba.CommunicationsModel;
import imagerec.corba.CommunicationsAdapter;

/**
 * {@link Client} is a {@link Node} that connects the in-node through
 * a named CORBA call to a {@link Server}, which connects to an out-node.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Client extends Node {
    private final CommunicationsAdapter cs;

    /** Construct a {@link Client} node with <code>name</code> as the identifier
     *  which locates the server to connect to.  Images passed to a client will be 
     *  automatically transmitted to the identified server node.
     *
     *  @param cm <code>CommunicationsModel</code> provides the transport mechanism
     *            for communicating between the server and the client.
     *  @param name An identifier which locates the server.  
     *  
     *  <code>args</code> should contain the command-line arguments to the program.
     *  It should have an -ORBInitRef NameService=X
     *  where X is the location of the name service and could be 
     *  file://home/wbeebee/.jacorb, for example.
     */
    public Client(CommunicationsModel cm, String name) {
	super();
	try {
	    cs = cm.setupIDClient(name);
	} catch (Exception e) {
	    throw new Error(e);
	}
    }

    /** The <code>process</code> call that actually triggers a CORBA call to a server. */
    public synchronized void process(final ImageData id) {
	(new Thread() {
	    public void run() {
		cs.process(id);
	    }
	}).start();
    }
}
