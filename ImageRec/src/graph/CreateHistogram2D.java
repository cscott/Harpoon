// CreateHistogram2D.java, created by benster
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.Histogram2D;

/**
 * {@link CreateHistogram2D} takes an image and calculates a
 * {@link Histogram2D} from two local characteristics based
 * on Gaussian derivatives. (See B. Schiele and J. L. Crowley, 
 * "Probabilistic Object Recognition using MultiDimensional 
 * Receptive Field Histograms)<br><br>
 *
 * Currently, nothing is done with the {@link Histogram2D}, but
 * it is intended to be saved as a text file for future
 * use when comparing sample images.<br><br>
 *
 * This {@link Node} is not intended to be used as part of
 * a normal image processing pipeline. It would be used
 * prior to real-time processing to create a database of
 * {link @Histogram2D}s for future comparisons.<br><br>
 *
 * <b>This class has not been fully implemented or tested and 
 * is not currently intended for use.</b>
 *
 * @author Reuben Sterling <<a href="mailto:benster@mit.edu">benster@mit.edu</a>>
 */
public class CreateHistogram2D extends Node {
    
    /**
     * The common prefix for filenames for the stored
     * {@link Histogram2D}s created by this object.
     */
    protected String prefix;
    /**
     * The incrementing counter for filenames for the stored
     * {@link Histogram2D}s created by this object.
     */
    protected int counter; /** Shared mutated state! */
    /**
     * The one of the axis characteristics for the {@link Histogram2D}s
     * that will be created by this object.
     * @see Histogram2D
     */
    protected double axis1Min, axis1Max, axis2Min, axis2Max;
    /**
     * The one of the axis characteristics for the {@link Histogram2D}s
     * that will be created by this object.
     * @see Histogram2D
     */
    protected int axis1Buckets, axis2Buckets;

    /**
     * The sigma (scaling factor) used when calculating
     * the gaussian derivatives of image regions.
     */
    protected double sigma;
    /**
     * The default sigma (scaling factor) used when calculating
     * the gaussian deriviatives of image regions.
     */
    protected static final double defaultSigma = 1.;

    /**
     * The kernel that is overlayed on regions of the image
     * to calculate the gaussian derivative of that region.
     * It is assumed to be square.
     */
    protected double[][] kernel;
    //protected int[][] kernel;

    /**
     * Default value for one of the axis characteristics of the
     * {@link Histogram2D}s created by this object.
     * @see Histogram2D
     */
    public static final double axis1MinDefault = 0, axis1MaxDefault = 1;
    /**
     * Default value for one of the axis characteristics of the
     * {@link Histogram2D}s created by this object.
     * @see Histogram2D
     */
    public static final double axis2MinDefault = 0, axis2MaxDefault = 1;
    /**
     * Default value for one of the axis characteristics of the
     * {@link Histogram2D}s created by this object.
     * @see Histogram2D
     */
    public static final int axis1BucketsDefault = 100, axis2BucketsDefault = 100;

    /**
     * Constructs a new {@link CreateHistogram2D} by specifing a filename
     * prefix and a destination node for processed images.
     *
     * @param prefix The filename prefix for files that will be created by
     * this object.
     *
     * @param out The {@link Node} to which processed images will be sent.
     */
    public CreateHistogram2D(String prefix, Node out) {
	super(out);
	init(prefix, axis1MinDefault, axis1MaxDefault, axis1BucketsDefault,
	     axis2MinDefault, axis2MaxDefault,
	     axis2BucketsDefault);
    }
    
    /**
     * Constructs a new {@link CreateHistogram2D} by specifing a filename
     * prefix, the destination node for processed images, and several
     * axis characteristics for the {@link Histogram2D}s that will be
     * created for this node.
     *
     * @param prefix The filename prefix for files that will be created by
     *               this object.
     *
     * @param axis1Min One of the axis characteristics for the histograms 
     *                 that will be created by this object.
     *
     * @param axis1Max One of the axis characteristics for the histograms 
     *                 that will be created by this object.
     *
     * @param axis1Buckets One of the axis characteristics for the histograms 
     *                     that will be created by this object.
     *
     * @param axis2Min One of the axis characteristics for the histograms 
     *                 that will be created by this object.
     *
     * @param axis2Max One of the axis characteristics for the histograms 
     *                 that will be created by this object.
     *
     * @param axis2Buckets One of the axis characteristics for the histograms 
     *                     that will be created by this object.
     *
     * @param out The {@link Node} to which processed images will be sent.
     *
     * @see Histogram2D
     */
    public CreateHistogram2D(String prefix, double axis1Min, double axis1Max,
			     int axis1Buckets, double axis2Min, double axis2Max,
			     int axis2Buckets, Node out) {
	super(out);
	init(prefix, axis1Min, axis1Max, axis1Buckets, axis2Min, axis2Max,
	     axis2Buckets);
    }

    /**
     * Method called by all constructors to initialize object fields.
     */
    protected void init(String prefix, double axis1Min, double axis1Max,
			int axis1Buckets, double axis2Min, double axis2Max,
			int axis2Buckets) {
	this.prefix = prefix;
	this.axis1Min = axis1Min;
	this.axis1Max = axis1Max;
	this.axis1Buckets = axis1Buckets;
 	this.axis2Min = axis2Min;
	this.axis2Max = axis2Max;
	this.axis2Buckets = axis2Buckets;
	
	this.setSigma(defaultSigma);
    }
    

    /**
     * Sets the scaling factor for calculating gaussian derivatives and
     * calculates a new kernel based on the new sigma value.
     *
     * @param newSigma The new sigma value that will be used 
     *                 to calculate gaussian derivatives.
     */
    public void setSigma(double newSigma) {
	this.sigma = newSigma;
	//recalculate kernel
	int count = 0, count2 = 0;
	while(G(count, count) >= 1./273) {
	    count++;
	}
	int length = 2*(count-1)+1; //always odd
	System.out.println("length: "+length);
	int offset = count-1;
	kernel = new double[length][length];
	//kernel = new int[length][length];
	for (count = 0; count < length/2+1; count++) {
	    for (count2 = 0; count2 < length/2+1; count2++) {
		//int g = (int)(G(count, count2)*273);
		double g = G(count, count2);
		System.out.println("g: "+g);
		kernel[offset+count][offset+count2] = g;
		kernel[offset-count][offset-count2] = g;
		kernel[offset+count][offset-count2] = g;
		kernel[offset-count][offset+count2] = g;
	    }
	}
    }

    /**
     * Calculates the gaussian derivative from two numbers.
     */
    public double G(double x, double y) {
	return Math.pow(Math.E, -1*(Math.pow(x,2)+Math.pow(y,2))/
			(2*Math.pow(sigma,2)));
    }
    
    /**
     * Overlays the kernal on the specified {@link ImageData}, centered at the
     * specified location, and calculates the convolution.
     *
     * @param l The pixel location in the specified {@link ImageData} around 
     *          which to center the convolution kernel.
     *
     * @param id The {@link ImageData} on which to convolve.
     */
    public double convolve(int l, ImageData id) {
	int kLen = kernel.length;
	int halfKLen = kLen/2;
	int width = id.width;
	double sum = 0;
	//int sum = 0;
	for (int count2 = 0; count2 < kLen; count2++) {
	    for (int count = 0; count < kLen; count++) {
		sum += kernel[count][count2]*
		    id.rvals[l+count-halfKLen+(count2-halfKLen)*width];
	    }
	}
	return sum;
    }

    /**
     * Creates a new {@link Histogram2D} from the specified {@link ImageData} based on
     * the charactistics mentioned in the class description.
     *
     * @param imageData The {@link ImageData} from which to calculate the {@link Histogram2D}.
     */
    public void process(ImageData imageData) {
	Histogram2D h = new Histogram2D(axis1Min, axis1Max, axis1Buckets,
					axis2Min, axis2Max, axis2Buckets);
	double alpha = Math.PI/4;
	double alpha2 = alpha+Math.PI/2;
	int length = imageData.rvals.length;
	int width = imageData.width;
	//kernel width and height are assumed to be the same
	int kLen = kernel.length;
	int halfKLen = kLen/2;
	double g = 0, gx, gy, mag, dir, Dx, Dy;
	double sigSq = Math.pow(sigma,2);
	int x, y;
	long startTime;
	long endTime;
	long totalTime = 0;
	double avgTime;
	int numSamples = 0;
	for (int count = halfKLen+width*halfKLen;
	     count < length-halfKLen-width*halfKLen;
	     count++) {
	    //calculate Magnitude of gaussian derivative
	    startTime = System.currentTimeMillis();
	    g = convolve(count, imageData);
	    x = count%width;
	    y = count/width;
	    
	    gx = -x/sigSq*g;
	    gy = -y/sigSq*g;
	    Dx = Math.cos(alpha)*gx + Math.sin(alpha)*gy;
	    Dy = Math.cos(alpha2)*gx + Math.sin(alpha2)*gy;
	    mag = Math.sqrt(Math.pow(Dx,2)+Math.pow(Dy,2));
	    dir = Math.atan(Dy/Dx);
	    endTime = System.currentTimeMillis();
	    numSamples++;
	    totalTime += endTime-startTime;
	    if (endTime-startTime > 0)
		System.out.println("Hurray");
	    if (count % 500 == 0) {
		avgTime = totalTime*1./numSamples;
		System.out.println("totalTime: "+totalTime);
		System.out.println("avgTime: "+avgTime);
	    }
	    //System.out.println("mag: "+mag);
	    //System.out.println("dir: "+dir);
	    //System.out.println("");
	}
	String filename = prefix+counter+".ppm";
	super.process(imageData);
    }
}
