package imagerec;

import org.omg.CosNaming.NamingContext;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NameComponent;

public class Client extends Node {
    private ORB orb;
    private final ClientServer cs;

    public Client(String name, String[] args) {
	super();
	try {
	    orb = ORB.init(args, null);
	    cs = ClientServerHelper.narrow(
		NamingContextExtHelper.narrow(orb
			       .resolve_initial_references("NameService"))
		.resolve(new NameComponent[] {new NameComponent(name, "")}));
	} catch (Exception e) {
	    throw new Error(e);
	}
    }

    public synchronized void process(final ImageData id) {
	(new Thread() {
	    public void run() {
		cs.process(id);
	    }
	}).start();
    }
}
