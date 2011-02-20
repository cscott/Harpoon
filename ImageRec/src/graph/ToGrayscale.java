/** ToGrayscale.java, created by Reuben Sterling (benster@mit.edu)
   Licensed under the terms of the GNU GPL;
   see COPYING for details.
*/
package imagerec.graph;

/**
   {@link ToGrayscale} takes a 24-bit RGB image and converts it into
   an "8-bit" grayscale one.  The resulting image still contains all three
   color channels, but the grayscale pixel data is only stored in one channel,
   while the other two are set to all 0's.

   <br><br>The grayscale pixel data is stored in the green channel by default,
   but this may be changed.

   <br><br>The grayscale value of a pixel may be calculated in two ways:
   <li>The maximum value of all three color channels.
   <li>The average value of all three color channels.

   <br><br>The default conversion method is by maximum value.

   <br><br>The default

   <br><br>An image that passes through this node is mutated.

   @author Reuben Sterling <<a href="mailto:benster@mit.edu">benster@mit.edu</a>>
*/
public class ToGrayscale extends Node {
    protected int conversionType;
    public static final int AVERAGE_VALUE = 0;
    public static final int MAXIMUM_VALUE = 1;
    protected static final int defaultConversionType = MAXIMUM_VALUE;

    protected int finalChannel;
    public static final int RED = 10;
    public static final int BLUE = 11;
    public static final int GREEN = 12;
    protected static final int defaultFinalChannel = GREEN;
    

    /**
       Construct a new {@link ToGrayscale} node that will
       convert a given image from full color to grayscale,
       then pass the image to the specified node.
       
       <br><br>The grayscale pixel values will be calculated based
       on the specified conversion type and will be stored in the specified
       color channel.
     */
    public ToGrayscale(int colorChannel, int conversionType, Node out) {
	super(out);
	init(colorChannel, conversionType);
    }

    /**
       Construct a new {@link ToGrayscale} node that will
       convert a given image from full color to grayscale,
       then pass the image to the specified node.
       
       <br><br>The grayscale pixel values will be calculated based
       on the default conversion type and will be stored in the default
       color channel.
     */
    public ToGrayscale(Node out) {
	super(out);
	init(defaultFinalChannel, defaultConversionType);
    }


    /**
       Construct a new {@link ToGrayscale} node that will
       convert a given image from full color to grayscale,
       then pass the image to the specified node.
       
       <br><br>The grayscale pixel values will be calculated based
       on the default conversion type and will be stored in the specified
       color channel.
     */
    public ToGrayscale(int colorChannel, Node out) {
	super(out);
	init(colorChannel, defaultConversionType);
    }
    
    protected void init(int colorChannel, int conversionType) {
	if (isValidConversionType(conversionType))
	    this.conversionType = conversionType;
	else {
	    System.out.println("Invalid conversionType");
	    this.conversionType = defaultConversionType;
	}
	if (isValidColorChannel(colorChannel))
	    this.finalChannel = colorChannel;
	else {
	    System.out.println("Invalid color channel");
	    this.finalChannel = defaultFinalChannel;
	}
    }

    /**
       Test if the given integer represents a valid conversion type.
       Returns true if the integer represents a valid conversion type,
       false otherwise.
     */
    protected boolean isValidConversionType(int type) {
	if (type == AVERAGE_VALUE)
	    return true;
	if (type == MAXIMUM_VALUE)
	    return true;
	return false;
    }

    protected boolean isValidColorChannel(int channel) {
	if (channel == RED)
	    return true;
	if(channel == GREEN)
	    return true;
	if (channel == BLUE)
	    return true;
	return false;
    }

    /**
       Returns the maximum value of three numbers.
     */
    protected int max(int val1, int val2, int val3) {
	int result;
	result = Math.max(val1, val2);
	result = Math.max(result, val3);
	return result;
    }
    
    /**
       Apply the grayscale conversion to the specified
       {@link ImageData} and pass the resulting image to
       the {@link Node} specified in the constructor.

        @param imageData The {@link ImageData} to be processed. <code>imageData</code>
       will be mutated by this method.
     */
    public void process(ImageData imageData) {
	int width = imageData.width;
	int height = imageData.height;
	//cycle through all pixels
	for (int i = 0; i < width*height; i++) {
	    //retrieve color values for this pixel
	    //|256)&255 operation forces java to interpret
	    //the byte value as unsigned (which is how it is intended)
	    int rValue = (imageData.rvals[i]|256)&255;
	    int gValue = (imageData.gvals[i]|256)&255;
	    int bValue = (imageData.bvals[i]|256)&255;
	    //calculate appropriate grayscale value
	    int grayscaleValue;
	    if (conversionType == AVERAGE_VALUE) {
		grayscaleValue = (rValue+gValue+bValue)/3;
	    }
	    else {
		grayscaleValue = max(rValue, gValue, bValue);
	    }
	    //store grayscale value in the specified channel
	    //and set other channels to zero
	    if (finalChannel == RED) {
	    imageData.rvals[i] = (byte)grayscaleValue;
	    imageData.gvals[i] = imageData.bvals[i] = 0;
	    }
	    else if (finalChannel == GREEN) {
		imageData.gvals[i] = (byte)grayscaleValue;
		imageData.rvals[i] = imageData.bvals[i] = 0;
	    }
	    else {
		imageData.bvals[i] = (byte)grayscaleValue;
		imageData.rvals[i] = imageData.gvals[i] = 0;		
	    }
	}
	super.process(imageData);
    }
}
