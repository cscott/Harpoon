package imagerec;

public class Save extends Node {
    private String filePrefix;

    public Save(String filePrefix) {
	super();
	this.filePrefix = filePrefix;
    }

    public synchronized void process(ImageData id) {
	System.out.println("Saving image "+filePrefix+"."+id.id);
	ImageDataManip.writePPM(id, filePrefix+"."+id.id);
    }
}
