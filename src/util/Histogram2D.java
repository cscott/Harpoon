// Histogram.java, created by benster
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.util;

import java.io.File;
import java.io.StreamTokenizer;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileWriter;

/**
  This class implements a two-dimensional histogram data structure.
  <br><br>Upon creation, you must specify the characteristics of both axes:
    <li>The minimum value of the axis</li>
    <li>The maximum value of the axis</li>
    <li>The number of buckets for that axis</li>
  <br><br>
  If there are <b><code>m</code></b> buckets for one axis and <b><code>n</code></b> buckets for the second axis, there are
  <code>m x n</code> total buckets for the histogram. Each bucket stores samples for a range of values along each axis.
  For example, if you specify a single axis to range from 5 to 10 and contain 10 buckets, then samples in the range
  [0, .5) will be stored in bucket 0, samples in the range [.5, 1) will be stored in bucket 1, etc. The maximum value
  (in this case, 10) does not actually fall into any bucket.
  <br><br>
  You may add a sample to the histogram either by specifying the actual bucket for each axis
  where the sample should be stored or by specifying where the sample lies along each axis.
  If you provide values rather than buckets, the proper buckets for each axis will the calculated for you.
  <hr>
  Histogram2Ds may be saved to and loaded from text files. The format is described below. The presented
  format is only for readability. Aside from separating values, whitespace has no meaning.<br><br>
  <b><code>BEGINNING OF FILE</code></b><br><br>
  # comment: sample histogram file
  [axis 1 minimum value (double)]<br>
  [axis 1 maximum value (double)]<br>
  [axis 1 number of buckets (int)]<br>
  <br>
  [axis 2 minimum value (double)]<br>
  [axis 2 maximum value (double)]<br>
  [axis 2 number of buckets (int)]<br>
  <br>
  [# of samples in bucket (0,0) (double)]<br>
  [# of samples in bucket (0,1) (double)]<br>
  ...<br>
  [# of samples in bucket (0,n) (double)]<br>
  [# of samples in bucket (1,0) (double)]<br>
  [# of samples in bucket (1,1) (double)]<br>
  ...<br>
  [# of samples in bucket (m,n) (int)]<br>
  <br><br><b><code>END OF FILE</code></b>
*/
public class Histogram2D {

    protected int[][] samples;

    protected double axis1MinValue;
    protected double axis1MaxValue;
    protected double axis1NumBuckets;
    protected double axis1BucketSize;
    protected double axis2MinValue;
    protected double axis2MaxValue;
    protected double axis2NumBuckets;
    protected double axis2BucketSize;
    
    /**
       Create a new Histogram2D with the specified axes and number of buckets.
    */
    public Histogram2D(double axis1MinValue, double axis1MaxValue, int axis1NumBuckets,
		       double axis2MinValue, double axis2MaxValue, int axis2NumBuckets) {
	this.samples = new int[axis1NumBuckets][axis2NumBuckets];
	this.axis1MinValue = axis1MinValue;
	this.axis1MaxValue = axis1MaxValue;
	this.axis1NumBuckets = axis1NumBuckets;
	this.axis1BucketSize = (axis1MaxValue - axis1MinValue)/axis1NumBuckets;
	this.axis2MinValue = axis2MinValue;
	this.axis2MaxValue = axis2MaxValue;
	this.axis2NumBuckets = axis2NumBuckets;
	this.axis2BucketSize = (axis2MaxValue - axis2MinValue)/axis2NumBuckets;
    }
    
    /**
       Add a sample to the histogram by specifying values along each axis.
       The appropriate bucket for each axis will be calculated for you.
       @throws InvalidHistogramValueException Thrown if the specified values lie
       outside the appropriate range of values for their respective axes.
    */
    public void addSample(double axis1Value, double axis2Value)
	throws InvalidHistogramValueException {
	int axis1Bucket;
	int axis2Bucket;
	if (axis1Value < axis1MinValue)
	    throw new InvalidHistogramValueException("Argument 1 ("+axis1Value+") < "+
						 "Min value for axis 1 ("+axis1MinValue+")");
	if (axis1Value >= axis1MaxValue)
	    throw new InvalidHistogramValueException("Argument 1 ("+axis1Value+") >= "+
						 "Max value for axis 1 ("+axis1MaxValue+")");
	if (axis2Value < axis2MinValue)
	    throw new InvalidHistogramValueException("Argument 2 ("+axis2Value+") < "+
						 "Min value for axis 2 ("+axis2MinValue+")");
	if (axis2Value >= axis2MaxValue)
	    throw new InvalidHistogramValueException("Argument 2 ("+axis2Value+") >= "+
						 "Max value for axis 2 ("+axis2MaxValue+")");
	    
	axis1Bucket = (int)((axis1Value-axis1MinValue)/axis1BucketSize);
	axis2Bucket = (int)((axis2Value-axis2MinValue)/axis2BucketSize);
	samples[axis1Bucket][axis2Bucket]++;
    }

    /**
       Add a sample to the histogram by specifying the appropriate bucket along each axis.
       @throws InvalidHistogramValueException Thrown if the specified bucket numbers do
       not corrospond to real buckets along their respective axes.
    */
    public void addSample(int axis1Bucket, int axis2Bucket) 
	throws InvalidHistogramValueException {
	if (axis1Bucket < 0)
	    throw new InvalidHistogramValueException("Argument 1 ("+axis1Bucket+") < "+
						 "Min bucket value for axis 1 (0)");
	if (axis1Bucket >= axis1NumBuckets)
	    throw new InvalidHistogramValueException("Argument 1 ("+axis1Bucket+") > "+
						 "Max bucket value for axis 1 ("+(axis1NumBuckets-1)+")");
	if (axis2Bucket < 0)
	    throw new InvalidHistogramValueException("Argument 2 ("+axis2Bucket+") < "+
						 "Min bucket value for axis 2 (0)");
	if (axis2Bucket >= axis2NumBuckets)
	    throw new InvalidHistogramValueException("Argument 2 ("+axis2Bucket+") > "+
						 "Max bucket value for axis 2 ("+(axis2NumBuckets-1)+")");
	samples[axis1Bucket][axis2Bucket]++;
    }

    protected void setSample(int axis1Bucket, int axis2Bucket, int numSamples) 
	throws InvalidHistogramValueException {
	if (axis1Bucket < 0)
	    throw new InvalidHistogramValueException("Argument 1 ("+axis1Bucket+") < "+
						 "Min bucket value for axis 1 (0)");
	if (axis1Bucket >= axis1NumBuckets)
	    throw new InvalidHistogramValueException("Argument 1 ("+axis1Bucket+") > "+
						 "Max bucket value for axis 1 ("+(axis1NumBuckets-1)+")");
	if (axis2Bucket < 0)
	    throw new InvalidHistogramValueException("Argument 2 ("+axis2Bucket+") < "+
						 "Min bucket value for axis 2 (0)");
	if (axis2Bucket >= axis2NumBuckets)
	    throw new InvalidHistogramValueException("Argument 2 ("+axis2Bucket+") > "+
						     "Max bucket value for axis 2 ("+(axis2NumBuckets-1)+")");
	samples[axis1Bucket][axis2Bucket] = numSamples;
    }
    
    /**
       Get the number of samples added to the specified bucket.
       @throws InvalidHistogramValueException Thrown if the specified bucket nubmers do
       not corrospond to real buckets along their respective axes.
    */
    public int getSample(int axis1Bucket, int axis2Bucket) 
	throws InvalidHistogramValueException {
 	if (axis1Bucket < 0)
	    throw new InvalidHistogramValueException("Argument 1 ("+axis1Bucket+") "+
						 "< Min bucket value for axis 1 (0)");
	if (axis1Bucket >= axis1NumBuckets)
	    throw new InvalidHistogramValueException("Argument 1 ("+axis1Bucket+") > "+
						 "Max bucket value for axis 1 ("+(axis1NumBuckets-1)+")");
	if (axis2Bucket < 0)
	    throw new InvalidHistogramValueException("Argument 2 ("+axis2Bucket+") < "+
						 "Min bucket value for axis 2 (0)");
	if (axis2Bucket >= axis2NumBuckets)
	    throw new InvalidHistogramValueException("Argument 2 ("+axis2Bucket+") > "+
						 "Max bucket value for axis 2 ("+(axis2NumBuckets-1)+")");
	return samples[axis1Bucket][axis2Bucket];
    }

    /**
       Returns <b><code>true</code></b> if the specified {@link Histogram2D} is the same type as this one,
       <b><code>false</code></b> otherwise.
       Two Histogram2Ds of the same type have the same mininum and maximum values and the
       same number of buckets along each axis. Histogram2Ds <u>do not</u> need to contain
       the same data to be of the same type.
    */
    public boolean isSameType(Histogram2D h) {
	if (axis1MinValue != h.axis1MinValue)
	    return false;
	if (axis1MaxValue != h.axis1MaxValue)
	    return false;
	if (axis1NumBuckets != h.axis1NumBuckets)
	    return false;
	if (axis2MinValue != h.axis2MinValue)
	    return false;
	if (axis2MaxValue != h.axis2MaxValue)
	    return false;
	if (axis2NumBuckets != h.axis2NumBuckets)
	    return false;
	return true;
    }

    /**
       Returns <b><code>true</code></b> if the specified Histogram2Ds are of the same type, <b><code>false</code></b>
       otherwise.
       Two Histogram2Ds of the same type have the same mininum and maximum values and the
       same number of buckets along each axis. Histogram2Ds <u>do not</u> need to contain
       the same data to be of the same type.       
    */
    public static boolean areSameType(Histogram2D h1, Histogram2D h2) {
	return h1.isSameType(h2);
    }

    /**
       Returns the distance of the specified {@link Histogram2D} with this one.
       The distance between two histograms is calculated by summing
       the squares of the differences between corrosponding buckets, then taking
       the square root of the sum.
    */
    public double distanceFrom(Histogram2D h) 
	throws InvalidHistogramValueException {
	if (!isSameType(h))
	    throw new InvalidHistogramValueException("Provided Histogram2D is not of the same type.");
	double distance = 0;
	for (int axis1 = 0; axis1 < axis1NumBuckets; axis1++) {
	    for (int axis2 = 0; axis2 < axis2NumBuckets; axis2++) {
		distance += Math.pow(samples[axis1][axis2] - h.getSample(axis1,axis2),2);
	    }
	}
	distance = Math.sqrt(distance);
	return distance;
    }

    /**
       
    */
    public static Histogram2D loadFromFile(String filename) 
	throws FileNotFoundException, IOException {
	return loadFromFile(new File(filename));
    }


    public static Histogram2D loadFromFile(File file)
	throws FileNotFoundException, IOException {
	StreamTokenizer st;
	FileReader fr = new FileReader(file);
	st = new StreamTokenizer(fr);
	st.commentChar('#');
	st.parseNumbers();
	int result;
	
	//doesn't handle errors right now

	double axis1Min, axis1Max, axis2Min, axis2Max;
	int axis1Buckets, axis2Buckets;

	result = st.nextToken();
	axis1Min = st.nval;	
	result = st.nextToken();
	axis1Max = st.nval;
	result = st.nextToken();
	axis1Buckets = (int)st.nval;
	result = st.nextToken();
	axis2Min = st.nval;
	result = st.nextToken();
	axis2Max = st.nval;
	result = st.nextToken();
	axis2Buckets = (int)st.nval;

	Histogram2D h = new Histogram2D(axis1Min, axis1Max, axis1Buckets,
					axis2Min, axis2Max, axis2Buckets);
	int num;
	for (int axis1 = 0; axis1 < axis1Buckets; axis1++) {
	    for(int axis2 = 0; axis2 < axis2Buckets; axis2++) {
		result = st.nextToken();
		num = (int)st.nval;
		h.setSample(axis1, axis2, num);
	    }
	}
	fr.close();
	return h;
    }

    public static void saveToFile(String filename, Histogram2D h) 
	throws IOException {
	saveToFile(new File(filename), h);
    }

    public static void saveToFile(File file, Histogram2D h) 
	throws IOException {
	FileWriter fw = new FileWriter(file);
	String s = "#Imagerec.util.Histogram2D data\n"+h.axis1MinValue+"\n"+h.axis1MaxValue+"\n"+h.axis1NumBuckets+"\n\n"+
	    h.axis2MinValue+"\n"+h.axis2MaxValue+"\n"+h.axis2NumBuckets+"\n\n";
	for (int axis1 = 0; axis1 < h.axis1NumBuckets; axis1++) {
	    for (int axis2 = 0; axis2 < h.axis2NumBuckets; axis2++) {
		s+= ""+h.getSample(axis1, axis2)+"\n";
	    }
	    s+= "\n";
	}
	fw.write(s, 0, s.length());
	fw.close();
    }
}
