// IPaqVideo.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package ipaq;

/**
 * This is a simulation of the interface to the Philips backpaq camera.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class IPaqVideo {

    private int width;
    private int height;

    /** Construct a new {@link IPaqVideo} to interface with the camera */
    public IPaqVideo() {
    }

    /** Construct a new {@link IPaqVideo} to interface with the camera,
     *  and set the following properties.
     *
     *  @param brightness The desired brightness of the image (0-255).
     *  @param contrast The desired contrast of the image (0-255).
     *  @param fps The desired frame rate (1-45).
     *  @param gain The desired gain of the camera (0-4).
     *  @param poll Whether to do read polling.
     *  @param flip Whether to flip the image.
     *  @param width The width of the image   (640, 352, 320, 176, 160).
     *  @param height The height of the image (480, 288, 240, 144, 120).
     */
    public IPaqVideo(byte brightness, byte contrast, byte fps, byte gain,
		     boolean poll, boolean flip, int width, int height) {
	setProperties(brightness, contrast, fps, gain, poll, flip, width, height);
    }

    /** Capture into 3 arrays of red, green, and blue values.
     *
     *  @param rvals Red value array
     *  @param gvals Green value array
     *  @param bvals Blue value array
     */
    public void capture(byte[] rvals, byte[] gvals, byte[] bvals) {
	if ((rvals.length<(width*height))||(gvals.length<(width*height))||
	    (bvals.length<(width*height))) {
	    throw new Error("Arrays are too small.");
	}
    }

    /** Capture into single byte[] of RGB triplets
     *
     *  @param vals Array of RGB triplets
     */
    public void capture(byte[] vals) {
	if (vals.length<(width*height)) {
	    throw new Error("Array is too small.");
	}
    }

    /** Set the properties of the camera.
     *
     *  @param brightness The desired brightness of the image (0-255).
     *  @param contrast The desired contrast of the image (0-255).
     *  @param fps The desired frame rate (1-45).
     *  @param gain The desired gain of the camera (0-4).
     *  @param poll Whether to do read polling.
     *  @param flip Whether to flip the image.
     *  @param width The width of the image   (640, 352, 320, 176, 160).
     *  @param height The height of the image (480, 288, 240, 144, 120).
     */
    public void setProperties(byte brightness, byte contrast,
			      byte fps, byte gain, boolean poll,
			      boolean flip, int width, int height) {
	if ((fps<1)||(fps>45)) {
	    throw new Error("Invalid fps.");
	}
	if ((gain<0)||(gain>4)) {
	    throw new Error("Invalid gain.");
	}
	if (!(((width==640)&&(height==480))||
	      ((width==352)&&(height==288))||
	      ((width==320)&&(height==240))||
	      ((width==176)&&(height==144))||
	      ((width==160)&&(height==120)))) {
	    throw new Error("Invalid width/height");
	}
	unsafeSetProperties(brightness, contrast, fps, 
			    gain, poll, flip, width, height);
    }

    /** Set the properties of the camera, but do not wast time checking
     *  for invalid values.
     *
     *  @param brightness The desired brightness of the image (0-255).
     *  @param contrast The desired contrast of the image (0-255).
     *  @param fps The desired frame rate (1-45).
     *  @param gain The desired gain of the camera (0-4).
     *  @param poll Whether to do read polling.
     *  @param flip Whether to flip the image.
     *  @param width The width of the image   (640, 352, 320, 176, 160).
     *  @param height The height of the image (480, 288, 240, 144, 120).
     */
    public void unsafeSetProperties(byte brightness, byte contrast,
				    byte fps, byte gain, boolean poll,
				    boolean flip, int width, int height) {
	System.out.print("brightness: "+brightness);
	System.out.print(", contrast: "+contrast);
	System.out.print(", fps: "+fps);
	System.out.println(", gain: "+gain);
	System.out.print("poll: "+poll);
	System.out.print(", flip: "+flip);
	System.out.print(", width: "+width);
	System.out.println(", height: "+height);
    }
}
