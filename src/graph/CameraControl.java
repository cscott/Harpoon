// CameraControl.java, created by benster 5/31/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/**
 * This class is intended to be in a pipeline that runs asynchronously
 * with a pipeline containing a {@link Camera} node.<br><br>
 *
 * It allows for user control of camera properties when coupled 
 * with an input node like {@link CameraControlKeyboard}.<br><br>
 *
 * The following properties of {@link Camera} may be modified:<br>
 *     Brightness<br>
 *     Contrast<br>
 *     Gain<br>
 *     Frame Rate<br>
 *<br>
 *
 * To set a particular value:<br>
 *   Tag an {@link ImageData} with one of the following {@link Command}
 * tags and, if necessary, set the appropriate value to the {@link ImageData}'s
 * <code>time</code> field.<br><br>
 *
 * Command.SET_BRIGHTNESS: Causes the {@link Camera}'s "Brightness" property
 * to be set to the value given in the {@link ImageData}'s <code>time</code>
 * field.<br>
 * Command.BRIGHTNESS_UP: Causes the {@link Camera}'s "Brightness" property
 * to be incremented by DELTA_BRIGHTNESS.<br>
 * Command.BRIGHTNESS_DOWN: Causes the {@link Camera}'s "Brightness" property
 * to be decremented by DELTA_BRIGHTNESS.<br>
 * Command.SET_CONTRAST: Causes the {@link Camera}'s "Contrast" property
 * to be set to the value given in the {@link ImageData}'s <code>time</code>
 * field.<br>
 * Command.CONTRAST_UP: Causes the {@link Camera}'s "Contrast" property
 * to be incremented by DELTA_CONTRAST.<br>
 * Command.CONTRAST_DOWN: Causes the {@link Camera}'s "Contrast" property
 * to be decremented by DELTA_CONTRAST.<br>
 * Command.SET_GAIN: Causes the {@link Camera}'s "Gain" property
 * to be set to the value given in the {@link ImageData}'s <code>time</code>
 * field.<br>
 * Command.GAIN_UP: Causes the {@link Camera}'s "Gain" property
 * to be incremented by DELTA_GAIN.<br>
 * Command.GAIN_DOWN: Causes the {@link Camera}'s "Gain" property
 * to be decremented by DELTA_GAIN.<br>
 * Command.SET_FRAME_RATE: Causes the {@link Camera}'s "Frame Rate" property
 * to be set to the value given in the {@link ImageData}'s <code>time</code>
 * field.<br>
 * Command.FRAME_RATE_UP: Causes the {@link Camera}'s "Frame Rate" property
 * to be incremented by DELTA_FRAME_RATE.<br>
 * Command.FRAME_RATE_DOWN: Causes the {@link Camera}'s "Frame Rate" property
 * to be decremented by DELTA_FRAME_RATE.<br>
 * <br>
 *
 * If the value specifed in the {@link ImageData}'s <code>time</code>
 * field is not within the appropriate range specified by this class's
 * constants, then the highest or lowest possible value
 * is substituted.
 
 *
 * This class assumes that the {@link Camera}'s properties are not
 * set by any other class.
 *
 * @see Camera
 * @see CameraControlKeyboard
 * @see Command
 *
 * @author Reuben Sterling <<a href="mailto:benster@mit.edu">benster@mit.edu</a>>
 */
public class CameraControl extends Node{
    /**
     * Reference to the {@link Camera} that this
     * {@link CameraControl} node will mutate.
     */
    private Camera myCamera;

    private int currentContrast;
    public static final int MAX_CONTRAST = 255;
    public static final int MIN_CONTRAST = 0;
    public static final int DEFAULT_CONTRAST = 127;
    public static final int DELTA_CONTRAST = 5;

    private int currentGain;
    public static final int MAX_GAIN = 4;
    public static final int MIN_GAIN = 0;
    public static final int DEFAULT_GAIN = 2;
    public static final int DELTA_GAIN = 1;

    private int currentBrightness;
    public static final int MAX_BRIGHTNESS = 255;
    public static final int MIN_BRIGHTNESS = 0;
    public static final int DEFAULT_BRIGHTNESS = 255;
    public static final int DELTA_BRIGHTNESS = 5;

    private int currentFrameRate;
    public static final int MAX_FRAME_RATE = 45;
    public static final int MIN_FRAME_RATE = 1;
    public static final int DEFAULT_FRAME_RATE = 15;
    public static final int DELTA_FRAME_RATE = 3;
   
    /**
     * Creates a new {@link CameraControl} node that will mutate
     * the specified {@link Camera}.
     *
     * @param c The {@link Camera} node that this {@link CameraControl}
     * node should mutate.
     *
     * @see Camera
     */
    public CameraControl(Camera c) {
	super(null);
	init(c);
    }

    /**
     * All constructors should call this private method
     * to properly initialize the object's fields.
     *
     * @param c The {@link Camera} node that this {@link CameraControl}
     * node should mutate.
     *
     * @see Camera
     */
    private void init(Camera c) {
	this.myCamera = c;
	this.currentContrast = DEFAULT_CONTRAST;
	myCamera.setContrast((byte)currentContrast);
	this.currentGain = DEFAULT_GAIN;
	myCamera.setGain((byte)currentGain);
	this.currentBrightness = DEFAULT_BRIGHTNESS;
	myCamera.setBrightness((byte)currentBrightness);
	this.currentFrameRate = DEFAULT_FRAME_RATE;
	myCamera.setFPS((byte)currentFrameRate);
    }

    /**
     * Reads the {@link Command} tag from the specified {@link ImageData}
     * and takes the appropriate action on the {@link Camera} specified
     * in the constructor.
     *
     * @param id The {@link ImageData} carrying the information
     * to modify the {@link Camera} specified in the constructor.
     *
     * @see Camera
     * @see Command
     */
    public void process(ImageData id) {
	switch (Command.read(id)) {
	case Command.CONTRAST_UP: {
	    currentContrast += DELTA_CONTRAST;
	    if (currentContrast > MAX_CONTRAST)
		currentContrast = MAX_CONTRAST;
	    System.out.println("CameraControl: contrast="+currentContrast);
	    myCamera.setContrast((byte)currentContrast);
	    break;
	}
	case Command.CONTRAST_DOWN: {
	    currentContrast -= DELTA_CONTRAST;
	    if (currentContrast < MIN_CONTRAST)
		currentContrast = MIN_CONTRAST;
	    System.out.println("CameraControl: contrast="+currentContrast);
	    myCamera.setContrast((byte)currentContrast);
	    break;
	}
	case Command.GAIN_DOWN: {
	    currentGain -= DELTA_GAIN;
	    if (currentGain < MIN_GAIN)
		currentGain = MIN_GAIN;
	    System.out.println("CameraControl: gain="+currentGain);
	    myCamera.setGain((byte)currentGain);
	    break;
	}
	case Command.GAIN_UP: {
	    currentGain += DELTA_GAIN;
	    if (currentGain > MAX_GAIN)
		currentGain = MAX_GAIN;
	    System.out.println("CameraControl: gain="+currentGain);
	    myCamera.setGain((byte)currentGain);
	    break;
	}	    
	case Command.BRIGHTNESS_DOWN: {
	    currentBrightness -= DELTA_BRIGHTNESS;
	    if (currentBrightness < MIN_BRIGHTNESS)
		currentBrightness = MIN_BRIGHTNESS;
	    System.out.println("CameraControl: brightness="+currentBrightness);
	    myCamera.setBrightness((byte)currentBrightness);
	    break;
	}
	case Command.BRIGHTNESS_UP: {
	    currentBrightness += DELTA_BRIGHTNESS;
	    if (currentBrightness > MAX_BRIGHTNESS)
		currentBrightness = MAX_BRIGHTNESS;
	    System.out.println("CameraControl: brightness="+currentBrightness);
	    myCamera.setBrightness((byte)currentBrightness);
	    break;
	}	    
	case Command.FRAME_RATE_DOWN: {
	    currentFrameRate -= DELTA_FRAME_RATE;
	    if (currentFrameRate < MIN_FRAME_RATE)
		currentFrameRate = MIN_FRAME_RATE;
	    System.out.println("CameraControl: framerate="+currentFrameRate);
	    myCamera.setFPS((byte)currentFrameRate);
	    break;
	}
	case Command.FRAME_RATE_UP: {
	    currentFrameRate += DELTA_FRAME_RATE;
	    if (currentFrameRate > MAX_FRAME_RATE)
		currentFrameRate = MAX_FRAME_RATE;
	    System.out.println("CameraControl: framerate="+currentFrameRate);
	    myCamera.setFPS((byte)currentFrameRate);
	    break;
	}	    
	case (Command.SET_CONTRAST): {
	    currentContrast = (int)id.time;
	    if (currentContrast > MAX_CONTRAST)
		currentContrast = MAX_CONTRAST;
	    if (currentContrast < MIN_CONTRAST)
		currentContrast = MIN_CONTRAST;
	    System.out.println("CameraControl: Contrast="+currentContrast);
	    myCamera.setContrast((byte)currentContrast);
	    break;
	}
	case (Command.SET_BRIGHTNESS): {
	    currentBrightness = (int)id.time;
	    if (currentBrightness > MAX_BRIGHTNESS)
		currentBrightness = MAX_BRIGHTNESS;
	    if (currentBrightness < MIN_BRIGHTNESS)
		currentBrightness = MIN_BRIGHTNESS;
	    System.out.println("CameraControl: Brightness="+currentBrightness);
	    myCamera.setBrightness((byte)currentBrightness);
	    break;
	}
	case (Command.SET_GAIN): {
	    currentGain = (int)id.time;
	    if (currentGain > MAX_GAIN)
		currentGain = MAX_GAIN;
	    if (currentGain < MIN_GAIN)
		currentGain = MIN_GAIN;
	    System.out.println("CameraControl: Gain="+currentGain);
	    myCamera.setGain((byte)currentGain);
	    break;
	}
	case (Command.SET_FRAME_RATE): {
	    currentFrameRate = (int)id.time;
	    if (currentFrameRate > MAX_FRAME_RATE)
		currentFrameRate = MAX_FRAME_RATE;
	    if (currentFrameRate < MIN_FRAME_RATE)
		currentFrameRate = MIN_FRAME_RATE;
	    System.out.println("CameraControl: FrameRate="+currentFrameRate);
	    myCamera.setFPS((byte)currentFrameRate);
	    break;
	}
	default: {
	    System.out.println("Unrecognized command");
	}
	}
	super.process(id);
	    
    }
}
