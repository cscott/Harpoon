/* My stripped down video-capture interfaces.  --csa */
import java.io.OutputStream;

public abstract class AltVideo {
    /* returns 'true' on success, 'false' on failure */
    public static native boolean capture(int width, int height, int[] buffer);

    /* utility */
    public static void writeppm(OutputStream out,
				int width, int height, int[] buffer)
				throws java.io.IOException {
	String header="P6 "+width+" "+height+" 255 ";
	for (int i=0; i<header.length(); i++) {
	    /* hacked utf-8 encoding! */
	    out.write(header.charAt(i));
	}
	/* now write the image data */
	for (int y=0; y<height; y++)
	    for (int x=0; x<width; x++) {
		int index = (y*width)+x;
		int pixel = (index<buffer.length)?buffer[index]:0;
		out.write((pixel>>16)&0xFF);
		out.write((pixel>> 8)&0xFF);
		out.write((pixel>> 0)&0xFF);
	    }
	out.flush();
    }
}
