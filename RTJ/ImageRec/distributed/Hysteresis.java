package imagerec;

public class Hysteresis extends ClientServer {
    public Hysteresis(String args[]) {
	super(args);
    }

    public synchronized void process(ImageData id) {


       
	remoteProcess(id);
    }

    public static void main(String args[]) {
	if (args.length < 2) {
	    System.out.print("Usage: jaco imagerec.Hysteresis <input> ");
	    System.out.print("<output> -ORBInitRef ");
	    System.out.println("NameService=file://dir/ns");
	    System.exit(-1);
	}
	Hysteresis hysteresis = new Hysteresis(args);
	hysteresis.server(args[0]);
	hysteresis.client(args[1]);
    }

}
