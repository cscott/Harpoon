package imagerec;

public class Transform extends ClientServer {
    public static final int numArgs = 2;

    public Transform(String args[]) {
	super(args);
    }

    public ImageData transform(ImageData input) {
	System.out.println("Default transform called!");
	return input;
    }

    public final synchronized void process(ImageData id) {
	remoteProcess(transform(id));
    }

    public final static void processArgs(String args[], String name) {
	processArgs(args, name, "");
    }

    public final static void processArgs(String args[], String name, 
					 String addArgs) {
	if (!addArgs.equals("")) {
	    addArgs+=" ";
	}
	if (args.length < 2) {
	    System.out.print("Usage: jaco imagerec."+name+" <input> ");
	    System.out.print("<output> "+addArgs+"-ORBInitRef ");
	    System.out.println("NameService=file://dir/ns");
	    System.exit(-1);
	}
    }

    public final void setup() {
	server(args[0]);
	client(args[1]);
    }
}
