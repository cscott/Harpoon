package imagerec;

import org.omg.CosNaming.NamingContext;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManager;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NameComponent;

/** This class contains all the magic involved with getting CORBA to do an RMI.
 *  @author Wes Beebee <wbeebee@mit.edu>
 */

public class ClientServer extends _ClientServerIntStub {
    protected String args[];
    private ORB serverORB, clientORB;
    protected ClientServerInt cs = null;

    public ClientServer(String args[]) {
	this.args = args;
    }

    public final synchronized void client(String name) {
	try {
	    clientORB = ORB.init(args, null);
	    cs = ClientServerIntHelper.narrow(
		NamingContextExtHelper.narrow(clientORB
			       .resolve_initial_references("NameService"))
		.resolve(new NameComponent[] {new NameComponent(name, "")}));
	} catch (Exception e) {
	    System.out.println(e);
	    System.exit(-1);
	}
    }

    public final synchronized void server(String name) {
	if (args.length < 2) {
	    System.out.println();
	    System.out.println("Need to specify location of Naming Service with:");
	    System.out.println("-ORBInitRef NameService=file://dir/ns");
	    System.out.println();
	    System.exit(1);
	}

	try {
	    serverORB = ORB.init(args, null);
	    POA rootpoa = 
		POAHelper.narrow(serverORB.resolve_initial_references("RootPOA"));
	    rootpoa.the_POAManager().activate();
	    NamingContextExt namingContext = 
		NamingContextExtHelper.narrow(serverORB
				       .resolve_initial_references("NameService"));
	    namingContext.rebind(namingContext.to_name(name),
				 ClientServerIntHelper.narrow(
				   rootpoa.servant_to_reference(
					         new ClientServerIntPOATie(this))));
	    (new Thread() {
		public void run() {
		    serverORB.run();
		}
	    }).start();
	} catch (Exception e) {
	    System.out.println(e);
	    System.exit(-1);
	}
    }

    public final synchronized void newServer(String name) {
	serverORB.shutdown(true);
	server(name);
    }

    public final synchronized void newClient(String name) {
	clientORB.shutdown(true);
	client(name);
    }

    public final synchronized void remoteProcess(final ImageData id) {
	if (cs == null) {
	    throw new Error("Client not established!");
	}
	(new Thread() {
	    public void run() {
		cs.process(id);
	    }
	}).start();
    }

    public synchronized void process(ImageData id) {
	System.out.println("Default process method called!");
    }

    public static void main(String args[]) {
	if (args[0].equals("server")) {
	    (new ClientServer(args)).server(args[1]);
	} else {
	    ClientServer client = new ClientServer(args);
	    client.client(args[1]);
	    ImageData id = new ImageData(new int[0], new int[0], new int[0], 0, 0);
	    client.remoteProcess(id);
	    client.remoteProcess(id);
	    client.remoteProcess(id);
	    client.remoteProcess(id);
	    client.remoteProcess(id);
	}
    }

}
