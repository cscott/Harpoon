package imagerec;

public class Thresholding extends Node {
    /* Is there a good heuristic for finding these???? */
    public int T1; /* Magic number */
    public int T2; /* Another magic number */  

    public Thresholding(int T1, int T2, Node out) {
	super(out);
	this.T1 = T1;
	this.T2 = T2;
    }

    public synchronized void process(ImageData id) {
	int[] in = id.gvals;
	for (int i = 0; i<id.width*id.height; i++) {
	    in[i] = (in[i]>=T1)?255:((in[i]>=T2)?64:0);
	}
	super.process(id);
    }
}
