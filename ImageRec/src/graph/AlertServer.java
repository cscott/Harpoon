// AlertServer.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.corba.CommunicationsAdapter;
import imagerec.corba.CommunicationsModel;
import imagerec.corba.CORBA;

import imagerec.util.ImageDataManip;

import Img.ImageHeader;

/** An {@link AlertServer} is a {@link Server} which can be connected to 
 *  from a {@link Server} node.
 *  {@link ImageData}s flow from {@link Alert} to {@link AlertServer}.
 *
 *  It encapsulates a {@link CommunicationsModel} to provide the transport mechanism.
 *
 *  @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class AlertServer extends Server {
    private Node out;
    private Node out2;

    /** Construct an {@link AlertServer} node with the default {@link CommunicationsModel}
     *  and name of server as a test for integration with existing BBN UAV software.
     *  An {@link AlertServer} is a stub with the same interface as the "tracker"
     *  in the original BBN UAV pipeline.
     *
     *  @param args The argument list given at the command line.
     *              Should include -ORBInitRef NameService=corbaloc::[host]:[port]/NameService
     *              or -ORBInitRef NameService=file://dir/ns
     *  @param out  the node to send alerts to.
     */
    public AlertServer(String args[], Node out) {
	this(new CORBA(args), "ATR Alert", out);
    }

    /** Construct an {@link AlertServer} node with <code>name</code> as
     *  the name of the server.
     *
     *  @param cm a {@link CommunicationsModel} which provides the transport mechanism.
     *  @param name the name of the server.  An {@link Alert} will use this name to 
     *              connect.
     *  @param out  the node to send alerts to.
     */
    public AlertServer(CommunicationsModel cm, String name, Node out) {
	super(cm, name, out);
	this.out = out;
    }

    /** The <code>process</code> call that sets up and starts the server.
     *
     *  @param id This variable is ignored - use <code>run()</code>
     *            to start the server.
     */
    public void process(ImageData id) {
	try {
	    (new Thread() {
		public void run() {
		    try {
			cm.runAlertServer(name, new CommunicationsAdapter() {
			    public void alert(float c1, float c2, float c3, long time, ImageHeader header) {
				//System.out.println("Alert Server #"+getUniqueID()+" received image.");
				ImageData newID = ImageDataManip.create(c1, c2, c3, time, header);
				if (out != null) {
				    AlertServer.this.out.process(newID);
				}
				if (out2 != null) {
				    AlertServer.this.out2.process(newID);
				}
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
