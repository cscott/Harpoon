package imagerec;

public class Pruning extends Transform {
    public static final int iterations=10;

    public Pruning(String args[]) {
	super(args);
    }

    public ImageData transform(ImageData id) {
	int[] in = id.gvals;
	int w = id.width;
	int h = id.height;
	int[] out = null;
	int changed=1;
	for (int j=0;(j<iterations)&&(changed==1);j++) {
	    changed=0;
	    System.arraycopy(in,0,out=new int[w*h],0,w*h);
	    for (int i=w+1;i<w*(h-2)-1;i++) {
		if (in[i]==255) {
		    /* 0 1 2
                       3 * 4
                       5 6 7 */
		    int[] n = new int[] {in[i-w-1],in[i-w],in[i-w+1],
					 in[i-1],          in[i+1],
					 in[i+w-1],in[i+w],in[i+w+1]};
		    if (((n[0]|n[1]|n[2]|n[3]|n[4])<65 && (n[5]&n[7])<65)||
			((n[1]|n[2]|n[4]|n[6]|n[7])<65 && (n[0]&n[5])<65)||
			((n[3]|n[4]|n[5]|n[6]|n[7])<65 && (n[0]&n[2])<65)||
			((n[0]|n[1]|n[3]|n[5]|n[6])<65 && (n[2]&n[7])<65)) {
			changed = 1;
			out[i]=0;
		    }
		}
	    }
	    in=out;
	}
	id.gvals=in;
	return id;
    }

    public static void main(String args[]) {
	processArgs(args, "Pruning");
	(new Pruning(args)).setup();
    }
}
