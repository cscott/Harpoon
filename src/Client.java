package imagerec;

import org.omg.CosNaming.NamingContext;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NameComponent;

public class Client extends Node {
    private ORB orb;
    private ClientServer cs;

    public Client(String name, String[] args) {
	super();
	this.corbaName = corbaName;
	try {
	    orb = ORB.init(args, null);
	    cs = ClientServerHelper.narrow(
		NamingContextExtHelper.narrow(orb
			       .resolve_initial_references("NameService"))
		.resolve(new NameComponent[] {new NameComponent(name, "")}));
	} catch (Exception e) {
	    System.out.println(e);
	    System.exit(-1);
	}
    }

    public synchronized void process(ImageData id) {
	cs.process(id);
    }
}
