package imagerec;

import org.omg.CosNaming.NamingContext;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManager;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NameComponent;

public class Server extends Node {
    private String args, name;

    public Server(String args, String name, Node out) {
	super(out);
	this.args = args;
	this.corbaName = name;
    }

    public synchronized void process(ImageData id) {
	try {
	    ORB orb = ORB.init(args, null);
	    POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
	    poa.the_POAManager().activate();
	    NamingContextExt namingContext = 
		NamingContextExtHelper.narrow(orb
				.resolve_initial_references("NameService"));
	    namingContext.rebind(namingContext.to_name(name),
				 ClientServerHelper.narrow(
				    poa.servant_to_reference(
					new ClientServerPOA() {
					public void process(ImageData id) {
					    Server.super.process(id);
					}
				    })));
	    orb.run();
	} catch (Exception e) {

	}
	super.process(id);
    }

}
