package imagerec;

public class LoadImage extends ClientServer {
    public LoadImage(String args[]) {
	super(args);
    }

    public static void main(String args[]) {
	if (args.length < 3) {
	    System.out.print("Usage: jaco LoadImage <name> <file prefix> ");
	    System.out.println("<num> -ORBInitRef NameService=file://dir/ns");
	}

	LoadImage li = new LoadImage(args);
	li.client(args[0]);

	for (int i=0; i<Integer.parseInt(args[2]); i++) {
	    System.out.println("Sending image #"+i);
	    ImageData id = ImageDataManip.readPPM(args[1]+"."+i);
	    li.remoteProcess(id);
	}
    }

}
