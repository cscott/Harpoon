package imagerec;

public class Thresholding extends Transform {
    /* Is there a good heuristic for finding these???? */
    public static int T1 = 0; /* Magic number */
    public static int T2 = 0; /* Another magic number */  

    public Thresholding(String args[]) {
	super(args);
    }

    public ImageData transform(ImageData id) {
	int[] in = id.gvals;
	for (int i = 0; i<id.width*id.height; i++) {
	    in[i] = (in[i]>=T1)?255:((in[i]>=T2)?64:0);
	}
	return id;
    }

    public static void main(String args[]) {
	processArgs(args, "Thresholding", "<definite edge> <possible edge>");
	T1 = Integer.valueOf(args[numArgs]).intValue();
	T2 = Integer.valueOf(args[numArgs+1]).intValue();
	(new Thresholding(args)).setup();
    }
}
