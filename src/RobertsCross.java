package imagerec;

public class RobertsCross extends Node {
    public RobertsCross(Node out) {
	super(out);
    }

    public synchronized void process(ImageData id) {
	int width = id.width;
	int height = id.height;
	int[] outs = new int[width*height];
	int[][] vals = new int[][] {id.rvals, id.gvals, id.bvals}; 
	for (int i=0; i<(width*(height-1)-1); i++) {
	    int out = 0;
	    for (int j = 0; j<3; j++) {
		int[] val = vals[j];
		out = Math.max(out, 
			       Math.abs(val[i]-val[i+width+1])+
			       Math.abs(val[i+1]-val[i+width]));
	    }
	    outs[i] = Math.min(out*2, 255);
	}
	id.gvals = outs;		     
	id.bvals = id.rvals = new int[width*height];
	super.process(id);
    }
}
