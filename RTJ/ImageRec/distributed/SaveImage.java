package imagerec;

public class SaveImage extends ClientServer {
    private int count = 0;

    public SaveImage(String args[]) {
	super(args);
    }

    public synchronized void process(ImageData id) {
	System.out.println("Saving image #"+count);
	ImageDataManip.writePPM(id, args[1]+"."+(count++));
    }

    public static void main(String args[]) {
	if (args.length < 2) {
	    System.out.print("Usage: jaco imagerec.SaveImage <name> ");
	    System.out.print("<file prefix> -ORBInitRef ");
	    System.out.println("NameService=file://dir/ns");
	    System.exit(-1);
	}
	(new SaveImage(args)).server(args[0]);
    }


}
