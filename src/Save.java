package imagerec;

public class Save extends Node {
    private int count = 0;
    
    private String filePrefix;

    public Save(String filePrefix) {
	super();
	this.filePrefix = filePrefix;
    }

    public synchronized void process(ImageData id) {
	System.out.println("Saving image "+filePrefix+"."+count);
	ImageDataManip.writePPM(id, filePrefix+"."+(count++));
    }
}
