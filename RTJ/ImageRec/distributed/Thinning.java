package imagerec;

public class Thinning extends ClientServer {
    public Thinning(String args[]) {
	super(args);
    }

    public synchronized void process(ImageData id) {



	remoteProcess(id);
    }

    public static void main(String args[]) {
	if (args.length < 2) {
	    System.out.print("Usage: jaco imagerec.Thinning <input> ");
	    System.out.print("<output> -ORBInitRef ");
	    System.out.println("NameService=file://dir/ns");
	    System.exit(-1);
	}
	Thinning thin = new Thinning(args);
	thin.client(args[1]);
	thin.server(args[0]);
    }
}
