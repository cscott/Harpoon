package imagerec;

public class RobertsCross extends ClientServer {
    public RobertsCross(String args[]) {
	super(args);
    }

    public synchronized void process(ImageData id) {
	


	remoteProcess(id);
    }

    public static void main(String args[]) {
	if (args.length < 2) {
	    System.out.print("Usage: jaco imagerec.RobertsCross <input> ");
	    System.out.print("<output> -ORBInitRef ");
	    System.out.println("NameService=file://dir/ns");
	    System.exit(-1);
	}
	RobertsCross rc = new RobertsCross(args);
	rc.client(args[1]);
	rc.server(args[0]);
    }
}
