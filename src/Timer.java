package imagerec;

public class Timer extends Node {
    private boolean start;
    private boolean announce;
    
    public Timer(boolean start, boolean announce, Node out) {
	super(out);
	this.start = start;
	this.announce = announce;
    }

    public synchronized void process(ImageData id) {
	long time = System.currentTimeMillis();
	if (start) {
	    id.time = time;
	} else {
	    id.time = time-id.time;
	    if (announce) {
		System.out.println(id.time);
	    }
	}
	super.process(id);
    }


}
