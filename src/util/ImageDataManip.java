// ImageDataManip.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StreamTokenizer;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipFile;

import java.util.jar.JarFile;

import imagerec.graph.ImageData;
import imagerec.graph.Cache;

/**
 * Various utilities for manipulating images contained in {@link ImageData}s. 
 *
 * This class should be the only class that knows the arguments to the 
 * {@link ImageData} constructor (called through the <code>create</code>
 * factory method).
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class ImageDataManip {

    /** Read a PPM from a file: automatically senses whether it's a GZIP'ed file
     *  (by looking for <code>.gz</code> in the fileName), and whether it's a P3 or P6
     *  PPM.
     *
     *  @param fileName The name of the file to read.
     *  @return an {@link ImageData} that contains an image read from the file.
     */
    public static ImageData readPPM(String fileName) {
	try {
	    InputStream st = new FileInputStream(fileName);
	    return readPPM((fileName.indexOf(".gz")!=-1)?(new GZIPInputStream(st)):st);
	} catch (IOException e) {
	    throw new Error(e);
	}
    }

    /** Read a PPM from a {@link JarFile} or {@link ZipFile}:
     *  automatically senses whether it's a GZIP'ed entry (by looking
     *  for <code>.gz</code> in the fileName), and whether it's a P3
     *  or P6 PPM.
     *
     *  @param zipFile The Zip/JarFile to grab the entry from.
     *  @param entryName The entry containing the image to load.  
     */
    public static ImageData readPPM(ZipFile zipFile, String entryName) {
	try {
	    InputStream st = zipFile.getInputStream(zipFile.getEntry(entryName));
	    return readPPM((entryName.indexOf(".gz")!=-1)?(new GZIPInputStream(st)):st);
	} catch (IOException e) {
	    throw new Error(e);
	}
    }

    /** Read a PPM from a <code>byte[]</code>.  
     *  Automatically senses whether a P3 or P6 PPM.
     *
     *  @param frame The <code>byte[]></code> containing the image data in PPM format.
     *  @return an {@link ImageData} that contains an image read from 
     *          the <code>byte[]</code>
     */
    public static ImageData readPPM(byte[] frame) {
	return readPPM(new ByteArrayInputStream(frame));
    }

    /** Read a PPM from an {@link InputStream}. 
     *  Automatically senses whether a P3 or P6 PPM.
     *
     *  @param st The {@link InputStream} containing the image data in PPM format.
     *  @return an {@link ImageData} that contains an image read from the 
     *          {@link InputStream}.
     */ 
    public static ImageData readPPM(InputStream st) {
	try {
	    ImageData id = new ImageData();
	    InputStreamReader r = new InputStreamReader(st);
	    StreamTokenizer s = new StreamTokenizer(r);
	    s.eolIsSignificant(false);
	    s.parseNumbers();
	    s.commentChar('#');
	    s.nextToken();
	    if (s.ttype!=s.TT_WORD) throw new IOException("Not a PPM!");
	    int type = 0;
	    if (s.sval.startsWith("P6")) {
		type = 1;
	    } else if (s.sval.startsWith("P3")) {
		type = 2;
	    } else {
		throw new IOException("Not a PPM!");
	    }
	    s.nextToken();
	    if (s.ttype!=s.TT_NUMBER) throw new IOException("Not a PPM!");
	    int width=(int)s.nval;
	    s.nextToken();
	    if (s.ttype!=s.TT_NUMBER) throw new IOException("Not a PPM!");
	    int height=(int)s.nval;
	    s.nextToken(); /* This is to eat the number of colors.  Optional?? */
	    if (s.ttype!=s.TT_NUMBER) throw new IOException("Not a PPM!");
	    byte[] rvals = new byte[width*height];
	    byte[] gvals = new byte[width*height];
	    byte[] bvals = new byte[width*height];
	    if (type == 1) {
		char[] cbuf=new char[3*width*height];
		r.read(cbuf);
		int j = 0;
		for (int i=0; i<width*height; i++) {
		    rvals[i] = (byte)cbuf[j++];
		    gvals[i] = (byte)cbuf[j++];
		    bvals[i] = (byte)cbuf[j++];
		}
	    } else {
		for (int i=0; i<width*height; i++) {
		    s.nextToken();
		    if (s.ttype!=s.TT_NUMBER) throw new IOException("Not a PPM!");
		    rvals[i] = (byte)s.nval;
		    s.nextToken();
		    if (s.ttype!=s.TT_NUMBER) throw new IOException("Not a PPM!");
		    gvals[i] = (byte)s.nval;
		    s.nextToken();
		    if (s.ttype!=s.TT_NUMBER) throw new IOException("Not a PPM!");
		    bvals[i] = (byte)s.nval;
		}
	    }
	    return create(null, rvals, gvals, bvals, width, height);
	} catch (IOException e) {
	    throw new Error(e);
	}
    }

    /** Write an {@link ImageData} to the file named by <code>fileName</code>
     *  in PPM P6 format.
     *
     *  @param id The {@link ImageData} containing the image to write.
     *  @param fileName The name of the file to write the image to.
     */
    public static void writePPM(ImageData id, String fileName) {
	writePPM(id, fileName, false);
    }

    /** Write an {@link ImageData} to the file named by <code>fileName</code>
     *  in PPM P3 or P6 format.
     *
     *  @param id The {@link ImageData} containing the image to write.
     *  @param fileName The name of the file to write the image to.
     *  @param P3 Whether the file is to be written in P3 or P6 format.
     */
    public static void writePPM(ImageData id, String fileName, boolean P3) {
	try {
	    OutputStream s = new FileOutputStream(fileName);
	    writePPM(id, (fileName.indexOf(".gz")!=-1)?(new GZIPOutputStream(s)):s, P3);
	} catch (Exception e) {
	    throw new Error(e);
	}
    }

    /** Write an {@link ImageData} to a <code>byte[]</code> in PPM P6 format.
     *
     *  @param id The {@link ImageData} to write to a PPM <code>byte[]</code>.
     *  @return The <code>byte[]</code> that represents the image in PPM P6 format.
     */
    public static byte[] writePPM(ImageData id) {
	return writePPM(id, false);
    }

    /** Write an {@link ImageData} to a <code>byte[]</code> in PPM P3 or P6 format.
     *
     *  @param id The {@link ImageData} to write to a PPM <code>byte[]</code>.
     *  @param P3 Whether the <code>byte[]</code> is to be written in P3 or P6 format.
     */
    public static byte[] writePPM(ImageData id, boolean P3) {
	ByteArrayOutputStream b = new ByteArrayOutputStream();
	writePPM(id, b, P3);
	return b.toByteArray();
    }

    /** Write an {@link ImageData} to an {@link OutputStream} in PPM P3 or P6 format.
     * 
     *  @param id The {@link ImageData} to write to a PPM {@link OutputStream}.
     *  @param s The {@link OutputStream} to write to.
     *  @param P3 Whether the {@link OutputStream} is to be written in P3 or P6 format.
     */
    public static void writePPM(ImageData id, OutputStream s, boolean P3) {
	try {
	    PrintWriter out = new PrintWriter(s);
	    if (P3) {
		out.println("P3 "+id.width+" "+id.height+" 255");
		for (int i=0; i<id.width*id.height; i++) {
		    out.println(((id.rvals[i]|256)&255)+" "+
				((id.gvals[i]|256)&255)+" "+
				((id.bvals[i]|256)&255));
		}
	    } else {
		out.println("P6 "+id.width+" "+id.height+" 255");
		int j = 0;
		char[] cbuf = new char[3*id.width*id.height];
		for (int i=0; i<id.width*id.height; i++) {
		    cbuf[j++] = (char)((id.rvals[i]|256)&255);
		    cbuf[j++] = (char)((id.gvals[i]|256)&255);
		    cbuf[j++] = (char)((id.bvals[i]|256)&255);
		}
		out.write(cbuf);
	    }
	    out.close();
	} catch (Exception e) {
	    throw new Error(e);
	}
    }

    /** Crop the image in an {@link ImageData} to <code>(x,y)-(x+width,y+height)</code>.
     *
     *  @param id The {@link ImageData} to crop.
     *  @param x The left most <code>x</code> position of the new image 
     *           in old image coordinates.
     *  @param y The upper most <code>y</code> position of the new image
     *           in old image coordinates.
     *  @param width The <code>width</code> of the new image.
     *  @param height The <code>height</code> of the new image.
     *  @return The new {@link ImageData} containing the cropped image.
     *          The <code>(x,y)-(x+width,y+height)</code> and <code>id</code>
     *          are stored in the new image so that the original can be retrieved from
     *          a {@link Cache} if need be.
     */
    public static ImageData crop(ImageData id, int x, int y, int width, int height) {
	byte[] rvals = new byte[width*height+1];
	byte[] gvals = new byte[width*height+1];
	byte[] bvals = new byte[width*height+1];
	int newPos = 0;
	for (int j=y; j<y+width; j++) {
	    int oldPos = y*id.width+x;
	    System.arraycopy(id.rvals, oldPos, rvals, newPos, width);
	    System.arraycopy(id.gvals, oldPos, gvals, newPos, width);
	    System.arraycopy(id.bvals, oldPos, bvals, newPos, width);
	    newPos+=width;
	}
	return create(id, rvals, gvals, bvals, x, y, width, height);
    }

    /** Draw a red ellipse around the object at <code>(x,y)-(x+width,y+height)</code>.
     *  Draws outside the bounding box.
     *
     *  @param id The image containing the object to draw the ellipse around (mutate)
     *  @param x The left most <code>x</code> position of the object.
     *  @param y The upper most <code>y</code> position of the object.
     *  @param width The <code>width</code> of the object.
     *  @param height The <code>height</code> of the object.
     */
    public static void ellipse(ImageData id, int x, int y, int width, int height) {
	ellipse(id, x, y, width, height, true);
    }

    /** Draw a red ellipse around the object at <code>(x,y)-(x+width,y+height)</code>.
     *
     *  @param id The image containing the object to draw the ellipse around (mutate)
     *  @param x The left most <code>x</code> position of the object.
     *  @param y The upper most <code>y</code> position of the object.
     *  @param width The <code>width</code> of the object.
     *  @param height The <code>height</code> of the object.
     *  @param outside Draw the ellipse outside or inside the bounding box.
     */
    public static void ellipse(ImageData id, int x, int y, int width, int height,
			       boolean outside) {
	double r2 = outside?Math.sqrt(2.0):1.0;
	double rx = (((double)width)/2.0)*r2;
	double ry = (((double)height)/2.0)*r2;
	double ox = (((double)width)/2.0)+((double)x);
	double oy = (((double)height)/2.0)+((double)y);
	double inc = Math.PI/(((double)(width+height))*r2);
	for (double theta = 0; theta < 2*Math.PI; theta += inc) {
	    int i = (int)(rx*Math.cos(theta)+ox);
	    int j = (int)(ry*Math.sin(theta)+oy);
	    if ((i>=0)&&(i<id.width)&&
		(j>=0)&&(j<id.height)) {
		id.rvals[i+id.width*j] = (byte)255;
	    }
	}
    }

    /** Scale an image to a specified width and height.
     *
     *  @param id The image to scale.
     *  @param width The new width.
     *  @param height The new height.
     *  @return A new {@link ImageData} containing the scaled image.
     */
    public static ImageData scale(ImageData id, int width, int height) {
	byte[] rvals = new byte[width*height+1];
	byte[] gvals = new byte[width*height+1];
	byte[] bvals = new byte[width*height+1];
	for (int i=0; i<width; i++) {
	    for (int j=0; j<height; j++) {
		int newPos = i+width*j;
		int pos = (int)((((double)i)/((double)width))*((double)id.width)) +
		    id.width*((int)((((double)j)/((double)height))*((double)id.height)));
		rvals[newPos]=id.rvals[pos];
		gvals[newPos]=id.gvals[pos];
		bvals[newPos]=id.bvals[pos];
	    }
	}

	return create(id, rvals, gvals, bvals, 0, 0, width, height);
    }

    /** Clone an {@link ImageData}.
     *
     *  @param id The {@link ImageData} to clone.
     */
    public static ImageData clone(ImageData id) {
	return create(id, (id.rvals!=null)?(byte[])id.rvals.clone():null,
		      (id.gvals!=null)?(byte[])id.gvals.clone():null,
		      (id.bvals!=null)?(byte[])id.bvals.clone():null,
		      id.width, id.height);
    }

    /** Factory to create an {@link ImageData} given partial information.
     *
     *  @param id The {@link ImageData} to grab unspecified information
     *            from (may be <code>null</code>, in which case defaults
     *            are used).
     *  @param rvals The red <code>byte[]</code> of intensities.
     *  @param gvals The green <code>byte[]</code> of intensities.
     *  @param bvals The blue <code>byte[]</code> of intensities.
     *  @param width The width of the image in pixels.
     *  @param height The height of the image in pixels.
     */
    public static ImageData create(ImageData id, 
				   byte[] rvals, byte[] gvals, byte[] bvals,
				   int width, int height) {
	if (id != null) {
	    return create(id, rvals, gvals, bvals, id.x, id.y, width, height);
	} else {
	    return create(id, rvals, gvals, bvals, 0, 0, width, height);
	}
    }

    /** Factory to create an {@link ImageData} given partial information.
     *
     *  @param id The {@link ImageData} to grab unspecified information
     *            from (may be <code>null</code>, in which case defaults
     *            are used).
     *  @param rvals The red <code>byte[]</code> of intensities.
     *  @param gvals The green <code>byte[]</code> of intensities.
     *  @param bvals The blue <code>byte[]</code> of intensities.
     *  @param x The x coordinate of the upper left pixel.
     *  @param y The y coordinate of the upper left pixel.
     *  @param width The width of the image in pixels.
     *  @param height The height of the image in pixels.
     */
    public static ImageData create(ImageData id, 
				   byte[] rvals, byte[] gvals, byte[] bvals,
				   int x, int y, int width, int height) {
	if (id != null) {
	    return new ImageData(rvals, gvals, bvals,
				 x, y, width, height,
				 id.time, id.id, id.command, id.receiverID,
				 id.c1, id.c2, id.c3);
	} else {
	    return new ImageData(rvals, gvals, bvals,
				 x, y, width, height,
				 0, 0, 0, 0, (float)0.0, (float)0.0, (float)0.0);
	}
    }
}
