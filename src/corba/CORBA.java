// CORBA.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.corba;

import org.omg.CosNaming.NamingContext;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManager;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NameComponent;

import imagerec.graph.ImageData;

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
     *  @param name
     *  @param out 
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
    }

    /**
     *  @param name
     *  @return a {@link CommunicationsAdapter} which wraps the client method to be invoked.
     */
    public CommunicationsAdapter setupIDClient(String name) {
	try {
	    ORB orb = ORB.init(args, null);
	    final ClientServer cs = ClientServerHelper.narrow(NamingContextExtHelper.narrow(orb
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

}
