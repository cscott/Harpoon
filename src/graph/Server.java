// Server.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.corba.CommunicationsAdapter;
import imagerec.corba.CommunicationsModel;

/** A {@link Server} is a node which can be connected to from a {@link Client} node.
 *  {@link ImageData}s flow from {@link Client} to {@link Server}
 *
 *  It encapsulates a {@link CommunicationsModel} to provide the transport mechanism.
 *
 *  @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class Server extends Node {
    protected String name;
    protected CommunicationsModel cm;

    /** Construct a {@link Server} node with <code>name</code> as
     *  the name of the server.
     * 
     *  @param cm a {@link CommunicationsModel} which provides the transport mechanism.
     *  @param name the name of the server.  A {@link Client} will use this name to connect.
     *  @param out the node to send images to. 
     *
     */
    public Server(CommunicationsModel cm, String name, Node out) {
	super(out);
	this.name = name;
	this.cm = cm;
    }

    /** The <code>process</code> call that sets up and starts the server. 
     *
     *  @param id This variable is ignored - use <code>run()</code> to
     *            start the server.  
     */
    public void process(ImageData id) {
	try {
	    (new Thread() {
		public void run() {
		    try {
			cm.runIDServer(name, new CommunicationsAdapter() {
			    public void process(ImageData id) {
				Server.super.process(id);
			    }
			});
		    } catch (Exception e) {
			throw new Error(e);
		    } 
		}
	    }).start();
	} catch (Exception e) {
	    throw new Error(e);
	}
    }
}
