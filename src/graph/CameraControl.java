// CameraControl.java, created by benster 5/31/03
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

public class CameraControl extends Node{
    private Camera myCamera;
    private int currentContrast;
    private static final int MAX_CONTRAST = 255;
    private static final int MIN_CONTRAST = 0;
    private static final int DEFAULT_CONTRAST = 127;
    private static final int DELTA_CONTRAST = 5;

    private int currentGain;
    private static final int MAX_GAIN = 4;
    private static final int MIN_GAIN = 0;
    private static final int DEFAULT_GAIN = 2;
    private static final int DELTA_GAIN = 1;

    private int currentBrightness;
    private static final int MAX_BRIGHTNESS = 255;
    private static final int MIN_BRIGHTNESS = 0;
    private static final int DEFAULT_BRIGHTNESS = 255;
    private static final int DELTA_BRIGHTNESS = 5;

    private int currentFrameRate;
    private static final int MAX_FRAME_RATE = 45;
    private static final int MIN_FRAME_RATE = 1;
    private static final int DEFAULT_FRAME_RATE = 15;
    private static final int DELTA_FRAME_RATE = 3;
   

    public CameraControl(Camera c) {
	super(null);
	init(c);
    }

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
