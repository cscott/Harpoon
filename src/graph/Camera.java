// Camera.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import ipaq.IPaqVideo;

/**
 * {@link Camera} is a {@link Node} that is a source of {@link ImageData}s.
 * Do <code>(new Camera(foo)).run()</code> to send images from the camera
 * to foo.
 *
 * Depending on the capabilities of the camera, you can set various 
 * properties.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class Camera extends Node {
    
    private IPaqVideo ipaq;
    private byte brightness, contrast, fps, gain;
    private boolean poll, flip;
    private int width, height;
    
    /** Construct a new {@link Camera}.
     *
     *  @param out The node to send images to.
     */
    public Camera(Node out) {
	this(128, 128, 5, 0, 0, 0, 640, 480);
    }

    /** Construct a new {@link Camera} with the specified properties.
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
    public Camera(byte brightness, byte contrast, 
		  byte fps, byte gain, boolean poll,
		  boolean flip, int width, int height, Node out) {
	super(out);
	ipaq = new IPaqVideo(this.brightness = brightness, 
			     this.contrast = contrast,
			     this.fps = fps, 
			     this.gain = gain, 
			     this.poll = poll, 
			     this.flip = flip, 
			     this.width = width, 
			     this.height = height);
    }
    
    /** Set the brightness of the image. 
     *
     *  @param brightness New brightness (0-255).
     */
    public void setBrightness(byte brightness) {
	ipaq.setProperties(this.brightness = brightness, this.contrast,
			   this.fps, this.gain, this.poll,
			   this.flip, this.width, this.height);
    }

    /** Set the contrast of the image.
     *
     *  @param contrast New contrast (0-255).
     */
    public void setContrast(byte contrast) {
	ipaq.setProperties(this.brightness, this.contrast = contrast,
			   this.fps, this.gain, this.poll,
			   this.flip, this.width, this.height);
    }

    /** Set the new shutter speed (FPS).
     *
     *  @param fps New frame rate (1-45).
     */
    public void setFPS(byte fps) {
	ipaq.setProperties(this.brightness, this.contrast,
			   this.fps = fps, this.gain, this.poll,
			   this.flip, this.width, this.height);
    }

    /** Set the new gain.
     *
     *  @param gain New gain (0-4).
     */
    public void setGain(byte gain) {
	ipaq.setProperties(this.brightness, this.contrast,
			   this.fps, this.gain = gain, this.poll,
			   this.flip, this.width, this.height);
    }

    /** Set whether to do read polling.
     *
     *  @param poll New poll.
     */
    public void setPoll(boolean poll) {
	ipaq.setProperties(this.brightness, this.contrast,
			   this.fps, this.gain, this.poll = poll,
			   this.flip, this.width, this.height);
    }

    /** Flip the image upside down. */
    public void flip() {
	ipaq.setProperties(this.brightness, this.contrast,
			   this.fps, this.gain, this.poll,
			   this.flip = !this.flip, this.width, 
			   this.height);
    }

    /** Set the size of the image.
     *
     *  @param width The new width   (640, 352, 320, 176, 160).
     *  @param height The new height (480, 288, 240, 144, 120).
     */
    public void setSize(int width, int height) {
	ipaq.setProperties(this.brightness, contrast,
			   this.fps, this.gain, this.poll,
			   this.flip, this.width = width, 
			   this.height = height);
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
	ipaq.setProperties(this.brightness = brightness, 
			   this.contrast = contrast,
			   this.fps = fps, 
			   this.gain = gain, 
			   this.poll = poll, 
			   this.flip = flip, 
			   this.width = width, 
			   this.height = height);
    }

    /** Process a node by generating a stream of {@link ImageData}'s 
     *  from the camera.
     */
    public void process(ImageData id) {
	int size = width*height;
	int num = 0;
	while (true) {
	    byte[] rvals = new byte[size];
	    byte[] gvals = new byte[size];
	    byte[] bvals = new byte[size];
	    
	    ipaq.capture(rvals, gvals, bvals);

	    ImageData id = ImageDataManip.create(null, rvals, gvals, bvals, 
						 width, height);
	    id.id = num++;
	    super.process(id);
	}
    }
}
