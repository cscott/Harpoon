// CORBA.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.corba;

import FrameManip.ProcessorPOA;
import FrameManip.ProcessorHelper;
import FrameManip.Frame;

import ATRManip.ATRSysCond;
import ATRManip.ATRSysCondHelper;
import ATRManip.Coordinate;

import org.omg.CosNaming.NamingContext;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManager;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NameComponent;

import imagerec.graph.ImageData;

import imagerec.util.ImageDataManip;

/** {@link CORBA} provides a transport mechanism for
 *  <a href="http://www.omg.org/gettingstarted/corbafaq.htm">CORBA</a> servers
 *  and clients used in the image recognition program.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class CORBA implements CommunicationsModel {
    private String[] args;

    /** Construct a new {@link CORBA} {@link CommunicationsModel}
     *
     *  @param args should contain the command-line arguments to the program.
     *              It should have an -ORBInitRef NameService=X
     *              where X is the location of the name service and could be 
     *              file://home/wbeebee/.jacorb, for example.
     */
    public CORBA(String[] args) {
	this.args=args;
    }

    /**
     *  Set up a server to receive {@link ImageData}s 
     *  (used primarily between image recognition components) via JacORB.
     *
     *  @param name The name to bind with the NameService.
     *  @param out  An adapter to send <code>process</code> requests to.
     */
    public void runIDServer(String name, final CommunicationsAdapter out) throws Exception {
	final ORB orb = ORB.init(args, null);
	POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
	poa.the_POAManager().activate();
	NamingContextExt namingContext = 
	    NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
	ClientServerPOA cs = new ClientServerPOA() {
	    public synchronized void process(ImageData id) {
		out.process(id);
	    }
	};
	namingContext.rebind(namingContext.to_name(name),
			     ClientServerHelper.narrow(poa.servant_to_reference(cs)));
	orb.run();
    }

    /**
     *  Set up a client for sending {@link ImageData}s to the server via JacORB.
     *  @param name The name that's bound to the NameService of the server to
     *              connect to.
     *  @return a {@link CommunicationsAdapter} which wraps the client method to be invoked.
     */
    public CommunicationsAdapter setupIDClient(String name) {
	try {
	    ORB orb = ORB.init(args, null);
	    final ClientServer cs = 
		ClientServerHelper.narrow(NamingContextExtHelper.narrow(orb
			     .resolve_initial_references("NameService"))
			     .resolve(new NameComponent[] {new NameComponent(name, "")}));
	    return new CommunicationsAdapter() {
		public synchronized void process(ImageData id) {
		    cs.process(id);
		}
	    };
	} catch (Exception e) {
	    throw new Error(e);
	}
    }

    /**
     *  Run a client that can connect to the BBN UAV OEP object tracker
     *  (via JacORB).
     *  
     *  @param name The name bound by the Alert server
     *  @return a {@link CommunicationsAdapter} which wraps the UAV Alert RMI call.
     */

    public CommunicationsAdapter setupAlertClient(String name) {
	try {
	    ORB orb = ORB.init(args, null);
	    final ATRSysCond atr = 
		ATRSysCondHelper.narrow(NamingContextExtHelper.narrow(orb
			     .resolve_initial_references("NameService"))
			     .resolve(new NameComponent[] {new NameComponent(name, "")}));
	    return new CommunicationsAdapter() {
		public synchronized void alert(float c1, float c2, float c3) {
		    atr.send_coordinate(new Coordinate(c1, c2, c3));
		}
	    };
	} catch (Exception e) {
	    throw new Error(e);
	}
    }

    /**
     *  Run a server that the BBN UAV receiver can connect to (via JacORB).
     *
     *  @param name The name bound by the ATR client (from the receiver node).
     *  @param out a {@link CommunicationsAdapter} to send the process requests to.
     */
    public void runATRServer(String name, final CommunicationsAdapter out) throws Exception {
	final ORB orb = ORB.init(args, null);
	POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
	poa.the_POAManager().activate();
	NamingContextExt namingContext = 
	    NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
	ProcessorPOA p = new ProcessorPOA() {
	    private int id = 0;

	    public FrameManip.Frame transform(FrameManip.Frame f) {
		return null; // Not currently used.
	    }

	    public void process(Frame f) {
		out.process(ImageDataManip.readPPM(f.data));
	    }
	};
	namingContext.rebind(namingContext.to_name(name),
			     ProcessorHelper.narrow(poa.servant_to_reference(p)));
	orb.run();
    }
}
