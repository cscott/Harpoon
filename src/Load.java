package imagerec;

public class Load extends Node {
    String filePrefix;
    int num;

    public Load(String filePrefix, int num, Node out) {
	super(out);
	this.filePrefix = filePrefix;
	this.num = num;
    }

    public synchronized void process(ImageData id) {
	for (int i=0; i<num; i++) {
	    System.out.println("Loading image "+filePrefix+"."+i);
	    (id = ImageDataManip.readPPM(filePrefix+"."+i)).id = i;
	    super.process(id);
	}
    }
}
