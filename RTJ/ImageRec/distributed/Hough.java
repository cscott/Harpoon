package imagerec;

public class Hough extends ClientServer {
    public Hough(String args[]) {
	super(args);
    }

    public synchronized void process(ImageData id) {
	

	remoteProcess(id);
    }    

    public static void main(String args[]) {
	if (args.length < 2) {
	    System.out.print("Usage: jaco imagerec.Hough <input> ");
	    System.out.print("<output> -ORBInitRef ");
	    System.out.println("NameService=file://dir/ns");
	    System.exit(-1);
	}
	Hough hough = new Hough(args);
	hough.client(args[1]);
	hough.server(args[0]);
    }
}
