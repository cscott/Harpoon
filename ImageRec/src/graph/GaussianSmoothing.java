/** Canny.java, created by Reuben Sterling (benster@mit.edu)
   Licensed under the terms of the GNU GPL;
   see COPYING for details.
*/
package imagerec.graph;

/**
   The {@link GaussianSmoothing} class implements the Gaussian Smoothing
   operator. Each pixel value is replaced with a average of its own value
   and surrounding pixel values, with each piece of the average weighted
   by a 2-D gaussian kernel centered at the target pixel.
   
   <br><br>This class operates on all three color channels, by default, but
   may be set to operate on specified channels.

   <br><br>Images which pass through this {@link Node} are mutated.
*/
public class GaussianSmoothing extends Node {

    protected boolean useRed = true;
    protected boolean useGreen = true;
    protected boolean useBlue = true;

    /**
      The kernel used to calculate the gaussian approximation.
      Calculations assume that all rows are of equal length.
      <br><br>Sum of elements is 273;
     */
    protected static final int[][] kernel =
    {{1, 4, 7, 4, 1},
     {4,16,26,16, 4},
     {7,26,41,26, 7},
     {4,16,26,16, 4},
     {1, 4, 7, 4, 1}};
    /*
      The sum of the elements of the kernel
      multiplied by the scaleKernel is 1;
    */
    protected double scaleKernel = 1./273;

    /*
      The default sigma used to calculate the Gaussian Distribution.
    */
    protected static final double defaultSigma = 1.0;

    /*
      The sigma used to calculate the Gaussian Distribution.
    */
    protected double sigma;

    /**
       Construct a {@link GaussianSmoothing} node which will apply
       the Gaussian Smoothing operator to a specified image, then
       pass the image onto the specified {@link Node}.

       Only the specified color channels will be operated on.
       The other channels will be left alone.
    */
    public GaussianSmoothing(boolean useRed, boolean useGreen, boolean useBlue,
			     Node out) {
	super(out);
	init(useRed, useGreen, useBlue, defaultSigma);
    }

    /**
       Construct a {@link GaussianSmoothing} node which will apply
       the Gaussian Smoothing operator to a specified image, then
       pass the image onto the specified {@link Node}.

       By default, all three color channels will be operated on.
    */
    public GaussianSmoothing(Node out) {
	super(out);
	init(true, true, true, defaultSigma);
    }

    /**
       I don't think it's correct to alter sigma without
       also changing the kernel and scaleKernel values.
    */
    protected GaussianSmoothing(double sigma, Node out) {
	super(out);
	init(true, true, true, sigma);
    }

   /**
       Call this function from all constructors to initialize
       fields.
    */
    protected void init(boolean useRed, boolean useGreen, boolean useBlue,
    double sigma) {
	this.useRed = useRed;
	this.useGreen = useGreen;
	this.useBlue = useBlue;
	this.sigma = sigma;
    }

    /*
      Performs gaussian smoothing on the specified image, then passes it to the next {@link Node}.
    */
    public void process(ImageData imageData) {
	int channelsUsed = 0;
	if (useRed) channelsUsed++;
	if (useGreen) channelsUsed++;
	if (useBlue) channelsUsed++;
	//if no channels are to be used, then skip processing
	if (channelsUsed == 0) {super.process(imageData);return;}
	int width = imageData.width;
	int height = imageData.height;
	byte[][] outs = new byte[channelsUsed][width*height];

	//the following code allows an arbitrary number of channels
	//to be processed
	byte[][] vals = new byte[channelsUsed][];
	int count = 0;
	if (useRed) {vals[0] = imageData.rvals; count++;}
	if (useGreen) {vals[count] = imageData.gvals; count++;}
	if (useBlue) {vals[count] = imageData.bvals;}
	//i starts 2 pixels diagonally in from top left
	//increment of i is also controlled by a conditional
	//incrementor at the bottom of the FOR block
	for (int i = 2*width+2; i < (width*(height-2)-2); i++) {
	    if (i >= (width*(height-2)-2))
		System.out.println("What the fuck??");
	    //compute gaussian average for all specified colors
	    for (int color = 0; color < channelsUsed; color++) {
		int kWidth = kernel[0].length;
		int kHeight = kernel.length;
		int total = 0;
		//multiply adjacent pixels with corrosponding
		//element in the kernel (convolution)
		for (int down = 0; down < kHeight; down++) {
		    for (int across = 0; across < kWidth; across++) {
			total += kernel[down][across]*
			    ((vals[color]
			      [i+width*(down-kHeight/2)+
					  (across-kWidth/2)]
			      |256)&255);
		    }
		}
		total *= scaleKernel;
		outs[color][i] = (byte)total;
		
	    }
	    //can't calculate average of pixels on the borders
	    //so we have to do a conditional increment
	    if ((i+3)%width == 0) {
		i+=4;
	    }
	}
	//once all smoothing has been done,
	//update imageData
	count = 0;
	if (useRed) {imageData.rvals = outs[0];count++;}
	if (useGreen) {imageData.gvals = outs[count];count++;}
	if (useBlue) {imageData.bvals = outs[count];}

	//continue on
	super.process(imageData);
    }
}
