/** Canny.java, created by Reuben Sterling (benster@mit.edu)
   Licensed under the terms of the GNU GPL;
   see COPYING for details.
*/
package imagerec.graph;

/**
   The {@link Canny} class implements the Canny edge operator.
   First, a 2-D first-derivative operator (similar to Robert's
   Cross) is applied in order to emphasize edges.  The edges
   are then tracked in order to thin and threshold them.

   <br><br>An image passed to a {@link Canny} node should be
   run through a {@link GaussianSmoothing} node first.

   <br><br>The image passed to this node through the process() method
   will be mutated.

   <br><br>For more information on the Canny edge detection operator, see
   <<a href="http://www.dai.ed.ac.uk/HIPR2/canny.htm">http://www.dai.ed.ac.uk/HIPR2/canny.htm</a>>

   <br><br><b>This operator is not fully implemented! Currently it only performs Gaussian Smoothing and Robert's Cross on the image</b>

   @author Reuben Sterling <<a href="mailto:benster@mit.edu">benster@mit.edu</a>>
*/

public class Canny extends Node {
    
    /**
      The kernel used to calculate the image gradient values.
      Calculations assume it to be a 2x2 matrix.
     */
    protected static final int[][] kernel =
    {{1, 1},
     {-1, -1}};

    /**
       If no threshold maximum is supplied, this
       default value is used when tracking edges.
    */
    public static final int defaultThresholdMax = 250;
    /**
      If no threshold minimum is supplied, this
       default value is used when tracking edges.
    */
    public static final int defaultThresholdMin = 10;

    /**
      The threshold maximum used when edge tracking.
    */
    protected int thresholdMax;
    /**
      The threshold minimum used when edge tracking.
    */
    protected int thresholdMin;

    /**
      Construct a new {@link Canny} node that will apply the
      Canny edge detection operator to a given image and then pass
      the image to the specified {@link Node}.

      Default threshold values will be used for edge tracing.
     */
    public Canny(Node out) {
	super(out);
	init(defaultThresholdMax, defaultThresholdMin);
    }

    /**
      Construct a new {@link Canny} node that will apply the
      Canny edge detection operator to a given image and then pass
      the image to the specified {@link Node}.

      The specified threshold values will be used for edge tracing.
    */
    public Canny(int thresholdMax, int thresholdMin, Node out) {
	super(out);
	init(thresholdMax, thresholdMin);
    }

    protected void init(int thresholdMax, int thresholdMin) {
	this.thresholdMax = thresholdMax;
	this.thresholdMin = thresholdMin;
    }

    /**
       Apply the Canny edge detection algorithm to the specified
       {@link ImageData} and pass the resulting image to the
       {@link Node} specified in the constructor.

       @param imageData The {@link ImageData} to be processed. <code>imageData</code>
       will be mutated by this method.
    */
    public void process(ImageData imageData) {
	//First perform Gaussian Smoothing on this image.
	//This step relies on the fact that GaussianSmoothing's process()
	//method mutates the image.
	(new GaussianSmoothing(null)).process(imageData);
	
	int width = imageData.width;
	int height = imageData.height;
	//byte[] outs = new byte[(width-1)*(height-1)];
	byte[] outs = new byte[width*height];
	//create a new byte array for ease of data access
	byte[][] vals = new byte[][] {imageData.rvals, imageData.gvals, imageData.bvals};
	//increment of i is also controlled by a conditional incrementor
	//at the bottom of the FOR block.
	for (int i = 0; i < (width*(height-1)-1); i++) {
	    int out = 0;
	    //Cycle through image data colors (R, G, then B)
	    //Find the maximum gradient of each color, then store
	    //the result (scaled by a constant) in the outs[] array.
	    for (int color= 0; color < 3; color++) {
		//|256)&255 operation forces java to interpret
		//the byte value as unsigned (which is how it is intended)
		int currentGradient =
		    Math.abs(((vals[color][i]|256)&255)*kernel[0][0]+
			     ((vals[color][i+width+1]|256)&255)*kernel[1][1])
		    +Math.abs(((vals[color][i+1]|256)&255)*kernel[0][1]+
			      ((vals[color][i+width]|256)&255)*kernel[1][0]);
		//takes the average of all three calculated gradients
		//out += 1/3 * currentGradient;
		
		//takes the max of all three calculated gradients
		out = Math.max(out, currentGradient);
	    }
	    //scale calculated gradient by (arbitrary?) factor and store
	    //in the out vector
	    byte finalOut = (byte)Math.min(out*2, 255);
	    outs[i] = finalOut;

	    //Operator uses pixel data to the right and below
	    //of the current pixel, so we can't process
	    //the gradient value of pixels on the right
	    //or bottom edges of the image,
	    //therefore when 'i' reaches the right edge
	    //of the image, increment it an extra time to
	    //move it back to the left side
	    if ((i+2) % width == 0) {
		i++;
		//outs[i+1] = finalOut;
	    }
	}
	//Since we did nothing to the bottom row of the image,
	//we need to put something there. Just fill it with
	//the values directly above.
	//for(int i = width*(height-1); i < width*height; i++) {
	//    outs[i] = outs[i-width];
	//}
	
	//store resulting gradient values in the green channel
	imageData.gvals = outs;
	//imageData.bvals = imageData.rvals = new byte[(width-1)*(height-1)];
	imageData.bvals = imageData.rvals = new byte[width*height];
	//imageData.width = width-1;
	//imageData.height = height-1;
	super.process(imageData);
    }
}
