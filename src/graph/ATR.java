// ATR.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.corba.CommunicationsAdapter;
import imagerec.corba.CommunicationsModel;
import imagerec.corba.CORBA;

/** A {@link ATR} is a node which can be connected to from the BBN UAV software 
 *  (receiver node).  This node is a source of {@link ImageData}s.
 *
 *  It encapsulates a {@link CommunicationsModel} to provide the transport mechanism.
 *
 *  @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class ATR extends Server {
    private Node out;
    
    /** Construct an {@link ATR} node with the default {@link CommunicationsModel}
     *  and name of server for integration with existing BBN UAV software.
     * 
     *  @param args The argument list given at the command line.
     *              Should include -ORBInitRef NameService=corbaloc::[host]:[port]/NameService
     *              or -ORBInitRef NameService=file://dir/ns
     *  @param out the node to send images to.
     */
    public ATR(String args[], Node out) {
	this(new CORBA(args), "LMCO ATR", out);
    }
    
    /** Construct an {@link ATR} node with <code>name</code> as
     *  the name of the server.
     *
     *  @param cm a {@link CommunicationsModel} which provides the transport mechanism.
     *  @param name the name of the server.  The BBN UAV software will use this name to 
     *              connect.
     *  @param out the node to send images to.
     */
    public ATR(CommunicationsModel cm, String name, Node out) {
	super(cm, name, out);
	this.out = out;
    }

    /** The <code>process</code> call that sets up and starts the server.
     * 
     *  @param id This variable is ignored - use <code>run()</code> to
     *            start the server.
     */
    public synchronized void process(ImageData id) {
	try {
	    cm.runATRServer(name, new CommunicationsAdapter() {
		public synchronized void process(ImageData id) {
		    ATR.this.out.process(id);
		}
	    });
	} catch (Exception e) {
	    throw new Error(e);
	}
    }
}
