package imagerec;

public class SearchThreshold extends Transform {
    public static final double T1 = 0.9; /* Top percentile */
    public static final double T2 = 0.85; /* Bottom percentile */

    public SearchThreshold(String args[]) {
	super(args);
    }

    public ImageData transform(ImageData id) {
	int t1 = 0;
	int t2 = 0;
	int[] in = id.gvals;
	int[] scale = new int[256];
	int numPix = id.width*id.height;
	for (int i=0; i<numPix; i++) {
	    scale[in[i]]++;
	}
	int pixThresh1 = (int)(T1*((double)numPix));
	int pixThresh2 = (int)(T2*((double)numPix));
	int num = 0;
	for (int i=0; i<256; i++) {
	    num+=scale[i]; 
	    if (num<pixThresh1) t1=i;
	    if (num<pixThresh2) t2=i;
	}
	for (int i=0; i<numPix; i++) {
	    in[i] = (in[i]>=t1)?255:((in[i]>=t2)?64:0);
	}
	return id;
    }

    public static void main(String args[]) {
	processArgs(args, "SearchThreshold");
	(new SearchThreshold(args)).setup();
    }
}
