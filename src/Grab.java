package imagerec;

public class Grab extends Node {
    private ImageData id = null;

    public Grab() {
	super();
    }

    public synchronized void process(ImageData id) {
	this.id = id;
    }

    public ImageData grab() {
	return id;
    }
}
