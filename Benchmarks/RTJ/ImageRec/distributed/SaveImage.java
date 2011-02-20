package imagerec;

public class SaveImage extends ClientServer {
    private int count = 0;

    private String filePrefix;

    public SaveImage(String args[]) {
	super(args);
    }

    public synchronized void process(ImageData id) {
	System.out.println("Saving image #"+count);
	ImageDataManip.writePPM(id, filePrefix+"."+(count++));
    }

    public static void main(String args[]) {
	if (args.length < 2) {
	    System.out.print("Usage: jaco imagerec.SaveImage <name> ");
	    System.out.print("<file prefix> -ORBInitRef ");
	    System.out.println("NameService=file://dir/ns");
	    System.exit(-1);
	}
	SaveImage si = new SaveImage(args);
	si.filePrefix = args[1];
	si.server(args[0]);
    }


}
