package imagerec;

public class Thresholding extends ClientServer {
    public Thresholding(String args[]) {
	super(args);
    }

    public synchronized void process(ImageData id) {
	


	remoteProcess(id);
    }

    public static void main(String args[]) {
	if (args.length < 2) {
	    System.out.print("Usage: jaco imagerec.Thresholding <input> ");
	    System.out.print("<output> -ORBInitRef ");
	    System.out.println("NameService=file://dir/ns");
	    System.exit(-1);
	}
	Thresholding threshold = new Thresholding(args);
	threshold.client(args[1]);
	threshold.server(args[0]);	
    }
}
