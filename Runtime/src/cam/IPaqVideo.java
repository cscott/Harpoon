// IPaqVideo.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package ipaq;

/** 
 * This is a minimal interface to the Philips backpaq camera.
 *
 * The Philips camera largely follows the Video4Linux standard,
 * but has an FPGA and additional features.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class IPaqVideo {

    /** Construct a new {@link IPaqVideo} to interface with the camera,
     *  and set up the camera with the default values.
     */
    public IPaqVideo() {
	setup();
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
	setup();
	setProperties(brightness, contrast, fps, gain, poll, flip, width, height);
    }

    /** Initialize the camera. */
    private native void setup();

    /** Capture into 3 arrays of red, green, and blue values. 
     *
     *  @param rvals Red value array
     *  @param gvals Green value array
     *  @param bvals Blue value array
     */
    public native void capture(byte[] rvals, byte[] gvals, byte[] bvals);

    /** Capture into a single byte[] of RGB triplets
     *
     *  @param vals Array of RGB triplets
     */
    public native void capture(byte[] vals);

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

    /** Set the properties of the camera, but do not waste time checking
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
    public native void unsafeSetProperties(byte brightness, byte contrast,
					   byte fps, byte gain, boolean poll,
					   boolean flip, int width, int height);
}
