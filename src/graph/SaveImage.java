// SaveImage.java, created by benster
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.ImageDataManip;

public class SaveImage extends Node {

    private int n;
    private String filename;
    private boolean p3;



    /**
     * Saves the image with <code>id.id == n</code>. Images are
     * saved in PPM 6 format.
     * @param filename The filename under which to save the ppm file.
     *        A filetype extention is NOT appended. You must specify
              one yourself. A '.gz' added to the filename,
	      will cause the image to be gzipped.
     * @param n The number of the image to be saved.
     */
    public SaveImage(String filename, int n) {
	init(filename, n, true);
    }

    /**
     * Saves the image with <code>id.id == n</code>. Images are
     * saved in PPM 6 format.
     * @param filename The filename under which to save the ppm file.
     *        A filetype extention is NOT appended. You must specify
              one yourself. A '.gz' added to the filename,
	      will cause the image to be gzipped.
     * @param n The number of the image to be saved.
     * @param p3 Indicates whether to save in ppm 3 or ppm 6 format.
     *           If <code>true</code>, then the image will be saved in
     *           ppm 3 format, otherwise it will be saved in ppm 6 format.
     */
    public SaveImage(String filename, int n, boolean p3) {
	init(filename, n, p3);
    }

    /** 
     * Method should be called by all constructors to
     * initialize object fields.
     */
    public void init(String filename, int n, boolean p3) {
	this.n = n;
	this.filename = filename;
	this.p3 = p3;
    }

    /**
     * Reads the {@link ImageData}'s ID#, and saves it to a file
     * if it is the right one. Then passes the {@link ImageData}
     * onto the next {@link Node}s.
     * 
     * @param id The {@link ImageData} that will be passed on to the next {@link Node}s.
     */
    public void process(ImageData id) {
	if (id.id == n) {
	    ImageDataManip.writePPM(id, filename, p3);
	}
	super.process(id);
    }
}
