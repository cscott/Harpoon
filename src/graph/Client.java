// Client.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.corba.CommunicationsModel;
import imagerec.corba.CommunicationsAdapter;
import imagerec.corba.CORBA;

/**
 * {@link Client} is a {@link Node} that connects the in-node through
 * a named CORBA call to a {@link Server}, which connects to an out-node.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class Client extends Node {
    protected CommunicationsAdapter cs;

    /** Provided so that subclasses can create their own {@link CommunicationsAdapter}.
     */
    protected Client() {}

    /** Construct a {@link Client} node with <code>name</code> as the identifier
     *  which locates the server to connect to.  Images passed to a client will be 
     *  automatically transmitted to the identified server node.
     *
     *  @param cm <code>CommunicationsModel</code> provides the transport mechanism
     *            for communicating between the server and the client.
     *  @param name An identifier which locates the server.  
     *
     *  If instantiating a {@link CORBA} client, <code>args</code> should contain the 
     *  command-line arguments to the program.
     *  It should have an -ORBInitRef NameService=X
     *  where X is the location of the name service and could be 
     *  file://home/wbeebee/.jacorb, for example.
     */
    public Client(CommunicationsModel cm, String name) {
	long timeoutLength = 2000;
	int triesTillFail = 500;
	int count = 1;
	boolean connectionMade = false;
	while ((count <= triesTillFail) && !connectionMade) { 
	    try {
		cs = cm.setupIDClient(name);
		connectionMade = true;
	    } catch (org.omg.CosNaming.NamingContextPackage.NotFound e){
		System.out.println("Client/CORBA Exception: No matching server by the name '"+name+"'"+
				   " was found.\n  Waiting and trying again. (Try #"+count+"/"+triesTillFail+")");
		try {
		    Thread.currentThread().sleep(timeoutLength);
		}
		catch (InterruptedException ie) {
		}
		count++;

	    } catch (Exception e2) {
		System.out.println("**** "+e2.getClass()+" ******");
		throw new Error(e2);
	    }
	}

	if (!connectionMade) {
	    int minutes = (int)timeoutLength*triesTillFail/1000/60;
	    throw new Error("Client was unable to find a matching server after "+minutes+" minutes. Giving up.");
	}
    }

    /** The <code>process</code> call that actually triggers a call to a server. */
    public void process(final ImageData id) {
	//System.out.println("Client #"+getUniqueID()+" sending image #"+id.id);
	final CommunicationsAdapter finalCS = cs;
// 	(new Thread() {
// 	    public void run() {
// 		synchronized (Client.this) {
		    finalCS.process(id);
		    //System.out.println("Client #"+getUniqueID()+" sent image #"+id.id);
// 		}
// 	    }
// 	}).start();

	if (getLeft() != null)
	    getLeft().process(id);
	if (getRight() != null)
	    getRight().process(id);
    }
}
