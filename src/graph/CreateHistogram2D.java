// CreateHistogram2D.java, created by benster
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.Histogram2D;

public class CreateHistogram2D extends Node {
    
    protected String prefix;
    protected int counter;
    protected double axis1Min, axis1Max, axis2Min, axis2Max;
    protected int axis1Buckets, axis2Buckets;

    protected double sigma;
    protected static final double defaultSigma = 1.;

    protected double[][] kernel;
    //protected int[][] kernel;

    public static final double axis1MinDefault = 0, axis1MaxDefault = 1;
    public static final double axis2MinDefault = 0, axis2MaxDefault = 1;
    public static final int axis1BucketsDefault = 100, axis2BucketsDefault = 100;

    public CreateHistogram2D(String prefix, Node out) {
	super(out);
	init(prefix, axis1MinDefault, axis1MaxDefault, axis1BucketsDefault,
	     axis2MinDefault, axis2MaxDefault,
	     axis2BucketsDefault);
    }

    public CreateHistogram2D(String prefix, double axis1Min, double axis1Max,
			     int axis1Buckets, double axis2Min, double axis2Max,
			     int axis2Buckets, Node out) {
	super(out);
	init(prefix, axis1Min, axis1Max, axis1Buckets, axis2Min, axis2Max,
	     axis2Buckets);
    }

    public void init(String prefix, double axis1Min, double axis1Max,
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

    public double G(double x, double y) {
	return Math.pow(Math.E, -1*(Math.pow(x,2)+Math.pow(y,2))/
			(2*Math.pow(sigma,2)));
    }

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
