// ATRClient.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.corba.CommunicationsModel;
import imagerec.corba.CORBA;

/**
 * {@link ATRClient} is a {@link Client} that connects the in-node
 * to an {@link ATR} using the BBN OEP UAV ATR interface.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class ATRClient extends Client {
    /** Construct an {@link ATRClient} node with the default 
     *  {@link CommunicationsModel} and name of server for integration with
     *  existing BBN UAV software.
     *  
     *  @param args The argument list given at the command line.
     *              Should include -ORBInitRef NameService=corbaloc::[host]:[port]/NameService
     *              or -ORBInitRef NameService=file://dir/ns
     */
    public ATRClient(String args[]) {
	this(new CORBA(args), "LMCO ATR");
    }

    /** Construct an {@link ATRClient} node with <code>name</code> as the 
     *  identifier which locates the server to connect to.  Images passed to 
     *  a client will be automatically transmitted to the identified {@link ATR}
     *  server node.
     *
     *  @param cm <code>CommunicationsModel</code> provides the transport
     *            mechanism for communicating between the server and the client.
     *  @param name An identifier which locates the server.
     */
    public ATRClient(CommunicationsModel cm, String name) {
	super();
	try {
	    cs = cm.setupATRClient(name);
	} catch (Exception e) {
	    throw new Error(e);
	}
    }
}
