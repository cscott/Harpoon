package imagerec;

public class Label extends Node {
    public Label(Node out) {
	super(out);
    }

    public synchronized void process(ImageData id) {
	super.process(id);
    }
}
