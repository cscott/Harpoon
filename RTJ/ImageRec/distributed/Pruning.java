package imagerec;

public class Pruning extends ClientServer {
    public Pruning(String args[]) {
	super(args);
    }

    public synchronized void process(ImageData id) {
	


	remoteProcess(id);
    }

    public static void main(String args[]) {
	if (args.length < 2) {
	    System.out.print("Usage: jaco imagerec.Pruning <input> ");
	    System.out.print("<output> -ORBInitRef ");
	    System.out.println("NameService=file://dir/ns");
	    System.exit(-1);
	}
	Pruning prune = new Pruning(args);
	prune.client(args[1]);
        prune.server(args[0]);
    }
}
