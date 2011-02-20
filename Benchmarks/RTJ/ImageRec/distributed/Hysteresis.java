package imagerec;

public class Hysteresis extends Transform {
    public Hysteresis(String args[]) {
	super(args);
    }

    public ImageData transform(ImageData id) {
	int[] in = id.gvals;
	int w = id.width;
	int h = id.height;
	for (int i=w+1;i<(w*(h-2)-1);i++) {
	    if (in[i]==255) {
		recurse(i, in, w, h);
	    }
	}
	return id;
    }
    
    public static void recurse(int i, int[] in, int w, int h) {
	if (i<w || i>w*(h-1) || (i%w)==0 || ((i+1)%w)==0) return;
	in[i] = 255;
	int[] pos = new int[] {i-w-1,i-w,i-w+1,i-1,i+1,i+w-1,i+w,i+w+1};
	for (int j=0; j<pos.length; j++) {
	    if (in[pos[j]]==64) recurse(pos[j], in, w, h);
	}
    } 

    public static void main(String args[]) {
	processArgs(args, "Hysteresis");
	(new Hysteresis(args)).setup();
    }
}
