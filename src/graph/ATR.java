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
    private Node out2;
    
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
    public void process(ImageData id) {
	try {
	    cm.runATRServer(name, new CommunicationsAdapter() {
		    public void process(ImageData id) {
			//System.out.println("ATR Server #"+getUniqueID()+" received image #"+id.id);
			if (out != null) {
			    ATR.this.out.process(id);
			}
			if (out2 != null) {
			    ATR.this.out2.process(id);
			}
		    }
		});
	} catch (Exception e) {
	    throw new Error(e);
	}
    }


    public void setLeft(Node out) {
	super.setLeft(out);
	this.out = out;
    }

    public void setRight(Node out2) {
	super.setRight(out2);
	this.out2 = out2;
    }

    public Node linkL(Node out) {
	setLeft(out);
	return this;
    }

    public Node linkR(Node out2) {
	setRight(out2);
	return this;
    }

    public Node link(Node out, Node out2) {
	setLeft(out);
	setRight(out2);
	return this;
    }
}
