package imagerec;

public class Async extends Node {
    private Node out;

    public Async(Node out) {
	super(out);
    }

    public synchronized void process(final ImageData id) {
	(new Thread() {
	    public void run() {
		Async.super.process(id);
	    }
	}).start();
    }
}
