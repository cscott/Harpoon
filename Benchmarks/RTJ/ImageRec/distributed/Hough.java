package imagerec;

public class Hough extends Transform {
    public Hough(String args[]) {
	super(args);
    }

    public ImageData transform(ImageData input) {
	return input;
    }

    public static void main(String args[]) {
	processArgs(args, "Hough");
	(new Hough(args)).setup();
    }
}
